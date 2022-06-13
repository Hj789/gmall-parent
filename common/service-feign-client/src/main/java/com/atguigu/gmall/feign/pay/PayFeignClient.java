package com.atguigu.gmall.feign.pay;

import com.atguigu.gmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("service-pay")
@RequestMapping("/rpc/inner/pay")
public interface PayFeignClient {

    /**
     * 查询某次交易详情
     */
    @GetMapping("/query/{outTradeNo}")
    Result<String> queryTrade(@PathVariable String outTradeNo);
}
