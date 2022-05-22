package com.atguigu.gmall.item.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients(basePackages = {"com.atguigu.gmall.feign.product"})
@Configuration
public class AppConfig {


}
