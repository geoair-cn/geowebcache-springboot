package cn.geoair.geoairteam.gwc.wcs.config;


import cn.geoair.map.dynamic.adv.query.IAdvExecutor;
import cn.geoair.map.dynamic.adv.spring.GirSpringPGAdvExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author ：张逢吉
 * @date ：Created in   13:00
 * @description： TODO
 */
@Configuration
public class AdvQueryConfig {

//    @Bean
//    IAdvExecutor springAdvExecutorGetter(DataSource dataSourceSpring) { // 这个参数上不要删除，主要是为了让spring初始化GirSpringAdvExecutor这个bean在DataSource之后
//        GirSpringPGAdvExecutor girSpringAdvExecutor = new GirSpringPGAdvExecutor();
//        girSpringAdvExecutor.initByDataSource(dataSourceSpring);
//        return girSpringAdvExecutor;
//    }

}
