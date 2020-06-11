package com.atguigu.gmallitem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor getThreadPoolExecutor() {
        return new ThreadPoolExecutor(40, 500, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000));
    }
}
