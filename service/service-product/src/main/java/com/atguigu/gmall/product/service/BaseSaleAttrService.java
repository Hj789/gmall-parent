package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 *
 */
public interface BaseSaleAttrService extends IService<BaseSaleAttr> {

    List<SpuSaleAttr> getSpuSaleAttrAndValue(Long spuId);
}
