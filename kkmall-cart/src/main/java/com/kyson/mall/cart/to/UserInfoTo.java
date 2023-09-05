package com.kyson.mall.cart.to;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@ToString
@Data
public class UserInfoTo implements Serializable {

    private Long userId;

    private String userKey;

    private Boolean tempUser = false;
}
