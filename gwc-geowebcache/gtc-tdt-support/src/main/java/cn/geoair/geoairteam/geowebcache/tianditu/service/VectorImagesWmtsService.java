// package com.tc.tools.geowebcache.tianditu.service;
//
// import cn.hutool.core.util.StrUtil;
// import com.tc.tools.geowebcache.tianditu.layer.TC_ArcGISCacheLayer;
// import com.tc.tools.geowebcache.tianditu.group.TC_ArcGISCacheLayerGroup;
// import com.tc.tools.geowebcache.tianditu.utils.TC_ResponseUtils;
// import java.util.ArrayList;
// import java.util.List;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
// import org.apache.commons.logging.Log;
// import org.apache.commons.logging.LogFactory;
// import org.geowebcache.GeoWebCacheDispatcher;
// import org.geowebcache.GeoWebCacheExtensions;
// import org.geowebcache.grid.GridSet;
// import org.geowebcache.grid.GridSubset;
// import org.geowebcache.service.OWSException;
// import org.geowebcache.stats.RuntimeStats;
// import org.geowebcache.util.ServletUtils;
//
/// **
// * @author yulei
// * @version 1.0
// * @date 2021/12/19 10:46
// */
// public class VectorImagesWmtsService extends CacheCaller {
//    private static Log LOGGER = LogFactory.getLog(GroupWmtsService.class);
//
//    //    public void doService(String[] VectorImages, HttpServletRequest request,
//    // HttpServletResponse response)
//    //            throws GeoWebCacheException, OWSException, IOException {
//    //        BufferedImage image = getImage(VectorImages, request, response);
//    //        if (image != null) {
//    //            response.setContentType("image/png");
//    //            ImageIO.write(image, "png", response.getOutputStream());
//    //        }
//    //    }
//    /**
//     * 获取元数据信息
//     *
//     * @param VectorImages
//     * @param request
//     * @param response
//     * @param capabilitiesShowName xml上面展示的名称
//     */
//    public void doServiceGetCapabilities(
//            String[] VectorImages,
//            HttpServletRequest request,
//            HttpServletResponse response,
//            String capabilitiesShowName) {
//        List<TC_ArcGISCacheLayer> layerGroup =
//                TC_ArcGISCacheLayerGroup.getLayerGroup(VectorImages[0]);
//        if (layerGroup == null) {
//            LOGGER.info(
//                    StrUtil.format(
//                            "GeoAir日志：无法找到对应图层：服务名称：{},请求url：{}",
//                            VectorImages[0],
//                            request.getRequestURI()));
//
//            TC_ResponseUtils.writeErrorAsXML(
//                    response,
//                    200,
//                    new OWSException(200, "404", request.getRequestURI(), "无法找到对应图层").toString(),
//                    GeoWebCacheExtensions.bean(RuntimeStats.class));
//            return;
//        }
//        TC_ArcGISCacheLayer tcArcGISCacheLayer = layerGroup.get(0);
//        String name = tcArcGISCacheLayer.getName();
//        if (capabilitiesShowName == null) {
//            tcArcGISCacheLayer.setName(VectorImages[0] + ":" + VectorImages[1]);
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
//                            request, new String[] {SERVICE_PATH}, servletPrefix);
//            GtcWMTSGetCapabilities gtcWmtsGetCapabilities =
//                    new GtcWMTSGetCapabilities(
//                            tcArcGISCacheLayer, gridSubsets, request, servletBase, context);
//            gtcWmtsGetCapabilities.writeResponse(
//                    response, GeoWebCacheExtensions.bean(RuntimeStats.class));
//            tcArcGISCacheLayer.setName(name);
//        } catch (Exception e) {
//            throw e;
//        } finally {
//            tcArcGISCacheLayer.setName(name);
//        }
//    }
// }
