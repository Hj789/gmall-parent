package com.atguigu.gmall.seckill.service.impl;

import com.atguigu.gmall.common.constants.RedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.common.util.JSONs;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.starter.cache.aop.annotation.Cache;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.seckill.service.SeckillGoodsService;
import com.atguigu.gmall.seckill.mapper.SeckillGoodsMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class SeckillGoodsServiceImpl extends ServiceImpl<SeckillGoodsMapper, SeckillGoods>
    implements SeckillGoodsService{

    @Autowired
    SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    StringRedisTemplate redisTemplate;

//    @Cache(cacheKey = RedisConst.SECKILL_GOODS_CACHE_PREFIX+"#{#currentDate}")
    @Override
    public List<SeckillGoods> queryCurrentDaySeckillGoods() {
        List<SeckillGoods> goods = null;
        String key = RedisConst.SECKILL_GOODS_CACHE_PREFIX + DateUtil.formatDate(new Date());
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(key);
        //redis中有这个
        if (hashOps.persist()) {
            //从redis中拿到的所有秒杀商品
            goods = hashOps.values().stream()
                    .map(str -> JSONs.strToObj(str, new TypeReference<SeckillGoods>() {
                    }))
                    .sorted((o1, o2) -> o1.getStartTime().compareTo(o2.getStartTime()))
                    .collect(Collectors.toList());
            return goods;
        }else {
            //没有就查库
            String formatDate = DateUtil.formatDate(new Date());
            goods = seckillGoodsMapper.getDaySeckillGoods(formatDate);
            //重新保存
            goods.stream().forEach(item->{
                //保存到redis
                hashOps.put(item.getSkuId().toString(),JSONs.toStr(item));
            });
            return goods;
        }
    }

    @Override
    public List<SeckillGoods> querySpecDaySeckillGoods(String upDays) {
        return seckillGoodsMapper.getDaySeckillGoods(upDays);

    }

    @Override
    public SeckillGoods getSeckillGoodsDetail(Long skuId) {
        String key = RedisConst.SECKILL_GOODS_CACHE_PREFIX + DateUtil.formatDate(new Date());
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(key);

        String json = hashOps.get(skuId.toString());

        SeckillGoods goods = JSONs.strToObj(json, new TypeReference<SeckillGoods>() {
        });

        return goods;
    }

    @Override
    public void deduceGoodsStock(Long skuId) {
        //
        seckillGoodsMapper.updateGoodsStockDecrement(skuId);
    }
}




