package com.kyson.mall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.kyson.mall.order.config.AlipayTemplate;
import com.kyson.mall.order.service.OrderService;
import com.kyson.mall.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝异步通知 监听
 */

@RestController
public class OrderPayedListener {

    @Autowired
    private OrderService orderService;

    @Autowired
    AlipayTemplate alipayTemplate;

    @PostMapping("/payed/notify")
    public String handleAlipayed(PayAsyncVo vo, HttpServletRequest request) throws AlipayApiException
    {

        /**
         * 只要收到了支付宝给我们异步的通知，告诉我们订单支付成功，应该返回success
         * 支付宝就再也不通知

        Map<String, String[]> map = request.getParameterMap();
        System.out.println("支付宝通知 ： " + map);

        for (String key: map.keySet()) {
            String value = request.getParameter(key);
            System.out.println("参数名：" + key + " 参数值： " + value);
        }
        */
        //TODO 验签
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            // valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipay_public_key(),
                alipayTemplate.getCharset(), alipayTemplate.getSign_type()); //调用SDK验证签名

        //签名验证成功
        if (signVerified){
            System.out.println("支付宝异步通知验签成功");
            //修改订单状态
            String result = orderService.handlePayResult(vo);
            return result;
        }else {
            System.out.println("支付宝异步通知验签失败");
            return "error";
        }

    }
}
