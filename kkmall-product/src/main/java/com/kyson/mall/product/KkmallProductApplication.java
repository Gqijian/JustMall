package com.kyson.mall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

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
 * 整合 redisson 作为分布式锁
 *
 * 整合 springcache 简化缓存开发 spring-boot-starter-cache spring-boot-starter-data-redis
 * 配置
 *  1、自动配置了哪些
 *      CacheAutoConfiguration 会导入 RedisCacheAutoConfiguration
 *      自动配置好了 缓存管理器 RedisCacheManager
 *  2、写配置
 *  spring.cache.type = redis
 *
 *  3.写注解
 *      开启缓存 @EnableCaching
 *      只需要注解就能完成缓存操作
 *  1 缓存中有，方法不调用
 *  2 key默认自动生成，缓存名字 catagory::SimpleKey[]
 *  3 缓存的 value 的值 默认使用 jdk 序列化机制，将序列化后的数据存到 redis
 *  4 默认时间 ttl -1 用不过期
 *
 *  自定义
 *      自定义生成的 key key 属性指定，接受一个 spel表达式 如果要自己写 那要带上 ''
 *      自定义存活的时间 spring配置文件中修改 ttl spring.cache.redis.time-to-live = 360000
 *      数据保存为json
 *          CacheAutoConfiguration -> RedisCacheAutoConfiguration ->
 *          自动配置了 redisCacheManager 初始化了所有缓存 -> 每个缓存决定使用什么配置
 *          -> RedisCacheAutoConfiguration  如果有就用自己的 没有就用默认的
 *          -> 想改缓存配置  只需要给容器中放入一个 RedisCacheAutoConfiguration
 *          -> 就会应用到当前 RedisCacheManager 管理的所有缓存分区中
 *
 */


@EnableFeignClients(basePackages = "com.kyson.mall.product.feign")
@EnableDiscoveryClient
@MapperScan("com.kyson.mall.product.dao")
@SpringBootApplication
public class KkmallProductApplication {

    public static void main(String[] args)
    {
        SpringApplication.run(KkmallProductApplication.class, args);
    }

}
