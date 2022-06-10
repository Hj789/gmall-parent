package com.atguigu.gmall.feign.cart;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartItem;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("service-cart")
@RequestMapping("/rpc/inner/cart")
public interface CartFeignClient {

    /**
     * 将商品添加到购物车
     * @param skuId  商品的id
     * @param skuNum 添加的数量
     * @return
     */
    @GetMapping("/add/{skuId}")
    Result<CartItem> addSkuToCart(@PathVariable("skuId") Long skuId,
                                  @RequestParam("skuNum") Integer skuNum);

    @GetMapping("/delete/checked")
    Result deleteCartChecked();

    /**
     * 获取所有选中的商品列表
     * @return
     */
    @GetMapping("/checked/list")
    Result<List<CartItem>> getCheckItem();

}