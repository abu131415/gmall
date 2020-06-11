package com.atguigu.gmallcart.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmallcart.pojo.Cart;
import com.atguigu.gmallcart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping
    public Resp<Object> addCart(@RequestBody Cart cart) {
        this.cartService.addCart(cart);
        return Resp.ok(null);
    }

    @GetMapping
    public Resp<List<Cart>> queryCart() {
        List<Cart> carts = this.cartService.queryCart();
        return Resp.ok(carts);
    }

    @PostMapping("update")
    public Resp<Object> updateCart(@RequestBody Cart cart) {
        this.cartService.updateCart(cart);
        return Resp.ok(null);
    }

    @DeleteMapping("delete/{skuId}")
    public Resp<Object> deleteCart(@PathVariable("skuId") Long skuId) {
        this.cartService.deleteCart(skuId);
        return Resp.ok(null);
    }

    @GetMapping("{userId}")
    public Resp<List<Cart>> queryCartByUserId(@PathVariable("userId") Long userId) {
        List<Cart> carts = this.cartService.queryCartById(userId);
        return Resp.ok(carts);
    }


}
