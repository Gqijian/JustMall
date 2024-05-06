package com.kyson.mall.product.feign;

import com.kyson.common.utils.R;
import com.kyson.mall.product.feign.fallback.SecKillFeignServiceFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "kkmall-seckill", fallback = SecKillFeignServiceFallBack.class)
public interface SeckillFeignService {

    @GetMapping("/sku/seckill/{skuId}")
    R getSkuSeckillInfo(@PathVariable("skuId") Long skuId);
}
