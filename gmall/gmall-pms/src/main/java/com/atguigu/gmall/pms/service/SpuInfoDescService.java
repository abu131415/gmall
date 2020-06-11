package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.SpuInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.SpuInfoDescEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * spu信息介绍
 *
 * @author abu
 * @email 824335342@qq.com
 * @date 2020-02-23 21:55:08
 */
public interface SpuInfoDescService extends IService<SpuInfoDescEntity> {

    PageVo queryPage(QueryCondition params);

    public void saveSpuInfoDesc(SpuInfoVo spuInfoVo, Long spuId);
}

