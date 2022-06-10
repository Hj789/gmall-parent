package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.feign.order.OrderFeignClient;
import com.atguigu.gmall.model.vo.order.OrderConfirmVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Slf4j
@Controller
public class OrderController {

    @Autowired
    OrderFeignClient orderFeignClient;


    /**
     * 点击去结算,订单确认页
     * @return
     */
    @GetMapping("/trade.html")
    public String orderConfirmPage(Model model){
        //类型；byte=8bit int； Unsafe byte,  String   1110 0110 0000 0000 0000 1111 0001 0001
        //JavaBean 为何存在【强类型】？ ---  JSON  {k:v,k:v}
        //Map<String,Object>
        // js【弱类型】  var xx = ;  function sum(a,b){ return a+b};
        Result<Map<String, Object>> data = orderFeignClient.getOrderConfirmData();

        Map<String, Object> objectMap = data.getData();

//        model.addAttribute("detailArrayList",dataData.getDetailArrayList());
//        model.addAttribute("totalNum",dataData.getTotalNum());

        model.addAllAttributes(objectMap);

        return "order/trade";
    }

    /**
     * 订单列表页
     * @return
     */
    @GetMapping("/myOrder.html")
    public String orderListPage(){


        return "order/myOrder";
    }



}
