package com.atguigu.gmall.model.vo.order;

import lombok.Data;

import java.util.List;


/**
 * 仓库和商品的分布关系
 */
@Data
public class WareSkuVo {
    private String wareId;
    private List<String> skuIds;
}