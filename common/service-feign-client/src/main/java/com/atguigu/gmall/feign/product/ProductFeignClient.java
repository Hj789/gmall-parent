package com.atguigu.gmall.feign.product;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.to.CategoryAndChildTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@FeignClient("service-product")
@RequestMapping("/rpc/inner/product")
public interface ProductFeignClient {

    /**
     * 获取系统的所有分类以及自分类
     * @return
     */
    @GetMapping("/categorys")
    Result<List<CategoryAndChildTo>> getAllCategoryWithChilds();

    /**
     * 获取一个sku的分类层级信息
     * @param skuId
     * @return
     */
    @GetMapping("/category/view/{skuId}")
    Result<BaseCategoryView> getSkuCategoryView(@PathVariable Long skuId);



    /**
     * 查询skuInfo信息
     * @param skuId
     * @return
     */
    @GetMapping("skuInfo/{skuId}")
    Result<SkuInfo> getSkuInfo(@PathVariable Long skuId);


    /**
     * 查询商品价格
     * @param skuId
     * @return
     */
    @GetMapping("/skuInfo/price/{skuId}")
    Result<BigDecimal> getPrice(@PathVariable Long skuId);

    /**
     * 查询指定的sku对应的spu对应的销售属性名和值
     */
    /**
     * 查询指定的sku对应的spu对应的销售属性名和值
     */
    @GetMapping("skuInfo/spu/saleAttrAndValues/{skuId}")
    Result<List<SpuSaleAttr>> getSkudeSpuSaleAttrAndValue(@PathVariable Long skuId);

    /**
     * 查询skuValueJson数据
     * @param skuId
     * @return
     */
    @GetMapping("skuInfo/valueJson/{skuId}")
    Result<Map<String,String>> getSkuValueJson(@PathVariable Long skuId);
}