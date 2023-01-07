package com.kyson.mall.product;

//import com.aliyun.oss.OSS;
//import com.aliyun.oss.OSSClient;
//import com.aliyun.oss.OSSClientBuilder;
//import com.aliyun.oss.OSSException;
import com.alibaba.fastjson2.JSON;
import com.alibaba.nacos.shaded.io.grpc.internal.JsonUtil;
import com.kyson.mall.product.entity.BrandEntity;
import com.kyson.mall.product.entity.GatewayInfoVo;
import com.kyson.mall.product.entity.GatewayInfoVos;
import com.kyson.mall.product.service.BrandService;
import com.netflix.client.ClientException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        //brandService.save(brandEntity);

        Map<String, Object> params = new HashMap<>();
        params.put("limit",1);
        params.put("page",1);
        String s = JSON.toJSONString(params);
        System.out.println(s);
    }

    public static void main(String[] args)
    {
        Map<String, Object> params = new HashMap<>();
        params.put("limit",1);
        params.put("page",1);
        String s = JSON.toJSONString(params);
        System.out.println(s);

        GatewayInfoVos gatewayInfoVos = new GatewayInfoVos();

        GatewayInfoVo gatewayInfoVo = new GatewayInfoVo();
        gatewayInfoVo.setGatewayId("1");
        gatewayInfoVo.setGatewayPwd("aa");
        gatewayInfoVo.setState(1);
        GatewayInfoVo gatewayInfoVo1 = new GatewayInfoVo();
        gatewayInfoVo1.setGatewayId("2");
        gatewayInfoVo1.setGatewayPwd("22");
        gatewayInfoVo1.setState(1);

        List<GatewayInfoVo> list =new ArrayList<>();
        list.add(gatewayInfoVo);
        list.add(gatewayInfoVo1);

        gatewayInfoVos.setGatewayInfoVoList(list);

        System.out.println(JSON.toJSONString(gatewayInfoVos));
    }
}
