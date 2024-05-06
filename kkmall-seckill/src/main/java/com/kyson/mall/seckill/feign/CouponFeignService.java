package com.kyson.mall.seckill.feign;

import com.kyson.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("kkmall-coupon")
public interface CouponFeignService {

    @GetMapping("/coupon/seckillsession/latest3DaysSession")
    R getLatest3DaysSession();
}
