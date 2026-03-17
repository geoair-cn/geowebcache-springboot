package cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext;

import cn.geoair.geoairteam.gwc.model.gwc.enums.GtcWmtsType;
import cn.geoair.geoairteam.geowebcache.tianditu.utils.CacheManger;

import java.awt.*;
import java.awt.image.BufferedImage;

import org.geowebcache.GeoWebCacheExtensions;

/**
 * @description: 缓存类
 * @author: zhang_jun
 * @create: 2021-10-08 14:57
 */
public abstract class CacheCaller {

    private CacheManger cacheManger;

    protected CacheManger getCacheManger() {
        if (cacheManger == null) {
            cacheManger = GeoWebCacheExtensions.bean(CacheManger.class);
        }
        return cacheManger;
    }

    /**
     * 检查缓存效果
     *
     * @param groupName
     * @return
     */
    protected boolean checkCache(
            GtcWmtsType gtcWmtsType, String groupName, String sortKeys, String[] xyz) {
        return getCacheManger().hasCache(gtcWmtsType, groupName, sortKeys, xyz);
    }

    protected boolean hasCache(
            GtcWmtsType gtcWmtsType, String groupName, String sortKeys, String[] xyz) {
        return checkCache(gtcWmtsType, groupName, sortKeys, xyz);
    }

    protected BufferedImage getCacheImg(
            GtcWmtsType gtcWmtsType, String groupName, String sortKeys, String[] xyz) {
        BufferedImage cacheImg =
                getCacheManger().getCacheImg(gtcWmtsType, groupName, sortKeys, xyz);
        if (cacheImg == null) {
            return null;
        } else {
            return cacheImg;
        }
    }

    /*
           绿色背景转透明色
    */
    protected BufferedImage tranGreenTotransparent(BufferedImage image) {
        Color myColor = new Color(255, 255, 255, 0);
        int rgb2 = myColor.getRGB();
        int width = image.getWidth();
        int height = image.getHeight();
        for (int ii = 0; ii < width; ii++) {
            for (int j = 0; j < height; j++) {
                int rgb1 = image.getRGB(ii, j);
                if ((((rgb1 >> 8) & 0xFF) == 255)
                        && (((rgb1) & 0xFF) != 255)
                        && (((rgb1 >> 16) & 0xFF) != 255)) {
                    image.setRGB(ii, j, rgb2);
                }
            }
        }
        return image;
    }
}
