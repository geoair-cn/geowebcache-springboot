/**
 * 该程序是免费软件：您可以根据自由软件基金会发布的 GNU 宽松通用公共许可证 （许可证的第 3 版或（由您选择）任何更高版本）的条款重新分发和/或修改它.
 *
 * <p>分发此程序是希望它有用，但不作任何保证； 甚至没有对适销性或针对特定目的的适用性的暗示保证。 有关更多详细信息，请参阅 GNU 通用公共许可证.
 *
 * <p>您应该已经收到一份 GNU 宽松通用公共许可证以及该程序。如果没有，请参阅 <http:www.gnu.orglicenses>。
 *
 * @author geoair，版权所有 2021
 */
package cn.geoair.geoairteam.geowebcache.tianditu.utils;

import cn.hutool.core.io.IoUtil;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geowebcache.GeoWebCacheDispatcher;
import org.geowebcache.GeoWebCacheExtensions;
import org.geowebcache.conveyor.Conveyor.CacheResult;
import org.geowebcache.conveyor.ConveyorTile;
import org.geowebcache.grid.BoundingBox;
import org.geowebcache.grid.GridSubset;
import org.geowebcache.io.ByteArrayResource;
import org.geowebcache.io.Resource;
import org.geowebcache.layer.TileLayer;
import org.geowebcache.mime.ImageMime;
import org.geowebcache.stats.RuntimeStats;
import org.geowebcache.storage.DefaultStorageFinder;
import org.geowebcache.util.ServletUtils;
import org.springframework.http.MediaType;

/**
 * 可用于将字符串写入为 http 响应的实用方法</br> 响应可以是有效的或包含错误状态代码</br> HTTP 响应可以呈现为 HTML 或 XML
 *
 * @author geoair
 */
public final class TC_ResponseUtils {

    private static Log log = LogFactory.getLog(TC_ResponseUtils.class);

    private TC_ResponseUtils() {}

    public static void writeTile(
            ConveyorTile convTile,
            BufferedImage resultPng,
            String layerName,
            RuntimeStats runtimeStats)
            throws IOException {
        writeData(convTile, resultPng);
    }

    /** 快乐的结局，设置标题并将响应写回客户端。 */
    public static void writeData(ConveyorTile tile, BufferedImage resultPng) throws IOException {
        HttpServletResponse servletResp = tile.servletResp;
        final HttpServletRequest servletReq = tile.servletReq;
        int httpCode = HttpServletResponse.SC_OK;
        Resource blob = tile.getBlob();
        String mimeType = tile.getMimeType().getMimeType(blob);

        servletResp.setHeader("geowebcache-cache-result", String.valueOf(CacheResult.HIT));
        servletResp.setHeader("geowebcache-tile-index", Arrays.toString(tile.getTileIndex()));
        long[] tileIndex = tile.getTileIndex();
        TileLayer layer = tile.getLayer();
        GridSubset gridSubset = layer.getGridSubset(tile.getGridSetId());
        BoundingBox tileBounds = gridSubset.boundsFromIndex(tileIndex);
        servletResp.setHeader("geowebcache-tile-bounds", tileBounds.toString());

        final long tileTimeStamp = tile.getTSCreated();
        final String ifModSinceHeader = servletReq.getHeader("If-Modified-Since");
        final String lastModified = DateUtil.formatDate(new Date(tileTimeStamp));
        servletResp.setHeader("Last-Modified", lastModified);

        final Date ifModifiedSince;
        if (ifModSinceHeader != null && ifModSinceHeader.length() > 0) {
            try {
                ifModifiedSince = DateUtil.parseDate(ifModSinceHeader);
                // the HTTP header has second precision
                long ifModSinceSeconds = 1000 * (ifModifiedSince.getTime() / 1000);
                long tileTimeStampSeconds = 1000 * (tileTimeStamp / 1000);
                if (ifModSinceSeconds >= tileTimeStampSeconds) {
                    httpCode = HttpServletResponse.SC_NOT_MODIFIED;
                    blob = null;
                }
            } catch (DateParseException e) {
                if (log.isDebugEnabled()) {
                    log.debug(" 无法解析客户端的 If-Modified-Since 标头：'" + ifModSinceHeader + "'");
                }
            }
        }

        if (httpCode == HttpServletResponse.SC_OK && tile.getLayer().useETags()) {
            String ifNoneMatch = servletReq.getHeader("If-None-Match");
            String hexTag = Long.toHexString(tileTimeStamp);

            if (ifNoneMatch != null) {
                if (ifNoneMatch.equals(hexTag)) {
                    httpCode = HttpServletResponse.SC_NOT_MODIFIED;
                    blob = null;
                }
            }

            servletResp.setHeader("ETag", hexTag);
        }
        servletResp.setStatus(httpCode);
        servletResp.setContentType(mimeType);
        ImageIO.write(resultPng, "png", servletResp.getOutputStream());
        IoUtil.close(servletResp.getOutputStream());
    }

