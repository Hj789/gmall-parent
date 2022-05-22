package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.atguigu.gmall.product.dto.ValueJsonDto;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.product.service.SkuSaleAttrValueService;
import com.atguigu.gmall.product.mapper.SkuSaleAttrValueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Service
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueMapper, SkuSaleAttrValue> implements SkuSaleAttrValueService{

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Override
    public Map<String, String> getSkuValueJson(Long skuId) {
        Map<String, String> map = new HashMap<>();
        //去数据库查询我和兄弟们的sku销售属性组合信息
        List<ValueJsonDto> dtos = skuSaleAttrValueMapper.getSkuValueJson(skuId);
        for (ValueJsonDto dto : dtos) {
            map.put(dto.getValueJson(), dto.getId().toString());
        }
        return map;
    }
}




