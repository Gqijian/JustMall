package com.kyson.mall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 1、整合mybatis plus
 * 2、配置
 *   配置数据源 导入数据库驱动 在yml中配置数据源信息
 *   配置mplus
 *     使用@MapperScan
 *     告诉Mybatis plus sql映射文件位置
 *
 * 3、逻辑删除
 *      配置全局逻辑删除配置
 *      加上逻辑删除注解 @TableLogic
 *
 */
@EnableDiscoveryClient
@MapperScan("com.kyson.mall.product.dao")
@SpringBootApplication
public class KkmallProductApplication {

    public static void main(String[] args)
    {
        SpringApplication.run(KkmallProductApplication.class, args);
    }

}
