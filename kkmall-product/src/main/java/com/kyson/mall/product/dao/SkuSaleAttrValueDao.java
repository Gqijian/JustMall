package com.kyson.mall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kyson.mall.product.entity.SkuSaleAttrValueEntity;
import com.kyson.mall.product.vo.SkuItemSaleAttrVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku销售属性&值
 *
 * @author kyson
 * @email kysonxxxx@gmail.com
 * @date 2022-07-29 16:39:51
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(@Param("spuId") Long spuId);

    List<String> getSkuSaleAttrValuesAsStringList(@Param("skuId") Long skuId);
}
