package com.kyson.mall.product.feign;

import com.kyson.common.to.SkuReductionTo;
import com.kyson.common.to.SpuBoundTo;
import com.kyson.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Kyson
 * @description:
 * @date: 2022/11/16 17:28
 */
@FeignClient("kkmall-coupon")
public interface CouponFeignService {

    /**
     * 1、CouponFeignService.saveSpuBounds(SpuBoundTo)
     * 首先 通过 @RequestBody 将SpuBoundTo转为json
     * 接着 在注册中心找到 kkmall-coupon 然后给 /coupon/spubounds/save 发送请求
     * 将上一步的json 放在请求体里发送请求
     * <p>
     * 然后对方服务请求体里的json数据 将请求体的 json 转为 SpuBoundsEntity
     * <p>
     * 只要 json 数据模型是兼容的 双方服务无需使用同一个 TO
     *
     * @param spuBoundTo
     * @return
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("/coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
