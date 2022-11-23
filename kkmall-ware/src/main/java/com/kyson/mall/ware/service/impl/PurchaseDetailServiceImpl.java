package com.kyson.mall.ware.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.kyson.mall.ware.entity.WareSkuEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kyson.common.utils.PageUtils;
import com.kyson.common.utils.Query;

import com.kyson.mall.ware.dao.PurchaseDetailDao;
import com.kyson.mall.ware.entity.PurchaseDetailEntity;
import com.kyson.mall.ware.service.PurchaseDetailService;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params)
    {

        QueryWrapper<PurchaseDetailEntity> queryWrapper = new QueryWrapper<PurchaseDetailEntity>();

        String key = params.get("key").toString();
        if (!StringUtils.isEmpty(key))
        {
            queryWrapper.and(wrapper -> {
                wrapper.eq("purchase_id", key).or().eq("sku_id", key);
            });
        }

        String status = params.get("status").toString();
        if (!StringUtils.isEmpty(status))
        {
            queryWrapper.eq("status", status);
        }

        String wareId = params.get("wareId").toString();
        if (!StringUtils.isEmpty(wareId))
        {
            queryWrapper.eq("ware_id", wareId);
        }

        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<PurchaseDetailEntity> listDetailByPurchaseId(Long id)
    {
        List<PurchaseDetailEntity> purchaseId = this.list(new QueryWrapper<PurchaseDetailEntity>().eq("purchase_id", id));
        return purchaseId;
    }

}