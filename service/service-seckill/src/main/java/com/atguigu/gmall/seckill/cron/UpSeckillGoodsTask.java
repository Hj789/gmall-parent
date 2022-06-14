package com.atguigu.gmall.seckill.cron;

import com.atguigu.gmall.common.constants.RedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.seckill.biz.SeckillGoodsLocalCache;
import com.atguigu.gmall.seckill.service.SeckillGoodsService;
import com.atguigu.gmall.starter.utils.JSONs;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class UpSeckillGoodsTask {


    @Autowired
    SeckillGoodsService seckillGoodsService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    SeckillGoodsLocalCache localCache;
    /**
     * 定时上架第二天参与秒杀的所有商品
     * 秒 分 时  日 月 周
     * 1、我们：每天晚上凌晨2点，上架当天需要参与秒杀的商品
     *         每天晚上11点，上架第二天需要参与秒杀的商品
     * 2、京东：(超前上架)：每天晚上凌晨2点，上架当天和第二天的商品；
     */
//    @Scheduled(cron = "0 0 23 * * ?")
    @Scheduled(cron = "0 * * * * ?") //每分钟整分钟上架初始化数据
    public  void  upseckillgoods(){

        log.info("定时上架第二天秒杀商品的数据");
        //1、第二天
//        String secondDay = secondDay(); //生产环境用这个
        String secondDay = currentDay();
        //2、拿到第二天需要上架的所有商品
        List<SeckillGoods> goods = daySeckillGoods(secondDay);

        //3、缓存中上架商品数据
        uptoCache(secondDay, goods);

        //4、上架商品的库存标志位信息。做成一个【分布式信号量】； redis中一个商品的key+1-1
        upStockSemaphore(secondDay,goods);

        //5、给自己的本地缓存也保存
        localCache.syncData(goods);

    }

    /**
     * 上架商品信号量
     * @param secondDay
     * @param goods
     */
    private void upStockSemaphore(String secondDay, List<SeckillGoods> goods) {

        //设置每个商品的库存信号
        goods.stream().forEach(item->{
            Long skuId = item.getSkuId(); //商品id
            Integer count = item.getStockCount(); //商品库存

            // seckill:goods:2022-06-11:8 = 10
            String semaphoreKey = RedisConst.SECKILL_GOODS_CACHE_PREFIX+secondDay+":"+skuId;
            RSemaphore semaphore = redissonClient.getSemaphore(semaphoreKey);
            //初始化信号量； 设置库存量
            semaphore.trySetPermits(count); //如果以前设置过就不设置。
        });

        log.info("秒杀商品的数据信号量设置完成");

    }

    private void uptoCache(String secondDay, List<SeckillGoods> goods) {
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS_CACHE_PREFIX + secondDay);
        //缓存商品数据
        goods.stream().forEach(item->{
            ops.put(item.getSkuId().toString(),JSONs.toStr(item));
        });


        log.info("秒杀商品的数据缓存上架完成");
    }


    /**
     * 每天晚上凌晨结算前一天所有秒杀的商品信息
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void jiesuanprevDaySeckillGoods(){

    }


    private String currentDay(){
        return DateUtil.formatDate(new Date());
    }


    private String secondDay(){
        //当天晚上23点的
//        Date date = new Date();
        //1、得到第二天时间
        LocalDate plusDays = LocalDate.now().plusDays(1L);
        //2、只要Date部分
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        //3、得到需要上架的哪一天的商品
        String upDays = plusDays.format(formatter);
        return upDays;
    }


    private List<SeckillGoods> daySeckillGoods(String day){

        //4、查询指定某一天的需要上架的商品
        List<SeckillGoods> goods = seckillGoodsService.querySpecDaySeckillGoods(day);

        return goods;
    }



}