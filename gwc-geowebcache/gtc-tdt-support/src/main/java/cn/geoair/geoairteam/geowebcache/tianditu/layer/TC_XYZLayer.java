package cn.geoair.geoairteam.geowebcache.tianditu.layer;

import cn.geoair.map.dynamic.tools.GirAdvTools;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext.dto.XyzDto;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.conveyor.ConveyorTile;
import org.geowebcache.grid.GridSetBroker;
import org.geowebcache.io.ByteArrayResource;
import org.geowebcache.layer.AbstractTileLayer;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * @description: tms图层
 * @author: zhang_jun
 * @create: 2021-09-26 14:56
 */
public class TC_XYZLayer extends TC_TMSLayer {

    private static final Log log = LogFactory.getLog(TC_XYZLayer.class);


    @Override
    protected boolean initializeInternal(GridSetBroker gridSetBroker) {
        String baseName = "XYZ图层 :" + getName();
        if (getLoadMethod().equals("local")) {
            if (localPath.endsWith("/")) {
                int i = localPath.lastIndexOf("/");
                localPath = localPath.substring(0, i);
            }
            log.info(baseName + "部署在本地，路径为：" + getLocalPath());
        }
        if (getLoadMethod().equals("http")) {
            if (httpPath.endsWith("/")) {
                int i = httpPath.lastIndexOf("/");
                httpPath = httpPath.substring(0, i);
            }
            log.info(baseName + "部署在远程服务器，路径为：" + getHttpPath());
        }
        log.info(baseName + "加载成功");
        return true;
    }

    @Override
    public ConveyorTile getTile(ConveyorTile tile) {
        XyzDto xyzDto = XyzDto.byConveyorTile(tile);
        Long x = xyzDto.getX();
        Long y = xyzDto.getY();
        Long z = xyzDto.getZ();
        // geowebacahe他请求的时候，就翻转了，所以xyz这里要给他翻转回来
        int reverseY = GirAdvTools.getTileGrid3857Opt().reverseY(Math.toIntExact(y), Math.toIntExact(z));
        // log.info("演示demo加载");
        ByteArrayResource byteArrayResource = new ByteArrayResource();
        if (getLoadMethod().equals("local")) {
            String localPath = getLocalPath();
            localPath = StrUtil.replace(localPath, "{x}", String.valueOf(x));
            localPath = StrUtil.replace(localPath, "{y}", String.valueOf(reverseY));
            localPath = StrUtil.replace(localPath, "{z}", String.valueOf(z));
            File file = new File(localPath);
            if (FileUtil.exist(file)) {
                BufferedInputStream inputStream = null;
                try {
                    inputStream = FileUtil.getInputStream(localPath);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream((int) file.length());
                    IoUtil.copy(inputStream, bos);
                    byteArrayResource = new ByteArrayResource(bos.toByteArray());
                } catch (Exception e) {
                    log.info("文件流获取失败" + localPath, e);
                } finally {
                    IoUtil.close(inputStream);
                }
            }
        }
        if (getLoadMethod().equals("http")) {
            String httpBasePath = getHttpPath();
            httpBasePath = StrUtil.replace(httpBasePath, "{x}", String.valueOf(x));
            httpBasePath = StrUtil.replace(httpBasePath, "{y}", String.valueOf(reverseY));
            httpBasePath = StrUtil.replace(httpBasePath, "{z}", String.valueOf(z));
            HttpRequest httpRequest = HttpUtil.createGet(httpBasePath).timeout(1000);
            try {
                HttpResponse execute = httpRequest.execute();
                byteArrayResource = new ByteArrayResource(execute.bodyBytes());
            } catch (Exception e) {
                log.info(e);
            }
        }
        tile.setBlob(byteArrayResource);
        return tile;
    }

    @Override
    public ConveyorTile getNoncachedTile(ConveyorTile tile) {
        return null;
    }

    @Override
    public void seedTile(ConveyorTile tile, boolean tryCache)
            throws GeoWebCacheException, IOException {
    }

    @Override
    public ConveyorTile doNonMetatilingRequest(ConveyorTile tile) throws GeoWebCacheException {
        return null;
    }

    @Override
    public String getStyles() {
        return null;
    }
}
