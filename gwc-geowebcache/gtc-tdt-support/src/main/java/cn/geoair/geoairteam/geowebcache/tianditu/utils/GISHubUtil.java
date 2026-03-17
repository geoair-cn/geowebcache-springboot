package cn.geoair.geoairteam.geowebcache.tianditu.utils;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.geoair.web.util.GirHttpServletHelper;
import cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext.RequestParamter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.geowebcache.service.OWSException;
import org.geowebcache.util.ServletUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author zhangjun
 * @version 1.0
 * @date 2021/8/16 16:53
 */
public class GISHubUtil {

    /**
     * 将http请求的内容写入response
     *
     * @param url
     * @param response
     */
    public static void writeResponse(String url, HttpServletResponse response) {
        HttpRequest httpRequest = HttpUtil.createGet(url).timeout(1000);
        try {
            HttpResponse httpResponse = httpRequest.execute();
            Map<String, List<String>> headers = httpResponse.headers();
            List<String> strings2 = headers.get("Content-Type");
            if (strings2 != null && strings2.size() != 0) {
                response.setHeader("Content-Type", strings2.get(0));
            }
            InputStream inputStream = httpResponse.bodyStream();
            IoUtil.copy(inputStream, response.getOutputStream());
            IoUtil.close(inputStream);
            IoUtil.close(response.getOutputStream());
        } catch (IORuntimeException e) {
        } catch (Exception e) {
        }
    }

    public static void createImage(
            String str, Font font, OutputStream os, Integer width, Integer height)
            throws Exception {
        // 创建图片
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        Graphics g = image.getGraphics();
        g.setClip(0, 0, width, height);
        //        g.setColor(new Color(0, 0, 0, 0));
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height); // 先用透明填充整张图片,也就是背景
        g.setColor(Color.red); // 在换成红色
        g.setFont(font); // 设置画笔字体
        /** 用于获得垂直居中y */
        Rectangle clip = g.getClipBounds();
        FontMetrics fm = g.getFontMetrics(font);
        int ascent = fm.getAscent();
        int descent = fm.getDescent();
        int y = (clip.height - (ascent + descent)) / 2 + ascent;
        for (int i = 0; i < 6; i++) { // 256 340 0 680
            g.drawString(str, i * 680, y); // 画出字符串
        }
        g.dispose();
        ImageIO.write(image, "png", os); // 输出png图片
    }

    public static String[] keys = {
        "layer",
        "request",
        "style",
        "format",
        "infoformat",
        "tilematrixset",
        "tilematrix",
        "tilerow",
        "tilecol",
        "tileformat",
        "i",
        "j"
    };

    public static Map<String, String> checkParameter(String layername) throws OWSException {
        HttpServletRequest request = GirHttpServletHelper.getRequest();
        String encoding = request.getCharacterEncoding();
        Map<String, String> values =
                ServletUtils.selectedStringsFromMap(
                        request.getParameterMap(),
                        encoding,
                        RequestParamter.getRequestParamterCodes());
        String layer = values.get("layer");
        if (layer == null && (layername == null || layername.equals("null"))) {
            throw new OWSException(400, "参数错误", "图层", "图层layer参数缺失");
        }
        String requesta = values.get("request");
        if (requesta == null) {
            throw new OWSException(400, "参数错误", "图层", "图层request参数缺失");
        }
        return values;
    }

    /**
     * 判断是否为纯色
     *
     * @param bufferedImage 图片源
     * @param percent 纯色百分比，即大于此百分比为同一种颜色则判定为纯色,范围[0-1]
     * @return
     * @throws IOException
     */
    public static boolean isSimpleColorImg(BufferedImage bufferedImage, float percent) {
        BufferedImage src = bufferedImage;
        int height = src.getHeight();
        int width = src.getWidth();
        int count = 0, pixTemp = 0, pixel = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                pixel = src.getRGB(i, j);
                if (pixel == pixTemp) // 如果上一个像素点和这个像素点颜色一样的话，就判定为同一种颜色
                count++;
                else count = 0;
                if ((float) count / (height * width) >= percent) // 如果连续相同的像素点大于设定的百分比的话，就判定为是纯色的图片
                return true;
                pixTemp = pixel;
            }
        }
        return false;
    }

    public static ServletRequestAttributes getRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return (ServletRequestAttributes) attributes;
    }

    /** 获取response */
    public static HttpServletResponse getResponse() {
        return getRequestAttributes().getResponse();
    }

    /** 获取request */
    public static HttpServletRequest getRequest() {
        return getRequestAttributes().getRequest();
    }
}
