package com.atguigu.gmall.web.config;

import com.atguigu.gmall.annotation.EnableFeignAuthHeaderInterceptor;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * 开启feign功能
 * @EnableFeignClients
 * 1. 扫描@EnableFeignClients所在类的包以及下面的子包所有的@FeignClient 标注的组件,创建代理对象并放到容器中
 */
@EnableFeignAuthHeaderInterceptor
@EnableFeignClients(basePackages = {"com.atguigu.gmall.feign.cart",
        "com.atguigu.gmall.feign.product",
        "com.atguigu.gmall.feign.list",
        "com.atguigu.gmall.feign.user",
        "com.atguigu.gmall.feign.item",
        "com.atguigu.gmall.feign.order",
        "com.atguigu.gmall.feign.seckill"})
@Configuration
public class AppFeignConfig {
}
