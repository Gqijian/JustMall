package com.kyson.mall.order.vo;

import com.kyson.mall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderResponseVo {

    //错误状态码 0成功
    private Integer code;

    private OrderEntity order;
}
