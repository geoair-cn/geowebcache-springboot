package cn.geoair.geoairteam.gwc.model.sys.dto;

import cn.geoair.base.data.model.annotation.GaModel;
import cn.geoair.geoairteam.gwc.model.sys.entity.ConfigPropertyPo;


import static cn.hutool.core.bean.BeanUtil.copyProperties;

/**
 * 配置属性表(ConfigProperty)DTO
 *
 * @author zhangjun
 * @date 2024-09-04
 */
@GaModel(text = "配置属性表DTO")
public class ConfigPropertyDto extends ConfigPropertyPo {
    private static final long serialVersionUID = 1725436188411L;

    public static ConfigPropertyDto empty() {
        return new ConfigPropertyDto();
    }


    public ConfigPropertyDto copy() {
        ConfigPropertyDto po = empty();
        copyProperties(this, po);
        return po;
    }

    public static ConfigPropertyDto ofConfigPropertyPo(ConfigPropertyPo source) {
        ConfigPropertyDto target = new ConfigPropertyDto();
        copyProperties(source, target);
        return target;
    }

    public static ConfigPropertyPo toPo(ConfigPropertyDto source) {
        ConfigPropertyPo target = new ConfigPropertyPo();
        copyProperties(source, target);
        return target;
    }
}
