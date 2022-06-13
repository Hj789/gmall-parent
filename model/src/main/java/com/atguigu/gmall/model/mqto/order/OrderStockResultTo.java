package com.atguigu.gmall.model.mqto.order;

import lombok.Data;

@Data
public class OrderStockResultTo {

    private Long orderId;
    private String status;// ‘DEDUCTED’  (已减库存)、‘OUT_OF_STOCK’  (库存超卖,库存不足)
}