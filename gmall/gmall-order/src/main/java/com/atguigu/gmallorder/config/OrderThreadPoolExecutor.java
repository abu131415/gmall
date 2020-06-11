package com.atguigu.gmallorder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class OrderThreadPoolExecutor {
    @Bean
    public ThreadPoolExecutor getThreadPoolExecutor() {
        return new ThreadPoolExecutor(20, 500, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000));
    }
}
