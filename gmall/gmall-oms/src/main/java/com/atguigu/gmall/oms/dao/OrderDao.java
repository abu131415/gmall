package com.atguigu.gmall.oms.dao;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author abu
 * @email 824335342@qq.com
 * @date 2020-03-09 09:07:28
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
