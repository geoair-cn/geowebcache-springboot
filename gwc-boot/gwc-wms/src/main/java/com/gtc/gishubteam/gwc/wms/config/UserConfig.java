package cn.geoair.geoairteam.gwc.wms.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;


/**
 * WebMvc配置
 */

@Configuration
public class UserConfig implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
    	
        //GwWebUserConfig.registerUserClass(CusSessionUser.class);
    }
}
