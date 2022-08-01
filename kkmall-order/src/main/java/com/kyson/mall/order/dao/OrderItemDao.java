package com.kyson.mall.order.dao;

import com.kyson.mall.order.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项信息
 * 
 * @author kyson
 * @email kysonxxxx@gmail.com
 * @date 2022-08-01 11:50:56
 */
@Mapper
public interface OrderItemDao extends BaseMapper<OrderItemEntity> {
	
}
