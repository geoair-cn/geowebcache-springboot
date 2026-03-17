/**
 * 该程序是免费软件：您可以根据自由软件基金会发布的 GNU 宽松通用公共许可证 （许可证的第 3 版或（由您选择）任何更高版本）的条款重新分发和/或修改它.
 *
 * <p>分发此程序是希望它有用，但不作任何保证； 甚至没有对适销性或针对特定目的的适用性的暗示保证。 有关更多详细信息，请参阅 GNU 通用公共许可证.
 *
 * <p>您应该已经收到一份 GNU 宽松通用公共许可证以及该程序。如果没有，请参阅 <http:www.gnu.orglicenses>。
 *
 * @author geoair，版权所有 2021
 */
package cn.geoair.geoairteam.geowebcache.tianditu.compact;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.geowebcache.io.Resource;

/**
 * ArcGIS 10.3 的 ArcGIS 压缩缓存的实现
 *
 * <p>压缩缓存由包含索引和实际图像数据的包文件 (*.bundle) 组成。 每个 .bundle 文件都以 64 字节的标头开头。 在 8 字节字的标头 128x128 矩阵（16384
 * 个瓦片）之后。 每个字的前 5 个字节是指向同一个 .bundle 文件内的平铺图像数据的偏移量。 接下来的 3 个字节是图像数据的大小。 图像数据的大小在 4 字节字中的偏移 4 处重复。
 * 未使用的索引条目使用 04|00|00|00|00|00|00|00。 如果大小为零，则没有可用的图像数据并且索引条目是。 如果地图缓存的行或列超过 128 行，则将其划分为多个 .bundle
 * 文件
 *
 * @author geoair
 */
public class ArcGISCompactCacheV2 extends ArcGISCompactCache {
    private static final int COMPACT_CACHE_HEADER_LENGTH = 64;

    private BundlxCache indexCache;

    /**
     * 构建新的 ArcGIS 10.3 压缩缓存。
     *
     * @param pathToCacheRoot pathToCacheRoot – 压缩缓存目录的路径（通常是“.../_alllayers/”）。
     *     路径必须包含缩放级别的目录（名为“Lxx”）。
     */
    public ArcGISCompactCacheV2(String pathToCacheRoot) {
        if (pathToCacheRoot.endsWith("" + File.separatorChar))
            this.pathToCacheRoot = pathToCacheRoot;
        else this.pathToCacheRoot = pathToCacheRoot + File.separatorChar;

        indexCache = new BundlxCache(10000);
    }

    @Override
    public Resource getBundleFileResource(int zoom, int row, int col) {
        if (zoom < 0 || col < 0 || row < 0) return null;

        BundlxCache.CacheKey key = new BundlxCache.CacheKey(zoom, row, col);
        BundlxCache.CacheEntry entry = null;

        Resource res = null;

        if ((entry = indexCache.get(key)) != null) {
            if (entry.size > 0)
                res = new BundleFileResource(entry.pathToBundleFile, entry.offset, entry.size);
        } else {

            String basePath = buildBundleFilePath(zoom, row, col);
            String pathToBundleFile = basePath + BUNDLE_EXT;

            if (!(new File(pathToBundleFile)).exists()) return null;

            entry = createCacheEntry(pathToBundleFile, row, col);

            if (entry.size > 0)
                res = new BundleFileResource(pathToBundleFile, entry.offset, entry.size);

            indexCache.put(key, entry);
        }

        return res;
    }

    private BundlxCache.CacheEntry createCacheEntry(String bundleFile, int row, int col) {
        // col and row are inverted for 10.3 caches
        int index = BUNDLX_MAXIDX * (row % BUNDLX_MAXIDX) + (col % BUNDLX_MAXIDX);

        // to save one addtional read, we read all 8 bytes in one read
        ByteBuffer offsetAndSize =
                readFromLittleEndianFile(bundleFile, (index * 8) + COMPACT_CACHE_HEADER_LENGTH, 8);

        byte[] offsetBytes = new byte[8];
        byte[] sizeBytes = new byte[4];

        offsetAndSize.get(offsetBytes, 0, 5);
        offsetAndSize.get(sizeBytes, 0, 3);

        long tileOffset = ByteBuffer.wrap(offsetBytes).order(ByteOrder.LITTLE_ENDIAN).getLong();
        int tileSize = ByteBuffer.wrap(sizeBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();

        return new BundlxCache.CacheEntry(bundleFile, tileOffset, tileSize);
    }
}
