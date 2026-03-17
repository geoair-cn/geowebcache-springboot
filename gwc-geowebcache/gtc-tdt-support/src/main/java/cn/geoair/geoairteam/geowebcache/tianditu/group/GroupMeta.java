package cn.geoair.geoairteam.geowebcache.tianditu.group;

import cn.geoair.base.util.GutilObject;
import cn.hutool.core.collection.ListUtil;

import cn.geoair.geoairteam.geowebcache.tianditu.layer.TC_ArcGISCacheLayer;
import java.util.*;
import org.geowebcache.grid.BoundingBox;
import org.geowebcache.grid.GridCoverage;
import org.geowebcache.grid.GridSubset;

/**
 * @author ：zfj
 * @date ：Created in 2024/1/24 16:44
 * @description： 图层组元数据
 */
public class GroupMeta {

    List<TC_ArcGISCacheLayer> tc_arcGISCacheLayers;

    String groupName;

    String remark;

    GridSubset gridSubset;

    public List<TC_ArcGISCacheLayer> isIn(
            String z, String x, String y, List<TC_ArcGISCacheLayer> tc_arcGISCacheLayers) {
        List<TC_ArcGISCacheLayer> inLayers = new ArrayList<>();
        long[] index = new long[3];
        index[0] = Long.valueOf(x);
        index[1] = Long.valueOf(y);
        index[2] = Long.valueOf(z);

        for (TC_ArcGISCacheLayer tc_arcGISCacheLayer : tc_arcGISCacheLayers) {
            GridSubset defaultGridSubset = tc_arcGISCacheLayer.getDefaultGridSubset();
            final long tilesHigh = defaultGridSubset.getNumTilesHigh(Integer.valueOf(z));
            // 这个Y值转换代码从 org/geowebcache/service/wmts/WMTSService.java:475 这一行抄过来的
            index[1] = tilesHigh - Long.valueOf(y) - 1;
            if (defaultGridSubset.covers(index)) {
                inLayers.add(tc_arcGISCacheLayer);
            }
        }
        return inLayers;
    }

    public GroupMeta(String groupName, TC_ArcGISCacheLayer tc_arcGISCacheLayer) {
        this.groupName = groupName;
        addTC_ArcGISCacheLayer(tc_arcGISCacheLayer);
    }

    public void addTC_ArcGISCacheLayer(TC_ArcGISCacheLayer tc_arcGISCacheLayer) {
        if (GutilObject.isEmpty(tc_arcGISCacheLayers)) {
            tc_arcGISCacheLayers = new ArrayList<>();
            gridSubset = new GridSubset(tc_arcGISCacheLayer.getDefaultGridSubset());
            tc_arcGISCacheLayers.add(tc_arcGISCacheLayer);
        } else {
            for (int i = 0; i < tc_arcGISCacheLayers.size(); i++) {
                TC_ArcGISCacheLayer tc_arcGISCacheLayer1 = tc_arcGISCacheLayers.get(i);
                String name = tc_arcGISCacheLayer1.getName();
                if (name.equals(tc_arcGISCacheLayer.getName())) {
                    return;
                }
            }
            tc_arcGISCacheLayers.add(tc_arcGISCacheLayer);
            this.gridSubset =
                    mergeGridSubset(this.gridSubset, tc_arcGISCacheLayer.getDefaultGridSubset());
            if (GutilObject.isEmpty(remark)) {
                remark = tc_arcGISCacheLayer.getRemark();
            }
        }
    }

    /**
     * 合并网格集合
     *
     * @param gridSubsetA
     * @param gridSubsetB
     * @return
     */
    public static GridSubset mergeGridSubset(GridSubset gridSubsetA, GridSubset gridSubsetB) {
        Integer maxCachedZoom =
                Math.max(
                        Optional.ofNullable(gridSubsetA.getMaxCachedZoom()).orElse(0),
                        Optional.ofNullable(gridSubsetB.getMaxCachedZoom()).orElse(0));
        Integer minCachedZoom =
                Math.min(
                        Optional.ofNullable(gridSubsetA.getMinCachedZoom()).orElse(0),
                        Optional.ofNullable(gridSubsetB.getMinCachedZoom()).orElse(0));
        BoundingBox subSetExtent =
                BoundingBox.merge(gridSubsetA.getOriginalExtent(), gridSubsetB.getOriginalExtent());
        Map<Integer, GridCoverage> AgridCoverageLevels = gridSubsetA.getGridCoverageLevels();
        Map<Integer, GridCoverage> BgridCoverageLevels = gridSubsetB.getGridCoverageLevels();
        Integer maxZoomA = Collections.max(AgridCoverageLevels.keySet());
        Integer maxZoomB = Collections.max(BgridCoverageLevels.keySet());
        Integer maxZoom = Math.max(maxZoomA, maxZoomB);
        Map<Integer, GridCoverage> meMap = new LinkedHashMap<>();
        for (Integer i = 0; i < maxZoom; i++) {
            GridCoverage mergeCoverage = null;
            long[] coverage = new long[5];
            GridCoverage gridCoverageA = AgridCoverageLevels.get(i);
            GridCoverage gridCoverageB = BgridCoverageLevels.get(i);
            if (GutilObject.isNull(gridCoverageA)) {
                mergeCoverage = gridCoverageB;
            }
            if (GutilObject.isNull(gridCoverageB)) {
                mergeCoverage = gridCoverageA;
            }
            if (GutilObject.isEmpty(mergeCoverage)) {
                long[] coverageA = gridCoverageA.getCoverage();
                long[] coverageB = gridCoverageB.getCoverage();
                //            minx,miny,maxx,maxy,zoomlevel
                coverage[0] = Math.max(coverageA[0], coverageB[0]);
                coverage[1] = Math.max(coverageA[1], coverageB[1]);
                coverage[2] = Math.max(coverageA[2], coverageB[2]);
                coverage[3] = Math.max(coverageA[3], coverageB[3]);
                coverage[4] = Math.max(coverageA[4], coverageB[4]);
                mergeCoverage = new GridCoverage(coverage);
            }
            meMap.put(i, mergeCoverage);
        }
        GridSubset gridSubset =
                new GridSubset(
                        gridSubsetA.getGridSet(),
                        meMap,
                        subSetExtent,
                        false,
                        minCachedZoom == 0 ? null : minCachedZoom,
                        maxCachedZoom == 0 ? null : maxCachedZoom);
        return gridSubset;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getRemark() {
        return remark;
    }

    public GridSubset getGridSubset() {
        return gridSubset;
    }

    public List<TC_ArcGISCacheLayer> getTGISCacheLayers() {
        return ListUtil.unmodifiable(tc_arcGISCacheLayers);
    }
}
