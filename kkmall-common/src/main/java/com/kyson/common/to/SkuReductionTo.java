package com.kyson.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Kyson
 * @description:
 * @date: 2022/11/16 17:51
 */
@Data
public class SkuReductionTo {

    private Long skuId;

    private Integer fullCount;

    private BigDecimal discount;

    private Integer countStatus;

    private BigDecimal fullPrice;

    private BigDecimal reducePrice;

    private Integer priceStatus;

    List<MemberPriceTo> memberPrice;
}
