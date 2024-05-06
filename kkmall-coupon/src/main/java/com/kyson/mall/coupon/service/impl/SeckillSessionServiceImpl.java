package com.kyson.mall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kyson.common.utils.PageUtils;
import com.kyson.common.utils.Query;
import com.kyson.mall.coupon.dao.SeckillSessionDao;
import com.kyson.mall.coupon.entity.SeckillSessionEntity;
import com.kyson.mall.coupon.entity.SeckillSkuRelationEntity;
import com.kyson.mall.coupon.service.SeckillSessionService;
import com.kyson.mall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    private SeckillSkuRelationService  seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLatest3DaysSession()
    {
        //select * from sms_seckill_session where between start_time  and start_time

        //计算最近三天的时间
        List<SeckillSessionEntity> list = this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", startTime(), endTime()));

        if(list != null && list.size() > 0){
            List<SeckillSessionEntity> collect = list.stream().map(session -> {

                List<SeckillSkuRelationEntity> relationEntities = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", session.getId()));
                session.setRelationSkus(relationEntities);
                return session;
            }).collect(Collectors.toList());

            return collect;
        }

        return null;
    }

    private String startTime(){

        LocalTime min = LocalTime.MIN;
        LocalDate now = LocalDate.now();
        LocalDateTime start = LocalDateTime.of(now, min);
        String format = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;
    }

    private String endTime(){

        LocalTime max = LocalTime.MAX;
        LocalDate now = LocalDate.now();
        LocalDateTime end = LocalDateTime.of(now.plusDays(2), max);
        String format = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;
    }

}