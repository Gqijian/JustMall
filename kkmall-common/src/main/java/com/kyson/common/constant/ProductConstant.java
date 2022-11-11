package com.kyson.common.constant;

import lombok.Data;

/**
 * @author Kyson
 * @description:
 * @date: 2022/11/11 14:59
 */
public class ProductConstant {


    public enum AttrEnum {
        ATTR_TYPE_BASE(1, "基本属性"),
        ATTR_TYPE_SALE(0, "销售属性");

        private Integer code;

        private String msg;

        AttrEnum(Integer code, String msg)
        {
            this.code = code;
            this.msg = msg;
        }

        public Integer getCode()
        {
            return code;
        }

        public String getMsg()
        {
            return msg;
        }
    }
}
