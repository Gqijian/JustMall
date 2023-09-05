package com.kyson.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kyson.common.utils.PageUtils;
import com.kyson.mall.order.entity.OrderEntity;
import com.kyson.mall.order.vo.OrderConfirmVo;
import com.kyson.mall.order.vo.OrderSubmitVo;
import com.kyson.mall.order.vo.SubmitOrderResponseVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author kyson
 * @email kysonxxxx@gmail.com
 * @date 2022-08-01 11:50:55
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 订单确认页返回数据
     * @return
     */
    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);
}

