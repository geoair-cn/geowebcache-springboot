package cn.geoair.geoairteam.gwc.service.utils;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.io.File;

/**
 * 瓦片缓存工具类（文件系统实现）
 * @author ：zfj
 * @date ：Created in 2025/5/13 11:09
 */
public class GtcFileTileCacheUtils {

    @Getter
    private String groupName;
    private String x;
    private String y;
    private String z;
    private String sortKeyMd5;

    // 私有化构造器
    private GtcFileTileCacheUtils() {}

    /**
     * 构建工具类实例（默认sortKeyMd5）
     */
    public static GtcFileTileCacheUtils of(String groupName, String z, String x, String y) {
        GtcFileTileCacheUtils bean = new GtcFileTileCacheUtils();
        bean.groupName = groupName;
        bean.z = z;
        bean.x = x;
        bean.y = y;
        bean.sortKeyMd5 = "1";
        return bean;
    }

    /**
     * 构建工具类实例（带sortKeyMd5）
     */
    public static GtcFileTileCacheUtils of(String groupName, String sortKeyMd5, String z, String x, String y) {
        GtcFileTileCacheUtils bean = new GtcFileTileCacheUtils();
        bean.groupName = groupName;
        bean.z = z;
        bean.x = x;
        bean.y = y;
        bean.sortKeyMd5 = sortKeyMd5;
        return bean;
    }

    /**
     * 获取缓存根目录名称（分组+排序键MD5）
     */
    public String getRootDirName() {
        return "cache_" + StrUtil.format("{}_{}", groupName, sortKeyMd5);
    }

    /**
     * 获取瓦片文件路径（z/x/y.png）
     */
    public String getTilePath() {
        return StrUtil.format("{}{}{}{}{}.png", z, File.separator, x, File.separator, y);
    }

}
