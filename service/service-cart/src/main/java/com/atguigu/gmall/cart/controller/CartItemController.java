package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/cart")
public class CartItemController {

    @Autowired
    CartService cartService;


    /**
     * 获取购物车列表数据
     * @return
     */
    @GetMapping("/cartList")
    public Result cartList(){
        log.info("获取购物车列表");

        List<CartItem> cartItems = cartService.getCartItems();

        return Result.ok(cartItems);
    }

    /**
     * 修改购物车中某个商品的数量
     * /api/cart/addToCart/41/-1
     * @param skuId 商品id
     * @param num 数量: -1 减一个  1 加一个
     * @return
     */
    @PostMapping("/addToCart/{skuId}/{num}")
    public Result addToCart(@PathVariable Long skuId, @PathVariable Integer num){

        cartService.updateCartItemNum(skuId,num);

        return Result.ok();
    }



    /**
     *     ///api/cart/checkCart/41/0
     *     选中购物车中某个商品
     * @param skuId
     * @param checked  0: 未选中  1: 选中
     * @return
     */
    @GetMapping("/checkCart/{skuId}/{checked}")
    public Result checkCart(@PathVariable Long skuId, @PathVariable Integer checked){
        cartService.updateCartItemCheckedStatus(skuId,checked);

        return Result.ok();
    }

    /**
     * /api/cart/deleteCart/41
     * @param skuId
     * @return
     */
    @DeleteMapping("/deleteCart/{skuId}")
    public Result deleteCartItem(@PathVariable Long skuId){

        cartService.deleteCartItem(skuId);
        return Result.ok();
    }
}
