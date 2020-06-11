package com.atguigu.gmall.ums.dao;

import com.atguigu.gmall.ums.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author abu
 * @email 824335342@qq.com
 * @date 2020-03-04 13:09:41
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
