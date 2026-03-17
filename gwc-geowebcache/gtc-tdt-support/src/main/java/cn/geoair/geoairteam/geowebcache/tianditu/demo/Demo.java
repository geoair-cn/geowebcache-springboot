/**
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Lesser General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 *
 * <p>Copyright 2019
 */
package cn.geoair.geoairteam.geowebcache.tianditu.demo;

import cn.geoair.geoairteam.geowebcache.tianditu.group.GroupMeta;
import cn.geoair.geoairteam.geowebcache.tianditu.group.TC_ArcGISCacheLayerGroup;
import cn.geoair.geoairteam.geowebcache.tianditu.layer.TC_ArcGISCacheLayer;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.text.StringEscapeUtils;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.filter.parameters.FloatParameterFilter;
import org.geowebcache.filter.parameters.ParameterFilter;
import org.geowebcache.filter.parameters.RegexParameterFilter;
import org.geowebcache.filter.parameters.StringParameterFilter;
import org.geowebcache.grid.BoundingBox;
import org.geowebcache.grid.GridSet;
import org.geowebcache.grid.GridSetBroker;
import org.geowebcache.grid.GridSubset;
import org.geowebcache.layer.TileLayer;
import org.geowebcache.layer.TileLayerDispatcher;
import org.geowebcache.mime.ApplicationMime;
import org.geowebcache.mime.MimeType;
import org.geowebcache.util.ServletUtils;
import org.springframework.util.Assert;

public class Demo {

