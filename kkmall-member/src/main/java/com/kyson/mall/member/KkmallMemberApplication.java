package com.kyson.mall.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 远程调用别的服务
 * 1、引入open feign
 * 2、编写接口告诉springcloud 这个接口需要调用远程服务
 * 3、声明接口的每一个方法都是调用哪个远程服务器的哪个请求
 * 4、开启远程调用功能 @EnableFeignClients
 */
@EnableFeignClients(basePackages = "com.kyson.mall.member.feign")
@EnableDiscoveryClient
@SpringBootApplication
public class KkmallMemberApplication {

    public static void main(String[] args)
    {
        SpringApplication.run(KkmallMemberApplication.class, args);
    }

}
