package com.atguigu.gmall.cache;

import com.atguigu.gmall.model.to.CategoryAndChildTo;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public interface CacheService {
//    List<CategoryAndChildTo> getCategorys();
//
//    void saveCategoryData(List<CategoryAndChildTo> childs);

    /**
     * 从缓存获取一个数据
     * @param cacheKey
     * @return
     */
    <T> T getCacheData(String cacheKey, TypeReference<T> typeReference);

    /**
     * 给缓存中保存数据
     * @param key
     */
    void save(String key,Object data);
}
