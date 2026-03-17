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
import org.geowebcache.io.Resource;

/**
 * ArcGIS 10.0 - 10.2 的 ArcGIS 压缩缓存的实现
 *
 * <p>压缩缓存由包含实际图像数据的包索引文件 (*.bundlx) 和包文件 (*.bundle) 组成。 每个 .bundlx 文件都包含一个 16 字节的页眉和 16 字节的页脚。
 * 页眉和页脚之间是 5 字节偏移量的 128x128 矩阵（16384 个图块）。 每个偏移量都指向相应 .bundle 文件中的一个 4 字节字，该文件包含平铺图像数据的大小。 实际图像数据从
 * offset+4 开始。 如果大小为零，则没有可用的图像数据并且不使用索引条目。 如果地图缓存的行或列超过 128 行，则它会被分成多个 .bundlx 和 .bundle 文件。
 *
 * @author geoair
 */
public class ArcGISCompactCacheV1 extends ArcGISCompactCache {
    private static final int COMPACT_CACHE_HEADER_LENGTH = 16;

    private BundlxCache indexCache;

    /**
     * 构建新的 ArcGIS 10.0-10.2 紧凑缓存。
     *
     * @param pathToCacheRoot – 压缩缓存目录的路径（通常是“.../_alllayers/”） 路径必须包含缩放级别的目录（名为“Lxx”）。
     */
    public ArcGISCompactCacheV1(String pathToCacheRoot) {
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
            String pathToBundlxFile = basePath + BUNDLX_EXT;
            String pathToBundleFile = basePath + BUNDLE_EXT;

            if (!(new File(pathToBundleFile)).exists() || !(new File(pathToBundlxFile)).exists())
                return null;

            long tileOffset = readTileStartOffset(pathToBundlxFile, row, col);
            int tileSize = readTileSize(pathToBundleFile, tileOffset);

            tileOffset += 4;

            if (tileSize > 0) res = new BundleFileResource(pathToBundleFile, tileOffset, tileSize);

            entry = new BundlxCache.CacheEntry(pathToBundleFile, tileOffset, tileSize);

            indexCache.put(key, entry);
        }

        return res;
    }

    private long readTileStartOffset(String bundlxFile, int row, int col) {
        int index = BUNDLX_MAXIDX * (col % BUNDLX_MAXIDX) + (row % BUNDLX_MAXIDX);

        ByteBuffer idxBytes =
                readFromLittleEndianFile(bundlxFile, (index * 5) + COMPACT_CACHE_HEADER_LENGTH, 5);

        return idxBytes.getLong();
    }

    private int readTileSize(String bundlxFile, long offset) {
        ByteBuffer tileSize = readFromLittleEndianFile(bundlxFile, offset, 4);

        return tileSize.getInt();
    }
}
