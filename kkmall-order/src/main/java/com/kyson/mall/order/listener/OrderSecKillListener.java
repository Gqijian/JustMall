package com.kyson.mall.order.listener;

import com.kyson.common.to.mq.SecKillOrderTo;
import com.kyson.mall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RabbitListener(queues = "stock.seckill.order.queue")
@Component
public class OrderSecKillListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void listener(SecKillOrderTo secKillOrder, Channel channel, Message message) throws IOException
    {
        //创建秒杀单的详细信息
        log.info("创建秒杀单的详细信息 ： " + secKillOrder);
        orderService.createSecKillOrder(secKillOrder);

    }
}
