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

import cn.geoair.base.bean.GirBeanHelper;

import cn.geoair.base.util.GutilObject;
import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerInfoPo;
import cn.geoair.geoairteam.gwc.servface.gwc.LayerInfoService;
import cn.geoair.geoairteam.geowebcache.tianditu.group.TC_ArcGISCacheLayerGroup;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.arcgis.compact.ArcGISCompactCache;
import org.geowebcache.arcgis.compact.ArcGISCompactCacheV1;
import org.geowebcache.arcgis.compact.ArcGISCompactCacheV2;
import org.geowebcache.arcgis.config.*;
import org.geowebcache.conveyor.Conveyor.CacheResult;
import org.geowebcache.conveyor.ConveyorTile;
import org.geowebcache.grid.*;
import org.geowebcache.io.FileResource;
import org.geowebcache.io.Resource;
import org.geowebcache.layer.AbstractTileLayer;
import org.geowebcache.layer.ExpirationRule;
import org.geowebcache.mime.MimeException;
import org.geowebcache.mime.MimeType;
import org.geowebcache.util.GWCVars;

/**
 * {@link org.geowebcache.layer.TileLayer} ArcGIS 切片图层的实现
 *
 * @author geoair
 */
public class TC_ArcGISCacheLayer extends AbstractTileLayer {

    private static final Log log = LogFactory.getLog(TC_ArcGISCacheLayer.class);

    /*
     * 配置属性
     */

    private Boolean enabled;

    /** conf.xml 切片方案配置文件的位置 */
    private File tilingScheme;

    /**
     * 可选，实际磁贴文件夹的位置。 如果未提供，则默认为 {@code _alllayers} 目录位于与 {@link #getTilingScheme() conf.xml}
     * 平铺方案相同的位置
     */
    private File tileCachePath;

    /** 可选，配置 z 值是否应为十六进制编码。 如果未提供，则默认为 false */
    private Boolean hexZoom;

    /** 所属的图层组 */
    private String group;

    /** 排序的key */
    private String sortkey;

    /** 备注信息 */
    private String remark = "";

    private transient CacheInfo cacheInfo;

    public BoundingBox getLayerBounds() {
        return layerBounds;
    }

    private transient BoundingBox layerBounds;

    private String storageFormat;

    private transient ArcGISCompactCache compactCache;

    public TC_ArcGISCacheLayer(String name) {
        this.name = name;
    }

    /** @return {@code null}, 这种层处理自己的存储. */
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

    public File getTilingScheme() {
        return tilingScheme;
    }

    public void setTilingScheme(final File tilingScheme) {
        this.tilingScheme = tilingScheme;
    }

    /** 返回实际平铺文件夹的位置，如果未提供，则返回null ， 在这种情况下，内部默认为 {@code _alllayers} 目录，该 目录与conf.xml平铺方案位于同一位置。 */
    public File getTileCachePath() {
        return tileCachePath;
    }

    /**
     * 选项，实际 tile 文件夹的位置。 如果未提供，则默认为 {@code_alllayers} 目录与 {@link #getTilingScheme() conf.xml}
     * 平铺方案位于同一位置。
     */
    public void setTileCachePath(File tileCachePath) {
        this.tileCachePath = tileCachePath;
    }

    public boolean isHexZoom() {
        return hexZoom;
    }

