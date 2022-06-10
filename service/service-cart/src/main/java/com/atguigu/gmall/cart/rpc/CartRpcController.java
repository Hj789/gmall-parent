package com.atguigu.gmall.cart.rpc;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthUtil;
import com.atguigu.gmall.model.cart.CartItem;
import com.atguigu.gmall.model.to.UserAuthTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/rpc/inner/cart")
public class CartRpcController {

    @Autowired
    CartService cartService;

    /**
     * 给购物车中添加一个商品
     * @param skuId
     * @param skuNum
     * @return
     */
    @GetMapping("/add/{skuId}")
    public Result<CartItem> addSkuToCart(@PathVariable("skuId") Long skuId,
                                         @RequestParam("skuNum") Integer skuNum,
                                         HttpServletRequest request){


        //1. 得知道这个用户登录了没?登录了用用户id,没登录用临时键


        CartItem cartItem = cartService.addSkuToCart(skuId,skuNum);
        return Result.ok(cartItem);
    }

    /**
     * 删除选中的商品
     * @return
     */
    @GetMapping("/delete/checked")
    public Result deleteCartChecked(){

        cartService.deleteChecked();

        return Result.ok();
    }

    /**
     * 获取所有选中的商品列表
     * @return
     */
    @GetMapping("/checked/list")
    public Result<List<CartItem>> getCheckItem(){

        List<CartItem> cartItems = cartService.getCheckList();
        return Result.ok(cartItems);
    }
}
