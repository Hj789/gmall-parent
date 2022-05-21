package com.atguigu.gmall.product.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/product")
public class SkuController {


    @Autowired
    SkuInfoService skuInfoService;

    /**
     * 获取sku分页信息
     * /admin/product/list/{page}/{limit}
     */
    @GetMapping("/list/{page}/{limit}")
    public Result getSkuPage(@PathVariable Long page,
                             @PathVariable Long limit){

        Page<SkuInfo> skuInfoPage = new Page<>(page, limit);
        Page<SkuInfo> result = skuInfoService.page(skuInfoPage);
        return Result.ok(result);
    }

    /**
     * 保存sku信息
     * @param skuInfo
     * @return
     */
    @PostMapping("/saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){
        skuInfoService.saveSkuInfo(skuInfo);

        return Result.ok();
    }

    /**
     * 商品上架
     * /onSale/{skuId}
     */
    @GetMapping("/onSale/{skuId}")
    public Result onSale(@PathVariable Long skuId){

        skuInfoService.upOrDownSku(skuId,1);

        return Result.ok();
    }

    /**
     * 商品下架
     * /cancelSale/{skuId}
     */
    @GetMapping("/cancelSale/{skuId}")
    public Result cancelSale(@PathVariable Long skuId){
        skuInfoService.upOrDownSku(skuId,0);

        return Result.ok();
    }
}
