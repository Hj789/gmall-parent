package com.atguigu.gmall.item;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;
import org.junit.jupiter.api.Test;

public class GuavaTest {

    @Test
    public  void bloomTest(){
        // Funnel<? super T> funnel,
        // int expectedInsertions,  //预估数据量
        // double fpp //误判率

        //1、准备一个数据存储规则的通道
        Funnel<Integer> funnel = Funnels.integerFunnel();


        //void funnel(T var1, PrimitiveSink var2);
        //2、创建一个布隆过滤器
        BloomFilter<Integer> filter = BloomFilter
                .create(funnel, 1000000, 0.000001);


        //3、放数据库全量数据;
        //项目要等这个全局结束以后才能运行，3min
        filter.put(88);
        filter.put(99);
        filter.put(100);






        System.out.println("布隆过滤器初始化完成：并且保存了 88,99,100");


        System.out.println("99:"+filter.mightContain(99));
        System.out.println("100:"+filter.mightContain(100));

        //只要说没有，一定没有，不用给数据库放请求
        System.out.println("77:"+filter.mightContain(77));

        //基于这个原理，防止随机值穿透攻击。

    }


}
