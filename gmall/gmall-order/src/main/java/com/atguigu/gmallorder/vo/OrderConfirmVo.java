package com.atguigu.gmallorder.vo;

import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import lombok.Data;

import java.util.List;


@Data
public class OrderConfirmVo {

    private List<MemberReceiveAddressEntity> addresses;

    private List<OrderItemVo> orderItem;        //商品列表
    private Integer bounds;     //积分

    private String orderToken;      //订单唯一标识，防止重复提交
}
