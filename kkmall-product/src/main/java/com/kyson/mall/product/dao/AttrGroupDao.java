package com.kyson.mall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kyson.mall.product.entity.AttrGroupEntity;
import com.kyson.mall.product.vo.SpuItemAttrGroupVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性分组
 * 
 * @author kyson
 * @email kysonxxxx@gmail.com
 * @date 2022-07-29 16:39:52
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(@Param("spuId") Long spuId, @Param("catalogId") Long catalogId);

}
