package com.kyson.mall.order;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 *
 * 1、 引入 amqp starter RabbitAutoConfiguration
 * 2、给容器自动配置了 RabbitTemplate AmqpAdmin
 *      所有属性都是 spring.rabbitmq
 *      @ConfigurationProperties( prefix = "spring.rabbitmq" )
 *      来自 RabbitProperties 类
 * 3、配置文件中配置
 * 4、@EnableRabbit
 * 5、监听消息 @RabbitListener 必须先有 @EnableRabbit
 *
 *    @RabbitListener 可以标记在类 + 方法上
 *    @RabbitHandler 只能标记在方法上
 *
 */

@EnableAspectJAutoProxy(exposeProxy = true)
@EnableRedisHttpSession
@EnableRabbit
@EnableDiscoveryClient
@SpringBootApplication
public class KkmallOrderApplication {

    public static void main(String[] args)
    {
        SpringApplication.run(KkmallOrderApplication.class, args);
    }

}
