package com.kyson.mall.ware.dao;

import com.kyson.mall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品库存
 * 
 * @author kyson
 * @email kysonxxxx@gmail.com
 * @date 2022-08-01 14:39:59
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {
	
}