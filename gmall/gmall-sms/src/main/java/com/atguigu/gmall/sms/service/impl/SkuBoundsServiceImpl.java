package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.sms.dao.SkuFullReductionDao;
import com.atguigu.gmall.sms.dao.SkuLadderDao;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.vo.SaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.sms.dao.SkuBoundsDao;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import org.springframework.transaction.annotation.Transactional;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsDao, SkuBoundsEntity> implements SkuBoundsService {

    @Autowired
    private SkuLadderDao skuLadderDao;

    @Autowired
    private SkuFullReductionDao reductionDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SkuBoundsEntity> page = this.page(
                new Query<SkuBoundsEntity>().getPage(params),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageVo(page);
    }

    @Transactional
    @Override
    public void saveSale(SkuSaleVo skuSaleVo) {

//        3.1保存到sms_sku_bounds
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        skuBoundsEntity.setSkuId(skuSaleVo.getSkuId());
        skuBoundsEntity.setBuyBounds(skuSaleVo.getBuyBounds());
        skuBoundsEntity.setGrowBounds(skuSaleVo.getGrowBounds());
        List<Integer> works = skuSaleVo.getWork();
        skuBoundsEntity.setWork(works.get(0) + works.get(1) * 2 + works.get(2) * 4 + works.get(3) * 8);
        this.save(skuBoundsEntity);

//        3.2保存sms_sku_ladder
        SkuLadderEntity ladderEntity = new SkuLadderEntity();
        ladderEntity.setSkuId(skuSaleVo.getSkuId());
        ladderEntity.setAddOther(skuSaleVo.getLadderAddOther());
        ladderEntity.setDiscount(skuSaleVo.getDiscount());
        ladderEntity.setFullCount(skuSaleVo.getFullCount());
        skuLadderDao.insert(ladderEntity);

//        3.3保存sms_sku_full_reduction
        SkuFullReductionEntity reductionEntity = new SkuFullReductionEntity();
        reductionEntity.setSkuId(skuSaleVo.getSkuId());
        reductionEntity.setReducePrice(skuSaleVo.getReducePrice());
        reductionEntity.setAddOther(skuSaleVo.getFullAddOther());
        reductionEntity.setFullPrice(skuSaleVo.getFullPrice());
        this.reductionDao.insert(reductionEntity);
    }

    @Override
    public List<SaleVo> querySalesBySkuId(Long skuId) {

        List<SaleVo> saleVoList = new ArrayList<>();
//        查询积分信息
        SkuBoundsEntity boundsEntity = this.getOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id", skuId));
        if (boundsEntity != null) {
            SaleVo saleVo = new SaleVo();
            saleVo.setType("积分");
            StringBuffer sb = new StringBuffer();
            if (boundsEntity.getGrowBounds() != null && boundsEntity.getGrowBounds().intValue() > 0) {
                sb.append("成长积分送" + boundsEntity.getGrowBounds());
            }
            if (boundsEntity.getBuyBounds() != null && boundsEntity.getBuyBounds().intValue() > 0) {
                if (StringUtils.isNotBlank(sb)) {
                    sb.append(",");
                }
                sb.append("赠送积分送" + boundsEntity.getBuyBounds());
            }
            saleVo.setDesc(sb.toString());
            saleVoList.add(saleVo);
        }
//        查询打折
        SkuLadderEntity ladderEntity = this.skuLadderDao.selectOne(new QueryWrapper<SkuLadderEntity>().eq("sku_id", skuId));
        if (ladderEntity != null) {
            SaleVo ladderVo = new SaleVo();
            ladderVo.setType("打折");
            ladderVo.setDesc("满" + ladderEntity.getFullCount() + "件，打" + ladderEntity.getDiscount().divide(new BigDecimal(10)) + "折");
            saleVoList.add(ladderVo);
        }
//        查询满减
        SkuFullReductionEntity reductionEntity = this.reductionDao.selectOne(new QueryWrapper<SkuFullReductionEntity>().eq("sku_id", skuId));
        if (reductionEntity != null) {
            SaleVo reductionVo = new SaleVo();
            reductionVo.setType("满减");
            reductionVo.setDesc("满" + reductionEntity.getFullPrice() + "减" + reductionEntity.getReducePrice());
            saleVoList.add(reductionVo);
        }
        return saleVoList;
    }

}