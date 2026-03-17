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
 * @author Arne Kepp, OpenGeo, Copyright 2009
 * @author Sandro Salari, GeoSolutions S.A.S., Copyright 2017
 */
package cn.geoair.geoairteam.geowebcache.tianditu.service;

import static com.google.common.base.Preconditions.checkNotNull;


import cn.geoair.base.Gir;
import cn.geoair.base.data.model.annotation.GaModelField;
import cn.geoair.web.util.GirHttpServletHelper;
import cn.geoair.geoairteam.gwc.model.gwc.enums.GtcWmtsType;

import cn.geoair.geoairteam.geowebcache.tianditu.layer.TC_ArcGISCacheLayer;
import cn.geoair.geoairteam.geowebcache.tianditu.utils.GISHubUtil;
import cn.geoair.geoairteam.geowebcache.tianditu.utils.WMTSUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geowebcache.GeoWebCacheDispatcher;
import org.geowebcache.GeoWebCacheExtensions;
import org.geowebcache.config.legends.LegendInfo;
import org.geowebcache.config.legends.LegendInfoBuilder;
import org.geowebcache.config.meta.ServiceContact;
import org.geowebcache.config.meta.ServiceInformation;
import org.geowebcache.config.meta.ServiceProvider;
import org.geowebcache.conveyor.Conveyor.CacheResult;
import org.geowebcache.filter.parameters.ParameterFilter;
import org.geowebcache.grid.*;
import org.geowebcache.io.XMLBuilder;
import org.geowebcache.layer.TileJSONProvider;
import org.geowebcache.layer.TileLayer;
import org.geowebcache.layer.meta.LayerMetaInformation;
import org.geowebcache.layer.meta.MetadataURL;
import org.geowebcache.mime.ApplicationMime;
import org.geowebcache.service.wmts.WMTSExtension;
import org.geowebcache.service.wmts.WMTSService;
import org.geowebcache.stats.RuntimeStats;
import org.geowebcache.util.NullURLMangler;
import org.geowebcache.util.ServletUtils;
import org.geowebcache.util.URLMangler;

/**
 * 天地图的获取元数据封装
 */
public class GtcWMTSGetCapabilities {

    private static Log log = LogFactory.getLog(GtcWMTSGetCapabilities.class);

    public static final String SERVICE_PATH =
            "/" + GeoWebCacheDispatcher.TYPE_SERVICE + "/" + "wmts";
    public static final String REST_PATH = SERVICE_PATH + "/rest";
    private String baseUrl;

    private String geoairBaseUrl;

    private String restBaseUrl;

    TileLayer tileLayer;

    List<GridSet> capabilitiesGridsets;

    Set<GridSet> userGridsets;

    @GaModelField(text = "wmts的类型")
    GtcWmtsType gtcWmtsType;

    @GaModelField(text = "图层名称")
    String layerName;

    @GaModelField(text = "组名")
    String groupName;

    private final Collection<WMTSExtension> extensions;

    public GtcWMTSGetCapabilities(
            TileLayer tileLayer,
            List<GridSet> capabilitiesGridsets,
            GtcWmtsType gtcWmtsType,
            String layerName,
            String groupName) {
        this(tileLayer, capabilitiesGridsets, new NullURLMangler(), Collections.emptyList());
        this.layerName = layerName;
        this.gtcWmtsType = gtcWmtsType;
        this.groupName = groupName;
    }

    protected GtcWMTSGetCapabilities(
            TileLayer tileLayer,
            List<GridSet> capabilitiesGridsets,
            URLMangler urlMangler,
            Collection<WMTSExtension> extensions) {
        this.tileLayer = tileLayer;
        this.capabilitiesGridsets = capabilitiesGridsets;
        this.userGridsets = new HashSet<>(capabilitiesGridsets);
        HttpServletRequest servReq = GISHubUtil.getRequest();
        String servletPrefix = Gir.beans.getBean(ServletContext.class).getContextPath();
        this.baseUrl = ServletUtils.getServletBaseURL(servReq, servletPrefix);
        this.geoairBaseUrl = this.baseUrl + "/geoair";
        String context =
                ServletUtils.getServletContextPath(
                        servReq, new String[]{SERVICE_PATH, REST_PATH}, servletPrefix);
        String forcedBaseUrl =
                ServletUtils.stringFromMap(
                        servReq.getParameterMap(), servReq.getCharacterEncoding(), "base_url");
        if (forcedBaseUrl != null) {
            this.baseUrl = forcedBaseUrl;
        } else {
            this.baseUrl = urlMangler.buildURL(baseUrl, context, WMTSService.SERVICE_PATH);
        }
        this.restBaseUrl = urlMangler.buildURL(baseUrl, context, WMTSService.REST_PATH);

        this.extensions = extensions;
    }

