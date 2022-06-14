package com.atguigu.gmall.seckill.mapper;

import com.atguigu.gmall.model.activity.SeckillGoods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.atguigu.gmall.seckill.domain.SeckillGoods
 */
public interface SeckillGoodsMapper extends BaseMapper<SeckillGoods> {


    List<SeckillGoods> getDaySeckillGoods(@Param("currentDate") String formatDate);

    /**
     * 修改这个商品的库存
     * @param skuId
     */
    void updateGoodsStockDecrement(@Param("skuId") Long skuId);
}




