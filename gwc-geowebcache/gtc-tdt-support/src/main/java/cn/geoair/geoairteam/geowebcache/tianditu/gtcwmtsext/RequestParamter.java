package cn.geoair.geoairteam.geowebcache.tianditu.gtcwmtsext;


import cn.geoair.base.data.GiVisualValuable;

/**
 * @author ：zhangjun
 * @date ：Created in 2023/12/5 11:33
 * @description： 请求入参
 */
public enum RequestParamter implements GiVisualValuable<String> {
    layer("layer"),
    request("request"),
    style("style"),
    format("format"),
    infoformat("infoformat"),
    tilematrixset("tilematrixset"),
    tilematrix("tilematrix"),
    tilerow("tilerow"),
    tilecol("tilecol"),
    tileformat("tileformat"),
    i(" i"),
    j("j"),
    ;

    public String getCode() {
        return code;
    }

    RequestParamter(String code) {
        this.code = code;
    }

    private String code;

    public static String[] keys = {
            "layer",
            "request",
            "style",
            "format",
            "infoformat",
            "tilematrixset",
            "tilematrix",
            "tilerow",
            "tilecol",
            "tileformat",
            "i",
            "j"
    };

    public static String[] getRequestParamterCodes() {
        RequestParamter[] values = RequestParamter.values();
        String[] keys = new String[values.length];
        for (int i1 = 0; i1 < values.length; i1++) {
            keys[i1] = values[i1].code;
        }
        return keys;
    }
}
