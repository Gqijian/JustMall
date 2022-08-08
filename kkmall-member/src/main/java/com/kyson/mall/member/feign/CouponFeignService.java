package com.kyson.mall.member.feign;

import com.kyson.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Administrator<br />
 * @description: <br/>
 * @date: 2022/8/2 14:57<br/>
 */

@FeignClient("kkmall-coupon")
public interface CouponFeignService {

    @RequestMapping("/coupon/coupon/member/list")
    public R memberCoupons();
}
