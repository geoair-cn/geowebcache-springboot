package cn.geoair.geoairteam.gwc.model.gwc.dto;

import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerGroupPo;
import cn.geoair.base.data.model.annotation.GaModel;
import cn.hutool.core.bean.BeanUtil;
import lombok.Data;

/**
 * ${tableComment}(LayerGroup)DTO
 *
 * @author geoairteam
 * @date 2026-03-11 15:59:37
 */
@GaModel(text = "${tableComment}DTO" )
@Data
public class LayerGroupDto extends LayerGroupPo {
    private static final long serialVersionUID = 1773215977761L;

    public static LayerGroupDto empty() {
        return new LayerGroupDto();
    }

    public   LayerGroupDto copy() {
        LayerGroupDto copy = new LayerGroupDto();
        BeanUtil.copyProperties(this, copy);
        return copy;
    }

    public static LayerGroupDto ofLayerGroupPo(LayerGroupPo source) {
         LayerGroupDto target = new LayerGroupDto();
        BeanUtil.copyProperties(source, target);
        return target;
    }

    public static LayerGroupPo toPo(LayerGroupDto source) {
         LayerGroupPo target = new LayerGroupPo();
        BeanUtil.copyProperties(source, target);
        return target;
    }
}