    /** 编写一个透明的 8 位 PNG 以避免让像 OpenLayers 这样的客户端显示大量粉红色 tiles */
    private static void writeEmpty(
            DefaultStorageFinder defaultStorageFinder,
            ConveyorTile tile,
            String message,
            RuntimeStats runtimeStats) {
        tile.servletResp.setHeader("geowebcache-message", message);
        TileLayer layer = tile.getLayer();
        if (layer != null) {
            layer.setExpirationHeader(tile.servletResp, (int) tile.getTileIndex()[2]);

            if (layer.useETags()) {
                String ifNoneMatch = tile.servletReq.getHeader("If-None-Match");
                if (ifNoneMatch != null && ifNoneMatch.equals("gwc-blank-tile")) {
                    tile.servletResp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    return;
                } else {
                    tile.servletResp.setHeader("ETag", "gwc-blank-tile");
                }
            }
        }

        writeFixedResponse(
                tile.servletResp,
                200,
                ImageMime.png.getMimeType(),
                loadBlankTile(defaultStorageFinder),
                CacheResult.OTHER,
                runtimeStats);
    }

    /**
     * 编写 HTTP 响应设置提供的 HTTP 代码的帮助程序方法。
     *
     * @param response – HTTP 响应
     * @param httpCode – HTTP 状态码
     * @param contentType – HTTP 响应内容类型
     * @param resource – HTTP 响应资源
     * @param cacheRes – 提供有关图块检索的信息
     * @param runtimeStats – 运行时统计信息
     */
    public static void writeFixedResponse(
            HttpServletResponse response,
            int httpCode,
            String contentType,
            Resource resource,
            CacheResult cacheRes,
            RuntimeStats runtimeStats) {

        int contentLength = (int) (resource == null ? -1 : resource.getSize());
        writeFixedResponse(
                response, httpCode, contentType, resource, cacheRes, contentLength, runtimeStats);
    }

    /**
     * 编写 HTTP 响应设置提供的 HTTP 代码的帮助程序方法。 使用提供的内容长度。
     *
     * @param response – HTTP 响应
     * @param httpCode – HTTP 状态码
     * @param contentType – HTTP 响应内容类型
     * @param resource – HTTP 响应资源
     * @param cacheRes – 提供有关图块检索的信息
     * @param contentLength – HTTP 响应内容长度
     * @param runtimeStats – 运行时统计信息
     */
    public static void writeFixedResponse(
            HttpServletResponse response,
            int httpCode,
            String contentType,
            Resource resource,
            CacheResult cacheRes,
            int contentLength,
            RuntimeStats runtimeStats) {

        response.setStatus(httpCode);
        response.setContentType(contentType);

        response.setContentLength(contentLength);
        if (resource != null) {
            try (OutputStream os = response.getOutputStream();
                    WritableByteChannel channel = Channels.newChannel(os)) {
                resource.transferTo(channel);
                runtimeStats.log(contentLength, cacheRes);

            } catch (IOException ioe) {
                log.debug("捕获 IOException: " + ioe.getMessage() + "\n\n" + ioe.toString());
            }
        }
    }

