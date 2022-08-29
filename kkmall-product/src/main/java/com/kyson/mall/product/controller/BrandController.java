package com.kyson.mall.product.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kyson.mall.product.entity.BrandEntity;
import com.kyson.mall.product.service.BrandService;
import com.kyson.common.utils.PageUtils;
import com.kyson.common.utils.R;

import javax.validation.Valid;


/**
 * 品牌
 *
 * @author kyson
 * @email kysonxxxx@gmail.com
 * @date 2022-07-29 17:07:28
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    //@RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:brand:save")
    public R save(@Valid @RequestBody BrandEntity brand /*, BindingResult result*/){
        /*
        if(result.hasErrors()){
            //1、获取校验错误结果
            Map<String, String> map = new HashMap<>();
            result.getFieldErrors().forEach((err)->{
                //FieldError 获取到错误提示
                String defaultMessage = err.getDefaultMessage();

                //获取到错误属性名字
                String field = err.getField();
                map.put(field, defaultMessage);
            });

            return R.error(400,"error").put("data", map);
        }else {


        }

        */
        brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:brand:update")
    public R update(@RequestBody BrandEntity brand){
		brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
