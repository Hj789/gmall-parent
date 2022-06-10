package com.atguigu.gmall.common.result;

import lombok.Getter;

/**
 * 统一返回结果状态信息类
 *
 */
@Getter
public enum ResultCodeEnum {

    SUCCESS(200,"成功"),
    FAIL(201, "失败"),
    SERVICE_ERROR(2012, "服务异常"),

    PAY_RUN(205, "支付中"),

    LOGIN_AUTH(208, "未登陆"),
    LOGIN_ERROR(2080, "账号密码错误"),
    PERMISSION(209, "没有权限"),
    FORBIDDEN(2090, "禁止访问"),
    SECKILL_NO_START(210, "秒杀还没开始"),
    SECKILL_RUN(211, "正在排队中"),
    SECKILL_NO_PAY_ORDER(212, "您有未支付的订单"),
    SECKILL_FINISH(213, "已售罄"),
    SECKILL_END(214, "秒杀已结束"),
    SECKILL_SUCCESS(215, "抢单成功"),
    SECKILL_FAIL(216, "抢单失败"),
    SECKILL_ILLEGAL(217, "秒杀请求不合法"),
    SECKILL_ORDER_SUCCESS(218, "下单成功"),
    COUPON_GET(220, "优惠券已经领取"),
    COUPON_LIMIT_GET(221, "优惠券已发放完毕"),

    CART_OVERFLOW(300,"购物车数量溢出"),
    CART_MERGE_OVERFLOW(301,"购物车合并数量超出限制,请移除部分商品"),
    PARAM_INVALIDA(400,"参数非法"),
    REQ_ILLEGAL_TOKEN_ERROR(5000,"页面过期,请刷新页面"),
    ORDER_PRICE_CHANGE(6000,"订单中的商品,有价格发生变化,请刷新页面重新确认"),
    PRODUCT_NO_STOCK(7000,"商品库存不足")
    ;

    private Integer code;

    private String message;

    private ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
