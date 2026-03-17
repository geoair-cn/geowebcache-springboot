/**
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Lesser General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Arne Kepp, The Open Planning Project, Copyright 2008
 */
package org.geowebcache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.geoair.geoairteam.gwc.service.config.GeoWebCacheConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geowebcache.config.*;
import org.geowebcache.conveyor.Conveyor;
import org.geowebcache.conveyor.Conveyor.CacheResult;
import org.geowebcache.conveyor.ConveyorTile;
import org.geowebcache.demo.Demo;
import org.geowebcache.filter.request.RequestFilterException;
import org.geowebcache.filter.security.SecurityDispatcher;
import org.geowebcache.grid.GridSetBroker;
import org.geowebcache.io.ByteArrayResource;
import org.geowebcache.io.Resource;
import org.geowebcache.layer.BadTileException;
import org.geowebcache.layer.TileLayer;
import org.geowebcache.layer.TileLayerDispatcher;
import org.geowebcache.service.HttpErrorCodeException;
import org.geowebcache.service.OWSException;
import org.geowebcache.service.Service;
import org.geowebcache.stats.RuntimeStats;
import org.geowebcache.storage.*;
import org.geowebcache.storage.blobstore.memory.CacheStatistics;
import org.geowebcache.storage.blobstore.memory.MemoryBlobStore;
import org.geowebcache.util.ResponseUtils;
import org.geowebcache.util.ServletUtils;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.xml.sax.SAXException;

/**
 * This is the main router for requests of all types.
 */
public class GeoWebCacheDispatcher extends AbstractController {
    private static Log log = LogFactory.getLog(org.geowebcache.GeoWebCacheDispatcher.class);

    public static final String TYPE_SERVICE = "service";

    public static final String TYPE_REST = "rest";

    public static final String TYPE_DEMO = "demo";

    public static final String TYPE_HOME = "home";

    private TileLayerDispatcher tileLayerDispatcher = null;

    private DefaultStorageFinder defaultStorageFinder = null;

    private GridSetBroker gridSetBroker = null;

    private StorageBroker storageBroker;

    private BlobStoreAggregator blobStoreAggregator;

    private RuntimeStats runtimeStats;

    private Map<String, Service> services = null;

    private Resource blankTile = null;

    private String servletPrefix = null;

    private BaseConfiguration mainConfiguration;

    private SecurityDispatcher securityDispatcher;
    private ServletContext servletContext;

    /**
     * Should be invoked through Spring
     */
    public GeoWebCacheDispatcher(
            TileLayerDispatcher tileLayerDispatcher,
            GridSetBroker gridSetBroker,
            StorageBroker storageBroker,
            BlobStoreAggregator blobStoreAggregator,
            ServerConfiguration mainConfiguration,
            ServletContext servletContext,
            RuntimeStats runtimeStats) {
        super();
        this.tileLayerDispatcher = tileLayerDispatcher;
        this.gridSetBroker = gridSetBroker;
        this.runtimeStats = runtimeStats;
        this.storageBroker = storageBroker;
        this.blobStoreAggregator = blobStoreAggregator;
        this.mainConfiguration = mainConfiguration;
        this.servletContext = servletContext;

        if (mainConfiguration.isRuntimeStatsEnabled()) {
            this.runtimeStats.start();
        } else {
            runtimeStats = null;
        }
    }

    public void setStorageBroker() {
        // This is just to force initialization
        log.debug("GeoWebCacheDispatcher received StorageBroker : " + storageBroker.toString());
    }

    public void setDefaultStorageFinder(DefaultStorageFinder defaultStorageFinder) {
        this.defaultStorageFinder = defaultStorageFinder;
    }

    /**
     * GeoServer and other solutions that embedded this dispatcher will prepend a path, this is used
     * to remove it.
     */
    public void setServletPrefix(String servletPrefix) {
        if (!servletPrefix.startsWith("/")) {
            this.servletPrefix = "/" + servletPrefix;
        } else {
            this.servletPrefix = servletPrefix;
        }

        log.info("Invoked setServletPrefix(" + servletPrefix + ")");
    }

    /**
     * GeoServer and other solutions that embedded this dispatcher will prepend a path, this is used
     * to remove it.
     */
    public String getServletPrefix() {
        return servletPrefix;
    }

