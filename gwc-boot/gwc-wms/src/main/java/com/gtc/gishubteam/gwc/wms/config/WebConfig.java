package cn.geoair.geoairteam.gwc.wms.config;

import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * WebMvc配置
 */

@Configuration
public class WebConfig implements ApplicationRunner, WebMvcConfigurer {
	
	
	@Override
    public void addCorsMappings(CorsRegistry registry) {
        /*registry.addMapping("/**")
                .allowedOrigins("*")
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "DELETE", "PUT", "PATCH")
                .maxAge(3600 * 24);*/
    }
	
	@Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index");
        registry.addViewController("/doc").setViewName("forward:/doc.html");
    }
    
    //@Resource
    //ControllerInterceptor controllerInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    	//registry.addInterceptor(controllerInterceptor).addPathPatterns("/**").excludePathPatterns("/**/*.*");
    }
    
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		//converters.add(0,new DefaultJsonHttpMessageConverter());
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
    	
    }
}
