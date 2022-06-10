package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.constants.RedisConst;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.AuthUtil;
import com.atguigu.gmall.common.util.JSONs;
import com.atguigu.gmall.feign.product.ProductFeignClient;
import com.atguigu.gmall.model.cart.CartItem;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.to.UserAuthTo;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignClient productFeignClient;

    @Qualifier("otherPool")
    @Autowired
    ThreadPoolExecutor otherPool;

    @Override
    public CartItem addSkuToCart(Long skuId, Integer skuNum) {
        //1. 决定使用哪个购物车键
        // user:cart:xxx
        String cartKey = determinCartKey();

        //异常机制
        validateCartOverflow(cartKey);

        //2、保存这个商品
        CartItem cartItem = saveSkuToCart(skuId, skuNum, cartKey);

        //3、过期时间
        if (AuthUtil.getUserAuth().getUserId() == null) {
            //说明用户没登录，设置过期时间。
            setCartTimeout(cartKey);
        }

        return cartItem;
    }

    @Override
    public String determinCartKey() {

        String prefix = RedisConst.CART_KEY_PREFIX;

        //获取用户信息
        UserAuthTo userAuth = AuthUtil.getUserAuth();
        if (userAuth.getUserId() != null){
            //用户登录了
            return prefix+userAuth.getUserId();
        }else {
            return prefix+userAuth.getUserTempId();
        }

    }

    /**
     * 把商品存到购物车
     * @param skuId
     * @param num
     * @param cartKey
     * @return
     */
    @Override
    public CartItem saveSkuToCart(Long skuId, Integer num, String cartKey) {

        //key：String   value： Hash（String、String）
        //1、绑定一个指定购物车的操作
        BoundHashOperations<String, String, String> cart = redisTemplate.boundHashOps(cartKey);


        //2、把skuId存到购物车
        //2.1、如果这个没存过，就是新增
        Boolean hasKey = cart.hasKey(skuId.toString()); //判断cartKey这个购物车中有没有 skuId 这个商品
        if (!hasKey) {
            //TODO 1）、远程调用product服务，找到这个商品的详细信息
            Result<SkuInfo> skuInfo = productFeignClient.getSkuInfo(skuId);

            //TODO 2）、制作一个 CartItem
            CartItem cartItem = convertSkuInfoToCartItem(skuInfo.getData());
            cartItem.setSkuNum(num);


            //TODO 3）、并转为json，存到redis
            String json = JSONs.toStr(cartItem);
            cart.put(skuId.toString(), json);

            return cartItem;
        } else {
            //2.2、如果这个存过，只是数量的增加
            String json = cart.get(skuId.toString());
            CartItem item = JSONs.strToObj(json, new TypeReference<CartItem>() {
            });
            //设置新数量
            item.setSkuNum(item.getSkuNum() + num);
            //更新数据
            cart.put(skuId.toString(), JSONs.toStr(item));

            return item;
        }
    }

    @Override
    public List<CartItem> getCartItems() {
        BoundHashOperations<String, String, String> userCart = getUserCart();  //190
        BoundHashOperations<String, String, String> tempCart = getTempCart();  //190


        //1、判断是否需要合并购物车（UserId，UserTempId）
        if (userCart != null && tempCart != null && tempCart.size() > 0) {
            //直接判断，超出就不用合并，返回错误
            if((userCart.size() + tempCart.size())>=RedisConst.CART_SIZE_LIMIT){
                throw new GmallException(ResultCodeEnum.CART_MERGE_OVERFLOW);
            }
            //3、如果需要合并，拿到临时购物车键
            String tempCartKey = getTempCartKey();
            //拿到临时购物车的所有商品数据
            List<CartItem> tempItems = getItems(tempCartKey); //47-2

            String userCartKey = determinCartKey();

            //遍历临时购物车的所有数据，添加到用户购物车
            tempItems.stream().forEach(cartItem -> {
                //给用户购物车新增
                saveSkuToCart(cartItem.getSkuId(), cartItem.getSkuNum(), userCartKey);
            });

            //4、删除临时购物车
            deleteCart(tempCartKey);


            //5、返回合并后的所有数据
            List<CartItem> cartItems = getItems(userCartKey);

            //6、更新一下价格
            updatePriceBatch(userCartKey);

            return cartItems;
        } else {
            //2、如果不需要合并【没登录 UserTempId】 【登录了，但是临时购物车没东西】 【登录了，临时购物车被合并过了】
            //1、得到购物车的键
            String cartKey = determinCartKey();
            //2、获取这个购物车中的商品
            List<CartItem> cartItems = getItems(cartKey);

            //提交给线程池  8
            updatePriceBatch(cartKey);

            return cartItems;
        }

    }

    //用户不能立即看到数据库改变，但是下次能看到最新价格。
    public void updatePriceBatch(String cartKey) {
        otherPool.submit(()->{
            List<CartItem> items = getItems(cartKey);
            items.stream().forEach(cartItem -> {
                //1、查价
                Result<BigDecimal> skuPrice = productFeignClient.getPrice(cartItem.getSkuId());
                //2、改redis的价格
                updateCartItemPrice(cartKey,cartItem.getSkuId(),skuPrice.getData());
            });
        },otherPool);
    }

    @Override
    public List<CartItem> getCheckList() {
        //1.获取购物车所有商品
        List<CartItem> cartItems = getCartItems();

        //2. 过滤选中的
        List<CartItem> items = cartItems.stream()
                .filter(cartItem -> cartItem.getIsChecked() == 1)
                .collect(Collectors.toList());


        return items;
    }


    private String getUserCartKey() {
        UserAuthTo auth = AuthUtil.getUserAuth();
        Long userId = auth.getUserId();
        if (userId != null) {
            String cartKey = RedisConst.CART_KEY_PREFIX + userId;
            return cartKey;
        }
        return null;
    }


    private String getTempCartKey() {
        UserAuthTo auth = AuthUtil.getUserAuth();
        String tempId = auth.getUserTempId();
        if (!StringUtils.isEmpty(tempId)) {
            String cartKey = RedisConst.CART_KEY_PREFIX + tempId;
            return cartKey;
        }
        return null;
    }


    private BoundHashOperations<String, String, String> getUserCart() {
        UserAuthTo auth = AuthUtil.getUserAuth();
        Long userId = auth.getUserId();
        if (userId != null) {
            String cartKey = RedisConst.CART_KEY_PREFIX + userId;
            return redisTemplate.boundHashOps(cartKey);
        }
        return null;
    }

    private BoundHashOperations<String, String, String> getTempCart() {
        UserAuthTo auth = AuthUtil.getUserAuth();
        String tempId = auth.getUserTempId();
        if (!StringUtils.isEmpty(tempId)) {
            String cartKey = RedisConst.CART_KEY_PREFIX + tempId;
            return redisTemplate.boundHashOps(cartKey);
        }
        return null;
    }



    @Override
    public void updateCartItemNum(Long skuId, Integer num) {
        //0、拿到购物车的键
        String cartKey = determinCartKey();
        //1、拿到购物车
        BoundHashOperations<String, String, String> cart = redisTemplate.boundHashOps(cartKey);

        //2、拿到一个商品
        CartItem cartItem = getCartItem(cartKey, skuId);

        //3、修改数量
        if (num.equals(1) || num.equals(-1)) {
            cartItem.setSkuNum(cartItem.getSkuNum() + num);
        } else {
            cartItem.setSkuNum(num);
        }
        cartItem.setUpdateTime(new Date());

        //4、修改远程的数据
        cart.put(skuId.toString(), JSONs.toStr(cartItem));
    }

    @Override
    public void updateCartItemCheckedStatus(Long skuId, Integer checked) {
        String cartKey = determinCartKey();
        //1.先拿到购物车中这个商品
        CartItem cartItem = getCartItem(cartKey, skuId);
        cartItem.setIsChecked(checked);

        //不用修改时间
        saveCartItem(cartKey,cartItem);


    }

    @Override
    public void deleteCartItem(Long skuId) {
        String cartKey = determinCartKey();
        //删除购物车中的这个商品
        deleteItem(cartKey,skuId);
    }

    @Override
    public void deleteChecked() {
        String cartKey = determinCartKey();
        //1、拿到当前购物车中所有商品
        List<CartItem> cartItems = getCartItems();

        //2、只要选中的商品，然后删除。可变参数是一个数组
        Object[] objects = cartItems.stream()
                .filter((item) -> item.getIsChecked() == 1)
                .map(item -> item.getSkuId().toString())
                .toArray();
        //3、删除
        if (objects != null && objects.length > 0) {
            deleteItem(cartKey, objects);
        }


    }

    @Override
    public void deleteCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    @Override
    public void setCartTimeout(String cartKey) {

        //1、如果这个购物车已经设置了过期时间，就不用设置了，没有就设置
        //设置过期时间即可； 如果不做上面判断，还拥有自动延期特效。（防止边界时间点用户用了，结果没有了）
        redisTemplate.expire(cartKey, RedisConst.TEMP_CART_TIMEOUT);
    }

    @Override
    public void validateCartOverflow(String cartKey) {
        Long size = redisTemplate.boundHashOps(cartKey).size();
        if (size >= RedisConst.CART_SIZE_LIMIT) {
            throw  new GmallException(ResultCodeEnum.CART_OVERFLOW);
        }
    }

    @Override
    public void updateCartItemPrice(String cartKey, Long skuId, BigDecimal price) {
        //1、拿到远程redis购物车中这个商品
        CartItem cartItem = getCartItem(cartKey, skuId);
        //2、修改价格
        cartItem.setSkuPrice(price);
        //3、重新存进去
        saveCartItem(cartKey,cartItem);
    }

    private void deleteItem(String cartKey, Object[] skuIds) {
        BoundHashOperations<String, String, String> cart = redisTemplate.boundHashOps(cartKey);
        cart.delete(skuIds);
    }

    private void deleteItem(String cartKey, Long skuId) {
        BoundHashOperations<String, String, String> cart = redisTemplate.boundHashOps(cartKey);
        cart.delete(skuId.toString());
    }

    // 给redis购物车存一个数据 : 新增,覆盖修改 二合一的功能
    private void saveCartItem(String cartKey, CartItem cartItem) {
        BoundHashOperations<String, String, String> cart = redisTemplate.boundHashOps(cartKey);
        cart.put(cartItem.getSkuId().toString(),JSONs.toStr(cartItem));
    }

    private List<CartItem> getItems(String cartKey) {
        //1、拿到购物车
        BoundHashOperations<String, String, String> cart = redisTemplate.boundHashOps(cartKey);

        //2、获取所有商品
        List<String> values = cart.values();

        //R apply(T t);
        List<CartItem> collect = values.stream()
                .map((jsonStr) -> JSONs.strToObj(jsonStr, new TypeReference<CartItem>() {
                }))
                .sorted((o1, o2) -> o2.getUpdateTime().compareTo(o1.getUpdateTime()))
                .collect(Collectors.toList());


        return collect;
    }


    private CartItem convertSkuInfoToCartItem(SkuInfo data) {
        UserAuthTo userAuth = AuthUtil.getUserAuth();
        CartItem cartItem = new CartItem();

        cartItem.setId(data.getId());

        if (userAuth.getUserId() != null) {
            cartItem.setUserId(userAuth.getUserId().toString());
        } else {
            cartItem.setUserId(userAuth.getUserTempId());
        }

        cartItem.setSkuId(data.getId());
        cartItem.setSkuNum(0);
        cartItem.setSkuDefaultImg(data.getSkuDefaultImg());
        cartItem.setSkuName(data.getSkuName());
        cartItem.setIsChecked(1);
        cartItem.setCreateTime(new Date());
        cartItem.setUpdateTime(new Date());

        //第一次放进购物车的价格
        cartItem.setCartPrice(data.getPrice());
        //实时价格
        cartItem.setSkuPrice(data.getPrice());
        return cartItem;
    }

    private CartItem getCartItem(String cartKey, Long skuId) {
        BoundHashOperations<String, String, String> cart = redisTemplate.boundHashOps(cartKey);

        String json = cart.get(skuId.toString());

        return JSONs.strToObj(json, new TypeReference<CartItem>() {
        });
    }
}
