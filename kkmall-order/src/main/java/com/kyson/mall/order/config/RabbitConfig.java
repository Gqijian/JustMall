package com.kyson.mall.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class RabbitConfig {

    @Autowired
    RabbitTemplate rabbitTemplate;
    /**
     * 消息转 json
     * @return
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制 RabbitTemplate
     *
     * 只要消息抵达 broker ack 就为 true
     *
     * 1、服务端收到消息就回调
     * 2、消息正确抵达队列回调
     * 3、服务端确认（保证每个消息被正确消费，此时才可以删除此消息）
     *      假如受到很多消息，只有一个处理成功，然后宕机了，消息丢失，改成手动模式，只要没有明确告知 MQ 货物被签收。
     *      只要没有 ack ，消息就一直是 unacked 的。即使父无宕机，消息也不会丢失，会重新变为 ready ，重新链接会
     *      再次进行消费。
     *    手动ack签收
     */
    @PostConstruct  //RabbitConfig 对象创建完成以后，执行这个方法
    public void initRabbitTemplate(){
        //设置确认回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             *
             * @param correlationData   消息的唯一关联数据（消息的唯一id）
             * @param ack   消息是否成功收到
             * @param cause 失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause)
            {

                System.out.println("correlationData" + correlationData + "ack " + ack + "cause" + cause);
            }
        });

        //设置消息抵达队列的确认回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {

            /**
             *
             * 只要消息没有投递给指定队列，就触发这个失败回调，成功不回调
             *
             * @param message   投递失败的消息详细信息
             * @param replyCode 回复状态码
             * @param replyText 回复的文本
             * @param exchange  当时这个消息发给哪个交换机的
             * @param routingKey    消息用的哪个路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey)
            {

                System.out.println("fail message " + message);
            }
        });
    }

    /**
     * 消息正确抵达 queue 进行回调
     */

}
