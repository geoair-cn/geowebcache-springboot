package cn.geoair.geoairteam.gwc.wcs.interceptor;

import cn.geoair.base.exception.GirException;
import cn.geoair.base.util.GutilObject;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;


import cn.geoair.geoairteam.gwc.servface.anno.RateLimit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * @author ：zfj
 * @date ：Created in 2023/7/4 9:44
 * @description： TODO
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private final RedisTemplate<String, Object> redisTemplate;
    private final AntPathMatcher pathMatcher;
    /**
     * 限流 redis key
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:" ;
    private static final String RATE_LIMIT_ERROR_MESSAGE = "访问频率过快" ;

    public RateLimitInterceptor(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.pathMatcher = new AntPathMatcher();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            RateLimit rateLimit = handlerMethod.getMethod().getAnnotation(RateLimit.class);
            // 获取请求的IP地址
            String ipAddress = request.getRemoteAddr();
            String requestURI = request.getRequestURI();
            if (rateLimit != null) {
                // 获取注解上的限制值
                int limit = rateLimit.limit();
                int period = rateLimit.period();
                // 设置IP地址的访问限制
                String key = RATE_LIMIT_KEY + String.format("%s + %s" , ipAddress, requestURI);
                key = DigestUtil.md5Hex(key);  // 通过请求地址和ip作为校验的key
                // 使用Redis来统计IP地址的请求次数
                ValueOperations<String, Object> ops = redisTemplate.opsForValue();
                Integer count = (Integer) ops.get(key);

                // 如果IP地址的请求次数超过限制次数，则限制访问
                if (count != null && count >= limit) {
                    String message = rateLimit.message();
                    if (GutilObject.isEmpty(message)) {
                        String temp = "该http接口限制{}秒内最多访问{}次！" ;
                        message = StrUtil.format(temp, period, limit);
                    }
                    throw new GirException(message);
                }
                // 增加IP地址的请求次数，并设置过期时间
                ops.increment(key, 1);
                redisTemplate.expire(key, period, TimeUnit.SECONDS);
            }


        }

        return true;

    }
}
