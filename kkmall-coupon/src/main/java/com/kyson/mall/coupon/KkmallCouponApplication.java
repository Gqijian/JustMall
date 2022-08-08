package com.kyson.mall.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


/**
 *  1、引入 spring cloud nacos config 依赖
 *  2、创建 bootstrap.properties
 *         nacos 配置中心中 添加 应用名.properties
 *  3、动态获取配置: @RefreshScope  @Value 获取某个配置的值
 *
 *  命名空间：配置隔离
 *      默认public
 */
@EnableDiscoveryClient
@SpringBootApplication
public class KkmallCouponApplication {

    public static void main(String[] args)
    {
        SpringApplication.run(KkmallCouponApplication.class, args);
    }

}