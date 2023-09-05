package com.kyson.mall.member.controller;

import com.kyson.common.exception.BizCodeEnum;
import com.kyson.common.utils.PageUtils;
import com.kyson.common.utils.R;
import com.kyson.mall.member.entity.MemberEntity;
import com.kyson.mall.member.exception.PhoneExistException;
import com.kyson.mall.member.exception.UserNameExistException;
import com.kyson.mall.member.feign.CouponFeignService;
import com.kyson.mall.member.service.MemberService;
import com.kyson.mall.member.vo.MemberLoginVo;
import com.kyson.mall.member.vo.MemberRegistVo;
import com.kyson.mall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 会员
 *
 * @author kyson
 * @email kysonxxxx@gmail.com
 * @date 2022-08-01 10:28:50
 */
@RestController
@RequestMapping("member/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;

    @RequestMapping("/coupons")
    public R test()
    {

        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("MemberController");

        R memberCoupons = couponFeignService.memberCoupons();

        return R.ok().put("memberEntity", memberEntity).put("coupons", memberCoupons);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params)
    {

        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }

    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo vo)
    {

        try {
            memberService.regist(vo);

        } catch (PhoneExistException e) {
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnum.PHONE_EXIST_EXCEPTION.getMsg());
        } catch (UserNameExistException e) {
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    @PostMapping("/oauth2/login")
    public R oauthLogin(@RequestBody SocialUser socialUser) throws Exception
    {

        MemberEntity entity = memberService.login(socialUser);

        if(entity != null){
            return R.ok().setData(entity);
        }else {
            return R.error(BizCodeEnum.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getCode(), BizCodeEnum.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getMsg());
        }

    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo){

        MemberEntity entity = memberService.login(vo);

        if(entity != null){
            return R.ok().setData(entity);
        }else {
            return R.error(BizCodeEnum.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getCode(), BizCodeEnum.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getMsg());
        }

    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id)
    {

        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member)
    {

        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member)
    {

        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids)
    {

        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
