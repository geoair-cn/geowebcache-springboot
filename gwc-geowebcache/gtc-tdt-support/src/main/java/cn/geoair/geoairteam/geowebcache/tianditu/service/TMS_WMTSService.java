// package com.tc.tools.geowebcache.tianditu.service;
//
// import cn.hutool.core.io.FileUtil;
// import cn.hutool.core.io.IoUtil;
// import cn.hutool.core.util.StrUtil;
// import cn.geoair.web.util.GirHttpServletHelper;
// import com.tc.tools.geowebcache.tianditu.layer.TC_TMSLayer;
// import com.tc.tools.geowebcache.tianditu.utils.GISHubUtil;
// import org.geowebcache.GeoWebCacheDispatcher;
// import org.geowebcache.GeoWebCacheException;
// import org.geowebcache.GeoWebCacheExtensions;
// import org.geowebcache.grid.GridSet;
// import org.geowebcache.grid.GridSetBroker;
// import org.geowebcache.layer.TileLayer;
// import org.geowebcache.layer.TileLayerDispatcher;
// import org.geowebcache.service.OWSException;
// import org.geowebcache.util.ServletUtils;
//
// import javax.imageio.ImageIO;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
// import java.awt.image.BufferedImage;
// import java.io.File;
// import java.io.IOException;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;
//
/// **
// * @description: tms谷歌切片的wmts加载方式
// * @author: zhang_jun
// * @create: 2021-09-26 15:42
// */
// public class TMS_WMTSService extends CacheCaller {
//
//    public void doService(
//            String layerName )
//            throws OWSException, IOException {
//        HttpServletRequest request = GirHttpServletHelper.getRequest();
//        HttpServletResponse response = GirHttpServletHelper.getResponse();
//        String encoding = request.getCharacterEncoding();
//        Map<String, String> values =
//                ServletUtils.selectedStringsFromMap(request.getParameterMap(), encoding, keys);
//        TileLayerDispatcher tileLayerDispatcher =
//                GeoWebCacheExtensions.bean(TileLayerDispatcher.class);
//        TileLayer tileLayer = null;
//        try {
//            tileLayer = tileLayerDispatcher.getTileLayer(values.get("layer"));
//        } catch (GeoWebCacheException e) {
//            try {
//                tileLayer = tileLayerDispatcher.getTileLayer(layerName);
//            } catch (GeoWebCacheException e1) {
//                throw new OWSException(
//                        400, "InvalidParameterValue", "LAYERS", "图层 '" + layerName + "' 不可用");
//            }
//        }
//        BufferedImage read = null;
//        TC_TMSLayer tc_tmsLayer = (TC_TMSLayer) tileLayer;
//
//        String loadMethod = tc_tmsLayer.getLoadMethod();
//
//        String TileMatrix = values.get("TileMatrix");
//
//        String[] split = TileMatrix.split(":");
//        if (split.length > 1) {
//            TileMatrix = split[split.length - 1];
//        }
//        int i = Integer.parseInt(TileMatrix);
//        //        int i1 = i + 1;
//        String x = values.get("TileCol");
//        String y = values.get("TileRow");
//        int yi = Integer.parseInt(y);
//        Double v = Math.pow(2, i - 1) - yi - 1;
//        if (loadMethod.equals("local")) {
//            String localPath = tc_tmsLayer.getLocalPath();
//            String filePath =
//                    localPath
//                            + File.separator
//                            + TileMatrix
//                            + File.separator
//                            + x
//                            + File.separator
//                            + v.intValue()
//                            //                            + y
//                            + "."
//                            + tc_tmsLayer.getFormat();
//            //            System.out.println(filePath);
//            File file = new File(filePath);
//            if (FileUtil.exist(file)) {
//                read = ImageIO.read(file);
//                response.setHeader("Content-Type", "image/png");
//                ImageIO.write(read, "png", response.getOutputStream());
//                IoUtil.close(response.getOutputStream());
//            } else {
//                throw new OWSException(
//                        400, "InvalidParameterValue", "LAYERS", "图层 '" + layerName + "'
// 请求的图片未找到");
//            }
//        }
//
//        if (loadMethod.equals("http")) {
//            String httpBasePath = tc_tmsLayer.getHttpPath();
//            httpBasePath = StrUtil.replace(httpBasePath, "{x}", String.valueOf(x));
//            httpBasePath = StrUtil.replace(httpBasePath, "{y}", String.valueOf(v.intValue()));
//            httpBasePath = StrUtil.replace(httpBasePath, "{z}", String.valueOf(TileMatrix));
//            GISHubUtil.writeResponse(httpBasePath, response);
//        }
//    }
//
//    public void doServiceGetCapabilities(
//            String layerName )
//            throws OWSException {
//        HttpServletRequest request = GirHttpServletHelper.getRequest();
//        HttpServletResponse response = GirHttpServletHelper.getResponse();
//        String encoding = request.getCharacterEncoding();
//        Map<String, String> values =
//                ServletUtils.selectedStringsFromMap(request.getParameterMap(), encoding, keys);
//        TileLayerDispatcher tileLayerDispatcher =
//                GeoWebCacheExtensions.bean(TileLayerDispatcher.class);
//        TileLayer tileLayer = null;
//        try {
//            tileLayer = tileLayerDispatcher.getTileLayer(layerName);
//        } catch (GeoWebCacheException e) {
//            try {
//                tileLayer = tileLayerDispatcher.getTileLayer(values.get("layer"));
//            } catch (GeoWebCacheException e1) {
//                throw new OWSException(
//                        400, "InvalidParameterValue", "LAYERS", "图层 '" + layerName + "' 不可用");
//            }
//        }
//
//        TC_TMSLayer tc_tmsLayer = (TC_TMSLayer) tileLayer;
//        GridSetBroker bean = GeoWebCacheExtensions.bean(GridSetBroker.class);
//        GridSet worldEpsg3857 = bean.getWorldEpsg3857();
//        try {
//            List<GridSet> gridSubsets = new ArrayList<>();
//            gridSubsets.add(worldEpsg3857);
//            String servletPrefix = null;
//            servletPrefix =
//                    GeoWebCacheExtensions.bean(GeoWebCacheDispatcher.class).getServletPrefix();
//            String servletBase = ServletUtils.getServletBaseURL(request, servletPrefix);
//            String context =
//                    ServletUtils.getServletContextPath(
//                            request, new String[] {SERVICE_PATH}, servletPrefix);
//            GtcWMTSGetCapabilities gtcWmtsGetCapabilities =
//                    new GtcWMTSGetCapabilities(
//                            tc_tmsLayer, gridSubsets );
//            gtcWmtsGetCapabilities.writeResponse(
//                  );
//        } catch (Exception e) {
//            throw e;
//        }
//    }
// }
