package com.atguigu.gmall.sms.dao;

import com.atguigu.gmall.sms.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author abu
 * @email 824335342@qq.com
 * @date 2020-02-24 11:10:18
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
