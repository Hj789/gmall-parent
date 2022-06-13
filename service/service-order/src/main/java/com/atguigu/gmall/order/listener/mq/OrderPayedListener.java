package com.atguigu.gmall.order.listener.mq;



import com.atguigu.gmall.common.constants.MqConst;
import com.atguigu.gmall.common.util.JSONs;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.mqto.order.PayNotifySuccessVo;
import com.atguigu.gmall.model.mqto.ware.WareOrderDetailTo;
import com.atguigu.gmall.model.mqto.ware.WareOrderTo;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class OrderPayedListener {


    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    OrderInfoService orderInfoService;

    /**
     * 监听支付成功单队列
     */
    @RabbitListener(queues = MqConst.ORDER_PAYED_QUEUE)
    public void listenPayedOrder(Message message, Channel channel) {
        try {

            if (message.getMessageProperties().getRedelivered()) {
                //这个消息是否是被重新派发.
                //1、什么都不用做。
                //2、推荐用业务的幂等性来解决消息重复问题；

            }

            //执行业务
            PayNotifySuccessVo obj = JSONs.strToObj(new String(message.getBody()), new TypeReference<PayNotifySuccessVo>() {
            });
            log.info("监听到成功支付的订单：{}", obj.getOut_trade_no());

            //修改订单为已支付状态
            //String outTradeNo,
            // long userId,
            // String processStatus,
            // String orderStatus
            Long userId = Long.parseLong(obj.getOut_trade_no().split("-")[2]);
            if ("TRADE_SUCCESS".equals(obj.getTrade_status())) {
                ProcessStatus paid = ProcessStatus.PAID;
                log.info("修改订单状态为已支付：{}", obj.getOut_trade_no());
                orderInfoService.updateOrderStatusToPaid(obj.getOut_trade_no(), userId, paid.name(), paid.getOrderStatus().name());
            }

            //2、通知库存服务
            WareOrderTo wareOrderTo = prepareWareOrder(obj);
            rabbitTemplate.convertAndSend(MqConst.WARE_EVENT_EXCHANGE, MqConst.RK_WARE_STOCK, JSONs.toStr(wareOrderTo));

            //回复消费成功
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {

        } finally {

        }


    }

    private WareOrderTo prepareWareOrder(PayNotifySuccessVo obj) {
        String outTradeNo = obj.getOut_trade_no();
        //1、获取当前用户对应的outTradeNo 的订单详情
        List<OrderInfo> orderInfos = orderInfoService.getOrderAndDetailByOutTradeNo(outTradeNo);

        OrderInfo info = orderInfos.get(0);


        //2、封装成MQ的to
        WareOrderTo orderTo = new WareOrderTo();

        orderTo.setOrderId(info.getId());
        orderTo.setConsignee(info.getConsignee());
        orderTo.setConsigneeTel(info.getConsigneeTel());
        orderTo.setOrderComment(info.getOrderComment());
        orderTo.setOrderBody(info.getTradeBody());
        orderTo.setDeliveryAddress(info.getDeliveryAddress());
        orderTo.setPaymentWay("2");



        //OrderDetail===  WareOrderDetailTo
        List<WareOrderDetailTo> list = info.getDetails().stream()
                .map(orderDetail -> {
                    WareOrderDetailTo detailTo = new WareOrderDetailTo();
                    detailTo.setSkuId(orderDetail.getSkuId());
                    detailTo.setSkuNum(orderDetail.getSkuNum());
                    detailTo.setSkuName(orderDetail.getSkuName());
                    return detailTo;
                }).collect(Collectors.toList());

        orderTo.setDetails(list);

        //3、返回
        return orderTo;
    }
}