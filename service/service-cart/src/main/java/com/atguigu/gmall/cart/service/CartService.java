package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartItem;

import java.math.BigDecimal;
import java.util.List;

public interface CartService {
    /**
     * 把一个商品添加到购物车
     * @param skuId
     * @param skuNum
     * @return
     */
    CartItem addSkuToCart(Long skuId, Integer skuNum);

    /**
     * 决定用哪个购物车的键
     * @return
     */
    String determinCartKey();

    /**
     * 把商品保存到购物车
     * @param skuId
     * @param num
     * @param cartKey
     * @return
     */
    CartItem saveSkuToCart(Long skuId,Integer num,String cartKey);

    /**
     *获取购物车列表数据
     * @return
     */
    List<CartItem> getCartItems();

    /**
     *修改购物车中某个商品数量
     * @param skuId
     * @param num
     */
    void updateCartItemNum(Long skuId, Integer num);

    /**
     * 修改购物车中某个商品的勾选状态
     * @param skuId
     * @param checked
     */
    void updateCartItemCheckedStatus(Long skuId, Integer checked);

    /**
     * 删除购物车某个商品
     * @param skuId
     */
    void deleteCartItem(Long skuId);

    /**
     * 删除购物车中选中的商品
     */
    void deleteChecked();

    /**
     * 删除整个购物车
     * @param cartKey
     */
    void deleteCart(String cartKey);

    /**
     * 设置过期时间
     * @param cartKey
     */
    void setCartTimeout(String cartKey);

    /**
     *判断这个购物车是否溢出
     * @param cartKey
     */
    void validateCartOverflow(String cartKey);

    /**
     * 更新指定购物车中某个商品的价格
     * @param cartKey
     * @param skuId
     * @param price
     */
    void updateCartItemPrice(String cartKey, Long skuId, BigDecimal price);

    /**
     * 批量更新购物车中商品价格
     * @param cartKey
     */
    void updatePriceBatch(String cartKey);

    /**
     * 获取所有选中的商品列表
     * @return
     */
    List<CartItem> getCheckList();
}
