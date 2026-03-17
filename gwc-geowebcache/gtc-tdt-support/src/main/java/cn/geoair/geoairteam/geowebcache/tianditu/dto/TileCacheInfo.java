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

import java.util.List;

/**
 * 表示 ArcGIS 缓存配置文件中的 {@code TileCacheInfo} 元素。
 *
 * <p>XML Structure:
 *
 * <pre>
 * <code>
 *   &lt;TileCacheInfo xsi:type='typens:TileCacheInfo'&gt;
 *     &lt;SpatialReference xsi:type='typens:ProjectedCoordinateSystem'&gt;
 *       ....
 *     &lt;/SpatialReference&gt;
 *     &lt;TileOrigin xsi:type='typens:PointN'&gt;
 *       &lt;X&gt;-4020900&lt;/X&gt;
 *       &lt;Y&gt;19998100&lt;/Y&gt;
 *     &lt;/TileOrigin&gt;
 *     &lt;TileCols&gt;512&lt;/TileCols&gt;
 *     &lt;TileRows&gt;512&lt;/TileRows&gt;
 *     &lt;DPI&gt;96&lt;/DPI&gt;
 *     &lt;PreciseDPI&gt;96&lt;/PreciseDPI&gt;
 *     &lt;LODInfos xsi:type='typens:ArrayOfLODInfo'&gt;
 *       &lt;LODInfo xsi:type='typens:LODInfo'&gt;
 *         &lt;LevelID&gt;0&lt;/LevelID&gt;
 *         &lt;Scale&gt;8000000&lt;/Scale&gt;
 *         &lt;Resolution&gt;2116.670900008467&lt;/Resolution&gt;
 *       &lt;/LODInfo&gt;
 *       .....
 *     &lt;/LODInfos&gt;
 *   &lt;/TileCacheInfo&gt;
 * </code>
 * </pre>
 *
 * @author Gabriel Roldan
 */
public class TileCacheInfo {

    private SpatialReference spatialReference;

    private TileOrigin tileOrigin;

    private int tileCols;

    private int tileRows;

    private int DPI;

    private int PreciseDPI;

    private List<LODInfo> lodInfos;

    public SpatialReference getSpatialReference() {
        return spatialReference;
    }

    public TileOrigin getTileOrigin() {
        return tileOrigin;
    }

    public int getTileCols() {
        return tileCols;
    }

    public int getTileRows() {
        return tileRows;
    }

    public int getDPI() {
        return DPI;
    }

    /** ArcGIS 10.1+ 中的新功能 */
    public int getPreciseDPI() {
        return PreciseDPI;
    }

    public List<LODInfo> getLodInfos() {
        return lodInfos;
    }
}
