package com.kyson.mall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import com.kyson.common.valid.AddGroup;
import com.kyson.common.valid.ListValue;
import com.kyson.common.valid.UpdateGroup;
import com.kyson.common.valid.UpdateStatusGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 * 
 * @author kyson
 * @email kysonxxxx@gmail.com
 * @date 2022-07-29 16:39:52
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@NotNull(message = "修改必须指定ID", groups = {UpdateGroup.class})
	@Null(message = "新增不能指定ID", groups = {AddGroup.class})
	@TableId
	private Long brandId;

	/**
	 * 品牌名
	 */
	@NotBlank(message = "必须有名字", groups = {AddGroup.class})
	private String name;

	/**
	 * 品牌logo地址
	 */
	@NotBlank(groups = {AddGroup.class})
	@URL(message = "uri地址不合法", groups = {AddGroup.class,UpdateGroup.class})
	private String logo;

	/**
	 * 介绍
	 */
	private String descript;

	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@NotNull(groups = {AddGroup.class, UpdateStatusGroup.class})
	@ListValue(values = {0, 1}, groups = {AddGroup.class, UpdateStatusGroup.class})
	private Integer showStatus;

	/**
	 * 检索首字母
	 */
	@NotEmpty(groups = {AddGroup.class})
	@Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须是一个字母", groups = {AddGroup.class,UpdateGroup.class})
	private String firstLetter;

	/**
	 * 排序
	 * Integer 不能用 @NotEmpty 约束
	 */
	@NotNull(groups = {AddGroup.class})
	@Min(value = 0, message = "排序必须大于零", groups = {AddGroup.class,UpdateGroup.class})
	private Integer sort;

}
