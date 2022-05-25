package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;

/**
 * 针对表【sku_info(库存单元表)】的数据库操作Service
 */
public interface SkuInfoService extends IService<SkuInfo> {

    /**
     * 保存前端提交的sku数据
     * @param skuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);

    /**
     * 商品上下架
     * @param skuId
     * @param status
     */
    void upOrDownSku(Long skuId, int status);

    /**
     * 查询商品价格
     * @param skuId
     * @return
     */
    BigDecimal getSkuPrice(Long skuId);

    /**
     * 查询指定的sku对应的spu对应的销售属性名和值
     * @param skuId
     * @return
     */
    List<SpuSaleAttr> getSkudeSpuSaleAttrAndValue(Long skuId);

    /**
     * 查询出所有的skuId
     * @return
     */
    List<Long> getAllSkuIds();

}
