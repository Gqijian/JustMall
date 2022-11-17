package com.kyson.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Kyson
 * @description:
 * @date: 2022/11/16 17:33
 */
@Data
public class SpuBoundTo {

    private Long spuId;

    private BigDecimal buyBounds;

    private BigDecimal growBounds;
}
