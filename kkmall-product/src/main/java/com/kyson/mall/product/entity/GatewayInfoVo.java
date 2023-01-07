package com.kyson.mall.product.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

/**
 * @author Kyson
 * @description:
 * @date: 2022/12/15 15:34
 */
@Data
public class GatewayInfoVo {
    @NotBlank
    String gatewayId;

    @NotBlank
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    String gatewayPwd;

    @NotEmpty
    Integer state;
}
