// package com.tc.tools.geowebcache.tianditu.service;
//
// import cn.hutool.core.io.IoUtil;
// import cn.hutool.core.lang.Singleton;
// import cn.hutool.core.util.StrUtil;
// import cn.geoair.web.util.GirHttpServletHelper;
// import com.tc.tools.geowebcache.tianditu.layer.TC_ArcGISCacheLayer;
// import com.tc.tools.geowebcache.tianditu.group.TC_ArcGISCacheLayerGroup;
// import com.tc.tools.geowebcache.tianditu.utils.GeowebcacheUtils;
// import com.tc.tools.geowebcache.tianditu.utils.TC_ResponseUtils;
// import org.apache.commons.logging.Log;
// import org.apache.commons.logging.LogFactory;
// import org.geowebcache.GeoWebCacheDispatcher;
// import org.geowebcache.GeoWebCacheException;
// import org.geowebcache.GeoWebCacheExtensions;
// import org.geowebcache.conveyor.ConveyorTile;
// import org.geowebcache.grid.GridSet;
// import org.geowebcache.grid.GridSubset;
// import org.geowebcache.grid.OutsideCoverageException;
// import org.geowebcache.io.Resource;
// import org.geowebcache.service.OWSException;
// import org.geowebcache.service.wmts.WMTSService;
// import org.geowebcache.stats.RuntimeStats;
// import org.geowebcache.util.ServletUtils;
//
// import javax.imageio.ImageIO;
// import javax.servlet.ServletOutputStream;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
// import java.awt.image.BufferedImage;
// import java.io.IOException;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;
//
/// **
// * @description: 组图层服务
// * @author: zhang_jun
// * @create: 2021-09-24 17:43
// */
// public class GroupWmtsService extends CacheCaller {
//
//    private static Log LOGGER = LogFactory.getLog(GroupWmtsService.class);
//
//    public void doService(String[] group)
//            throws GeoWebCacheException, OWSException, IOException {
//        HttpServletRequest request = GirHttpServletHelper.getRequest();
//        HttpServletResponse response = GirHttpServletHelper.getResponse();
//        BufferedImage image = getImage(group, request, response);
//        if (image != null) {
//            response.setContentType("image/png");
//            ServletOutputStream outputStream = response.getOutputStream();
//            ImageIO.write(image, "png", outputStream);
//            IoUtil.close(outputStream);
//        }
//    }
//
//    /**
//     * 获取元数据信息
//     *
//     * @param group
//     * @param capabilitiesShowName xml上面展示的名称
//     */
//    public void doServiceGetCapabilities(
//            String[] group,
//            String capabilitiesShowName) {
//        HttpServletRequest request = GirHttpServletHelper.getRequest();
//        HttpServletResponse response = GirHttpServletHelper.getResponse();
//        List<TC_ArcGISCacheLayer> layerGroup = TC_ArcGISCacheLayerGroup.getLayerGroup(group[0]);
//        if (layerGroup == null) {
//            LOGGER.info(
//                    StrUtil.format(
//                            "GeoAir日志：无法找到对应图层：服务名称：{},请求url：{}", group[0], request.getRequestURI()));
//
//            TC_ResponseUtils.writeErrorAsXML(
//                    response,
//                    200,
//                    "无法找到对应图层"
//            );
//            return;
//        }
//        TC_ArcGISCacheLayer tcArcGISCacheLayer = layerGroup.get(0);
//        String name = tcArcGISCacheLayer.getName();
//        if (capabilitiesShowName == null) {
//            tcArcGISCacheLayer.setName(group[0] + ":" + group[1]);
//        } else {
//            tcArcGISCacheLayer.setName(capabilitiesShowName);
//        }
//
//        try {
//            GridSubset defaultGridSubset = tcArcGISCacheLayer.getDefaultGridSubset();
//            List<GridSet> gridSubsets = new ArrayList<>();
//            gridSubsets.add(defaultGridSubset.getGridSet());
//            String servletPrefix = null;
//            servletPrefix =
//                    GeoWebCacheExtensions.bean(GeoWebCacheDispatcher.class).getServletPrefix();
//            String servletBase = ServletUtils.getServletBaseURL(request, servletPrefix);
//            String context =
//                    ServletUtils.getServletContextPath(
//                            request, new String[]{SERVICE_PATH}, servletPrefix);
//            GtcWMTSGetCapabilities gtcWmtsGetCapabilities =
//                    new GtcWMTSGetCapabilities(
//                            tcArcGISCacheLayer, gridSubsets);
//            gtcWmtsGetCapabilities.writeResponse(
//            );
//            tcArcGISCacheLayer.setName(name);
//        } catch (Exception e) {
//            throw e;
//        } finally {
//            tcArcGISCacheLayer.setName(name);
//        }
//    }
//
//    /**
//     * 获取图片
//     *
//     * @param group
//     * @param request
//     * @param response
//     * @return
//     * @throws GeoWebCacheException
//     * @throws OWSException
//     * @throws IOException
//     */
//    public BufferedImage getImage(
//            String[] group, HttpServletRequest request, HttpServletResponse response)
//            throws GeoWebCacheException, OWSException, IOException {
//        List<TC_ArcGISCacheLayer> layerGroup = TC_ArcGISCacheLayerGroup.getLayerGroup(group[0]);
//        if (layerGroup == null) {
//            LOGGER.info(
//                    StrUtil.format(
//                            "GeoAir日志：无法找到对应图层：服务名称：{},请求url：{}", group[0], request.getRequestURI()));
//
//            TC_ResponseUtils.writeErrorAsXML(
//                    response,
//                    200,"无法找到对应图层");
//            return null;
//        }
//        // 排序
//        String sortKeys = group[1];
//
//        if (!sortKeys.equals("all")) {
//            String[] sortKey = sortKeys.split("_");
//            List<TC_ArcGISCacheLayer> sortlayerGroup = new ArrayList<>();
//            for (int i = 0; i < sortKey.length; i++) {
//                String key = sortKey[i];
//                for (int i1 = 0; i1 < layerGroup.size(); i1++) {
//                    TC_ArcGISCacheLayer tcArcGISCacheLayer = layerGroup.get(i1);
//                    if (key.equals(tcArcGISCacheLayer.getSortkey())) {
//                        sortlayerGroup.add(tcArcGISCacheLayer);
//                        break;
//                    }
//                }
//            }
//            if (!sortlayerGroup.isEmpty()) {
//                layerGroup = sortlayerGroup;
//            }
//        } else {
//            String keys = "";
//            for (TC_ArcGISCacheLayer tc_arcGISCacheLayer : layerGroup) {
//                keys += tc_arcGISCacheLayer.getSortkey() + "_";
//            }
//            group[1] = keys;
//        }
//
//        GeowebcacheUtils geowebcacheUtils = Singleton.get(GeowebcacheUtils.class);
//        WMTSService service = (WMTSService) geowebcacheUtils.findService("wmts");
//        BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
//        //        BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
//        String encoding = request.getCharacterEncoding();
//        Map<String, String> values =
//                ServletUtils.selectedStringsFromMap(request.getParameterMap(), encoding, keys);
//
//        String TileCol = values.get("tilecol"); // x
//        String TileRow = values.get("tilerow"); // y
//
//        String[] keys = new String[3];
//        keys[0] = TileCol;
//        keys[1] = TileRow;
//
//        String Basetilematrixset = values.get("tilematrix");
//        String[] split = Basetilematrixset.split(":");
//        if (split.length > 1) {
//            Basetilematrixset = split[split.length - 1];
//        }
//        keys[2] = Basetilematrixset;
//
//        // 查询是否有缓存
//        boolean cachehit = checkCache(group[0] + group[1], keys);
//
//        if (!cachehit) {
//            ConveyorTile convTile = null;
//            boolean isblank = false;
//            for (int i = 0; i < layerGroup.size(); i++) {
//                TC_ArcGISCacheLayer tcArcGISCacheLayer1 = layerGroup.get(i);
//                GridSubset defaultGridSubset = tcArcGISCacheLayer1.getDefaultGridSubset();
//                BufferedImage read = null;
//                if (defaultGridSubset == null) {
//                    // 空白图
//                    isblank = true;
//                    read = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
//                } else {
//                    String gridSubsetname = defaultGridSubset.getName();
//                    // 修改 tile的 切片方案的 key
//                    values.put("tilematrixset", gridSubsetname);
//                    // 修改 tile的 级别的加载方式
//                    values.put("tilematrix", gridSubsetname + ":" + Basetilematrixset);
//                    // 设置图层名称
//                    values.put("layer", tcArcGISCacheLayer1.getName());
//
//                    try {
//                        // 这里 把获取 kvp的 方法放到  异常捕获里面，就可以实现到请求超出图层边界的时候 ，
//                        // 不会导致因为异常抛出，请求中断
//                        isblank = false;
//                        convTile = (ConveyorTile) service.getKvpConveyor(request, response,
// values);
//                        convTile = tcArcGISCacheLayer1.getTile(convTile);
//                        Resource blob = convTile.getBlob();
//                        read = ImageIO.read(blob.getInputStream());
//                    } catch (OutsideCoverageException | OWSException e) {
//                        // 没有命中图片 这里就会抛出异常 ，所以创建一张空白图片
//                        isblank = true;
//                        read = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
//                    }
//                }
//                if (!isblank) {
//                    int width = read.getWidth();
//                    int height = read.getHeight();
//                    for (int ii = 0; ii < width; ii++) {
//                        for (int j = 0; j < height; j++) {
//                            int rgb = read.getRGB(ii, j);
//                            int alpha = rgb >> 24;
//                            if (!(alpha >= 0)) {
//                                image.setRGB(ii, j, rgb);
//                            }
//                        }
//                    }
//                }
//            }
//            if (convTile != null) {
//                if (group[0].contains("yx")) {
//                    tranGreenTotransparent(image);
//                }
//                getCacheManger().saveCache(group[0] + group[1], keys, image);
//            } else {
//                throw new OWSException(400, "404", "图层", "无法找到对应图层");
//            }
//        } else {
//            image = getCacheImg(group[0] + group[1], keys);
//        }
//
//        return image;
//    }
// }