    /**
     * Services convert HTTP requests into the internal grid representation and specify what layer
     * the response should come from.
     *
     * <p>The application context is scanned for objects extending Service, thereby making it easy
     * to add new services.
     */
    private Map<String, Service> loadServices() {
        log.info("加载 GWC Service 拓展服务...");

        List<Service> plugins = GeoWebCacheExtensions.extensions(Service.class);
        Map<String, Service> services = new HashMap<>();
        // Give all service objects direct access to the tileLayerDispatcher
        for (Service aService : plugins) {
            services.put(aService.getPathName(), aService);
        }
        log.info("完成geowebcache拓展服务的加载 分别是 : " + new ArrayList<>(services.keySet()));
        return services;
    }

    private void loadBlankTile() {
        String blankTilePath =
                defaultStorageFinder.findEnvVar(DefaultStorageFinder.GWC_BLANK_TILE_PATH);

        if (blankTilePath != null) {
            File fh = new File(blankTilePath);
            if (fh.exists() && fh.canRead() && fh.isFile()) {
                long fileSize = fh.length();
                blankTile = new ByteArrayResource(new byte[(int) fileSize]);
                try {
                    loadBlankTile(blankTile, fh.toURI().toURL());
                } catch (IOException e) {
                    log.debug("无法加载空白瓦片", e);
                }

                if (fileSize == blankTile.getSize()) {
                    log.info("Loaded blank tile from " + blankTilePath);
                } else {
                    log.error("Failed to load blank tile from " + blankTilePath);
                }

                return;
            } else {
                log.error("" + blankTilePath + " does not exist or is not readable.");
            }
        }

        // Use the built-in one:
        try {
            URL url = GeoWebCacheDispatcher.class.getResource("blank.png");
            blankTile = new ByteArrayResource();
            loadBlankTile(blankTile, url);
            int ret = (int) blankTile.getSize();
            log.info("Read " + ret + " from blank PNG file (expected 425).");
        } catch (IOException ioe) {
            log.error(ioe);
        }
    }

    private void loadBlankTile(Resource blankTile, URL source) throws IOException {
        try (InputStream inputStream = source.openStream();
             ReadableByteChannel ch = Channels.newChannel(inputStream)) {
            blankTile.transferFrom(ch);
        } catch (IOException e) {
            log.error("加载空白瓦片错误", e);
        }
    }

    /**
     * Spring function for MVC, this is the entry point for the application.
     *
     * <p>If a tile is requested the request will be handed off to handleServiceRequest.
     */
    @Override
    protected ModelAndView handleRequestInternal(
            HttpServletRequest request, HttpServletResponse originalResponse) throws Exception {

        HttpServletResponseWrapper response =
                new HttpServletResponseWrapper(originalResponse) {
                    @Override
                    public ServletOutputStream getOutputStream() throws IOException {
                        return new DispatcherOutputStream(super.getOutputStream());
                    }
                };

        // Break the request into components, {type, service name}
        String[] requestComps = null;
        try {
            String normalizedURI =
                    request.getRequestURI().replaceFirst(request.getContextPath(), "");

            if (servletPrefix != null) {
                normalizedURI =
                        normalizedURI.replaceFirst(
                                servletPrefix,
                                ""); // getRequestURI().replaceFirst(request.getContextPath()+,
                // "");
            }
            requestComps = parseRequest(normalizedURI);
            // requestComps = parseRequest(request.getRequestURI());
        } catch (GeoWebCacheException gwce) {
            ResponseUtils.writeErrorPage(response, 400, gwce.getMessage(), runtimeStats);
            return null;
        }

        try {
            if (requestComps == null || requestComps[0].equalsIgnoreCase(TYPE_HOME)) {
                handleFrontPage(request, response);
            } else if (requestComps[0].equalsIgnoreCase(TYPE_SERVICE)) {
                handleServiceRequest(requestComps[1], request, response);
            } else if (requestComps[0].equalsIgnoreCase(TYPE_DEMO)
                    || requestComps[0].equalsIgnoreCase(TYPE_DEMO + "s")) {
                handleDemoRequest(requestComps[1], request, response);
            } else {
                ResponseUtils.writeErrorPage(
                        response, 404, "未知请求路径: " + requestComps[0], runtimeStats);
            }
        } catch (HttpErrorCodeException e) {
            ResponseUtils.writeFixedResponse(
                    response,
                    e.getErrorCode(),
                    "text/plain",
                    new ByteArrayResource(e.getMessage()),
                    //                    new ByteArrayResource(e.getMessage().getBytes()),
                    CacheResult.OTHER,
                    runtimeStats);
        } catch (RequestFilterException e) {

            RequestFilterException reqE = e;
            reqE.setHttpInfoHeader(response);

            ResponseUtils.writeFixedResponse(
                    response,
                    reqE.getResponseCode(),
                    reqE.getContentType(),
                    reqE.getResponse(),
                    CacheResult.OTHER,
                    runtimeStats);
        } catch (OWSException e) {
            ResponseUtils.writeFixedResponse(
                    response,
                    e.getResponseCode(),
                    e.getContentType(),
                    e.getResponse(),
                    CacheResult.OTHER,
                    runtimeStats);
        } catch (SecurityException e) {
            ResponseUtils.writeFixedResponse(
                    response,
                    403,
                    "text/plain",
                    new ByteArrayResource("未授权"),
                    //                    new ByteArrayResource("未授权".getBytes()),
                    CacheResult.OTHER,
                    runtimeStats);
            log.warn(e.getMessage());
        } catch (Exception e) {
            if (!(e instanceof BadTileException) || log.isDebugEnabled()) {
                log.error(e.getMessage() + " " + request.getRequestURL().toString());
            }

            ResponseUtils.writeErrorPage(response, 400, e.getMessage(), runtimeStats);

            if (!isClientStreamAbortedException(e)) {
                log.error("Request failed", e);
            } else if (log.isDebugEnabled()) {
                log.debug("Request failed, client closed connection", e);
            }
        }
        return null;
    }

