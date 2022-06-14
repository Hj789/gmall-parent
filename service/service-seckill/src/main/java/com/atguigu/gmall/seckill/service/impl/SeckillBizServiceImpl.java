package com.atguigu.gmall.seckill.service.impl;


import com.atguigu.gmall.common.constants.MqConst;
import com.atguigu.gmall.common.constants.RedisConst;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.AuthUtil;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.common.util.JSONs;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.mqto.seckill.SeckillQueueTo;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.seckill.biz.SeckillGoodsLocalCache;
import com.atguigu.gmall.seckill.service.SeckillBizService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SeckillBizServiceImpl implements SeckillBizService {


    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    SeckillGoodsLocalCache localCache;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public String generateSeckillCode(Long skuId) {
        //1、拿到秒杀的商品
        SeckillGoods detail = localCache.getDetailFromLocalCache(skuId);
        Date current = new Date();
        //前置校验？
        // 1)、商品是否开始秒杀了
        if (!detail.getStartTime().before(current)) {
            //秒杀还没开始
            throw new GmallException(ResultCodeEnum.SECKILL_NO_START);
        }

        // 2)、商品是否秒杀结束了
        if (!detail.getEndTime().after(current)) {
            //秒杀结束了
            throw new GmallException(ResultCodeEnum.SECKILL_END);
        }

        // 3)、商品是否秒杀还有库存。 只需要判断本地缓存
        if (detail.getStockCount() <= 0) {
            //商品没库存了
            throw new GmallException(ResultCodeEnum.SECKILL_FINISH);
        }


        Long userId = AuthUtil.getUserAuth().getUserId();
        String date = DateUtil.formatDate(new Date());

        //1、使用系统规则得到一个秒杀码
        String code = MD5.encrypt(date + skuId.toString() + userId.toString());

        //固定规则码？
        //  MD5(dateStr+skuId+userId)=秒杀码； 同一个用户对于同一个商品在当天使用的秒杀码都是一样的。固定防重

        //2、redis也缓存这个秒杀码
        String codeKey = RedisConst.SECKILL_CODE_CACHE_PREFIX + code;
        redisTemplate.opsForValue().set(codeKey, "1", 1, TimeUnit.DAYS);


        //3、以后秒这个商品，就必须带上这个码.
        return code;
    }

    /**
     * 校验秒杀码
     *
     * @param skuId
     * @param code
     * @return
     */
    @Override
    public boolean checkSeckillCode(Long skuId, String code) {
        //1、直接校验
        Long userId = AuthUtil.getUserAuth().getUserId();
        String date = DateUtil.formatDate(new Date());
        String sysCode = MD5.encrypt(date + skuId.toString() + userId.toString());


        String codeKey = RedisConst.SECKILL_CODE_CACHE_PREFIX + code;
        //这个秒杀码是对的
        if (sysCode.equals(code) && redisTemplate.hasKey(codeKey)) {
            return true;
        }
        return false;
    }


    @Override
    public void ajaxSeckillOrder(Long skuId, String skuIdStr) {
        Long userId = AuthUtil.getUserAuth().getUserId();
        //能来到这说明之前都生成了秒杀码，前置校验通过。在这里开始为用户生成秒杀订单即可
        SeckillGoods detail = localCache.getDetailFromLocalCache(skuId);

        //1、前置校验，继续校验
        Date current = new Date();
        //前置校验？
        // 1)、商品是否开始秒杀了
        if (!detail.getStartTime().before(current)) {
            //秒杀还没开始
            throw new GmallException(ResultCodeEnum.SECKILL_NO_START);
        }

        // 2)、商品是否秒杀结束了
        if (!detail.getEndTime().after(current)) {
            //秒杀结束了
            throw new GmallException(ResultCodeEnum.SECKILL_END);
        }

        // 3)、商品是否秒杀还有库存。 只需要判断本地缓存
        if (detail.getStockCount() <= 0) {
            //商品没库存了
            throw new GmallException(ResultCodeEnum.SECKILL_FINISH);
        }


        //2、校验秒杀码
        boolean check = checkSeckillCode(skuId, skuIdStr);
        if (!check) {
            //假的秒杀码
            throw new GmallException(ResultCodeEnum.SECKILL_ILLEGAL);
        }


        //2、是否是重复排队。 默认是1，
        Long increment = redisTemplate.opsForValue().increment(RedisConst.SECKILL_CODE_CACHE_PREFIX + skuIdStr);
        if (increment > 2) {
            //说明重复排队进来了；直接结束，不用往下走
            log.info("当前用户：【{}】 秒杀【{}】商品的请求已经发过了:", userId, skuId);
            return;
        }


        //3、本地缓存先扣一下； 本地说有，redis不一定有库存，但是本地说没有，redis就一定没有这个库存
        Integer localStock = detail.getStockCount() - 1;
        detail.setStockCount(localStock);  //10
        if (localStock < 0) {
            //本地库存量都不能减了。redis远程更不用减了
            throw new GmallException(ResultCodeEnum.SECKILL_FINISH);
        }


        //3、下秒杀单。远程扣库存信号量。
        String skuSemaphoreKey = RedisConst.SECKILL_GOODS_CACHE_PREFIX + DateUtil.formatDate(new Date()) + ":" + skuId;
        //redis中保存的sku的数量
        RSemaphore semaphore = redissonClient.getSemaphore(skuSemaphoreKey);
        //去redis原子 -1； 最多减到0就不减了
        boolean acquire = semaphore.tryAcquire();
        if (acquire) {
            //扣信号量成功，说明我们可以买这个商品； 给秒杀服务就发一个排队成功的消息
            SeckillQueueTo queue = new SeckillQueueTo(userId, skuId, skuIdStr, DateUtil.formatDate(new Date()));
            rabbitTemplate.convertAndSend(MqConst.SECKILL_EVENT_EXCHANGE, MqConst.RK_SECKILL_QUEUE, JSONs.toStr(queue));
        } else {
            //redis中这个商品的信号量都已经没有了，不用买了
            throw new GmallException(ResultCodeEnum.SECKILL_FINISH);
        }


    }

    @Override
    public ResultCodeEnum checkSeckillOrder(Long skuId) {
        Long userId = AuthUtil.getUserAuth().getUserId();
        //1、去redis中拿到临时单
        String tempOrderKey = RedisConst.SECKILL_ORDER_TEMP_CACHE + userId + ":" + DateUtil.formatDate(new Date()) + ":" + skuId;
        String json = redisTemplate.opsForValue().get(tempOrderKey);

        if (StringUtils.isEmpty(json)) {
            //说明redis中没有临时单，但是请求发过了（code码对应的次数>1）
            return ResultCodeEnum.SECKILL_RUN;
        } else {
            if ("boom".equals(json)) {
                //库存不足
                return ResultCodeEnum.SECKILL_FAIL;
            } else {
                OrderInfo info = JSONs.strToObj(json, new TypeReference<OrderInfo>() {
                });
                if (info.getId() == null) {
                    return ResultCodeEnum.SECKILL_SUCCESS;
                } else {
                    return ResultCodeEnum.SECKILL_ORDER_SUCCESS;
                }
            }
        }
    }

    @Override
    public OrderInfo queryOrderInfo(Long skuId) {
        //seckill:orders:3:2022-06-11:46
        Long userId = AuthUtil.getUserAuth().getUserId();
        String key = RedisConst.SECKILL_ORDER_TEMP_CACHE + userId + ":" + DateUtil.formatDate(new Date()) + ":" + skuId;
        String json = redisTemplate.opsForValue().get(key);
        if(!StringUtils.isEmpty(json) && !"boom".equals(json)){
            return JSONs.strToObj(json, new TypeReference<OrderInfo>() {
            });
        }
        return null;
    }
}
