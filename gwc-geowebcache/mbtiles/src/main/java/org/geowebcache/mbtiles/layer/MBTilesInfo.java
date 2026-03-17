/**
 * 该程序是免费软件：您可以根据自由软件基金会发布的 GNU 宽松通用公共许可证 （许可证的第 3 版或（由您选择）任何更高版本）的条款重新分发和/或修改它.
 *
 * <p>分发此程序是希望它有用，但不作任何保证； 甚至没有对适销性或针对特定目的的适用性的暗示保证。 有关更多详细信息，请参阅 GNU 通用公共许可证.
 *
 * <p>您应该已经收到一份 GNU 宽松通用公共许可证以及该程序。如果没有，请参阅 <http:www.gnu.orglicenses>。
 *
 * @author geoair，版权所有 2021
 */
package org.geowebcache.mbtiles.layer;

import static org.geotools.mbtiles.MBTilesFile.SPHERICAL_MERCATOR;
import static org.geotools.mbtiles.MBTilesFile.WORLD_ENVELOPE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.mbtiles.MBTilesFile;
import org.geotools.mbtiles.MBTilesMetadata;
import org.geotools.referencing.CRS;
import org.geowebcache.grid.BoundingBox;
import org.geowebcache.layer.meta.TileJSON;
import org.geowebcache.layer.meta.VectorLayerMetadata;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

/** 存储基本 MBTiles 缓存信息的信息对象 */
public class MBTilesInfo {

    private static Log log = LogFactory.getLog(MBTilesInfo.class);

    private static final CoordinateReferenceSystem WGS_84;

    private static final BoundingBox WORLD_MERCATOR_WGS_84_BOUNDS;

    static {
        try {
            WGS_84 = CRS.decode("EPSG:4326", true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        WORLD_MERCATOR_WGS_84_BOUNDS = new BoundingBox(-180.0, -85, 180, 85.0);
    }

    private MBTilesMetadata metadata;

    private BoundingBox bounds;

    private BoundingBox wgs84Bounds;

    public MBTilesMetadata.t_format getFormat() {
        return format;
    }

    private final MBTilesMetadata.t_format format;

    private int minZoom;

    private int maxZoom;

    private String metadataName;

    public int getMinZoom() {
        return minZoom;
    }

    public int getMaxZoom() {
        return maxZoom;
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public String getMetadataName() {
        return metadataName;
    }

    public MBTilesInfo(MBTilesFile file) throws IOException {
        metadata = file.loadMetaData();
        metadataName = metadata.getName();
        format = metadata.getFormat();
        minZoom = metadata.getMinZoom();
        maxZoom = metadata.getMaxZoom();

        Envelope env = metadata.getBounds();
        ReferencedEnvelope envelope = null;
        if (env != null) {
            try {
                wgs84Bounds = getBBoxFromEnvelope(env);
                envelope =
                        ReferencedEnvelope.create(env, WGS_84).transform(SPHERICAL_MERCATOR, true);
            } catch (TransformException | FactoryException e) {
                throw new IllegalArgumentException(
                        "转换边界时发生异常: " + file.getFile().getAbsolutePath(), e);
            }
        } else {
            if (log.isWarnEnabled()) {
                log.warn(
                        "如果 MBTile 有一个空信封: "
                                + file.getFile().getAbsolutePath()
                                + ".使用完整的 GridSet 范围  ");
            }
            envelope = WORLD_ENVELOPE;
            wgs84Bounds = WORLD_MERCATOR_WGS_84_BOUNDS;
        }
        bounds = getBBoxFromEnvelope(envelope);
    }

    private BoundingBox getBBoxFromEnvelope(Envelope envelope) {
        BoundingBox bbox = null;
        if (envelope != null) {
            bbox =
                    new BoundingBox(
                            envelope.getMinimum(0),
                            envelope.getMinimum(1),
                            envelope.getMaximum(0),
                            envelope.getMaximum(1));
        }
        return bbox;
    }

    public void decorateTileJSON(TileJSON tileJSON) {
        tileJSON.setMinZoom(minZoom);
        tileJSON.setMaxZoom(maxZoom);
        tileJSON.setBounds(
                new double[] {
                    wgs84Bounds.getMinX(),
                    wgs84Bounds.getMinY(),
                    wgs84Bounds.getMaxX(),
                    wgs84Bounds.getMaxY()
                });
        if (metadata != null) {
            String description = metadata.getDescription();
            if (description != null) {
                tileJSON.setDescription(description);
            }
            tileJSON.setCenter(metadata.getCenter());
            tileJSON.setAttribution(metadata.getAttribution());
            String json = metadata.getJson();

            int index = -1;
            if (json != null && ((index = json.indexOf("[")) > 0)) {
                // skip the "vector_layers initial part and go straight to the array
                json = json.substring(index, json.length() - 1).trim();
                ObjectMapper mapper = new ObjectMapper();
                List<VectorLayerMetadata> layers = null;
                try {
                    layers =
                            mapper.readValue(
                                    json, new TypeReference<List<VectorLayerMetadata>>() {});
                } catch (JsonProcessingException e) {
                    throw new IllegalArgumentException("解析图层元数据时发生异常. " + e);
                }
                tileJSON.setLayers(layers);
            }
        }
    }
}
