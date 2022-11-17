package com.kyson.mall.product.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.kyson.mall.product.entity.AttrEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Kyson
 * @description:
 * @date: 2022/11/15 16:43
 */
@Data
public class AttrGroupWithAttrsVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;

    private List<AttrEntity> attrs;
}
