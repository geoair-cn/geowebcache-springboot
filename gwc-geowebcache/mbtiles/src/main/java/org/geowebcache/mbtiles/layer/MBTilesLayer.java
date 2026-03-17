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

import com.google.common.annotations.VisibleForTesting;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.mbtiles.MBTilesFile;
import org.geotools.mbtiles.MBTilesMetadata;
import org.geotools.mbtiles.MBTilesTile;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.config.DefaultGridsets;
import org.geowebcache.conveyor.Conveyor.CacheResult;
import org.geowebcache.conveyor.ConveyorTile;
import org.geowebcache.grid.BoundingBox;
import org.geowebcache.grid.GridSet;
import org.geowebcache.grid.GridSetBroker;
import org.geowebcache.grid.GridSubset;
import org.geowebcache.grid.GridSubsetFactory;
import org.geowebcache.grid.OutsideCoverageException;
import org.geowebcache.io.ByteArrayResource;
import org.geowebcache.layer.AbstractTileLayer;
import org.geowebcache.layer.ExpirationRule;
import org.geowebcache.layer.TileJSONProvider;
import org.geowebcache.layer.meta.TileJSON;
import org.geowebcache.mime.ApplicationMime;
import org.geowebcache.mime.ImageMime;
import org.geowebcache.mime.MimeType;
import org.geowebcache.util.GWCVars;

/**
 * {@link org.geowebcache.layer.TileLayer} MBtiles 层的实现
 *
 * @author geoair
 */
public class MBTilesLayer extends AbstractTileLayer implements TileJSONProvider {

    private static final String UNZIP_CONTENT_KEY = "gwc.mbtiles.pbf.unzip.debug";

    private static final boolean UNZIP_CONTENT =
            Boolean.valueOf(System.getProperty(UNZIP_CONTENT_KEY, "true"));

    private static final int TILE_SIZE_256 = 256;

    private static final int TILE_SIZE_512 = 512;

    private static final int DEFAULT_TILE_SIZE = TILE_SIZE_256;

    private static final Log LOG = LogFactory.getLog(MBTilesLayer.class);

    /*
     * configuration properties
     */
    private Boolean enabled;

    private File tilesPath;

    private int tileSize = DEFAULT_TILE_SIZE;

    private transient MBTilesInfo tilesInfo;

    private transient BoundingBox layerBounds;

    private MimeType mimeType;

    private MBTilesFile mbTilesFile;

    private boolean vectorTiles;

    @VisibleForTesting
    MBTilesLayer(String name) {
        this.name = name;
    }

