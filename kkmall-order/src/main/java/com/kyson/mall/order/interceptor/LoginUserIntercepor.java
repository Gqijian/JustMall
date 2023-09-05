package com.kyson.mall.order.interceptor;

import com.kyson.common.constant.AuthConstant;
import com.kyson.common.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 拦截器 需要一个web 配置来起作用
 */
@Component
public class LoginUserIntercepor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {

        MemberRespVo attribute = (MemberRespVo) request.getSession().getAttribute(AuthConstant.LOGIN_USER);

        if(attribute != null){
            loginUser.set(attribute);
            return true;
        }else {

            //没登录 就去登录
            request.getSession().setAttribute("msg", " 请先进行登录 ");
            response.sendRedirect("http://auth.kysonmall.com/login.html");
            return false;
        }

    }
}
