package com.atguigu.gmallauth.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.utils.JwtUtils;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmallauth.feign.GmallUmsClient;
import com.atguigu.gmallauth.config.JwtConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class AuthService {

    @Autowired
    private JwtConfiguration jwtConfiguration;
    @Autowired
    private GmallUmsClient gmallUmsClient;

    public String authentication(String username, String password) {
//        远程调用
        Resp<MemberEntity> entityResp = this.gmallUmsClient.queryMember(username, password);
        MemberEntity memberEntity = entityResp.getData();
//        校验用户是否存在
//        如果不存在
        if (memberEntity == null) {
            return null;
        }
        String token = null;
        try {
//        制作jwt
            HashMap<String, Object> map = new HashMap<>();
            map.put("id", memberEntity.getId());
            map.put("username", memberEntity.getUsername());
            token = JwtUtils.generateToken(map, jwtConfiguration.getPrivateKey(), jwtConfiguration.getExpire());
//        设置token信息
        } catch (Exception e) {
            e.printStackTrace();
        }

        return token;
    }
}
