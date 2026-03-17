// package com.tc.tools.geowebcache.tianditu.service;
//
// import cn.hutool.core.lang.Singleton;
// import cn.geoair.web.util.GirHttpServletHelper;
// import com.tc.tools.geowebcache.tianditu.layer.TC_ArcGISCacheLayer;
// import com.tc.tools.geowebcache.tianditu.utils.GeowebcacheUtils;
// import java.awt.image.BufferedImage;
// import java.io.IOException;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;
// import javax.imageio.ImageIO;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
// import org.geowebcache.GeoWebCacheDispatcher;
// import org.geowebcache.GeoWebCacheException;
// import org.geowebcache.GeoWebCacheExtensions;
// import org.geowebcache.conveyor.ConveyorTile;
// import org.geowebcache.grid.GridSet;
// import org.geowebcache.grid.GridSubset;
// import org.geowebcache.grid.OutsideCoverageException;
// import org.geowebcache.io.Resource;
// import org.geowebcache.layer.TileLayer;
// import org.geowebcache.layer.TileLayerDispatcher;
// import org.geowebcache.service.OWSException;
// import org.geowebcache.service.wmts.WMTSService;
// import org.geowebcache.stats.RuntimeStats;
// import org.geowebcache.util.ServletUtils;
//
/// **
// * @description: 单图层wmts加载
// * @author: zhang_jun
// * @create: 2021-09-26 13:32
// */
// public class OneTiantiduWmtsService extends CacheCaller {
//
//    public void doService(
//            String layerName )
//            throws GeoWebCacheException, OWSException, IOException {
//        HttpServletRequest request = GirHttpServletHelper.getRequest();
//        HttpServletResponse response = GirHttpServletHelper.getResponse();
//        BufferedImage image = getImage(layerName, request, response);
//        if (image != null) {
//            response.setContentType("image/png");
//            ImageIO.write(image, "png", response.getOutputStream());
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
//                layerName = values.get("layer");
//            } catch (GeoWebCacheException e1) {
//                throw new OWSException(400, "无效的参数值", "LAYERS", "图层 '" + layerName + "' 不可用");
//            }
//        }
//        TC_ArcGISCacheLayer tctileLayer = (TC_ArcGISCacheLayer) tileLayer;
//        GridSubset defaultGridSubset = tctileLayer.getDefaultGridSubset();
//        try {
//            List<GridSet> gridSubsets = new ArrayList<>();
//            gridSubsets.add(defaultGridSubset.getGridSet());
//
//            String servletPrefix = null;
//            servletPrefix =
//                    GeoWebCacheExtensions.bean(GeoWebCacheDispatcher.class).getServletPrefix();
//            String servletBase = ServletUtils.getServletBaseURL(request, servletPrefix);
//            String context =
//                    ServletUtils.getServletContextPath(
//                            request, new String[] {SERVICE_PATH}, servletPrefix);
//            GtcWMTSGetCapabilities gtcWmtsGetCapabilities =
//                    new GtcWMTSGetCapabilities(
//                            tctileLayer, gridSubsets );
//            gtcWmtsGetCapabilities.writeResponse(
//                   );
//        } catch (Exception e) {
//            throw e;
//        }
//    }
//
//    public BufferedImage getImage(
//            String layerName, HttpServletRequest request, HttpServletResponse response)
//            throws GeoWebCacheException, OWSException, IOException {
//
//        GeowebcacheUtils geowebcacheUtils = Singleton.get(GeowebcacheUtils.class);
//        WMTSService service = (WMTSService) geowebcacheUtils.findService("wmts");
//        String encoding = request.getCharacterEncoding();
//        Map<String, String> values =
//                ServletUtils.selectedStringsFromMap(request.getParameterMap(), encoding, keys);
//
//        TileLayerDispatcher tileLayerDispatcher =
//                GeoWebCacheExtensions.bean(TileLayerDispatcher.class);
//        ConveyorTile conveyorTile = null;
//        BufferedImage read = null;
//        TileLayer tileLayer = null;
//        try {
//            tileLayer = tileLayerDispatcher.getTileLayer(values.get("layer"));
//            layerName = values.get("layer");
//        } catch (GeoWebCacheException e) {
//            try {
//                tileLayer = tileLayerDispatcher.getTileLayer(layerName);
//            } catch (GeoWebCacheException e1) {
//                throw new OWSException(
//                        400, "InvalidParameterValue", "LAYERS", "图层 '" + layerName + "' 不可用");
//            }
//        }
//        String TileCol = values.get("tilecol"); // x
//        String TileRow = values.get("tilerow"); // y
//
//        String[] keys = new String[3];
//        keys[0] = TileCol;
//        keys[1] = TileRow;
//        String Basetilematrixset = values.get("tilematrix");
//        String[] split = Basetilematrixset.split(":");
//        if (split.length > 1) {
//            Basetilematrixset = split[split.length - 1];
//        }
//        keys[2] = Basetilematrixset;
//        //        keys[2] = Basetilematrixset;
//        if (!tileLayer.isEnabled()) {
//            throw new OWSException(
//                    400, "无效的参数值", "LAYERS", "Layer '" + layerName + "' is disabled");
//        }
//
//        // 判断缓存
//        boolean cachehit = checkCache(layerName, keys);
//
//        // 缓存未命中
//        if (!cachehit) {
//            TC_ArcGISCacheLayer tctileLayer = (TC_ArcGISCacheLayer) tileLayer;
//            GridSubset defaultGridSubset = tctileLayer.getDefaultGridSubset();
//            if (defaultGridSubset == null) {
//
//            } else {
//                String gridSubsetname = defaultGridSubset.getName();
//                // 修改 tile的 切片方案的 key
//                values.put("tilematrixset", gridSubsetname);
//                // 修改 tile的 级别的加载方式
//                values.put("tilematrix", gridSubsetname + ":" + Basetilematrixset);
//                // 设置图层名称
//                values.put("layer", tctileLayer.getName());
//
//                conveyorTile = (ConveyorTile) service.getKvpConveyor(request, response, values);
//                try {
//                    conveyorTile = tctileLayer.getTile(conveyorTile);
//                    Resource blob = conveyorTile.getBlob();
//                    read = ImageIO.read(blob.getInputStream());
//                    getCacheManger().saveCache(layerName, keys, read);
//                } catch (OutsideCoverageException | IOException e) {
//                    // 没有命中图片 这里就会抛出异常 ，所以创建一张空白图片
//                    read = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
//                }
//            }
//        } else {
//            read = getCacheImg(layerName, keys);
//        }
//        return read;
//    }
// }
