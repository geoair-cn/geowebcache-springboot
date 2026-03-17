package cn.geoair.geoairteam.gwc.wcs;

import cn.geoair.base.Gir;
import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.springframework.context.annotation.*;
import tk.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


@SpringBootApplication
@ComponentScan({"cn.geoair.geoairteam"})
@MapperScan(basePackages = {"cn.geoair.geoairteam.platform.**.mapper", "cn.geoair.geoairteam.**.mapper"})
@EnableScheduling
@EnableFileStorage
@EnableAspectJAutoProxy
@ImportResource("classpath*:/geowebcache-servlet.xml")
public class RootBootWcsApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        Gir.log.info("启动开始!");
        SpringApplication.run(RootBootWcsApplication.class, args);
        Gir.log.info("启动完毕!");
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(RootBootWcsApplication.class);
    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        // 线程池维护线程的最少数量
        pool.setCorePoolSize(20);
        // 线程池维护线程的最大数量
        pool.setMaxPoolSize(2000);
        // 当调度器shutdown被调用时等待当前被调度的任务完成
        pool.setWaitForTasksToCompleteOnShutdown(true);
        return pool;
    }

}
