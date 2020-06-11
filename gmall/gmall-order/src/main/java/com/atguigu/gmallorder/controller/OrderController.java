package com.atguigu.gmallorder.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmallorder.service.OrderService;
import com.atguigu.gmallorder.vo.OrderConfirmVo;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("confirm")
    public Resp<OrderConfirmVo> confirm() {
        OrderConfirmVo orderConfirmVo = this.orderService.confirm();
        return Resp.ok(orderConfirmVo);
    }

    @PostMapping("submit")
    public Resp<Object> submit(@RequestBody OrderSubmitVo orderSubmitVo) {
        this.orderService.orderSubmit(orderSubmitVo);
        return Resp.ok(null);
    }
}
