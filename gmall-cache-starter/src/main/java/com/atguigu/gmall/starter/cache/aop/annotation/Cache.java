package com.atguigu.gmall.starter.cache.aop.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache {

    //自定义属性
    String value() default ""; //代表key，缓存用的key


    // 1、 cacheKey = "hello:#{1+1}"   代表 cacheKey = hello:2
    // 2、 cacheKey = "haha-#{redis->msg}" 代表 cacheKey = haha-world
    // 3、 cacheKey = RedisConst.HAHA + "hehe#{args[2]}"  代表 cacheKey = hahahehe第三个参数的值
    @AliasFor("value")
    String cacheKey() default "";  //支持动态表达式计算


    //默认布隆不开启
//    boolean enableBloom() default false;
    //传入布隆过滤器的名字
    String bloomName() default "";

    //如果启动布隆过滤器，布隆过滤器判断用的值
    String bloomValue() default "";

}
