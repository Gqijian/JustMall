package com.kyson.mall.order;

import com.kyson.mall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
class KkmallOrderApplicationTests {

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 1、创建 Exchange Queue Binding
     *      使用 AmqpAdmin 创建
     * 2、收发消息
     */
    @Test
    void contextLoads()
    {
    }

    @Test
    public void sendMessage(){

        OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
        reasonEntity.setId(1L);

        String msg = "hello world";

        //如果消息发送的是个对象，必须实现 Serializable 接口
        rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", reasonEntity, new CorrelationData(UUID.randomUUID().toString()));
        log.info("消息发送完成{}", reasonEntity);
    }

    @Test
    public void createExchange(){

        /**
         *
         * DirectExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments)
         */
        DirectExchange directExchange = new DirectExchange("hello-java-exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
    }

    @Test
    public void createQueue(){

        /**
         *
         * Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments)
         */
        Queue queue = new Queue("hello-java-queue" ,true, false, false);
        amqpAdmin.declareQueue(queue);
    }

    @Test
    public void createBinding(){

        /**
         * Binding(String destination, DestinationType destinationType, String exchange, String routingKey, @Nullable Map<String, Object> arguments) {
         *  String destination 目的地
         *  DestinationType destinationType 目的地类型
         *  String exchange 交换机
         *  String routingKey   路由键
         *
         *  把传入的交换机 和 那个目的地绑定，使用 routingKey 作为路由键
         */
        Binding binding = new Binding("hello-java-queue", Binding.DestinationType.QUEUE,
                "hello-java-exchange", "hello.java", null);
        amqpAdmin.declareBinding(binding);
    }
}