    public void writeResponse() {
        final Charset encoding = StandardCharsets.UTF_8;
        byte[] data = generateGetCapabilities(encoding).getBytes(encoding);
        HttpServletResponse response = GirHttpServletHelper.getResponse();
        RuntimeStats stats = GeoWebCacheExtensions.bean(RuntimeStats.class);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/xml");
        response.setCharacterEncoding(encoding.name());
        response.setContentLength(data.length);
        response.setHeader("content-disposition", "inline;filename=tc-wmts-getcapabilities.xml");

        stats.log(data.length, CacheResult.OTHER);

        try (OutputStream os = response.getOutputStream()) {
            os.write(data);
            os.flush();
        } catch (IOException ioe) {
            log.debug("捕获 IOException" + ioe.getMessage());
        }
    }

    private String generateGetCapabilities(Charset encoding) {
        StringBuilder str = new StringBuilder();
        XMLBuilder xml = new XMLBuilder(str);

        try {
            xml.header("1.0", encoding);
            xml.indentElement("Capabilities");
            xml.attribute("xmlns", "http://www.opengis.net/wmts/1.0");
            xml.attribute("xmlns:ows", "http://www.opengis.net/ows/1.1");
            xml.attribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
            xml.attribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            xml.attribute("xmlns:gml", "http://www.opengis.net/gml");
            // allow extensions to register their names spaces
            for (WMTSExtension extension : extensions) {
                extension.registerNamespaces(xml);
            }
            StringBuilder schemasLocations = new StringBuilder("http://www.opengis.net/wmts/1.0 ");
            schemasLocations.append(
                    "http://schemas.opengis.net/wmts/1.0/wmtsGetCapabilities_response.xsd ");
            // allow extensions to register their schemas locations
            for (WMTSExtension extension : extensions) {
                for (String schemaLocation : extension.getSchemaLocations()) {
                    schemasLocations.append(schemaLocation).append(" ");
                }
            }
            schemasLocations.delete(schemasLocations.length() - 1, schemasLocations.length());
            // add schemas locations
            xml.attribute("xsi:schemaLocation", schemasLocations.toString());
            xml.attribute("version", "1.0.0");

            ServiceInformation serviceInformation = getServiceInformation();

            serviceIdentification(xml, serviceInformation);
            serviceProvider(xml, serviceInformation);
            operationsMetadata(xml);

            contents(xml);

            xml.indentElement("ServiceMetadataURL")
                    .attribute("xlink:href", WMTSUtils.getKvpServiceMetadataURL(baseUrl))
                    .endElement();

            xml.indentElement("ServiceMetadataURL")
                    .attribute("xlink:href", restBaseUrl + "/WMTSCapabilities.xml")
                    .endElement();

            xml.endElement("Capabilities");

            return str.toString();
        } catch (IOException e) {
            // Should not happen as StringBuilder doesn't throw
            throw new IllegalStateException(e);
        }
    }

    /**
     * Composes service information using information provided by extensions.
     */
    private ServiceInformation getServiceInformation() {
        ServiceInformation servInfo = new ServiceInformation();
        servInfo.setTitle("GeoAir在线地图服务");
        servInfo.setDescription("GeoAir基于OGC标准的WMTS在线地图服务");
        servInfo.setProviderName("geoairTeam");

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setProviderName("geoairTeam");
        serviceProvider.setProviderSite("http://www.geoair.com/");
        ServiceContact serviceContact = new ServiceContact();
        serviceContact.setPhoneNumber("+86 110112119");
        serviceContact.setAddressAdministrativeArea("中国");
        serviceContact.setAddressCity("北京");
        serviceContact.setAddressType("Work");
        serviceContact.setAddressPostalCode("101399");
        serviceContact.setAddressEmail("sales@71ditu.com");
        serviceContact.setAddressStreet("天安门");
        serviceProvider.setServiceContact(serviceContact);
        servInfo.setServiceProvider(serviceProvider);
        return servInfo;
    }

    private void serviceIdentification(XMLBuilder xml, ServiceInformation servInfo)
            throws IOException {

        xml.indentElement("ows:ServiceIdentification");

        if (servInfo != null) {
            appendTag(xml, "ows:Title", servInfo.getTitle(), "geoairTeam");
            appendTag(xml, "ows:Abstract", servInfo.getDescription(), null);

            if (servInfo != null && servInfo.getKeywords() != null) {
                xml.indentElement("ows:Keywords");
                Iterator<String> keywordIter = servInfo.getKeywords().iterator();
                while (keywordIter.hasNext()) {
                    appendTag(xml, "ows:Keyword", keywordIter.next(), null);
                }
                xml.endElement();
            }
        } else {
            xml.simpleElement("ows:Title", "geoairTeam", true);
        }
        xml.simpleElement("ows:ServiceType", "OGC WMTS", true);
        xml.simpleElement("ows:ServiceTypeVersion", "1.0.0", true);

        if (servInfo != null) {
            appendTag(xml, "ows:Fees", servInfo.getFees(), null);
            appendTag(xml, "ows:AccessConstraints", servInfo.getAccessConstraints(), null);
        }

        xml.endElement("ows:ServiceIdentification");
    }

