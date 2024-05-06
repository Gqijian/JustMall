package com.kyson.mall.seckill.to;

import com.kyson.mall.seckill.vo.SkuInfoVo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SecKillSkuRedisTo {

    private Long id;

    /**
     * 活动id
     */
    private Long promotionId;

    /**
     * 活动场次id
     */
    private Long promotionSessionId;

    /**
     * 商品id
     */
    private Long skuId;

    private String randomCode;

    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;

    /**
     * 秒杀总量
     */
    private Integer seckillCount;

    /**
     * 每人限购数量
     */
    private Integer seckillLimit;

    /**
     * 排序
     */
    private Integer seckillSort;

    //当前商品开始时间 和 结束时间
    private Long startTime;

    private Long endTime;

    //sku 的详细信息
    private SkuInfoVo skuInfo;
}
