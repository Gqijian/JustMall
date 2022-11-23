package com.kyson.mall.ware.feign;

import com.kyson.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Kyson
 * @description:
 * @date: 2022/11/22 17:24
 */
@FeignClient("kkmall-product")
public interface ProducktFeignService {

    /**
     *
     * /product/skuinfo/info/{skuId}
     *
     * /api/product/skuinfo/info/{skuId}
     *
     * 1、让所有请求过网关  @FeignClient("kkmall-gateway")
     *    配置这个路径 /api/product/skuinfo/info/{skuId}
     *
     * 2、直接让后台指定 服务处理  @FeignClient("kkmall-product")
     *    /product/skuinfo/info/{skuId}
     *
     * @param skuId
     * @return
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);
}
