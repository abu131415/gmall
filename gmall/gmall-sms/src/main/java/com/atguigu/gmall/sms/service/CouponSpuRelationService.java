package com.atguigu.gmall.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.sms.entity.CouponSpuRelationEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 优惠券与产品关联
 *
 * @author abu
 * @email 824335342@qq.com
 * @date 2020-02-24 11:10:18
 */
public interface CouponSpuRelationService extends IService<CouponSpuRelationEntity> {

    PageVo queryPage(QueryCondition params);
}

