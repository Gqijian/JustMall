package com.kyson.common.constant;

/**
 * @author Kyson
 * @description:
 * @date: 2022/11/22 10:32
 */
public class WareConstant {

    public enum PurchaseStatusEnum {
        CREATED(0, "新建"),
        ASSIGNED(1, "已分配"),
        RECEIVE(2, "已领取"),
        FINISH(3, "已完成"),
        HASERROR(4, "有异常");

        private Integer code;

        private String msg;

        PurchaseStatusEnum(Integer code, String msg)
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

    public enum PurchaseDetailEnum {
        CREATED(0, "新建"),
        ASSIGNED(1, "已分配"),
        BUYING(2, "正在采购"),
        FINISH(3, "已完成"),
        HASERROR(4, "采购失败");

        private Integer code;

        private String msg;

        PurchaseDetailEnum(Integer code, String msg)
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
