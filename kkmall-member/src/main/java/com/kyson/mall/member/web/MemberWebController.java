package com.kyson.mall.member.web;

import com.kyson.common.utils.R;
import com.kyson.mall.member.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;

@Controller
public class MemberWebController {

    @Autowired
    private OrderFeignService orderFeignService;

    @GetMapping("/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum, Model model){

        HashMap<String, Object> page = new HashMap<>();
        page.put("page", pageNum.toString());

        R r = orderFeignService.listWithItem(page);
        model.addAttribute("orders", r);
        return "orderList";
    }
}
