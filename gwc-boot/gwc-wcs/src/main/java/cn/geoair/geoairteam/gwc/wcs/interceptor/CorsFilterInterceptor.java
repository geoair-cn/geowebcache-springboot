package cn.geoair.geoairteam.gwc.wcs.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author ：zfj
 * @date ：Created in 2025/1/24 09:59
 * @description： TODO
 */
@Component
public class CorsFilterInterceptor implements HandlerInterceptor {
    private void setCorsFilter(HttpServletRequest request, HttpServletResponse response) {
        String join = StrUtil.join(",", ServletUtil.getHeaderMap(request).keySet());
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE,PUT");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "authorization,appId,appSecret,user-login-token,use-static-tile,X-Requested-With,content-type " + "," + join);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        setCorsFilter(request, response);
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }
        return true;
    }
}