    private boolean isClientStreamAbortedException(Throwable t) {
        Throwable current = t;
        while (current != null
                && !(current instanceof ClientStreamAbortedException)
                && !(current instanceof HttpErrorCodeException)) {
            if (current instanceof SAXException) current = ((SAXException) current).getException();
            else current = current.getCause();
        }
        if (current instanceof ClientStreamAbortedException) {
            log.debug("Client has closed stream", t);
            return true;
        }
        return false;
    }

    /**
     * Destroy function, has to be referenced in bean declaration: <bean ...
     * destroy="destroy">...</bean>
     */
    public void destroy() {
        log.info("webcache http请求分发器因为spring环境启动异常正在停止");
    }

    /**
     * Essentially this slices away the prefix, leaving type and request
     *
     * @return {type, service}ervletPrefix
     */
    private String[] parseRequest(String servletPath) throws GeoWebCacheException {
        String[] retStrs = new String[2];
        String[] splitStr = servletPath.split("/");

        if (splitStr == null || splitStr.length < 2) {
            return null;
        }

        retStrs[0] = splitStr[1];
        if (splitStr.length > 2) {
            retStrs[1] = splitStr[2];
        }
        return retStrs;
    }

    /**
     * This is the main method for handling service requests. See comments in the code.
     */
    private void handleServiceRequest(
            String serviceStr, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        Conveyor conv = null;

        // 1) Figure out what Service should handle this request
        Service service = findService(serviceStr);

        // 2) Find out what layer will be used and how
        conv = service.getConveyor(request, response);
        final String layerName = conv.getLayerId();

        final TileLayer layer;
        if (Objects.nonNull(layerName)) {
            layer = tileLayerDispatcher.getTileLayer(layerName);
            if (!layer.isEnabled()) {
                throw new OWSException(400, "参数错误", "图层", "图层： '" + layerName + "' 未启用");
            }
            if (conv instanceof ConveyorTile) {
                ((ConveyorTile) conv).setTileLayer(layer);
            }
        } else {
            layer = null;
        }

        // Check where this should be dispatched
        if (conv.reqHandler == Conveyor.RequestHandler.SERVICE) {
            // A3 The service object takes it from here
            service.handleRequest(conv);
        } else {
            ResponseUtils.writeTile(
                    getSecurityDispatcher(),
                    conv,
                    layerName,
                    tileLayerDispatcher,
                    defaultStorageFinder,
                    runtimeStats);
        }
    }

    private void handleDemoRequest(
            String action, HttpServletRequest request, HttpServletResponse response)
            throws GeoWebCacheException {
        Demo.makeMap(tileLayerDispatcher, gridSetBroker, action, request, response);
    }

