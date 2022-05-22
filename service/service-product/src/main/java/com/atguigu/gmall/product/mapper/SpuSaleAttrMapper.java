package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.atguigu.gmall.product.domain.SpuSaleAttr
 */
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {
    /**
     * 查询指定的sku对应的spu对应的销售属性名和值
     * @param skuId
     * @return
     */
    List<SpuSaleAttr> getSkudeSpuSaleAttrAndValue(@Param("skuId") Long skuId);
}




