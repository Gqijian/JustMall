package com.kyson.mall.gateway;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class KkmallGatewayApplicationTests {

    @Test
    void contextLoads()
    {
    }

    @Test
    public void testRedisson(){

        Integer[] aa = {1,2};
        String s = JSON.toJSONString(aa);
        System.out.println("aaaaaaaaaaa" + s);
        System.out.println();
    }

}
