package com.kyson.mall.member.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class FeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor()
    {

        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate)
            {
                //1、RequestContextHolder 刚进来的请求数据 本质上还是通过 ThreadLocal 获得的
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                if (attributes != null) {
                    //得到老请求
                    HttpServletRequest request = attributes.getRequest();

                    //同步请求头数据，尤其是 cookie，请求头中有cookie 远程才会知道取出哪个用户

                    if (request != null) {
                        //给新请求同步老请求cookie
                        //"feign 远程前，先进行 RequestInterceptor.apply() "
                        String cookie = request.getHeader("Cookie");
                        requestTemplate.header("Cookie", cookie);
                    }
                }

            }
        };
    }
}
