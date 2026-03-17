package cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext.dto;

import cn.hutool.core.util.NumberUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.geowebcache.conveyor.Conveyor;
import org.geowebcache.stats.RuntimeStats;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;

/**
 * @author ：zfj
 * &#064;date ：Created in 2025/11/13 16:17
 * &#064;description：瓦片请求数据传输对象
 */
@Data
@Accessors(chain = true)
public class TileResult implements Serializable {

    public static TileResult of(String layerName) {
        return new TileResult();
    }


    private String mimeType = MediaType.IMAGE_PNG_VALUE;
    /**
     * 图层名称
     */
    private String layerName;


    /**
     * 瓦片输入流
     */
    private byte[] bytes;

    /**
     * 最后修改时间戳
     */
    private long lastModified;

    /**
     * 瓦片文件大小
     */
    private long size;


    /**
     * 瓦片是否存在标识
     */
    private boolean exists;

    public MediaType mimeTypeToType() {
        return MediaType.parseMediaType(mimeType);
    }


    public ResponseEntity<byte[]> toResponse(RuntimeStats runtimeStats) {
        if (!this.isExists()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        runtimeStats.log(NumberUtil.parseInt(this.getSize() + ""), Conveyor.CacheResult.HIT);
        HttpHeaders headers = buildHeaders(this);
        return new ResponseEntity<>(this.getBytes(), headers, HttpStatus.OK);
    }

    public ResponseEntity<byte[]> toResponse() {
        if (!this.isExists()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        HttpHeaders headers = buildHeaders(this);
        return new ResponseEntity<>(this.getBytes(), headers, HttpStatus.OK);
    }

    private HttpHeaders buildHeaders(TileResult tileRequest) {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(tileRequest.mimeTypeToType());


        headers.setCacheControl("public, max-age=86400");


        headers.setLastModified(tileRequest.getLastModified());


        if (tileRequest.getSize() > 0) {
            headers.setContentLength(tileRequest.getSize());
        }

        return headers;
    }
}
