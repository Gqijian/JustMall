package com.kyson.mall.auth.controller;

import com.alibaba.fastjson2.TypeReference;
import com.kyson.common.constant.AuthConstant;
import com.kyson.common.exception.BizCodeEnum;
import com.kyson.common.utils.R;
import com.kyson.common.vo.MemberRespVo;
import com.kyson.mall.auth.feign.MemberFeignService;
import com.kyson.mall.auth.feign.ThirdPartFeignService;
import com.kyson.mall.auth.vo.UserLoginVo;
import com.kyson.mall.auth.vo.UserRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ThirdPartFeignService thirdPartFeignService;

    @Autowired
    private MemberFeignService memberFeignService;

    @ResponseBody   //返回json数据
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone)
    {

        //TODO 接口防刷

        String redisCode = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + phone);
        Long l = Long.parseLong(redisCode.split("_")[1]);
        if (System.currentTimeMillis() - l < 60000) {
            //60s 内不能再发
            return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
        }

        //验证码再次校验 redis key - phone value - code

        String code = UUID.randomUUID().toString().substring(0, 5) + "_" + System.currentTimeMillis();

        //redis 缓存验证码
        redisTemplate.opsForValue().set(AuthConstant.SMS_CODE_CACHE_PREFIX + phone, code, 10, TimeUnit.MINUTES);
        thirdPartFeignService.sendCode(phone, code.split("_")[0]);
        return R.ok();
    }


    /**
     * RedirectAttributes attributes 模拟重定向携带数据。
     * addFlashAttribute 只取一次
     * <p>
     * //TODO 分布式 session 问题 重定向携带数据 利用 session。 将数据放在 session 中，只要跳到下一个页面取出数据后，session里面的数据就会删除掉
     *
     * @param vo
     * @param result
     * @param attributes
     * @return
     */
    @ResponseBody   //返回json数据
    @PostMapping("/regist")
    public String regist(@Validated UserRegistVo vo, BindingResult result, RedirectAttributes attributes)
    {

        if (result.hasErrors()) {

            /**
             * .map(fieldError -> {
             *      errors.put(fieldError.getField(), fieldError.getDefaultMessage());
             * });
             *
             */
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            //model.addAttribute("errors", errors);

            attributes.addFlashAttribute("errors", errors);
            //校验出错，转发到注册页

            //Request method POST  not support
            //用户注册 -> /regist[post] -> 转发  /reg.html (路径映射默认方式是 get 转发是直接把原请求原封不动的转发)
            //return "forward:/reg.html";

            // RedirectAttributes attributes
            return "redirect:http://auth.kysonmall.com/reg.html";
        }

        //调用远程服务进行注册
        //1、校验验证码
        String redisCode = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (!StringUtils.isEmpty(redisCode)) {

            if (vo.getCode().equals(redisCode.split("_")[0])) {
                //删除验证码 令牌机制
                redisTemplate.delete(AuthConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());

                //验证码校验通过
                R r = memberFeignService.regist(vo);

                if (r.getCode() == 0) {
                    //成功
                    return "redirect:http://auth.kysonmall.com/login.html";
                } else {
                    //失败返回注册页

                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", r.getData("msg", new TypeReference<String>() {
                    }));

                    attributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.kysonmall.com/reg.html";
                }

            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                attributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.kysonmall.com/reg.html";
            }

        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            attributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.kysonmall.com/reg.html";
        }

    }

    @GetMapping("/login.html")
    public String LoginPage(HttpSession session)
    {

        Object attribute = session.getAttribute(AuthConstant.LOGIN_USER);
        if (attribute == null) {
            //没登录
            return "login";
        } else {

            return "redirect:http://kysonmall.com";
        }

    }

    //@ResponseBody   //返回json数据
    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes attributes, HttpSession session)
    {

        //远程登录
        R login = memberFeignService.login(vo);

        if (login.getCode() == 0) {
            //成功
            MemberRespVo data = login.getData("data", new TypeReference<MemberRespVo>() {
            });
            session.setAttribute(AuthConstant.LOGIN_USER, data);
            return "redirect:http://kysonmall.com";
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", login.getData("msg", new TypeReference<String>() {
            }));

            attributes.addFlashAttribute("erros", errors);
            return "redirect:http://auth.kysonmall.com/login.html";
        }

    }

}
