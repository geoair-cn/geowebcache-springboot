package cn.geoair.geoairteam.gwc.model.gwc.dto;

import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerInfoPo;
import cn.geoair.base.data.model.annotation.GaModel;
import cn.hutool.core.bean.BeanUtil;
import lombok.Data;

/**
 * 图层信息表(LayerInfo)DTO
 *
 * @author geoairteam
 * @date 2026-03-11 15:45:16
 */
@GaModel(text = "图层信息表DTO")
@Data
public class LayerInfoDto extends LayerInfoPo {
    private static final long serialVersionUID = 1773215116578L;

    public static LayerInfoDto empty() {
        return new LayerInfoDto();
    }

    public LayerInfoDto copy() {
        LayerInfoDto copy = new LayerInfoDto();
        BeanUtil.copyProperties(this, copy);
        return copy;
    }

    public static LayerInfoDto ofLayerInfoPo(LayerInfoPo source) {
        LayerInfoDto target = new LayerInfoDto();
        BeanUtil.copyProperties(source, target);
        return target;
    }

    public static LayerInfoPo toPo(LayerInfoDto source) {
        LayerInfoPo target = new LayerInfoPo();
        BeanUtil.copyProperties(source, target);
        return target;
    }
}
