package com.kyson.mall.thirdparty.controller;

import com.kyson.common.utils.R;
import com.kyson.mall.thirdparty.component.SMScomponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms")
public class SmsSendController {

    @Autowired
    private SMScomponent smScomponent;

    /**
     *
     * 提供给别的服务器进行调用
     *
     * @param phone
     * @param code
     * @return
     */
    @GetMapping("/sendcode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code")String code){
        smScomponent.sendSmsCode(phone, code);
        return R.ok();
    }

}
