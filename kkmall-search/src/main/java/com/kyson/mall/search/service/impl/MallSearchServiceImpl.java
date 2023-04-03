package com.kyson.mall.search.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.kyson.common.to.es.SkuEsModel;
import com.kyson.common.utils.R;
import com.kyson.mall.search.config.ElasticSearchConfig;
import com.kyson.mall.search.constant.EsConstant;
import com.kyson.mall.search.feign.ProductFeignService;
import com.kyson.mall.search.service.MallSearchService;
import com.kyson.mall.search.vo.AttrResponseVo;
import com.kyson.mall.search.vo.BrandVo;
import com.kyson.mall.search.vo.SearchParam;
import com.kyson.mall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam param)
    {

        //准备检索请求
        SearchRequest searchRequest = buildSearchRequest(param);
        SearchResult result = null;


        try {
            //执行检索请求
            SearchResponse response = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);

            //分析响应数据，封装成指定格式
            result = buildSearchResult(response, param);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }


    /**
     * 准备检索请求
     *
     * @param param
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param)
    {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        /**
         * 查询 模糊匹配 过滤
         */
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //must 模糊匹配
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }

        //bool filter 按照三级分类id查询
        if (param.getCatalog3Id() != null) {

            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }

        //bool filter 按照品牌id查询
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termQuery("brandId", param.getBrandId()));
        }
        //bool filter 按照所有指定的属性查询
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {


            for (String attStr : param.getAttrs()) {
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();

                String[] split = attStr.split("_");
                String attrId = split[0];   //检索的属性ID
                String[] attrValues = split[1].split(":");  // 属性值
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));

                //每一个都必须生成一个 nested 查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }

        }

        //bool filter 按照库存查询
        boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));

        //bool filter 按照价格区间
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            //1_500 _500 500_
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");

            String[] split = param.getSkuPrice().split("_");
            if (split.length == 2) {
                //get >   lte <
                rangeQuery.gte(split[0]).lte(split[1]);
            } else if (split.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    rangeQuery.lte(split[0]);
                }

                if (param.getSkuPrice().endsWith("_")) {
                    rangeQuery.gte(split[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }

        sourceBuilder.query(boolQuery);

        /**
         * 排序 分页 高亮
         */
        if (!StringUtils.isEmpty(param.getSort())) {
            String sort = param.getSort();
            String[] split = sort.split("_");
            SortOrder order = split[1].equals("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(split[0], order);
        }

        //分页
        /*
            pageNum 1 from 0 size 5 [0,1,2,3,4]
            pageNum  2 from 5 size 5
            from = (pageNum - 1) * size
         */
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        //高亮
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder builder = new HighlightBuilder();

            builder.field("skuTitle");
            builder.preTags("<b style = 'color:red'>");
            builder.postTags("</b>");
            sourceBuilder.highlighter(builder);
        }

        /**
         * 聚合分析
         */
        //品牌聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
        //品牌聚合的子聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        sourceBuilder.aggregation(brand_agg);

        //分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg");

        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        sourceBuilder.aggregation(catalog_agg);

        //属性聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");

        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        //聚合分析出 attr_id_agg 对应的名字和 属性值
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attr_agg.subAggregation(attr_id_agg);
        sourceBuilder.aggregation(attr_agg);

        System.out.println("构建的 DSL " + sourceBuilder.toString());

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
        return searchRequest;
    }

    /**
     * 分析响应数据，封装成指定格式
     *
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param)
    {

        SearchResult result = new SearchResult();

        // 返回所有查询到的商品
        SearchHits hits = response.getHits();
        ArrayList<SkuEsModel> esModels = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            List<SkuEsModel> list = new ArrayList<>();

            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();

                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);

                if (!StringUtils.isEmpty(param.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    skuEsModel.setSkuTitle(string);
                }

                esModels.add(skuEsModel);
            }
        }

        result.setProducts(esModels);

        // 所有商品涉及到的所有属性信息
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");

        List<SearchResult.AttrVo> attrVos = new ArrayList<>();

        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            //属性 id
            attrVo.setAttrId(bucket.getKeyAsNumber().longValue());

            //属性 名字
            String attr_name_agg = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attr_name_agg);

            //属性 所有值
            List<String> attr_value_agg = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(bucket1 -> {

                String keyAsString = bucket1.getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());

            attrVo.setAttrValue(attr_value_agg);

            attrVos.add(attrVo);
        }

        result.setAttrs(attrVos);

        // 当前商品涉及的品牌信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");

        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();

            //品牌 id
            brandVo.setBrandId(bucket.getKeyAsNumber().longValue());

            //品牌名
            String brand_name_agg = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brand_name_agg);
            //品牌图片
            String brand_img_agg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brand_img_agg);

            brandVos.add(brandVo);
        }

        // 当前商品涉及的所有分类信息 f4 打开Hierarchy
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");

        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {

            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();

            //分类ID
            catalogVo.setBrandId(Long.parseLong(bucket.getKeyAsString()));

            //分类名
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            catalogVo.setBrandName(catalog_name_agg.getBuckets().get(0).getKeyAsString());

        }

        result.setCatalogs(catalogVos);

        // ================= 以上都为聚合信息中抽取的 ====================

        // 分页信息 页码 总记录数 总页码
        result.setPageNum(param.getPageNum());

        long total = hits.getTotalHits().value;
        result.setTotal(total);

        Integer totalPages = (int) total % EsConstant.PRODUCT_PAGESIZE == 0 ? (int) total / EsConstant.PRODUCT_PAGESIZE : ((int) total / EsConstant.PRODUCT_PAGESIZE + 1);

        result.setTotalPages(totalPages);

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        //面包屑导航功能
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            List<SearchResult.NavVo> navVos = param.getAttrs().stream().map(attr -> {

                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] split = attr.split("_");
                navVo.setNavValue(split[1]);

                R r = productFeignService.attrInfo(Long.parseLong(split[0]));
                result.getAttrIds().add(Long.parseLong(split[0]));
                if (r.getCode() == 0) {
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });

                    navVo.setNavName(data.getAttrName());
                } else {
                    navVo.setNavName(split[0]);
                }

                //取消面包屑之后 跳转到哪里
                //拿到所有的查询条件 去掉当前的

                String replace = replaceQueryString(param, attr, "attrs");
                navVo.setLink("http://search.kysonmall.com/list.html?" + replace);

                return navVo;
            }).collect(Collectors.toList());

            result.setNavs(navVos);
        }

        if(param.getBrandId() != null && param.getBrandId().size() > 0){

            List<SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();

            //TODO 远程查询所有品牌
            R r = productFeignService.brandInfos(param.getBrandId());
            if(r.getCode() == 0){
                List<BrandVo> brands = r.getData("brands", new TypeReference<List<BrandVo>>() {
                });
                StringBuffer buffer = new StringBuffer();
                String replace = "";
                for (BrandVo brandVo: brands) {
                    buffer.append(brandVo.getBrandName()+";");
                    replace = replaceQueryString(param, brandVo.getBrandId()+"", "brandId");
                }

                navVo.setNavValue(buffer.toString());
                navVo.setLink("http://search.kysonmall.com/list.html?" + replace);
            }

            navs.add(navVo);
        }

        //TODO 分类 不需要导航取消

        return result;
    }

    private static String replaceQueryString(SearchParam param, String value, String key)
    {

        String encode = "";
        try {
            encode = URLEncoder.encode(value, "utf-8");//URLEncoder.DEFAULT
            encode.replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        String replace = param.get_queryString().replace("&"+key+"=" + encode, "");
        return replace;
    }
}
