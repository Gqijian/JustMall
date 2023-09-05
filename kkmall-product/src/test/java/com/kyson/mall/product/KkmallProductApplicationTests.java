package com.kyson.mall.product;

//import com.aliyun.oss.OSS;
//import com.aliyun.oss.OSSClient;
//import com.aliyun.oss.OSSClientBuilder;
//import com.aliyun.oss.OSSException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson2.JSON;
import com.kyson.mall.product.dao.SkuSaleAttrValueDao;
import com.kyson.mall.product.entity.BrandEntity;
import com.kyson.mall.product.entity.GatewayInfoVo;
import com.kyson.mall.product.entity.GatewayInfoVos;
import com.kyson.mall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class KkmallProductApplicationTests {

    @Autowired
    BrandService brandService;

//    @Autowired
//    StringRedisTemplate stringRedisTemplate;

//    @Autowired
//    RedissonClient redissonClient;


//    @Test
//    public void testStringRedisTemplate(){
//
//        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
//        opsForValue.set("hello", " world " + UUID.randomUUID().toString());
//
//    }

    @Autowired
    private SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Test
    public void genIEEE()
    {
        // 将 $istest、$chiptype 和 $isgateway 的值转换成十六进制并拼接起来
        String prefix = "B0FD0BE";
        int istest = 0;
        int chiptype = 0;
        int isgateway = 0;
        int address = 1030023;

        // 将结果与 $prefix 拼接起来，形成地址的前缀
        String hexIstest = Integer.toHexString(istest & 0xf);
        String hexChiptype = Integer.toHexString(chiptype & 0xf);
        String hexIsgateway = Integer.toHexString(isgateway & 0xf);
        String fullPrefix = prefix.toUpperCase() + hexIstest + hexChiptype + hexIsgateway;

        // 将 $address 的值加上 0x1000000，并将结果转换成十六进制
        // 取该结果的第二位到第八位（即从右往左数的第 2 位到第 8 位），并将结果拼接到地址前缀后面
        String hexAddress = Integer.toHexString((address & 0xffffff) + 0x1000000);

        System.out.println(hexAddress);
        String fullAddress = fullPrefix + hexAddress.substring(1, 7).toUpperCase();
        System.out.println(fullAddress);
        System.out.println(fullAddress);
    }

    @Test
    void contextLoads()
    {

        BrandEntity brandEntity = new BrandEntity();
        //test
        brandEntity.setDescript("test");
        brandEntity.setName("test");
        //brandService.save(brandEntity);

        Map<String, Object> params = new HashMap<>();
        params.put("limit", 1);
        params.put("page", 1);
        String s = JSON.toJSONString(params);
        System.out.println(s);
    }

    @Test
    void jsonTest()
    {

        String jsonMessage = "[{'num':'成绩', '外语':88, '历史':65, '地理':99, 'object':{'aaa':'1111','bbb':'2222','cccc':'3333'}}," +
                "{'num':'兴趣', '外语':28, '历史':45, '地理':19, 'object':{'aaa':'11a11','bbb':'2222','cccc':'3333'}}," +
                "{'num':'爱好', '外语':48, '历史':62, '地理':39, 'object':{'aaa':'11c11','bbb':'2222','cccc':'3333'}}]";
        JSONArray myJsonArray = JSONArray.parseArray(jsonMessage);
        System.out.println(myJsonArray.get(0).toString());
    }

    public static void main(String[] args)
    {

        Map<String, Object> params = new HashMap<>();
        params.put("limit", 1);
        params.put("page", 1);
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

        List<GatewayInfoVo> list = new ArrayList<>();
        list.add(gatewayInfoVo);
        list.add(gatewayInfoVo1);

        gatewayInfoVos.setGatewayInfoVoList(list);

        System.out.println(JSON.toJSONString(gatewayInfoVos));
    }
}
