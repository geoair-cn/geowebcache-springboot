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
 * 表示 ArcGIS 缓存配置文件中的 {@code TileOrigin} 元素。 平铺网格的左上点。 平铺原点通常不是平铺开始创建的点； 这只发生在地图的整个范围内。
 * 通常，切片原点远离地图，以确保地图区域将被覆盖，并且具有相同切片原点的其他缓存可以覆盖您的缓存。
 *
 * <p>XML Structure:
 *
 * <pre>
 * <code>
 *     &lt;TileOrigin xsi:type='typens:PointN'&gt;
 *       &lt;X&gt;-4020900&lt;/X&gt;
 *       &lt;Y&gt;19998100&lt;/Y&gt;
 *     &lt;/TileOrigin&gt;
 * </code>
 * </pre>
 *
 * @author geoair
 */
public class TileOrigin {

    private double X;

    private double Y;

    public double getX() {
        return X;
    }

    public double getY() {
        return Y;
    }
}
