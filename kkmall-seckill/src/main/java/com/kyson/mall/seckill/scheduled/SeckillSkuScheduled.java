package com.kyson.mall.seckill.scheduled;

import com.kyson.mall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 秒杀商品的定时上架
 * 每晚三点，上架最近三天需要秒杀的商品
 */
@Slf4j
@Component
public class SeckillSkuScheduled {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private RedissonClient redissonClient;

    private final String UPDLOAD_LOCK = "seckill:upload:lock";

    //幂等性处理
    @Scheduled(cron = "0 0 3 * * ?")
    public void uploadSeckillSkuLatest3Days()
    {
        //重复上架无需处理

        RLock lock = redissonClient.getLock(UPDLOAD_LOCK);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.uploadSeckillSkuLatest3Days();
        } finally {
            lock.unlock();
        }

    }

}
