package com.atguigu.gmall.order.service.impl;
import java.math.BigDecimal;

import com.atguigu.gmall.common.constants.MqConst;
import com.atguigu.gmall.common.constants.RedisConst;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.AuthUtil;
import com.atguigu.gmall.common.util.JSONs;
import com.atguigu.gmall.feign.cart.CartFeignClient;
import com.atguigu.gmall.feign.product.ProductFeignClient;
import com.atguigu.gmall.feign.user.UserFeignClient;
import com.atguigu.gmall.feign.ware.WareFeignClient;
import com.atguigu.gmall.model.cart.CartItem;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.PaymentWay;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.mqto.OrderCreateTo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.to.UserAuthTo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.vo.order.CartItemForOrderVo;
import com.atguigu.gmall.model.vo.order.OrderConfirmVo;
import com.atguigu.gmall.model.vo.order.OrderSubmitVo;
import com.atguigu.gmall.order.service.OrderDetailService;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.atguigu.gmall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    CartFeignClient cartFeignClient;

    @Autowired
    ProductFeignClient productFeignClient;

    @Autowired
    UserFeignClient userFeignClient;

    @Autowired
    WareFeignClient wareFeignClient;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    OrderInfoService orderInfoService;

    @Autowired
    OrderDetailService orderDetailService;

    @Autowired
    ThreadPoolExecutor corePool;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public OrderConfirmVo getOrderConfirmVData() {

        OrderConfirmVo confirmVo = new OrderConfirmVo();

        Result<List<CartItem>> checkItem = cartFeignClient.getCheckItem();
        if (checkItem.isOk()) {
            List<CartItem> items = checkItem.getData();
            List<CartItemForOrderVo> vos = items.stream()
                    .map(cartItem -> {
                        CartItemForOrderVo vo = new CartItemForOrderVo();
                        vo.setImgUrl(cartItem.getSkuDefaultImg());
                        vo.setSkuName(cartItem.getSkuName());
                        //再查实时价格
                        Result<BigDecimal> price = productFeignClient.getPrice(cartItem.getSkuId());
                        vo.setOrderPrice(price.getData());
                        vo.setSkuNum(cartItem.getSkuNum());

                        //再实时查询下商品有货无货
                        String stock = wareFeignClient.hasStock(cartItem.getSkuId(), cartItem.getSkuNum());
                        vo.setStock(stock);

                        return vo;

                    }).collect(Collectors.toList());
            //设置所有选中的商品
            confirmVo.setDetailArrayList(vos);

            //计算价格和总量
            //计算总量
            Integer total = items.stream()
                    .map(CartItem::getSkuNum)  //数字 skuNum
                    .reduce((a, b) -> a + b)
                    .get();
            confirmVo.setTotalNum(total);

            //计算价格
            BigDecimal bigDecimal = vos.stream()
                    .map(i -> i.getOrderPrice().multiply(new BigDecimal(i.getSkuNum().toString())))
                    .reduce((a, b) -> a.add(b))
                    .get();
            confirmVo.setTotalAmount(bigDecimal);
        }
        //设置用户地址列表
        Result<List<UserAddress>> addressList = userFeignClient.getUserAddressList();
        confirmVo.setUserAddressList(addressList.getData());


        //设置 tradeNo； 防重令牌，给redis一个
        String tradeNo = generateTradeNo();
        //防重令牌，给页面一个
        confirmVo.setTradeNo(tradeNo);

        return confirmVo;
    }

    @Override
    public String generateTradeNo() {
        //1、生成防重令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        //2、保存到redis； 每个数据都应该有过期时间
        redisTemplate.opsForValue().set(RedisConst.NO_REPEAT_TOKEN + token, "1", 10, TimeUnit.MINUTES);
        return token;
    }

    @Override
    public boolean checkTradeNo(String token) {
        //1、原子验令牌+删令牌
        String script = "if redis.call('get', KEYS[1]) == '1' then return redis.call('del', KEYS[1]) else return 0 end";
        //2、执行
        Long result = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(RedisConst.NO_REPEAT_TOKEN + token), "1");
        return result == 1L;
    }

    @Override
    public Long submitOrder(String tradeNo, OrderSubmitVo orderSubmitVo) {
        //1、验令牌
        boolean no = checkTradeNo(tradeNo);
        if (!no) {
            throw new GmallException(ResultCodeEnum.REQ_ILLEGAL_TOKEN_ERROR);
        }

        //2、验价格；验总价。
        //2.1、前端提交来的所有商品的总价
        BigDecimal frontTotal = orderSubmitVo.getOrderDetailList().stream()
                .map(item -> item.getOrderPrice().multiply(new BigDecimal(item.getSkuNum().toString())))
                .reduce((a, b) -> a.add(b))
                .get();

        //2.2、购物车中这个选中商品的总价
        Result<List<CartItem>> checkItems = cartFeignClient.getCheckItem();
        BigDecimal backTotal = checkItems.getData().stream()
                .map(item -> {
                    Result<BigDecimal> skuPrice = productFeignClient.getPrice(item.getSkuId());
                    BigDecimal price = skuPrice.getData();
                    Integer skuNum = item.getSkuNum();
                    return price.multiply(new BigDecimal(skuNum.toString()));
                })
                .reduce((a, b) -> a.add(b))
                .get();
        //2.3、比对  -1, 0, or 1
        if (backTotal.compareTo(frontTotal) != 0) {
            throw new GmallException(ResultCodeEnum.ORDER_PRICE_CHANGE);
        }


        //3、验库存，提示精确
        List<String> noStock = new ArrayList<>();
        checkItems.getData().stream()
                .forEach(item -> {
                    String stock = wareFeignClient.hasStock(item.getSkuId(), item.getSkuNum());
                    if (!"1".equals(stock)) {
                        //没库存了
                        noStock.add("【" + item.getSkuName() + ": 没有库存】");
                    }
                });
        if (noStock.size() > 0) {
            String msg = noStock.stream()
                    .reduce((a, b) -> a + "，" + b)
                    .get();

            GmallException exception = new GmallException(msg, ResultCodeEnum.PRODUCT_NO_STOCK.getCode());
            throw exception;
        }


        //4、保存订单
        Long orderId = saveOrder(orderSubmitVo);

        //获取到老请求
        RequestAttributes oldReq = RequestContextHolder.getRequestAttributes();
        //5、删除购物车中选中商品。长事务。
        corePool.submit(() -> {
            //再给当前线程一放
            RequestContextHolder.setRequestAttributes(oldReq);
            log.info("正在准备删除购物车中选中的商品：");
            cartFeignClient.deleteCartChecked();
        });

        //6、30min以后关闭这个订单
//        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(5);
//
//        scheduledThreadPool.schedule(()->{
//            closeOrder(orderInfo);
//        },30,TimeUnit.MINUTES);

        //7、给 MQ 发送一个消息：表示某个订单创建成功了； orderId，userId。
        // 把他放到保存订单的事务环节了。
        //缺点：
        //  1)、MQ稳定性差会导致经常下单失败。


        return orderId;

    }

    @Override
    public void sendOrderCreateMsg(Long orderId) {
        Long userId = AuthUtil.getUserAuth().getUserId();

        OrderCreateTo orderCreateTo = new OrderCreateTo(orderId, userId);
        String json = JSONs.toStr(orderCreateTo);

        rabbitTemplate.convertAndSend(MqConst.ORDER_EVENT_EXCHANGE,
                MqConst.RK_ORDER_CREATE, json);

    }


    @Transactional
    @Override
    public Long saveOrder(OrderSubmitVo orderSubmitVo) {
        //1、将vo带来的数据，转成订单保存数据库中的数据模型
        //order_info_1：订单信息表。order_detail：订单详情表： order_detail是order_info的绑定
        OrderInfo orderInfo = prepareOrderInfo(orderSubmitVo);
        orderInfoService.save(orderInfo);


        //2、保存 order_detail
        List<OrderDetail> orderDetails = prepareOrderDetail(orderInfo);
        orderDetailService.saveBatch(orderDetails);

        //订单只要存到数据库就发消息。
        sendOrderCreateMsg(orderInfo.getId());

        return orderInfo.getId();
    }

    /**
     * 准备订单项的数据
     *
     * @return
     */
    private List<OrderDetail> prepareOrderDetail(OrderInfo orderInfo) {
        //1、拿到订单需要购买的所有商品
        List<CartItem> items = cartFeignClient.getCheckItem().getData();

        //2、每个要购买的商品其实就是一个订单项数据
        List<OrderDetail> detailList = items.stream()
                .map(item -> {
                    OrderDetail detail = new OrderDetail();
                    detail.setOrderId(orderInfo.getId());
                    Long userId = AuthUtil.getUserAuth().getUserId();
                    detail.setUserId(userId);

                    detail.setSkuId(item.getSkuId());
                    detail.setSkuName(item.getSkuName());
                    detail.setImgUrl(item.getSkuDefaultImg());
                    detail.setOrderPrice(item.getSkuPrice());
                    detail.setSkuNum(item.getSkuNum());
                    detail.setHasStock("1");
                    detail.setCreateTime(new Date());
                    detail.setSplitTotalAmount(new BigDecimal("0"));
                    detail.setSplitActivityAmount(new BigDecimal("0"));
                    detail.setSplitCouponAmount(new BigDecimal("0"));

                    return detail;
                }).collect(Collectors.toList());

        return detailList;
    }


    /**
     * 准备orderinfo数据
     *
     * @param vo
     * @return
     */
    private OrderInfo prepareOrderInfo(OrderSubmitVo vo) {
        OrderInfo orderInfo = new OrderInfo();

        orderInfo.setConsignee(vo.getConsignee());
        orderInfo.setConsigneeTel(vo.getConsigneeTel());

        List<CartItemForOrderVo> detailList = vo.getOrderDetailList();
        BigDecimal totalAmount = detailList.stream()
                .map(item -> item.getOrderPrice().multiply(new BigDecimal(item.getSkuNum().toString())))
                .reduce((a, b) -> a.add(b))
                .get();

        orderInfo.setTotalAmount(totalAmount);


        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());

        UserAuthTo auth = AuthUtil.getUserAuth();
        orderInfo.setUserId(auth.getUserId());
        orderInfo.setPaymentWay(PaymentWay.ONLINE.name());

        orderInfo.setDeliveryAddress(vo.getDeliveryAddress());
        orderInfo.setOrderComment(vo.getOrderComment());

        //对外交易号  48248294829084 9
        // 00000    9+26=35^5
        String random = UUID.randomUUID().toString().substring(0, 5);
        //提前生成
        orderInfo.setOutTradeNo("GMALL-" + System.currentTimeMillis() + "-" + auth.getUserId() + "-" + random); //同一用户、同一时刻，最大5000万并发


        //交易体: 所有购买的商品名
        String skuNames = detailList.stream().map(CartItemForOrderVo::getSkuName)
                .reduce((a, b) -> a + ";" + b)
                .get();
        orderInfo.setTradeBody(skuNames);


        orderInfo.setCreateTime(new Date());


        //过期时间  30min
        long time = System.currentTimeMillis() + 1000 * 60 * 30;
        orderInfo.setExpireTime(new Date(time));


        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());

        //物流追踪号
        orderInfo.setTrackingNo("");

        //拆单：父子订单
        orderInfo.setParentOrderId(0L);

        orderInfo.setImgUrl(detailList.get(0).getImgUrl()); //订单展示的图片

//        orderInfo.setOrderDetailList(Lists.newArrayList());

        orderInfo.setWareId("");
        orderInfo.setProvinceId(0L);

        orderInfo.setActivityReduceAmount(new BigDecimal("0"));
        orderInfo.setCouponAmount(new BigDecimal("0"));
        orderInfo.setOriginalTotalAmount(new BigDecimal("0"));
        //可退款日期 7天
        orderInfo.setRefundableTime(null);
        orderInfo.setFeightFee(new BigDecimal("0"));
        orderInfo.setOperateTime(new Date());

//        orderInfo.setId(0L);

        return orderInfo;
    }
}