    public static void makeMap(
            TileLayerDispatcher tileLayerDispatcher,
            GridSetBroker gridSetBroker,
            String action,
            HttpServletRequest request,
            HttpServletResponse response)
            throws GeoWebCacheException {
        String page = null;
        if (action != "") {

            String baselayer = request.getParameter("baselayer");

            TileLayer layer = tileLayerDispatcher.getTileLayer(baselayer);

            String gridSetStr = request.getParameter("subset");

            String formatStr = request.getParameter("format");

            page = generateHTML(layer, action, gridSetStr, formatStr, gridSetBroker);

        } else {
            if (request.getRequestURI().endsWith("/")) {
                try {
                    String reqUri = request.getRequestURI();
                    response.sendRedirect(
                            response.encodeRedirectURL(reqUri.substring(0, reqUri.length() - 1)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            } else {
                page = generateHTML();
            }
        }
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        try {
            response.getOutputStream().write(page.getBytes("UTF-8"));
        } catch (IOException ioe) {
            throw new GeoWebCacheException("渲染html页面失败");
        }
    }

    private static String generateHTML() {
        StringBuffer buf = new StringBuffer();

        buf.append("<html>\n");
        buf.append(ServletUtils.gwcHtmlHeader("", "图层组演示"));
        buf.append("<body>\n");
        buf.append(ServletUtils.gwcHtmlLogoLink(""));
        buf.append(
                "<table cellspacing=\"10\" border=\"0\">\n"
                        + "<tr><td><strong>图层组组名:</strong></td>\n"
                        + "<td><strong>排序方式（默认方式，您可以自己修改排序）:</strong></td>\n"
                        + "<td><strong>图层名称:</strong></td>\n"
                        + "<td><strong>图层类型:</strong></td>\n"
                        + "<td><strong>元数据信息:</strong></td>\n"
                        + "<td><strong>切片方案集:</strong></td>\n"
                        + "<td><strong>备注信息:</strong></td>\n");
        buf.append("</tr>\n");

        tableRows(buf);

        buf.append("</body></html>");

        return buf.toString();
    }

    private static void tableRows(StringBuffer buf) {
        HashMap<String, GroupMeta> layerHashMap = TC_ArcGISCacheLayerGroup.getGroupMetas();
        Set<String> stringSet = layerHashMap.keySet();
        for (String key : stringSet) {
            List<TC_ArcGISCacheLayer> tc_arcGISCacheLayers =
                    layerHashMap.get(key).getTGISCacheLayers();
            //  获取到某一个 key的 所有图层
            buf.append("<tr><td style=\"min-width: 100px;\"><strong>")
                    .append(key)
                    //                    .append("</strong><br /></td>\n");
                    .append("</strong><br />\n");
            buf.append("<a href=\"emptyDrawingCache/").append(key).append("\">清空图层缓存</a>\n");
            buf.append("</td>\n");
            String desort = "";

            TC_ArcGISCacheLayer tc_arcGISCacheLayer1 = tc_arcGISCacheLayers.get(0);
            GridSubset gridSubset = tc_arcGISCacheLayer1.getDefaultGridSubset();
            List<GridSubset> gridSubsets = new ArrayList<>();
            Map<GridSubset, String> layerNamel = new HashMap<>();

            for (TC_ArcGISCacheLayer tc_arcGISCacheLayer : tc_arcGISCacheLayers) {
                desort += tc_arcGISCacheLayer.getSortkey() + "_";
                GridSubset defaultGridSubset = tc_arcGISCacheLayer.getDefaultGridSubset();
                gridSubsets.add(defaultGridSubset);
                layerNamel.put(defaultGridSubset, tc_arcGISCacheLayer.getName());
            }
            int i = desort.lastIndexOf("_");

            desort = desort.substring(0, i);

            String desortShow = "";

            if (desort.length() > 20) {
                desortShow = desort.substring(0, 20) + "...";
            } else {
                desortShow = desort;
            }
            buf.append("<td style=\"min-width: 100px;\">")
                    .append(desortShow)
                    .append("<br /></td>\n");
            String layername = key + ":" + desort;
            buf.append("<td style=\"min-width: 100px;\"><strong>")
                    .append(
                            layername.length() > 20
                                    ? layername.substring(0, 20) + "..."
                                    : layername)
                    .append("</strong><br /></td>\n");

            buf.append("<td style=\"min-width: 100px;\">")
                    .append("TC_ArcGISCacheLayer")
                    .append("<br /></td>\n");

            //  元数据接口
            buf.append(
                    "<td><a href=\""
                            + "group"
                            + "/"
                            + layername
                            + "/wmts?REQUEST=getcapabilities \">元数据</a>");

            buf.append("</td>");

            buf.append("<td style=\"min-width: 100px;\">")
                    .append(spritMartset(gridSubset.getName()))
                    .append("<br /></td>\n");
            // 备注信息
            buf.append("<td style=\"min-width: 100px;\">")
                    .append(TC_ArcGISCacheLayerGroup.getLayerGroupRemark(key))
                    .append("<br /></td>\n");

            buf.append("<td>打开图层(天地图请求方式):");
            int size = gridSubsets.size();
            if (size > 20) {
                buf.append("{数量太多，取前20个}");
                size = 20;
            }
            buf.append(" [");
            for (int i1 = 0; i1 < size; i1++) {
                GridSubset gridSubset1 = gridSubsets.get(i1);
                buf.append(
                        generateDemoUrl(layername, layerNamel.get(gridSubset1), gridSubset1, i1)
                                + " ");
            }
            buf.append("]<td>\n");
        }
    }

    static String spritMartset(String old) {
        String[] split = old.split(":");
        String s = split[1];
        String[] s1 = s.split("_");
        return "EPSG:" + s1[0];
    }

    private static String generateDemoUrl(
            String layerName, String baselayername, GridSubset gridSubset1, int i) {
        return String.format(
                "<a href=\"geoair/demo?layer=%s&format=%s&subset=%s&baselayer=%s\">%s</a>",
                layerName, "image/png", gridSubset1.getName(), baselayername, "中心点" + i);
    }

    private static String generateHTML(
            TileLayer layer,
            String layername,
            String gridSetStr,
            String formatStr,
            GridSetBroker gridSetBroker) {
        String layerName = layername;

        GridSubset gridSubset = layer.getGridSubset(gridSetStr);

        GridSet gridSet = gridSubset.getGridSet();

        BoundingBox bbox = gridSubset.getGridSetBounds();
        BoundingBox zoomBounds = gridSubset.getOriginalExtent();

        MimeType formatMime = null;

        for (MimeType mime : layer.getMimeTypes()) {
            if (formatStr.equalsIgnoreCase(mime.getFormat())) {
                formatMime = mime;
            }
        }
        if (formatMime == null) {
            formatMime = layer.getDefaultMimeType();
        }

        StringBuffer buf = new StringBuffer();

        String openLayersPath = "../rest/web/openlayers3/";

        buf.append("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head>\n");
        buf.append("<meta http-equiv=\"imagetoolbar\" content=\"no\">\n" + "<title>")
                .append(layerName);
        buf.append(" ").append(gridSubset.getName());
        buf.append(" ").append(formatStr);
        buf.append("</title>\n");
        buf.append(
                "<style type=\"text/css\">\n"
                        + "body { font-family: sans-serif; font-weight: bold; font-size: .8em; }\n"
                        + "body { border: 0px; margin: 0px; padding: 0px; }\n"
                        + "#map { width: 85%; height: 85%; border: 0px; padding: 0px; }\n"
                        + "#info iframe {border: none;}\n"
                        + ".ol-scale-value {top: 24px; right: 8px; position: absolute; }\n"
                        + ".ol-zoom-value {top: 40px; right: 8px; position: absolute; }\n"
                        + "</style>\n");

        buf.append("<script src=\"").append(openLayersPath).append("ol.js\"></script>\n");
        buf.append("<link rel='stylesheet' href='")
                .append(openLayersPath)
                .append("ol.css' type='text/css'>\n");
        buf.append(
                "<script type=\"text/javascript\">\n"
                        + "function init(){\n"
                        + "function ScaleControl(opt_options) {\n"
                        + "  var options = opt_options || {};\n"
                        + "\n"
                        + "  var element = document.createElement('div');\n"
                        + "  element.setAttribute('id', 'scale');\n"
                        + "  element.className = 'ol-scale-value';\n"
                        + "\n"
                        + "  ol.control.Control.call(this, {\n"
                        + "    element: element,\n"
                        + "    target: options.target\n"
                        + "  });\n"
                        + "\n"
                        + "};\n"
                        + "ol.inherits(ScaleControl, ol.control.Control);\n"
                        + "ScaleControl.prototype.setMap = function(map) {\n"
                        + "  map.on('postrender', function() {\n"
                        + "    var view = map.getView();\n"
                        + "    var resolution = view.getResolution();\n");
        buf.append("    var dpi = ").append(gridSubset.getDotsPerInch()).append(";\n");
        buf.append("    var mpu = map.getView().getProjection().getMetersPerUnit();\n");
        buf.append(
                "    var scale = resolution * mpu * 39.37 * dpi;\n"
                        + "\n"
                        + "    if (scale >= 9500 && scale <= 950000) {\n"
                        + "        scale = Math.round(scale / 1000) + 'K';\n"
                        + "    } else if (scale >= 950000) {\n"
                        + "        scale = Math.round(scale / 1000000) + 'M';\n"
                        + "    } else {\n"
                        + "        scale = Math.round(scale);\n"
                        + "    } \n"
                        + "    document.getElementById('scale').innerHTML =  'Scale = 1 : ' + scale;\n"
                        + "  }, this);\n"
                        + "  ol.control.Control.prototype.setMap.call(this, map);\n"
                        + "}\n"
                        + "\n");
        buf.append(
                "function ZoomControl(opt_options) {\n"
                        + "  var options = opt_options || {};\n"
                        + "\n"
                        + "  var element = document.createElement('div');\n"
                        + "  element.setAttribute('id', 'zoom');\n"
                        + "  element.className = 'ol-zoom-value';\n"
                        + "\n"
                        + "  ol.control.Control.call(this, {\n"
                        + "    element: element,\n"
                        + "    target: options.target\n"
                        + "  });\n"
                        + "\n"
                        + "};\n"
                        + "ol.inherits(ZoomControl, ol.control.Control);\n"
                        + "ZoomControl.prototype.setMap = function(map) {\n"
                        + "  map.on('moveend', function() {\n"
                        + "    var view = map.getView();\n"
                        + "    document.getElementById('zoom').innerHTML =  'Zoom level = ' + view.getZoom();\n"
                        + "  }, this);\n"
                        + "  ol.control.Control.prototype.setMap.call(this, map);\n"
                        + "}\n"
                        + "\n");
        buf.append("var gridsetName = '")
                .append(gridSubset.getGridSet().getName())
                .append("';\n" + "var gridNames = ")
                .append(
                        Arrays.stream(gridSubset.getGridNames())
                                .map(StringEscapeUtils::escapeEcmaScript)
                                .map(s -> String.format("'%s'", s))
                                .collect(Collectors.joining(", ", "[", "]")));
        if (layerName.contains("@") && layerName.contains(",")) {
            buf.append(
                    ";\n"
                            + "var baseUrl = '../layer_group/"
                            + layerName
                            + "/wmts';\n"
                            + "var style = '';\n");
        } else {
            buf.append(
                    ";\n"
                            + "var baseUrl = '../group/"
                            + layerName
                            + "/wmts';\n"
                            + "var style = '';\n");
        }

        buf.append("var format = '").append(formatStr).append("';\n");
        buf.append("var infoFormat = 'text/html';\n");
        buf.append("var layerName = '").append(layerName).append("';\n");

        String unit = "";
        double mpu = gridSet.getMetersPerUnit();
        if (doubleEquals(mpu, 1)) {
            unit = "m";
        } else if (doubleEquals(mpu, 0.3048)) {
            unit = "ft";
            // Use the average of equatorial and polar radius, and a large margin of error
        } else if (doubleEquals(
                mpu, Math.PI * (6378137 + 6356752) / 360, Math.PI * (6378137 - 6356752) / 360)) {
            unit = "degrees";
        }

        buf.append("var projection = new ol.proj.Projection({\n")
                .append("code: '")
                .append(gridSubset.getSRS().toString())
                .append("',\n")
                .append("units: '")
                .append(unit)
                .append("',\n")
                .append("axisOrientation: 'neu'\n")
                .append("});\n");
        buf.append("var resolutions = ")
                .append(Arrays.toString(gridSubset.getResolutions()))
                .append(";\n");

        if (formatMime.isVector()) {
            buf.append(
                    "params = {\n"
                            + "  'REQUEST': 'GetTile',\n"
                            + "  'SERVICE': 'WMTS',\n"
                            + "  'VERSION': '1.0.0',\n"
                            + "  'LAYER': layerName,\n"
                            + "  'STYLE': style,\n"
                            + "  'TILEMATRIX': '{z}',\n"
                            + "  'TILEMATRIXSET': 'c',\n"
                            + "  'FORMAT': format,\n"
                            + "  'TILECOL': '{x}',\n"
                            + "  'TILEROW': '{y}'\n"
                            + "};\n"
                            + "\n");
            buf.append(
                    "function constructSource() {\n"
                            + "  var url = baseUrl+'?'\n"
                            + "  for (var param in params) {\n"
                            + "    url = url + param + '=' + params[param] + '&';\n"
                            + "  }\n"
                            + "  url = url.slice(0, -1);\n"
                            + "\n"
                            + "  var source = new ol.source.VectorTile({\n"
                            + "    url: url,\n");
            // Examine mime type for correct VT format
            String vtName = formatMime.getInternalName();
            if (ApplicationMime.mapboxVector.getInternalName().equals(vtName)) {
                buf.append("    format: new ol.format.MVT({}),\n");
            } else if (ApplicationMime.topojson.getInternalName().equals(vtName)) {
                buf.append("    format: new ol.format.TopoJSON({}),\n");
            } else if (ApplicationMime.geojson.getInternalName().equals(vtName)) {
                buf.append("    format: new ol.format.GeoJSON({}),\n");
            }
            buf.append("    projection: projection,\n" + "    tileGrid: new ol.tilegrid.WMTS({\n");
            buf.append("      tileSize: [")
                    .append(gridSubset.getTileWidth())
                    .append(",")
                    .append(gridSubset.getTileHeight())
                    .append("],\n");
            buf.append("      origin: [")
                    .append(bbox.getMinX())
                    .append(", ")
                    .append(bbox.getMaxY())
                    .append("],\n");
            buf.append(
                    "      resolutions: resolutions,\n"
                            + "      matrixIds: gridNames\n"
                            + "    }),\n"
                            + "    wrapX: true\n"
                            + "  });\n"
                            + "  return source;\n"
                            + "}\n"
                            + "\n"
                            + "var layer = new ol.layer.VectorTile({\n"
                            + "  source: constructSource()\n"
                            + "});\n"
                            + "\n");

        } else {
            buf.append(
                    "baseParams = ['VERSION','LAYER','STYLE','TILEMATRIX','TILEMATRIXSET','SERVICE','FORMAT'];\n"
                            + "\n"
                            + "params = {\n"
                            + "  'VERSION': '1.0.0',\n"
                            + "  'LAYER': layerName,\n"
                            + "  'STYLE': style,\n"
                            + "  'TILEMATRIX': gridNames,\n"
                            + "  'TILEMATRIXSET': 'c',\n"
                            + "  'SERVICE': 'WMTS',\n"
                            + "  'FORMAT': format\n"
                            + "};\n"
                            + "\n");
            buf.append(
                    "function constructSource() {\n"
                            + "  var url = baseUrl+'?'\n"
                            + "  for (var param in params) {\n"
                            + "    if (baseParams.indexOf(param.toUpperCase()) < 0) {\n"
                            + "      url = url + param + '=' + params[param] + '&';\n"
                            + "    }\n"
                            + "  }\n"
                            + "  url = url.slice(0, -1);\n"
                            + "\n"
                            + "  var source = new ol.source.WMTS({\n"
                            + "    url: url,\n"
                            + "    layer: params['LAYER'],\n"
                            + "    matrixSet: params['TILEMATRIXSET'],\n"
                            + "    format: params['FORMAT'],\n"
                            + "    projection: projection,\n"
                            + "    tileGrid: new ol.tilegrid.WMTS({\n");
            buf.append("      tileSize: [")
                    .append(gridSubset.getTileWidth())
                    .append(",")
                    .append(gridSubset.getTileHeight())
                    .append("],\n");
            buf.append("      extent: [")
                    .append(bbox.getMinX())
                    .append(",")
                    .append(bbox.getMinY())
                    .append(",")
                    .append(bbox.getMaxX())
                    .append(",")
                    .append(bbox.getMaxY())
                    .append("],\n");

            if (gridSubset.fullGridSetCoverage()) {
                buf.append("      origins: [");
                for (int i = 0; i < gridSubset.getResolutions().length; i++) {
                    if (i != 0) {
                        buf.append(",");
                    }
                    BoundingBox subbox = gridSubset.getCoverageBounds(i);
                    buf.append("[")
                            .append(subbox.getMinX())
                            .append(", ")
                            .append(subbox.getMaxY())
                            .append("]");
                }
                buf.append("],\n");
            } else {
                buf.append("      origin: [")
                        .append(bbox.getMinX())
                        .append(", ")
                        .append(bbox.getMaxY())
                        .append("],\n");
            }
            buf.append(
                    "      resolutions: resolutions,\n"
                            + "      matrixIds: params['TILEMATRIX']\n"
                            + "    }),\n"
                            + "    style: params['STYLE'],\n"
                            + "    wrapX: true\n"
                            + "  });\n"
                            + "  return source;\n"
                            + "}\n"
                            + "\n"
                            + "var layer = new ol.layer.Tile({\n"
                            + "  source: constructSource()\n"
                            + "});\n"
                            + "\n");
        }

        buf.append(
                "var view = new ol.View({\n"
                        + "  center: [0, 0],\n"
                        + "  zoom: 2,\n"
                        + "  resolutions: resolutions,\n"
                        + "  projection: projection,\n");
        buf.append("  extent: [").append(bbox.toString()).append("]\n");
        buf.append(
                "});\n"
                        + "\n"
                        + "var map = new ol.Map({\n"
                        + "  controls: ol.control.defaults({attribution: false}).extend([\n"
                        + "    new ol.control.MousePosition(),\n"
                        + "    new ScaleControl(),\n"
                        + "    new ZoomControl()\n"
                        + "  ]),\n"
                        + "  layers: [layer],\n"
                        + "  target: 'map',\n"
                        + "  view: view\n"
                        + "});\n");
        buf.append("map.getView().fit([")
                .append(zoomBounds.toString())
                .append("], map.getSize());\n");
        buf.append(
                "\n"
                        + "window.setParam = function(name, value) {\n"
                        + "  if (name == \"STYLES\") {\n"
                        + "    name = \"STYLE\"\n"
                        + "  }\n"
                        + "  params[name] = value;\n"
                        + "  layer.setSource(constructSource());\n"
                        + "  map.updateSize();\n"
                        + "} \n"
                        + "\n");
        //                        + "map.on('singleclick', function(evt) {\n"
        //                        + "  document.getElementById('info').innerHTML = '';\n"
        //                        + "\n"
        //                        + "  var source = layer.getSource();\n"
        //                        + "  var resolution = view.getResolution();\n"
        //                        + "  var tilegrid = source.getTileGrid();\n"
        //                        + "  var tileResolutions = tilegrid.getResolutions();\n"
        //                        + "  var zoomIdx, diff = Infinity;\n"
        //                        + "\n"
        //                        + "  for (var i = 0; i < tileResolutions.length; i++) {\n"
        //                        + "      var tileResolution = tileResolutions[i];\n"
        //                        + "      var diffP = Math.abs(resolution-tileResolution);\n"
        //                        + "      if (diffP < diff) {\n"
        //                        + "          diff = diffP;\n"
        //                        + "          zoomIdx = i;\n"
        //                        + "      }\n"
        //                        + "      if (tileResolution < resolution) {\n"
        //                        + "        break;\n"
        //                        + "      }\n"
        //                        + "  }\n"
        //                        + "  var tileSize = tilegrid.getTileSize(zoomIdx);\n"
        //                        + "  var tileOrigin = tilegrid.getOrigin(zoomIdx);\n"
        //                        + "\n"
        //                        + "  var fx = (evt.coordinate[0] - tileOrigin[0]) / (resolution *
        // tileSize[0]);\n"
        //                        + "  var fy = (tileOrigin[1] - evt.coordinate[1]) / (resolution *
        // tileSize[1]);\n"
        //                        + "  var tileCol = Math.floor(fx);\n"
        //                        + "  var tileRow = Math.floor(fy);\n"
        //                        + "  var tileI = Math.floor((fx - tileCol) * tileSize[0]);\n"
        //                        + "  var tileJ = Math.floor((fy - tileRow) * tileSize[1]);\n"
        //                        + "  var matrixIds = tilegrid.getMatrixIds()[zoomIdx];\n"
        //                        + "  var matrixSet = source.getMatrixSet();\n"
        //                        + "\n"
        //                        + "  var url = baseUrl+'?'\n"
        //                        + "  for (var param in params) {\n"
        //                        + "    if (param.toUpperCase() == 'TILEMATRIX') {\n"
        //                        + "      url = url + 'TILEMATRIX='+matrixIds+'&';\n"
        //                        + "    } else {\n"
        //                        + "      url = url + param + '=' + params[param] + '&';\n"
        //                        + "    }\n"
        //                        + "  }\n"
        //                        + "\n"
        //                        + "  url = url\n"
        //                        + "    + 'SERVICE=WMTS&REQUEST=GetFeatureInfo'\n"
        //                        + "    + '&INFOFORMAT=' +  infoFormat\n"
        //                        + "    + '&TileCol=' +  tileCol\n"
        //                        + "    + '&TileRow=' +  tileRow\n"
        //                        + "    + '&I=' +  tileI\n"
        //                        + "    + '&J=' +  tileJ;\n"
        //                        + "\n"
        //                        + "  if (url) {\n"
        //                        + "    document.getElementById('info').innerHTML = 'Loading...
        // please wait...';\n"
        //                        + "    var xmlhttp = new XMLHttpRequest();"
        //                        + "    xmlhttp.onreadystatechange = function() {\n"
        //                        + "        if (xmlhttp.readyState == XMLHttpRequest.DONE ) {\n"
        //                        + "           if (xmlhttp.status == 200) {\n"
        //                        + "               document.getElementById('info').innerHTML =
        // xmlhttp.responseText;\n"
        //                        + "           }\n"
        //                        + "           else {\n"
        //                        + "              document.getElementById('info').innerHTML =
        // '';\n"
        //                        + "           }\n"
        //                        + "        }\n"
        //                        + "    }\n"
        //                        + "  xmlhttp.open('GET', url, true);\n"
        //                        + "  xmlhttp.send();\n"
        //                        + "  }\n"
        //                        + "});\n"
        //                        + "}\n");
        buf.append(" }</script>\n" + "</head>\n" + "<body onload=\"init()\">\n");
        buf.append("<div id=\"params\">")
                .append(makeModifiableParameters(layer))
                .append("</div>\n");

        buf.append("<div id=\"map\"></div>\n" + "<div id=\"info\"></div>\n</body>\n" + "</html>");
        return buf.toString();
    }

    private static String makeModifiableParameters(TileLayer tl) {
        List<ParameterFilter> parameterFilters = tl.getParameterFilters();
        if (parameterFilters == null || parameterFilters.isEmpty()) {
            return "";
        }
        parameterFilters = new ArrayList<>(parameterFilters);
        Collections.sort(parameterFilters, (o1, o2) -> o1.getKey().compareTo(o2.getKey()));
        StringBuilder doc = new StringBuilder();
        doc.append("Modifiable Parameters:\n");
        doc.append("<table>\n");
        for (ParameterFilter pf : parameterFilters) {
            Assert.notNull(pf, "The parameter filter must be non null");
            String key = pf.getKey();
            String defaultValue = pf.getDefaultValue();
            List<String> legalValues = pf.getLegalValues();
            doc.append("<tr><td>").append(key.toUpperCase()).append(": ").append("</td><td>");
            String parameterId = key;
            if (pf instanceof StringParameterFilter) {
                Map<String, String> keysValues = makeParametersMap(defaultValue, legalValues);
                makePullDown(doc, parameterId, keysValues, defaultValue);
            } else if (pf instanceof RegexParameterFilter) {
                makeTextInput(doc, parameterId, 25);
            } else if (pf instanceof FloatParameterFilter) {
                FloatParameterFilter floatParam = (FloatParameterFilter) pf;
                if (floatParam.getValues().isEmpty()) {
                    // accepts any value
                    makeTextInput(doc, parameterId, 25);
                } else {
                    Map<String, String> keysValues = makeParametersMap(defaultValue, legalValues);
                    makePullDown(doc, parameterId, keysValues, defaultValue);
                }
            } else if ("org.geowebcache.filter.parameters.NaiveWMSDimensionFilter"
                    .equals(pf.getClass().getName())) {
                makeTextInput(doc, parameterId, 25);
            } else {
                // Unknown filter type
                if (legalValues == null) {
                    // Doesn't have a defined set of values, just provide a text field
                    makeTextInput(doc, parameterId, 25);
                } else {
                    // Does have a defined set of values, so provide a drop down
                    Map<String, String> keysValues = makeParametersMap(defaultValue, legalValues);
                    makePullDown(doc, parameterId, keysValues, defaultValue);
                }
            }
            doc.append("</td></tr>\n");
        }
        doc.append("</table>\n");
        return doc.toString();
    }

    private static Map<String, String> makeParametersMap(
            String defaultValue, List<String> legalValues) {
        Map<String, String> map = new TreeMap<>();
        for (String s : legalValues) {
            map.put(s, s);
        }
        map.put(defaultValue, defaultValue);
        return map;
    }

    private static void makePullDown(
            StringBuilder doc, String id, Map<String, String> keysValues, String defaultKey) {
        doc.append(
                "<select name=\"" + id + "\" onchange=\"window.setParam('" + id + "', value)\">\n");

        Iterator<Entry<String, String>> iter = keysValues.entrySet().iterator();

        while (iter.hasNext()) {
            Entry<String, String> entry = iter.next();
            final String key = entry.getKey();
            // equal, including both null
            if ((key == null && defaultKey == null) || (key != null && key.equals(defaultKey))) {
                doc.append(
                        "<option value=\""
                                + entry.getValue()
                                + "\" selected=\"selected\">"
                                + entry.getKey()
                                + "</option>\n");
            } else {
                doc.append(
                        "<option value=\""
                                + entry.getValue()
                                + "\">"
                                + entry.getKey()
                                + "</option>\n");
            }
        }

        doc.append("</select>\n");
    }

    private static void makeTextInput(StringBuilder doc, String id, int size) {
        doc.append(
                "<input name=\""
                        + id
                        + "\" type=\"text\" size=\""
                        + size
                        + "\" onblur=\"window.setParam('"
                        + id
                        + "', value)\" />\n");
    }

    private static boolean doubleEquals(double d1, double d2) {
        return doubleEquals(d1, d2, 0);
    }

    private static boolean doubleEquals(double d1, double d2, double buffer) {
        double diff = Math.abs(d1 - d2);
        return diff < (Math.ulp(d1) + Math.ulp(d2) + buffer);
    }
}
