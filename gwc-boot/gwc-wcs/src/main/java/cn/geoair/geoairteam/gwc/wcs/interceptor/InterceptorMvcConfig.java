package cn.geoair.geoairteam.gwc.wcs.interceptor;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @description: 拦截器配置
 * @author: zhang_jun
 * @create: 2021-05-27 14:02
 **/
@Configuration
public class InterceptorMvcConfig implements WebMvcConfigurer {

    @Resource
    private RateLimitInterceptor rateLimitInterceptor;
    @Resource
    private CorsFilterInterceptor corsFilterInterceptor;

    @Resource
    private RepeatSubmitInterceptor repeatSubmitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor);
        registry.addInterceptor(corsFilterInterceptor);
        registry.addInterceptor(repeatSubmitInterceptor).addPathPatterns("/**" );
    }


}
