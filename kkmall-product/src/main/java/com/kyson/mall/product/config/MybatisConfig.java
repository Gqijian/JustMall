package com.kyson.mall.product.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Administrator<br />
 * @description: <br/>
 * @date: 2022/9/6 15:28<br/>
 */
@Configuration
@EnableTransactionManagement    //开启事务
@MapperScan("com.kyson.mall.product.dao")
public class MybatisConfig {

    /**
     * 新的分页插件,一缓和二缓遵循mybatis的规则,需要设置 MybatisConfiguration#useDeprecatedExecutor = false 避免缓存出现问题(该属性会在旧插件移除后一同移除)
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {

        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);

        //设置请求的页面大于最大页后的操作，true 返回首页，false继续请求 默认false
        paginationInnerInterceptor.setOverflow(true);

        //设置最大页限制数量，默认500，-1不受限制
        paginationInnerInterceptor.setMaxLimit(new Long(500));
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }

}
