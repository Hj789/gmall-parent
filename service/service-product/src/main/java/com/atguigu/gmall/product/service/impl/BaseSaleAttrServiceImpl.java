package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.mapper.SpuSaleAttrValueMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.product.service.BaseSaleAttrService;
import com.atguigu.gmall.product.mapper.BaseSaleAttrMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 */
@Service
public class BaseSaleAttrServiceImpl extends ServiceImpl<BaseSaleAttrMapper, BaseSaleAttr> implements BaseSaleAttrService{

    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrAndValue(Long spuId) {
        List<SpuSaleAttr> spuSaleAttrs = spuSaleAttrValueMapper.getSpuSaleAttrAndValue(spuId);
        return spuSaleAttrs;
    }
}