    private void serviceProvider(XMLBuilder xml, ServiceInformation servInfo) throws IOException {
        ServiceProvider servProv = null;
        if (servInfo != null) {
            servProv = servInfo.getServiceProvider();
        }
        xml.indentElement("ows:ServiceProvider");

        if (servProv != null) {
            appendTag(xml, "ows:ProviderName", servProv.getProviderName(), null);

            if (servProv.getProviderSite() != null) {
                xml.indentElement("ows:ProviderSite")
                        .attribute("xlink:href", servProv.getProviderSite())
                        .endElement();
            }

            ServiceContact servCont = servProv.getServiceContact();
            if (servCont != null) {
                xml.indentElement("ows:ServiceContact");
                appendTag(xml, "ows:IndividualName", servCont.getIndividualName(), null);
                appendTag(xml, "ows:PositionName", servCont.getPositionName(), null);
                xml.indentElement("ows:ContactInfo");

                if (servCont.getPhoneNumber() != null || servCont.getFaxNumber() != null) {
                    xml.indentElement("ows:Phone");
                    appendTag(xml, "ows:Voice", servCont.getPhoneNumber(), null);
                    appendTag(xml, "ows:Facsimile", servCont.getFaxNumber(), null);
                    xml.endElement();
                }

                xml.indentElement("ows:Address");
                appendTag(xml, "ows:DeliveryPoint", servCont.getAddressStreet(), null);
                appendTag(xml, "ows:City", servCont.getAddressCity(), null);
                appendTag(
                        xml,
                        "ows:AdministrativeArea",
                        servCont.getAddressAdministrativeArea(),
                        null);
                appendTag(xml, "ows:PostalCode", servCont.getAddressPostalCode(), null);
                appendTag(xml, "ows:Country", servCont.getAddressCountry(), null);
                appendTag(xml, "ows:ElectronicMailAddress", servCont.getAddressEmail(), null);
                xml.endElement("ows:Address");

                xml.endElement();
                xml.endElement();
            }
        } else {
            appendTag(xml, "ows:ProviderName", baseUrl, null);
            xml.indentElement("ows:ProviderSite").attribute("xlink:href", baseUrl).endElement();
            xml.indentElement("ows:ServiceContact");
            appendTag(xml, "ows:IndividualName", "GeoWebCache User", null);
            xml.endElement();
        }

        xml.endElement("ows:ServiceProvider");
    }

    private void operationsMetadata(XMLBuilder xml) throws IOException {
        xml.indentElement("ows:OperationsMetadata");
        //        operation(xml, "GetCapabilities", baseUrl);
        //        operation(xml, "GetTile", baseUrl);   //  todo 使用新地址  geoair的
        String geoairWmtsUrl =
                this.geoairBaseUrl + "/" + gtcWmtsType.getSuffix() + "/" + layerName + "/wmts";
        operation(xml, "GetCapabilities", geoairWmtsUrl);
        operation(xml, "GetTile", geoairWmtsUrl);
        operation(xml, "GetFeatureInfo", baseUrl);
        // allow extension to inject their own metadata
        for (WMTSExtension extension : extensions) {
            List<WMTSExtension.OperationMetadata> operationsMetaData =
                    extension.getExtraOperationsMetadata();
            if (operationsMetaData != null) {
                for (WMTSExtension.OperationMetadata operationMetadata : operationsMetaData) {
                    operation(
                            xml,
                            operationMetadata.getName(),
                            operationMetadata.getBaseUrl() == null
                                    ? baseUrl
                                    : operationMetadata.getBaseUrl());
                }
            }
            extension.encodedOperationsMetadata(xml);
        }
        xml.endElement("ows:OperationsMetadata");
    }

    private void operation(XMLBuilder xml, String operationName, String baseUrl)
            throws IOException {
        xml.indentElement("ows:Operation").attribute("name", operationName);
        xml.indentElement("ows:DCP");
        xml.indentElement("ows:HTTP");
        if (baseUrl.contains("?")) {
            xml.indentElement("ows:Get").attribute("xlink:href", baseUrl + "&");
        } else {
            xml.indentElement("ows:Get").attribute("xlink:href", baseUrl + "?");
        }
        xml.indentElement("ows:Constraint").attribute("name", "GetEncoding");
        xml.indentElement("ows:AllowedValues");
        xml.simpleElement("ows:Value", "KVP", true);
        xml.endElement();
        xml.endElement();
        xml.endElement();
        xml.endElement();
        xml.endElement();
        xml.endElement("ows:Operation");
    }

