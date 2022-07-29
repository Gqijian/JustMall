package com.kyson.mall.product;

import com.kyson.mall.product.entity.BrandEntity;
import com.kyson.mall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class KkmallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Test
    void contextLoads()
    {
        BrandEntity brandEntity = new BrandEntity();

        brandEntity.setDescript("test");
        brandEntity.setName("test");
        brandService.save(brandEntity);
    }

}
