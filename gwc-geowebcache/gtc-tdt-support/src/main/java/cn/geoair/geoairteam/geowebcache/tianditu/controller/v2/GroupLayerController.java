package cn.geoair.geoairteam.geowebcache.tianditu.controller.v2;

import cn.geoair.map.dynamic.tools.GirAdvTools;
import cn.geoair.web.GirWeb;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.geoair.base.util.GutilObject;
import cn.geoair.geoairteam.gwc.servface.gwc.LayerGroupAliasService;
import cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext.DebugGridLayerService;
import cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext.GtcGroupWmtsService;
import cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext.MixtureGroupService;
import cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext.SingletonLayerService;
import cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext.dto.TileResult;
import cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext.dto.XyzDto;
import cn.geoair.geoairteam.geowebcache.tianditu.utils.GISHubUtil;
import cn.geoair.geoairteam.geowebcache.tianditu.utils.TC_ResponseUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.service.OWSException;
import org.geowebcache.stats.RuntimeStats;
import org.geowebcache.util.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @description: 天地图控制器
 * @author: zhang_jun
 * @create: 2021-09-24 16:58
 */
@Controller
@RequestMapping("geoair")
public class GroupLayerController {

    private static Log LOGGER = LogFactory.getLog(GroupLayerController.class);

    @Autowired
    RuntimeStats runtimeStats;

    @Autowired
    LayerGroupAliasService layerGroupAliasService;

