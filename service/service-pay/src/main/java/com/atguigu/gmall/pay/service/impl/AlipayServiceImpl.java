package com.atguigu.gmall.pay.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.common.util.JSONs;
import com.atguigu.gmall.feign.order.OrderFeignClient;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.pay.config.alipay.AliPayProperties;
import com.atguigu.gmall.pay.service.AlipayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
public class AlipayServiceImpl implements AlipayService {

    @Autowired
    AlipayClient alipayClient;

    @Autowired
    AliPayProperties properties;

    @Autowired
    OrderFeignClient orderFeignClient;

    @Override
    public String payPage(Long orderId) throws AlipayApiException {
        //1、创建一个阿里client

        //2、准备一个支付请求
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();

        //3、设置好请求参数
        alipayRequest.setReturnUrl(properties.getReturn_url());
        alipayRequest.setNotifyUrl(properties.getNotify_url());

        //根据当前订单，得到订单的价格等信息，构造出 支付要用的请求参数的 json
        String json = buildBizContent(orderId);
        alipayRequest.setBizContent(json);

        //4、执行请求
        String result = alipayClient.pageExecute(alipayRequest).getBody();


        //5、得到响应的表单页
        return result;
    }

    @Override
    public boolean checkSign(Map<String,String> params) throws AlipayApiException {
        boolean signVerified = AlipaySignature.rsaCheckV1(params,
                properties.getAlipay_public_key(),
                properties.getCharset(),
                properties.getSign_type());
        return signVerified;
    }

    @Override
    public String queryTrade(String outTradeNo) throws AlipayApiException {
        String tradeStatus = "";
        //1、构造查询请求
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", outTradeNo);
        request.setBizContent(bizContent.toString());

        //2、执行查询； alipayClient得到或发送的数据都是安全，直接用
        AlipayTradeQueryResponse response = alipayClient.execute(request);

        //3、查到的数据
        if(response.isSuccess()){
            //提取订单的状态数据
            tradeStatus = response.getTradeStatus();

        }

        return tradeStatus;
    }


//    @Override
//    public String queryTrade(String outTradeNo) throws AlipayApiException {
//        String tradeStatus = "";
//        //1、构造查询请求
//        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
//        JSONObject bizContent = new JSONObject();
//        bizContent.put("out_trade_no", outTradeNo);
//        request.setBizContent(bizContent.toString());
//
//        //2、执行查询； alipayClient得到或发送的数据都是安全，直接用
//        AlipayTradeQueryResponse response = alipayClient.execute(request);
//
//        //3、查到的数据
//        if(response.isSuccess()){
//            //提取订单的状态数据
//            tradeStatus = response.getTradeStatus();
//
//        }
//
//        return tradeStatus;
//    }


    /**
     * 构造支付参数
     * @param orderId
     * @return
     */
    private String buildBizContent(Long orderId) {
        Map<String,String> params = new HashMap<>();

        Result<OrderInfo> info = orderFeignClient.getOrderInfoByUserId(orderId);
        if (info.isOk()){
            OrderInfo data = info.getData();
            params.put("out_trade_no",data.getOutTradeNo());
            params.put("total_amount",data.getTotalAmount().toPlainString());
            params.put("subject","尚品汇-"+data.getTradeBody());
            params.put("body",data.getTradeBody());
            params.put("product_code","FAST_INSTANT_TRADE_PAY");
//            //绝对时间  yyyy-MM-dd HH:mm:ss
            Date time = info.getData().getExpireTime();
            String formatDate = DateUtil.formatDate(time, "yyyy-MM-dd HH:mm:ss");
//            //自动收单（关单）
            params.put("time_expire",formatDate);
//////            params.put("business_params")
        }
        return JSONs.toStr(params);

    }
}