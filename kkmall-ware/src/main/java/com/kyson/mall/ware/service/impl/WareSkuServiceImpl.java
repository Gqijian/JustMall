package com.kyson.mall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kyson.common.to.mq.StockDetailTo;
import com.kyson.common.to.mq.StockLockedTo;
import com.kyson.common.utils.PageUtils;
import com.kyson.common.utils.Query;
import com.kyson.common.utils.R;
import com.kyson.mall.ware.dao.WareSkuDao;
import com.kyson.mall.ware.entity.WareOrderTaskDetailEntity;
import com.kyson.mall.ware.entity.WareOrderTaskEntity;
import com.kyson.mall.ware.entity.WareSkuEntity;
import com.kyson.common.exception.NoStockException;
import com.kyson.mall.ware.feign.ProducktFeignService;
import com.kyson.mall.ware.service.WareOrderTaskDetailService;
import com.kyson.mall.ware.service.WareOrderTaskService;
import com.kyson.mall.ware.service.WareSkuService;
import com.kyson.mall.ware.vo.OrderItemVo;
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

    @Data
    class SkuWareHasStock {

        private Long skuId;

        private Integer num;

        private List<Long> wareId;
    }

}