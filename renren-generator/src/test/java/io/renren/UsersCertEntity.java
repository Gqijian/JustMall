package io.renren;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author kyson
 * @email kysonxxxx@gmail.com
 * @date 2023-08-07 19:27:20
 */
@Data
public class UsersCertEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("gatewayId")
    private String gatewayId;

	@TableField("commonName")
    private String commonName;

	@TableField("subjectDn")
    private String subjectDn;

	@TableField("issuerDn")
    private String issuerDn;

	@TableField("issuerName")
    private String issuerName;

	@TableField("serialNumber")
    private String serialNumber;

	@TableField("signatureAlgorithm")
    private String signatureAlgorithm;

	@TableField("keyPairType")
    private String keyPairType;

	@TableField("notBefore")
    private Date notBefore;

	@TableField("notAfter")
    private Date notAfter;

	@TableField("cert")
    private String cert;

	@TableField("privateKey")
    private String privateKey;

	@TableField("updatetime")
    private Date updateTime;

	@TableField("createtime")
    private Date createTime;

}
