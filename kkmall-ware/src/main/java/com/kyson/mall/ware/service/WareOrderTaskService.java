package com.kyson.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kyson.common.utils.PageUtils;
import com.kyson.mall.ware.entity.WareOrderTaskEntity;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author kyson
 * @email kysonxxxx@gmail.com
 * @date 2022-08-01 14:39:59
 */
public interface WareOrderTaskService extends IService<WareOrderTaskEntity> {

    PageUtils queryPage(Map<String, Object> params);

    WareOrderTaskEntity getOrderTaskByOrderSn(String orderSn);
}

