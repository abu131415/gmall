package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import lombok.Data;

import java.util.List;

@Data
public class GroupVo {

    private List<AttrEntity> attrEntities;
    private List<AttrAttrgroupRelationEntity> relations;

}
