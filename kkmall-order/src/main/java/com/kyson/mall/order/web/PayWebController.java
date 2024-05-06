package com.kyson.mall.order.web;

import com.alipay.api.AlipayApiException;
import com.kyson.mall.order.config.AlipayTemplate;
import com.kyson.mall.order.service.OrderService;
import com.kyson.mall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PayWebController {

    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private OrderService orderService;

    /**
     * 支付成功后 跳转到用户订单列表页
     * @param orderSn
     * @return
     * @throws AlipayApiException
     */
    @ResponseBody
    @GetMapping(value = "/payOrder", produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException
    {

        PayVo payVo = orderService.getOrderPay(orderSn);

//        PayVo payVo = new PayVo();
//        payVo.setBody();    //订单备注
//        payVo.setOut_trade_no();    //订单号
//        payVo.setSubject(); //订单主题
//        payVo.setTotal_amount();    //订单金额

        //返回的是一个 html 页面
        String pay = alipayTemplate.pay(payVo);
        return pay;
    }
}
