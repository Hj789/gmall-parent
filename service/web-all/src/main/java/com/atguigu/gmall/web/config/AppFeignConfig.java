package com.atguigu.gmall.web.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * 开启feign功能
 * @EnableFeignClients
 * 1. 扫描@EnableFeignClients所在类的包以及下面的子包所有的@FeignClient 标注的组件,创建代理对象并放到容器中
 */
@EnableFeignClients(basePackages = "com.atguigu.gmall.feign")
@Configuration
public class AppFeignConfig {
}
