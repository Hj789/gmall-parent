package com.atguigu.gmall.seckill.listener;

import java.util.Date;
import java.math.BigDecimal;

import com.atguigu.gmall.common.constants.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.feign.product.ProductFeignClient;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.product.SkuInfo;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.atguigu.gmall.model.activity.CouponInfo;


import com.atguigu.gmall.common.constants.MqConst;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.mqto.seckill.SeckillQueueTo;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.seckill.biz.SeckillGoodsLocalCache;
import com.atguigu.gmall.seckill.service.SeckillGoodsService;
import com.atguigu.gmall.starter.utils.JSONs;
import com.fasterxml.jackson.core.type.TypeReference;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.min;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 监听所有排队的秒杀单信息
 */
@Slf4j
@Component
public class SeckillOrderQueuedListener {



    @Autowired
    SeckillGoodsService seckillGoodsService;

    @Autowired
    SeckillGoodsLocalCache localCache;

    @Autowired
    ProductFeignClient feignClient;

    @Autowired
    StringRedisTemplate redisTemplate;
    /**
     * 监听 seckill-success-queue  排队成功的成功单队列
     */
    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(value = MqConst.SECKILL_SUCCESS_QUEUE,exclusive = "false",durable = "true",autoDelete = "false"),
                    exchange = @Exchange(value = MqConst.SECKILL_EVENT_EXCHANGE,durable = "true",autoDelete = "false",type = ExchangeTypes.TOPIC),
                    key = MqConst.RK_SECKILL_QUEUE
            )
    })
    public void listenSeckillQueueSuccess(Message message, Channel channel){
        String json = new String(message.getBody());

        SeckillQueueTo queueTo = JSONs.strToObj(json, new TypeReference<SeckillQueueTo>() {
        });

        try {
            //1、减库存并生成redis的临时单； 生成成功一切ok
            deduceGoodsStockAndGenerateTempOrder(queueTo);

        }catch (Exception e){
            //2、数据库改库存失败； 临时单就是 boom
            String tempOrderKey = RedisConst.SECKILL_ORDER_TEMP_CACHE + queueTo.getUserId() + ":" + queueTo.getDateStr() + ":" + queueTo.getSkuId();
            redisTemplate.opsForValue().set(tempOrderKey,"boom",2, TimeUnit.DAYS);
        }
        log.info("监听到排队成功的秒杀单信息：{}",json);


        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }catch (Exception e){

        }

    }


    @Transactional
    public void deduceGoodsStockAndGenerateTempOrder(SeckillQueueTo queueTo ){
        //1、扣减数据库中这个商品的秒杀库存。
        seckillGoodsService.deduceGoodsStock(queueTo.getSkuId());
        //2、改库存成功；redis存一下临时单数据；

        // seckill:order: 3:2021-10-11:49: {临时订单数据}
        OrderInfo orderInfo = prepareOrder(queueTo);

        //3、保存到redis
        String tempOrderKey = RedisConst.SECKILL_ORDER_TEMP_CACHE + queueTo.getUserId() + ":" + queueTo.getDateStr() + ":" + queueTo.getSkuId();
        redisTemplate.opsForValue().set(tempOrderKey,JSONs.toStr(orderInfo),2, TimeUnit.DAYS);

    }

    private OrderInfo prepareOrder(SeckillQueueTo queueTo) {

        Long skuId = queueTo.getSkuId();
        SeckillGoods detail = localCache.getDetailFromLocalCache(skuId);
        Long userId = queueTo.getUserId();
        String dateStr = queueTo.getDateStr();


        OrderInfo info = new OrderInfo();

        //订单的金额为商品的秒杀价
        info.setTotalAmount(detail.getCostPrice());
        ProcessStatus unpaid = ProcessStatus.UNPAID;
        info.setOrderStatus(unpaid.getOrderStatus().name());
        info.setUserId(userId);
        info.setPaymentWay("2");
        info.setOutTradeNo("ATGUIGU-"+System.currentTimeMillis()+"-"+userId+"-"+ UUID.randomUUID().toString().substring(0,5));
        info.setTradeBody(detail.getSkuName());
        info.setCreateTime(new Date());
        long min30 = System.currentTimeMillis() + 1000*60*30;
        info.setExpireTime(new Date(min30));
        info.setProcessStatus(unpaid.name());

        info.setParentOrderId(0L);
        info.setImgUrl(detail.getSkuDefaultImg());

        //TODO 造好详情
        Result<SkuInfo> skuInfo = feignClient.getSkuInfo(skuId);
        OrderDetail orderDetail = buildSkuInfoToOrderDetail(skuInfo.getData(),detail,queueTo);

        info.setOrderDetailList(Arrays.asList(orderDetail));


        //原始总额为商品的原件
        info.setOriginalTotalAmount(detail.getPrice());


        info.setFeightFee(new BigDecimal("0"));
        info.setOperateTime(new Date());




        return info;
    }

    /**
     *
     * @param data   商品原始数据
     * @param detail 商品秒杀的数据
     * @param queueTo 商品秒杀排队成功的信息
     * @return
     */
    private OrderDetail buildSkuInfoToOrderDetail(SkuInfo data, SeckillGoods detail, SeckillQueueTo queueTo) {
        OrderDetail orderDetail = new OrderDetail();

        orderDetail.setUserId(queueTo.getUserId());
        orderDetail.setSkuId(queueTo.getSkuId());
        orderDetail.setSkuName(detail.getSkuName());
        orderDetail.setImgUrl(detail.getSkuDefaultImg());

        //此时购买商品的价格
        orderDetail.setOrderPrice(detail.getCostPrice());
        orderDetail.setSkuNum(1);
        orderDetail.setCreateTime(new Date());
        orderDetail.setSplitTotalAmount(detail.getCostPrice());


        return orderDetail;
    }
}