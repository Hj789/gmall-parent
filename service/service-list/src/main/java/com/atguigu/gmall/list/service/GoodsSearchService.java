package com.atguigu.gmall.list.service;

import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.vo.GoodsSearchResultVo;

public interface GoodsSearchService {

    //保存商品数据到es中
    void saveGoods(Goods goods);

    //从es中删除商品信息
    void deleteGoods(Long skuId);

    //检索
    GoodsSearchResultVo search(SearchParam param);


    void updateHotScore(Long skuId, Long score);
}
