package com.kyson.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SecKillOrderTo {

    private String orderSn;

    /**
     * 活动场次id
     */
    private Long promotionSessionId;

    /**
     * 商品id
     */
    private Long skuId;

    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;

    /**
     * 购买数量
     */
    private Integer num;

    private Long memberId;
}
