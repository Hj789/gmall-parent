package com.atguigu.gmall.pay.config.alipay;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AliPayProperties.class)
public class AlipayAutoConfiguration {

    @Bean
    public AlipayClient alipayClient(AliPayProperties properties){
        return new DefaultAlipayClient(properties.gatewayUrl,
                properties.app_id,
                properties.merchant_private_key,
                "json",
                properties.charset,
                properties.alipay_public_key, properties.sign_type);
    }
}
