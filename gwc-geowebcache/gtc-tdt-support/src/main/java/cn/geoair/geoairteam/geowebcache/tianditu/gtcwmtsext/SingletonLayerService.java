package cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext;

import cn.hutool.core.lang.Singleton;
import cn.geoair.base.util.GutilObject;
import cn.geoair.geoairteam.gwc.model.gwc.enums.GtcWmtsType;
import cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext.dto.XyzDto;
import cn.geoair.geoairteam.geowebcache.tianditu.layer.TC_ArcGISCacheLayer;
import cn.geoair.geoairteam.geowebcache.tianditu.service.GtcWMTSGetCapabilities;
import cn.geoair.geoairteam.geowebcache.tianditu.utils.GeowebcacheUtils;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

import org.geowebcache.GeoWebCacheException;
import org.geowebcache.conveyor.ConveyorTile;
import org.geowebcache.grid.GridSet;
import org.geowebcache.grid.GridSubset;
import org.geowebcache.io.Resource;
import org.geowebcache.layer.TileLayer;
import org.geowebcache.service.OWSException;
import org.geowebcache.service.wmts.WMTSService;

/**
 * @author ：zfj
 * @date ：Created in 2023/12/5 13:25
 * @description： ArcGis单图层加载
 */
public class SingletonLayerService extends TileBaseGetterSupport {
    static GeowebcacheUtils geowebcacheUtils;
    private String layerName;
    TileLayer tileLayer;
    GtcWmtsType gtcWmtsType;

    public SingletonLayerService(String requestLayerName, String layerName) throws OWSException {
        super(requestLayerName);
        this.layerName = layerName;
        if (GutilObject.isEmpty(geowebcacheUtils)) {
            geowebcacheUtils = Singleton.get(GeowebcacheUtils.class);
        }
        gtcWmtsType = GtcWmtsType.singletonLayer;
        notExist();
    }

    @Override
    public void doServiceGetCapabilities(String capabilitiesShowName)
            throws GeoWebCacheException, OWSException {
        TC_ArcGISCacheLayer tctileLayer = (TC_ArcGISCacheLayer) tileLayer;
        GridSubset defaultGridSubset = tctileLayer.getDefaultGridSubset();
        try {
            List<GridSet> gridSubsets = new ArrayList<>();
            gridSubsets.add(defaultGridSubset.getGridSet());
            GtcWMTSGetCapabilities gtcWmtsGetCapabilities =
                    new GtcWMTSGetCapabilities(
                            tctileLayer,
                            gridSubsets,
                            GtcWmtsType.singletonLayer,
                            capabilitiesShowName,
                            null);
            gtcWmtsGetCapabilities.writeResponse();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public BufferedImage getImage(XyzDto xyzDto) throws GeoWebCacheException, OWSException {
        String[] xyzArray = xyzDto.getXyzArray();
        if (checkCache(gtcWmtsType, layerName, "1", xyzArray)) {
            return getCacheImg(gtcWmtsType, layerName, "1", xyzArray);
        } else {
            BufferedImage imageByServices = getImageByService(xyzDto);
            getCacheManger().saveCache(gtcWmtsType, layerName, "1", xyzArray, imageByServices, 0L);
            return imageByServices;
        }
    }

    private BufferedImage getImageByService(XyzDto xyzDto) throws GeoWebCacheException, OWSException {
        WMTSService service = (WMTSService) geowebcacheUtils.findService("wmts");
        BufferedImage read = null;
        ConveyorTile conveyorTile = null;
        String zStr = xyzDto.getZStr();// z
        if (!tileLayer.isEnabled()) {
            throw new OWSException(
                    400, "无效的参数值", "LAYERS", "Layer '" + layerName + "' is disabled");
        }
        TC_ArcGISCacheLayer tctileLayer = (TC_ArcGISCacheLayer) tileLayer;
        GridSubset defaultGridSubset = tctileLayer.getDefaultGridSubset();
        if (defaultGridSubset == null) {

        } else {

            String gridSubsetname = defaultGridSubset.getName();
            // 修改 tile的 切片方案的 key
            values.put(RequestParamter.tilematrixset.getCode(), gridSubsetname);
            // 修改 tile的 级别的加载方式
            values.put(RequestParamter.tilematrix.getCode(), gridSubsetname + ":" + zStr);
            // 设置图层名称
            values.put(RequestParamter.layer.getCode(), tctileLayer.getName());
            try {
                conveyorTile = (ConveyorTile) service.getKvpConveyor(request, response, values);
                conveyorTile = tctileLayer.getTile(conveyorTile);
                Resource blob = conveyorTile.getBlob();
                read = ImageIO.read(blob.getInputStream());
            } catch (Exception e) {
                // 没有命中图片 这里就会抛出异常 ，所以创建一张空白图片
                read = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
            }
        }
        return read;
    }

    /**
     * 检查服务是否存在
     *
     * @return
     */
    private boolean notExist() throws OWSException {

        try {
            tileLayer = tileLayerDispatcher.getTileLayer(values.get("layer"));
            layerName = values.get("layer");
        } catch (Exception e) {
            try {
                tileLayer = tileLayerDispatcher.getTileLayer(layerName);
            } catch (Exception e1) {
                throw new OWSException(
                        400, "InvalidParameterValue", "LAYERS", "图层 '" + layerName + "' 不可用");
                //               try{
                //                   String decode = URLDecoder.decode(layerName,
                // Charset.forName("UTF-8"));
                //               }catch (Exception e2){
                //                   throw new OWSException(
                //                           400, "InvalidParameterValue", "LAYERS", "图层 '" +
                // layerName + "' 不可用");
                //               }
            }
        }
        return false;
    }
}
