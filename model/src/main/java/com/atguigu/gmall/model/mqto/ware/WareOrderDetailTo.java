package com.atguigu.gmall.model.mqto.ware;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WareOrderDetailTo {

    private Long skuId;
    private Integer skuNum;
    private String skuName;
}
