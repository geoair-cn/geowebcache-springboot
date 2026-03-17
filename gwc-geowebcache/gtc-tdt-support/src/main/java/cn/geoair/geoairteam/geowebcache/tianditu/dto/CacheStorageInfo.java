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
 * 表示 ArcGIS 切片缓存配置文件中的 {@code CacheStorageInfo} 元素。
 *
 * <p>此元素从 ArcGIS 10.0 开始存在，用于定义缓存是“分解”格式还是“压缩”格式。 由于 ESRI 没有记录“compact”格式，我们只支持“exploded”格式。
 *
 * <p>XML 表示:
 *
 * <pre>
 * <code>
 *   &lt;CacheStorageInfo xsi:type='typens:CacheStorageInfo'&gt;
 *     &lt;StorageFormat&gt;esriMapCacheStorageModeExploded&lt;/StorageFormat&gt;
 *     &lt;PacketSize&gt;0&lt;/PacketSize&gt;
 *   &lt;/CacheStorageInfo&gt;
 * </code>
 * </pre>
 *
 * @author geoair
 */
public class CacheStorageInfo {

    public static final String EXPLODED_FORMAT_CODE = "esriMapCacheStorageModeExploded";
    public static final String COMPACT_FORMAT_CODE = "esriMapCacheStorageModeCompact";
    public static final String COMPACT_FORMAT_CODE_V2 = "esriMapCacheStorageModeCompactV2";

    private String storageFormat;

    private int packetSize;

    private Object readResolve() {
        if (storageFormat == null) {
            storageFormat = EXPLODED_FORMAT_CODE;
        }
        return this;
    }

    /** 配置文件中定义的存储格式，默认为展开 {@link #EXPLODED_FORMAT_CODE exploded format} */
    public String getStorageFormat() {
        return storageFormat;
    }

    public int getPacketSize() {
        return packetSize;
    }
}