    private void contents(XMLBuilder xml) throws IOException {
        xml.indentElement("Contents");
        layer(xml, tileLayer, baseUrl, userGridsets);
        // 只转储实际使用的网格集，因为 OGC TMS 规范引入了许多默认的
        //        List<GridSet> capabilitiesGridsets = new ArrayList<>(gsb.getGridSets());
        //        capabilitiesGridsets.retainAll(usedGridsets);
        // 排序使查找网格集变得更容易，因为级别不重复网格集名称
        capabilitiesGridsets.sort(Comparator.comparing(GridSet::getName));
        for (GridSet gset : capabilitiesGridsets) {
            tileMatrixSet(xml, gset);
        }

        xml.endElement("Contents");
    }

    private void layer(XMLBuilder xml, TileLayer layer, String baseurl, Set<GridSet> usedGridsets)
            throws IOException {
        xml.indentElement("Layer");
        LayerMetaInformation layerMeta = layer.getMetaInformation();
        if (layerMeta == null) {
            //            appendTag(xml, "ows:Title", layer.getName(), null);
            appendTag(xml, "ows:Title", layerName, null); // todozfj 修改展示名称
        } else {
            appendTag(xml, "ows:Title", layerMeta.getTitle(), null);
            appendTag(xml, "ows:Abstract", layerMeta.getDescription(), null);
        }

        layerWGS84BoundingBox(xml, layer, usedGridsets);

        appendTag(xml, "ows:Identifier", layerName, null);

        if (layer.getMetadataURLs() != null) {
            for (MetadataURL metadataURL : layer.getMetadataURLs()) {
                xml.indentElement("MetadataURL");
                xml.attribute("type", metadataURL.getType());
                xml.simpleElement("Format", metadataURL.getFormat(), true);
                xml.indentElement("OnlineResource")
                        .attribute("xmlns:xlink", "http://www.geoair.com/#/")
                        .attribute("xlink:type", "simple")
                        .attribute("xlink:href", metadataURL.getUrl().toString())
                        .endElement();
                xml.endElement();
            }
        }

        // We need the filters for styles and dimensions
        List<ParameterFilter> filters = layer.getParameterFilters();

        layerStyles(xml, layer, filters);

        layerFormats(xml, layer);

        layerInfoFormats(xml, layer);

        if (filters != null) {
            layerDimensions(xml, layer, filters);
        }

        layerGridSubSets(xml, layer, usedGridsets);

        layerResourceUrls(xml, layer, filters, restBaseUrl);

        // allow extensions to contribute extra metadata to this layer
        for (WMTSExtension extension : extensions) {
            extension.encodeLayer(xml, layer);
        }

        xml.endElement("Layer");
    }

