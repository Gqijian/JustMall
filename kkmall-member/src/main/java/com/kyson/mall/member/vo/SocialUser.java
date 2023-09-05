package com.kyson.mall.member.vo;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SocialUser {

    /**
     * 令牌
     */
    private String access_token;

    private String remind_in;

    /**
     * 令牌过期时间
     */
    private long expires_in;

    /**
     * 该社交用户的唯一标识
     */
    private String uid;

    private String isRealName;
}
