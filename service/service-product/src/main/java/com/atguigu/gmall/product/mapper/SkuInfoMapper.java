package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Entity com.atguigu.gmall.product.domain.SkuInfo
 */
public interface SkuInfoMapper extends BaseMapper<SkuInfo> {

    /**
     * 修改sku上下架状态
     * @param skuId
     * @param status
     */
    void updateSkuStatus(@Param("skuId") Long skuId, @Param("status") int status);

    /**
     * 查询sku价格
     * @param skuId
     * @return
     */
    BigDecimal getPrice(@Param("skuId") Long skuId);

    /**
     * 查询出所有的skuId
     * @return
     */
    List<Long> getSkuIds();
}




