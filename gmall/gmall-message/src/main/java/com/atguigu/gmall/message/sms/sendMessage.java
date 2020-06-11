package com.atguigu.gmall.message.sms;

import com.aliyuncs.exceptions.ClientException;
import com.atguigu.gmall.message.utils.SendSms;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class sendMessage {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String prefix = "user:register:";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "GMALL.MESSAGE.USER", durable = "true"),
            exchange = @Exchange(value = "GMALL.MESSAGE.USER.EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"SMS.USER"}
    ))
    public void sendSms(Map<String, String> msg) throws ClientException {
        if (CollectionUtils.isEmpty(msg)) {
            return;
        }
//        获取验证码
        String code = msg.get("code");
        String phone = msg.get("phone");
//        发送验证码
        SendSms.sendSms(phone, code);
//        放入缓存中
        redisTemplate.opsForValue().set(prefix + phone, code, 5, TimeUnit.HOURS);
    }

}
