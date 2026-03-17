package cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.util.StrUtil;
import cn.geoair.base.log.GiLogger;
import cn.geoair.base.log.GirLogger;
import cn.geoair.base.util.GutilObject;
import cn.geoair.geoairteam.gwc.model.gwc.enums.GtcWmtsType;
import cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext.dto.XyzDto;
import cn.geoair.geoairteam.geowebcache.tianditu.layer.TC_ArcGISCacheLayer;
import cn.geoair.geoairteam.geowebcache.tianditu.layer.TC_TMSLayer;
import cn.geoair.geoairteam.geowebcache.tianditu.utils.GeowebcacheUtils;

import java.awt.image.BufferedImage;
import java.util.*;

import org.geowebcache.GeoWebCacheException;
import org.geowebcache.layer.TileLayer;
import org.geowebcache.service.OWSException;

/**
 * @author ：zfj
 * @date ：Created in 2023/12/5 16:02
 * @description： 混合组的服务，结合了xyz图层与arcGis的图层
 */
public class MixtureGroupService extends TileBaseGetterSupport {
    private static final GiLogger logger = GirLogger.getLoger(MixtureGroupService.class);

    String mixtureGroupName;

    LinkedHashMap<String, LayerMeta> layersMap;

    List<String> layers;

    static GeowebcacheUtils geowebcacheUtils;

    GtcWmtsType gtcWmtsType;

    /**
     * @param mixtureGroupName 混合的组名 layer|图层名@group|图层组名
     */
    public MixtureGroupService(String requestLayerName, String mixtureGroupName)
            throws OWSException, GeoWebCacheException {
        super(requestLayerName);
        this.mixtureGroupName = mixtureGroupName;
        layers = StrUtil.split(mixtureGroupName, "@");
        initLayerMeta();
        if (GutilObject.isEmpty(geowebcacheUtils)) {
            geowebcacheUtils = Singleton.get(GeowebcacheUtils.class);
        }

        gtcWmtsType = GtcWmtsType.mixtureGroupLayer;
    }

    @Override
    public void doServiceGetCapabilities(String capabilitiesShowName)
            throws GeoWebCacheException, OWSException {
        Set<Map.Entry<String, LayerMeta>> entries = layersMap.entrySet();
        List<TileGetterSupport> tileGetterSupports = new ArrayList<>();
        for (Map.Entry<String, LayerMeta> entry : entries) {
            LayerMeta value = entry.getValue();
            GtcWmtsType layerType = value.getLayerType();
            if (layerType.equals(GtcWmtsType.singletonLayer)) {
                tileGetterSupports.add(
                        new SingletonLayerService(requestLayerName, value.getLayerName()));
            } else if (layerType.equals(GtcWmtsType.groupLayer)) {
                String layerName = value.getLayerName();
                String[] split = layerName.split(":");
                tileGetterSupports.add(new GtcGroupWmtsService(requestLayerName, split[0], split[1]));
            } else {
                //  todo
            }
        }
        tileGetterSupports.get(0).doServiceGetCapabilities(requestLayerName); // 取得最前面一个的元数据
    }

    @Override
    public BufferedImage getImage(XyzDto xyzDto) throws GeoWebCacheException, OWSException {
        return getImageByServices(xyzDto);
    }

