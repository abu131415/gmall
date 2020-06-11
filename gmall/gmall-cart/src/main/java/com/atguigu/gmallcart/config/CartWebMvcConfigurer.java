package com.atguigu.gmallcart.config;

import com.atguigu.gmallcart.intercepter.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class CartWebMvcConfigurer implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        拦截访问购物车的所有请求
        registry.addInterceptor(interceptor).addPathPatterns("/**");
    }

}
