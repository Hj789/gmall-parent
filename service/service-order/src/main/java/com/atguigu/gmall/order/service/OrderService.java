package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.mqto.ware.WareOrderTo;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.vo.order.OrderConfirmVo;
import com.atguigu.gmall.model.vo.order.OrderSpiltVo;
import com.atguigu.gmall.model.vo.order.OrderSubmitVo;

import java.util.List;

public interface OrderService {
    /**
     *
     * @return
     */
    OrderConfirmVo getOrderConfirmVData();

    /**
     * 生成一个 交易号 [防重令牌]
     * @return
     */
    String generateTradeNo();

    /**
     * 校验防重令牌
     * @return
     */
    boolean checkTradeNo(String token);

    /**
     * 提交订单
     * @param tradeNo
     * @param orderSubmitVo
     */
    Long submitOrder(String tradeNo, OrderSubmitVo orderSubmitVo);

    /**
     * 保存订单
     */
    Long saveOrder(OrderSubmitVo orderSubmitVo);

    /**
     * 订单创建完成后，发送消息
     * @param orderId
     */
    void sendOrderCreateMsg(Long orderId);

    /**
     * 获取一个订单信息
     * @param orderId
     * @return
     */
    OrderInfo getOrderInfoIdAndAmount(Long orderId);

    /**
     * 修改订单状态为已支付
     * @param outTradeNo
     */
    void updateOrderStatusToPAID(String outTradeNo);

    /**
     * 检查指定订单的状态,是否和支付宝中订单的支付状态一致,不一致则同步
     * @param outTradeNo
     */
    void checkAndSyncOrderStatus(String outTradeNo);

    /**
     * //订单服务按照 库存提供的 spilt组合信息，拆出子订单，返回给库存系统
     * @param spiltVo
     * @return
     */
    List<WareOrderTo> orderSpilt(OrderSpiltVo spiltVo);
}
