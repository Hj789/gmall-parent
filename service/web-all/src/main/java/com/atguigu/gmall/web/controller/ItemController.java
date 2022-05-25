package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.feign.item.ItemFeignClient;
import com.atguigu.gmall.feign.product.ProductFeignClient;
import com.atguigu.gmall.model.to.SkuDetailTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@Slf4j
@Controller
public class ItemController {

    @Autowired
    ItemFeignClient itemFeignClient;

    @Autowired
    ProductFeignClient productFeignClient;

    /**
     * 商品详情服务:
     * 查询sku详情得做这么多事
     * 1. 差分类
     * 2. 查sku信息
     * 3. 查价格
     * 4. 查所有销售属性组合
     * 5. 查实际sku组合
     * 6. 查介绍(不用管)
     */
    @GetMapping("/{skuId}.html")
    public String itemPage(@PathVariable Long skuId, Model model){
        log.info("商品详情:{}",skuId);
        //RPC查询数据
        Result<SkuDetailTo> skuDetail = itemFeignClient.getSkuDetail(skuId);
        if (skuDetail.isOk()){
            SkuDetailTo detailData = skuDetail.getData();
            //1. 当前sku所在的分类的完整信息;
            //category1Id,category1Name
            //category2Id,category2Name
            //category3Id,category3Name
            model.addAttribute("categoryView",detailData.getCategoryView());

            //2. 当前sku信息
            //skuId,skuName,skuDefaultImg,skuImageList
            model.addAttribute("skuInfo",detailData.getSkuInfo());


            //${spuSaleAttrList}; 定义的所有版本
            //4. sku的spu所有销售属性集合
            model.addAttribute("spuSaleAttrList",detailData.getSupSaleAttrList());


            //${valueSkuJson}: 所有实际存在的sku组合
            //5. 当前spu可用的所有sku销售属性组合
            model.addAttribute("valuesSkuJson",detailData.getValuesSkuJson());
        }


        Result<BigDecimal> skuPrice = productFeignClient.getPrice(skuId);
        if (skuPrice.isOk()){
            //3. sku的价格
            model.addAttribute("price",skuPrice.getData());
        }


        return "item/index";
    }
}
