package com.atguigu.gmall.model.mqto.seckill;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class SeckillQueueTo {
    private Long userId;
    private Long skuId;
    private String code;//秒杀码
    private String dateStr;//当天的时间


}