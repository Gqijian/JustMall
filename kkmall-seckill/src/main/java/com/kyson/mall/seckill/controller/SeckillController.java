package com.kyson.mall.seckill.controller;

import com.kyson.common.utils.R;
import com.kyson.mall.seckill.service.SeckillService;
import com.kyson.mall.seckill.to.SecKillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    /**
     * 返回当前时间可以参与的秒杀商品信息
     * @return
     */
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus(){

        List<SecKillSkuRedisTo> vos = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(vos);
    }

    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId){

        SecKillSkuRedisTo to = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(to);
    }

    @GetMapping("/kill")
    public String secKill(@RequestParam("killId") String killId, @RequestParam("key") String key,
                     @RequestParam("num") Integer num, Model model){

        //判断是否登录

        //秒杀成功返回订单号
        String orderSn = seckillService.kill(killId, key, num);
        model.addAttribute("orderSn", orderSn);
        return "success";
    }
}
