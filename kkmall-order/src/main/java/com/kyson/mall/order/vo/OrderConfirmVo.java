package com.kyson.mall.order.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单确认页需要的数据
 */

public class OrderConfirmVo {

    // 收货地址，ums_member_receive_address 表
    @Getter
    @Setter
    private List<MemberAddressVo> address;

    // 购物清单，根据购物车页面传递过来的 skuIds 查询
    @Getter
    @Setter
    private List<OrderItemVo> items;

    //发票

    //优惠券信息
    @Getter
    @Setter
    private Integer integration;

    private BigDecimal total;

    //应付价格
    private BigDecimal payPrice;

    @Getter
    @Setter
    Map<Long, Boolean> stocks;


    // 订单令牌，防止重复提交
    @Getter
    @Setter
    private String orderToken;

    public Integer getCount(){

        Integer i = 0;
        if (items != null) {

            for (OrderItemVo item : items) {
                i+=item.getCount();
            }
        }
        return i;
    }

    public BigDecimal getTotal()
    {

        BigDecimal sum = new BigDecimal("0");
        if (items != null) {

            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

    public BigDecimal getPayPrice()
    {

        return getTotal();
    }
}