    private static ByteArrayResource loadBlankTile(DefaultStorageFinder defaultStorageFinder) {
        ByteArrayResource blankTile = null;
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
                    log.error(e.getMessage(), e);
                }

                if (fileSize == blankTile.getSize()) {
                    log.info("加载的空白图块来自 " + blankTilePath);
                } else {
                    log.error("无法从以下位置加载空白图块 " + blankTilePath);
                }

            } else {
                log.error("" + blankTilePath + " 不存在或不可读。 ");
            }
        }

        // Use the built-in one:
        if (blankTile == null) {
            try {
                URL url = GeoWebCacheDispatcher.class.getResource("blank.png");
                blankTile = new ByteArrayResource();
                loadBlankTile(blankTile, url);
                int ret = (int) blankTile.getSize();
                log.info("读取 " + ret + " 空白 PNG 文件（预计 425）.");
            } catch (IOException ioe) {
                log.error(ioe.getMessage());
            }
        }

        return blankTile;
    }

    private static void loadBlankTile(Resource blankTile, URL source) throws IOException {
        try (InputStream inputStream = source.openStream();
                ReadableByteChannel ch = Channels.newChannel(inputStream)) {
            blankTile.transferFrom(ch);
        } catch (IOException e) {
            log.debug(e);
        }
    }

    /**
     * 于将错误写回客户端并同时记录的包装方法。
     *
     * @param response 写信到哪里
     * @param httpCode – 要提供的 HTTP 代码
     * @param errorMsg – 实际的错误信息，人类可读
     */
    public static void writeErrorPage(
            HttpServletResponse response,
            int httpCode,
            String errorMsg,
            RuntimeStats runtimeStats) {
        log.debug(errorMsg);
        errorMsg =
                "<html>\n"
                        + ServletUtils.gwcHtmlHeader("../", "webcache 系统错误")
                        + "<body>\n"
                        + ServletUtils.gwcHtmlLogoLink("../")
                        + "<h4>"
                        + httpCode
                        + ": "
                        + ServletUtils.disableHTMLTags(errorMsg)
                        + "</h4>"
                        + "</body></html>\n";
        writePage(response, httpCode, errorMsg, runtimeStats, MediaType.TEXT_HTML_VALUE);
    }

    /**
     * 写入 HTTP 响应设置，因为它满足以 XML 编码的提供的异常消息。 原始错误也被记录。
     *
     * @param response – HTTP 响应
     * @param httpCode – HTTP 状态码
     * @param errorMsg – 以 XML 编码的错误消息
     */
    public static void writeErrorAsXML(
            HttpServletResponse response, int httpCode, String errorMsg) {
        RuntimeStats runtimeStats = GeoWebCacheExtensions.bean(RuntimeStats.class);
        log.debug(errorMsg);
        writePage(response, httpCode, errorMsg, runtimeStats, MediaType.APPLICATION_XML_VALUE);
    }

    /**
     * 写入 HTTP 响应设置，因为它包含提供的消息并使用提供的内容类型。
     *
     * @param response – HTTP 响应
     * @param httpCode – HTTP 状态码
     * @param message – HTTP 响应内容
     * @param runtimeStats – 运行时统计信息
     * @param contentType – HTTP 响应内容类型
     */
    public static void writePage(
            HttpServletResponse response,
            int httpCode,
            String message,
            RuntimeStats runtimeStats,
            String contentType) {
        Resource res = null;
        if (message != null) {
            res = new ByteArrayResource(message.getBytes());
        }
        TC_ResponseUtils.writeFixedResponse(
                response, httpCode, contentType, res, CacheResult.OTHER, runtimeStats);
    }
}
