package com.atguigu.gmall.wms.service.impl;

import com.atguigu.gmall.wms.vo.SkuLockVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.wms.dao.WareSkuDao;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;
import org.springframework.util.CollectionUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {


    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private WareSkuDao wareSkuDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageVo(page);
    }


    @Override
    public String checkAndLockStore(List<SkuLockVo> skuLockVos) {
        if (CollectionUtils.isEmpty(skuLockVos)) {
            return "没有选中商品";
        }
        skuLockVos.forEach(skuLockVo -> {
            this.lockStore(skuLockVo);
        });

        List<SkuLockVo> unLockVoList = skuLockVos.stream().filter(skuLockVo -> skuLockVo.getLock() == false).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(unLockVoList)) {
//            解锁已经锁定商品的库存
            List<SkuLockVo> lockSku = skuLockVos.stream().filter(SkuLockVo::getLock).collect(Collectors.toList());
            lockSku.forEach(skuLockVo -> {
                this.wareSkuDao.unLockStore(skuLockVo.getWareSkuId(),skuLockVo.getCount());
            });

//            提示锁定失败的商品
            List<Long> skuIds = unLockVoList.stream().map(SkuLockVo::getSkuId).collect(Collectors.toList());
            return "下单失败，库存不足：" + skuIds.toString();
        }
        return null;
    }



    public void lockStore(SkuLockVo skuLockVo) {
        RLock lock = this.redissonClient.getLock("stock" + skuLockVo.getSkuId());
//        加锁
        lock.lock();

//        检验库存够不够
        List<WareSkuEntity> wareSkuEntities = this.wareSkuDao.checkStore(skuLockVo.getSkuId(), skuLockVo.getCount());
        if (!CollectionUtils.isEmpty(wareSkuEntities)) {
//        锁定库存信息
            Long wareSkuId = wareSkuEntities.get(0).getId();
            this.wareSkuDao.lockStore(wareSkuId, skuLockVo.getCount());
            skuLockVo.setLock(true);
            skuLockVo.setWareSkuId(wareSkuId);
        } else {
            skuLockVo.setLock(false);
        }

//        释放锁
        lock.unlock();
    }

}