package cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Singleton;
import cn.geoair.base.log.GiLogger;
import cn.geoair.base.log.GirLogger;
import cn.geoair.base.util.GutilObject;
import cn.geoair.geoairteam.gwc.model.gwc.enums.GtcWmtsType;
import cn.geoair.web.util.GirHttpServletHelper;
import cn.geoair.geoairteam.geowebcache.tianditu.group.GroupMeta;
import cn.geoair.geoairteam.geowebcache.tianditu.group.TC_ArcGISCacheLayerGroup;
import cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext.dto.XyzDto;
import cn.geoair.geoairteam.geowebcache.tianditu.layer.TC_ArcGISCacheLayer;
import cn.geoair.geoairteam.geowebcache.tianditu.service.GtcWMTSGetCapabilities;
import cn.geoair.geoairteam.geowebcache.tianditu.utils.GeowebcacheUtils;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.geowebcache.GeoWebCacheException;
import org.geowebcache.conveyor.ConveyorTile;
import org.geowebcache.grid.GridSet;
import org.geowebcache.grid.GridSubset;
import org.geowebcache.io.Resource;
import org.geowebcache.service.OWSException;
import org.geowebcache.service.wmts.WMTSService;

/**
 * @author ：zfj
 * @date ：Created in 2023/12/5 10:53
 * @description： arcGis组的wmts服务启动类
 */
public class GtcGroupWmtsService extends TileBaseGetterSupport {

    private static final GiLogger logger = GirLogger.getLoger(GtcGroupWmtsService.class);

    private final String groupName;

    private String sortKeys;

    GroupMeta groupMeta;

    List<TC_ArcGISCacheLayer> tc_arcGISCacheLayers;

    GtcWmtsType gtcWmtsType;

    public GtcGroupWmtsService(String requestLayerName, String groupName, String sortKeys)
            throws OWSException {
        super(requestLayerName);
        this.groupName = groupName;
        this.sortKeys = sortKeys;
        gtcWmtsType = GtcWmtsType.groupLayer;
        notExist();
    }

