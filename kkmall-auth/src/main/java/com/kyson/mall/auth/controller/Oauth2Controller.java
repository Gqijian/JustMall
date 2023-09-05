package com.kyson.mall.auth.controller;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.kyson.common.constant.AuthConstant;
import com.kyson.common.utils.HttpUtils;
import com.kyson.common.utils.R;
import com.kyson.mall.auth.feign.MemberFeignService;
import com.kyson.common.vo.MemberRespVo;
import com.kyson.mall.auth.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
public class Oauth2Controller {

    @Autowired
    private MemberFeignService memberFeignService;

    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session, HttpServletResponse servletResponse) throws Exception
    {

        //根据 code 换取 accessToken
        Map<String, String> param = new HashMap<>();
        param.put("client_id", "");
        param.put("client_secret", "");
        param.put("grant_type", "authorization_code");
        param.put("redirect_uri", "http://kysonmall.com/oauth2.0/weibo/success");
        param.put("code", code);

        HttpResponse response = HttpUtils.doPost("api.weibo.com", "/oauth2/access_token", "post", null, null, param);

        //登录成功跳回首页
        if (response.getStatusLine().getStatusCode() == 200) {
            //获取到了 accessToken
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);

            //如果是第一次注册 则自动生成一个会员信息账号
            R oauthLogin = memberFeignService.oauthLogin(socialUser);
            if (oauthLogin.getCode() == 0) {
                MemberRespVo data = oauthLogin.getData("data", new TypeReference<MemberRespVo>() {
                });

                log.info("登陆成功,用户信息： {}", data.toString());
                //第一次用 session 命令浏览器保存卡号 就是 jessionid 这个 cookie
                //以后浏览器访问哪个网站 就会带上这个网站的 cookie
                //子域之间 kysonmall.com auth.kysonmall.com 之间的
                //发卡的时候（指定域名为父域） 即使是子域系统发卡 也能 父域也能使用
                session.setAttribute(AuthConstant.LOGIN_USER, data);
                //new Cookie("JSESSIONID", "DATA").setDomain("");
                //servletResponse.addCookie();
                return "redirect:http://kysonmall.com";
            } else {
                return "redirect:http://auth.kysonmall.com/login.html";
            }

        } else {
            return "redirect:http://auth.kysonmall.com/login.html";
        }
    }
}
