package com.kyson.mall.ware.service.impl;

import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kyson.common.exception.NoStockException;
import com.kyson.common.to.mq.OrderTo;
import com.kyson.common.to.mq.StockDetailTo;
import com.kyson.common.to.mq.StockLockedTo;
import com.kyson.common.utils.PageUtils;
import com.kyson.common.utils.Query;
import com.kyson.common.utils.R;
import com.kyson.mall.ware.dao.WareSkuDao;
import com.kyson.mall.ware.entity.WareOrderTaskDetailEntity;
import com.kyson.mall.ware.entity.WareOrderTaskEntity;
import com.kyson.mall.ware.entity.WareSkuEntity;
import com.kyson.mall.ware.feign.OrderFeignService;
import com.kyson.mall.ware.feign.ProducktFeignService;
import com.kyson.mall.ware.service.WareOrderTaskDetailService;
import com.kyson.mall.ware.service.WareOrderTaskService;
import com.kyson.mall.ware.service.WareSkuService;
import com.kyson.mall.ware.vo.OrderItemVo;
import com.kyson.mall.ware.vo.OrderVo;
import com.kyson.mall.ware.vo.SkuHasStockVo;
import com.kyson.mall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ProducktFeignService producktFeignService;

    @Autowired
    private WareOrderTaskService orderTaskService;

    @Autowired
    private WareOrderTaskDetailService orderTaskDetailService;

    @Autowired
    private OrderFeignService orderFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params)
    {

        QueryWrapper<WareSkuEntity> wareSkuQueryWrapper = new QueryWrapper<>();

        String skuId = params.get("skuId").toString();

        if (!StringUtils.isEmpty(skuId)) {
            wareSkuQueryWrapper.eq("sku_id", skuId);
        }

        String wareId = params.get("wareId").toString();

        if (!StringUtils.isEmpty(wareId)) {
            wareSkuQueryWrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum)
    {
        //判断如果还没有这个库存记录 新增
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (entities == null || entities.size() < 1) {

            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            //TODO 远程查询sku的名字 如果失败 整个事务无需回滚  还有什么办法 异常不回滚
            /**
             * 如果失败 整个事务无需回滚
             * 1、try catch
             * 2、还有什么办法 异常不回滚
             */
            try {
                R info = producktFeignService.info(skuId);
                if (info.getCode() == 0) {
                    Map<String, Object> data = (Map<String, Object>) info.get("data");
                    skuEntity.setSkuName(data.get("skuName").toString());
                }
            } catch (Exception e) {

            }

            wareSkuDao.insert(skuEntity);

        } else {

            wareSkuDao.addStock(skuId, wareId, skuNum);
        }

    }

    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds)
    {

        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();

            //查询当前总库存
            //select sum(stock-stock_locked) from wms_ware_sku where sku_id =
            Long count = baseMapper.getSkuStock(skuId);
            vo.setSkuId(skuId);
            vo.setHasStock(count == null ? false : count > 0);
            return vo;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 为某订单锁定库存
     * (rollbackFor = NoStockException.class) 默认只要是运行时异常都会回滚
     * <p>
     * 库存解锁场景
     * 1、下订单成功 订单没有支持 被自动取消、 被用户手动取消。都要解锁库存
     * 2、业务异常导致订单回滚，之前锁定的库存自动解锁
     *
     * @param vo
     * @return
     */
    @Transactional
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo)
    {
        /**
         * 保存库存工作单的详情
         * 追溯
         */
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        orderTaskService.save(taskEntity);

        //按照下单收获地址 找到就近仓库，锁定库存

        //找到每个商品在哪个仓库有库存
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {

            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();

            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            //查询这个商品在哪里有库存
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());

        //锁定库存
        for (SkuWareHasStock hasStock : collect) {
            Boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();

            if (wareIds == null || wareIds.size() < 1) {
                //没有任何仓库有库存
                throw new NoStockException(skuId);
            }

            //如果每一个商品都锁定成功，将当前商品锁定了几件的工作单记录发送 mq
            //如果锁定失败 前面保存的工作单信息就回滚了 发送出去的消息 即使要解锁记录，由于查不到指定id 所以也不用解锁

            for (Long wareId : wareIds) {

                //成功就返回 1 ,否则就是0 这里返回值是受影响数据行数
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, hasStock.getNum());
                if (count == 1) {
                    skuStocked = true;

                    //TODO 告诉MQ 库存锁定成功
                    WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity(null, skuId, "", hasStock.getNum(), taskEntity.getId(), wareId, 1);
                    orderTaskDetailService.save(detailEntity);

                    StockLockedTo lockedTo = new StockLockedTo();
                    lockedTo.setId(taskEntity.getId());

                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(detailEntity, stockDetailTo);

                    //只发id 不行 防止回滚找不到数据
                    lockedTo.setDetail(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", lockedTo);
                    break;
                } else {
                    //当前仓库锁定失败 应该去下一个仓库

                }
            }

            if (skuStocked == false) {

                //当前商品在所有仓库都没锁住
                throw new NoStockException(skuId);
            }

        }
        //全部锁定成功

        return true;
    }

    @Override
    public void unLockStock(StockLockedTo to)
    {
        Long id = to.getId();   //库存工作单的id
        StockDetailTo detail = to.getDetail();
        Long skuId = detail.getSkuId();

        //解锁
        //1、查询数据库关于这个订单的锁定库存信息

        //数据库有

        //数据库没有  库存锁定失败 库存回滚了 这种情况无需解锁
        WareOrderTaskDetailEntity byId = orderTaskDetailService.getById(detail.getId());
        if (byId != null) {
            //解锁 要先检查订单是否成功了
            /**
             * 没有订单 必须解锁
             * 有订单
             *      订单状态：已取消 解锁
             */
            WareOrderTaskEntity taskEntity = orderTaskService.getById(to.getId());
            R r = orderFeignService.getOrderStatus(taskEntity.getOrderSn());
            if (r.getCode() == 0) {
                OrderVo orderVo = r.getData(new TypeReference<OrderVo>() {
                });

                if (orderVo == null || orderVo.getStatus() == 4) {
                    //订单已经被取消了
                    if(orderVo.getStatus() == 1){
                        unLockStock(skuId, detail.getWareId(), detail.getSkuNum(), detail.getId());
                        //channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                    }

                }
            } else {
                //消息拒绝以后重新发回队列，让别人再来解锁 消费消息解锁
                //channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
                throw new RuntimeException("远程服务失败");

            }

        } else {
            //无需解锁
            //channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }

    }

    private void unLockStock(Long skuId, Long wareId, Integer num, Long taskDetailId)
    {
        //库存解锁
        wareSkuDao.unLockStock(skuId, wareId, num);

        //更新库存工作单的状态
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(taskDetailId);
        entity.setLockStatus(2);    //变为已解锁
        orderTaskDetailService.updateById(entity);
    }

    /**
     * 防止订单服务卡顿 导致订单状态一直改变不了 库存消息优先到期，查询到订单状态是新建的，什么都不做
     * 导致卡顿的订单 永远无法解锁
     * @param to
     */
    @Transactional
    @Override
    public void unLockStock(OrderTo to)
    {

        String orderSn = to.getOrderSn();
        //查一下最新库存解锁状态，防止重复解锁库存
        WareOrderTaskEntity taskEntity = orderTaskService.getOrderTaskByOrderSn(orderSn);

        //按照工作单 找到所有没有解锁的库存 进行解锁
        List<WareOrderTaskDetailEntity> entities = orderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>()
                .eq("task_id", taskEntity.getId())
                .eq("lock_status", 1));
        for (WareOrderTaskDetailEntity entity : entities) {
            unLockStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum(), entity.getId());
        }
    }

    @Data
    class SkuWareHasStock {

        private Long skuId;

        private Integer num;

        private List<Long> wareId;
    }

}