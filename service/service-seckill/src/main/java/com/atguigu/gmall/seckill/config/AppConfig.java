package com.atguigu.gmall.seckill.config;

import com.atguigu.gmall.annotation.EnableAutoHandleException;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableScheduling
@EnableTransactionManagement
@EnableAutoHandleException //全局自动异常处理
@MapperScan("com.atguigu.gmall.seckill.mapper")
@EnableFeignClients(basePackages = "com.atguigu.gmall.feign.product")
@EnableRabbit
@Configuration
public class AppConfig {
}
