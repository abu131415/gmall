package com.atguigu.gmallauth.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.utils.CookieUtils;
import com.atguigu.core.utils.JwtUtils;
import com.atguigu.gmallauth.config.JwtConfiguration;
import com.atguigu.gmallauth.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("auth")
@EnableConfigurationProperties(JwtConfiguration.class)
public class AuthController {

    @Autowired
    private JwtConfiguration jwtConfiguration;

    @Autowired
    private AuthService authService;

    @PostMapping("accredit")
    public Resp<Object> authentication(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
//        登陆校验
        String token = this.authService.authentication(username, password);
        if (StringUtils.isBlank(token)) {
            return Resp.fail("登陆失败，用户名或密码有误");
        }
//        设置cookie信息
        CookieUtils.setCookie(request, response, jwtConfiguration.getCookieName(), token, jwtConfiguration.getExpire() * 60);
        return Resp.ok(null);
    }

}
