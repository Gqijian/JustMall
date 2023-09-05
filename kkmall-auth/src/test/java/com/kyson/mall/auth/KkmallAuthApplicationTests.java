package com.kyson.mall.auth;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class KkmallAuthApplicationTests {

    @Test
    void contextLoads()
    {

        DigestUtils.md5Hex("123456");

        //MD5 不能直接进行密码的加密存储

        //盐值加密：随机值
        Md5Crypt.md5Crypt("".getBytes(), "$1$");

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String encodePassword = encoder.encode("");

        boolean matches = encoder.matches("原密码", "加密密码");
    }

}
