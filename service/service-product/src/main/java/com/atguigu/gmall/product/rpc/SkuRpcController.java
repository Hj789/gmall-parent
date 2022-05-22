package com.atguigu.gmall.product.rpc;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.SkuImageService;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.atguigu.gmall.product.service.SkuSaleAttrValueService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RequestMapping("/rpc/inner/product")
@RestController
public class SkuRpcController {

    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    SkuImageService skuImageService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    /**
     * 查询skuInfo信息
     * @param skuId
     * @return
     */
    @GetMapping("skuInfo/{skuId}")
    public Result<SkuInfo> getSkuInfo(@PathVariable Long skuId){
        //1. 查询skuInfo信息
        SkuInfo info = skuInfoService.getById(skuId);
        //2. 查询skuImageList
        List<SkuImage> skuImages = skuImageService.list(new QueryWrapper<SkuImage>().eq("sku_id", skuId));
        info.setSkuImageList(skuImages);
        return Result.ok(info);
    }

    /**
     * 查询商品价格
     * @param skuId
     * @return
     */
    @GetMapping("/skuInfo/price/{skuId}")
    public Result<BigDecimal> getPrice(@PathVariable Long skuId){
        BigDecimal price =  skuInfoService.getSkuPrice(skuId);
        return Result.ok(price);
    }

    /**
     * 查询指定的sku对应的spu对应的销售属性名和值
     */
    @GetMapping("skuInfo/spu/saleAttrAndValues/{skuId}")
    public Result<List<SpuSaleAttr>> getSkudeSpuSaleAttrAndValue(@PathVariable Long skuId){
        List<SpuSaleAttr> spuSaleAttrs = skuInfoService.getSkudeSpuSaleAttrAndValue(skuId);
        return Result.ok(spuSaleAttrs);
    }
    /**
     *
     */
    @GetMapping("skuInfo/valueJson/{skuId}")
    public Result<Map<String,String>> getSkuValueJson(@PathVariable Long skuId){
        Map<String,String> valueJson = skuSaleAttrValueService.getSkuValueJson(skuId);
        return Result.ok(valueJson);
    }
}
