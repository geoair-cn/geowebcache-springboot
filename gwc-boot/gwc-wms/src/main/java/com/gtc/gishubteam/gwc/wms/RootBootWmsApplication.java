package cn.geoair.geoairteam.gwc.wms;

import cn.geoair.base.Gtc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;



import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication()
@ComponentScan({"cn.geoair.geoairteam","com.gtc"})
@MapperScan(basePackages = {"cn.geoair.geoairteam.platform.gtc.**.mapper"})
@EnableScheduling
public class RootBootWmsApplication extends SpringBootServletInitializer{
	
	public static void main(String[] args) {
		
		Gtc.log.info("启动开始!");
		SpringApplication.run(RootBootWmsApplication.class, args);
		Gtc.log.info("启动完毕!");
	}

	@Override 
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(RootBootWmsApplication.class);
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
