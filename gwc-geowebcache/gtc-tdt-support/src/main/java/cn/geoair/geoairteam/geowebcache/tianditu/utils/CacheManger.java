package cn.geoair.geoairteam.geowebcache.tianditu.utils;

import cn.geoair.base.Gir;
import cn.hutool.core.thread.ThreadUtil;

import cn.geoair.geoairteam.gwc.model.gwc.enums.GtcWmtsType;
import cn.geoair.geoairteam.gwc.servface.gwc.LayerCacheService;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CacheManger {

    private static Log log = LogFactory.getLog(CacheManger.class);

    LayerCacheService layerCacheService;

    ExecutorService executorService = ThreadUtil.newExecutor();

    private LayerCacheService getLayerCacheService() {
        if (layerCacheService == null) {
            layerCacheService = Gir.beans.getBean(LayerCacheService.class);
        }
        return layerCacheService;
    }

    public void saveCache(
            GtcWmtsType gtcWmtsType,
            String layerName,
            String sortKey,
            String[] xyz,
            BufferedImage bufferedImage,
            Long timeout) {
        try {
            executorService.execute(
                    () ->
                            getLayerCacheService()
                                    .putCache(
                                            gtcWmtsType,
                                            layerName,
                                            sortKey,
                                            xyz,
                                            bufferedImage,
                                            timeout));
        } catch (Exception e) {
            log.error(e);
        }
    }

    public boolean hasCache(
            GtcWmtsType gtcWmtsType, String groupName, String sortKey, String[] xyz) {
        return getLayerCacheService().hasCache(gtcWmtsType, groupName, sortKey, xyz);
    }

    public BufferedImage getCacheImg(
            GtcWmtsType gtcWmtsType, String groupName, String sortKey, String[] xyz) {
        try {
            BufferedImage cacheImg =
                    getLayerCacheService().getCacheImg(gtcWmtsType, groupName, sortKey, xyz);
            return cacheImg;
        } catch (Exception e) {
            log.error(e);
        }
        return null;
    }

    /**
     * 清空图形缓存
     *
     * @param groupName
     * @return
     */
    public boolean emptyDrawingCache(String groupName, String sortKey) {
        if (sortKey == null) {
            getLayerCacheService().delCacheByGroupName(groupName);
        } else {
            getLayerCacheService().delCacheByGroupNameNAndSortKey(groupName, sortKey);
        }
        return true;
    }
}
