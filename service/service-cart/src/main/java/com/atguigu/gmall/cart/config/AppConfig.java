package com.atguigu.gmall.cart.config;

import com.atguigu.gmall.annotation.EnableAppDoubleThreadPool;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableAppDoubleThreadPool
@EnableFeignClients(basePackages = "com.atguigu.gmall.feign.product")
@Configuration
public class AppConfig {
}
