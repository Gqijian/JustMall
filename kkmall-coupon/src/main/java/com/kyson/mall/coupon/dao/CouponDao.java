package com.kyson.mall.coupon.dao;

import com.kyson.mall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author kyson
 * @email kysonxxxx@gmail.com
 * @date 2022-08-01 10:02:12
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
