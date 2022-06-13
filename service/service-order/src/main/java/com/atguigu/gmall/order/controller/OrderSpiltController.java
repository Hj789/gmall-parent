package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.mqto.ware.WareOrderTo;
import com.atguigu.gmall.model.vo.order.OrderSpiltVo;
import com.atguigu.gmall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderSpiltController {

    @Autowired
    OrderService orderService;
    /**
     * 拆单；
     * 由库存服务远程调用
     * @param spiltVo
     * @return
     */
    @PostMapping("/orderSplit")
    public List<WareOrderTo> orderSplit(OrderSpiltVo spiltVo){

        System.out.println("收到拆单数据："+spiltVo);
        //订单服务按照 库存提供的 spilt组合信息，拆出子订单，返回给库存系统
        List<WareOrderTo> order = orderService.orderSpilt(spiltVo);

        return order;
    }
}
