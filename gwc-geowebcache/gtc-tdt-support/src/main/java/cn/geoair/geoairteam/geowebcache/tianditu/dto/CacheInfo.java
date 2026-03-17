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

public class CacheInfo {

    private TileCacheInfo tileCacheInfo;

    private TileImageInfo tileImageInfo;

    private CacheStorageInfo cacheStorageInfo;

    private Object readResolve() {
        if (cacheStorageInfo == null) {
            cacheStorageInfo = new CacheStorageInfo();
        }
        return this;
    }

    public TileCacheInfo getTileCacheInfo() {
        return tileCacheInfo;
    }

    public TileImageInfo getTileImageInfo() {
        return tileImageInfo;
    }

    public CacheStorageInfo getCacheStorageInfo() {
        return cacheStorageInfo;
    }
}
