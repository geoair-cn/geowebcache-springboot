package cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.geoair.base.log.GiLogger;
import cn.geoair.base.log.GirLogger;
import cn.geoair.geoairteam.gwc.model.gwc.enums.GtcWmtsType;
import cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext.dto.XyzDto;
import cn.geoair.geoairteam.geowebcache.tianditu.layer.TC_XYZLayer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

import org.geowebcache.GeoWebCacheException;
import org.geowebcache.service.OWSException;

/**
 * @author ：zfj
 * @date ：Created in 2023/12/5 10:53
 * @description： xyz的wmts服务启动类
 */
public class XyzLayerService extends TmsLayerService {

    private static final GiLogger logger = GirLogger.getLoger(XyzLayerService.class);


    public XyzLayerService(String requestLayerName, String layerName) throws OWSException {
        super(requestLayerName, layerName);
        gtcWmtsType = GtcWmtsType.xyzLayer;

    }


    @Override
    public BufferedImage getImage(XyzDto xyzDto) throws GeoWebCacheException, OWSException {
        TC_XYZLayer tc_tmsLayer = (TC_XYZLayer) tileLayer;
        String loadMethod = tc_tmsLayer.getLoadMethod();
        if (loadMethod.equals("local")) {
            String localPath = tc_tmsLayer.getLocalPath();
            localPath = StrUtil.replace(localPath, "{x}", String.valueOf(xyzDto.getX()));
            localPath = StrUtil.replace(localPath, "{y}", String.valueOf(xyzDto.getYStr()));
            localPath = StrUtil.replace(localPath, "{z}", String.valueOf(xyzDto.getZStr()));
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
            httpBasePath = StrUtil.replace(httpBasePath, "{x}", xyzDto.getXStr());
            httpBasePath = StrUtil.replace(httpBasePath, "{y}", xyzDto.getYStr());
            httpBasePath = StrUtil.replace(httpBasePath, "{z}", xyzDto.getZStr());
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


}
