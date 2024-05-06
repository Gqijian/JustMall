package interceptor;

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

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        /**
         * /member/xxxxx
         * url 会加上服务器的整个地址名 uri 只有后面的
         *
         * 服务器之间的调用来放行
         */
        String uri = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/member/**", uri);
        if(match){
            return true;
        }

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
