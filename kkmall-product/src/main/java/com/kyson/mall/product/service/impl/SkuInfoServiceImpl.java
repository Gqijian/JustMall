package com.kyson.mall.product.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kyson.common.utils.PageUtils;
import com.kyson.common.utils.Query;

import com.kyson.mall.product.dao.SkuInfoDao;
import com.kyson.mall.product.entity.SkuInfoEntity;
import com.kyson.mall.product.service.SkuInfoService;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params)
    {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity)
    {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params)
    {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();

        String key = params.get("key").toString();
        if (!StringUtils.isEmpty(key))
        {
            wrapper.and(w -> {
                w.eq("sku_id", key).or().like("sku_name", key);
            });
        }
        String catelogId = params.get("catelogId").toString();
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId))
        {
            wrapper.eq("catelog_id", catelogId);
        }
        String brandId = params.get("brandId").toString();
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(catelogId))
        {
            wrapper.eq("brand_id", brandId);
        }
        String min = params.get("min").toString();
        if (!StringUtils.isEmpty(min))
        {
            wrapper.ge("price", min);
        }
        String max = params.get("max").toString();
        if (!StringUtils.isEmpty(max))
        {
            try
            {
                BigDecimal bigDecimal = new BigDecimal(max);

                if (bigDecimal.compareTo(new BigDecimal("0")) == 1)
                {

                    wrapper.le("price", max);
                }
            } catch (Exception e)
            {

            }

        }


        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

}