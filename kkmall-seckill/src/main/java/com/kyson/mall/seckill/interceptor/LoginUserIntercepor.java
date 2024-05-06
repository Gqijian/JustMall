package com.kyson.mall.seckill.interceptor;

import com.kyson.common.constant.AuthConstant;
import com.kyson.common.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 拦截器 需要一个web 配置来起作用
 */
@Component
public class LoginUserIntercepor implements HandlerInterceptor {

    public static ThreadLocal<MemberRespVo> loginUser = new ThreadLocal<>();

    /*

    server {
    listen       80;
    server_name  kysonmall.com *.kysonmall.com; #加上内网穿透地址 否则访问静态资源

    #charset koi8-r;
    #access_log  /var/log/nginx/log/host.access.log  main;
    location /static {
        root   /usr/share/nginx/html;
    }

    location /payed/ {
        proxy_set_header Host order.kysonmall.com;
        proxy_pass http://kysonmall;
    }

    location / {
        proxy_set_header Host $host;
	proxy_pass http://kysonmall;
    }
     */

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        /**
         * /order/order/status/xxxxx
         * url 会加上服务器的整个地址名 uri 只有后面的
         *
         * 服务器之间的调用来放行
         */
        String uri = request.getRequestURI();
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean match = antPathMatcher.match("/kill", uri);

        if (match) {
            MemberRespVo attribute = (MemberRespVo) request.getSession().getAttribute(AuthConstant.LOGIN_USER);

            if (attribute != null) {
                loginUser.set(attribute);
                return true;
            } else {

                //没登录 就去登录
                request.getSession().setAttribute("msg", " 请先进行登录 ");
                response.sendRedirect("http://auth.kysonmall.com/login.html");
                return false;
            }
        }
        return true;
    }
}
