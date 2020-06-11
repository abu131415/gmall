package com.atguigu.gmall.ums.service.impl;

import com.atguigu.core.exception.MemberException;
import com.atguigu.gmall.ums.utils.RandomNum;
import org.apache.commons.codec.cli.Digest;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.ums.dao.MemberDao;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.service.MemberService;

import javax.servlet.http.Cookie;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {


    private static final String prefix = "user:register:";
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {

        QueryWrapper<MemberEntity> queryWrapper = new QueryWrapper<>();
        switch (type) {
            case 1:
                queryWrapper.eq("username", data);
                break;
            case 2:
                queryWrapper.eq("mobile", data);
                break;
            case 3:
                queryWrapper.eq("email", data);
                break;
            default:
                return false;
        }
        return this.count(queryWrapper) == 0;
    }

    @Override
    public void register(MemberEntity memberEntity, String code) {
//        校验验证码
        String redisCode = redisTemplate.opsForValue().get(prefix + memberEntity.getMobile());
        if (!StringUtils.equals(redisCode, code)) {
            return;
        }
//        生成盐
        String salt = UUID.randomUUID().toString().substring(0, 5);
        memberEntity.setSalt(salt);
//        加盐加密
        String md5HexPassword = DigestUtils.md5Hex(memberEntity.getPassword() + salt);
//        新增用户
        memberEntity.setPassword(md5HexPassword);
        memberEntity.setCreateTime(new Date());
        memberEntity.setLevelId(0L);
        memberEntity.setStatus(1);
        memberEntity.setGrowth(0);
        memberEntity.setIntegration(0);
        this.save(memberEntity);
//        删除验证码
        this.redisTemplate.delete(prefix + memberEntity.getMobile());
    }

    @Override
    public void checkVerifyCode(String phone) {
        HashMap<String, String> map = new HashMap<>();
//        获取验证码
        String code = RandomNum.getRandom(6);
        map.put("phone", phone);
        map.put("code", code);
//        执行发送验证码
        this.rabbitTemplate.convertAndSend("GMALL.MESSAGE.USER.EXCHANGE", "SMS.USER", map);
    }

    @Override
    public MemberEntity queryMember(String username, String password) {
//        根据用户名查询用户
        MemberEntity entity = this.getOne(new QueryWrapper<MemberEntity>().eq("username", username));
//        判断用户是否存在,如果用户不存在
        if (entity == null) {
            throw new MemberException("用户名有误！");
        }
//        对密码进行加盐加密
        String md5HexPassword = DigestUtils.md5Hex(password + entity.getSalt());

//        判断密码是否一致
        if (!StringUtils.equals(md5HexPassword, entity.getPassword())) {
//        如果不一致
            throw new MemberException("密码有误！");
        }
//        如果一致
        return entity;

    }

}