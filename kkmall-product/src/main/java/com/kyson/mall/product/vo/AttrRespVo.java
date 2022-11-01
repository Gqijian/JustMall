package com.kyson.mall.product.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Administrator<br />
 * @description: <br/>
 * @date: 2022/9/8 10:43<br/>
 */
@Data
public class AttrRespVo extends AttrVo implements Serializable {

    //所属分类名字
    private String catelogName;

    //所属分组名字
    private String groupName;

    private Long[] catelogPath;
}
