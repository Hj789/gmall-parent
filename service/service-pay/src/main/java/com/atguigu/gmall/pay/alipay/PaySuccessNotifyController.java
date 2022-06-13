package com.atguigu.gmall.pay.alipay;

import com.alipay.api.AlipayApiException;
import com.atguigu.gmall.common.constants.MqConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.JSONs;
import com.atguigu.gmall.feign.order.OrderFeignClient;
import com.atguigu.gmall.model.mqto.order.PayNotifySuccessVo;
import com.atguigu.gmall.pay.service.AlipayService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 感知支付宝给我们发来的通知
 */
@RequestMapping("/api/payment")
@RestController
public class PaySuccessNotifyController {

    @Autowired
    AlipayService alipayService;

    @Autowired
    OrderFeignClient orderFeignClient;


    @Autowired
    RabbitTemplate rabbitTemplate;



    /**
     * 支付宝的异步通知里面改订单状态
     *
     * @return
     * @RequestParam Map<String, String> req 把当前请求带来的所有参数封装到map
     *
     * 支付成功====支付宝 http://2yv3x6up8z.51xd.pub/api/payment/notify/success
     * ==== api.gmall.com 发给网关 ===
     *
     *
     */
    @PostMapping("/notify/success")  //支付宝调用
    public String paySuccessNotify(PayNotifySuccessVo vo,
                                   @RequestParam Map<String, String> req) throws AlipayApiException {
        /**
         * 最大努力通知；分布式事务；
         * 程序执行完后必须打印输出“success”（不包含引号）。
         * 如果商户反馈给支付宝的字符不是 success 这7个字符，
         * 支付宝服务器会不断重发通知，直到超过 24 小时 22 分钟。
         * 一般情况下，25 小时以内完成 8 次通知
         * （通知的间隔频率一般是：立即，4m,10m,10m,1h,2h,6h,15h）。
         */
        System.out.println("支付宝通知到达：" + vo);
        Map<String, String> params = req;
        //验签很重要
        boolean sign = alipayService.checkSign(params);
        if (sign) {
            System.out.println("验签通过.....");
            //订单信息（out_trade_no，status）发给消息队列； 1ms
            //签名验证通过，修改订单状态信息 //订单服务宕机
//            Result result = orderFeignClient.updateOrderStatusToPAID(vo.getOut_trade_no());
//            if (result.isOk()) {
//                return "success";  //收到success支付宝就不通知
//            }
//            return "error";

            //给订单发送消息即可
            rabbitTemplate.convertAndSend(
                    MqConst.ORDER_EVENT_EXCHANGE,
                    MqConst.RK_ORDER_PAYED,
                    JSONs.toStr(vo));

            return "success";
        }
        System.out.println("验签失败....");
        return "error";
    }

}