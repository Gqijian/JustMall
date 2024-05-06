package com.kyson.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kyson.common.to.mq.SecKillOrderTo;
import com.kyson.common.utils.PageUtils;
import com.kyson.mall.order.entity.OrderEntity;
import com.kyson.mall.order.vo.*;

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

    PageUtils queryPageWithItem(Map<String, Object> params);

    /**
     * 订单确认页返回数据
     * @return
     */
    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);

    OrderEntity getOrderStatus(String orderSn);

    void closeOrder(OrderEntity entity);

    OrderEntity getOrderByOrderSn(String orderSn);

    /**
     * 获取当前订单的支付信息
     * @param orderSn
     * @return
     */
    PayVo getOrderPay(String orderSn);

    String handlePayResult(PayAsyncVo vo);

    void createSecKillOrder(SecKillOrderTo secKillOrder);
}

