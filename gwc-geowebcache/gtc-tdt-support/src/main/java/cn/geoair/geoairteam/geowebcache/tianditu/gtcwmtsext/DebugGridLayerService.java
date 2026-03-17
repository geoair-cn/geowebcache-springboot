package cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext;

import cn.hutool.core.lang.Singleton;
import cn.geoair.base.util.GutilObject;
import cn.geoair.geoairteam.gwc.model.gwc.enums.GtcWmtsType;
import cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext.dto.XyzDto;
import cn.geoair.geoairteam.geowebcache.tianditu.utils.GeowebcacheUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

import org.geowebcache.GeoWebCacheException;
import org.geowebcache.service.OWSException;

/**
 * @author ：zfj
 * @date ：Created in 2023/12/5 13:25
 * @description： ArcGis单图层加载
 */
public class DebugGridLayerService extends TileBaseGetterSupport {
    static GeowebcacheUtils geowebcacheUtils;

    GtcWmtsType gtcWmtsType;

    public DebugGridLayerService(String requestLayerName) throws OWSException {
        super(requestLayerName);
        if (GutilObject.isEmpty(geowebcacheUtils)) {
            geowebcacheUtils = Singleton.get(GeowebcacheUtils.class);
        }
        gtcWmtsType = GtcWmtsType.debugGridLayer;
    }

    @Override
    public void doServiceGetCapabilities(String capabilitiesShowName)
            throws GeoWebCacheException, OWSException {

        throw new GeoWebCacheException("暂不支持");
    }

    //    @Override
//    public BufferedImage getImage(XyzDto xyzDto) throws GeoWebCacheException, OWSException {
//        // 创建256*256的透明PNG图片
//        BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
//        Graphics2D g2d = image.createGraphics();
//
//        // 设置抗锯齿
//        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        g2d.setRenderingHint(
//                RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//
//        // 绘制透明背景
//        g2d.setComposite(AlphaComposite.Clear);
//        g2d.fillRect(0, 0, 256, 256);
//        g2d.setComposite(AlphaComposite.SrcOver);
//
//        // 绘制红色边框
//        g2d.setColor(Color.RED);
//        g2d.setStroke(new BasicStroke(1.0f));
//        g2d.drawRect(0, 0, 255, 255);
//
//        // 在图片中心绘制黑色的zxy文本
//        g2d.setColor(Color.BLACK);
//        g2d.setColor(Color.BLACK);
//        Font font = new Font("Arial", Font.BOLD, 18);
//        g2d.setFont(font);
//        String text = xyzDto.getXyzPath();
//        FontMetrics metrics = g2d.getFontMetrics(font);
//        int textWidth = metrics.stringWidth(text);
//        int textHeight = metrics.getHeight();
//
//        g2d.drawString(text, (256 - textWidth) / 2, (256 + textHeight) / 2 - 10);
//
//        g2d.dispose();
//
//        // 将图片转换为字节数组
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        try {
//            ImageIO.write(image, "PNG", baos);
//        } catch (IOException e) {
//        }
//
//        return image;
//    }
    public BufferedImage getImage(XyzDto xyzDto) throws GeoWebCacheException, OWSException {
        BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

// 设置抗锯齿（优化绘制效果）
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

// 绘制透明背景
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, 256, 256);
        g2d.setComposite(AlphaComposite.SrcOver);

// 绘制红色边框
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRect(0, 0, 255, 255);

// ========== 1. 绘制淡色的 requestLayerName（图层名） ==========
        g2d.setColor(new Color(153, 153, 153)); // 淡灰色（#999999），可根据需求调整
        Font layerFont = new Font("Arial", Font.PLAIN, 14); // 图层名字体：常规、14号（比XYZ小）
        g2d.setFont(layerFont);
        FontMetrics layerMetrics = g2d.getFontMetrics(layerFont);
// 图层名绘制在左上角偏移位置（X:10像素，Y:25像素），避免贴边
        int layerX = 10;
        int layerY = 25;
        g2d.drawString(requestLayerName, layerX, layerY);

// ========== 2. 绘制偏上偏左的 XYZ 文本 ==========
        g2d.setColor(Color.BLACK);
        Font xyzFont = new Font("Arial", Font.BOLD, 18);
        g2d.setFont(xyzFont);
        String xyzText = xyzDto.getXyzPath();
        FontMetrics xyzMetrics = g2d.getFontMetrics(xyzFont);
        int xyzWidth = xyzMetrics.stringWidth(xyzText);
        int xyzHeight = xyzMetrics.getHeight();

// 调整XYZ位置：偏上（中心Y轴-30） + 偏左（中心X轴-20）
// 原居中公式：(256 - xyzWidth)/2 , (256 + xyzHeight)/2 -10
        int xyzX = (256 - xyzWidth) / 2 - 20; // 偏左20像素
        int xyzY = (256 + xyzHeight) / 2 - 40; // 偏上30像素（原-10 → 改为-40）
        g2d.drawString(xyzText, xyzX, xyzY);

// 释放绘图资源
        g2d.dispose();

// 将图片转换为字节数组（可选：如果需要返回字节数组而非BufferedImage）
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "PNG", baos);
            baos.flush();
        } catch (IOException e) {
            // 补充异常处理，避免空catch
            e.printStackTrace();
            throw new RuntimeException("生成PNG图片失败", e);
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return image;
    }
}
