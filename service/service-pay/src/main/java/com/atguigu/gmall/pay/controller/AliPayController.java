package com.atguigu.gmall.pay.controller;

import com.alipay.api.AlipayApiException;
import com.atguigu.gmall.pay.service.AlipayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/payment/alipay")
public class AliPayController {

    @Autowired
    AlipayService alipayService;

    ///api/payment/alipay/submit/742334158554005505

    @GetMapping(value = "/submit/{orderId}",produces = "text/html;chartset=utf-8")
    public String pay(@PathVariable("orderId") Long orderId)  throws AlipayApiException {
        //TODO 处理支付

        String result = alipayService.payPage(orderId);

        //6、把这个页面直接交给浏览器，浏览器自己渲染，就会自动提交表单给支付宝网关，支付宝网关给浏览器响应二维码收款台页面

        return result;
    }

}
