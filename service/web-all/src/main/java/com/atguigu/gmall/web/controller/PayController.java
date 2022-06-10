package com.atguigu.gmall.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PayController {

        ///pay.html?orderId=742160939633082368
    @GetMapping("/pay.html")
    public String payPage(@RequestParam Long orderId){


        return "payment/pay";
    }

}
