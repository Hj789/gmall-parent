package com.atguigu.gmall.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@SpringBootTest //这是一个SpringBoot测试
public class AppThreadPoolTest {

    @Qualifier("corePool")
    @Autowired
    ThreadPoolExecutor poolExecutor;


    /**
     * 启动异步任务
     * 启动一个任务: 返回一个CompletableFuture
     */
    //@Transactional //所有测试期间的数据会被自动回滚
    @Test
    public void startAsyncTest() throws ExecutionException, InterruptedException {


        CompletableFuture.runAsync(()->{
            System.out.println(Thread.currentThread().getName()+"哈哈哈...");
        },poolExecutor);

        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "正在计算");
            Double random = Math.random()*100;
            return random.intValue();
        }, poolExecutor);
        Integer result = future.get(); //阻塞等待
        System.out.println("结果:"+result );
    }

    /**
     * CompletableFuture future
     * 1、thenXXX： 前一个任务结束以后，继续做接下来的事情
     * 2、whenXxx: when的事件回调
     * whenComplete： 完成后干啥
     * 前一个任务.whenComplete((t,u)->{ 处理t[上一步结果],u[上一步异常] })
     * xxxxAsync： 带了Async代表这些方法运行需要开新线程
     * 指定线程池：  就在指定线程池中开新线程
     * 3、exceptionally： 前面异常以后干什么
     */

//    @Test
//    public void bianpaiTest(){
//        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//            System.out.println("啦啦啦");
//        }, poolExecutor);
//
//        future.whenComplete()






        //get就是阻塞等待
//        future.get();
//        System.out.println("///");

//    }


    @Test
    public void poolExecutorTest(){
        System.out.println("线程池:"+poolExecutor);
        int corePoolSize = poolExecutor.getCorePoolSize();
        System.out.println(corePoolSize);
        System.out.println(poolExecutor.getQueue().remainingCapacity());
    }



}
