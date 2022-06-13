package com.atguigu.gmall.order.listener.mq;

import com.atguigu.gmall.common.constants.MqConst;
import com.atguigu.gmall.common.util.JSONs;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.mqto.order.OrderStockResultTo;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 监听库存服务对某个订单进行扣库存的结果
 */
@Slf4j
@Service
public class OrderStockResultListener {


    @Autowired
    OrderInfoService orderInfoService;


    /**
     * 每个子单扣减无论成功失败都会有消息
     * @param message
     * @param channel
     * MqConst.WARE_STOCK_RESULT_QUEUE
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.WARE_STOCK_RESULT_QUEUE,durable = "true",exclusive = "false",autoDelete = "false"),
            exchange = @Exchange(value = MqConst.WARE_ORDER_EVENT_EXCHANGE,durable = "true",autoDelete = "false",type="direct"),
            key = MqConst.ROUTING_WARE_ORDER
    ))
    public void stockResultListen(Message message, Channel channel){
        try {

            //处理扣减结果
            OrderStockResultTo resultTo = JSONs.strToObj(new String(message.getBody()), new TypeReference<OrderStockResultTo>() {
            });
            log.info("正在处理订单的库存扣减结果:{}",resultTo);
            Long orderId = resultTo.getOrderId();
            String status = resultTo.getStatus();
            ProcessStatus newStatus = null;
            switch (status){
                case "OUT_OF_STOCK":
                    newStatus = ProcessStatus.STOCK_EXCEPTION;
                    break; //超卖了
                case "DEDUCTED":
                    newStatus = ProcessStatus.WAITING_DELEVER;
                    break; //扣减成功了
            }

            //修改订单的状态
            orderInfoService.updateStatusByOrderId(orderId,newStatus);

            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            log.error("处理订单的库存扣减结果异常：{}",e);
        }
    }

}