package com.kyson.mall.ware.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig {

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 第一次连接rabbitmq 的时候才会创建
     */
//    @RabbitListener(queues = "stock.release.stock.queue")
//    public void handle(Message message){
//         只能有一个消费者
//    }

    /**
     * 消息转 json
     * @return
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }


    @Bean
    public Exchange stockEventExchange()
    {

        //String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new TopicExchange("stock-event-exchange", true, false);
    }

    @Bean
    public Queue stockReleaseStockQueue()
    {

        return new Queue("stock.release.stock.queue", true, false, false);
    }

    /**
     * 延迟队列
     * @return
     */
    @Bean
    public Queue stockDelayQueue()
    {
        Map<String, Object> arguments = new HashMap<>();
        //死信交换机
        arguments.put("x-dead-letter-exchange", "stock-event-exchange");
        //死信路由键
        arguments.put("x-dead-letter-routing-key", "stock.release");
        //消息过期时间 1分钟
        arguments.put("x-message-ttl", 120000);

        return new Queue("stock.delay.queue", true, false, false, arguments);
    }

    @Bean
    public Binding stockReleaseBinding()
    {
        //String destination, DestinationType destinationType, String exchange, String routingKey, @Nullable Map<String, Object> arguments

        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#",
                null);
    }

    @Bean
    public Binding stockLockedBinding()
    {
        //String destination, DestinationType destinationType, String exchange, String routingKey, @Nullable Map<String, Object> arguments

        return new Binding("stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.locked",
                null);
    }
}
