package com.atguigu.gmall.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


/**
 * 无需数据源,排除数据源的自动配置
 * @SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
 * @EnableDiscoveryClient
 * @EnableCircuitBreaker
 *
 *
 * 页面渲染
 * 1/ 引入thymeleaf
 * 2. 编写controller , 处理请求并return页面,并给页面放入一些属性
 * 3. 编写页面渲染逻辑, ${}取值遍历等操作
 */

@SpringCloudApplication
public class WebAllApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebAllApplication.class,args);
    }

}