    public void setHexZoom(boolean hexZoom) {
        this.hexZoom = hexZoom;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getSortkey() {
        return sortkey;
    }

    public void setSortkey(String sortkey) {
        this.sortkey = sortkey;
    }

    public String getRemark() {
        return GutilObject.isEmpty(remark) ? "" : remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * @return {@code true} 如果成功则为true 。 请注意，此方法的返回类型应为 void。 它没有在任何地方检查
     * @see org.geowebcache.layer.TileLayer#initialize(GridSetBroker)
     */
    @Override
    protected boolean initializeInternal(GridSetBroker gridSetBroker) {
        LayerInfoService layerInfoService =
                GirBeanHelper.getProvider().getBean(LayerInfoService.class);
        if (this.enabled == null) {
            this.enabled = true;
        }
        if (this.tilingScheme == null) {
            throw new IllegalStateException(
                    " tilingScheme 尚未设置。 它应该指向 ArcGIS" + "该层的缓存切片方案文件 (conf.xml)");
        }
        if (tileCachePath != null) {
            if (!tileCachePath.exists()
                    || !tileCachePath.isDirectory()
                    || !tileCachePath.canRead()) {
                throw new IllegalStateException(
                        " 图层的 tileCachePath 属性 '"
                                + getName()
                                + "' 被设定为 '"
                                + tileCachePath
                                + "' 但该目录不存在或不可读");
            }
        }
        if (this.hexZoom == null) {
            this.hexZoom = false;
        }
        try {
            LayerInfoPo layerInfoPo = layerInfoService.getPoById(this.getName());
            CacheInfoPersister tilingSchemeLoader = new CacheInfoPersister();
            cacheInfo = tilingSchemeLoader.load(new FileReader(tilingScheme));

            File layerBoundsFile = new File(tilingScheme.getParentFile(), "conf.cdi");
            if (!layerBoundsFile.exists()) {
                throw new RuntimeException("未找到图层边界文件： " + layerBoundsFile.getAbsolutePath());
            }
            log.info("解析图层边界 " + getName());
            this.layerBounds = tilingSchemeLoader.parseLayerBounds(new FileReader(layerBoundsFile));
            log.info(" 解析图层边界" + getName() + ": " + layerBounds);
            layerInfoPo.setMaxx(layerBounds.getMaxX());
            layerInfoPo.setMinx(layerBounds.getMinX());
            layerInfoPo.setMaxy(layerBounds.getMaxY());
            layerInfoPo.setMiny(layerBounds.getMinY());
            TileOrigin tileOrigin = cacheInfo.getTileCacheInfo().getTileOrigin();
            layerInfoPo.setTileOrigin(tileOrigin.getX() + "," + tileOrigin.getY());
            layerInfoPo.setDpi(cacheInfo.getTileCacheInfo().getDPI() + "");
            layerInfoPo.setWkid(cacheInfo.getTileCacheInfo().getSpatialReference().getWKID());
            this.storageFormat = cacheInfo.getCacheStorageInfo().getStorageFormat();
            layerInfoPo.setStorageFormat(storageFormat);
            layerInfoService.update(layerInfoPo);
            layerInfoService.updateBbox(layerInfoPo.getId());
            if (this.storageFormat.equals(CacheStorageInfo.COMPACT_FORMAT_CODE)
                    || this.storageFormat.equals(CacheStorageInfo.COMPACT_FORMAT_CODE_V2)) {
                String pathToCacheRoot = tilingScheme.getParent() + "/_alllayers";
                if (tileCachePath != null) pathToCacheRoot = tileCachePath.getAbsolutePath();

                if (this.storageFormat.equals(CacheStorageInfo.COMPACT_FORMAT_CODE)) {
                    log.info(getName() + " 使用的arcgis的版本为 (ArcGIS 10.0 - 10.2)");
                    compactCache = new ArcGISCompactCacheV1(pathToCacheRoot);
                } else {
                    log.info(getName() + " 使用的arcgis的版本为 (ArcGIS 10.3)");
                    compactCache = new ArcGISCompactCacheV2(pathToCacheRoot);
                }
            }
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("未找到切片方案文件: " + tilingScheme.getAbsolutePath());
        }
        log.info("图层 " + getName() + " : ArcGIS 切片方案 解析完成" + tilingScheme.getAbsolutePath());

        super.subSets = createGridSubsets(gridSetBroker);
        super.formats = loadMimeTypes();
        TC_ArcGISCacheLayerGroup.putGroup(this.group, this);
        log.info("图层组加入图层:" + this.group + ":" + this.getName());
        return true;
    }

    private List<MimeType> loadMimeTypes() {
        String cacheTileFormat = this.cacheInfo.getTileImageInfo().getCacheTileFormat();
        if ("mixed".equalsIgnoreCase(cacheTileFormat) || "jpg".equalsIgnoreCase(cacheTileFormat)) {
            cacheTileFormat = "JPEG";
        } else if (cacheTileFormat.toLowerCase().startsWith("png")) {
            cacheTileFormat = "png";
        }
        cacheTileFormat = "image/" + cacheTileFormat.toLowerCase();
        MimeType format;
        try {
            format = MimeType.createFromFormat(cacheTileFormat);
        } catch (MimeException e) {
            throw new RuntimeException(e);
        }
        return Collections.singletonList(format);
    }

    private HashMap<String, GridSubset> createGridSubsets(final GridSetBroker gridSetBroker) {

        final CacheInfo info = this.cacheInfo;
        final TileCacheInfo tileCacheInfo = info.getTileCacheInfo();

        final String layerName = getName();
        final GridSetBuilder gsBuilder = new GridSetBuilder();
        GridSet gridSet = gsBuilder.buildGridset(layerName, info, layerBounds);

        getGridsetConfiguration(gridSetBroker).addInternal(gridSet);

        final List<LODInfo> lodInfos = tileCacheInfo.getLodInfos();
        Integer zoomStart = lodInfos.get(0).getLevelID();
        Integer zoomStop = lodInfos.get(lodInfos.size() - 1).getLevelID();

        GridSubset subSet =
                GridSubsetFactory.createGridSubSet(gridSet, this.layerBounds, zoomStart, zoomStop);

        HashMap<String, GridSubset> subsets = new HashMap<>();
        subsets.put(gridSet.getName(), subSet);
        return subsets;
    }

    private TC_ArcGISCacheGridsetConfiguration getGridsetConfiguration(
            final GridSetBroker gridSetBroker) {
        List<? extends TC_ArcGISCacheGridsetConfiguration> configs =
                gridSetBroker.getConfigurations(TC_ArcGISCacheGridsetConfiguration.class);
        if (configs.isEmpty()) {
            throw new IllegalStateException("没有找到TC-ArcGISCacheGridsetConfiguration");
        } else {
            if (configs.size() > 1) {
                log.warn("ArcGISCacheGridsetConfiguration 的多个实例，首先使用第一个");
            }
            return configs.iterator().next();
        }
    }

    /** @see org.geowebcache.layer.TileLayer#getTile(ConveyorTile) */
    @Override
    public ConveyorTile getTile(final ConveyorTile tile)
            throws GeoWebCacheException, IOException, OutsideCoverageException {

        Resource tileContent = null;

        if (storageFormat.equals(CacheStorageInfo.COMPACT_FORMAT_CODE)
                || storageFormat.equals(CacheStorageInfo.COMPACT_FORMAT_CODE_V2)) {
            final long[] tileIndex = tile.getTileIndex();
            final String gridSetId = tile.getGridSetId();
            final GridSubset gridSubset = this.getGridSubset(gridSetId);

            GridSet gridSet = gridSubset.getGridSet();
            final int zoom = (int) tileIndex[2];

            Grid grid = gridSet.getGrid(zoom);
            long coverageMaxY = grid.getNumTilesHigh() - 1;

            final int col = (int) tileIndex[0];
            final int row = (int) (coverageMaxY - tileIndex[1]);

            tileContent = compactCache.getBundleFileResource(zoom, row, col);

        } else if (storageFormat.equals(CacheStorageInfo.EXPLODED_FORMAT_CODE)) {
            String path = getTilePath(tile);
            File tileFile = new File(path);

            if (tileFile.exists()) {
                tileContent = readFile(tileFile);
            }
        }

        if (tileContent != null) {
            tile.setCacheResult(CacheResult.HIT);
            tile.setBlob(tileContent);
        } else {
            tile.setCacheResult(CacheResult.MISS);
            if (!setLayerBlankTile(tile)) {
                throw new OutsideCoverageException(tile.getTileIndex(), 0, 0);
            }
        }
        // TODO 添加到这里
        saveExpirationInformation((int) (tile.getExpiresHeader() / 1000));

        return tile;
    }

    protected void saveExpirationInformation(int backendExpire) {
        this.saveExpirationHeaders = false;

        try {
            if (getExpireCache(0) == GWCVars.CACHE_USE_WMS_BACKEND_VALUE) {
                if (backendExpire == -1) {
                    this.expireCacheList.set(0, new ExpirationRule(0, 7200));
                    log.error("图层配置文件需要后端的 MaxAge," + " 但后端不提供这个,设置为 7200 秒。");
                } else {
                    this.expireCacheList.set(backendExpire, new ExpirationRule(0, 7200));
                }
                log.trace("将 expireCache 设置为: " + expireCache);
            }
            if (getExpireCache(0) == GWCVars.CACHE_USE_WMS_BACKEND_VALUE) {
                if (backendExpire == -1) {
                    this.expireClientsList.set(0, new ExpirationRule(0, 7200));
                    log.error("图层配置文件需要后端的 MaxAge," + " 但后端不提供这个,设置为 7200 秒。");
                } else {
                    this.expireClientsList.set(0, new ExpirationRule(0, backendExpire));
                    log.trace("将 expireCache 设置为: " + expireClients);
                }
            }
        } catch (Exception e) {
            // 有时这不起作用（网络条件？），而且真的不值得关注它。
            log.debug(e);
        }
    }

    private boolean setLayerBlankTile(ConveyorTile tile) {
        // TODO 缓存结果
        String layerPath = getLayerPath().append(File.separatorChar).toString();
        File png = new File(layerPath + "blank.png");
        Resource blank = null;
        try {
            if (png.exists()) {
                blank = readFile(png);
                tile.setBlob(blank);
                tile.setMimeType(MimeType.createFromFormat("image/png"));
            } else {
                File jpeg = new File(layerPath + "missing.jpg");
                if (jpeg.exists()) {
                    blank = readFile(jpeg);
                    tile.setBlob(blank);
                    tile.setMimeType(MimeType.createFromFormat("image/jpeg"));
                }
            }
        } catch (Exception e) {
            return false;
        }
        return blank != null;
    }

    private String getTilePath(final ConveyorTile tile) {

        final MimeType mimeType = tile.getMimeType();
        final long[] tileIndex = tile.getTileIndex();
        final String gridSetId = tile.getGridSetId();
        final GridSubset gridSubset = this.getGridSubset(gridSetId);

        GridSet gridSet = gridSubset.getGridSet();
        final int z = (int) tileIndex[2];

        Grid grid = gridSet.getGrid(z);

        // long[] coverage = gridSubset.getCoverage(z);
        // long coverageMinY = coverage[1];
        long coverageMaxY = grid.getNumTilesHigh() - 1;

        final long x = tileIndex[0];
        // 反转请求的 Y 坐标的顺序，因为 ArcGIS 缓存是从左上到右下，而 GWC 以从左下到右上的顺序计算切片
        final long y = (coverageMaxY - tileIndex[1]);

        String level = (this.hexZoom) ? Integer.toHexString(z) : Integer.toString(z);
        level = zeroPadder(level, 2);

        String row = Long.toHexString(y);
        row = zeroPadder(row, 8);

        String col = Long.toHexString(x);
        col = zeroPadder(col, 8);

        StringBuilder path = getLayerPath();

        path.append(File.separatorChar)
                .append('L')
                .append(level)
                .append(File.separatorChar)
                .append('R')
                .append(row)
                .append(File.separatorChar)
                .append('C')
                .append(col);

        String fileExtension = mimeType.getFileExtension();
        if ("jpeg".equalsIgnoreCase(fileExtension)) {
            fileExtension = "jpg";
        }
        path.append('.').append(fileExtension);

        return path.toString();
    }

    private StringBuilder getLayerPath() {
        StringBuilder path;
        if (tileCachePath == null) {
            path = new StringBuilder(this.tilingScheme.getParent());
            // 请注意，我们假设它是一个“融合”的切片缓存。 当涉及到支持多个图层切片缓存时，我们需要参数化图层的缓存目录
            path.append(File.separatorChar).append("_alllayers");
        } else {
            path = new StringBuilder(tileCachePath.getAbsolutePath());
        }
        return path;
    }

    private String zeroPadder(String s, int order) {
        if (s.length() >= order) {
            return s;
        }
        char[] data = new char[order];
        Arrays.fill(data, '0');

        for (int i = s.length() - 1, j = order - 1; i >= 0; i--, j--) {
            data[j] = s.charAt(i);
        }
        return String.valueOf(data);
    }

    private Resource readFile(File fh) {
        if (!fh.exists()) {
            return null;
        }
        Resource res = new FileResource(fh);
        return res;
    }

    /** @see org.geowebcache.layer.TileLayer#getNoncachedTile(ConveyorTile) */
    @Override
    public ConveyorTile getNoncachedTile(ConveyorTile tile) throws GeoWebCacheException {
        throw new UnsupportedOperationException();
    }

    /** @see org.geowebcache.layer.TileLayer#seedTile(ConveyorTile, boolean) */
    @Override
    public void seedTile(ConveyorTile tile, boolean tryCache)
            throws GeoWebCacheException, IOException {
        throw new UnsupportedOperationException();
    }

    /** @see org.geowebcache.layer.TileLayer#doNonMetatilingRequest(ConveyorTile) */
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
         * NOTE: 注意：这个方法似乎不属于 TileLayer，而是属于 GeoWebCacheDispatcher 本身
         */
    }

    public GridSubset getDefaultGridSubset() {
        Set<Map.Entry<String, GridSubset>> entries = this.subSets.entrySet();
        if (!entries.isEmpty()) {
            Iterator<Map.Entry<String, GridSubset>> iterator = entries.iterator();
            Map.Entry<String, GridSubset> next = iterator.next();
            return next.getValue();
        } else {
            return null;
        }
    }

    @Override
    public TC_ArcGISCacheLayer clone() {
        TC_ArcGISCacheLayer tc_arcGISCacheLayer = null;
        try {
            tc_arcGISCacheLayer = (TC_ArcGISCacheLayer) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return tc_arcGISCacheLayer;
    }

    public String setName(String name) {
        this.name = name;
        return name;
    }
}
