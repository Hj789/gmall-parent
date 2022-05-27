package com.atguigu.gmall.starter.cache.impl;

import com.atguigu.gmall.starter.cache.CacheService;
import com.atguigu.gmall.starter.utils.JSONs;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
public class CacheServiceImpl implements CacheService {

    @Autowired
    StringRedisTemplate redisTemplate;

//    @Override
//    public List<CategoryAndChildTo> getCategorys() {
//        //1. 远程查询redis 的categorys数据
//        String categorys = redisTemplate.opsForValue().get("categorys");
//
//        //2. redis没有缓存这个key 的数据
//        if (StringUtils.isEmpty(categorys)){
//            return null;
//        }
//
//        //3. redis有数据[反序列化]
//        // 对象转流(字符流,字节流) 序列化
//        // 流转对象: 反序列化
//        //牵扯到数据的传输或保存
//        List<CategoryAndChildTo> data = JSONs.strToCategoryObj(categorys);
//        return data;
//    }
//
//    @Override
//    public void saveCategoryData(List<CategoryAndChildTo> childs) {
//        String str = JSONs.toStr(childs);
//        redisTemplate.opsForValue().set("categorys",str);
//    }

    @Override
    public <T extends Object> T getCacheData(String key, TypeReference<T> typeReference) {
        //1.获取redis指定key数据
        String json = redisTemplate.opsForValue().get(key);

        //判断.缓存只要这个数据被查过一次,就一定有东西(不为空)
        if (!StringUtils.isEmpty(json)){
            //2.转换成指定的格式
            if ("no".equals(json)){
                T t = JSONs.nullInstance(typeReference);
                return t;
            }
            //真实数据
            T t = JSONs.strToObj(json,typeReference);
            return t;
        }
        //缓存中真没有, 都没有人查过这个数据
        return null; //只要返回null就调用数据库逻辑
    }

    @Override
    public void save(String key, Object data) {
        if (data == null){
            //数据库没有  //过期时间30分钟 被动型检查数据 ,缓存的短一点
            redisTemplate.opsForValue().set(key,"no",30, TimeUnit.MINUTES);
        }else {
            //数据库有 ,有的数据缓存的久一点
            //为了防止同时过期,给每个过去时间加上随机值
            Double  v= Math.random()*1000000000L;
            long mill = 1000 * 60 * 60 * 24 * 3 + v.intValue();
            redisTemplate.opsForValue().set(key,JSONs.toStr(data),mill,TimeUnit.MILLISECONDS);
        }
    }


}
