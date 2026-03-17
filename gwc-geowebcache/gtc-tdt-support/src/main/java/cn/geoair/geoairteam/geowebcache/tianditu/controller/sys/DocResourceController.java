package cn.geoair.geoairteam.geowebcache.tianditu.controller.sys;

import java.io.IOException;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

/**
 * @description: 处理以 /doc 开头的请求，并将请求转发到 resource/static 目录下的资源
 * @author: zhang_jun
 * @create: 2023-10-20
 */
@Controller
@RequestMapping("/doc")
public class DocResourceController {

    private final ResourceHttpRequestHandler resourceHandler;

    public DocResourceController() throws Exception {
        this.resourceHandler = new ResourceHttpRequestHandler();

        // 设置资源位置
        this.resourceHandler.setLocations(
                Collections.singletonList(new ClassPathResource("static/")));

        // 设置支持的HTTP方法
        this.resourceHandler.setSupportedMethods(HttpMethod.GET.name());

        // 显式初始化 ResourceResolverChain
        this.resourceHandler.setResourceResolvers(
                Collections.singletonList(new PathResourceResolver()));

        // 确保 ResourceResolverChain 被正确初始化
        this.resourceHandler.afterPropertiesSet();
    }

    @GetMapping(value = "/**", produces = MediaType.ALL_VALUE)
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            resourceHandler.handleRequest(request, response);
        } catch (Exception e) {
            // 增加日志记录以便排查问题
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found");
        }
    }
}
