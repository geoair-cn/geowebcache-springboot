package cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext;

import cn.geoair.base.log.GiLogger;
import cn.geoair.base.log.GirLogger;
import cn.geoair.base.util.GutilObject;

import cn.geoair.web.GirWeb;
import cn.geoair.web.util.GirHttpServletHelper;
import cn.geoair.geoairteam.gwc.servface.gwc.LayerGroupAliasService;
import cn.geoair.geoairteam.gwc.servface.gwc.LayerInfoService;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext.dto.TileResult;
import cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext.dto.XyzDto;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.GeoWebCacheExtensions;
import org.geowebcache.layer.TileLayerDispatcher;
import org.geowebcache.service.OWSException;
import org.geowebcache.util.ServletUtils;

/**
 * @author ：zfj
 * @date ：Created in 2023/12/5 12:06
 * @description： 抽象一些通用方法
 */
public abstract class TileBaseGetterSupport extends CacheCaller implements TileGetterSupport {

    private static final GiLogger logger = GirLogger.getLoger(TileBaseGetterSupport.class);

    HttpServletRequest request;
    HttpServletResponse response;
    static TileLayerDispatcher tileLayerDispatcher;

    Map<String, String> values;

    LayerInfoService layerInfoService = null;

    protected String requestLayerName;
    LayerGroupAliasService layerGroupAliasService;

    public TileBaseGetterSupport(String requestLayerName) {
        this.requestLayerName = requestLayerName;
        if (tileLayerDispatcher == null) {
            tileLayerDispatcher = GeoWebCacheExtensions.bean(TileLayerDispatcher.class);
        }
        if (GutilObject.isEmpty(layerGroupAliasService)) {
            layerGroupAliasService = GeoWebCacheExtensions.bean(LayerGroupAliasService.class);
        }
        if (layerInfoService == null) {
            layerInfoService = GeoWebCacheExtensions.bean(LayerInfoService.class);
        }
        request = GirWeb.getRequest();
        response = GirHttpServletHelper.getResponse();
        String encoding = request.getCharacterEncoding();
        values =
                ServletUtils.selectedStringsFromMap(
                        request.getParameterMap(),
                        encoding,
                        RequestParamter.getRequestParamterCodes());

    }

    @Override
    public TileResult doService(XyzDto xyzDto) throws GeoWebCacheException, OWSException {
        BufferedImage image = null;
        byte[] imageBytes = null;
        ServletOutputStream outputStream = null;
        try {
            try {
                image = getImage(xyzDto);
                if (image == null) {
                    image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
                }
                // 将 BufferedImage 转换为 byte 数组
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                imageBytes = baos.toByteArray();
                baos.close();
            } catch (Exception e) {
                logger.warn(e);
                image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
                // 即使生成默认图片，也尝试转换
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", baos);
                    imageBytes = baos.toByteArray();
                    baos.close();
                } catch (Exception ex) {
                    logger.warn("转换默认图片失败", ex);
                }
            }
            if (imageBytes != null) {
                return TileResult.of(requestLayerName).setBytes(imageBytes).setSize(imageBytes.length).setExists(true);
            }
        } catch (Exception e) {
            logger.info("获取的图片转换到输出流异常 {}", e.getMessage());
        }
        return TileResult.of(requestLayerName).setExists(false);
    }

    /**
     * 创建图片
     *
     * @param str
     * @param width
     * @param height
     * @return
     */
    public static BufferedImage createImage(String str, Integer width, Integer height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        g.setClip(0, 0, width, height);
        g.setColor(Color.blue);
        g.setFont(new Font(null, Font.BOLD, 11));
        Rectangle clip = g.getClipBounds();
        FontMetrics fm = g.getFontMetrics(new Font(null, Font.BOLD, 10));
        int ascent = fm.getAscent();
        int descent = fm.getDescent();
        int y = (clip.height - (ascent + descent)) / 2 + ascent;
        for (int i = 0; i < 6; i++) {
            g.drawString("errorCode:" + str, i * 680, y);
        }
        g.dispose();
        return image;
    }

    protected void mergeImg(BufferedImage target, BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        for (int ii = 0; ii < width; ii++) {
            for (int j = 0; j < height; j++) {
                int rgb = source.getRGB(ii, j);
                int alpha = rgb >> 24;
                if (!(alpha >= 0)) {
                    target.setRGB(ii, j, rgb);
                }
            }
        }
    }
}
