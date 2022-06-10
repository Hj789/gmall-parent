package com.atguigu.gmall.model.vo.order;

import com.atguigu.gmall.model.user.UserAddress;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderConfirmVo {

    //imgUrl、skuName、orderPrice[实时价格]、skuNum
    private List<CartItemForOrderVo> detailArrayList;
    private Integer totalNum;
    private BigDecimal totalAmount;
    private List<UserAddress> userAddressList;

    //用户可能会让同一个请求发很多次
    //只需要处理一次即可
    //1、基于前端。  每次发请求的时候准备一个随机数（t=jdk），服务器收到请求以后，需要看这个随机数的请求有没有处理过（setnx）？
    //   风险：伪造、脚本

    //2、基于后台。  令牌机制。

    private String tradeNo; //防重复提交

}
