package com.atguigu.gmall.feign.order;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.vo.order.OrderConfirmVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@FeignClient("service-order")
@RequestMapping("/rpc/inner/order")
public interface OrderFeignClient {


    @GetMapping("/confirm")
    Result<Map<String,Object>> getOrderConfirmData();

    @GetMapping("/info/{id}")
    Result<OrderInfo> getOrderInfoByUserId(@PathVariable("id") Long orderId);

    /**
     * 修改订单为已支付
     * @param outTradeNo
     * @return
     */
    @GetMapping("/status/update/paid/{outTradeNo}")
    Result updateOrderStatusToPAID(@PathVariable("outTradeNo") String outTradeNo);

    /**
     * 检查数据库此订单是否已经同步了支付宝订单的状态
     * @return
     */
    @GetMapping("/status/checkandsync/{outTradeNo}")
    Result checkOrderStatus(@PathVariable("outTradeNo") String outTradeNo);
}
