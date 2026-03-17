package cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext;

import java.awt.image.BufferedImage;

import cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext.dto.TileResult;
import cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext.dto.XyzDto;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.service.OWSException;

/**
 * @author ：zhangjun
 * @date ：Created in 2023/12/5 10:35
 * @description： GeoAir的wmts的服务类
 */
public interface TileGetterSupport {

    TileResult doService(XyzDto xyzDto) throws GeoWebCacheException, OWSException;

    void doServiceGetCapabilities(String capabilitiesShowName)
            throws GeoWebCacheException, OWSException;

    BufferedImage getImage(XyzDto xyzDto) throws GeoWebCacheException, OWSException;
}
