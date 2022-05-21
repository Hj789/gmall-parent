package com.atguigu.gmall.product.config;

import com.atguigu.gmall.annotation.EnableAppSwaggerApi;
import com.atguigu.gmall.annotation.EnableAutoHandleException;
import com.atguigu.gmall.annotation.EnableMinio;
import com.atguigu.gmall.config.AppMybatisPlusConfiguratoin;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

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
public class AppConfiguration {

    //额外配置写到这里




}
