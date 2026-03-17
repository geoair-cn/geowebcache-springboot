package cn.geoair.geoairteam.geowebcache.tianditu.controller.sys;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.geoair.base.bean.GirBeanHelper;
import cn.geoair.geoairteam.gwc.servface.gwc.LayerGroupAliasService;
import cn.geoair.web.util.GirHttpServletHelper;
import cn.geoair.geoairteam.gwc.service.config.GeoWebCacheConfig;
import cn.geoair.geoairteam.geowebcache.tianditu.demo.Demo;
import cn.geoair.geoairteam.geowebcache.tianditu.demo.Demo1;
import cn.geoair.geoairteam.geowebcache.tianditu.group.GroupMeta;
import cn.geoair.geoairteam.geowebcache.tianditu.group.TC_ArcGISCacheLayerGroup;
import cn.geoair.geoairteam.geowebcache.tianditu.utils.CacheManger;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.GeoWebCacheExtensions;
import org.geowebcache.config.XMLConfiguration;
import org.geowebcache.grid.GridSetBroker;
import org.geowebcache.layer.TileLayerDispatcher;
import org.geowebcache.stats.RuntimeStats;
import org.geowebcache.storage.DefaultStorageFinder;
import org.geowebcache.util.ServletUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author ：zfj
 * @date ：Created in 2023/12/4 15:48
 * @description： 系统相关的控制器处理
 */
@Controller
@RequestMapping("geoair")
public class SystemController implements ApplicationContextAware {
    private static Log LOGGER = LogFactory.getLog(SystemController.class);
    @Autowired RuntimeStats runtimeStats;

    private ApplicationContext applicationContext;
    @Autowired LayerGroupAliasService layerGroupAliasService;

    /**
     * @param request
     * @param response
     * @throws GeoWebCacheException
     */
    @RequestMapping("/demo")
    @CrossOrigin
    void demo(HttpServletRequest request, HttpServletResponse response)
            throws GeoWebCacheException {
        String action = "";
        String layer = request.getParameter("layer");
        if (layer != null) {
            String s = layerGroupAliasService.searchLayerStringByLayerGroupAlias(layer);
            if (s != null) {
                layer = s;
            }
            action = layer;
        }
        TileLayerDispatcher tileLayerDispatcher =
                GeoWebCacheExtensions.bean(TileLayerDispatcher.class);
        GridSetBroker gridSetBroker = GeoWebCacheExtensions.bean(GridSetBroker.class);

        Demo.makeMap(tileLayerDispatcher, gridSetBroker, action, request, response);
    }

    /**
     * @param request
     * @param response
     * @throws GeoWebCacheException
     */
    @RequestMapping("/demov2")
    @CrossOrigin
    void demov2(HttpServletRequest request, HttpServletResponse response)
            throws GeoWebCacheException {
        String action = "";
        String layer = request.getParameter("layer");
        if (layer != null) {
            action = layer;
        }
        TileLayerDispatcher tileLayerDispatcher =
                GeoWebCacheExtensions.bean(TileLayerDispatcher.class);
        GridSetBroker gridSetBroker = GeoWebCacheExtensions.bean(GridSetBroker.class);

        Demo1.makeMap(tileLayerDispatcher, gridSetBroker, action, request, response);
    }

    /**
     * 重载图层配置
     *
     * @return
     */
    @RequestMapping("/reloadLayerGroup")
    @CrossOrigin
    @ResponseBody
    public ResponseEntity<?> reloadLayerGroup() {
        StringBuilder doc = new StringBuilder();
        doc.append(
                "<html>\n"
                        + ServletUtils.gwcHtmlHeader("../", "图层组 重载数据")
                        + "<body>\n"
                        + ServletUtils.gwcHtmlLogoLink("../"));

        try {
            HashMap<String, GroupMeta> layerHashMap = TC_ArcGISCacheLayerGroup.getGroupMetas();
            layerHashMap.clear();
            GeoWebCacheExtensions.reinitialize(applicationContext);
            String info = "重新加载了 图层组. 读取到 " + layerHashMap.size() + "个图层组来自geowebcache.xml配置文件";

            doc.append("<p>" + info + "</p>");

            doc.append("<p>请注意，此功能尚未经过严格测试，如果遇到任何问题，请重新加载 servlet。另请注意，您必须截断任何已更改图层的图块。</p>");

        } catch (Exception e) {
            doc.append(
                    "<p>重新加载图层组配置时出现问题:<br>\n"
                            + e.getMessage()
                            + "\n<br>"
                            + " 如果您认为这是一个错误，请在以下位置提交票证 "
                            + "<a href=\"http://geowebcache.org\">GeoWebCache.org</a>"
                            + "</p>");
        }

        doc.append("<p><a href=\"../demo\">返回上一页</a></p>\n");
        doc.append("</body></html>");

        return new ResponseEntity<Object>(doc.toString(), HttpStatus.OK);
    }

