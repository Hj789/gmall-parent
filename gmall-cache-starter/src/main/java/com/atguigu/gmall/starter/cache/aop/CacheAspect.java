package com.atguigu.gmall.starter.cache.aop;

import com.atguigu.gmall.starter.constant.RedisConst;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * aop就是filter思想
 * 使用切面完成缓存的自动拦截逻辑
 *
 * 1. 导入 aop-starter
 * 2. @EnableAspectJAutoProxy
 * 3. 编写切面
 * 4. 切入点表达式
 */

@Component //切面也必须放在springboot容易中才能起作用
@Aspect
public class CacheAspect {

    @Autowired
    CacheHelper cacheHelper;



    @Around(value = "@annotation(com.atguigu.gmall.starter.cache.aop.annotation.Cache)")
    public Object around(ProceedingJoinPoint joinPoint){
        //获取目标方法参数
        Object[] args = joinPoint.getArgs();
        Object result = null;

        try {
            //前置通知,动态计算表达式
            //String cacheKey = RedisConst.SKU_CACHE_KEY_PREFIX+args[0];
            String cacheKey = cacheHelper.evaluteExpression(joinPoint);
            //1.先查缓存中有没有这个数据
            Object obj = cacheHelper.getCacheData(cacheKey,joinPoint);
            if (obj == null){
                //2.缓存中没有准备回源
                //4. 准备回源锁
                 String lockKey = RedisConst.LOCK_PREFIX+cacheKey;
                //4.1 确定回源之前,先问布隆 ①用不用 ②怎么用
                //判断布隆是否需要启用
                String bloomName = cacheHelper.determinBloom(joinPoint);
                if (StringUtils.isEmpty(bloomName)){
                    //TODO 不启用布隆,直接调用目标方法,并且要加锁
                    //4.1.1 布隆说有
                    boolean tryLock = cacheHelper.tryLock(lockKey);
                    if (tryLock){
                        //5. 加锁成功,回源.放行目标方法进行查询
                        //利用反射执行目标方法,改变目标方法用的参数
                        result = joinPoint.proceed(args);
                        //将查询到的数据存到redis缓存中
                        cacheHelper.saveData(cacheKey,result);
                        //返回目标方法查询到的数据
                        //7. 解锁
                        cacheHelper.unLock(lockKey);
                        return result;
                    }
                    //7. 没加锁成功
                    Thread.sleep(1000);
                    obj = cacheHelper.getCacheData(cacheKey,joinPoint);
                    //8. 返回数据
                    return obj;
                }else {
                    //TODO 启用布隆
                    boolean bloomContains = cacheHelper.bloomTest(bloomName,joinPoint);
                    if (bloomContains){
                        //4.1.1 布隆说有
                        boolean tryLock = cacheHelper.tryLock(lockKey);
                        if (tryLock){
                            //5. 加锁成功,回源.放行目标方法进行查询
                            //利用反射执行目标方法,改变目标方法用的参数
                            result = joinPoint.proceed(args);
                            //将查询到的数据存到redis缓存中
                            cacheHelper.saveData(cacheKey,result);
                            //返回目标方法查询到的数据
                            //7. 解锁
                            cacheHelper.unLock(lockKey);
                            return result;
                        }
                        //7. 没加锁成功
                        Thread.sleep(1000);
                        obj = cacheHelper.getCacheData(cacheKey,joinPoint);
                        //8. 返回数据
                        return obj;
                    }else {
                        //4.1.2 布隆说没有
                        return null;
                    }
                }

            }

            //3. 缓存中有
            return obj;
            //返回通知

        } catch (Throwable throwable) {
            //异常通知

        }finally {
            {
                //后置通知

            }
        }
        //目标方法的结果进行返回
        return result;
    }
}
