package com.kyson.mall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Kyson
 * @description:
 * @date: 2022/11/22 15:48
 */
@Data
public class PurchaseDoneVo {

    @NotNull
    private Long id;    //采购单id

    private List<PurchaseItemDoneVo> items;

}
