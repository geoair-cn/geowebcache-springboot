package cn.geoair.geoairteam.gwc.service.config;

import cn.hutool.setting.dialect.Props;
import cn.hutool.setting.dialect.PropsUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * GeoWebCache配置类：初始化缓存目录参数（对应原context-param）
 */
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "geoair.gwc.cache")
@Data
public class GeoWebCacheConfig {


    /**
     * GeoWebCache缓存目录
     */
    private String gwcCacheDir;

    /**
     * Gtc缓存目录
     */
    private String gtcCacheDir;


    private String adminUserName = "admin";

    private String adminPassword = "admin";


    public void setGwcCacheDir(String gwcCacheDir) {
        System.setProperty("GEOWEBCACHE_CACHE_DIR", gwcCacheDir);
        log.info("设置GeoWebCache缓存目录：{}", gwcCacheDir);
        this.gwcCacheDir = gwcCacheDir;
    }

    public void setGtcCacheDir(String gtcCacheDir) {
        System.setProperty("GTC_CACHE_DIR", gtcCacheDir);
        log.info("设置Gtc缓存目录：{}", gtcCacheDir);
        this.gtcCacheDir = gtcCacheDir;
    }


    public static String getCommitTime() {
        return PropsUtil.get("git.properties").getStr("git.commit.time");
    }

    public static String getCommitId() {
        return PropsUtil.get("git.properties").getStr("git.commit.id");
    }

    public static String getCommitBranch() {
        return PropsUtil.get("git.properties").getStr("git.branch");
    }

    public static String getCommitMessageShort() {
        return PropsUtil.get("git.properties").getStr("git.commit.message.short");
    }


}
