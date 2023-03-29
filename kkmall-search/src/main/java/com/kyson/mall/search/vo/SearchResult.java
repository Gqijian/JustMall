package com.kyson.mall.search.vo;

import com.kyson.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

@Data
public class SearchResult {

    //查询到的所有商品信息
    private List<SkuEsModel> products;

    //==== 下面为分页信息 ========

    //当前页码
    private Integer pageNum;

    //总记录数
    private Long total;

    //总页码
    private Integer totalPages;

    private List<Integer> pageNavs;

    //查询结果中所有属性
    private List<AttrVo> attrs;

    //查询结果中所有品牌
    private List<BrandVo> brands;

    private List<CatalogVo> catalogs;


    //====== 以上为返给页面的所有信息 ========

    //面包屑 导航
    private List<NavVo> navs;

    @Data
    public static class NavVo{
        private String navName;
        private String navValue;

        private String link;
    }

    @Data
    public static class AttrVo{
        private Long attrId;

        private String attrName;

        private List<String> attrValue;

    }
    @Data
    public static class BrandVo{
        private Long brandId;

        private String brandName;

        private String brandImg;

    }

    @Data
    public static class CatalogVo{
        private Long brandId;

        private String brandName;

        private String brandImg;

    }

}
