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

import com.thoughtworks.xstream.XStream;
import java.io.Reader;
import java.util.ArrayList;
import org.geowebcache.grid.BoundingBox;
import org.geowebcache.io.GeoWebCacheXStream;

/** 从 ArcGIS Server 切片缓存的{@code conf.xml}文件加载 {@link CacheInfo}对象 */
public class CacheInfoPersister {

    public CacheInfo load(final Reader reader) {
        XStream xs = getConfiguredXStream();
        CacheInfo ci = (CacheInfo) xs.fromXML(reader);
        return ci;
    }

    XStream getConfiguredXStream() {
        XStream xs = new GeoWebCacheXStream();

        // 允许任何属于 GWC 的内容
        // TODO：用更窄的白名单替换它
        xs.allowTypesByWildcard(new String[] {"org.geowebcache.**"});

        xs.setMode(XStream.NO_REFERENCES);

        xs.alias("SpatialReference", SpatialReference.class);
        xs.alias("TileOrigin", TileOrigin.class);

        xs.alias("TileCacheInfo", TileCacheInfo.class);
        xs.aliasField("SpatialReference", TileCacheInfo.class, "spatialReference");
        xs.aliasField("TileOrigin", TileCacheInfo.class, "tileOrigin");
        xs.aliasField("TileCols", TileCacheInfo.class, "tileCols");
        xs.aliasField("TileRows", TileCacheInfo.class, "tileRows");
        xs.aliasField("LODInfos", TileCacheInfo.class, "lodInfos");
        xs.alias("LODInfos", new ArrayList<LODInfo>().getClass());

        xs.alias("LODInfo", LODInfo.class);
        xs.aliasField("LevelID", LODInfo.class, "levelID");
        xs.aliasField("Scale", LODInfo.class, "scale");
        xs.aliasField("Resolution", LODInfo.class, "resolution");

        xs.alias("TileImageInfo", TileImageInfo.class);
        xs.aliasField("CacheTileFormat", TileImageInfo.class, "cacheTileFormat");
        xs.aliasField("CompressionQuality", TileImageInfo.class, "compressionQuality");
        xs.aliasField("Antialiasing", TileImageInfo.class, "antialiasing");

        xs.alias("CacheStorageInfo", CacheStorageInfo.class);
        xs.aliasField("StorageFormat", CacheStorageInfo.class, "storageFormat");
        xs.aliasField("PacketSize", CacheStorageInfo.class, "packetSize");

        xs.alias("CacheInfo", CacheInfo.class);
        xs.aliasField("TileCacheInfo", CacheInfo.class, "tileCacheInfo");
        xs.aliasField("TileImageInfo", CacheInfo.class, "tileImageInfo");
        xs.aliasField("CacheStorageInfo", CacheInfo.class, "cacheStorageInfo");

        xs.alias("EnvelopeN", EnvelopeN.class);
        xs.aliasField("XMin", EnvelopeN.class, "xmin");
        xs.aliasField("YMin", EnvelopeN.class, "ymin");
        xs.aliasField("XMax", EnvelopeN.class, "xmax");
        xs.aliasField("YMax", EnvelopeN.class, "ymax");
        xs.aliasField("SpatialReference", EnvelopeN.class, "spatialReference");

        return xs;
    }

    public BoundingBox parseLayerBounds(final Reader layerBoundsFile) {

        EnvelopeN envN = (EnvelopeN) getConfiguredXStream().fromXML(layerBoundsFile);

        BoundingBox bbox =
                new BoundingBox(envN.getXmin(), envN.getYmin(), envN.getXmax(), envN.getYmax());

        return bbox;
    }
}
