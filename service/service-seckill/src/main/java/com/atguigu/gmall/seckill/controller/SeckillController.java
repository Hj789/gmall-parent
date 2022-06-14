package com.atguigu.gmall.seckill.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.seckill.service.SeckillBizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/activity/seckill")
@RestController
public class SeckillController {



    @Autowired
    SeckillBizService seckillBizService;
    /**
     * 获取秒杀码； 生成一个秒杀码
     * @param skuId
     * @return
     */
    @GetMapping("/auth/getSeckillSkuIdStr/{skuId}")
    public Result seckillCode(@PathVariable("skuId") Long skuId){


        //生成一个商品的秒杀码; uuid。限流，防非法，防各种操作
        String code = seckillBizService.generateSeckillCode(skuId);

        /**
         * 秒杀为了挡住一些非法请求。
         * 1）、秒杀下单。 /seckill/order/47
         *     脚本： /seckill/order/88
         * 为了安全期间，隐藏秒杀连接
         *      /seckill/order/uuid
         *     脚本： /seckill/order/4328490238908894382JDKL
         *     1）真正到了秒杀那一刻，给我发送了请求获取到秒杀码了。4328490238908894382JDKL
         *     2）前端才能得到一个真正的秒杀地址。 /seckill/order/4328490238908894382JDKL
         *     3）给这个地址提交一个秒杀请求，才给你转到秒杀地址抢单
         *
         */
        return Result.ok(code);
        //前端会自己跳转到 秒杀排队页 /seckill/queue.html?skuId=46&skuIdStr=bf04b3e613af0f28e363641999acf3f0
    }


    /**
     * 秒杀下单
     * @param skuId
     * @param skuIdStr
     * @return
     */
    @PostMapping("/auth/seckillOrder/{skuId}")
    public Result seckillOrder(@PathVariable("skuId") Long skuId,
                               @RequestParam("skuIdStr") String skuIdStr){


        //能下单就返回 ok();
        seckillBizService.ajaxSeckillOrder(skuId,skuIdStr);

        //不能下单就返回 fail();
        return Result.ok();
    }


    /**
     * 检查当前用户这个秒杀商品的结果怎么样了
     * 前端每3s就发这个请求检查
     * @param skuId
     * @return
     */
    @GetMapping("/auth/checkOrder/{skuId}")
    public Result checkSeckillOrder(@PathVariable("skuId") Long skuId){


        //检查订单状态
        ResultCodeEnum resultCodeEnum = seckillBizService.checkSeckillOrder(skuId);

        return Result.build("",resultCodeEnum);
    }


    /**
     * 提交保存秒杀订单；
     * 1、订单数据库要保存订单
     * 2、redis也要更新这个订单信息
     * 3、设计好接下来的所有流程
     *      秒杀订单的流程和普通订单流程；普通订单支付完成后，通知库存系统减库存。
     *      秒杀单已经提前减了。不需要扣库存。
     */
    //http://api.gmall.com/api/activity/seckill/auth/submitOrder
    //TODO 自己做



}