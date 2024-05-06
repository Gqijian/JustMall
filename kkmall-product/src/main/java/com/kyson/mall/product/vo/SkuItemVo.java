package com.kyson.mall.product.vo;

import com.kyson.mall.product.entity.SkuImagesEntity;
import com.kyson.mall.product.entity.SkuInfoEntity;
import com.kyson.mall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {

    //sku 基本信息 pms_sku_info
    SkuInfoEntity info;

    //是否有货
    Boolean hasStock = true;

    //sku 图片信息 pms_sku_images
    List<SkuImagesEntity> images;

    //spu 销售属性组合

    List<SkuItemSaleAttrVo> saleAttr;

    //spu 介绍
    SpuInfoDescEntity desp;

    //spu 规格参数信息
    List<SpuItemAttrGroupVo> groupAttrs;

    //当前商品的秒杀优惠信息
    SeckillInfoVo seckillInfo;

}


