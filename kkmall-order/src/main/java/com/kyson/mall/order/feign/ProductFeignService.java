package com.kyson.mall.order.feign;

import com.kyson.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("kkmall-product")
public interface ProductFeignService {

    @RequestMapping("/product/spuinfo/skuId/{id}")
    R getSpuInfoBySkuId(@PathVariable("id")Long skuId);
}
