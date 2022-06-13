package com.atguigu.gmall.pay.config;

import com.atguigu.gmall.annotation.EnableAutoHandleException;
import com.atguigu.gmall.annotation.EnableFeignAuthHeaderInterceptor;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients(basePackages = {"com.atguigu.gmall.feign.order"})
@Configuration
@EnableAutoHandleException
@EnableFeignAuthHeaderInterceptor
public class AppConfig {
}