    /** @return {@code null}, 这种层处理自己的存储。 */
    @Override
    public String getBlobStoreId() {
        return null;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** 返回实际图块集的位置。 */
    public File getTilesPath() {
        return tilesPath;
    }

    public void setTilesPath(File tilesPath) {
        this.tilesPath = tilesPath;
    }

    public int getTileSize() {
        return tileSize;
    }

    public void setTileSize(int tileSize) {
        this.tileSize = tileSize;
    }

    public boolean isVectorTiles() {
        return vectorTiles;
    }

    /**
     * @return 如果成功则为{@code true} 请注意，此方法的返回类型应为 void。 它没有在任何地方检查
     * @see org.geowebcache.layer.TileLayer#initialize(org.geowebcache.grid.GridSetBroker)
     */
    @Override
    protected boolean initializeInternal(GridSetBroker gridSetBroker) {
        if (this.enabled == null) {
            this.enabled = true;
        }
        String specifiedName = getName();
        if (tilesPath != null) {
            if (!tilesPath.exists() || !tilesPath.canRead()) {
                throw new IllegalStateException(
                        "此图层的tilesPath 属性 "
                                + (specifiedName != null ? specifiedName : "")
                                + " 被设定为 '"
                                + tilesPath
                                + "' 但该文件不存在或不可读e");
            }
        }
        try {
            mbTilesFile = new MBTilesFile(tilesPath);
            tilesInfo = new MBTilesInfo(mbTilesFile);
            layerBounds = tilesInfo.getBounds();
            if (StringUtils.isEmpty(specifiedName)) {
                name = tilesInfo.getMetadataName();
            }
        } catch (IOException e) {
            throw new IllegalStateException("无法打开提供的 MBTile: " + tilesPath);
        }

        super.subSets = createGridSubsets(gridSetBroker);
        super.formats = loadMimeTypes();
        return true;
    }

    private List<MimeType> loadMimeTypes() {
        MBTilesMetadata.t_format metadataFormat = tilesInfo.getFormat();

        switch (metadataFormat) {
            case PNG:
                mimeType = ImageMime.png;
                break;
            case JPEG:
            case JPG:
                mimeType = ImageMime.jpeg;
                break;
            case PBF:
                mimeType = ApplicationMime.mapboxVector;
                vectorTiles = true;
                break;
        }
        return Collections.singletonList(mimeType);
    }

    private HashMap<String, GridSubset> createGridSubsets(final GridSetBroker gridSetBroker) {
        GridSet gridSet;

        DefaultGridsets defaultGridSet = new DefaultGridsets(true, true);

        if (tileSize <= 0) {
            tileSize = DEFAULT_TILE_SIZE;
        }

        switch (tileSize) {
            case TILE_SIZE_256:
                gridSet = defaultGridSet.worldEpsg3857();
                break;
            case TILE_SIZE_512:
                gridSet = defaultGridSet.worldEpsg3857x2();
                break;
            default:
                throw new IllegalArgumentException("不支持的 tileSize: " + tileSize);
        }
        Integer minZoom = tilesInfo.getMinZoom();
        Integer maxZoom = tilesInfo.getMaxZoom();

        GridSubset subSet =
                GridSubsetFactory.createGridSubSet(gridSet, this.layerBounds, minZoom, maxZoom);

        HashMap<String, GridSubset> subsets = new HashMap<>();
        subsets.put(gridSet.getName(), subSet);
        return subsets;
    }

    /** @see org.geowebcache.layer.TileLayer#getTile(org.geowebcache.conveyor.ConveyorTile) */
    @Override
    public ConveyorTile getTile(final ConveyorTile tile)
            throws IOException, OutsideCoverageException {

        long[] tileIndex = tile.getTileIndex();
        int zl = (int) tileIndex[2];
        int row = (int) tileIndex[1];
        int column = (int) tileIndex[0];
        MBTilesTile loadedTile = mbTilesFile.loadTile(zl, column, row);
        byte[] content = loadedTile.getData();
        if (content != null) {
            if (tilesInfo.getFormat() == MBTilesMetadata.t_format.PBF) {
                content = getPbfFromTile(content);
            }

            tile.setBlob(new ByteArrayResource(content));
            tile.setCacheResult(CacheResult.HIT);
        } else {
            tile.setCacheResult(CacheResult.MISS);
            throw new OutsideCoverageException(tile.getTileIndex(), 0, 0);
        }

        saveExpirationInformation((int) (tile.getExpiresHeader() / 1000));

        return tile;
    }

    private byte[] getPbfFromTile(byte[] raw) throws IOException {
        if (!UNZIP_CONTENT) {
            return raw;
        }
        // GZIP GZIP 幻数检查
        byte[] byteArray = raw;
        if (raw != null && raw.length >= 2 && raw[0] == (byte) 0x1F && raw[1] == (byte) 0x8b) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                    ByteArrayInputStream bin = new ByteArrayInputStream(raw);
                    InflaterInputStream in = new GZIPInputStream(bin)) {
                byte[] buffer = new byte[1024];
                int noRead;
                while ((noRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, noRead);
                }
                byteArray = out.toByteArray();
            }
        }
        return byteArray;
    }

    protected void saveExpirationInformation(int backendExpire) {
        this.saveExpirationHeaders = false;

        try {
            if (getExpireCache(0) == GWCVars.CACHE_USE_WMS_BACKEND_VALUE) {
                if (backendExpire == -1) {
                    this.expireCacheList.set(0, new ExpirationRule(0, 7200));
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("图层配置文件需要后端的 MaxAge," + " 但是后端没有提供。 设置为 7200 秒。");
                    }
                } else {
                    this.expireCacheList.set(backendExpire, new ExpirationRule(0, 7200));
                }
                if (LOG.isTraceEnabled()) {
                    LOG.trace("将 expireCache 设置为: " + expireCache);
                }
            }
            if (getExpireCache(0) == GWCVars.CACHE_USE_WMS_BACKEND_VALUE) {
                if (backendExpire == -1) {
                    this.expireClientsList.set(0, new ExpirationRule(0, 7200));
                    if (LOG.isDebugEnabled()) {
                        LOG.error("图层配置文件需要后端的 MaxAge," + " 但是后端没有提供。 设置为 7200 秒。");
                    }
                } else {
                    this.expireClientsList.set(0, new ExpirationRule(0, backendExpire));
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("将 expireCache 设置为: " + expireClients);
                    }
                }
            }
        } catch (Exception e) {
            // 有时这不起作用（网络条件？），并且真的不值得被追上。
            if (LOG.isDebugEnabled()) {
                LOG.debug(e);
            }
        }
    }

    /**
     * @see org.geowebcache.layer.TileLayer#getNoncachedTile(org.geowebcache.conveyor.ConveyorTile)
     */
    @Override
    public ConveyorTile getNoncachedTile(ConveyorTile tile) throws GeoWebCacheException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.geowebcache.layer.TileLayer#seedTile(org.geowebcache.conveyor.ConveyorTile, boolean)
     */
    @Override
    public void seedTile(ConveyorTile tile, boolean tryCache)
            throws GeoWebCacheException, IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see
     *     org.geowebcache.layer.TileLayer#doNonMetatilingRequest(org.geowebcache.conveyor.ConveyorTile)
     */
    @Override
    public ConveyorTile doNonMetatilingRequest(ConveyorTile tile) throws GeoWebCacheException {
        throw new UnsupportedOperationException();
    }

    /** @see org.geowebcache.layer.TileLayer#getStyles() */
    @Override
    public String getStyles() {
        return null;
    }

    /**
     * @see
     *     org.geowebcache.layer.TileLayer#setExpirationHeader(javax.servlet.http.HttpServletResponse,
     *     int)
     */
    @Override
    public void setExpirationHeader(HttpServletResponse response, int zoomLevel) {
        /*
         * 注意：这个方法似乎不属于 TileLayer，而是属于 GeoWebCacheDispatcher 本身
         */
    }

    @Override
    public boolean supportsTileJSON() {
        return true;
    }

    @Override
    public TileJSON getTileJSON() {
        TileJSON tileJSON = new TileJSON();
        tileJSON.setName(name);
        if (metaInformation != null) {
            tileJSON.setDescription(metaInformation.getDescription());
        }
        tilesInfo.decorateTileJSON(tileJSON);
        return tileJSON;
    }
}
