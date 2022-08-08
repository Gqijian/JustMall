package com.kyson.mall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 1、开启服务注册发现(配置Nacos的注册中心地址)
 * 2、排除数据库相关配置 exclude = {DataSourceAutoConfiguration.class}
 */
@EnableDiscoveryClient
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class KkmallGatewayApplication {

    public static void main(String[] args)
    {
        SpringApplication.run(KkmallGatewayApplication.class, args);
    }

}
