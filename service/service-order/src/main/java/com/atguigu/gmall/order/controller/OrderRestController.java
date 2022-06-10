package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.feign.cart.CartFeignClient;
import com.atguigu.gmall.model.vo.order.OrderSubmitVo;
import com.atguigu.gmall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/order/auth")
@RestController
public class OrderRestController {

    @Autowired
    OrderService orderService;


    /**
     * http://api.gmall.com/api/order/auth/submitOrder?tradeNo=1171b5d509b24cf89cb6d417bc76e3ce
     *
     * @return
     */
    @PostMapping("/submitOrder")
    public Result submitOrder(@RequestParam String tradeNo,
                              @RequestBody OrderSubmitVo orderSubmitVo){

        //TODO 提交订单
        Long orderId = orderService.submitOrder(tradeNo, orderSubmitVo);

        //前端不能接到Long 的数据 ,需要转成字符串
        return Result.ok(orderId.toString());
    }


}
