package com.kyson.mall.product.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Kyson
 * @description:
 * @date: 2022/11/11 17:11
 */
@Data
public class AttrGroupRelationVo implements Serializable {

    private Long attrId;

    private Long attrGroupId;
}
