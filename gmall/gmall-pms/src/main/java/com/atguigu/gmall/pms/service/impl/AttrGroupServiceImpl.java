package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.dao.ProductAttrValueDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import org.springframework.beans.BeanUtils;
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

import com.atguigu.gmall.pms.dao.AttrGroupDao;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    private ProductAttrValueDao attrValueDao;

    @Autowired
    private AttrAttrgroupRelationDao relationDao;

    @Autowired
    private AttrDao attrDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryGroupByPage(QueryCondition queryCondition, Long catId) {

        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        if (catId != null) {
            wrapper.eq("catelog_id", catId);
        }

        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(queryCondition),
                wrapper
        );

        return new PageVo(page);
    }

    @Override
    public GroupVo queryGroupWithAttrsById(Long gid) {
//        查询组
        GroupVo groupVo = new GroupVo();
        AttrGroupEntity groupEntity = this.getById(gid);
        BeanUtils.copyProperties(groupEntity, groupVo);
//        查询关联关系对的集合
        List<AttrAttrgroupRelationEntity> relationEntities = this.relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", gid));
        if (CollectionUtils.isEmpty(relationEntities)) {
            return groupVo;
        }
        groupVo.setRelations(relationEntities);
//        根据attrIds查询所有的规格参数
//        使用stream().map表达式，通过一个对象集合获取对象中的某一个属性的集合
        List<Long> attrIds = relationEntities.stream().map(attrAttrgroupRelationEntity -> attrAttrgroupRelationEntity.getAttrId()).collect(Collectors.toList());
        List<AttrEntity> attrEntities = this.attrDao.selectBatchIds(attrIds);
        groupVo.setAttrEntities(attrEntities);

        return groupVo;
    }

    @Override
    public List<GroupVo> queryGroupWithAttrsByCid(Long catId) {

//        根据cid查询三级分类下的所有属性分组
        List<AttrGroupEntity> entityList = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catId));
//        调用queryGroupWithAttrsById方法查询，返回GroupVo对象，并将GroupVo对象组成一个List集合返回
        return entityList.stream().map(attrGroupEntity ->
                this.queryGroupWithAttrsById(attrGroupEntity.getAttrGroupId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemGroupVo> queryItemGroupVoByCidAndSpuId(Long cid, Long spuId) {

        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", cid));
        return attrGroupEntities.stream().map(attrGroupEntity -> {

            ItemGroupVo itemGroupVo = new ItemGroupVo();
            itemGroupVo.setName(attrGroupEntity.getAttrGroupName());
//            查询规格参数组及其值
            List<AttrAttrgroupRelationEntity> relationEntities = this.relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrGroupEntity.getAttrGroupId()));
            List<Long> attrIds = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
            List<ProductAttrValueEntity> productAttrValueEntityList = this.attrValueDao.selectList(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId).in("attr_id", attrIds));

            itemGroupVo.setBaseAttrs(productAttrValueEntityList);
            return itemGroupVo;
        }).collect(Collectors.toList());
    }

}