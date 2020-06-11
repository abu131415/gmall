package com.atguigu.gmall.oms.vo;

import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderSubmitVo {

    private String orderToken;      //订单唯一标识

    private String deliverCompany;  //运送方式

    private String payType;     //支付方式

    private MemberReceiveAddressEntity address;

    private List<OrderItemVo> items;        //商品信息

    private BigDecimal totalPrice;      //订单总价格

    private Integer bounds;     //积分信息

    private Long userId;

}
