package com.kyson.mall.coupon.dao;

import com.kyson.mall.coupon.entity.MemberPriceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品会员价格
 * 
 * @author kyson
 * @email kysonxxxx@gmail.com
 * @date 2022-08-01 10:02:12
 */
@Mapper
public interface MemberPriceDao extends BaseMapper<MemberPriceEntity> {
	
}
