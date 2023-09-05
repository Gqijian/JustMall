package io.renren;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

@ToString
@Data
public class CertificateInfoVo {

    private String id;

    private Date createAt;

    private Date updateAt;

    private boolean deleted;

    private String version;

    private String commonName;

    private String subjectDn;

    private String serialNumber;

    private int certType;

    private String signatureAlgorithm;

    private String keyPairType;

    private Date notBefore;

    private Date notAfter;

    private String extKeyUsage;

    private String ocspUrl;

    private String crlUrl;

    private String cert;

    private String keyUsage;

    private int generateWay;

    private int keyGenWay;

    private String keyInfoId;

    private String profileId;

    private int certStatus;

    private String memberId;

    private String issuerId;

    private String accountId;

    private String issuerDn;

    private int issuerType;

    private String issuerName;

    private String alternativeCertId;

    private String privateKey;
}
