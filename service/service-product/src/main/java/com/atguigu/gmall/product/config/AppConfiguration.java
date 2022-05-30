package com.atguigu.gmall.product.config;

import com.atguigu.gmall.annotation.*;
import com.atguigu.gmall.config.AppMybatisPlusConfiguratoin;
import com.atguigu.gmall.starter.annotation.EnableAppRedissonAndCache;
import com.atguigu.gmall.starter.annotation.EnableAutoCache;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 当前应用的配置
 */
//@ProductConfig
@MapperScan(basePackages = {"com.atguigu.gmall.product.dao","com.atguigu.gmall.product.mapper"})
@Import(AppMybatisPlusConfiguratoin.class)
@Configuration
@EnableMinio
@EnableAutoHandleException
@EnableAppSwaggerApi
@EnableScheduling
@EnableFeignClients("com.atguigu.gmall.feign.list")
//@EnableAutoCache //开启自动缓存功能
//@EnableAppRedissonAndCache
public class AppConfiguration {

    //额外配置写到这里




}
