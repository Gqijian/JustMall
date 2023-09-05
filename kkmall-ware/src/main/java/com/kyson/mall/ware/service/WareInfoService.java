package com.kyson.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kyson.common.utils.PageUtils;
import com.kyson.mall.ware.entity.WareInfoEntity;
import com.kyson.mall.ware.vo.FareVo;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author kyson
 * @email kysonxxxx@gmail.com
 * @date 2022-08-01 14:39:59
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     *
     * 根据用户的收货地址 计算运费
     *
     * @param addrId
     * @return
     */
    FareVo getFare(Long addrId);
}

