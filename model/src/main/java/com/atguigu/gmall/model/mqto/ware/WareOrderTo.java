package com.atguigu.gmall.model.mqto.ware;

import lombok.Data;

import java.util.List;

@Data
public class WareOrderTo {


    private Long orderId;
    private String consignee;
    private String consigneeTel;
    private String orderComment;
    private String orderBody;
    private String deliveryAddress;
    private String paymentWay = "2";
    private List<WareOrderDetailTo> details;

    private String wareId; //仓库编号
}
