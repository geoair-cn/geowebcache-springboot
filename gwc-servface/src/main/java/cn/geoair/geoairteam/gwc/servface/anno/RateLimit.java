package cn.geoair.geoairteam.gwc.servface.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    int limit() default 1;

    int period() default 1;  //  默认1秒钟限制请求一次

    String message() default "";
}