    /**
     * Helper function for looking up the service that should handle the request.
     *
     * @param serviceStr name of the service
     */
    private Service findService(String serviceStr) throws GeoWebCacheException {
        if (this.services == null) {
            this.services = loadServices();
            loadBlankTile();
        }

        // E.g. /wms/test -> /wms
        Service service = services.get(serviceStr);
        if (service == null) {
            if (serviceStr == null || serviceStr.isEmpty()) {
                serviceStr = ", try service/&lt;name of service&gt;";
            } else {
                serviceStr = " \"" + serviceStr + "\"";
            }
            throw new GeoWebCacheException("无法找到该服务的处理器" + serviceStr);
        }
        return service;
    }

    /**
     * Create a minimalistic frontpage
     */
    private void handleFrontPage(HttpServletRequest request, HttpServletResponse response) {

        String baseUrl = null;

        if (request.getRequestURL().toString().endsWith("/")
                || request.getRequestURL().toString().endsWith("home")) {
            baseUrl = "";
        } else {
            String[] strs = request.getRequestURL().toString().split("/");
            baseUrl = strs[strs.length - 1] + "/";
        }

        StringBuilder str = new StringBuilder();

        String commitId = StrUtil.format("geoairTeam：[{}]:{}", GeoWebCacheConfig.getCommitBranch(), GeoWebCacheConfig.getCommitId());
        String time = GeoWebCacheConfig.getCommitTime();

        //        String version = GeoWebCache.getVersion();
        //        String version = "{2021年11月GeoAir发布版本}";
        //        String version = "{2024年01月GeoAir发布版本}";
        //        String version = "{2024年08月GeoAir发布版本}{无缓存实现}";
//        String version = "{2025年06月GeoAir发布版本}";
//        String commitId = "{2025年06月06日}";
        //        if (version == null) {
        //            version = "{NO VERSION INFO IN MANIFEST}";
        //        }
        //        if (commitId == null) {
        //            commitId = "{NO BUILD INFO IN MANIFEST}";
        //        }

        str.append(
                "<html>\n"
                        + ServletUtils.gwcHtmlHeader(baseUrl, "webcache主页面")
                        + "<body>\n"
                        + ServletUtils.gwcHtmlLogoLink(baseUrl));
//        str.append("<h3>欢迎访问geowebcache，您访问的版本是" + version + ", 构建于 " + commitId + "</h3>\n");
        str.append("<h3>欢迎访问geowebcache，您访问的版本是" + commitId + ", 构建于 " + time + "</h3>\n");
        str.append("<p><a href=\"http://geowebcache.org\">GeoWebCache</a> 是用于 WMS 服务器的高级切片缓存。.");
        str.append("它支持多种协议和格式，包括 WMS-C、WMTS、KML、谷歌地图和虚拟地球。.</p>");
        str.append("<h3>自动生成的演示:</h3>\n");
        str.append("<ul><li><a href=\"" + baseUrl + "demo\">所有图层和自动演示的列表</a></li></ul>\n");
        str.append("<ul><li><a href=\"" + baseUrl + "geoair/demo\">图层组列表</a></li></ul>\n");
        str.append("<ul><li><a href=\"" + baseUrl + "geoair/demov2\">图层组列表v2</a></li></ul>\n");
        str.append("<ul><li><a href=\"" + baseUrl + "doc/index.html\">测试页面</a></li></ul>\n");
        str.append("<ul><li><a href=\"" + baseUrl + "doc/doc.html\">接口文档</a></li></ul>\n");
        str.append(
                "<ul><li><a href=\""
                        + baseUrl
                        + "geoair/reloadLayerGroup\">刷新图层配置</a></li></ul>\n");
        str.append("<h3>获取元数据文档:</h3>\n");
        str.append(
                "<ul><li><a href=\""
                        + baseUrl
                        + "service/wmts?REQUEST=getcapabilities\">WMTS 1.0.0 GetCapabilities（元数据） 文档</a></li>");
        str.append(
                "<li><a href=\""
                        + baseUrl
                        + "service/wms?SERVICE=WMS&VERSION=1.1.1&REQUEST=getcapabilities&TILED=true\">WMS 1.1.1 GetCapabilities（元数据） 文档</a></li>");
        str.append("<li><a href=\"" + baseUrl + "service/tms/1.0.0\">TMS 1.0.0 文档</a></li>");
        str.append("<li>请注意，后者仅适用于支持 ");
        str.append(
                "<a href=\"http://wiki.osgeo.org/wiki/WMS_Tiling_Client_Recommendation\">WMS-C 客户端</a>.</li>\n");
        str.append("<li>从 URL 中省略 tiled=true 将省略 TileSet 元素.</li></ul>\n");
        if (runtimeStats != null) {
            str.append("<h3>运行时状态统计</h3>\n");
            str.append(runtimeStats.getHTMLStats());
            str.append("</table>\n");
        }
        if (!Boolean.parseBoolean(
                GeoWebCacheExtensions.getProperty("GEOWEBCACHE_HIDE_STORAGE_LOCATIONS"))) {
            appendStorageLocations(str, baseUrl);
        }

        if (storageBroker != null) {
            appendInternalCacheStats(str);
        }
        str.append("</body></html>\n");

        ResponseUtils.writePage(
                response, 200, str.toString(), runtimeStats, MediaType.TEXT_HTML_VALUE);
    }

