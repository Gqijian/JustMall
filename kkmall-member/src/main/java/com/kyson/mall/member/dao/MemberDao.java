package com.kyson.mall.member.dao;

import com.kyson.mall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author kyson
 * @email kysonxxxx@gmail.com
 * @date 2022-08-01 10:28:50
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
