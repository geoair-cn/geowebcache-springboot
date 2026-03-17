/**
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>分发此程序是希望它有用，但不作任何保证； 甚至没有对适销性或针对特定目的的适用性的暗示保证。 有关更多详细信息，请参阅 GNU 通用公共许可证.
 *
 * <p>您应该已经收到一份 GNU 宽松通用公共许可证以及该程序。如果没有，请参阅 <http:www.gnu.orglicenses>。
 *
 * @author geoair，版权所有 2021
 */
package cn.geoair.geoairteam.geowebcache.tianditu.layer;

import java.util.List;
import org.geowebcache.arcgis.config.*;
import org.geowebcache.grid.BoundingBox;
import org.geowebcache.grid.GridSet;
import org.geowebcache.grid.GridSetFactory;
import org.geowebcache.grid.SRS;
import org.springframework.util.Assert;

/**
 * Utility to crate a {@link GridSet} out of a ArcGIS tiling scheme
 *
 * <p>从 ArcGIS 切片方案中创建 {@link GridSet} 实用程序
 *
 * @author geoair
 */
class GridSetBuilder {

    /** 根据 ArcGIS 切片方案创建 {@link GridSet} */
    public GridSet buildGridset(
            final String layerName, final CacheInfo info, final BoundingBox layerBounds) {

        Assert.notNull(layerName, "图层名称不能为空");
        Assert.notNull(info, "图层信息不能为空");
        Assert.notNull(layerBounds, "图层边界不能为空");

        final TileCacheInfo tileCacheInfo = info.getTileCacheInfo();
        final SpatialReference spatialReference = tileCacheInfo.getSpatialReference();

        final SRS srs;
        final BoundingBox gridSetExtent;

        final boolean alignTopLeft = true;
        final double[] resolutions;
        /*
         * 让 scale denoms 为 null，以便 GridSetFactory 根据分辨率计算它们。 结果值将非常接近 ArcGIS 切片方案中定义的值
         */
        final double[] scaleDenominators = null;
        final Double metersPerUnit;
        final String[] scaleNames = null;
        final int tileWidth = tileCacheInfo.getTileCols();
        final int tileHeight = tileCacheInfo.getTileRows();
        final boolean yCoordinateFirst = false;
        final double pixelSize = 0.0254 / tileCacheInfo.getDPI(); // see GridSubset.getDotsPerInch()
        {
            int epsgNumber = spatialReference.getWKID();
            srs = SRS.getSRS(epsgNumber);
        }
        {
            final List<LODInfo> lodInfos = tileCacheInfo.getLodInfos();
            double[][] resAndScales = getResolutions(lodInfos);
            resolutions = resAndScales[0];
            double[] scales = resAndScales[1];
            metersPerUnit = (GridSetFactory.DEFAULT_PIXEL_SIZE_METER * scales[0]) / resolutions[0];
        }
        {
            // 请参阅“如何计算上述示例中使用的 -x 参数”，网址为
            // http://resources.arcgis.com/content/kbase?q=content/kbase&fa=articleShow&d=15558&print=true
            // double XOrigin = spatialReference.getXOrigin();
            // double YOrigin = spatialReference.getYOrigin();
            // 其中 40075017 是地球在赤道的周长，360 是厄瓜多尔的地图单位
            // XYScale = 40075017 / 360 = ~111319,
            // final double xyScale = spatialReference.getXYScale();

            final TileOrigin tileOrigin = tileCacheInfo.getTileOrigin(); // top left coordinate

            double xmin = tileOrigin.getX();
            double ymax = tileOrigin.getY();
            double ymin = layerBounds.getMinY();
            double xmax = layerBounds.getMaxX();

            // 使网格集高度匹配整数个图块，以便客户端（OpenLayers）假设图块原点是左下角而不是右上角来计算图块边界框
            final double resolution = resolutions[resolutions.length - 1];
            double width = resolution * tileWidth;
            double height = resolution * tileHeight;

            long numTilesWide = (long) Math.ceil((xmax - xmin) / width);
            long numTilesHigh = (long) Math.ceil((ymax - ymin) / height);

            xmax = xmin + (numTilesWide * width);
            ymin = ymax - (numTilesHigh * height);
            gridSetExtent = new BoundingBox(xmin, ymin, xmax, ymax);
        }

        String gridsetName = srs.toString() + "_" + layerName;
        GridSet layerGridset =
                GridSetFactory.createGridSet(
                        gridsetName,
                        srs,
                        gridSetExtent,
                        alignTopLeft,
                        resolutions,
                        scaleDenominators,
                        metersPerUnit,
                        pixelSize,
                        scaleNames,
                        tileWidth,
                        tileHeight,
                        yCoordinateFirst);

        return layerGridset;
    }

    private double[][] getResolutions(List<LODInfo> lodInfos) {
        final int numLevelsOfDetail = lodInfos.size();
        double[][] resolutionsAndScales = new double[2][numLevelsOfDetail];
        LODInfo lodInfo;
        for (int i = 0; i < numLevelsOfDetail; i++) {
            lodInfo = lodInfos.get(i);
            resolutionsAndScales[0][i] = lodInfo.getResolution();
            resolutionsAndScales[1][i] = lodInfo.getScale();
        }
        return resolutionsAndScales;
    }
}
