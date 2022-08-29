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
 * 4、JSR303校验
 *      添加注解: import javax.validation.constraints
 *      @Valid 标记需要检验的对象
 *          效果：
 *           就是400错误 且有提示
 *
 *      BandingResult 校验结果
 *      save(@Valid @RequestBody BrandEntity brand, BindingResult bindingResult)
 *
 *      分组校验
 *
 * 5、统一的异常处理 @RestControllerAdvice
 *
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
