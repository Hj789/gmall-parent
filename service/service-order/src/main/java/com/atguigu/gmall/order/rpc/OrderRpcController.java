package com.atguigu.gmall.order.rpc;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.vo.order.OrderConfirmVo;
import com.atguigu.gmall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/rpc/inner/order")
@RestController
public class OrderRpcController {

    @Autowired
    OrderService orderService;

    /**
     * 返回订单确认页数据
     * @return
     */
    @GetMapping("/confirm")
    public Result<OrderConfirmVo> getOrderConfirmData(){

        OrderConfirmVo confirmVo = orderService.getOrderConfirmVData();

        return Result.ok(confirmVo);
    }


}
