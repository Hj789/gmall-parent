package com.atguigu.gmall.seckill.biz;

import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.seckill.service.SeckillGoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SeckillGoodsLocalCache {

    private Map<Long, SeckillGoods> cache = new ConcurrentHashMap<>();

    @Autowired
    SeckillGoodsService seckillGoodsService;


    /**
     * 本地缓存同步数据
     * @param goods
     */
    public void syncData(List<SeckillGoods> goods) {

        goods.stream().forEach(item->{
            cache.put(item.getSkuId(),item);
        });
        log.info("本地缓存同步数据完成...");
    }

    void remoteLocalSync(){
        //1、查询当天的所有秒杀商品； redis也有数据了
        List<SeckillGoods> goods = seckillGoodsService.queryCurrentDaySeckillGoods();

        //2、同步到本地
        syncData(goods);
        log.info("本地缓存远程同步数据完成...");
    }


    /**
     * 拿到一个商品
     * @param skuId
     * @return
     */
    public SeckillGoods getDetailFromLocalCache(Long skuId){
        SeckillGoods goods = cache.get(skuId);
        //1、如果拿不到，把远程的数据再同步
        if(goods == null){
            if(cache.size() > 0){
                //本地缓存有东西，没有就是真没有
                return null;
            }else {
                //本地缓存没东西，有可能没同步
                remoteLocalSync();
                return cache.get(skuId);
            }
        }

        log.info("本地缓存返回秒杀商品详情:{}",skuId);
        return goods;
    }

    /**
     * 所有的秒杀商品
     * @return
     */
    public List<SeckillGoods> getAllSeckillGoods(){
        if(cache.size()<=0){
            //本地缓存没数据，就远程同步
            remoteLocalSync();
        }

        log.info("本地缓存返回所有秒杀商品");

        //返回本地缓存中的所有数据
        return cache.values().stream()
                .sorted((o1, o2) -> o1.getStartTime().compareTo(o2.getStartTime()))
                .collect(Collectors.toList());

    }
}