    BufferedImage getImageByServices(XyzDto xyzDto) throws OWSException {
        Set<Map.Entry<String, LayerMeta>> entries = layersMap.entrySet();
        List<TileGetterSupport> tileGetterSupports = new ArrayList<>();
        for (Map.Entry<String, LayerMeta> entry : entries) {
            LayerMeta value = entry.getValue();
            GtcWmtsType layerType = value.getLayerType();
            if (layerType.equals(GtcWmtsType.singletonLayer)) {
                tileGetterSupports.add(
                        new SingletonLayerService(requestLayerName, value.getLayerName()));
            } else if (layerType.equals(GtcWmtsType.groupLayer)) {
                String layerName = value.getLayerName();
                String[] split = layerName.split(":");
                tileGetterSupports.add(new GtcGroupWmtsService(requestLayerName, split[0], split[1]));
            } else if (layerType.equals(GtcWmtsType.xyzLayer)) {
                tileGetterSupports.add(new XyzLayerService(requestLayerName, value.getLayerName()));
            } else if (layerType.equals(GtcWmtsType.debugGridLayer)) {
                tileGetterSupports.add(new DebugGridLayerService(requestLayerName));
            } else {
                //  todo
            }
        }
        BufferedImage base = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        List<BufferedImage> bufferedImages = new ArrayList<>();
        for (TileGetterSupport tileGetterSupport : tileGetterSupports) {
            try {
                BufferedImage image = tileGetterSupport.getImage(xyzDto);
                if (GutilObject.isNotEmpty(image)) {
                    bufferedImages.add(image);
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
        //  图片叠加
        ListUtil.reverse(bufferedImages);
        for (int i = 0; i < bufferedImages.size(); i++) {
            BufferedImage bufferedImage = bufferedImages.get(i);
            mergeImg(base, bufferedImage);
        }
        return base;
    }

    void initLayerMeta() throws OWSException, GeoWebCacheException {
        layersMap = new LinkedHashMap<>();
        for (String layer : layers) {
            List<String> split = StrUtil.split(layer, ",");
            if (split.size() != 2) {
                throw new OWSException(
                        400,
                        "参数错误",
                        "图层",
                        StrUtil.format(
                                "图层格式不合法 -{}，正确组合格式为《layer,图层名@group,图层组名》", mixtureGroupName));
            }
            String layerName = split.get(1);
            String layerType = split.get(0);
            String layerNameByDB =
                    layerGroupAliasService.searchLayerStringByLayerGroupAlias(layerName);
            if (GutilObject.isNotEmpty(layerNameByDB)) {
                layerName = layerNameByDB;
            }
            if (layerType.equals("debugGrid")) {
                layersMap.put(
                        layer,
                        new LayerMeta()
                                .setLayerType(GtcWmtsType.debugGridLayer)
                                .setLayerName("debugGrid"));
            }
            if (layerType.equals("group")) {
                layersMap.put(
                        layer,
                        new LayerMeta()
                                .setLayerType(GtcWmtsType.groupLayer)
                                .setLayerName(layerName));
            } else {
                TileLayer tileLayer = tileLayerDispatcher.getTileLayer(layerName);
                if (tileLayer instanceof TC_ArcGISCacheLayer) {
                    layersMap.put(
                            layer,
                            new LayerMeta()
                                    .setLayerType(GtcWmtsType.singletonLayer)
                                    .setLayerName(layerName)
                                    .setTileLayer(tileLayer));
                } else if (tileLayer instanceof TC_TMSLayer) {
                    layersMap.put(
                            layer,
                            new LayerMeta()
                                    .setLayerType(GtcWmtsType.xyzLayer)
                                    .setLayerName(layerName)
                                    .setTileLayer(tileLayer));
                } else {
                    layersMap.put(
                            layer,
                            new LayerMeta()
                                    .setLayerType(GtcWmtsType.otherLayer)
                                    .setLayerName(layerName)
                                    .setTileLayer(tileLayer));
                }
            }
        }
    }

    static class LayerMeta {
        GtcWmtsType layerType;
        String layerName;
        TileLayer tileLayer;

        public GtcWmtsType getLayerType() {
            return layerType;
        }

        public LayerMeta setLayerType(GtcWmtsType layerType) {
            this.layerType = layerType;
            return this;
        }

        public TileLayer getTileLayer() {
            return tileLayer;
        }

        public LayerMeta setTileLayer(TileLayer tileLayer) {
            this.tileLayer = tileLayer;
            return this;
        }

        public String getLayerName() {
            return layerName;
        }

        public LayerMeta setLayerName(String layerName) {
            this.layerName = layerName;
            return this;
        }
    }
}
