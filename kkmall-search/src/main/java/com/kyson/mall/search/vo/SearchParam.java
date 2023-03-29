package com.kyson.mall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装页面可能传递过来的所有关键字
 */
@Data
public class SearchParam {

    private String keyword;

    private Long catalog3Id;

    /**
     * sort=saleCount_asc/desc
     * sort=skuPrice_asc
     * sort=hotScore_asc
     */
    private String sort;

    /**
     * skuPrice=1_500 1到500之间 _500 小于500 / 500_ 大于500
     */

    //是否显示有货
    private Integer hasStock = 1;

    //价格区间
    private String skuPrice;

    //品牌 ID
    private List<Long> brandId;

    //按照属性进行筛选
    private List<String> attrs;

    //页码
    private Integer pageNum = 1;

}
