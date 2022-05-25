package com.atguigu.gmall.product;


import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RedissonTest {

    @Autowired
    RedissonClient redissonClient;

    @Test
    public void redissonHello(){
        System.out.println(redissonClient);
    }

    @Test
    public void bloomCreate(){
        RBloomFilter<Long> filter = redissonClient.getBloomFilter("sku:bloom");

        //尝试初始化布隆
        //long expectedInsertions, double falseProbability
        filter.tryInit(1000000,0.00001);

        //保存数据
        filter.add(11L);
        filter.add(12L);
        filter.add(13L);

        System.out.println("12:"+filter.contains(12L));
        System.out.println("13:"+filter.contains(13L));
        System.out.println("17:"+filter.contains(17L));

    }




}
