package com.kyson.mall.product;

//import com.aliyun.oss.OSS;
//import com.aliyun.oss.OSSClient;
//import com.aliyun.oss.OSSClientBuilder;
//import com.aliyun.oss.OSSException;
import com.kyson.mall.product.entity.BrandEntity;
import com.kyson.mall.product.service.BrandService;
import com.netflix.client.ClientException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

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
