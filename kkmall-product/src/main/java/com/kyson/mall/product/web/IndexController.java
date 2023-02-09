package com.kyson.mall.product.web;

import com.kyson.mall.product.entity.CategoryEntity;
import com.kyson.mall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 *         <dependency>
 *             <groupId>org.springframework.boot</groupId>
 *             <artifactId>spring-boot-devtools</artifactId>
 *             <optional>true</optional>
 *         </dependency>
 *
 *         不重启项目 刷新页面 导入 devtools 并且 optional 为 true
 *         并且在 yml 中关闭 thymeleaf 的缓存 然后在编译器页面中 ctrl + f9 / ctrl + shift + f9 自动编译下页面
 */

@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model){

        List<CategoryEntity> categoryEntities = categoryService.getLevel1Category();

        //视图解析器进行拼串
        //默认前缀 classpath:/templates/ + 返回值 +  默认后缀 .html
        model.addAttribute("categorys", categoryEntities);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Object    getCatalogJson(){

        return "";
    }
}
