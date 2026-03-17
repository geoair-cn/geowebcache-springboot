package cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext.dto;

import cn.geoair.base.util.GutilObject;
import cn.geoair.web.GirWeb;
import cn.hutool.core.util.StrUtil;
import cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext.RequestParamter;
import lombok.Data;
import org.geowebcache.conveyor.ConveyorTile;
import org.geowebcache.util.ServletUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author ：张逢吉
 * @date ：Created in   10:43
 * @description： TODO
 */
@Data
public class XyzDto {

    private Integer gridSrid;

    private String xStr;
    private String yStr;
    private String zStr;
    private Long x;
    private Long y;
    private Long z;


    public String[] getXyzArray() {
        return new String[]{xStr, yStr, zStr};
    }

    public String getXyzPath() {
        return String.format("zxy=%s/%s/%s", zStr, xStr, yStr);
    }

    public static XyzDto byRequestParamter() {
        HttpServletRequest request = GirWeb.getRequest();
        String encoding = request.getCharacterEncoding();
        Map<String, String> values = ServletUtils.selectedStringsFromMap(
                request.getParameterMap(),
                encoding,
                RequestParamter.getRequestParamterCodes());
        String tilematrix = values.get(RequestParamter.tilematrix.getCode());
        String[] split = tilematrix.split(":"); // z值
        if (split.length > 1) {
            tilematrix = split[split.length - 1];

        }
        int i = Integer.parseInt(tilematrix);
        String xstring = values.get("TileCol");
        String ystring = values.get("TileRow");

        Long x = Long.parseLong(xstring);
        Long y1 = Long.parseLong(ystring);
        Long z = Long.parseLong(String.valueOf(i));
        XyzDto xyzDto = new XyzDto();
        xyzDto.setX(x);
        xyzDto.setY(y1);
        xyzDto.setZ(z);
        xyzDto.setXStr(xstring);
        xyzDto.setYStr(ystring);
        xyzDto.setZStr(tilematrix);

        String tilematrixset = values.get(RequestParamter.tilematrixset.getCode());
        if(GutilObject.isNotEmpty(tilematrixset)){
            if (tilematrixset.contains("4326") || tilematrixset.contains("4490")) {
                xyzDto.setGridSrid(4326);
            } else if ("c".equals(tilematrixset)) {
                xyzDto.setGridSrid(4326);
            } else if ("w".equals(tilematrixset)) {
                xyzDto.setGridSrid(3857);
            } else if (tilematrixset.contains("900913")) {
                xyzDto.setGridSrid(3857);
            } else {
                try {
                    List<String> split1 = StrUtil.split(tilematrixset, ':', 2);
                    xyzDto.setGridSrid(Integer.parseInt(split1.get(1)));
                } catch (Exception e) {
                    xyzDto.setGridSrid(4326);
                }
            }
        }


        return xyzDto;


    }

    public static XyzDto byConveyorTile(ConveyorTile tile) {
        long[] tileIndex = tile.getTileIndex();
        Long x = tileIndex[0];
        Long y1 = tileIndex[1];
        Long z = tileIndex[2];
        XyzDto xyzDto = new XyzDto();
        xyzDto.setX(x);
        xyzDto.setY(y1);
        xyzDto.setZ(z);
        xyzDto.setXStr(x.toString());
        xyzDto.setYStr(y1.toString());
        xyzDto.setZStr(z.toString());
        String gridSetId = tile.getGridSetId();
        if ("EPSG:4326".equals(gridSetId) || "EPSG:4490".equals(gridSetId)) {
            xyzDto.setGridSrid(4326);
        } else {
            xyzDto.setGridSrid(3857);
        }
        return xyzDto;


    }

    public static XyzDto byXYZ(String xStr, String yStr, String zStr) {
        Long x = Long.parseLong(xStr);
        Long y1 = Long.parseLong(yStr);
        Long z = Long.parseLong(zStr);
        XyzDto xyzDto = new XyzDto();
        xyzDto.setX(x);
        xyzDto.setY(y1);
        xyzDto.setZ(z);
        xyzDto.setXStr(x.toString());
        xyzDto.setYStr(y1.toString());
        xyzDto.setZStr(z.toString());
        return xyzDto;


    }

    public static XyzDto byXYZ(Integer xInt, Integer yInt, Integer zInt, Integer gridSrid) {
        Long x = Long.parseLong(String.valueOf(xInt));
        Long y1 = Long.parseLong(String.valueOf(yInt));
        Long z = Long.parseLong(String.valueOf(zInt));
        XyzDto xyzDto = new XyzDto();
        xyzDto.setX(x);
        xyzDto.setY(y1);
        xyzDto.setZ(z);
        xyzDto.setXStr(x.toString());
        xyzDto.setYStr(y1.toString());
        xyzDto.setZStr(z.toString());
        xyzDto.setGridSrid(gridSrid);
        return xyzDto;


    }
}
