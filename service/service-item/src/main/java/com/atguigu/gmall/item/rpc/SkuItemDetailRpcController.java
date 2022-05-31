package com.atguigu.gmall.item.rpc;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.service.SkuDetailService;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.to.SkuDetailTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rpc/inner/item")
public class SkuItemDetailRpcController {

    @Autowired
    SkuDetailService skuDetailService;

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

    @GetMapping("/sku/detail/{skuId}")
    public Result<SkuDetailTo> getSkuDetail(@PathVariable Long skuId){
        //代理对象

        SkuDetailTo detailTo = skuDetailService.getSkuDetail(skuId);
        //增加商品热度  延迟更热度
        skuDetailService.incrHotScore(skuId);

        return Result.ok(detailTo);
    }

}
