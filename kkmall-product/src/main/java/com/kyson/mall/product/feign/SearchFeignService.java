package com.kyson.mall.product.feign;

import com.kyson.common.to.es.SkuEsModel;
import com.kyson.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("kkmall-product")
public interface SearchFeignService {
    @PostMapping("/search/save/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
