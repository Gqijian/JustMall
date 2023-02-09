package com.kyson.mall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 二级分类
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Catalog2Vo {

    private String id;

    private String name;
    private String catalog1Id;  //一级父分类

    private List<Catalog3Vo> catelog3List;  //三级子分类

    /**
     * 三级分类
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Catalog3Vo{
        private String id;

        private String name;
        private String catalog2Id;  //父分类 2级分类id
    }
}
