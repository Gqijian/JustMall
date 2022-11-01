package com.kyson.mall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 *
 * @NotNull：用在基本类型的包装类型上面的属性注解，不能为null，但可以为empty
 *
 * @NotEmpty：用在集合类上面的属性的注解，不能为null，而且长度必须大于0
 *
 * @NotBlank：用在String上面属性的注解，不能为null，而且调用trim()后，长度必须大于0
 *
 *
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
 * 5、分组校验
 *      @NotBlank(message = "必须有名字", groups = {AddGroup.class,UpdateGroup.class})
 *      @Validated({AddGroup.class})
 *      默认没有指定分组的校验注解 （@Notxxx） 在分组校验情况下不生效 （@Validated({AddGroup.class})）
 *
 * 6、自定义校验
 *      1、编写一个自定义校验注解
 *      2、编写一个自定义校验器
 *      3、关联自定义 校验器和注解 @Constraint(validatedBy = {ListValueConstraintValidator.class [可以指定多个不同的校验器， 比如 int double 等] })
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
