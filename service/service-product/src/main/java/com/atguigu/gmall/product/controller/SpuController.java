package com.atguigu.gmall.product.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.service.SpuInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/admin/product")
@RestController
public class SpuController {

    @Autowired
    SpuInfoService spuInfoService;

    /**
     * 分页查询Spu信息
     * /admin/product/{page}/{limit}?category3Id=61
     */
    @GetMapping("/{page}/{limit}")
    public Result getSupInfoPage(@PathVariable Long page,
                                 @PathVariable Long limit,
                                 @RequestParam("category3Id") Long c3Id){

        Page<SpuInfo> infoPage = new Page<>(page, limit);
        QueryWrapper<SpuInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category3_id",c3Id);
        Page<SpuInfo> result = spuInfoService.page(infoPage,queryWrapper);
        return Result.ok(result);
    }

    /**
     * 添加spui信息
     * /admin/product/saveSpuInfo
     */
    @PostMapping("/saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){
        spuInfoService.saveSpuInfo(spuInfo);
        return Result.ok();
    }





}
