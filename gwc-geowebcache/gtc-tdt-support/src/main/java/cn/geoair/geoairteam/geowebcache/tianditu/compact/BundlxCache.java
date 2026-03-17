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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * 存储来自 .bundlx 文件的数据的缓存。
 *
 * <p>磁贴的缩放、行和列用作键。 条目包含 .bundle 文件的路径、图块的大小以及 .bundle 文件内图像数据的偏移量。
 *
 * @author geoair
 */
public class BundlxCache {
    public static class CacheKey {
        public final int zoom;

        public final int row;

        public final int col;

        public CacheKey(int zoom, int row, int col) {
            this.zoom = zoom;
            this.row = row;
            this.col = col;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheKey cacheKey = (CacheKey) o;

            if (zoom != cacheKey.zoom) return false;
            if (row != cacheKey.row) return false;
            return col == cacheKey.col;
        }

        @Override
        public int hashCode() {
            int result = zoom;
            result = 31 * result + row;
            result = 31 * result + col;
            return result;
        }
    }

    public static class CacheEntry {
        public CacheEntry(String pathToBundleFile, long offset, int size) {
            this.pathToBundleFile = pathToBundleFile;
            this.offset = offset;
            this.size = size;
        }

        public String pathToBundleFile;

        public long offset;

        public int size;
    }

    private Cache<CacheKey, CacheEntry> indexCache;

    /**
     * 存储路径的缓存。
     *
     * @param maxSize 缓存的最大大小。 如果缓存的大小等于 maxSize，则添加新条目将从缓存中删除最近最少使用的条目
     */
    public BundlxCache(int maxSize) {
        indexCache = CacheBuilder.newBuilder().maximumSize(maxSize).build();
    }

    /**
     * 从缓存中获取键的条目。
     *
     * @param key Key.
     * @return 如果键具有空值或键没有条目，则返回 null。
     */
    public synchronized CacheEntry get(CacheKey key) {
        return indexCache.getIfPresent(key);
    }

    /**
     * 将键条目映射放入此缓存中。
     *
     * @param key – 要添加的键。
     * @param entry – 要添加的条目
     */
    public synchronized void put(CacheKey key, CacheEntry entry) {
        indexCache.put(key, entry);
    }
}
