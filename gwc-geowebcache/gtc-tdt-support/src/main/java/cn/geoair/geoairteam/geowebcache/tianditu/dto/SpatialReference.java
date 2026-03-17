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
 * 表示缓存配置文件中的 {@code SpatialReference} 元素。
 *
 * <p>XML structure: <code>
 * <pre>
 * &lt;SpatialReference xsi:type='typens:ProjectedCoordinateSystem'&gt;
 *       &lt;WKT&gt;PROJCS[&quot;NZGD_2000_New_Zealand_Transverse_Mercator&quot;,GEOGCS[&quot;GCS_NZGD_2000&quot;,DATUM[&quot;D_NZGD_2000&quot;,SPHEROID[&quot;GRS_1980&quot;,6378137.0,298.257222101]],PRIMEM[&quot;Greenwich&quot;,0.0],UNIT[&quot;Degree&quot;,0.0174532925199433]],PROJECTION[&quot;Transverse_Mercator&quot;],PARAMETER[&quot;False_Easting&quot;,1600000.0],PARAMETER[&quot;False_Northing&quot;,10000000.0],PARAMETER[&quot;Central_Meridian&quot;,173.0],PARAMETER[&quot;Scale_Factor&quot;,0.9996],PARAMETER[&quot;Latitude_Of_Origin&quot;,0.0],UNIT[&quot;Meter&quot;,1.0],AUTHORITY[&quot;EPSG&quot;,2193]]&lt;/WKT&gt;
 *       &lt;XOrigin&gt;-4020900&lt;/XOrigin&gt;
 *       &lt;YOrigin&gt;1900&lt;/YOrigin&gt;
 *       &lt;XYScale&gt;450445547.3910538&lt;/XYScale&gt;
 *       &lt;ZOrigin&gt;0&lt;/ZOrigin&gt;
 *       &lt;ZScale&gt;1&lt;/ZScale&gt;
 *       &lt;MOrigin&gt;-100000&lt;/MOrigin&gt;
 *       &lt;MScale&gt;10000&lt;/MScale&gt;
 *       &lt;XYTolerance&gt;0.0037383177570093459&lt;/XYTolerance&gt;
 *       &lt;ZTolerance&gt;2&lt;/ZTolerance&gt;
 *       &lt;MTolerance&gt;2&lt;/MTolerance&gt;
 *       &lt;HighPrecision&gt;true&lt;/HighPrecision&gt;
 *       &lt;WKID&gt;2193&lt;/WKID&gt;
 *       &lt;LatestWKID&gt;2193&lt;/LatestWKID&gt;
 * &lt;/SpatialReference&gt;
 * </pre>
 * </code>
 *
 * @author Gabriel Roldan
 */
public class SpatialReference {

    private String WKT;

    private double XOrigin;

    private double YOrigin;

    private double XYScale;

    private double ZOrigin;

    private double ZScale;

    private double MOrigin;

    private double MScale;

    private double XYTolerance;

    private double ZTolerance;

    private double MTolerance;

    private boolean HighPrecision;

    private int WKID;

    private int LatestWKID;

    private double LeftLongitude;

    public String getWKT() {
        return WKT;
    }

    public double getXOrigin() {
        return XOrigin;
    }

    public double getYOrigin() {
        return YOrigin;
    }

    public double getXYScale() {
        return XYScale;
    }

    public double getZOrigin() {
        return ZOrigin;
    }

    public double getZScale() {
        return ZScale;
    }

    public double getMOrigin() {
        return MOrigin;
    }

    public double getMScale() {
        return MScale;
    }

    public double getXYTolerance() {
        return XYTolerance;
    }

    public double getZTolerance() {
        return ZTolerance;
    }

    public double getMTolerance() {
        return MTolerance;
    }

    public boolean isHighPrecision() {
        return HighPrecision;
    }

    public int getWKID() {
        return WKID;
    }

    /** ArcGIS 10.1+ 中的新功能 */
    public int getLatestWKID() {
        return LatestWKID;
    }

    /** 似乎只有 ArcGIS 9.2 格式？ */
    public double getLeftLongitude() {
        return LeftLongitude;
    }
}
