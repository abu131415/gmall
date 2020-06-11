package com.atguigu.gmallcart.intercepter;

import com.atguigu.core.utils.CookieUtils;
import com.atguigu.core.utils.JwtUtils;
import com.atguigu.gmallcart.config.JwtConfiguration;
import com.atguigu.core.bean.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@Component
@EnableConfigurationProperties(JwtConfiguration.class)
public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private JwtConfiguration jwtConfiguration;

    private static ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 在购物车微服务中方法执行之前进行登录校验
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//       创建一个userInfo存储数据
        UserInfo userInfo = new UserInfo();

//        获取cookie中token的信息，以及userKey信息
        String token = CookieUtils.getCookieValue(request, jwtConfiguration.getCookieName());
        String userKey = CookieUtils.getCookieValue(request, jwtConfiguration.getUserKeyName());
//        判断有没有userKey信息，没有的话制作一个
        if (StringUtils.isBlank(userKey)) {
            userKey = UUID.randomUUID().toString();
            CookieUtils.setCookie(request, response, jwtConfiguration.getUserKeyName(), userKey, 43200);
        }

//        设置游客的唯一标识
        userInfo.setUserKey(userKey);

//        判断token信息存在
        if (StringUtils.isNotBlank(token)) {
//        解析token信息
            Map<String, Object> info = JwtUtils.getInfoFromToken(token, jwtConfiguration.getPublicKey());
//            设置id
            userInfo.setId(Long.parseLong(info.get("id").toString()));
        }

//         放入ThreadLocal变量中
        THREAD_LOCAL.set(userInfo);

        return super.preHandle(request, response, handler);
    }

    public static UserInfo getUserInfo() {
        return THREAD_LOCAL.get();
    }


    /**
     * 当视图渲染结束后需手动将ThreadLocal释放掉
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        当一个请求请求tomcat时，tomcat线程池会取一个线程去执行请求，当请求执行结束后，会将该线程归还到tomcat线程中，等待其他请求
//        （注意ThreadLocal会等到 线程 结束后会自动被垃圾回收）
//         此时tomcat线程池中的该线程并没有结束，只是归还到tomcat线程池中了，所以必须显式的释放ThreadLocal变量
        THREAD_LOCAL.remove();
    }
}
