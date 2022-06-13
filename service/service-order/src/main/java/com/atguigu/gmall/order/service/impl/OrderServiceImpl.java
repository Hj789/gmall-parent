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
import com.atguigu.gmall.feign.pay.PayFeignClient;
import com.atguigu.gmall.feign.product.ProductFeignClient;
import com.atguigu.gmall.feign.user.UserFeignClient;
import com.atguigu.gmall.feign.ware.WareFeignClient;
import com.atguigu.gmall.model.cart.CartItem;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.PaymentWay;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.mqto.order.OrderCreateTo;
import com.atguigu.gmall.model.mqto.ware.WareOrderDetailTo;
import com.atguigu.gmall.model.mqto.ware.WareOrderTo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.to.UserAuthTo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.vo.order.*;
import com.atguigu.gmall.order.service.OrderDetailService;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.atguigu.gmall.order.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Autowired
    PayFeignClient payFeignClient;

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

    @Override
    public OrderInfo getOrderInfoIdAndAmount(Long orderId) {
        Long userId = AuthUtil.getUserAuth().getUserId();
        //select * from wh
        LambdaQueryWrapper<OrderInfo> wrapper =
                Wrappers.lambdaQuery(OrderInfo.class)
                        .eq(OrderInfo::getId, orderId)
                        .eq(OrderInfo::getUserId, userId);

        OrderInfo one = orderInfoService.getOne(wrapper);
        return one;
    }

    @Override
    public void updateOrderStatusToPAID(String outTradeNo) {
        //1、查出订单  outTradeNo  GMALL-1654649165168-3-9dc10
        long userId = Long.parseLong(outTradeNo.split("-")[2]);

        //2、修改
        ProcessStatus paid = ProcessStatus.PAID;
        orderInfoService.updateOrderStatusToPaid(outTradeNo, userId, paid.name(), paid.getOrderStatus().name());
    }

    @Override
    public void checkAndSyncOrderStatus(String outTradeNo) {
        //1、数据库查出此单
        long userId = Long.parseLong(outTradeNo.split("-")[2]);
        LambdaQueryWrapper<OrderInfo> wrapper = Wrappers.lambdaQuery(OrderInfo.class)
                .eq(OrderInfo::getUserId, userId)
                .eq(OrderInfo::getOutTradeNo, outTradeNo);
        OrderInfo orderInfo = orderInfoService.getOne(wrapper);




        //2、支付宝查出此单 payFeignClient
        Result<String> result = payFeignClient.queryTrade(outTradeNo);
        /**
         * TRADE_FINISHED
         * TRADE_SUCCESS  支付成功
         * WAIT_BUYER_PAY
         * TRADE_CLOSED
         */
        if("TRADE_SUCCESS".equals(result.getData()) && (orderInfo.getOrderStatus().equals(OrderStatus.UNPAID.name()) || orderInfo.getOrderStatus().equals(OrderStatus.CLOSED.name()))){
            //改成已支付即可
            updateOrderStatusToPAID(outTradeNo);
        }

    }

    @Override
    public List<WareOrderTo> orderSpilt(OrderSpiltVo spiltVo) {

        //库存系统不给我们这个订单的用户
        Long orderId = spiltVo.getOrderId();
        //仓库和商品的分布关系
        String wareSkuMap = spiltVo.getWareSkuMap();
        List<WareSkuVo> skuMap = JSONs.strToObj(wareSkuMap, new TypeReference<List<WareSkuVo>>() {
        });
//        List<WareSkuVo> skuMap = spiltVo.getWareSkuMap();

        //1、查出当前订单的详细信息
        OrderInfo info = orderInfoService.getById(orderId);
        //2、查出这个订单的订单项
        Long id = info.getId();
        Long userId = info.getUserId();
        List<OrderDetail> details = orderDetailService.list(Wrappers.lambdaQuery(OrderDetail.class)
                .eq(OrderDetail::getUserId, userId)
                .eq(OrderDetail::getOrderId, id));
        info.setOrderDetailList(details);

        //3、开始拆单并保存子订单
        List<WareOrderTo> collect = skuMap.stream().map(wareSkuVo -> {
            //3.1、每个仓库组合其实对应一个新的子订单
            String wareId = wareSkuVo.getWareId(); //在这个仓库 - 2
            List<String> skuIds = wareSkuVo.getSkuIds(); //有这些商品 - 9、10
            //3.2、拆单并保存
            WareOrderTo order = prepareAndSaveChildOrder(info, wareSkuVo);
            return order;
        }).collect(Collectors.toList());

        //4、修改父订单状态为 已拆分
        orderInfoService.updateOrderStatusToSpilt(ProcessStatus.SPLIT,userId,id);


        return collect;
    }

    /**
     *
     * @param parent     父订单
     * @param wareSkuVo  当前仓库组合
     */
    private WareOrderTo prepareAndSaveChildOrder(OrderInfo parent, WareSkuVo wareSkuVo) {
        //1、找到子单中的所有商品
        String wareId = wareSkuVo.getWareId();
        Set<Long> skuIds = wareSkuVo.getSkuIds().stream()
                .map(str -> Long.parseLong(str))
                .collect(Collectors.toSet());//当前子单的所有商品
        //子订单中所有商品的集合
        List<OrderDetail> details = parent.getOrderDetailList()
                .stream().filter(item -> skuIds.contains(item.getSkuId()))
                .collect(Collectors.toList());

        //2、设置子订单
        OrderInfo childOrder = new OrderInfo();

        childOrder.setConsignee(parent.getConsignee());
        childOrder.setConsigneeTel(parent.getConsigneeTel());
        childOrder.setOrderStatus(parent.getOrderStatus());
        childOrder.setUserId(parent.getUserId());
        childOrder.setPaymentWay(parent.getPaymentWay());
        childOrder.setDeliveryAddress(parent.getDeliveryAddress());
        childOrder.setOrderComment(parent.getOrderComment());
        childOrder.setOutTradeNo(parent.getOutTradeNo()); //支付宝的流水和父单一样；
        childOrder.setCreateTime(new Date());
        childOrder.setExpireTime(parent.getExpireTime());
        childOrder.setProcessStatus(parent.getProcessStatus());
        childOrder.setTrackingNo("");
        childOrder.setParentOrderId(parent.getId()); //设置父单id
        childOrder.setImgUrl(details.get(0).getImgUrl()); //子单中所有商品的第一个商品的图片

        childOrder.setWareId(wareId);

        childOrder.setProvinceId(parent.getProvinceId());
        childOrder.setTradeBody(details.get(0).toString());

        //订单的商品集合
        childOrder.setOrderDetailList(details);
        //订单原始总额
//        childOrder.setOriginalTotalAmount(new BigDecimal("0"));
        //订单总额
//        childOrder.setTotalAmount(new BigDecimal("0"));
        childOrder.sumTotalAmount();//求总额，自动设置好 OriginalTotalAmount、TotalAmount

        childOrder.setRefundableTime(parent.getRefundableTime());
        childOrder.setFeightFee(new BigDecimal("0"));
        childOrder.setOperateTime(new Date());

        //保存子订单。 判断这个单是否已经被拆了。
        orderInfoService.save(childOrder);

        //保存子订单项
        List<OrderDetail> detailList = childOrder.getOrderDetailList();
        //回填子订单项的订单id
        detailList.stream().forEach(item->item.setOrderId(childOrder.getId()));

        orderDetailService.saveBatch(detailList);


        //构造当前子订单的返回
        WareOrderTo orderTo = new WareOrderTo();
        orderTo.setOrderId(childOrder.getId());
        orderTo.setConsignee(childOrder.getConsignee());
        orderTo.setConsigneeTel(childOrder.getConsigneeTel());
        orderTo.setOrderComment(childOrder.getOrderComment());
        orderTo.setOrderBody(childOrder.getTradeBody());
        orderTo.setDeliveryAddress(childOrder.getDeliveryAddress());
        orderTo.setPaymentWay("2");
        List<WareOrderDetailTo> detailTos = childOrder.getOrderDetailList()
                .stream()
                .map(item ->
                        new WareOrderDetailTo(item.getSkuId(),
                                item.getSkuNum(),
                                item.getSkuName()))
                .collect(Collectors.toList());

        orderTo.setDetails(detailTos);
        orderTo.setWareId(wareId);


        return orderTo;

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
