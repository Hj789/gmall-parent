package com.atguigu.gmall.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SeckillController {


    @GetMapping("/seckill.html")
    public String seckillListPage(Model model){

        //list.item.skuId skuDefaultImg skuName costPrice price num stockCount
        model.addAttribute("list",null);

        return "seckill/index";
    }
}
