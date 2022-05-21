package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
