package com.atguigu.gmall.seckill.service;

import com.atguigu.gmall.model.activity.SeckillGoods;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 *
 */
public interface SeckillGoodsService extends IService<SeckillGoods> {
    /**
     * 查询当前所有的秒杀商品
     * 1. 去redis查,如果redis没有,继续查库,查到以后放redis即可
     * @return
     */
    List<SeckillGoods> queryCurrentDaySeckillGoods();


    /**
     * 查询指定一天需要上架的商品
     * @param day
     * @return
     */
    List<SeckillGoods> querySpecDaySeckillGoods(String day);

    /**
     * 获取指定的秒杀商品详情
     * @param skuId
     * @return
     */
    SeckillGoods getSeckillGoodsDetail(Long skuId);

    /**
     * 减掉指定商品的库存
     * @param skuId
     */
    void deduceGoodsStock(Long skuId);
}
