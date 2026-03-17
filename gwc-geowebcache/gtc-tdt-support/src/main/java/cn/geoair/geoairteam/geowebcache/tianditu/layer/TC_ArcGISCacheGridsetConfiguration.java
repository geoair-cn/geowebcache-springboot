/**
 * 该程序是免费软件：您可以根据自由软件基金会发布的 GNU 宽松通用公共许可证 （许可证的第 3 版或（由您选择）任何更高版本）的条款重新分发和/或修改它.
 *
 * <p>分发此程序是希望它有用，但不作任何保证； 甚至没有对适销性或针对特定目的的适用性的暗示保证。 有关更多详细信息，请参阅 GNU 通用公共许可证.
 *
 * <p>您应该已经收到一份 GNU 宽松通用公共许可证以及该程序。如果没有，请参阅 <http:www.gnu.orglicenses>。
 *
 * @author geoair，版权所有 2021
 */
package cn.geoair.geoairteam.geowebcache.tianditu.layer;

import org.geowebcache.GeoWebCacheException;
import org.geowebcache.config.SimpleGridSetConfiguration;
import org.geowebcache.grid.GridSet;

/** {@link org.geowebcache.config.GridSetConfiguration} 用于 ArcGIS 缓存生成的网格集 */
public class TC_ArcGISCacheGridsetConfiguration extends SimpleGridSetConfiguration {

    @Override
    public String getIdentifier() {
        return "tc-天地图-ArcGIS Cache Generated Gridsets";
    }

    @Override
    public String getLocation() {
        return "";
    }

    @Override
    public void afterPropertiesSet() throws GeoWebCacheException {}

    @Override
    protected void addInternal(GridSet gs) {
        super.addInternal(gs);
    }
}
