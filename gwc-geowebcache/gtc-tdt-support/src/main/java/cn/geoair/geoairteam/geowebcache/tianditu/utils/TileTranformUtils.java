package cn.geoair.geoairteam.geowebcache.tianditu.utils;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public class TileTranformUtils {
    /** 谷歌切片方案比例尺 */
    public static HashMap<Integer, Double> GoogleTilesScale = new HashMap<>();

    /** 天地图切片方案比例尺 */
    public static HashMap<Integer, Double> tiantiduTilesScale = new HashMap<>();

    /** 赤道周长的一半 */
    public static final double mercatorMax = 20037508.34278925;

    /** 谷歌切片方案中心原点 */
    public static Double[] GoogleTilesOrigin = new Double[2];

    static {
        GoogleTilesOrigin[0] = -20037508.342787;
        GoogleTilesOrigin[1] = 20037508.342787;
        // 通过赤道周长获取 每个256图片的 对应的比例尺
        Double maxResolution = (mercatorMax * 2) / 256;
        for (int i = 0; i <= 19; i++) {
            GoogleTilesScale.put(i, maxResolution / Math.pow(2, i));
        }
        maxResolution = (360.0) / 256;
        for (int i = 0; i <= 19; i++) {
            tiantiduTilesScale.put(i, maxResolution / Math.pow(2, i));
        }
    }

    private static final double PI = Math.PI;

    /**
     * 4326坐标转3857即经纬度转墨卡托
     *
     * @param lon
     * @param lat
     */
    public static List<Double> transformTo3857(double lon, double lat) {
        // 赤道半径
        double earthRad = 6378137.0;
        double mercatorx = lon * Math.PI / 180 * earthRad;
        double a = lat * Math.PI / 180;
        double mercatory = earthRad / 2 * Math.log((1.0 + Math.sin(a)) / (1.0 - Math.sin(a)));
        //        System.out.printf("经纬度坐标转墨卡托后的坐标:%f,%f", mercatorx, mercatory);
        ArrayList<Double> doubles = new ArrayList<>();
        doubles.add(mercatorx);
        doubles.add(mercatory);
        return doubles;
    }

    /**
     * 墨卡托坐标转3857即墨卡托转经纬度
     *
     * @param mercatorx
     * @param mercatory
     */
    public static List<Double> tarnsformTo4326(double mercatorx, double mercatory) {
        double lon = mercatorx / mercatorMax * 180.0;
        double lat = mercatory / mercatorMax * 180.0;
        lat = (180.0 / PI) * (2 * Math.atan(Math.exp((lat * PI) / 180.0)) - PI / 2.0);
        // lat= 180/Math.PI*(2*Math.atan(Math.exp(lat*Math.PI/180))-Math.PI/2);
        //        System.out.printf("墨卡托坐标转经纬度后的坐标:%f,%f \n", lon, lat);
        ArrayList<Double> doubles = new ArrayList<>();
        doubles.add(lon);
        doubles.add(lat);
        return doubles;
    }

    public static void main(String[] args) {
        //  TileMatrix=9&TileCol=380&TileRow=66

        //
        double[] tiantitu4326 = new double[] {wmtsColToLon(380, 9), wmtsColToLon(380, 9)};
        List<Double> guge3857 = transformTo3857(tiantitu4326[0], tiantitu4326[1]);
        Double resolution = GoogleTilesScale.get(9);
        Double xFromOrigin =
                Math.floor((guge3857.get(0) - GoogleTilesOrigin[0]) / resolution) / 256;
        Double yFromOrigin =
                Math.floor((GoogleTilesOrigin[1] - guge3857.get(1)) / resolution) / 256;

        Double gugeX = Math.floor(xFromOrigin);
        Double gugeY = Math.floor(yFromOrigin);
    }

    /**
     * 通过wmts的 列号获取 经度 *参考 openlayer 源码 ol/tilegrid/TileGrid.js 第 419行方法 getTileCoordForXYAndZ_
     * 逆运算得出
     *
     * @param column
     * @param zoom
     * @return
     */
    public static double wmtsColToLon(int column, int zoom) {
        Double resolution = tiantiduTilesScale.get(zoom);
        return column * 256 * resolution - 180;
    }

    /**
     * 通过wmts的 行号获取 纬度 参考 openlayer 源码 ol/tilegrid/TileGrid.js 第 419行方法 getTileCoordForXYAndZ_ 逆运算得出
     *
     * @param row
     * @param zoom
     * @return
     */
    public static double wmtsRowToLon(int row, int zoom) {
        Double resolution = tiantiduTilesScale.get(zoom);
        return 90 - (row * 256 * resolution);
    }

    /**
     * @param zuoshang 左上角3857坐标
     * @return
     */
    public static List<Double> getZuoXia3857(List<Double> zuoshang, int z) {
        Double x = zuoshang.get(0);
        Double y = zuoshang.get(1);
        Double aDouble = GoogleTilesScale.get(z);
        Double addCount = aDouble * 256;
        y = y - addCount;
        ArrayList<Double> doubles = new ArrayList<>();
        doubles.add(x);
        doubles.add(y);
        return doubles;
    }

    /**
     * @param youshang 右上角3857坐标
     * @return
     */
    public static List<Double> getYouShang3857(List<Double> youshang, int z) {
        Double x = youshang.get(0);
        Double y = youshang.get(1);
        Double aDouble = GoogleTilesScale.get(z);
        Double addCount = aDouble * 256;
        x = x + addCount;
        ArrayList<Double> doubles = new ArrayList<>();
        doubles.add(x);
        doubles.add(y);
        return doubles;
    }

    /**
     * @param youxia 右下3857坐标
     * @return
     */
    public static List<Double> getYouXia3857(List<Double> youxia, int z) {
        Double x = youxia.get(0);
        Double y = youxia.get(1);
        Double aDouble = GoogleTilesScale.get(z);
        Double addCount = aDouble * 256;
        y = y - addCount;
        x = x + addCount;
        ArrayList<Double> doubles = new ArrayList<>();
        doubles.add(x);
        doubles.add(y);
        return doubles;
    }

    /**
     * 获取3857坐标点所在的网格
     *
     * @param coord
     * @return
     */
    public static List<Integer> getCoordTiles(List<Double> coord, int z) {
        Double resolution = GoogleTilesScale.get(z);
        Double xFromOrigin = Math.floor((coord.get(0) - GoogleTilesOrigin[0]) / resolution);
        Double yFromOrigin = Math.floor((GoogleTilesOrigin[1] - coord.get(1)) / resolution);
        Double tileCoordX = xFromOrigin / 256;
        Double tileCoordY = yFromOrigin / 256;
        tileCoordX = Math.floor(tileCoordX);
        tileCoordY = Math.floor(tileCoordY);
        Integer tileCoordXInt = tileCoordX.intValue();
        Integer tileCoordYInt = tileCoordY.intValue();
        List<Integer> result = new ArrayList<>();
        result.add(tileCoordXInt);
        result.add(tileCoordYInt);
        return result;
    }

    /**
     * 请求http流下载一张图片
     *
     * @param url 请求url
     * @param width 宽度
     * @param height 高度
     */
    public static BufferedImage GetResponsePng(String url, Integer width, Integer height) {
        HttpRequest httpRequest = HttpUtil.createGet(url).timeout(1000);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        try {
            HttpResponse httpResponse = httpRequest.execute();
            Map<String, List<String>> headers = httpResponse.headers();
            List<String> strings2 = headers.get("Content-Type");

            if (strings2 != null && strings2.size() != 0) {
                boolean equals = "text/xml".equals(strings2.get(0));
                if (equals) {
                    Color myColor = new Color(255, 255, 255, 0);
                    int rgb = myColor.getRGB();
                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {
                            image.setRGB(i, j, rgb);
                        }
                    }
                    return image;
                }
            }
            InputStream inputStream = httpResponse.bodyStream();
            image = ImageIO.read(inputStream);
            IoUtil.close(inputStream);
        } catch (IORuntimeException e) {
        } catch (Exception e) {
        }
        return image;
    }

    public static BufferedImage getSplicingZuoShang(
            List<Double> coord, int z, BufferedImage Inimage, BufferedImage Outimage) {
        Double resolution = GoogleTilesScale.get(z);
        Double xFromOrigin = Math.floor((coord.get(0) - GoogleTilesOrigin[0]) / resolution);
        Double yFromOrigin = Math.floor((GoogleTilesOrigin[1] - coord.get(1)) / resolution);
        Double tileCoordX = xFromOrigin / 256;
        Double tileCoordY = yFromOrigin / 256;
        Double tileCoordXZheng = Math.floor(tileCoordX);
        Double tileCoordYZheng = Math.floor(tileCoordY);
        //  此处求得偏移量
        tileCoordXZheng = (tileCoordX - tileCoordXZheng) * 256;
        tileCoordYZheng = (tileCoordY - tileCoordYZheng) * 256;
        for (int i = tileCoordXZheng.intValue(); i < 256; i++) {
            for (int j = tileCoordYZheng.intValue(); j < 256; j++) {
                int rgb = Inimage.getRGB(i, j);
                Outimage.setRGB(
                        i - tileCoordXZheng.intValue(), j - tileCoordYZheng.intValue(), rgb);
            }
        }
        return Outimage;
    }

    public static BufferedImage getSplicingZuoxia(
            List<Double> coord, int z, BufferedImage Inimage, BufferedImage Outimage) {
        Double resolution = GoogleTilesScale.get(z);
        Double xFromOrigin = Math.floor((coord.get(0) - GoogleTilesOrigin[0]) / resolution);
        Double yFromOrigin = Math.floor((GoogleTilesOrigin[1] - coord.get(1)) / resolution);
        Double tileCoordX = xFromOrigin / 256;
        Double tileCoordY = yFromOrigin / 256;
        Double tileCoordXZheng = Math.floor(tileCoordX);
        Double tileCoordYZheng = Math.floor(tileCoordY);
        //  此处求得偏移量
        tileCoordXZheng = (tileCoordX - tileCoordXZheng) * 256;
        tileCoordYZheng = (tileCoordY - tileCoordYZheng) * 256;
        for (int i = tileCoordXZheng.intValue(); i < 256; i++) {
            for (int j = 0; j < tileCoordYZheng.intValue(); j++) {
                int rgb = Inimage.getRGB(i, j);
                Outimage.setRGB(
                        i - tileCoordXZheng.intValue(), 256 + j - tileCoordYZheng.intValue(), rgb);
            }
        }
        return Outimage;
    }

    public static BufferedImage getSplicingYouShang(
            List<Double> coord, int z, BufferedImage Inimage, BufferedImage Outimage) {
        Double resolution = GoogleTilesScale.get(z);
        Double xFromOrigin = Math.floor((coord.get(0) - GoogleTilesOrigin[0]) / resolution);
        Double yFromOrigin = Math.floor((GoogleTilesOrigin[1] - coord.get(1)) / resolution);
        Double tileCoordX = xFromOrigin / 256;
        Double tileCoordY = yFromOrigin / 256;
        Double tileCoordXZheng = Math.floor(tileCoordX);
        Double tileCoordYZheng = Math.floor(tileCoordY);
        //  此处求得偏移量
        tileCoordXZheng = (tileCoordX - tileCoordXZheng) * 256;
        tileCoordYZheng = (tileCoordY - tileCoordYZheng) * 256;
        for (int i = 0; i < tileCoordXZheng.intValue(); i++) {
            for (int j = tileCoordYZheng.intValue(); j < 256; j++) {
                int rgb = Inimage.getRGB(i, j);
                Outimage.setRGB(
                        256 + i - tileCoordXZheng.intValue(), j - tileCoordYZheng.intValue(), rgb);
            }
        }
        return Outimage;
    }

    public static BufferedImage getSplicingYouXia(
            List<Double> coord, int z, BufferedImage Inimage, BufferedImage Outimage) {
        Double resolution = GoogleTilesScale.get(z);
        Double xFromOrigin = Math.floor((coord.get(0) - GoogleTilesOrigin[0]) / resolution);
        Double yFromOrigin = Math.floor((GoogleTilesOrigin[1] - coord.get(1)) / resolution);
        Double tileCoordX = xFromOrigin / 256;
        Double tileCoordY = yFromOrigin / 256;
        Double tileCoordXZheng = Math.floor(tileCoordX);
        Double tileCoordYZheng = Math.floor(tileCoordY);
        //  此处求得偏移量
        tileCoordXZheng = (tileCoordX - tileCoordXZheng) * 256;
        tileCoordYZheng = (tileCoordY - tileCoordYZheng) * 256;
        for (int i = 0; i < tileCoordXZheng.intValue(); i++) {
            for (int j = 0; j < tileCoordYZheng.intValue(); j++) {
                int rgb = Inimage.getRGB(i, j);
                Outimage.setRGB(
                        256 + i - tileCoordXZheng.intValue(),
                        256 + j - tileCoordYZheng.intValue(),
                        rgb);
            }
        }
        return Outimage;
    }
}
