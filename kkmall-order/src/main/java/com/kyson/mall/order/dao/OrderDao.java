package com.kyson.mall.order.dao;

import com.kyson.mall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author kyson
 * @email kysonxxxx@gmail.com
 * @date 2022-08-01 11:50:55
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
