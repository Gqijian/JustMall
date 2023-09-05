package io.renren;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.alibaba.fastjson.JSON;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveGenParameterSpec;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.StringWriter;
import java.security.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RenrenApplicationTests {

    @Test
    public void contextLoads()
    {

    }

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    void generateCSR() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, OperatorCreationException, IOException, NoSuchProviderException
    {

        //Security.addProvider(new BouncyCastleProvider());

        // 生成 ECDSA P-256 密钥对
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
        ECNamedCurveGenParameterSpec parameterSpec = new ECNamedCurveGenParameterSpec("P-256");
        keyPairGenerator.initialize(parameterSpec);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        PublicKey aPublic = keyPair.getPublic();

        // 私钥保存好，建议使用专门的密码模块产生私钥
        PrivateKey aPrivate = keyPair.getPrivate();

        String privateKeyStr = Base64.getEncoder().encodeToString(aPrivate.getEncoded());
        System.out.println(privateKeyStr);


        // 生成证书签名请求(CSR)，为了便于区分，建议添加一些唯一性标识在 CN 值中
        X500Name subjectDN = new X500Name("CN=FeiBit DAC Certificate Signing Request");

        JcaPKCS10CertificationRequestBuilder certificationRequestBuilder =
                new JcaPKCS10CertificationRequestBuilder(subjectDN, aPublic);

        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withECDSA")
                .build(aPrivate);
        PKCS10CertificationRequest pkcs10CertificationRequest = certificationRequestBuilder
                .build(contentSigner);

        HashMap<String, String> key = new HashMap<>();

        PemObject privateKeyPemObject = new PemObject(PEMParser.TYPE_PRIVATE_KEY, aPrivate.getEncoded());
        try (StringWriter stringWriter = new StringWriter();
             JcaPEMWriter jcaPEMWriter = new JcaPEMWriter(stringWriter)){
            jcaPEMWriter.writeObject(privateKeyPemObject);
            jcaPEMWriter.flush();
            String pemPrivateKeyStr = stringWriter.toString();
            key.put("privateKey", pemPrivateKeyStr);
        }


        // 将 CSR 转换为 PEM 格式字符串
        PemObject pemObject = new PemObject(PEMParser.TYPE_CERTIFICATE_REQUEST, pkcs10CertificationRequest.getEncoded());
        try (StringWriter stringWriter = new StringWriter();
             JcaPEMWriter jcaPEMWriter = new JcaPEMWriter(stringWriter))
        {
            jcaPEMWriter.writeObject(pemObject);
            jcaPEMWriter.flush();
            // 得到 PEM 格式的 CSR 编码字符串，每次申请证书时，都需要重新生成密钥、产生对应的 CSR
            // 将 CSR 通过 certificateRequest 参数传递给 证书服务平台
            String csr = stringWriter.toString();
            key.put("csr", csr);

        }

        System.out.println();

    }

    @Test
    void testRequestCertificate()
    {

        String requestUri = CertConstant.CERT_HOST + CertConstant.CERT_API_CERTIFICATES;

        // generate payload struct
        Map<String, Object> payload = new HashMap<>();
        payload.put("api", CertConstant.CERT_API_CERTIFICATES);
        payload.put("accessKey", CertConstant.ACCESS_KEY);
        payload.put("timestamp", System.currentTimeMillis());
        payload.put("signNonce", UUID.randomUUID().toString().replace("-", ""));

        JWT jwt = JWT.create()
                .setHeader("alg", "HS256")
                .setSigner(JWTSignerUtil.hs256(CertConstant.SECRET_KEY.getBytes()))
                .addPayloads(payload);
        String requestToken = jwt.sign();

        HashMap<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("issuerId", CertConstant.CERT_ISSUER_ID);
        bodyMap.put("profileId", CertConstant.CERT_PROFILE_ID);
        bodyMap.put("keyAlg", "ECDSA");
        bodyMap.put("keyParam", "P-256");
        bodyMap.put("keyGenWay", 4);
        bodyMap.put("requiredKey", true);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        bodyMap.put("notBefore", ZonedDateTime.now().format(formatter));
        bodyMap.put("notAfter", "2025-03-13 23:19:46");

        JSONArray subjectDnArray = new JSONArray();
        // CN 需要做对应修改
        subjectDnArray.put(new JSONObject().set("id", 0).set("item", "CN").set("value", CertConstant.CERT_SUBJECTDN_CN));
        // 设置 PID
        subjectDnArray.put(new JSONObject().set("id", 0).set("item", "MatterProductIdentifier").set("value", CertConstant.CERT_MATTER_PRODUCT_ID));
        // 设置 VID
        subjectDnArray.put(new JSONObject().set("id", 0).set("item", "MatterVendorIdentifier").set("value", CertConstant.CERT_MATTER_VENDOR_ID));
        bodyMap.put("subjectDn", subjectDnArray);

        // 得到请求 JSON 请求体
        String requestBody = JSONUtil.toJsonStr(bodyMap);

        // 构建请求参数
        HttpRequest httpRequest = HttpUtil.createPost(requestUri)
                .header("Content-Type", "application/json")
                .setReadTimeout(10000)
                .body(requestBody)
                .bearerAuth(requestToken);
        System.out.println(httpRequest);

        // 发起请求
        String rep = httpRequest.execute().body().toString();

        com.alibaba.fastjson.JSONObject repJson = JSON.parseObject(rep);

        System.out.println();
        if (repJson.getInteger("code") == 0) {

            CertificateInfoVo certificateInfoVo = JSON.parseObject(repJson.getJSONObject("data").getString("cmsCertificateInfoVo"), CertificateInfoVo.class);

            UsersCertEntity entity = new UsersCertEntity();
            BeanUtils.copyProperties(certificateInfoVo, entity);

            System.out.println(entity);
        }
    }

}
