package com.atguigu.gmall.model.vo.order;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@ToString
@Data
public class OrderSpiltVo {
    private Long orderId;  //当前订单

    //[{"wareId":"1","skuIds":["48"]},{"wareId":"2","skuIds":["49"]}]
    //[{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}]
    private String wareSkuMap;  //仓库组合
}