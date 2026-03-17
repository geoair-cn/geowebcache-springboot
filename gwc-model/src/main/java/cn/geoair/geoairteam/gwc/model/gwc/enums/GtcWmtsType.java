package cn.geoair.geoairteam.gwc.model.gwc.enums;



import cn.geoair.base.data.GiVisualValuable;
import cn.geoair.base.data.model.annotation.GaModelField;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ：zfj
 * @date ：Created in 2023/6/28 14:27
 * @description： TODO
 */
public enum GtcWmtsType implements GiVisualValuable<String> {
    singletonLayer("singletonLayer", "layer"),
    groupLayer("groupLayer", "group"),
    xyzLayer("xyzLayer", "xyz"),
    tmsLayer("tmsLayer", "tms"),
    mixtureGroupLayer("mixtureGroupLayer", "layer_group"),
    otherLayer("otherLayer", ""),
    debugGridLayer("debugGridLayer", ""),
    ;

    @GaModelField(isID = true)
    private String code;

    private String suffix;

    public String getSuffix() {
        return suffix;
    }

    GtcWmtsType(String code, String suffix) {
        this.code = code;
        this.suffix = suffix;
    }

    public static String getDisPlayByCode(String code) {
        for (GtcWmtsType f : GtcWmtsType.values()) {
            if (f.getCode().equals(code)) {
                return f.display();
            }
        }
        return null;
    }

    public String getCode() {
        return this.code;
    }

    @Override
    public String display() {
        return this.name();
    }

    @Override
    public String value() {
        return this.code;
    }

    public static List<String> getAllDisplay() {
        List<String> displays = new ArrayList<>();
        for (GtcWmtsType f : GtcWmtsType.values()) {
            displays.add(f.display());
        }
        return displays;
    }
}
