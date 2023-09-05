package com.kyson.mall.ssoclient.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {

    @ResponseBody
    @GetMapping("/hello")
    public String hello(){

        return "hello";
    }

    @GetMapping("/")
    public String employees(){

        return "";
    }
}
