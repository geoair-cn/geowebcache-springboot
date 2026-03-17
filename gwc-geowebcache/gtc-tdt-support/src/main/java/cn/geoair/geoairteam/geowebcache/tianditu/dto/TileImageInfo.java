/**
 * 该程序是免费软件：您可以根据自由软件基金会发布的 GNU 宽松通用公共许可证 （许可证的第 3 版或（由您选择）任何更高版本）的条款重新分发和/或修改它.
 *
 * <p>分发此程序是希望它有用，但不作任何保证； 甚至没有对适销性或针对特定目的的适用性的暗示保证。 有关更多详细信息，请参阅 GNU 通用公共许可证.
 *
 * <p>您应该已经收到一份 GNU 宽松通用公共许可证以及该程序。如果没有，请参阅 <http:www.gnu.orglicenses>。
 *
 * @author geoair，版权所有 2021
 */
package cn.geoair.geoairteam.geowebcache.tianditu.dto;

/**
 * 表示 ArcGIS 切片缓存配置文件中的 {@code TileImageInfo} 元素。
 *
 * <p>XML representation:
 *
 * <pre>
 * <code>
 *   &lt;TileImageInfo xsi:type='typens:TileImageInfo'&gt;
 *     &lt;CacheTileFormat&gt;JPEG&lt;/CacheTileFormat&gt;
 *     &lt;CompressionQuality&gt;80&lt;/CompressionQuality&gt;
 *     &lt;Antialiasing&gt;true&lt;/Antialiasing&gt;
 *   &lt;/TileImageInfo&gt;
 * </code>
 * </pre>
 *
 * @author geoair
 */
public class TileImageInfo {

    private String cacheTileFormat;

    private float compressionQuality;

    private boolean antialiasing;

    private int BandCount;

    private float LERCError;

    /**
     * {@code PNG8, PNG24, PNG32, JPEG, Mixed} 之一
     *
     * <p>{@code Mixed} 主要使用 JPEG，但在缓存的边界上使用 32
     */
    public String getCacheTileFormat() {
        return cacheTileFormat;
    }

    public float getCompressionQuality() {
        return compressionQuality;
    }

    public boolean isAntialiasing() {
        return antialiasing;
    }

    public int getBandCount() {
        return BandCount;
    }

    public float getLERCError() {
        return LERCError;
    }
}
