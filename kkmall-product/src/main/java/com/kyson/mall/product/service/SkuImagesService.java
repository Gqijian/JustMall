package com.kyson.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kyson.common.utils.PageUtils;
import com.kyson.mall.product.entity.SkuImagesEntity;

import java.util.List;
import java.util.Map;

/**
 * sku图片
 *
 * @author kyson
 * @email kysonxxxx@gmail.com
 * @date 2022-07-29 16:39:51
 */
public interface SkuImagesService extends IService<SkuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuImagesEntity> getImagesBySkuId(Long skuId);
}