    @RequestMapping(value = "/debug/grid/wmts")
    @CrossOrigin
    public ResponseEntity<?> debugGridLayer()
            throws IOException, OWSException, GeoWebCacheException {
        Map<String, String> values = null;
        values = GISHubUtil.checkParameter("debugGrid");
        String requestme = values.get("request");
        DebugGridLayerService debugGridLayerService = new DebugGridLayerService("debugGrid");
        if (requestme.equalsIgnoreCase("getcapabilities")) {
            debugGridLayerService.doServiceGetCapabilities("debugGrid");
        } else {
            TileResult tileResult = debugGridLayerService.doService(XyzDto.byRequestParamter());
            return tileResult.toResponse(runtimeStats);
        }
        return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/debug/grid/xyz/{layerName}/{gridSrid}/{z}/{x}/{y}")
    @CrossOrigin
    public ResponseEntity<?> debugGridXyz(
            @PathVariable String layerName,
            @PathVariable String gridSrid,
            @PathVariable Integer z,
            @PathVariable Integer x,
            @PathVariable Integer y,
            @RequestParam(name = "orginType") String orginType)
            throws IOException, OWSException, GeoWebCacheException {
        int wmtsY = y;
        int xInt = x;
        int zInt = z;
        if (StringUtils.equals(orginType, "tms")) {
            if (Objects.equals(gridSrid, "4326") || Objects.equals(gridSrid, "4490")) {
                wmtsY = GirAdvTools.getTileGrid4326SeparateOpt().reverseY(wmtsY, zInt);
            } else {
                wmtsY = GirAdvTools.getTileGrid3857Opt().reverseY(wmtsY, zInt);
            }
        }
        XyzDto xyzDto = XyzDto.byXYZ(xInt, wmtsY, zInt, Integer.valueOf(gridSrid));
        DebugGridLayerService debugGridLayerService = new DebugGridLayerService(layerName);
        TileResult tileResult = debugGridLayerService.doService(xyzDto);
        return tileResult.toResponse(runtimeStats);
    }


    /**
     * 组图层请求方式
     *
     * @param requestLayerName
     */
    @RequestMapping("/group/{layer}/wmts")
    @CrossOrigin
    ResponseEntity<?> layerGroup(
            @PathVariable("layer") String requestLayerName)
            throws OWSException, IOException, GeoWebCacheException {
        Map<String, String> values = null;
        values = GISHubUtil.checkParameter(requestLayerName);
        String layerString =
                layerGroupAliasService.searchLayerStringByLayerGroupAlias(requestLayerName);
        if (GutilObject.isEmpty(layerString)) {
            layerString = requestLayerName;
        }
        String requestme = values.get("request");
        String[] split = layerString.split(":");
        if (split.length > 1) {
            GtcGroupWmtsService groupWmtsService =
                    new GtcGroupWmtsService(requestLayerName, split[0], split[1]);
            if (requestme.equalsIgnoreCase("getcapabilities")) {
                groupWmtsService.doServiceGetCapabilities(requestLayerName);
            } else {
                TileResult tileResult = groupWmtsService.doService(XyzDto.byRequestParamter());
                return tileResult.toResponse(runtimeStats);
            }
        } else {
            throw new OWSException(200, "10002", GirWeb.getRequest().getRequestURI(), "请求URl或者参数错误");
        }
        return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
    }


    /**
     * 单图层请求方式
     */
    @RequestMapping("/layer/{layer}/wmts/**")
    @CrossOrigin
    ResponseEntity<?> layerLayer(@PathVariable("layer") String requestLayerName)
            throws OWSException, IOException, GeoWebCacheException {
        Map<String, String> values = null;
        values = GISHubUtil.checkParameter(requestLayerName);
        String layerString =
                layerGroupAliasService.searchLayerStringByLayerGroupAlias(requestLayerName);
        if (GutilObject.isEmpty(layerString)) {
            layerString = requestLayerName;
        }
        String requestme = values.get("request");
        SingletonLayerService oneTiantiduWmtsService =
                new SingletonLayerService(requestLayerName, layerString);
        if ("getcapabilities".equalsIgnoreCase(requestme)) {
            oneTiantiduWmtsService.doServiceGetCapabilities(requestLayerName);
        } else {
            TileResult tileResult = oneTiantiduWmtsService.doService(XyzDto.byRequestParamter());
            return tileResult.toResponse(runtimeStats);
        }
        return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
    }

    /**
     * 图层图层组叠加
     *
     * @param requestLayerName layer,图层名@group,图层组名
     */
    @RequestMapping("/layer_group/{layer}/wmts")
    @CrossOrigin
    ResponseEntity<?> layerLayer_group(@PathVariable("layer") String requestLayerName)
            throws OWSException, GeoWebCacheException {
        Map<String, String> values = null;
        values = GISHubUtil.checkParameter(requestLayerName);
        String requestme = values.get("request");
        String layerString =
                layerGroupAliasService.searchLayerStringByLayerGroupAlias(requestLayerName);
        if (GutilObject.isEmpty(layerString)) {
            layerString = requestLayerName;
        }
        if (requestme.equalsIgnoreCase("getcapabilities")) {
            MixtureGroupService mixtureGroupService =
                    new MixtureGroupService(requestLayerName, layerString);
            mixtureGroupService.doServiceGetCapabilities(requestLayerName);
        } else {
            MixtureGroupService mixtureGroupService =
                    new MixtureGroupService(requestLayerName, layerString);
            TileResult tileResult = mixtureGroupService.doService(XyzDto.byRequestParamter());
            return tileResult.toResponse(runtimeStats);
        }
        return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
    }

    /**
     * 各种请求方式都兼容的方式 （新增，但是暂时没有上线）
     */
    @RequestMapping("/mix/{layer}/wmts/**")
    @CrossOrigin
    ResponseEntity<?> mixLayer(
            @PathVariable("layer") String requestLayerName, HttpServletRequest request)
            throws OWSException, IOException, GeoWebCacheException {
        String layerString =
                layerGroupAliasService.searchLayerStringByLayerGroupAlias(requestLayerName);
        if (GutilObject.isEmpty(layerString)) {
            layerString = requestLayerName;
        }
        if (layerString.contains("@")) {
            return layerLayer_group(requestLayerName);
        } else if (layerString.contains(":")) {
            return layerGroup(requestLayerName);
        } else {
            return layerLayer(requestLayerName);
        }
    }

    @GetMapping("/xyz/{layerName}/{gridSrid}/{z}/{x}/{y}")
    @CrossOrigin
    public Object getXyzTileXyz(
            @PathVariable String layerName,
            @PathVariable String gridSrid,
            @PathVariable Integer z, // 层级
            @PathVariable Integer x, // X坐标（列号）
            @PathVariable Integer y,
            @RequestParam(name = "zxyType") String zxyType,
            @RequestParam(name = "orginType") String orginType
    ) {
        try {
            int wmtsY = y;
            int xInt = x;
            int zInt = z;

            if (zxyType.equals("zyx")) {
                int temp = xInt;
                xInt = wmtsY;
                wmtsY = temp;
            }
            if (StringUtils.equals(orginType, "tms")) {
                if (Objects.equals(gridSrid, "4326") || Objects.equals(gridSrid, "4490")) {
                    wmtsY = GirAdvTools.getTileGrid4326SeparateOpt().reverseY(wmtsY, zInt);
                } else {
                    wmtsY = GirAdvTools.getTileGrid3857Opt().reverseY(wmtsY, zInt);
                }
            }
            XyzDto xyzDto = XyzDto.byXYZ(xInt, wmtsY, zInt, Integer.valueOf(gridSrid));
            String layerString =
                    layerGroupAliasService.searchLayerStringByLayerGroupAlias(layerName);
            if (GutilObject.isEmpty(layerString)) {
                layerString = layerName;
            }
            if (layerString.contains("@")) {
                MixtureGroupService mixtureGroupService =
                        new MixtureGroupService(layerName, layerString);
                TileResult tileResult = mixtureGroupService.doService(xyzDto);
                return tileResult.toResponse(runtimeStats);
            } else if (layerString.contains(":")) {
                String[] split = layerString.split(":");
                if (split.length > 1) {
                    GtcGroupWmtsService groupWmtsService =
                            new GtcGroupWmtsService(layerString, split[0], split[1]);
                    TileResult tileResult = groupWmtsService.doService(xyzDto);
                    return tileResult.toResponse(runtimeStats);
                }
            } else {
                SingletonLayerService singletonLayerService =
                        new SingletonLayerService(layerName, layerString);
                TileResult tileResult = singletonLayerService.doService(xyzDto);
                return tileResult.toResponse(runtimeStats);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    /**
     * 图层图层组叠加
     *
     * @param requestLayerName layer|图层名@group|图层组名
     */
    @RequestMapping("/v2/{layer}/layer_group/wmts")
    @CrossOrigin
    ResponseEntity<?> v2layerLayer_group(@PathVariable("layer") String requestLayerName)
            throws OWSException, IOException, GeoWebCacheException {
        return layerLayer_group(requestLayerName);
    }

    /**
     * 组图层请求方式
     */
    @RequestMapping("/v2/{layer}/group/wmts")
    @CrossOrigin
    ResponseEntity<?> v2layerGroup(
            @PathVariable("layer") String requestLayerName, HttpServletRequest request)
            throws OWSException, IOException, GeoWebCacheException {
        return layerGroup(requestLayerName);
    }

    /**
     * 单图层请求方式
     */
    @RequestMapping("/v2/{layer}/layer/wmts/**")
    @CrossOrigin
    ResponseEntity<?> v2layerLayer(@PathVariable("layer") String layerName)
            throws OWSException, IOException, GeoWebCacheException {
        return layerLayer(layerName);
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
