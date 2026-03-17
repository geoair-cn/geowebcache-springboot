/**
 * 该程序是免费软件：您可以根据自由软件基金会发布的 GNU 宽松通用公共许可证 （许可证的第 3 版或（由您选择）任何更高版本）的条款重新分发和/或修改它.
 *
 * <p>分发此程序是希望它有用，但不作任何保证； 甚至没有对适销性或针对特定目的的适用性的暗示保证。 有关更多详细信息，请参阅 GNU 通用公共许可证.
 *
 * <p>您应该已经收到一份 GNU 宽松通用公共许可证以及该程序。如果没有，请参阅 <http:www.gnu.orglicenses>。
 *
 * @author geoair，版权所有 2021
 */
package org.geowebcache.s3;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Strings;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.filter.parameters.ParametersUtils;
import org.geowebcache.layer.TileLayer;
import org.geowebcache.layer.TileLayerDispatcher;
import org.geowebcache.mime.MimeException;
import org.geowebcache.mime.MimeType;
import org.geowebcache.storage.TileObject;
import org.geowebcache.storage.TileRange;

final class TMSKeyBuilder {

    private static final String DELIMITER = "/";

    public static final String LAYER_METADATA_OBJECT_NAME = "metadata.properties";
    public static final String PARAMETERS_METADATA_OBJECT_PREFIX = "parameters-";
    public static final String PARAMETERS_METADATA_OBJECT_SUFFIX = ".properties";

    private String prefix;

    private TileLayerDispatcher layers;

    public TMSKeyBuilder(final String prefix, TileLayerDispatcher layers) {
        this.prefix = prefix;
        this.layers = layers;
    }

    public String layerId(String layerName) {
        TileLayer layer;
        try {
            layer = layers.getTileLayer(layerName);
        } catch (GeoWebCacheException e) {
            throw new RuntimeException(e);
        }
        return layer.getId();
    }

    public Set<String> layerGridsets(String layerName) {
        TileLayer layer;
        try {
            layer = layers.getTileLayer(layerName);
        } catch (GeoWebCacheException e) {
            throw new RuntimeException(e);
        }
        return layer.getGridSubsets();
    }

    public Set<String> layerFormats(String layerName) {
        TileLayer layer;
        try {
            layer = layers.getTileLayer(layerName);
        } catch (GeoWebCacheException e) {
            throw new RuntimeException(e);
        }
        return layer.getMimeTypes()
                .stream()
                .map(MimeType::getFileExtension)
                .collect(Collectors.toSet());
    }

    public String forTile(TileObject obj) {
        checkNotNull(obj.getLayerName());
        checkNotNull(obj.getGridSetId());
        checkNotNull(obj.getBlobFormat());
        checkNotNull(obj.getXYZ());

        String layer = layerId(obj.getLayerName());
        String gridset = obj.getGridSetId();
        String shortFormat;
        String parametersId = obj.getParametersId();
        if (parametersId == null) {
            Map<String, String> parameters = obj.getParameters();
            parametersId = ParametersUtils.getId(parameters);
            if (parametersId == null) {
                parametersId = "default";
            } else {
                obj.setParametersId(parametersId);
            }
        }
        Long x = Long.valueOf(obj.getXYZ()[0]);
        Long y = Long.valueOf(obj.getXYZ()[1]);
        Long z = Long.valueOf(obj.getXYZ()[2]);
        String extension;
        try {
            String format = obj.getBlobFormat();
            MimeType mimeType = MimeType.createFromFormat(format);
            shortFormat = mimeType.getFileExtension(); // png, png8, png24, etc
            extension = mimeType.getInternalName(); // png, jpeg, etc
        } catch (MimeException e) {
            throw new RuntimeException(e);
        }

        // 密钥格式，包括
        // {@code <prefix>/<layer name>/<gridset id>/<format id>/<parameters
        // hash>/<z>/<x>/<y>.<extension>}
        String key =
                join(
                        false,
                        prefix,
                        layer,
                        gridset,
                        shortFormat,
                        parametersId,
                        z,
                        x,
                        y + "." + extension);
        return key;
    }

    public String forLayer(final String layerName) {
        String layerId = layerId(layerName);
        // Layer prefix format, comprised of {@code <prefix>/<layer name>/}
        return join(true, prefix, layerId);
    }

    public String forGridset(final String layerName, final String gridsetId) {
        String layerId = layerId(layerName);
        // Layer prefix format, comprised of {@code <prefix>/<layer name>/}
        return join(true, prefix, layerId, gridsetId);
    }

    public Set<String> forParameters(final String layerName, final String parametersId) {
        String layerId = layerId(layerName);
        // 坐标前缀 : {@code <prefix>/<layer name>/<gridset id>/<format id>/<parametershash>/}
        return layerGridsets(layerName)
                .stream()
                .flatMap(
                        gridsetId ->
                                layerFormats(layerName)
                                        .stream()
                                        .map(
                                                format ->
                                                        join(
                                                                true,
                                                                prefix,
                                                                layerId,
                                                                gridsetId,
                                                                format,
                                                                parametersId)))
                .collect(Collectors.toSet());
    }

    public String layerMetadata(final String layerName) {
        String layerId = layerId(layerName);
        return join(false, prefix, layerId, LAYER_METADATA_OBJECT_NAME);
    }

    public String storeMetadata() {
        return join(false, prefix, LAYER_METADATA_OBJECT_NAME);
    }

    public String parametersMetadata(final String layerName, final String parametersId) {
        String layerId = layerId(layerName);
        return join(
                false,
                prefix,
                layerId,
                PARAMETERS_METADATA_OBJECT_PREFIX
                        + parametersId
                        + PARAMETERS_METADATA_OBJECT_SUFFIX);
    }

    public String parametersMetadataPrefix(final String layerName) {
        String layerId = layerId(layerName);
        return join(false, prefix, layerId, PARAMETERS_METADATA_OBJECT_PREFIX);
    }

    /**
     * @return the key prefix up to the coordinates(i.e. {@code
     *     "<prefix>/<layer>/<gridset>/<format>/<parametersId>"})
     */
    public String coordinatesPrefix(TileRange obj) {
        checkNotNull(obj.getLayerName());
        checkNotNull(obj.getGridSetId());
        checkNotNull(obj.getMimeType());

        String layer = layerId(obj.getLayerName());
        String gridset = obj.getGridSetId();
        MimeType mimeType = obj.getMimeType();

        String parametersId = obj.getParametersId();
        if (parametersId == null) {
            Map<String, String> parameters = obj.getParameters();
            parametersId = ParametersUtils.getId(parameters);
            if (parametersId == null) {
                parametersId = "default";
            } else {
                obj.setParametersId(parametersId);
            }
        }
        String shortFormat = mimeType.getFileExtension(); // png, png8, png24, etc

        String key = join(true, prefix, layer, gridset, shortFormat, parametersId);
        return key;
    }

    public String pendingDeletes() {
        return String.format("%s/%s", prefix, "_pending_deletes.properties");
    }

    private static String join(boolean closing, Object... elements) {
        StringJoiner joiner = new StringJoiner(DELIMITER);
        for (Object o : elements) {
            String s = o == null ? null : o.toString();
            if (!Strings.isNullOrEmpty(s)) {
                joiner.add(s);
            }
        }
        if (closing) {
            joiner.add("");
        }
        return joiner.toString();
    }
}
