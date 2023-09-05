package com.kyson.mall.search;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.kyson.mall.search.config.ElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
class KkmallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    @Test
    public void jsonTest1(){
        String str = "{\n" +
                "  \"fileList\": [\n" +
                "    {\n" +
                "      \"type\": \"MCU\",\n" +
                "      \"version\": \"0.9.3\",\n" +
                "      \"fileName\": \" mcu_0.9.3-u.bin\",\n" +
                "      \"key\": \"Sha256sum\",\n" +
                "      \"model\": \"FGS80T0-FBHW\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"FW\",\n" +
                "      \"version\": \"1.7.7\",\n" +
                "      \"fileName\": \" fw_1.7.7-v.tar.gz\",\n" +
                "      \"key\": \"Sha256sum\",\n" +
                "      \"model\": \"FGS80T0-FBHW\"\n" +
                "    },\n" +
                "\n" +
                "    {\n" +
                "      \"type\": \"MCU\",\n" +
                "      \"version\": \"0.9.3\",\n" +
                "      \"fileName\": \" mcu_0.9.3-u.bin\",\n" +
                "      \"key\": \"Sha256sum\",\n" +
                "      \"model\": \"FGS80B0-FBHW\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"FW\",\n" +
                "      \"version\": \"1.7.7\",\n" +
                "      \"fileName\": \" fw_1.7.7-v.tar.gz\",\n" +
                "      \"key\": \"Sha256sum\",\n" +
                "      \"model\": \"FGS80B0-FBHW\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";
        JSONObject genConf = JSON.parseObject(str);
        String fileListStr = genConf.getString("fileList");

        System.out.println(fileListStr);
        System.out.println();
    }

    @Data
    class EsUser{
        private String userName;
        private String age;
    }

    @Test
    public void testRedisson(){

        Integer[] aa = {1,2};
        String s = JSON.toJSONString(aa);
        System.out.println("aaaaaaaaaaa" + s);
    }

    public void searchData() throws IOException
    {
        //1、创建检索请求
        SearchRequest searchRequest = new SearchRequest();

        //指定索引
        searchRequest.indices("bank");

        //指定dsl 检索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));
//        searchSourceBuilder.from();
//        searchSourceBuilder.size();
//        searchSourceBuilder.aggregation();

        //按照年龄的值分布聚合
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        searchSourceBuilder.aggregation(ageAgg);

        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        searchSourceBuilder.aggregation(balanceAvg);

        searchRequest.source(searchSourceBuilder);
        //执行检索
        SearchResponse search = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);

        SearchHits hits = search.getHits();
        SearchHit[] searchHits = hits.getHits();

        //json 转对象
        EsUser esUser = JSON.parseObject("", EsUser.class);
    }

    /**
     * 测试存储请求
     */
    public void indexData() throws IOException
    {

        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
        //indexRequest.source("username", "zhangsan", "age", 18);

        EsUser esUser = new EsUser();
        String json = JSON.toJSONString(esUser);
        indexRequest.source(json, XContentType.JSON);   //要保存的内容

        IndexResponse index = client.index(indexRequest, ElasticSearchConfig.COMMON_OPTIONS);

    }

    @Test
    public void contextLoads()
    {

        System.out.println("aaa " + client);
    }

}
