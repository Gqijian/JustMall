package com.kyson.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kyson.common.to.SkuReductionTo;
import com.kyson.common.to.SpuBoundTo;
import com.kyson.common.to.es.SkuEsModel;
import com.kyson.common.utils.PageUtils;
import com.kyson.common.utils.Query;
import com.kyson.common.utils.R;
import com.kyson.mall.product.dao.SpuInfoDao;
import com.kyson.mall.product.entity.*;
import com.kyson.mall.product.feign.CouponFeignService;
import com.kyson.mall.product.feign.SearchFeignService;
import com.kyson.mall.product.feign.WareFeignService;
import com.kyson.mall.product.service.*;
import com.kyson.mall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService attrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }


    /**
     * //TODO 高级部分完善
     * @param vo
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveSpuInfo(SpuSaveVo vo)
    {
        //保存spu基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);

        //保存spu的描述图片 pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(spuInfoEntity.getId());
        descEntity.setDecript(String.join(",", decript));
        spuInfoDescService.saveSpuInfoDesc(descEntity);

        //保存spu的图片集 pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(), images);

        //保存spu规格参数 pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setSpuId(spuInfoEntity.getId());
            valueEntity.setAttrId(attr.getAttrId());

            AttrEntity id = attrService.getById(attr.getAttrId());

            if (!ObjectUtils.isEmpty(id))
            {
                valueEntity.setAttrName(id.getAttrName());
            } else
            {
                valueEntity.setAttrName("");
            }
            valueEntity.setAttrValue(valueEntity.getAttrValue());
            valueEntity.setQuickShow(attr.getShowDesc());

            return valueEntity;
        }).collect(Collectors.toList());
        attrValueService.saveProductAttrs(collect);

        //远程保存spu的积分信息 kkmall_sms -> sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if(r.getCode() != 0){
            log.error("远程保存spu积分信息失败");
        }

        //保存当前spu对应的所有sku信息
        // sku 基本信息 pms_sku_info
        List<Skus> skus = vo.getSkus();
        if(skus != null && skus.size() > 0){
            skus.forEach(item ->{

                String defaultImage = "";
                for (Images image : item.getImages())
                {
                    if(image.getDefaultImg() == 1){
                        defaultImage = image.getImgUrl();
                    }
                }

                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImage);

                skuInfoService.saveSkuInfo(skuInfoEntity);

                // sku 图片信息 pms_sku_images
                //保存之后就有主键了
                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity -> {
                    //返回true需要 返回false 过滤
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                //TODO 没有url的不存数据库
                skuImagesService.saveBatch(imagesEntities);

                // sku 销售属性 pms_sku_sale_attr_value
                List<Attr> attrs = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attrs.stream().map(attr -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());

                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                // sku 优惠满减的信息 kkmall_sms -> sms_sku_ladder sms_sku_full_reduction sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);

                if(skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal(0)) == 1){
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if(r1.getCode() != 0){
                        log.error("远程保存sku优惠信息失败");
                    }
                }

            });
        }


    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity)
    {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params)
    {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        String key = params.get("key").toString();
        if(!StringUtils.isEmpty(key)){
            wrapper.and(w -> {
               w.eq("id", key).or().like("spu_name", key);
            });
        }
        String status = params.get("status").toString();
        if(!StringUtils.isEmpty(status)){
            wrapper.eq("publish_status", status);
        }
        String brandId = params.get("brandId").toString();
        if(!StringUtils.isEmpty(brandId)){
            wrapper.eq("brand_id", brandId);
        }
        String catelogId = params.get("catelogId").toString();
        if(!StringUtils.isEmpty(catelogId)){
            wrapper.eq("catelog_id", catelogId);
        }


        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId)
    {
        //1、查出当前spuid对应的所有sku信息，品牌的名字
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIdList = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        //TODO 查询当前sku 所有的可以被检索的规格属性 放在外边查一遍就行
        List<ProductAttrValueEntity> baseAttrs = attrValueService.baseAttrListForSpu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        List<Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);
        Set<Long> idSet = new HashSet<>(searchAttrIds);

        List<SkuEsModel.Attrs> attrsList = baseAttrs.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item -> {

            SkuEsModel.Attrs attrs1 = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrs1);
            return attrs1;
        }).collect(Collectors.toList());

        //TODO  发送远程调用 库存系统查询是否有库存

        Map<Long, Boolean> stockMap = null;
        //远程调用可能有网络问题
        try {
            R<List<SkuHasStockVo>> skuHasStock = wareFeignService.getSkuHasStock(skuIdList);
            stockMap = skuHasStock.getData().stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, item -> item.getHasStock()));

        }catch (Exception e){
            log.error("库存服务查询异常 原因 {}", e);
        }

        //2、封装信息
        //java 8 effectively final
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> upProducts = skus.stream().map(sku -> {
            //组装数据
            SkuEsModel esModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, esModel);

            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());

            //拿到是否有库存
            if(finalStockMap == null){
                esModel.setHasStock(true);
            }else {
                esModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }

            //TODO  热度评分  0
            esModel.setHotScore(0L);

            //TODO  品牌和分类信息
            BrandEntity brandEntity = brandService.getById(esModel.getBrandId());
            esModel.setBrandName(brandEntity.getName());
            esModel.setBrandImg(brandEntity.getLogo());

            CategoryEntity category = categoryService.getById(esModel.getCatalogId());
            esModel.setCatalogName(category.getName());
            //设置检索属性
            esModel.setAttrs(attrsList);

            return esModel;
        }).collect(Collectors.toList());

        //TODO 保存在 elasticSearch 中保存
        R r = searchFeignService.productStatusUp(upProducts);
        if(r.getCode() == 0){
            //远程调用成功
            //TODO 修改 spu 状态
            //this.baseMapper.upda
        }else {
            //远程调用失败

        }
    }

}