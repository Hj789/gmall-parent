package com.atguigu.gmall.seckill.rpc;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.seckill.biz.SeckillGoodsLocalCache;
import com.atguigu.gmall.seckill.service.SeckillBizService;
import com.atguigu.gmall.seckill.service.SeckillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.soap.Detail;
import java.util.List;

@RestController
@RequestMapping("/rpc/inner/seckill")
public class SeckillRpcController {

    @Autowired
    SeckillGoodsService seckillGoodsService;

    @Autowired
    SeckillGoodsLocalCache localCache;

    @Autowired
    SeckillBizService seckillBizService;

//    @Autowired
//    SeckillBizService seckillBizService;


    /**
     * 查询当天的所有秒杀商品
     * @return
     */
    @GetMapping("/currentday/goods")
    public Result<List<SeckillGoods>> queryCurrentDaySeckillGoods(){
//        List<SeckillGoods> seckillGoods = seckillGoodsService.queryCurrentDaySeckillGoods();

        List<SeckillGoods> goods = localCache.getAllSeckillGoods();

        return Result.ok(goods);
    }


    @GetMapping("/detail/{skuId}")
    public Result<SeckillGoods> seckillGoodsDetail(@PathVariable Long skuId){
//        SeckillGoods goods = seckillGoodsService.getSeckillGoodsDetail(skuId);

        SeckillGoods goods = localCache.getDetailFromLocalCache(skuId);

        return Result.ok(goods);
    }

    @GetMapping("/orderinfo/{skuId}")
    public Result<OrderInfo> getOrderInfoBySkuId(@PathVariable("skuId") Long skuId){

        //查询当前这个商品的秒杀单数据
        OrderInfo orderInfo = seckillBizService.queryOrderInfo(skuId);
        return Result.ok(orderInfo);
    }


}
