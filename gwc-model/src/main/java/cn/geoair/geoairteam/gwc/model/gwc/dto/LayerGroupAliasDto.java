package cn.geoair.geoairteam.gwc.model.gwc.dto;

import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerGroupAliasPo;
import cn.geoair.base.data.model.annotation.GaModel;
import cn.hutool.core.bean.BeanUtil;
import lombok.Data;

/**
 * 图层组别名表(LayerGroupAlias)DTO
 *
 * @author geoairteam
 * @date 2026-03-11 15:44:18
 */
@GaModel(text = "图层组别名表DTO" )
@Data
public class LayerGroupAliasDto extends LayerGroupAliasPo {
    private static final long serialVersionUID = 1773215058717L;

    public static LayerGroupAliasDto empty() {
        return new LayerGroupAliasDto();
    }

    public   LayerGroupAliasDto copy() {
        LayerGroupAliasDto copy = new LayerGroupAliasDto();
        BeanUtil.copyProperties(this, copy);
        return copy;
    }

    public static LayerGroupAliasDto ofLayerGroupAliasPo(LayerGroupAliasPo source) {
         LayerGroupAliasDto target = new LayerGroupAliasDto();
        BeanUtil.copyProperties(source, target);
        return target;
    }

    public static LayerGroupAliasPo toPo(LayerGroupAliasDto source) {
         LayerGroupAliasPo target = new LayerGroupAliasPo();
        BeanUtil.copyProperties(source, target);
        return target;
    }
}
