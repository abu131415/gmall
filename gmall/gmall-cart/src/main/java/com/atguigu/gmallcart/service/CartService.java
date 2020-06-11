package com.atguigu.gmallcart.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.SaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmallcart.feign.GmallPmsClient;
import com.atguigu.gmallcart.feign.GmallSmsClient;
import com.atguigu.gmallcart.feign.GmallWmsClient;
import com.atguigu.gmallcart.intercepter.LoginInterceptor;
import com.atguigu.gmallcart.pojo.Cart;
import com.atguigu.core.bean.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "GMALL:CART:";
    private static final String PRICE_PREFIX = "GMALL:SKU:";

    /**
     * 新增购物车
     *
     * @param cart
     */
    public void addCart(Cart cart) {

        String key = getCartState();
//        获取购物车
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        String skuId = cart.getSkuId().toString();
        Integer cartCount = cart.getCount();

//        判断购物车中是否有记录
        if (hashOps.hasKey(skuId)) {
//        有记录则更新
//            获取购物车信息
            String cartJson = hashOps.get(skuId).toString();
//            将json反序列化
            cart = JSON.parseObject(cartJson, Cart.class);
//            增加购物车记录
            cart.setCount(cart.getCount() + cartCount);

        } else {
//        无记录则新增
//            添加sku有关信息
            cart.setIsCheck(true);
            Resp<SkuInfoEntity> infoEntityResp = this.pmsClient.querySkuById(cart.getSkuId());
            SkuInfoEntity skuInfoEntity = infoEntityResp.getData();
            if (skuInfoEntity == null) {
                return;
            }
            cart.setDefaultImage(skuInfoEntity.getSkuDefaultImg());
            cart.setPrice(skuInfoEntity.getPrice());
            cart.setTitle(skuInfoEntity.getSkuTitle());
//            添加库存有关信息
            Resp<List<WareSkuEntity>> listResp = this.wmsClient.queryWareSkusBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = listResp.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
            }

//            添加销售信息
            Resp<List<SaleVo>> saleVos = this.smsClient.querySalesBySkuId(cart.getSkuId());
            List<SaleVo> saleVoList = saleVos.getData();
            if (CollectionUtils.isEmpty(saleVoList)) {
                return;
            }
            cart.setSaleVos(saleVoList);
//            添加销售属性
            Resp<List<SkuSaleAttrValueEntity>> skuSaleAttrValues = this.pmsClient.querySkuSaleAttrValuesBySkuId(cart.getSkuId());
            List<SkuSaleAttrValueEntity> skuSaleAttrValueEntityList = skuSaleAttrValues.getData();

            cart.setSkuAttrValue(skuSaleAttrValueEntityList);
//            新增购物车时同步价格到缓存中一份

            this.redisTemplate.opsForValue().set(PRICE_PREFIX + skuId, skuInfoEntity.getPrice().toString());
        }
//        存入redis
        hashOps.put(skuId, JSON.toJSONString(cart));
    }

    private String getCartState() {
        String key = KEY_PREFIX;
//        获取登录状态
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if (userInfo.getId() != null) {
            key += userInfo.getId();
        } else {
            key += userInfo.getUserKey();
        }
        return key;
    }

    /**
     * 查询购物车
     *
     * @return
     */
    public List<Cart> queryCart() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
//        根据userKey查询未登录购物车的记录
        List<Cart> userKeyCarts = null;
        String userKey = KEY_PREFIX + userInfo.getUserKey();
        BoundHashOperations<String, Object, Object> unLoginHashOps = this.redisTemplate.boundHashOps(userKey);
        List<Object> cartJsonList = unLoginHashOps.values();
        if (!CollectionUtils.isEmpty(cartJsonList)) {
            userKeyCarts = cartJsonList.stream().map(cartJson -> {

                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                String currentPrice = this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
//                同步价格
                cart.setCurrentPrice(new BigDecimal(currentPrice));
                return cart;
            }).collect(Collectors.toList());
        }
//        判断用户是否登陆，未登陆直接返回
        if (userInfo.getId() == null) {
            return userKeyCarts;
        }
//        已登陆查询未登录时是否有记录
        String key = this.KEY_PREFIX + userInfo.getId();
        BoundHashOperations<String, Object, Object> loginHashOps = this.redisTemplate.boundHashOps(key);
        if (!CollectionUtils.isEmpty(userKeyCarts)) {
//        未登录的有记录
            userKeyCarts.forEach(cart -> {
                Long skuId = cart.getSkuId();
                Integer count = cart.getCount();
//                如果有相同购物车记录则合并
                if (loginHashOps.hasKey(skuId.toString())) {
                    String cartJson = loginHashOps.get(skuId.toString()).toString();
                    cart = JSON.parseObject(cartJson, Cart.class);
                    cart.setCount(cart.getCount() + count);
                }
//                如果没有相应记录则新增购物车
                loginHashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
            });
//          删除缓存中的购物车
            this.redisTemplate.delete(userKey);
        }
//        查询登陆状态下的记录
        List<Object> jsonObjectCarts = loginHashOps.values();
        return jsonObjectCarts.stream().map(userKeyCart -> {
            Cart cart = JSON.parseObject(userKeyCart.toString(), Cart.class);
            String currentPrice = this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
//            同当前价格
            cart.setCurrentPrice(new BigDecimal(currentPrice));
            return cart;
        }).collect(Collectors.toList());
    }


    /**
     * 更新购物车
     *
     * @param cart
     */
    public void updateCart(Cart cart) {
//        获取登陆的状态
        String key = this.getCartState();
//        获取操作对象
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
//        获取要更新的记录
        Integer count = cart.getCount();
//        判断当前要更新的购物车是否有与之相应的购物车记录
        if (hashOps.hasKey(cart.getSkuId().toString())) {
            String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(count);
//            放入缓存中
            hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
        }
    }

    /**
     * 删除购物车
     *
     * @param skuId
     */
    public void deleteCart(Long skuId) {
//        获取用户的id
        String key = this.getCartState();
//        获取操作对象
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
//        判断用户是否有对应的购物车记录
        if (hashOps.hasKey(skuId.toString())) {
            hashOps.delete(skuId.toString());
        }

    }

    public List<Cart> queryCartById(Long userId) {
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        List<Object> values = hashOps.values();
        return values.stream()
                .map(cartJson -> JSON.parseObject(cartJson.toString(), Cart.class))
                .filter(Cart::getIsCheck).collect(Collectors.toList());
    }
}
