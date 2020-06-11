package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

// 在实际访问的域中进行配置
@Configuration
public class GmallCorsConfig {

    @Bean
//我们使用的网关是gateway是集成于webFlus,所以使用CorsWebFilter，如果使用时zull网关则要使用CorsFilter
    public CorsWebFilter getCorsWebFilter() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
//我们想通过发送http://localhost:1000请求，进行访问网关中的资源。
// 作用：允许该域进行跨域访问网关的资源
       configuration.addAllowedOrigin("http://localhost:1000");
        configuration.addAllowedMethod("*");    //允许所有的请求方式
        configuration.addAllowedHeader("*");    //允许携带头信息
        configuration.setAllowCredentials(true);      //允许携带cookie

//  注意：导入  import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**", configuration);     //对所有请求进行检查
        return new CorsWebFilter(configurationSource);
    }

}
