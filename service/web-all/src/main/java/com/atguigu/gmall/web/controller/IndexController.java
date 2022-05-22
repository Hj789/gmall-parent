package com.atguigu.gmall.web.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.feign.product.ProductFeignClient;
import com.atguigu.gmall.model.to.CategoryAndChildTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Slf4j
@Controller
public class IndexController {

    @Autowired
    ProductFeignClient productFeignClient;

    @GetMapping("/")
    public String indexPage(Model model){
        log.info("首页.....");

        Result<List<CategoryAndChildTo>> result = productFeignClient.getAllCategoryWithChilds();
        if (result.isOk()){
            //如果远程正常
            //远程真正返回的数据
            List<CategoryAndChildTo> data = result.getData();
            model.addAttribute("list",data);//所有分类数据
        }
        return "index/index";
    }
}
