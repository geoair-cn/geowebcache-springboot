package cn.geoair.geoairteam.gwc.wcs.controller.gwc.layergroupalias;

import com.alibaba.fastjson.annotation.JSONField;
import cn.hutool.core.collection.ListUtil;
import cn.geoair.base.util.GutilObject;
import org.springframework.beans.BeanUtils;
import cn.geoair.base.data.model.annotation.GaModel;
import cn.geoair.base.data.model.annotation.GaModelField;
import cn.geoair.geoairteam.gwc.model.gwc.dto.LayerGroupAliasDto;
import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerGroupAliasPo;

import javax.validation.constraints.NotBlank;

import lombok.Data;
import java.util.List;

                    
/**
 * 图层组别名表详情VO tgwc_layer_group_alias
 *
 * @author geoairteam
 * @date 2026-03-11 15:44:18
 */
@Data
@GaModel(text = "图层组别名表详情")
public class LayerGroupAliasDetailVo {

@NotBlank(message = "主键不能为空")
@GaModelField(text = "主键", isID = true)
private String id;

@GaModelField(text = "图层组别名")
private String layerGroupAlias;

@GaModelField(text = "图层排序字符串")
private String layerString;

@GaModelField(text = "备注")
private String remark;

@GaModelField(text = "wmts的访问地址")
private String wmtsUrl;


    public static LayerGroupAliasDetailVo fromDto(LayerGroupAliasDto dto) {
        return fromPo(dto);
    }

    public static LayerGroupAliasDetailVo fromPo(LayerGroupAliasPo po) {
            LayerGroupAliasDetailVo vo = new LayerGroupAliasDetailVo();
        BeanUtils.copyProperties(po, vo);
        return vo;
    }

    public static List<LayerGroupAliasDetailVo> fromDtos(Iterable<LayerGroupAliasDto> dtos) {
        if (GutilObject.isEmpty(dtos)) {
            return ListUtil.empty();
        }

        List<LayerGroupAliasDetailVo> list = ListUtil.list(false);
        for (LayerGroupAliasDto dto : dtos) {
            list.add(fromDto(dto));
        }
        return list;
    }

    public static List<LayerGroupAliasDetailVo> fromPos(Iterable<LayerGroupAliasPo> pos) {
        if (GutilObject.isEmpty(pos)) {
            return ListUtil.empty();
        }

        List<LayerGroupAliasDetailVo> list = ListUtil.list(false);
        for (LayerGroupAliasPo po : pos) {
            list.add(fromPo(po));
        }
        return list;
    }
}
