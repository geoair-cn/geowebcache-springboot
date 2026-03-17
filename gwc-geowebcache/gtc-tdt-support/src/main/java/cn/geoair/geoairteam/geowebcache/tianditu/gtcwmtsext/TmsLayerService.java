package cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext;

import cn.geoair.base.log.GiLogger;
import cn.geoair.base.log.GirLogger;
import cn.geoair.map.dynamic.tools.GirAdvTools;
import cn.geoair.map.dynamic.tools.grid.GirTileConverterOpt;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.geoair.geoairteam.gwc.model.gwc.enums.GtcWmtsType;
import cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext.dto.TileResult;
import cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext.dto.XyzDto;
import cn.geoair.geoairteam.geowebcache.tianditu.layer.TC_TMSLayer;
import cn.geoair.geoairteam.geowebcache.tianditu.service.GtcWMTSGetCapabilities;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.GeoWebCacheExtensions;
import org.geowebcache.grid.GridSet;
import org.geowebcache.grid.GridSetBroker;
import org.geowebcache.layer.TileLayer;
import org.geowebcache.service.OWSException;
import org.geowebcache.util.ServletUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author ：zfj
 * @date ：Created in 2023/12/5 10:53
 * @description： xyz的wmts服务启动类
 */
public class TmsLayerService extends TileBaseGetterSupport {

    private static final GiLogger logger = GirLogger.getLoger(TmsLayerService.class);

    String layerName;
    GridSetBroker gridSetBroker;
    TileLayer tileLayer;
    Map<String, String> values;
    GtcWmtsType gtcWmtsType;

    public TmsLayerService(String requestLayerName, String layerName) throws OWSException {
        super(requestLayerName);
        this.layerName = layerName;
        this.gridSetBroker = GeoWebCacheExtensions.bean(GridSetBroker.class);
        notExist();
        gtcWmtsType = GtcWmtsType.tmsLayer;
    }

    @Override
    public TileResult doService(XyzDto xyzDto) throws GeoWebCacheException, OWSException {
        return super.doService(xyzDto);
    }

    @Override
    public void doServiceGetCapabilities(String capabilitiesShowName)
            throws GeoWebCacheException, OWSException {
        TC_TMSLayer tc_tmsLayer = (TC_TMSLayer) tileLayer;
        String crs = tc_tmsLayer.getCrs();
        GridSet worldEpsg = null;
        if (crs.contains("3857")) {
            worldEpsg = gridSetBroker.getWorldEpsg3857();
        } else if (crs.contains("4326")) {
            worldEpsg = gridSetBroker.getWorldEpsg4326();
        } else {
            for (GridSet gridSet : gridSetBroker.getGridSets()) {
                String name = gridSet.getName();
                if (name.contains(crs)) {
                    worldEpsg = gridSet;
                    break;
                }
            }
        }
        if (worldEpsg == null) {
            worldEpsg = gridSetBroker.getWorldEpsg4326();
        }
        try {
            List<GridSet> gridSubsets = new ArrayList<>();
            gridSubsets.add(worldEpsg);
            GtcWMTSGetCapabilities gtcWmtsGetCapabilities =
                    new GtcWMTSGetCapabilities(
                            tc_tmsLayer,
                            gridSubsets,
                            GtcWmtsType.tmsLayer,
                            capabilitiesShowName,
                            null);
            gtcWmtsGetCapabilities.writeResponse();
        } catch (Exception e) {
            throw e;
        }
    }


    @Override
    public BufferedImage getImage(XyzDto xyzDto) throws GeoWebCacheException, OWSException {
        TC_TMSLayer tc_tmsLayer = (TC_TMSLayer) tileLayer;
        String loadMethod = tc_tmsLayer.getLoadMethod();
        Long y = xyzDto.getY();
        Long x = xyzDto.getX();
        Long z = xyzDto.getZ();
        GirTileConverterOpt tileGridOpt;
        if (tc_tmsLayer.getCrs().equals("4326") || tc_tmsLayer.getCrs().equals("4490")) {
            tileGridOpt = GirAdvTools.getTileGrid4326SeparateOpt();
        } else {
            tileGridOpt = GirAdvTools.getTileGrid3857Opt();
        }
        int reverseY = tileGridOpt.reverseY(y.intValue(), z.intValue());
        if (loadMethod.equals("local")) {
            String localPath = tc_tmsLayer.getLocalPath();
            localPath = StrUtil.replace(localPath, "{x}", String.valueOf(x));
            localPath = StrUtil.replace(localPath, "{y}", String.valueOf(reverseY));
            localPath = StrUtil.replace(localPath, "{z}", String.valueOf(z));
            File file = new File(localPath);
            if (FileUtil.exist(file)) {
                return ImgUtil.read(file);
            } else {
                throw new OWSException(
                        400, "InvalidParameterValue", "LAYERS", "图层 '" + layerName + "' 请求的图片未找到");
            }
        }
        if (loadMethod.equals("http")) {
            String httpBasePath = tc_tmsLayer.getHttpPath();
            httpBasePath = StrUtil.replace(httpBasePath, "{x}", String.valueOf(x));
            httpBasePath = StrUtil.replace(httpBasePath, "{y}", String.valueOf(reverseY));
            httpBasePath = StrUtil.replace(httpBasePath, "{z}", String.valueOf(z));
            HttpRequest get = HttpUtil.createGet(httpBasePath);
            HttpResponse execute = get.execute();
            if (execute.getStatus() == 200) {
                InputStream inputStream = null;
                try {
                    inputStream = execute.bodyStream();
                    return ImgUtil.read(inputStream);
                } catch (Exception e) {
                    logger.info(e, "代理请求失败--{}", httpBasePath);
                } finally {
                    IoUtil.close(inputStream);
                }
            }
        }
        return null;
    }

    /**
     * 检查服务是否存在
     *
     * @return
     */
    private boolean notExist() throws OWSException {
        String encoding = request.getCharacterEncoding();
        values =
                ServletUtils.selectedStringsFromMap(
                        request.getParameterMap(),
                        encoding,
                        RequestParamter.getRequestParamterCodes());
        try {
            tileLayer = tileLayerDispatcher.getTileLayer(values.get("layer"));
        } catch (Exception e) {
            try {
                tileLayer = tileLayerDispatcher.getTileLayer(layerName);
            } catch (Exception e1) {
                throw new OWSException(
                        400, "InvalidParameterValue", "LAYERS", "图层 '" + layerName + "' 不可用");
            }
        }
        return false;
    }


}
