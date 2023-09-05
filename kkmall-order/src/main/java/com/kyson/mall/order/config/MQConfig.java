package com.kyson.mall.order.config;

import com.kyson.mall.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MQConfig {

    @Bean
    public Exchange orderEventExchange()
    {

        //String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new TopicExchange("order-event-exchange", true, false);
    }

    @RabbitListener(queues = "order.release.order.queue")
    public void listener(OrderEntity entity, Channel channel, Message message) throws IOException
    {
        //因为是手动确认，所以需要自己回复

        System.out.println("受到过期的订单信息：准备关闭订单 " + entity.getOrderSn());
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    /**
     * 容器中的组件 无论是 Binding Queue 还是 Exchange 都会通过 @Bean 中自动创建
     * RabbitMQ中只要有，@Bean 属性发生变化，也不会覆盖，只能先删除队列，然后再启动
     *
     * @return
     */
    @Bean
    public Queue orderDelayQueue()
    {
        //String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments) {

        Map<String, Object> arguments = new HashMap<>();
        //死信交换机
        arguments.put("x-dead-letter-exchange", "order-event-exchange");
        //死信路由键
        arguments.put("x-dead-letter-routing-key", "order.release.order");
        //消息过期时间 1分钟
        arguments.put("x-message-ttl", 60000);

        return new Queue("order.delay.queue", true, false, false, arguments);
    }

    @Bean
    public Queue orderReleaseOrderQueue()
    {

        return new Queue("order.release.order.queue", true, false, false);
    }

    @Bean
    public Binding orderCreateOrderBinding()
    {
        //String destination, DestinationType destinationType, String exchange, String routingKey, @Nullable Map<String, Object> arguments

        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",
                null);
    }

    @Bean
    public Binding orderReleaseOrderBinding()
    {
        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",
                null);
    }
}
