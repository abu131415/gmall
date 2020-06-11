package com.atguigu.index.service.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVo;
import com.atguigu.index.service.feign.GmallPmsClient;
import com.atguigu.index.service.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("index")
public class IndexController {

    @Autowired
    private IndexService indexService;

    @GetMapping("cates")
    public Resp<List<CategoryEntity>> queryCategoryLev01ByPid() {
        List<CategoryEntity> entities = indexService.queryCategoryLev01ByPid();
        return Resp.ok(entities);
    }

    @GetMapping("cates/{pid}")
    public Resp<List<CategoryVo>> querySbusCategories(@PathVariable("pid") Long pid) {
        List<CategoryVo> categoryVos = this.indexService.querySbusCategories(pid);
        return Resp.ok(categoryVos);
    }



}
