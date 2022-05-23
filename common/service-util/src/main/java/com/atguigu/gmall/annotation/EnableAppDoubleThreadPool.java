package com.atguigu.gmall.annotation;

import com.atguigu.gmall.config.Swagger2Config;
import com.atguigu.gmall.config.threadPool.AppThreadPoolAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Import(AppThreadPoolAutoConfiguration.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EnableAppDoubleThreadPool {
}
