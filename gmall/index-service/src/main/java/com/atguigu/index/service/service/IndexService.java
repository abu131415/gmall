package com.atguigu.index.service.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVo;
import com.atguigu.index.service.annotation.GmallCache;
import com.atguigu.index.service.feign.GmallPmsClient;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class IndexService {

    @Autowired
    private RedissonClient redissonClient;


    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "index:cates:";

    @Autowired
    private GmallPmsClient gmallPmsClient;

    public List<CategoryEntity> queryCategoryLev01ByPid() {
        Resp<List<CategoryEntity>> resp = this.gmallPmsClient.queryCategoryEntityByIdOrLevel(1, null);
        return resp.getData();
    }

    /**
     * 查询二级分类三级类数据，并且进行缓存优化
     *
     * @param pid
     * @return
     */
    @GmallCache(prefix = "index:cates:", timeout = 7200)
    public List<CategoryVo> querySbusCategories(Long pid) {
/*
//        判断缓存有没有数据
        String categoryVos = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
//        有数据直接返回
        if (!StringUtils.isBlank(categoryVos)) {
//            使用fastJson工具，将字符串接卸为集合对象
            List<CategoryVo> categoryVoList = JSON.parseArray(categoryVos, CategoryVo.class);
            return categoryVoList;
        }


//        加锁
        RLock lock = this.redissonClient.getLock("lock" + pid);
        lock.lock();


//        再次判断缓存有没有数据
        String categoryVos02 = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
//        有数据直接返回
        if (!StringUtils.isBlank(categoryVos02)) {
//            使用fastJson工具，将字符串接卸为集合对象
            List<CategoryVo> categoryVoList = JSON.parseArray(categoryVos02, CategoryVo.class);
            lock.unlock();
            return categoryVoList;
        }
*/

//        没有数据读取mysql
        Resp<List<CategoryVo>> listResp = this.gmallPmsClient.querySubsCategories(pid);

//        1.解决穿透，不进行非空判断，当访问不同的数据时也放入缓存中
        List<CategoryVo> vos = listResp.getData();
//        将集合转化为字符串,然后再放入redis

//        2.解决雪崩，将放入缓存中的数据设置不同的过期时间
/*        this.redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(vos), new Random().nextInt(5), TimeUnit.DAYS);
//        释放锁
        lock.unlock();*/
        return vos;

    }

}
