package com.atguigu.gmall.pms.api;

import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.CategoryVo;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {

    @GetMapping("pms/productattrvalue/{spuId}")
    Resp<List<ProductAttrValueEntity>> querySearchAttrValueBySpuId(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/skusaleattrvalue/{spuId}")
    Resp<List<SkuSaleAttrValueEntity>> querySkuSaleAttrValuesBySpuId(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/skusaleattrvalue/sku/{skuId}")
    Resp<List<SkuSaleAttrValueEntity>> querySkuSaleAttrValuesBySkuId(@PathVariable("skuId") Long skuId);

    @GetMapping("pms/spuinfodesc/info/{spuId}")
    Resp<SpuInfoDescEntity> queryDescBySpuId(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/skuinfo/{spuId}")
    Resp<List<SkuInfoEntity>> querySkusBySpuId(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/spuinfo/info/{id}")
    Resp<SpuInfoEntity> querySpuById(@PathVariable("id") Long id);

    @PostMapping("pms/spuinfo/page")
    Resp<List<SpuInfoEntity>> querySpusByPage(@RequestBody QueryCondition queryCondition);

    @GetMapping("pms/attrgroup/item/group/{cid}/{spuId}")
    Resp<List<ItemGroupVo>> queryItemGroupVoByCidAndSpuId(@PathVariable("cid") Long cid, @PathVariable("spuId") Long spuId);

    @GetMapping("pms/skuimages/{skuId}")
    Resp<List<SkuImagesEntity>> querySkuImagesBySkuId(@PathVariable("skuId") Long skuId);

    @GetMapping("pms/skuinfo/info/{skuId}")
    Resp<SkuInfoEntity> querySkuById(@PathVariable("skuId") Long skuId);

    @GetMapping("pms/brand/info/{brandId}")
    Resp<BrandEntity> queryBrandById(@PathVariable("brandId") Long brandId);

    @GetMapping("pms/category/info/{catId}")
    Resp<CategoryEntity> queryCategoryById(@PathVariable("catId") Long catId);

    @GetMapping("pms/category")
    Resp<List<CategoryEntity>> queryCategoryEntityByIdOrLevel(
            @RequestParam(value = "level", defaultValue = "0") Integer level,
            @RequestParam(value = "parentCid", required = false) Long pid
    );

    @GetMapping("pms/category/{pid}")
    Resp<List<CategoryVo>> querySubsCategories(@PathVariable("pid") Long pid);


}
