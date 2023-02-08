package com.kyson.kkmall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.kyson.common.to.es.SkuEsModel;
import com.kyson.kkmall.search.config.ElasticSearchConfig;
import com.kyson.kkmall.search.constant.EsConstant;
import com.kyson.kkmall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    RestHighLevelClient restHighLevelClient;
    @Override
    public Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException
    {
        //保存到es
        //给 es 建立索引 product 建立好映射关系 product-mapping.txt

        //保存这些数据
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel model : skuEsModels) {
            //构造保存请求
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(model.getSkuId().toString());
            String s = JSON.toJSONString(model);
            indexRequest.source(s, XContentType.JSON);

            bulkRequest.add(indexRequest);
        }

        //TODO 如果批量错误 可以处理
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, ElasticSearchConfig.COMMON_OPTIONS);
        Boolean hasFailures = bulk.hasFailures();
        List<String> collect = Arrays.asList(bulk.getItems()).stream().map(item -> {
            return item.getId();
        }).collect(Collectors.toList());

        log.info("商品上架完成：{}, 返回数据：{}",collect, bulk.toString());

        return hasFailures;
    }
}
