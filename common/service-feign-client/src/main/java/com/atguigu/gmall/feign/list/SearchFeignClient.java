package com.atguigu.gmall.feign.list;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.vo.GoodsSearchResultVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/rpc/inner/es")
@FeignClient("service-list")
public interface SearchFeignClient {

    /**
     * 保存商品数据到es
     * @param goods
     * @return
     */
    @PostMapping("/goods/save")
    Result saveGoods(@RequestBody Goods goods);

    @DeleteMapping("/goods/delete/{skuId}")
    Result deleteGoods(@PathVariable Long skuId);

    /**
     * 远程检索商品
     * @param param
     * @return
     */
    @PostMapping("/goods/search")
    Result<GoodsSearchResultVo> searchGoods(@RequestBody SearchParam param);

}