    private void appendStorageLocations(StringBuilder str, String baseUrl) {
        str.append("<h3>配置文件位置</h3>\n");
        str.append("<table class=\"stats\">\n");
        str.append("<tbody>");
        XMLConfiguration config;
        if (mainConfiguration instanceof XMLConfiguration) {
            config = (XMLConfiguration) mainConfiguration;
        } else {
            config = GeoWebCacheExtensions.bean(XMLConfiguration.class);
        }
        String configLoc;
        String localStorageLoc;
        // TODO: Disk Quota location
        Map<String, String> blobStoreLocations = new HashMap<>();
        if (storageBroker instanceof DefaultStorageBroker) {
            BlobStore bStore = ((DefaultStorageBroker) storageBroker).getBlobStore();
            if (bStore instanceof CompositeBlobStore) {
                for (BlobStoreInfo bsConfig : blobStoreAggregator.getBlobStores()) {
                    blobStoreLocations.put(bsConfig.getName(), bsConfig.getLocation());
                }
            }
        }
        try {
            configLoc = config.getConfigLocation();
        } catch (ConfigurationException | NullPointerException ex) {
            configLoc = "错误";
            log.error("无法找到配置文件路径", ex);
        }
        try {
            localStorageLoc = defaultStorageFinder.getDefaultPath();
        } catch (ConfigurationException ex) {
            localStorageLoc = "错误";
            log.error("无法找到webcache配置目录", ex);
        }
        str.append("<a href=\"" + baseUrl + "geoair/PreviewXML\">预览用户配置文件<br>");
        str.append("<a href=\"" + baseUrl + "geoair/PreviewXMLTemplate\">预览配置文件模板<br>");
        str.append("<tr><th scope=\"row\">配置文件路径:</th><td><tt>")
                .append(configLoc)
                .append("</tt></td></tr>");
        str.append("<tr><th scope=\"row\">本地缓存目录:</th><td><tt>")
                .append(localStorageLoc)
                .append("</tt></td></tr>");
        str.append("<tr><th scope=\"row\">GeoAir缓存目录:</th><td><tt>")
                .append(
                        System.getProperty("GTC_CACHE_DIR")
                                + File.separator
                                + "gtcCache")
                .append("</tt></td></tr>");
        str.append("</tbody>");
        if (!blobStoreLocations.isEmpty()) {
            str.append("<tbody>");
            str.append("<tr><th scope=\"rowgroup\" colspan=\"2\">其他数据仓库</th></tr>");
            for (Map.Entry<String, String> e : blobStoreLocations.entrySet()) {
                str.append("<tr><th scope=\"row\">")
                        .append(e.getKey())
                        .append(":</th><td><tt>")
                        .append(e.getValue())
                        .append("</tt></td></tr>");
            }
            str.append("</tbody>");
        }

        str.append("</tbody>");
        str.append("</table>\n");
    }

