package com.atguigu.gmallitem.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmallitem.service.ItemService;
import com.atguigu.gmallitem.vo.ItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping("{skuId}")
    public Resp<ItemVo> queryItemVo(@PathVariable("skuId") Long skuId) {
        ItemVo itemVo = this.itemService.queryItemVo(skuId);
        return Resp.ok(itemVo);
    }
}
