package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.feign.cart.CartFeignClient;
import com.atguigu.gmall.model.cart.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;


/**
 * 购物车
 */
@Controller
public class CartController {

//    public static Map<Thread,Object> threadMap = new HashMap<>();

//    public static ThreadLocal<HttpServletRequest> threadLocal = new ThreadLocal<>();

    @Autowired
    CartFeignClient cartFeignClient;

    /**
     * 购物车列表页
     * @return
     */
    @GetMapping("/cart.html")
    public String cartPage(){


        return "cart/index";
    }

    /**
     * http://cart.gmall.com/addCart.html?skuId=50&skuNum=1
     * 把一个商品添加到购物车
     */
    @GetMapping("/addCart.html")
    public String addCart(@RequestParam("skuId") Long skuId,
                          @RequestParam("skuNum") Integer skuNum,
                          Model model,
                          HttpServletRequest request){
//        threadLocal.set(request);

        String userId = request.getHeader("UserId");
        String userTempId = request.getHeader("UserTempId");

        Result<CartItem> result = cartFeignClient.addSkuToCart(skuId, skuNum);
        if (result.isOk()){
            model.addAttribute("skuInfo",result.getData());
            model.addAttribute("skuNum",result.getData().getSkuNum());
        }

        return "cart/addCart"; //商品添加成功的提示页
    }

}
