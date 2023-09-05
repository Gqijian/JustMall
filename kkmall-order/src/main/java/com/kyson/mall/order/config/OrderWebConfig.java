package com.kyson.mall.order.config;

import com.kyson.mall.order.interceptor.LoginUserIntercepor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class OrderWebConfig implements WebMvcConfigurer {

    @Autowired
    LoginUserIntercepor loginUserIntercepor;

    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {

        registry.addInterceptor(loginUserIntercepor).addPathPatterns("/**");
    }
}
