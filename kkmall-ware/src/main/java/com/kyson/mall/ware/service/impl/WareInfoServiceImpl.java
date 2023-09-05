package com.kyson.mall.ware.service.impl;

import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kyson.common.utils.PageUtils;
import com.kyson.common.utils.Query;
import com.kyson.common.utils.R;
import com.kyson.mall.ware.dao.WareInfoDao;
import com.kyson.mall.ware.entity.WareInfoEntity;
import com.kyson.mall.ware.feign.MemberFeignService;
import com.kyson.mall.ware.service.WareInfoService;
import com.kyson.mall.ware.vo.FareVo;
import com.kyson.mall.ware.vo.MemberAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params)
    {

        QueryWrapper<WareInfoEntity> wareInfoQueryWrapper = new QueryWrapper<>();

        String key = params.get("key").toString();

        if (!StringUtils.isEmpty(key)) {
            wareInfoQueryWrapper.eq("id", key)
                    .or().like("id", key)
                    .or().like("address", key)
                    .or().like("areacode", key);
        }

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wareInfoQueryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long addrId)
    {

        R r = memberFeignService.addrInfo(addrId);
        MemberAddressVo data = r.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {
        });

        //调用快递接口计算运费
        if (data != null) {

            FareVo fareVo = new FareVo();

            String phone = data.getPhone();
            String substring = phone.substring(phone.length() - 1, phone.length());

            fareVo.setAddress(data);
            fareVo.setFare(new BigDecimal(substring));

            return fareVo;
        }

        return null;
    }

}