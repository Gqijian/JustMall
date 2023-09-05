package com.kyson.mall.member.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kyson.common.utils.HttpUtils;
import com.kyson.common.utils.PageUtils;
import com.kyson.common.utils.Query;
import com.kyson.mall.member.dao.MemberDao;
import com.kyson.mall.member.entity.MemberEntity;
import com.kyson.mall.member.entity.MemberLevelEntity;
import com.kyson.mall.member.exception.PhoneExistException;
import com.kyson.mall.member.exception.UserNameExistException;
import com.kyson.mall.member.service.MemberLevelService;
import com.kyson.mall.member.service.MemberService;
import com.kyson.mall.member.vo.MemberLoginVo;
import com.kyson.mall.member.vo.MemberRegistVo;
import com.kyson.mall.member.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params)
    {

        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo)
    {

        MemberEntity entity = new MemberEntity();

        //设置默认等级
        MemberLevelEntity levelEntity = memberLevelService.getDefaultLevel();
        entity.setLevelId(levelEntity.getId());

        //检查用户名 和 手机号的唯一性 为了让controller 感知异常
        checkPhoneUnique(vo.getPhone());
        checkUserNameUnique(vo.getUserName());

        entity.setMobile(vo.getPhone());
        entity.setUsername(vo.getUserName());
        entity.setNickname(vo.getUserName());

        //密码要进行加密存储
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        entity.setPassword(encoder.encode(vo.getPassword()));

        //其他默认信息

        this.baseMapper.insert(entity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException
    {

        Integer mobile = Math.toIntExact(this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone)));
        if (mobile > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUserNameUnique(String username) throws UserNameExistException
    {

        Integer user = Math.toIntExact(this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username)));

        if (user > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkEmailUnique(String email)
    {

    }

    @Override
    public MemberEntity login(MemberLoginVo vo)
    {
        //select * from ums_member where username = or mobile =

        MemberEntity entity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", vo.getLoginacct())
                .or().eq("mobile", vo.getLoginacct()));
        if (ObjectUtils.isEmpty(entity)) {
            return null;
        } else {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            //密码校验
            boolean matches = encoder.matches(vo.getPassword(), entity.getPassword());

            if (matches) {
                return entity;
            } else {
                return null;
            }
        }
    }

    @Override
    public MemberEntity login(SocialUser socialUser) throws Exception
    {

        MemberEntity entity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", socialUser.getUid()));

        if (!ObjectUtils.isEmpty(entity)) {
            MemberEntity update = new MemberEntity();
            update.setId(entity.getId());
            update.setSocialUid(socialUser.getUid());
            update.setAccessToken(socialUser.getAccess_token());
            update.setExpiresIn(socialUser.getExpires_in());
            this.baseMapper.updateById(update);

            entity.setAccessToken(socialUser.getAccess_token());
            entity.setExpiresIn(socialUser.getExpires_in());
            return entity;
        } else {

            //没有查到当前社交用户
            MemberEntity regist = new MemberEntity();

            try {
                //查询当前用户的 社交信息
                Map<String, String> param = new HashMap<>();
                param.put("uid", socialUser.getUid());
                param.put("access_token", socialUser.getAccess_token());
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<String, String>(), param);

                if (response.getStatusLine().getStatusCode() == 200) {
                    String json = EntityUtils.toString(response.getEntity());

                    //微博社交帐号
                    JSONObject jsonObject = JSON.parseObject(json);

                    regist.setNickname(jsonObject.getString("name"));
                    regist.setGender(jsonObject.getString("gender").equals("m") ? 1 : 0);

                }
            } catch (Exception e) {
            }

            regist.setSocialUid(socialUser.getUid());
            regist.setAccessToken(socialUser.getAccess_token());
            regist.setExpiresIn(socialUser.getExpires_in());

            this.baseMapper.insert(regist);
            return regist;
        }

    }

}