package com.kyson.mall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kyson.common.to.SkuReductionTo;
import com.kyson.common.utils.PageUtils;
import com.kyson.mall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author kyson
 * @email kysonxxxx@gmail.com
 * @date 2022-08-01 10:02:12
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuReduction(SkuReductionTo skuReductionTo);
}

