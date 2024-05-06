package com.kyson.mall.product.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson2.JSON;
import com.kyson.common.exception.BizCodeEnum;
import com.kyson.common.utils.R;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 */
@Configuration
public class SentinelConfig implements BlockExceptionHandler {


    //https://github.com/alibaba/Sentinel/tree/master/sentinel-adapter/sentinel-spring-webmvc-adapter
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception
    {

        R error = R.error(BizCodeEnum.TOO_MANY_REQUEST_EXCEPTION.getCode(), BizCodeEnum.TOO_MANY_REQUEST_EXCEPTION.getMsg());
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/json");
        response.getWriter().write(JSON.toJSONString(error));
    }
}
