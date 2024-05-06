package com.kyson.mall.member.config;

import interceptor.LoginUserIntercepor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MemberWebConfig implements WebMvcConfigurer {

    @Autowired
    LoginUserIntercepor loginUserIntercepor;

    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {

        registry.addInterceptor(loginUserIntercepor).addPathPatterns("/**");
    }
}
