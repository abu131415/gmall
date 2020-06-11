package com.atguigu.gmallpay.controller;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.atguigu.gmallpay.config.AlipayTemplate;
import com.atguigu.gmallpay.config.PayAsyncVo;
import com.atguigu.gmallpay.config.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PayController {

    @Autowired
    private AlipayTemplate alipayTemplate;


    @GetMapping("hello")
    public String getData() {
        return "hello";
    }

    @PostMapping("pay")
    public ResponseEntity<String> aliPay(@RequestBody PayVo payVo) {

        String payHtml = null;
        try {
            payHtml = this.alipayTemplate.pay(payVo);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(payHtml);
    }

    @PostMapping("pay/success")
    public ResponseEntity<String> getNotify(PayAsyncVo payAsyncVo) {
        String payAsyncMsg = JSON.toJSONString(payAsyncVo);
        System.out.println(payAsyncMsg);
        return ResponseEntity.ok(null);
    }
}
