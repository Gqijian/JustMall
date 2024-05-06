package com.kyson.mall.coupon;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@SpringBootTest
class KkmallCouponApplicationTests {

    @Test
    void contextLoads()
    {
        LocalDate now = LocalDate.now();
        LocalDate plusDays = now.plusDays(3);

        System.out.println(now);
        System.out.println(plusDays);

        LocalTime min = LocalTime.MIN;
        LocalTime max = LocalTime.MAX;

        System.out.println(min);
        System.out.println(max);

        LocalDateTime start = LocalDateTime.of(now, min);
        LocalDateTime end = LocalDateTime.of(plusDays, max);

        System.out.println(start);
        System.out.println(end);
    }

}
