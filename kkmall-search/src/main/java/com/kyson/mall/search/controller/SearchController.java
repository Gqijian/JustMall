package com.kyson.mall.search.controller;

import com.kyson.mall.search.service.MallSearchService;
import com.kyson.mall.search.vo.SearchParam;
import com.kyson.mall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class SearchController {

    @Autowired
    private MallSearchService mallSearchService;
    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model) throws IOException
    {

        //根据传递来的参数 去es 中检索商品
        SearchResult result = mallSearchService.search(param);
        model.addAttribute("result", result);
        return "list";
    }
}
