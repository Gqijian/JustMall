package com.kyson.mall.seckill.service.impl;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.kyson.common.to.mq.SecKillOrderTo;
import com.kyson.common.utils.R;
import com.kyson.common.vo.MemberRespVo;
import com.kyson.mall.seckill.feign.CouponFeignService;
import com.kyson.mall.seckill.feign.ProductFeignService;
import com.kyson.mall.seckill.interceptor.LoginUserIntercepor;
import com.kyson.mall.seckill.service.SeckillService;
import com.kyson.mall.seckill.to.SecKillSkuRedisTo;
import com.kyson.mall.seckill.vo.SeckillSessionWithSkus;
import com.kyson.mall.seckill.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";

    private final String SKUKILL_CACHE_PREFIX = "seckill:skus:";

    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";    //+商品随机码

    @Override
    public void uploadSeckillSkuLatest3Days()
    {
        //扫描最近三天需要参与秒杀的活动
        R session = couponFeignService.getLatest3DaysSession();

        if (session.getCode() == 0) {
            //上架商品
            List<SeckillSessionWithSkus> sessionData = session.getData(new TypeReference<List<SeckillSessionWithSkus>>() {
            });

            //缓存到 redis
            //1、缓存活动信息
            saveSessionInfos(sessionData);

            //2、缓存商品关联的活动信息
            saveSessionSkuInfos(sessionData);
        }
    }

    public List<SecKillSkuRedisTo> blockHandler(BlockException e){
        log.error("getCurrentSeckillSkus  被限流 ：" + e.getMessage());
        return new ArrayList<>();
    }

    /**
     * 返回当前时间可以参与的秒杀商品信息
     *
     * @return
     */
    @SentinelResource(value = "getCurrentSeckillSkusResource", blockHandler = "blockHandler")
    @Override
    public List<SecKillSkuRedisTo> getCurrentSeckillSkus()
    {
        //确定当前时间属于哪个秒杀场次
        //与 1970 年的差值
        Long time = new Date().getTime();

        try (Entry entry = SphU.entry("getCurrentSeckillSkus")) {
            Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
            for (String key : keys) {
                //seckill:sessions:xxxxxxxxxxxx
                String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
                String[] s = replace.split("_");
                Long start = Long.parseLong(s[0]);
                Long end = Long.parseLong(s[1]);

                if (time >= start && time <= end) {
                    //获取这个场次需要秒杀的所有商品信息
                    List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                    BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                    List<String> list = hashOps.multiGet(range);
                    if (list != null && list.size() > 0) {
                        List<SecKillSkuRedisTo> collect = list.stream().map(item -> {

                            SecKillSkuRedisTo redisTo = JSON.parseObject(item, SecKillSkuRedisTo.class);
                            //redisTo.setRandomCode(null); 秒杀开始了就需要随机码
                            return redisTo;
                        }).collect(Collectors.toList());
                        return collect;
                    }
                    break;
                }
            }
        } catch (BlockException e) {
            log.error("资源被限流：{} " + e.getMessage());
        }

        return null;
    }

    @Override
    public SecKillSkuRedisTo getSkuSeckillInfo(Long skuId)
    {

        //找到所有需要参与秒杀商品的key信息
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();

        if (keys != null && keys.size() > 0) {
            String regx = "\\d_" + skuId;

            for (String key : keys) {
                if (Pattern.matches(regx, key)) {
                    String json = hashOps.get(key);
                    SecKillSkuRedisTo redisTo = JSON.parseObject(json, SecKillSkuRedisTo.class);

                    //随机码 当前时间是否是秒杀时间 否则不返回
                    Long current = new Date().getTime();
                    if (current >= redisTo.getStartTime() && current <= redisTo.getEndTime()) {

                    } else {
                        redisTo.setRandomCode(null);
                    }
                    return redisTo;
                }
            }
        }
        return null;
    }

    //TODO 上架商品的时候 每一个数据都有过期时间
    //TODO 秒杀后续流程 简化了收货地址等
    @Override
    public String kill(String killId, String key, Integer num)
    {

        MemberRespVo respVo = LoginUserIntercepor.loginUser.get();

        //获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);

        // killId 1_7 之类的场次 + sku id
        String json = hashOps.get(killId);
        if (StringUtils.isEmpty(json)) {
            return null;
        } else {

            SecKillSkuRedisTo redisTo = JSON.parseObject(json, SecKillSkuRedisTo.class);
            //检验合法性
            Long startTime = redisTo.getStartTime();
            Long endTime = redisTo.getEndTime();
            Long current = new Date().getTime();

            //1、校验时间合法性
            if (current >= startTime && current <= endTime) {
                //2、校验随机码 和 商品id 是否符合
                String randomCode = redisTo.getRandomCode();
                String killStr = redisTo.getPromotionSessionId() + "_" + redisTo.getSkuId();
                if (randomCode.equals(key) && killId.equals(killStr)) {
                    //3、验证购物数量是否合理
                    if (num <= redisTo.getSeckillLimit()) {
                        //4、验证这个人是否已经购买过了 幂等性处理。只要秒杀成功就去占位 userId + sessionId + skuId

                        //SETNX 就是不存在的时候才占位
                        String redisKey = respVo.getId() + "_" + killStr;
                        //并且要设置自动过期时间
                        Long ttl = endTime - startTime;
                        Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);

                        if (ifAbsent) {
                            //占位成功说明从来没有买过
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE);
                            try {

                                //拿到信号量就成功 拿不到就失败
                                boolean b = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
                                //秒杀成功 快速下单 发送 MQ消息
                                String timeId = IdWorker.getTimeId();

                                SecKillOrderTo orderTo = new SecKillOrderTo();
                                orderTo.setOrderSn(timeId);
                                orderTo.setMemberId(respVo.getId());
                                orderTo.setNum(num);
                                orderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
                                orderTo.setSkuId(redisTo.getSkuId());
                                orderTo.setSeckillPrice(redisTo.getSeckillPrice());
                                rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);
                                return timeId;
                            } catch (InterruptedException e) {
                                return null;
                            }
                        } else {
                            //说明已经买过了
                            return null;
                        }

                    }

                } else {
                    return null;
                }

            } else {
                return null;
            }

        }

        return null;
    }

    private void saveSessionInfos(List<SeckillSessionWithSkus> sessions)
    {

        if (sessions != null) {
            sessions.stream().forEach(session -> {
                Long startTime = session.getStartTime().getTime();
                Long endTime = session.getEndTime().getTime();
                String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;

                //缓存活动信息 幂等性处理
                Boolean hasKey = redisTemplate.hasKey(key);

                if (!hasKey) {
                    List<String> collect = session.getRelationSkus().stream().map(item -> item.getPromotionSessionId() + "_" + item.getSkuId().toString()).collect(Collectors.toList());
                    redisTemplate.opsForList().leftPushAll(key, collect);
                }
            });
        }

    }

    private void saveSessionSkuInfos(List<SeckillSessionWithSkus> sessions)
    {

        sessions.stream().forEach(session -> {

            //准备 hash 操作
            BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);

            session.getRelationSkus().stream().forEach(seckillSkuVo -> {

                //设置商品的随机码 只有秒杀开始那一刻才暴露出来 防止攻击
                String token = UUID.randomUUID().toString().replace("-", "");

                //幂等性处理 防止不同场次同一商品重复
                if (!operations.hasKey(seckillSkuVo.getPromotionSessionId() + "_" + seckillSkuVo.getSkuId().toString())) {

                    //缓存商品
                    SecKillSkuRedisTo redisTo = new SecKillSkuRedisTo();
                    //sku 基本数据
                    R skuInfo = productFeignService.getSkuInfo(seckillSkuVo.getSkuId());
                    if (skuInfo.getCode() == 0) {
                        SkuInfoVo info = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        redisTo.setSkuInfo(info);
                    }

                    //sku秒杀信息
                    BeanUtils.copyProperties(seckillSkuVo, redisTo);

                    //设置当前商品的秒杀时间
                    redisTo.setStartTime(session.getStartTime().getTime());
                    redisTo.setEndTime(session.getEndTime().getTime());
                    redisTo.setRandomCode(token);

                    operations.put(seckillSkuVo.getPromotionSessionId() + "_" + seckillSkuVo.getSkuId().toString(), JSON.toJSONString(redisTo));

                    //如果当前场次的商品库存信息 已经上架 就不需要上架，如果不同场次同一商品要分开上架
                    //引入分布式信号量 限流;
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);

                    //商品秒杀件数的信号量
                    semaphore.trySetPermits(seckillSkuVo.getSeckillCount());
                }

            });
        });
    }
}
