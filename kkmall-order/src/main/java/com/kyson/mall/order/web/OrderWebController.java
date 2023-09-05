package com.kyson.mall.order.web;

import com.kyson.mall.order.service.OrderService;
import com.kyson.mall.order.vo.OrderConfirmVo;
import com.kyson.mall.order.vo.OrderSubmitVo;
import com.kyson.mall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException
    {

        OrderConfirmVo confirmVo = orderService.confirmOrder();

        model.addAttribute("orderConfirmData", confirmVo);
        return "confirm";
    }

    /**
     *
     * 提交订单功能
     *
     * 表单提交
     *
     * @param vo
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes){

        //下单：创建订单 验令牌 验价格 锁库存
        SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);

        if (responseVo.getCode() == 0){
            //下单成功 去到支付选择页
            model.addAttribute("submitOrderResp", responseVo);
            return "pay";
        }else {
            //下单失败重新回到订单确认页重新确认订单信息
            Integer code = responseVo.getCode();
            String msg = "下单失败;";
            switch (code) {
                case 1:
                    msg += "防重令牌校验失败";
                    break;
                case 2:
                    msg += "商品价格发生变化";
                    break;
                case 3:
                    msg += "商品库存不足";
                    break;
            }
            redirectAttributes.addFlashAttribute("msg", msg);
            return "redirect:http://order.kysonmall.com/toTrade";
        }

    }
}
