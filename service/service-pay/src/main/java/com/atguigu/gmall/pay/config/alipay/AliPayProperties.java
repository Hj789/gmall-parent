package com.atguigu.gmall.pay.config.alipay;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.pay.alipay")
public class AliPayProperties {

    String app_id; //应用id

    String merchant_private_key; //商户私钥
    String alipay_public_key; //支付宝公钥
    String notify_url;  //异步通知地址
    String return_url;  //支付成功后页面同步跳转地址
    String sign_type;   //签名方式 RSA2
    String charset;     //字符集
    String gatewayUrl;  //支付宝网关

}
