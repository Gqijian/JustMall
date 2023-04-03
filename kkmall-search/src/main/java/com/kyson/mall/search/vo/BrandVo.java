package com.kyson.mall.search.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Kyson
 * @description:
 * @date: 2022/11/14 18:00
 */
@Data
public class BrandVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long brandId;

    private String brandName;
}