    /**
     * 预览配置文件
     *
     * @return
     */
    @RequestMapping("/PreviewXML")
    @CrossOrigin
    @ResponseBody
    void PreviewXML() {
        HttpServletResponse response = GirHttpServletHelper.getResponse();
        response.setContentType("text/xml;charset=UTF-8");

        GeoWebCacheConfig bean = GirBeanHelper.getProvider()
                .getBean(GeoWebCacheConfig.class);

        File file =
                new File(
                        bean.getGwcCacheDir()
                                + File.separator
                                + XMLConfiguration.DEFAULT_CONFIGURATION_FILE_NAME);
        response.setContentLength(Math.toIntExact(file.length()));
        ServletOutputStream outputStream = null;
        BufferedInputStream inputStream = null;
        try {
            outputStream = response.getOutputStream();
            inputStream = FileUtil.getInputStream(file);
            IoUtil.copy(inputStream, outputStream);
        } catch (Exception e) {

        } finally {
            IoUtil.close(outputStream);
            IoUtil.close(inputStream);
        }
    }

    /**
     * 预览配置文件模板
     *
     * @return
     */
    @RequestMapping("/PreviewXMLTemplate")
    @CrossOrigin
    @ResponseBody
    void PreviewXMLTemplate() {
        HttpServletResponse response = GirHttpServletHelper.getResponse();
        response.setContentType("text/xml;charset=UTF-8");
        ClassPathResource classPathResource =
                new ClassPathResource(XMLConfiguration.DEFAULT_CONFIGURATION_FILE_NAME);
        ServletOutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            outputStream = response.getOutputStream();
            inputStream = classPathResource.getStream();
            IoUtil.copy(inputStream, outputStream);
        } catch (Exception e) {

        } finally {
            IoUtil.close(outputStream);
            IoUtil.close(inputStream);
        }
    }

    /**
     * 重载图层配置
     *
     * @return
     */
    @RequestMapping("/emptyDrawingCache/{groupName}")
    @CrossOrigin
    @ResponseBody
    public ResponseEntity<?> EmptyDrawingCache(@PathVariable("groupName") String groupName) {

        CacheManger cacheManger = GeoWebCacheExtensions.bean(CacheManger.class);

        cacheManger.emptyDrawingCache(groupName, null);

        StringBuilder doc = new StringBuilder();
        doc.append(
                "<html>\n"
                        + ServletUtils.gwcHtmlHeader("../", "清空图层组缓存")
                        + "<body>\n"
                        + ServletUtils.gwcHtmlLogoLink("../"));
        String info = "清空图形缓存成功. ";

        doc.append("<p>" + info + "</p>");

        doc.append("<p>请注意，此功能尚未经过严格测试，如果遇到任何问题，请重新加载 servlet。另请注意，您必须截断任何已更改图层的图块。</p>");

        doc.append("<p><a href=\"../demo\">返回上一页</a></p>\n");
        doc.append("</body></html>");

        return new ResponseEntity<Object>(doc.toString(), HttpStatus.OK);
    }

    @RequestMapping("/**")
    @CrossOrigin
    static String redirect(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        int geoair = requestURI.indexOf("geoair");
        String prefix = "";
        String substring = requestURI.substring(geoair);
        String[] split = substring.split("/");
        for (int i = 1; i < split.length; i++) {
            prefix = prefix + "/" + split[i];
        }
        return prefix;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
