package com.atguigu.index.service.aspect;

import com.alibaba.fastjson.JSON;
import com.atguigu.index.service.annotation.GmallCache;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class GmallCacheAspect {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate redisTemplate;


    @Around("@annotation(com.atguigu.index.service.annotation.GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        Object result = null;
//        获取目标方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        GmallCache annotation = method.getAnnotation(GmallCache.class);
        Class<?> returnType = method.getReturnType();
        String prefix = annotation.prefix();
//        获取目标方法参数
        Object[] args = joinPoint.getArgs();
        String key = prefix + Arrays.asList(args).toString();

        result = cacheHit(key, returnType);
//        缓存中没有数据，加锁
        if (result != null) {
            return result;
        }
        RLock lock = this.redissonClient.getLock("lock" + Arrays.asList(args).toString());
        lock.lock();
//        再次查询缓存
        result = this.cacheHit(key, returnType);
//        缓存中有数据直接返回，并且释放锁
        if (result != null) {
            lock.unlock();
            return result;
        }
//        缓存中没有数据查询数据库，执行目标方法
        result = joinPoint.proceed(joinPoint.getArgs());
//        将数据放入缓存中
        int timeout = annotation.timeout();
        int random = annotation.random();
        this.redisTemplate.opsForValue().set(key, JSON.toJSONString(result), timeout + (int) (Math.random() * random), TimeUnit.MINUTES);
//        释放锁
        lock.unlock();
        return result;
    }


    private Object cacheHit(String key, Class<?> returnType) {
        // 从缓存中查询
        String json = this.redisTemplate.opsForValue().get(key);

        // 命中，直接返回
        if (StringUtils.isNotBlank(json)) {
            return JSON.parseObject(json, returnType);
        }
        return null;
    }
}
