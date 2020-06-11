package com.atguigu.gmall.wms.dao;

import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 *
 * @author abu
 * @email 824335342@qq.com
 * @date 2020-02-26 09:29:54
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    List<WareSkuEntity> checkStore(@Param("skuId") Long skuId, @Param("count") Integer count);

    void lockStore(@Param("wareSkuId") Long wareSkuId, @Param("count") Integer count);

    void unLockStore(@Param("wareSkuId") Long wareSkuId, @Param("count") Integer count);
}
