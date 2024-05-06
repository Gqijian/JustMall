package com.kyson.mall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kyson.common.utils.PageUtils;
import com.kyson.common.utils.Query;
import com.kyson.mall.coupon.dao.SeckillSkuRelationDao;
import com.kyson.mall.coupon.entity.SeckillSkuRelationEntity;
import com.kyson.mall.coupon.service.SeckillSkuRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl extends ServiceImpl<SeckillSkuRelationDao, SeckillSkuRelationEntity> implements SeckillSkuRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params)
    {

        QueryWrapper<SeckillSkuRelationEntity> queryWrapper = new QueryWrapper<>();

        String promotionSessionId = params.get("promotionSessionId").toString();
        if(!StringUtils.isEmpty(promotionSessionId)){
            queryWrapper.eq("promotion_session_id", promotionSessionId);
        }

        IPage<SeckillSkuRelationEntity> page = this.page(
                new Query<SeckillSkuRelationEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

}