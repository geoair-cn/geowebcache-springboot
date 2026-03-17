package cn.geoair.geoairteam.geowebcache.tianditu.controller.v2;

import cn.geoair.map.dynamic.tools.GirAdvTools;
import cn.hutool.core.util.StrUtil;
import cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext.XyzLayerService;
import cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext.dto.TileResult;
import cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext.dto.XyzDto;
import cn.geoair.geoairteam.geowebcache.tianditu.utils.GISHubUtil;
 
import cn.geoair.geoairteam.geowebcache.tianditu.utils.TC_ResponseUtils;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.service.OWSException;
import org.geowebcache.stats.RuntimeStats;
import org.geowebcache.util.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author ：zfj
 * @date ：Created in 2023/12/4 15:47
 * @description： xyz的图层叠加处理
 */
@Controller
@RequestMapping("geoair")
public class XyzLayerController {
    private static Log LOGGER = LogFactory.getLog(XyzLayerController.class);
    @Autowired
    RuntimeStats runtimeStats;

    /**
     * xyz数据源
     *
     * @throws OWSException
     * @throws IOException
     */
    @RequestMapping("/xyz/{layer}/wmts")
    @CrossOrigin
    ResponseEntity<?> layerXYZ(@PathVariable("layer") String layerName)
            throws OWSException, GeoWebCacheException {
        Map<String, String> values = null;
        values = GISHubUtil.checkParameter(layerName);
        String requestme = values.get("request");
        XyzLayerService xyzLayerService = new XyzLayerService(layerName, layerName);
        if ("getcapabilities".equalsIgnoreCase(requestme)) {
            xyzLayerService.doServiceGetCapabilities(layerName);
        } else {
            TileResult tileResult = xyzLayerService.doService(XyzDto.byRequestParamter());
            return tileResult.toResponse(runtimeStats);
        }
        return new ResponseEntity<Object>(HttpStatus.OK);
    }


    /**
     * 用于处理异常的
     *
     * @return
     */
    @ExceptionHandler({Exception.class})
    public void exception(Exception e) {

        if (e instanceof OWSException) {
            ResponseUtils.writeErrorAsXML(
                    GISHubUtil.getResponse(), 400, e.toString(), runtimeStats);
            return;
        }

        if (e instanceof IOException) {
            ResponseUtils.writeErrorPage(
                    GISHubUtil.getResponse(), 400, e.getMessage(), runtimeStats);
            return;
        }

        if (e instanceof GeoWebCacheException) {
            GeoWebCacheException geoWebCacheException = (GeoWebCacheException) e;
            ResponseUtils.writeErrorPage(
                    GISHubUtil.getResponse(), 400, geoWebCacheException.getMessage(), runtimeStats);
            return;
        }

        if (e != null) {
            LOGGER.warn(
                    StrUtil.format(
                            "GeoAir日志：加载服务出现异常：服务名称：{},请求url：{},异常原因{}",
                            e.getCause(),
                            GISHubUtil.getRequest().getRequestURI(),
                            e.getMessage()));
            LOGGER.error(e, e);
            TC_ResponseUtils.writeErrorAsXML(
                    GISHubUtil.getResponse(),
                    200,
                    new OWSException(
                            200,
                            "10001",
                            GISHubUtil.getRequest().getRequestURI(),
                            "加载服务出现异常,该服务不可用")
                            .toString());
        }
    }
}
