package com.atguigu.gmallcart.pojo;


import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.SaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Cart {

    private Long skuId;// 商品id
    private String title;// 标题
    private String defaultImage;// 图片
    private BigDecimal price;// 加入购物车时的价格
    private BigDecimal currentPrice;// 加入购物车时的价格
    private Boolean store;
    private Integer count;// 购买数量
    private List<SkuSaleAttrValueEntity> skuAttrValue;// 商品规格参数
    private List<SaleVo> saleVos;      //销售属性
    private Boolean isCheck;       //是否勾选
}