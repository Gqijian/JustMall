package com.kyson.mall.search.controller;

import com.kyson.common.exception.BizCodeEnum;
import com.kyson.common.to.es.SkuEsModel;
import com.kyson.common.utils.R;
import com.kyson.mall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequestMapping("/search/save")
@RestController
public class ElasticSaveController {

    @Autowired
    private ProductSaveService productSaveService;

    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels){
        Boolean b = false;
        try {
            b = productSaveService.productStatusUp(skuEsModels);
        }catch (Exception e){
            log.error("ElasticSaveController 商品上架错误：{}", e);
            R.error(BizCodeEnum.PRODUCT_UP_EXCETION.getCode(), BizCodeEnum.PRODUCT_UP_EXCETION.getMsg());
        }
        if(!b){
            return R.ok();
        }else {
            return R.error(BizCodeEnum.PRODUCT_UP_EXCETION.getCode(), BizCodeEnum.PRODUCT_UP_EXCETION.getMsg());
        }

    }
}
