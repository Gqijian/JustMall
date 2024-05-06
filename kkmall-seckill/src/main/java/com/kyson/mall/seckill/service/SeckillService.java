package com.kyson.mall.seckill.service;

import com.kyson.mall.seckill.to.SecKillSkuRedisTo;

import java.util.List;

public interface SeckillService {

    void uploadSeckillSkuLatest3Days();

    /**
     * 返回当前时间可以参与的秒杀商品信息
     * @return
     */
    List<SecKillSkuRedisTo> getCurrentSeckillSkus();

    SecKillSkuRedisTo getSkuSeckillInfo(Long skuId);

    String kill(String killId, String key, Integer num);
}
