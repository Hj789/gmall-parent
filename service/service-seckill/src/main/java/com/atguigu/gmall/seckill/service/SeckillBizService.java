package com.atguigu.gmall.seckill.service;

import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.model.order.OrderInfo;

public interface SeckillBizService {


    /**
     * 为一个商品生成秒杀码
     * @param skuId
     * @return
     */
    String generateSeckillCode(Long skuId);


    /**
     * 校验一个商品秒杀码
     * @param skuId
     * @param code
     * @return
     */
    boolean checkSeckillCode(Long skuId,String code);

    /**
     * ajax秒杀下单
     * @param skuId
     * @param skuIdStr
     */
    void ajaxSeckillOrder(Long skuId, String skuIdStr);

    /**
     * 检查秒杀订单当前的状态
     * @param skuId
     * @return
     */
    ResultCodeEnum checkSeckillOrder(Long skuId);

    /**
     * 查询一个商品的秒杀单数据
     * @param skuId
     * @return
     */
    OrderInfo queryOrderInfo(Long skuId);
}
