package com.atguigu.gmall.feign.item;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.to.SkuDetailTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("service-item")
@RequestMapping("/rpc/inner/item")
public interface ItemFeignClient {


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
    Result<SkuDetailTo> getSkuDetail(@PathVariable Long skuId);
}
