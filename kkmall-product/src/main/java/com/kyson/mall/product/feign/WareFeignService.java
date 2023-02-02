package com.kyson.mall.product.feign;

import com.kyson.common.utils.R;
import com.kyson.mall.product.vo.SkuHasStockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("kkmall-ware")
public interface WareFeignService {

    @PostMapping("/hasstock")
    public R<List<SkuHasStockVo>> getSkuHasStock(@RequestBody List<Long> skuIds);
}
