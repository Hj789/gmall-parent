package com.atguigu.gmall.common.constants;

public class MqConst {
    public static final String ORDER_EVENT_EXCHANGE = "order-event-exchange";
    public static final String RK_ORDER_TIMEOUT = "order.timeout";
    public static final String ORDER_DELAY_QUEUE = "order-delay-queue";
    public static final String RK_ORDER_CREATE = "order.create";
    public static final String ORDER_DEAD_QUEUE = "order-dead-queue";
    public static final String RK_ORDER_PAYED = "order.payed";
    public static final String ORDER_PAYED_QUEUE = "order-payed-queue";

    public static final String WARE_EVENT_EXCHANGE = "exchange.direct.ware.stock";
    public static final String WARE_ORDER_EVENT_EXCHANGE = "exchange.direct.ware.order";

    public static final String RK_WARE_STOCK = "ware.stock";
    public static final String WARE_STOCK_RESULT_QUEUE = "queue.ware.order";
    public static final String ROUTING_WARE_ORDER = "ware.order";

    public static final String SECKILL_EVENT_EXCHANGE = "seckill-event-exchange";
    public static final String RK_SECKILL_QUEUE = "seckill.queued";
    public static final String SECKILL_SUCCESS_QUEUE = "seckill-success-queue";
}
