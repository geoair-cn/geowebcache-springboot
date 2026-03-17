package cn.geoair.geoairteam.gwc.servface.anno;

import java.lang.annotation.*;

/**
 * 自定义注解防止表单重复提交
 *
 * @author ruoyi
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmit {
    /**
     * 间隔时间(ms)，小于此时间视为重复提交
     */
     int interval() default 800;

    /**
     * 提示消息
     */
     String message() default "不允许重复提交，请稍候再试";
}
