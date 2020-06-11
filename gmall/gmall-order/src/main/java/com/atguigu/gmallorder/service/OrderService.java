package com.atguigu.gmallorder.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.bean.UserInfo;
import com.atguigu.core.exception.OrderItemException;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.atguigu.gmallcart.pojo.Cart;
import com.atguigu.gmallorder.feign.*;
import com.atguigu.gmallorder.intercepter.LoginInterceptor;
import com.atguigu.gmallorder.vo.OrderConfirmVo;
import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GmallOmsClient omsClient;

    @Autowired
    private GmallUmsClient umsClient;

    @Autowired
    private GmallCartClient cartClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    private static final String TOKEN_PREFIX = "order:token:";

    public OrderConfirmVo confirm() {

        OrderConfirmVo confirmVo = new OrderConfirmVo();
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userInfoId = userInfo.getId();
        if (userInfoId == null) {
            return null;
        }

        CompletableFuture<Void> addressCompletableFuture = CompletableFuture.runAsync(() -> {

//        获取用户地址列表，根据用户id查询收货地址列表
            Resp<List<MemberReceiveAddressEntity>> listResp = this.umsClient.queryAddressByUserId(userInfoId);
            List<MemberReceiveAddressEntity> addressEntities = listResp.getData();
            confirmVo.setAddresses(addressEntities);
        }, threadPoolExecutor);

        CompletableFuture<Void> bigSkuCompletableFuture = CompletableFuture.supplyAsync(() -> {
//        获取商品列表
            Resp<List<Cart>> cartsResp = this.cartClient.queryCartByUserId(userInfoId);
            List<Cart> cartList = cartsResp.getData();
            if (CollectionUtils.isEmpty(cartList)) {
                throw new OrderItemException("请选中购物车商品");
            }
            return cartList;
        }, threadPoolExecutor).thenAcceptAsync(cartList -> {

            List<OrderItemVo> itemVoList = cartList.stream().map(cart -> {
                OrderItemVo itemVo = new OrderItemVo();
                Long skuId = cart.getSkuId();

                CompletableFuture<Void> skuCompletableFuture = CompletableFuture.runAsync(() -> {
                    Resp<SkuInfoEntity> infoEntityResp = this.pmsClient.querySkuById(skuId);
                    SkuInfoEntity skuInfoEntity = infoEntityResp.getData();
                    if (skuInfoEntity != null) {

                        itemVo.setDefaultImage(skuInfoEntity.getSkuDefaultImg());
                        itemVo.setWeight(skuInfoEntity.getWeight());
                        itemVo.setPrice(skuInfoEntity.getPrice());
                        itemVo.setTitle(skuInfoEntity.getSkuTitle());
                        itemVo.setSkuId(skuId);
                        itemVo.setCount(cart.getCount());
                    }
                }, threadPoolExecutor);

                CompletableFuture<Void> saleCompletableFuture = CompletableFuture.runAsync(() -> {
                    Resp<List<SkuSaleAttrValueEntity>> attrValuesResp = this.pmsClient.querySkuSaleAttrValuesBySkuId(skuId);
                    List<SkuSaleAttrValueEntity> attrValueEntities = attrValuesResp.getData();
                    itemVo.setSkuAttrValue(attrValueEntities);
                }, threadPoolExecutor);

                CompletableFuture<Void> wareCompletableFuture = CompletableFuture.runAsync(() -> {
                    Resp<List<WareSkuEntity>> wareSkusResp = this.wmsClient.queryWareSkusBySkuId(skuId);
                    List<WareSkuEntity> wareSkuEntities = wareSkusResp.getData();
                    if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                        itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
                    }
                }, threadPoolExecutor);

                return itemVo;
            }).collect(Collectors.toList());

            confirmVo.setOrderItem(itemVoList);
        }, threadPoolExecutor);


        CompletableFuture<Void> integrationCompletableFuture = CompletableFuture.runAsync(() -> {
//        获取积分
            Resp<MemberEntity> memberEntityResp = this.umsClient.queryMemberById(userInfoId);
            MemberEntity memberEntity = memberEntityResp.getData();
            confirmVo.setBounds(memberEntity.getIntegration());
        }, threadPoolExecutor);

        CompletableFuture<Void> idCompletableFuture = CompletableFuture.runAsync(() -> {
//        获取订单唯一标识，防止重复提（相应页面一份，redis中有一份）
//        使用雪花算法生成用户的唯一订单id
            String orderIdStr = IdWorker.getIdStr();
            confirmVo.setOrderToken(orderIdStr);
//            存入缓存中一份
            this.redisTemplate.opsForValue().set(TOKEN_PREFIX + orderIdStr, orderIdStr);
        }, threadPoolExecutor);

        //等待所有任务完成
        CompletableFuture.allOf(
                addressCompletableFuture,
                integrationCompletableFuture,
                idCompletableFuture,
                bigSkuCompletableFuture
        ).join();
        return confirmVo;
    }

    public void orderSubmit(OrderSubmitVo orderSubmitVo) {

        UserInfo userInfo = LoginInterceptor.getUserInfo();
//        1.防止重复提交，查询缓存中有没有orderToken信息，有的话则是第一次提交，放行并且删除缓存数据
//        获取tokeOrder
        String orderToken = orderSubmitVo.getOrderToken();
        // 1. 防重复提交，查询redis中有没有orderToken信息，有，则是第一次提交，放行并删除redis中的orderToken
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long flag = this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(TOKEN_PREFIX + orderToken), orderToken);
        if (flag == 0) {
            throw new OrderItemException("订单不可重复提交！");
        }

//        2.核对总价格是否正确，正确则放行
        List<OrderItemVo> items = orderSubmitVo.getItems();     //获取送货清单
        BigDecimal totalPrice = orderSubmitVo.getTotalPrice();
        if (CollectionUtils.isEmpty(items)) {
            throw new OrderItemException("没有购买商品，请先在购物车勾选商品");
        }
        BigDecimal currentTotalPrice = items.stream().map(item -> {
            Resp<SkuInfoEntity> infoEntityResp = this.pmsClient.querySkuById(item.getSkuId());
            SkuInfoEntity skuInfoEntity = infoEntityResp.getData();
            if (skuInfoEntity != null) {
                return skuInfoEntity.getPrice().multiply(new BigDecimal(item.getCount()));
            }
            return new BigDecimal(0);

        }).reduce((a, b) -> a.add(b)).get();
        if (currentTotalPrice.compareTo(totalPrice) != 0) {
            throw new OrderItemException("页面已经过期，请刷新页面重试！");
        }

//        3.检验库存中库存是否充足，一次性提示所有库存不足的商品信息
        List<SkuLockVo> skuLockVoList = items.stream().map(orderItemVo -> {
            SkuLockVo skuLockVo = new SkuLockVo();
            skuLockVo.setSkuId(orderItemVo.getSkuId());
            skuLockVo.setCount(orderItemVo.getCount());
            return skuLockVo;
        }).collect(Collectors.toList());
        Resp<Object> wareStore = this.wmsClient.checkAndLockStore(skuLockVoList);
        if (wareStore.getCode() != 0) {
            throw new OrderItemException(wareStore.getMsg());
        }

//        4.下单，即创建订单
        try {
            orderSubmitVo.setUserId(userInfo.getId());
            this.omsClient.saveOrder(orderSubmitVo);
        } catch (Exception e) {
            e.printStackTrace();
            throw new OrderItemException("服务器错误，创建订单失败");
        }
//        5.删除购物车（消息解耦和，保证删除购物车不影响当前业务的成功与否）

    }
}