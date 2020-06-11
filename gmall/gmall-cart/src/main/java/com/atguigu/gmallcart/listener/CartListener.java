package com.atguigu.gmallcart.listener;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmallcart.feign.GmallPmsClient;
import io.lettuce.core.dynamic.annotation.Key;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CartListener {

    private static final String PRICE_PREFIX = "GMALL:SKU:";

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "CART.ITEM.QUEUE", durable = "true"),
            exchange = @Exchange(value = "GMALL-PMS-EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"item.update"}
    ))
    public void priceListener(Long spuId) {
        Resp<List<SkuInfoEntity>> listResp = this.pmsClient.querySkusBySpuId(spuId);
        List<SkuInfoEntity> skuInfoEntities = listResp.getData();
        skuInfoEntities.forEach(skuInfoEntity -> {
            this.redisTemplate.opsForValue().set(PRICE_PREFIX + skuInfoEntity.getSkuId(), skuInfoEntity.getPrice().toString());
        });
    }
}
