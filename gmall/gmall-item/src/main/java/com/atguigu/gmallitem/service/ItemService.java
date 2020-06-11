package com.atguigu.gmallitem.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.sms.api.GmallSmsApi;
import com.atguigu.gmall.sms.vo.SaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmallitem.feign.GmallPmsClient;
import com.atguigu.gmallitem.feign.GmallSmsClient;
import com.atguigu.gmallitem.feign.GmallWmsClient;
import com.atguigu.gmallitem.vo.ItemVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ItemService {

    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private GmallWmsClient gmallWmsClient;

    @Autowired
    private GmallSmsClient gmallSmsClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    public ItemVo queryItemVo(Long skuId) {
        ItemVo itemVo = new ItemVo();

        itemVo.setSkuId(skuId);
//        根据skuId查询sku
        CompletableFuture<Object> skuCompletableFuture = CompletableFuture.supplyAsync(() -> {
            Resp<SkuInfoEntity> skuResp = this.gmallPmsClient.querySkuById(skuId);
            SkuInfoEntity skuInfoEntity = skuResp.getData();
            if (skuInfoEntity == null) {
                return itemVo;
            }
            itemVo.setSkuTitle(skuInfoEntity.getSkuTitle());
            itemVo.setSubTitle(skuInfoEntity.getSkuSubtitle());
            itemVo.setPrice(skuInfoEntity.getPrice());
            itemVo.setWeight(skuInfoEntity.getWeight());
            itemVo.setSpuId(skuInfoEntity.getSpuId());
//        根据sku中的spuId查询spu
//        获取spuId
            return skuInfoEntity;
        }, threadPoolExecutor);


        CompletableFuture<Void> spuCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {

            Resp<SpuInfoEntity> spuResp = this.gmallPmsClient.querySpuById(((SkuInfoEntity) skuEntity).getSpuId());
            SpuInfoEntity spuInfoEntity = spuResp.getData();
            if (spuInfoEntity != null) {
                itemVo.setSpuName(spuInfoEntity.getSpuName());
            }
        }, threadPoolExecutor);


//        根据skuId查询图片列表
        CompletableFuture<Void> imagesCompletableFuture = CompletableFuture.runAsync(() -> {

            Resp<List<SkuImagesEntity>> imagesResp = this.gmallPmsClient.querySkuImagesBySkuId(skuId);
            List<SkuImagesEntity> imagesEntities = imagesResp.getData();
            itemVo.setPics(imagesEntities);
        }, threadPoolExecutor);


//        根据sku中brandId和categoryId查询品牌和分类
        CompletableFuture<Void> brandCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            Resp<BrandEntity> brandResp = this.gmallPmsClient.queryBrandById(((SkuInfoEntity) skuEntity).getBrandId());
            BrandEntity brandEntity = brandResp.getData();
            itemVo.setBrandEntity(brandEntity);
        }, threadPoolExecutor);

        CompletableFuture<Void> categoryCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            Resp<CategoryEntity> categoryResp = this.gmallPmsClient.queryCategoryById(((SkuInfoEntity) skuEntity).getCatalogId());
            CategoryEntity categoryEntity = categoryResp.getData();
            itemVo.setCategoryEntity(categoryEntity);
        }, threadPoolExecutor);


//       根据skuId查询营销信息
        CompletableFuture<Void> saleCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<List<SaleVo>> salesResp = this.gmallSmsClient.querySalesBySkuId(skuId);
            List<SaleVo> saleVoList = salesResp.getData();
            itemVo.setSales(saleVoList);
        }, threadPoolExecutor);

//        根据skuId查询所有的库存信息
        CompletableFuture<Void> stockCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<List<WareSkuEntity>> wareResp = this.gmallWmsClient.queryWareSkusBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = wareResp.getData();
            itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
        }, threadPoolExecutor);

//        根据spuId查询所有的skuIds，再去查询所有的销售属性
        CompletableFuture<Void> saleAttrCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            Resp<List<SkuSaleAttrValueEntity>> saleAttrValueResp = this.gmallPmsClient.querySkuSaleAttrValuesBySpuId(((SkuInfoEntity) skuEntity).getSpuId());
            List<SkuSaleAttrValueEntity> saleAttrValueEntities = saleAttrValueResp.getData();
            itemVo.setSaleAttrs(saleAttrValueEntities);
        }, threadPoolExecutor);

//        根据spuId查询商品描述
        CompletableFuture<Void> descCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            Resp<SpuInfoDescEntity> descEntityResp = this.gmallPmsClient.queryDescBySpuId(((SkuInfoEntity) skuEntity).getSpuId());
            SpuInfoDescEntity descEntity = descEntityResp.getData();
            if (descEntity != null) {
                String desc = descEntity.getDecript();
                String[] descArray = StringUtils.split(desc, ",");
                itemVo.setImages(Arrays.asList(descArray));
            }
        }, threadPoolExecutor);
//        根据spuId和categoryId查询组和组下的规格参数（带值）
        CompletableFuture<Void> groupCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            Resp<List<ItemGroupVo>> itemGroupResp = this.gmallPmsClient.queryItemGroupVoByCidAndSpuId(((SkuInfoEntity) skuEntity).getCatalogId(), ((SkuInfoEntity) skuEntity).getSpuId());
            List<ItemGroupVo> itemGroupVos = itemGroupResp.getData();
            itemVo.setGroups(itemGroupVos);
        }, threadPoolExecutor);

//        等待所有任务完成，才返回
        CompletableFuture.allOf(
                spuCompletableFuture,
                imagesCompletableFuture,
                brandCompletableFuture,
                categoryCompletableFuture,
                descCompletableFuture,
                saleAttrCompletableFuture,
                saleCompletableFuture,
                stockCompletableFuture,
                groupCompletableFuture

        ).join();

        return itemVo;
    }

}
