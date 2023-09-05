package com.kyson.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kyson.common.utils.PageUtils;
import com.kyson.mall.member.entity.MemberEntity;
import com.kyson.mall.member.exception.PhoneExistException;
import com.kyson.mall.member.exception.UserNameExistException;
import com.kyson.mall.member.vo.MemberLoginVo;
import com.kyson.mall.member.vo.MemberRegistVo;
import com.kyson.mall.member.vo.SocialUser;

import java.util.Map;

/**
 * 会员
 *
 * @author kyson
 * @email kysonxxxx@gmail.com
 * @date 2022-08-01 10:28:50
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo vo);

    void checkPhoneUnique(String phone) throws PhoneExistException;

    void checkUserNameUnique(String username) throws UserNameExistException;

    void checkEmailUnique(String email);

    MemberEntity login(MemberLoginVo vo);

    MemberEntity login(SocialUser socialUser) throws Exception;
}