    /**
     * This method appends the cache statistics to the GWC homepage if the blobstore used is an
     * instance of the {@link MemoryBlobStore} class
     *
     * @param strGlobal Input {@link StringBuilder} containing the HTML for the GWC homepage
     */
    private void appendInternalCacheStats(StringBuilder strGlobal) {

        if (storageBroker == null) {
            return;
        }

        // Searches for the BlobStore inside the StorageBroker
        BlobStore blobStore = null;
        if (log.isDebugEnabled()) {
            log.debug("搜索所有存储仓库的位置");
        }
        // Getting the BlobStore if present
        if (storageBroker instanceof DefaultStorageBroker) {
            blobStore = ((DefaultStorageBroker) storageBroker).getBlobStore();
        }

        // If it is not present, or it is not a memory blobstore, nothing is done
        if (blobStore == null || !(blobStore instanceof MemoryBlobStore)) {
            if (log.isDebugEnabled()) {
                log.debug("未找到存储仓库");
            }
            return;
        }
        if (log.isDebugEnabled()) {
            //            log.debug("Memory BlobStore found");
            log.debug("找到内存中存储仓库");
        }

        // Get the MemoryBlobStore if present
        MemoryBlobStore store = (MemoryBlobStore) blobStore;

        if (log.isDebugEnabled()) {
            log.debug("Getting statistics");
        }
        // Get the statistics
        CacheStatistics statistics = store.getCacheStatistics();

        // No Statistics found
        if (statistics == null) {
            return;
        }

        // Get the statistics values
        long hitCount = statistics.getHitCount();
        long missCount = statistics.getMissCount();
        long evictionCount = statistics.getEvictionCount();
        long requestCount = statistics.getRequestCount();
        double hitRate = statistics.getHitRate();
        double missRate = statistics.getMissRate();
        double currentMemory = statistics.getCurrentMemoryOccupation();
        long byteToMb = 1024 * 1024;
        double actualSize = ((long) (100 * (statistics.getActualSize() * 1.0d) / byteToMb)) / 100d;
        double totalSize = ((long) (100 * (statistics.getTotalSize() * 1.0d) / byteToMb)) / 100d;

        // Append the HTML with the statistics to a new StringBuilder
        StringBuilder str = new StringBuilder();

        str.append("<h3>内存缓存统计信息</h3>\n");

        str.append("<table border=\"0\" cellspacing=\"5\">");

        str.append(
                "<tr><td colspan=\"2\">请求总数:</td><td colspan=\"3\">"
                        + (requestCount >= 0 ? requestCount + "" : "不可用"));
        str.append("</td></tr>\n");

        str.append("<tr><td colspan=\"5\"> </td></tr>");

        str.append("<tr><td colspan=\"2\">内部缓存命中计数:</td><td colspan=\"3\">");
        str.append(hitCount >= 0 ? hitCount + "" : "不可用");
        str.append("</td></tr>\n");

        str.append("<tr><td colspan=\"2\">内存缓存未命中总数：</td><td colspan=\"3\">");
        str.append(missCount >= 0 ? missCount + "" : "不可用");
        str.append("</td></tr>\n");

        str.append("<tr><td colspan=\"2\">内存缓存命中率:</td><td colspan=\"3\">");
        str.append(hitRate >= 0 ? hitRate + " %" : "不可用");
        str.append("</td></tr>\n");

        str.append("<tr><td colspan=\"2\">内存缓存未命中率:</td><td colspan=\"3\">");
        str.append(missRate >= 0 ? missRate + " %" : "不可用");
        str.append("</td></tr>\n");

        str.append("<tr><td colspan=\"5\"> </td></tr>");

        str.append(
                "<tr><td colspan=\"2\">请求撤回的瓦片总数:</td><td colspan=\"3\">"
                        + (evictionCount >= 0 ? evictionCount + "" : "不可用"));
        str.append("</td></tr>\n");

        str.append("<tr><td colspan=\"5\"> </td></tr>");

        str.append(
                "<tr><td colspan=\"2\">内存占用:</td><td colspan=\"3\">"
                        + (currentMemory >= 0 ? currentMemory + " %" : "不可用"));
        str.append("</td></tr>\n");

        str.append("<tr><td colspan=\"5\"> </td></tr>");

        str.append(
                "<tr><td colspan=\"2\">缓存实际大小/总大小 :</td><td colspan=\"3\">"
                        + (totalSize >= 0 && actualSize >= 0
                        ? actualSize + " / " + totalSize + " Mb"
                        : "不可用"));
        str.append("</td></tr>\n");

        str.append("<tr><td colspan=\"5\"> </td></tr>");

        str.append("</table>\n");

        // Append to the homepage HTML
        strGlobal.append(str);
    }

    protected SecurityDispatcher getSecurityDispatcher() {
        return securityDispatcher;
    }

    /**
     * Set the security dispatcher to use to test service requests.
     */
    public void setSecurityDispatcher(SecurityDispatcher secDispatcher) {
        this.securityDispatcher = secDispatcher;
    }
}
