package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.cache.CacheService;
import com.atguigu.gmall.common.constants.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.JSONs;
import com.atguigu.gmall.feign.product.ProductFeignClient;
import com.atguigu.gmall.item.service.SkuDetailService;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.to.SkuDetailTo;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SkuDetailServiceImpl implements SkuDetailService {

    @Autowired
    ProductFeignClient productFeignClient;

    @Autowired
    ThreadPoolExecutor corePool;

    @Autowired
    CacheService cacheService;

    @Autowired
    RBloomFilter<Object> skuIdBloom;

    @Autowired
    StringRedisTemplate redisTemplate;


    /**
     * 使用redis原生的分布式锁
     * 引入缓存的查询商品详情
     * sku:detail:45  value:json
     * sku:detail:46  value:json
     * sku:detail:47  value:json
     * @param skuId
     * @return
     */
    @Override
    public SkuDetailTo getSkuDetail(Long skuId) {
        String cacheKey = RedisConst.SKU_CACHE_KEY_PREFIX + skuId;
        //1. 查询缓存
        SkuDetailTo cacheData = cacheService.getCacheData(cacheKey,
                new TypeReference<SkuDetailTo>() {
                });
        //2. 判断
        if (cacheData == null){
            //3. 缓存中没有,查库[回源]
            //回源之前,先问下布隆,这个东西有没有
            if (skuIdBloom.contains(skuId)) {
                //4.布隆中有
                log.info("SkuDetail:{}缓存没命中,正在回源",skuId);

                //5. 注意加锁,否则会被击穿;最好加分布锁.setnx 防止击穿
                //去redis中占坑一个key(如果这个key没有人占,那我们就能占成功) 原子的. 以为redis是原子的
                String token = UUID.randomUUID().toString();
                //加锁【占坑+自动过期时间(合起来是原子)】
                //锁的粒度: 一定设计更细粒度的锁,来保证并发吞吐能力
                Boolean lock = redisTemplate.opsForValue()
                        .setIfAbsent(RedisConst.LOCK_PREFIX+skuId, token,10,TimeUnit.SECONDS);
                SkuDetailTo db = null;
                if (lock){
                    //就算100w请求,只会有一个人返回true,代表抢锁成功
                    //即使业务断电,由redis自动删锁

                    //非原子的加锁操作
                    //redisTemplate.expire("lock",10, TimeUnit.SECONDS);//自动解锁逻辑

                    //加锁和设置过期时间应该一起完成

                    try{
                        log.info("分布式加锁成功:SkuDetail{}:真的查库",skuId);
                        db = getSkuDetailFromDb(skuId);
                        cacheService.save(cacheKey,db);
                        //业务期间,如果发生了断电风险,会导致finally不执行,解锁失败,导致死锁

                        //防止业务卡住,锁会自动过期
                        //TODO 1. 锁的续期: 自动续期
                    }finally {
                        //释放锁
//                        String lockValue = redisTemplate.opsForValue().get("lock");
                        //删锁脚本 if(redis.get(lock) == "ddddddd") then return redis.del(lock) else return 0
                        // 1（删除成功）  0（删除失败）
                        //删锁： 【对比锁值+删除(合起来保证原子性)】
                        String deleteScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                        //List<K> keys, Object... args
                        Long result = redisTemplate.execute(new DefaultRedisScript<>(deleteScript,Long.class),
                                Arrays.asList(RedisConst.LOCK_PREFIX+skuId), token);
                        if(result == 1){
                            log.info("我的分布式锁解锁完成.");
                        }else {
                            //别人的锁【说明之前由于业务卡顿，续期失败等原因，锁被自动释放，被别人抢到】
                            log.info("这是别人的锁，我不能删.");
                        }
                    }

                }else {
                    log.info("分布式抢锁失败:1s后直接查缓存");
                    //抢锁失败?false
                    try {
                        Thread.sleep(1000); //睡1s的业务时长
                        //缓存中是说明样就给什么样的数据,不用走抢锁,100w请求也总共就等1s,不要让100w自旋抢锁
                        cacheData = cacheService.getCacheData(cacheKey,
                                new TypeReference<SkuDetailTo>() {
                                });
                        log.info("SkuDetail：{}缓存命中111", skuId);
                        return cacheData;
                    } catch (InterruptedException e) {

                    }

                }

                return db;
            }
            // 5.布隆没有
            log.info("SkuDetail{}缓存没命中,bloom防火墙拦截打回",skuId);
            return null;
        }
        //6. 缓存中有,直接返回
        log.info("SkuDetail：{}缓存命中", skuId);
        return cacheData;
    }




    /**
     * 商品详情服务:
     * 查询sku详情得做这么多事
     * 1. 差分类
     * 2. 查sku信息
     * 3. 查sku的图片列表
     * 4. 查价格
     * 5. 查所有销售属性组合
     * 6. 查实际sku组合
     * 7. 查介绍(不用管)
     */
    //@Override
    public SkuDetailTo getSkuDetailFromDb(Long skuId) {
        SkuDetailTo detailTo = new SkuDetailTo();
        //异步
        //编排:编组(管理)+排序组合(运行)
        CompletableFuture<Void> categoryTask = CompletableFuture.runAsync(() -> {
            //1. 查分类
            Result<BaseCategoryView> skuCategoryView = productFeignClient.getSkuCategoryView(skuId);
            if (skuCategoryView.isOk()) {
                detailTo.setCategoryView(skuCategoryView.getData());
            }
        }, corePool);

        CompletableFuture<Void> skuInfoTask = CompletableFuture.runAsync(()->{
            //2. 查sku信息&3. 查sku的图片列表
            Result<SkuInfo> skuInfo = productFeignClient.getSkuInfo(skuId);
            if (skuInfo.isOk()) {
                detailTo.setSkuInfo(skuInfo.getData());
            }
        },corePool);

        CompletableFuture<Void> priceTask = CompletableFuture.runAsync(()->{
            //4. 查价格
            Result<BigDecimal> price = productFeignClient.getPrice(skuId);
            if (price.isOk()){
                detailTo.setPrice(price.getData());
            }
        },corePool);

        CompletableFuture<Void> saleAttrTask = CompletableFuture.runAsync(()->{
            //5. 查所有销售属性组合
            Result<List<SpuSaleAttr>> saleAttrAndValue = productFeignClient.getSkudeSpuSaleAttrAndValue(skuId);
            if (saleAttrAndValue.isOk()){
                detailTo.setSupSaleAttrList(saleAttrAndValue.getData());
            }
        },corePool);

        CompletableFuture<Void> valueJsonTask = CompletableFuture.runAsync(()->{
             //6.查询valueJson信息
            Result<Map<String, String>> skuValueJson = productFeignClient.getSkuValueJson(skuId);
            if (skuValueJson.isOk()){
                Map<String, String> jsonData = skuValueJson.getData();
                detailTo.setValuesSkuJson( JSONs.toStr(jsonData));
            }
        },corePool);

        CompletableFuture.allOf(categoryTask,skuInfoTask,priceTask,saleAttrTask,valueJsonTask)
                .join(); // allOf 返回的CompletableFuture总任务结束再往下

        return detailTo;
    }


}
