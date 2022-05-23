package com.atguigu.gmall.product;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
public class RedisTest {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Test
    void testRedis(){
        redisTemplate.opsForValue().set("hello","world");

        String hello = redisTemplate.opsForValue().get("hello");
        System.out.println("取出的值："+hello);
    }
}