package com.kyson.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Kyson
 * @description:
 * @date: 2022/11/16 17:53
 */
@Data
public class MemberPriceTo {

    private Long id;

    private String name;

    private BigDecimal price;

}
