package com.atguigu.gmall.feign.seckill;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.order.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/rpc/inner/seckill")
@FeignClient("service-seckill")
public interface SeckillFeignClient {

    /**
     * 获取秒杀的所有商品
     * @return
     */
    @GetMapping("/currentday/goods")
    Result<List<SeckillGoods>> queryCurrentDaySeckillGoods();

    /**
     * 获取某个秒杀商品的详情
     * @param skuId
     * @return
     */
    @GetMapping("/detail/{skuId}")
    Result<SeckillGoods> seckillGoodsDetail(@PathVariable Long skuId);

    @GetMapping("/orderinfo/{skuId}")
    Result<OrderInfo> getOrderInfoBySkuId(@PathVariable("skuId") Long skuId);
}
