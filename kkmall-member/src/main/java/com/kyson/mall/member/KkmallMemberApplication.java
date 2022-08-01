package com.kyson.mall.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class KkmallMemberApplication {

    public static void main(String[] args)
    {
        SpringApplication.run(KkmallMemberApplication.class, args);
    }

}
