package com.kyson.mall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kyson.common.utils.PageUtils;
import com.kyson.common.utils.Query;
import com.kyson.common.utils.R;
import com.kyson.mall.ware.dao.WareSkuDao;
import com.kyson.mall.ware.entity.WareSkuEntity;
import com.kyson.mall.ware.feign.ProducktFeignService;
import com.kyson.mall.ware.service.WareSkuService;
import com.kyson.mall.ware.vo.SkuHasStockVo;
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
    private ProducktFeignService producktFeignService;

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

}