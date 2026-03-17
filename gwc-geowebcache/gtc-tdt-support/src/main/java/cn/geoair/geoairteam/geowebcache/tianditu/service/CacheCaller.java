// package com.tc.tools.geowebcache.tianditu.service;
//
// import com.tc.tools.geowebcache.tianditu.utils.CacheManger;
// import com.tc.tools.geowebcache.tianditu.utils.GISHubUtil;
// import java.awt.*;
// import java.awt.image.BufferedImage;
// import org.geowebcache.GeoWebCacheDispatcher;
// import org.geowebcache.GeoWebCacheExtensions;
//
/// **
// * @description: 缓存类
// * @author: zhang_jun
// * @create: 2021-10-08 14:57
// */
// public abstract class CacheCaller {
//
//    public static final String SERVICE_WMTS = "wmts";
//
//    public static final String SERVICE_PATH =
//            "/" + GeoWebCacheDispatcher.TYPE_SERVICE + "/" + SERVICE_WMTS;
//
//    protected  String[] keys = GISHubUtil.keys;
//
//    private CacheManger cacheManger;
//
//    protected CacheManger getCacheManger() {
//        if (cacheManger == null) {
//            cacheManger = GeoWebCacheExtensions.bean(CacheManger.class);
//        }
//        return cacheManger;
//    }
//
//    /**
//     * 检查缓存效果
//     *
//     * @param LayerName
//     * @return
//     */
//  protected   boolean checkCache(String LayerName, String[] xyz) {
//        return getCacheManger().hasCache(LayerName, xyz);
//    }
//
//    protected  BufferedImage getCacheImg(String LayerName, String[] xyz) {
//        BufferedImage cacheImg = getCacheManger().getCacheImg(LayerName, xyz);
//        if (cacheImg == null) {
//            return null;
//        } else {
//            return cacheImg;
//        }
//    }
//
//    /*
//    透明背景转白色背景
//     */
//    BufferedImage transparentConversionToWhite(BufferedImage image) {
//        //  设置半透明色为白色
//        int rgb = Color.white.getRGB();
//        int width = image.getWidth();
//        int height = image.getHeight();
//        for (int ii = 0; ii < width; ii++) {
//            for (int j = 0; j < height; j++) {
//                int rgb1 = image.getRGB(ii, j);
//                int i = rgb1 >> 24;
//                if ((i >= 0) && (i <= 120)) {
//                    image.setRGB(ii, j, rgb);
//                }
//            }
//        }
//        return image;
//    }
//
//    /*
//           绿色背景转透明色
//    */
//  protected   BufferedImage tranGreenTotransparent(BufferedImage image) {
//        Color myColor = new Color(255, 255, 255, 0);
//        int rgb2 = myColor.getRGB();
//        int width = image.getWidth();
//        int height = image.getHeight();
//        for (int ii = 0; ii < width; ii++) {
//            for (int j = 0; j < height; j++) {
//                int rgb1 = image.getRGB(ii, j);
//                if ((((rgb1 >> 8) & 0xFF) == 255)
//                        && (((rgb1) & 0xFF) != 255)
//                        && (((rgb1 >> 16) & 0xFF) != 255)) {
//                    image.setRGB(ii, j, rgb2);
//                }
//            }
//        }
//        return image;
//    }
//
//
//
// }
