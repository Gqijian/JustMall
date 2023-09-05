package com.kyson.mall.cart.interceptor;

import com.kyson.common.constant.AuthConstant;
import com.kyson.common.constant.CartConstant;
import com.kyson.common.vo.MemberRespVo;
import com.kyson.mall.cart.to.UserInfoTo;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * 在执行目标方法前 ，判断用户的登录状态 并封装 传递给 Controller
 * 拦截器 需要一个web 配置来起作用
 */
public class CartInterceptor implements HandlerInterceptor {

    /**
     * 同一个线程共享数据
     */
    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    /**
     * 目标方法执行前
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {

        UserInfoTo userInfoTo = new UserInfoTo();
        HttpSession session = request.getSession();
        MemberRespVo member = (MemberRespVo) session.getAttribute(AuthConstant.LOGIN_USER);
        if (!ObjectUtils.isEmpty(member)) {
            userInfoTo.setUserId(member.getId());
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {

                if(cookie.getName().equals(CartConstant.TEMP_USER_COOKIE_NAME)){
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setTempUser(true);
                }
            }
        }

        //如果没有临时用户 一定分配一个临时用户
        if(StringUtils.isEmpty(userInfoTo.getUserKey())){
            userInfoTo.setUserKey(UUID.randomUUID().toString());
        }

        //目标方法执行前
        threadLocal.set(userInfoTo);
        return true;
    }

    /**
     *
     * 业务执行之后
     *
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception
    {

        UserInfoTo userInfoTo = threadLocal.get();

        if(!userInfoTo.getTempUser()){
            //持续延长临时用户过期时间
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            cookie.setDomain("kysonmall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }

    }
}
