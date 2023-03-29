package com.kyson.mall.search.service;

import com.kyson.mall.search.vo.SearchParam;
import com.kyson.mall.search.vo.SearchResult;

import java.io.IOException;

public interface MallSearchService {

    /**
     * 检索的所有参数
     * @param param
     * @return
     */
    SearchResult search(SearchParam param) throws IOException;
}
