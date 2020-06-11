package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.SkuInfoDao;
import com.atguigu.gmall.pms.dao.SpuInfoDescDao;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallFeignClient;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.BaseAttrVo;
import com.atguigu.gmall.pms.vo.SkuInfoVo;
import com.atguigu.gmall.pms.vo.SpuInfoVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.xml.crypto.Data;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private AmqpTemplate amqpTemplate;


    @Value("${item.rabbitmq.exchange}")
    private String EXCHANGE_NAME;

    @Autowired
    private SpuInfoDescDao descDao;

    @Autowired
    private ProductAttrValueService attrValueService;

    @Autowired
    private SkuInfoDao skuInfoDao;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService saleAttrValueService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private GmallFeignClient gmallFeignClient;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo querySpuPage(QueryCondition condition, Long cid) {
//        SELECT * from pms_spu_info WHERE catalog_id =225 and( id =3 OR spu_name LIKE '%华为%')
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
//        cid!=0,表示分类则根据catalog_id查询
        if (cid != 0) {
            wrapper.eq("catalog_id", cid);
        }
//        id==0，全站id查询
        String key = condition.getKey();
//      判断关键字是否为空
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(t -> t.eq("id", key).or().like("spu_name", key));
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(condition),
                wrapper
        );

        return new PageVo(page);
    }

    @GlobalTransactional
    @Override
    public void BigSave(SpuInfoVo spuInfoVo) {
//      1.保存spu相关的三张表
//        1.1保存pms_spu_info信息

        Long spuId = saveSpuInfo(spuInfoVo);

//        1.2保存pms_spu_info_desc信息
        this.spuInfoDescService.saveSpuInfoDesc(spuInfoVo, spuId);

//        1.3保存pms_sku_sale_attr_value信息
        saveSkuSaleAttrValue(spuInfoVo, spuId);

//        int a = 1 / 0;


//        2.保存spu相关的信息

        List<SkuInfoVo> skus = spuInfoVo.getSkus();
        if (CollectionUtils.isEmpty(skus)) {
            return;
        }
        skus.forEach(skuInfoVo -> {
//            2.1保存pms_sku_info
            skuInfoVo.setSpuId(spuId);
            skuInfoVo.setSkuCode(UUID.randomUUID().toString());
            skuInfoVo.setBrandId(spuInfoVo.getBrandId());
            skuInfoVo.setCatalogId(spuInfoVo.getCatalogId());
            List<String> images = skuInfoVo.getImages();
//            设置默认图片
            if (!CollectionUtils.isEmpty(images)) {
                skuInfoVo.setSkuDefaultImg(StringUtils.isNotBlank(skuInfoVo.getSkuDefaultImg()) ? skuInfoVo.getSkuDefaultImg() : images.get(0));
            }
            this.skuInfoDao.insert(skuInfoVo);
            Long skuId = skuInfoVo.getSkuId();

//        2.2保存pms_sku_images
            if (!CollectionUtils.isEmpty(images)) {
                List<SkuImagesEntity> imagesEntities = images.stream().map(image -> {
                    SkuImagesEntity imagesEntity = new SkuImagesEntity();
                    imagesEntity.setImgUrl(image);
                    imagesEntity.setSkuId(skuId);
//                    设置默认图片
                    imagesEntity.setDefaultImg(StringUtils.equals(skuInfoVo.getSkuDefaultImg(), image) ? 1 : 0);
                    return imagesEntity;
                }).collect(Collectors.toList());
                this.skuImagesService.saveBatch(imagesEntities);
            }

//            2.3保存pms_sku_sale_attr_value
            List<SkuSaleAttrValueEntity> saleAttrs = skuInfoVo.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)) {
                for (SkuSaleAttrValueEntity saleAttr : saleAttrs) {
//                    设置skuId
                    saleAttr.setSkuId(skuId);
                }
                this.saleAttrValueService.saveBatch(saleAttrs);
            }

//        3保存营销信息中的三张表（使用feign远程调用）
            SkuSaleVo skuSaleVo = new SkuSaleVo();
            BeanUtils.copyProperties(skuInfoVo, skuSaleVo);
            skuSaleVo.setSkuId(skuId);
            gmallFeignClient.saveSale(skuSaleVo);
        });
//        消息发送spuId
        this.sendMsg("insert", spuId);

    }


    //    消息发送
    public void sendMsg(String type, Long spuId) {
        this.amqpTemplate.convertAndSend(EXCHANGE_NAME, "item." + type, spuId);
    }


    private void saveSkuSaleAttrValue(SpuInfoVo spuInfoVo, Long spuId) {
        List<BaseAttrVo> baseAttrs = spuInfoVo.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            List<ProductAttrValueEntity> productAttrValueEntityList = baseAttrs.stream().map(baseAttrVo -> {
                ProductAttrValueEntity attrValueEntity = baseAttrVo;
                attrValueEntity.setSpuId(spuId);
                return attrValueEntity;
            }).collect(Collectors.toList());
            this.attrValueService.saveBatch(productAttrValueEntityList);

        }
    }

    private Long saveSpuInfo(SpuInfoVo spuInfoVo) {
        spuInfoVo.setCreateTime(new Date());
        spuInfoVo.setUodateTime(spuInfoVo.getCreateTime());
        this.save(spuInfoVo);
        return spuInfoVo.getId();
    }

}