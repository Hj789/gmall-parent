package com.atguigu.gmall.model.to;

import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class SkuDetailTo {

    //sku所属的三级分类详情
    BaseCategoryView categoryView;

    //sku基本信息
    SkuInfo skuInfo;

    //最新价格
    BigDecimal price;

    //spu所有销售属性值集合
    List<SpuSaleAttr> supSaleAttrList;

    //所以可用的销售属性集合
    String valuesSkuJson;



}
