package com.kyson.mall.ware.service.impl;

import com.kyson.common.constant.WareConstant;
import com.kyson.mall.ware.entity.PurchaseDetailEntity;
import com.kyson.mall.ware.service.PurchaseDetailService;
import com.kyson.mall.ware.service.WareSkuService;
import com.kyson.mall.ware.vo.MergeVo;
import com.kyson.mall.ware.vo.PurchaseDoneVo;
import com.kyson.mall.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kyson.common.utils.PageUtils;
import com.kyson.common.utils.Query;

import com.kyson.mall.ware.dao.PurchaseDao;
import com.kyson.mall.ware.entity.PurchaseEntity;
import com.kyson.mall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params)
    {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params)
    {
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();


        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper.eq("status", 0).or().eq("status", 1)
        );

        return new PageUtils(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void mergePurchase(MergeVo mergeVo)
    {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null)
        {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);

            purchaseId = purchaseEntity.getId();
        }

        //TODO 确认采购单状态 是 0 1 才能合并

        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(i -> {

            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();

            purchaseDetailEntity.setId(i);
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailEnum.ASSIGNED.getCode());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(collect);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    /**
     * 采购单 id
     *
     * @param ids
     */
    @Override
    public void received(List<Long> ids)
    {
        //确认当前采购单是新建或者已分配状态
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity byId = this.getById(id);
            return byId;
        }).filter(item -> {
            if (item.getStatus().equals(WareConstant.PurchaseStatusEnum.CREATED.getCode())
                    || item.getStatus().equals(WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()))
            {
                return true;
            } else
            {
                return false;
            }
        }).map(item -> {

            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());

        //改变采购单状态
        this.updateBatchById(collect);

        //改变采购项状态
        collect.forEach((item) -> {
            List<PurchaseDetailEntity> entities = purchaseDetailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> detailEntities = entities.stream().map(entity -> {

                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(entity.getId());
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailEnum.BUYING.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(detailEntities);
        });
    }

    @Override
    public void done(PurchaseDoneVo doneVo)
    {

        Long id = doneVo.getId();

        //改变采购项的状态
        Boolean flag = true;
        List<PurchaseItemDoneVo> items = doneVo.getItems();
        List<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseItemDoneVo item : items)
        {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            if(item.getStatus().equals(WareConstant.PurchaseDetailEnum.HASERROR.getCode())){
                flag = false;
                detailEntity.setStatus(item.getStatus());
            }else {
                detailEntity.setStatus(WareConstant.PurchaseDetailEnum.FINISH.getCode());
                //将成功采购的进行入库
                PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum());
            }
            detailEntity.setId(item.getItemId());

            updates.add(detailEntity);
        }
        purchaseDetailService.updateBatchById(updates);

        // 改变采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag?WareConstant.PurchaseStatusEnum.FINISH.getCode() : WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);



    }

}