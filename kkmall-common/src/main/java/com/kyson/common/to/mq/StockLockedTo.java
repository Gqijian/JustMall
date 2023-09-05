package com.kyson.common.to.mq;

import lombok.Data;

@Data
public class StockLockedTo  {

    //库存工作单id
    private Long id;

    //工作详情的 id
    private StockDetailTo detail;
}
