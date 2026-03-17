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
 * 表示 ArcGIS 切片缓存配置文件中的{@code LODInfo}（详细程度信息）元素
 *
 * <p>XML 表示:
 *
 * <pre>
 * <code>
 *       &lt;LODInfo xsi:type='typens:LODInfo'&gt;
 *         &lt;LevelID&gt;1&lt;/LevelID&gt;
 *         &lt;Scale&gt;6000000&lt;/Scale&gt;
 *         &lt;Resolution&gt;1587.5031750063501&lt;/Resolution&gt;
 *       &lt;/LODInfo&gt;
 * </code>
 * </pre>
 *
 * @author geoair
 */
public class LODInfo {

    private int levelID;

    private double scale;

    private double resolution;

    public int getLevelID() {
        return levelID;
    }

    public double getScale() {
        return scale;
    }

    public double getResolution() {
        return resolution;
    }
}
