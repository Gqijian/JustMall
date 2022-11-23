package com.kyson.mall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Kyson
 * @description:
 * @date: 2022/11/22 10:24
 */

@Data
public class MergeVo {

    private Long purchaseId;

    private List<Long> items;
}