    private void layerWGS84BoundingBox(XMLBuilder xml, TileLayer layer, Set<GridSet> usedGridsets)
            throws IOException {
        GridSubset subset = layer.getGridSubsetForSRS(SRS.getEPSG4326());
        if (subset != null) {
            double[] coords = subset.getOriginalExtent().getCoords();
            xml.indentElement("ows:WGS84BoundingBox");
            xml.simpleElement("ows:LowerCorner", coords[0] + " " + coords[1], true);
            xml.simpleElement("ows:UpperCorner", coords[2] + " " + coords[3], true);
            xml.endElement("ows:WGS84BoundingBox");
            return;
        }
        subset = layer.getGridSubsetForSRS(SRS.getEPSG900913());
        if (subset != null) {
            double[] coords = subset.getOriginalExtent().getCoords();
            double originShift = 2 * Math.PI * 6378137 / 2.0;
            double mx = coords[0];
            double my = coords[1];
            double lon = (mx / originShift) * 180.0;
            double lat = (my / originShift) * 180.0;

            lat = 180 / Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180.0)) - Math.PI / 2.0);
            xml.indentElement("ows:WGS84BoundingBox");
            xml.simpleElement("ows:LowerCorner", lon + " " + lat, true);

            mx = coords[2];
            my = coords[3];
            lon = (mx / originShift) * 180.0;
            lat = (my / originShift) * 180.0;

            lat = 180 / Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180.0)) - Math.PI / 2.0);

            xml.simpleElement("ows:UpperCorner", lon + " " + lat, true);
            xml.endElement("ows:WGS84BoundingBox");
        }
        //        if (GutilObject.isNotEmpty(groupName) &&
        // gtcWmtsType.equals(GtcWmtsType.groupLayer)) {
        //            GroupMeta layerGroup = TC_ArcGISCacheLayerGroup.getLayerGroup(groupName);
        //            GridSubset gridSubset = layerGroup.getGridSubset();
        //            BoundingBox gridSetBounds = gridSubset.getGridSetBounds();
        //            xml.indentElement("ows:WGS84BoundingBox");
        //            xml.simpleElement(
        //                    "ows:LowerCorner",
        //                    gridSetBounds.getMinX() + " " + gridSetBounds.getMinY(),
        //                    true);
        //            xml.simpleElement(
        //                    "ows:UpperCorner",
        //                    gridSetBounds.getMaxX() + " " + gridSetBounds.getMaxY(),
        //                    true);
        //            xml.endElement("ows:WGS84BoundingBox");
        //            return;
        //        }

        if (layer instanceof TC_ArcGISCacheLayer) {
            TC_ArcGISCacheLayer tc_arcGISCacheLayer = (TC_ArcGISCacheLayer) layer;
            // 手动设置 中心点哦
            BoundingBox bounds = tc_arcGISCacheLayer.getLayerBounds();
            xml.indentElement("ows:WGS84BoundingBox");
            xml.simpleElement("ows:LowerCorner", bounds.getMinX() + " " + bounds.getMinY(), true);
            xml.simpleElement("ows:UpperCorner", bounds.getMaxX() + " " + bounds.getMaxY(), true);
            xml.endElement("ows:WGS84BoundingBox");
            return;
        }
    }

    /**
     * Helper method that get layer legends info by merging deprecated legends info objects with the
     * new ones.
     */
    private Map<String, LegendInfo> getLegendsInfo(TileLayer layer) {
        Map<String, LegendInfo> legendsInfo = new HashMap<>();
        for (Map.Entry<String, LegendInfo> entry : layer.getLayerLegendsInfo().entrySet()) {
            // convert deprecated model to new model
            String styleName = entry.getKey();
            LegendInfo legend = entry.getValue();
            legendsInfo.put(
                    styleName,
                    new LegendInfoBuilder()
                            .withWidth(legend.getWidth())
                            .withHeight(legend.getHeight())
                            .withFormat(legend.getFormat())
                            .withCompleteUrl(legend.getLegendUrl())
                            .withStyleName(styleName)
                            .build());
        }
        // add the new legend info model objects
        legendsInfo.putAll(layer.getLayerLegendsInfo());
        return legendsInfo;
    }

    private void layerStyles(XMLBuilder xml, TileLayer layer, List<ParameterFilter> filters)
            throws IOException {
        String defStyle = layer.getStyles();
        Map<String, LegendInfo> legendsInfo = getLegendsInfo(layer);
        if (filters == null) {
            xml.indentElement("Style");
            xml.attribute("isDefault", "true");
            if (defStyle == null) {
                xml.simpleElement("ows:Identifier", "", true);
            } else {
                xml.simpleElement("ows:Identifier", TileLayer.encodeDimensionValue(defStyle), true);
            }
            encodeStyleLegendGraphic(xml, legendsInfo.get(defStyle));
            xml.endElement("Style");
        } else {
            ParameterFilter stylesFilter = null;
            Iterator<ParameterFilter> iter = filters.iterator();
            while (stylesFilter == null && iter.hasNext()) {
                ParameterFilter filter = iter.next();
                if (filter.getKey().equalsIgnoreCase("STYLES")) {
                    stylesFilter = filter;
                }
            }

            List<String> legalStyles = null;
            if (stylesFilter != null) legalStyles = stylesFilter.getLegalValues();

            if (legalStyles != null && !legalStyles.isEmpty()) {
                // There's a style filter listing at least one value
                String defVal = stylesFilter.getDefaultValue();
                if (defVal == null) {
                    if (defStyle != null) {
                        defVal = defStyle;
                    } else {
                        defVal = "";
                    }
                }

                for (String value : legalStyles) {
                    xml.indentElement("Style");
                    if (value.equals(defVal)) {
                        xml.attribute("isDefault", "true");
                    }
                    xml.simpleElement(
                            "ows:Identifier", TileLayer.encodeDimensionValue(value), true);
                    encodeStyleLegendGraphic(xml, legendsInfo.get(value));
                    xml.endElement();
                }
            } else {
                // Couldn't get a list of styles so just say there's a default.
                xml.indentElement("Style");
                xml.attribute("isDefault", "true");
                xml.simpleElement("ows:Identifier", "", true);
                if (defStyle != null) {
                    encodeStyleLegendGraphic(xml, legendsInfo.get(defStyle));
                }
                xml.endElement();
            }
        }
    }

    /**
     * XML encodes the provided legend information. If the provided information legend is NULL
     * nothing is done.
     */
    private void encodeStyleLegendGraphic(XMLBuilder xml, LegendInfo legendInfo)
            throws IOException {
        if (legendInfo == null) {
            // nothing to do
            return;
        }
        xml.indentElement("LegendURL");
        // validate mandatory attributes
        checkNotNull(legendInfo.getFormat(), "Legend format is mandatory in WMTS.");
        checkNotNull(legendInfo.getLegendUrl(), "Legend URL is mandatory in WMTS.");
        // add mandatory attributes
        xml.attribute("format", legendInfo.getFormat());
        xml.attribute("xlink:href", legendInfo.getLegendUrl());
        // add optional attributes
        if (legendInfo.getWidth() != null) {
            xml.attribute("width", String.valueOf(legendInfo.getWidth()));
        }
        if (legendInfo.getHeight() != null) {
            xml.attribute("height", String.valueOf(legendInfo.getHeight()));
        }
        if (legendInfo.getMinScale() != null) {
            xml.attribute("minScaleDenominator", String.valueOf(legendInfo.getMinScale()));
        }
        if (legendInfo.getMaxScale() != null) {
            xml.attribute("maxScaleDenominator", String.valueOf(legendInfo.getMaxScale()));
        }
        xml.endElement("LegendURL");
    }

    private void layerFormats(XMLBuilder xml, TileLayer layer) throws IOException {
        List<String> mimeFormats = WMTSUtils.getLayerFormats(layer);
        for (String format : mimeFormats) {
            xml.simpleElement("Format", format, true);
        }
    }

    private void layerInfoFormats(XMLBuilder xml, TileLayer layer) throws IOException {
        if (layer.isQueryable()) {
            List<String> infoFormats = WMTSUtils.getInfoFormats(layer);
            for (String format : infoFormats) {
                xml.simpleElement("InfoFormat", format, true);
            }
        }
    }

    private void layerDimensions(XMLBuilder xml, TileLayer layer, List<ParameterFilter> filters)
            throws IOException {
        List<ParameterFilter> layerDimensions = WMTSUtils.getLayerDimensions(filters);
        for (ParameterFilter dimension : layerDimensions) {
            dimensionDescription(xml, dimension, dimension.getLegalValues());
        }
    }

    private void dimensionDescription(XMLBuilder xml, ParameterFilter filter, List<String> values)
            throws IOException {
        xml.indentElement("Dimension");
        xml.simpleElement("ows:Identifier", filter.getKey(), false);
        String defaultStr = TileLayer.encodeDimensionValue(filter.getDefaultValue());
        xml.simpleElement("Default", defaultStr, false);

        Iterator<String> iter = values.iterator();
        while (iter.hasNext()) {
            String value = TileLayer.encodeDimensionValue(iter.next());
            xml.simpleElement("Value", value, false);
        }
        xml.endElement("Dimension");
    }

    String splitTileMatrixSet(String oldName) {
        if (oldName.contains(":")) {
            String[] split = oldName.split(":");
            oldName = split[split.length - 1];
        }
        return oldName;
    }

    private void layerGridSubSets(XMLBuilder xml, TileLayer layer, Set<GridSet> usedGridSets)
            throws IOException {
        //        if (GutilObject.isNotEmpty(groupName) &&
        // gtcWmtsType.equals(GtcWmtsType.groupLayer)) {
        //            GroupMeta layerGroup = TC_ArcGISCacheLayerGroup.getLayerGroup(groupName);
        //            GridSubset gridSubset = layerGroup.getGridSubset();
        //            xml.indentElement("TileMatrixSetLink");
        //            xml.simpleElement("TileMatrixSet", "c", true);
        //            if (!gridSubset.fullGridSetCoverage()) {
        //                String[] levelNames = gridSubset.getGridNames();
        //                long[][] wmtsLimits = gridSubset.getWMTSCoverages();
        //
        //                xml.indentElement("TileMatrixSetLimits");
        //                for (int i = 0; i < levelNames.length; i++) {
        //                    xml.indentElement("TileMatrixLimits");
        //                    xml.simpleElement("TileMatrix", splitTileMatrixSet(levelNames[i]),
        // true);
        //                    xml.simpleElement("MinTileRow", Long.toString(wmtsLimits[i][1]),
        // true);
        //                    xml.simpleElement("MaxTileRow", Long.toString(wmtsLimits[i][3]),
        // true);
        //                    xml.simpleElement("MinTileCol", Long.toString(wmtsLimits[i][0]),
        // true);
        //                    xml.simpleElement("MaxTileCol", Long.toString(wmtsLimits[i][2]),
        // true);
        //                    xml.endElement();
        //                }
        //                xml.endElement();
        //            }
        //            xml.endElement("TileMatrixSetLink");
        //            return;
        //        }

        for (String gridSetId : layer.getGridSubsets()) {
            GridSubset gridSubset = layer.getGridSubset(gridSetId);
            //            String name = splitTileMatrixSet(gridSubset.getName());
            xml.indentElement("TileMatrixSetLink");
            // todo  by zhangjun 2021-10-19 因为这个图层组是 动态的 ，这个 tileMatrixSet 没有办法 动态，索性就全局统一为 与天地图一致
            // 叫做 c
            //            xml.simpleElement("TileMatrixSet", gridSubset.getName(), true);
            xml.simpleElement("TileMatrixSet", "c", true);
            usedGridSets.add(gridSubset.getGridSet());

            if (!gridSubset.fullGridSetCoverage()) {
                String[] levelNames = gridSubset.getGridNames();
                long[][] wmtsLimits = gridSubset.getWMTSCoverages();

                xml.indentElement("TileMatrixSetLimits");
                for (int i = 0; i < levelNames.length; i++) {
                    xml.indentElement("TileMatrixLimits");
                    xml.simpleElement("TileMatrix", splitTileMatrixSet(levelNames[i]), true);
                    xml.simpleElement("MinTileRow", Long.toString(wmtsLimits[i][1]), true);
                    xml.simpleElement("MaxTileRow", Long.toString(wmtsLimits[i][3]), true);
                    xml.simpleElement("MinTileCol", Long.toString(wmtsLimits[i][0]), true);
                    xml.simpleElement("MaxTileCol", Long.toString(wmtsLimits[i][2]), true);
                    xml.endElement();
                }
                xml.endElement();
            }
            xml.endElement("TileMatrixSetLink");
        }
    }

    /**
     * For each layer discovers the available image formats, feature info formats and dimensions and
     * produce the necessary <ResourceURL> elements.
     */
    private void layerResourceUrls(
            XMLBuilder xml, TileLayer layer, List<ParameterFilter> filters, String baseurl)
            throws IOException {
        String baseTemplate = baseurl + "/" + layer.getName();
        String commonTemplate =
                baseTemplate + "/{style}/{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}";
        String commonDimensions = "";
        // Extracts layer dimension
        List<ParameterFilter> layerDimensions = WMTSUtils.getLayerDimensions(filters);
        if (!layerDimensions.isEmpty()) {
            commonDimensions =
                    "&"
                            + layerDimensions
                            .stream()
                            .map(d -> d.getKey() + "={" + d.getKey() + "}")
                            .collect(Collectors.joining("&"));
        }
        // Extracts image formats   byzfj 这个只有一种类型
        List<String> mimeFormats = WMTSUtils.getLayerFormats(layer);
        for (String format : mimeFormats) {
            String template =
                    this.geoairBaseUrl + "/" + gtcWmtsType.getSuffix() + "/" + layerName + "/wmts";
            template =
                    template
                            + "?layer="
                            + layerName
                            + "&tilematrixset=c&Service=WMTS&Request=GetTile&Version=1.0.0&Format=image/png&TileMatrix={TileMatrix}&TileCol={TileCol}&TileRow={TileRow}";
            template = template + commonDimensions;
            layerResourceUrlsGen(xml, format, "tile", template);
        }
        // Extracts feature info formats
        List<String> infoFormats = WMTSUtils.getInfoFormats(layer);
        for (String format : infoFormats) {
            String template = commonTemplate + "/{J}/{I}?format=" + format + commonDimensions;
            layerResourceUrlsGen(xml, format, "FeatureInfo", template);
        }
        if (layer instanceof TileJSONProvider) {
            List<String> formatExtensions = WMTSUtils.getLayerFormatsExtensions(layer);
            TileJSONProvider provider = (TileJSONProvider) layer;
            String outputFormat = ApplicationMime.json.getFormat();
            if (provider.supportsTileJSON()) {
                for (String tileJsonFormat : formatExtensions) {
                    String template =
                            baseTemplate
                                    + "/{style}/tilejson/"
                                    + tileJsonFormat
                                    + "?format="
                                    + outputFormat;
                    layerResourceUrlsGen(xml, outputFormat, "TileJSON", template);
                }
            }
        }
    }

    //    private void layerResourceUrls(
    //            XMLBuilder xml, TileLayer layer, List<ParameterFilter> filters, String baseurl)
    //            throws IOException {
    //        String baseTemplate = baseurl + "/" + layer.getName();
    //        String commonTemplate =
    //                baseTemplate + "/{style}/{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}";
    //        String commonDimensions = "";
    //        // Extracts layer dimension
    //        List<ParameterFilter> layerDimensions = WMTSUtils.getLayerDimensions(filters);
    //        if (!layerDimensions.isEmpty()) {
    //            commonDimensions =
    //                    "&"
    //                            + layerDimensions
    //                            .stream()
    //                            .map(d -> d.getKey() + "={" + d.getKey() + "}")
    //                            .collect(Collectors.joining("&"));
    //        }
    //        // Extracts image formats
    //        List<String> mimeFormats = WMTSUtils.getLayerFormats(layer);
    //        for (String format : mimeFormats) {
    //            String template = commonTemplate + "?format=" + format + commonDimensions;
    //            layerResourceUrlsGen(xml, format, "tile", template);
    //        }
    //        // Extracts feature info formats
    //        List<String> infoFormats = WMTSUtils.getInfoFormats(layer);
    //        for (String format : infoFormats) {
    //            String template = commonTemplate + "/{J}/{I}?format=" + format + commonDimensions;
    //            layerResourceUrlsGen(xml, format, "FeatureInfo", template);
    //        }
    //        if (layer instanceof TileJSONProvider) {
    //            List<String> formatExtensions = WMTSUtils.getLayerFormatsExtensions(layer);
    //            TileJSONProvider provider = (TileJSONProvider) layer;
    //            String outputFormat = ApplicationMime.json.getFormat();
    //            if (provider.supportsTileJSON()) {
    //                for (String tileJsonFormat : formatExtensions) {
    //                    String template =
    //                            baseTemplate
    //                                    + "/{style}/tilejson/"
    //                                    + tileJsonFormat
    //                                    + "?format="
    //                                    + outputFormat;
    //                    layerResourceUrlsGen(xml, outputFormat, "TileJSON", template);
    //                }
    //            }
    //        }
    //    }

    /**
     * Generate the <ResourceURL> element into XML.
     */
    private void layerResourceUrlsGen(XMLBuilder xml, String format, String type, String template)
            throws IOException {
        xml.indentElement("ResourceURL");
        xml.attribute("format", format);
        xml.attribute("resourceType", type);
        xml.attribute("template", template);
        xml.endElement("ResourceURL");
    }

    private void tileMatrixSet(XMLBuilder xml, GridSet gridSet) throws IOException {
        xml.indentElement("TileMatrixSet");
        // todo  by zhangjun 2021-10-19 因为这个图层组是 动态的 ，这个 tileMatrixSet 没有办法 动态，索性就全局统一为 与天地图一致 叫做 c
        //        xml.simpleElement("ows:Identifier", gridSet.getName(), true);

        xml.simpleElement("ows:Identifier", "c", true);
        // 如果以下内容不够好，请与我们联系，我们会尽力修复它:)
        xml.simpleElement(
                "ows:SupportedCRS", "urn:ogc:def:crs:EPSG::" + gridSet.getSrs().getNumber(), true);
        for (int i = 0; i < gridSet.getNumLevels(); i++) {
            double[] tlCoordinates = gridSet.getOrderedTopLeftCorner(i);
            tileMatrix(
                    xml,
                    gridSet.getGrid(i),
                    tlCoordinates,
                    gridSet.getTileWidth(),
                    gridSet.getTileHeight(),
                    gridSet.isScaleWarning());
        }
        xml.endElement("TileMatrixSet");
    }

    private void tileMatrix(
            XMLBuilder xml,
            Grid grid,
            double[] tlCoordinates,
            int tileWidth,
            int tileHeight,
            boolean scaleWarning)
            throws IOException {
        xml.indentElement("TileMatrix");
        if (scaleWarning) {
            xml.simpleElement("ows:Abstract", "网格没有明确定义，因此比例尺假设为每个地图单位 1m.", true);
        }
        xml.simpleElement("ows:Identifier", splitTileMatrixSet(grid.getName()), true);
        xml.simpleElement("ScaleDenominator", Double.toString(grid.getScaleDenominator()), true);
        xml.indentElement("TopLeftCorner")
                .text(Double.toString(tlCoordinates[0]))
                .text(" ")
                .text(Double.toString(tlCoordinates[1]))
                .endElement();
        xml.simpleElement("TileWidth", Integer.toString(tileWidth), true);
        xml.simpleElement("TileHeight", Integer.toString(tileHeight), true);
        xml.simpleElement("MatrixWidth", Long.toString(grid.getNumTilesWide()), true);
        xml.simpleElement("MatrixHeight", Long.toString(grid.getNumTilesHigh()), true);
        xml.endElement("TileMatrix");
    }

    private void appendTag(XMLBuilder xml, String tagName, String value, String defaultValue)
            throws IOException {
        if (value == null) {
            if (defaultValue == null) return;
            else value = defaultValue;
        }
        xml.simpleElement(tagName, value, true);
    }
}