    @Override
    public void doServiceGetCapabilities(String capabilitiesShowName)
            throws GeoWebCacheException, OWSException {
        TC_ArcGISCacheLayer tcArcGISCacheLayer = groupMeta.getTGISCacheLayers().get(0);
        if (capabilitiesShowName == null) {
            capabilitiesShowName = groupName + ":" + sortKeys;
        }
        try {
            GridSubset defaultGridSubset = tcArcGISCacheLayer.getDefaultGridSubset();
            List<GridSet> gridSubsets = new ArrayList<>();
            gridSubsets.add(defaultGridSubset.getGridSet());
            GtcWMTSGetCapabilities gtcWmtsGetCapabilities =
                    new GtcWMTSGetCapabilities(
                            tcArcGISCacheLayer,
                            gridSubsets,
                            GtcWmtsType.groupLayer,
                            capabilitiesShowName,
                            groupName);
            gtcWmtsGetCapabilities.writeResponse();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public BufferedImage getImage(XyzDto xyzDto) throws GeoWebCacheException, OWSException {
        String[] xyzArray = xyzDto.getXyzArray();
        if (checkCache(gtcWmtsType, groupName, sortKeys, xyzArray)) {
            return getCacheImg(gtcWmtsType, groupName, sortKeys, xyzArray);
        } else {
            BufferedImage imageByServices = getImageByService(xyzDto);
            getCacheManger().saveCache(gtcWmtsType, groupName, sortKeys, xyzArray, imageByServices, 0L);
            return imageByServices;
        }
    }

    private BufferedImage getImageByService(XyzDto xyzDto) throws GeoWebCacheException, OWSException {
        if (!sortKeys.equals("all")) {
            String[] sortKey = sortKeys.split("_");
            List<TC_ArcGISCacheLayer> sortlayerGroup = new ArrayList<>();
            for (int i = 0; i < sortKey.length; i++) {
                String key = sortKey[i];
                for (int i1 = 0; i1 < tc_arcGISCacheLayers.size(); i1++) {
                    TC_ArcGISCacheLayer tcArcGISCacheLayer = tc_arcGISCacheLayers.get(i1);
                    if (key.equals(tcArcGISCacheLayer.getSortkey())) {
                        sortlayerGroup.add(tcArcGISCacheLayer);
                        break;
                    }
                }
            }
            if (!sortlayerGroup.isEmpty()) {
                tc_arcGISCacheLayers = sortlayerGroup;
            }
        } else {
            String keys = "";
            for (TC_ArcGISCacheLayer tc_arcGISCacheLayer : tc_arcGISCacheLayers) {
                keys += tc_arcGISCacheLayer.getSortkey() + "_";
            }
            sortKeys = keys;
        }

        GeowebcacheUtils geowebcacheUtils = Singleton.get(GeowebcacheUtils.class);
        WMTSService service = null;
        try {
            service = (WMTSService) geowebcacheUtils.findService("wmts");
        } catch (GeoWebCacheException e) {
            logger.info("geowebcache的wmts服务未初始化{}", e);
            return null;
        }
        BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        String tilecol = xyzDto.getXStr(); // x
        String tilerow = xyzDto.getYStr(); // y
        String tilematrix = xyzDto.getZStr(); // z
        ConveyorTile convTile = null;
        List<TC_ArcGISCacheLayer> in =
                groupMeta.isIn(tilematrix, tilecol, tilerow, tc_arcGISCacheLayers);
        if (GutilObject.isNotEmpty(in)) {
            tc_arcGISCacheLayers = in;
        } else {
            return image;
        }
        for (int i = 0; i < tc_arcGISCacheLayers.size(); i++) {
            TC_ArcGISCacheLayer tcArcGISCacheLayer1 = tc_arcGISCacheLayers.get(i);

            GridSubset defaultGridSubset = tcArcGISCacheLayer1.getDefaultGridSubset();
            BufferedImage read = null;
            if (defaultGridSubset == null) {
                continue;
            } else {

                String gridSubsetname = defaultGridSubset.getName();
                // 修改 tile的 切片方案的 key
                values.put(RequestParamter.tilematrixset.getCode(), gridSubsetname);
                // 修改 tile的 级别的加载方式
                values.put(RequestParamter.tilematrix.getCode(), gridSubsetname + ":" + tilematrix);
                // 设置图层名称
                values.put(RequestParamter.layer.getCode(), tcArcGISCacheLayer1.getName());
                InputStream inputStream = null;
                try {
                    // 这里 把获取 kvp的 方法放到  异常捕获里面，就可以实现到请求超出图层边界的时候 ，
                    // 不会导致因为异常抛出，请求中断
                    convTile = (ConveyorTile) service.getKvpConveyor(request, response, values);
                    convTile = tcArcGISCacheLayer1.getTile(convTile);
                    Resource blob = convTile.getBlob();
                    inputStream = blob.getInputStream();
                    read = ImageIO.read(inputStream);
                } catch (Exception e) {
                    continue;
                } finally {
                    IoUtil.close(inputStream);
                }
            }
            mergeImg(image, read);
        }
        if (convTile != null) {
            if (groupName.contains("yx")) {
                tranGreenTotransparent(image);
            }
        } else {
            throw new OWSException(400, "404", "瓦片", "无法找到图层对应瓦片");
        }
        return image;
    }

    /**
     * 检查服务是否存在
     *
     * @return
     */
    private boolean notExist() throws OWSException {
        HttpServletRequest request = GirHttpServletHelper.getRequest();
        groupMeta = TC_ArcGISCacheLayerGroup.getLayerGroup(groupName);
        if (GutilObject.isEmpty(groupMeta)) {
            logger.info(
                    "无法找到对应图层：服务名称：{},请求url：{} ",
                    groupName,
                    request.getRequestURI()
            );
            throw new OWSException(400, "404", "图层", "无法找到对应图层");
        } else {
            tc_arcGISCacheLayers = groupMeta.getTGISCacheLayers();
            return false;
        }
    }


}
