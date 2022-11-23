package com.kyson.mall.ware.vo;

import lombok.Data;

/**
 * @author Kyson
 * @description:
 * @date: 2022/11/22 15:49
 */
@Data
public class PurchaseItemDoneVo {

    private Long itemId;

    private Integer status;

    private String reason;
}
