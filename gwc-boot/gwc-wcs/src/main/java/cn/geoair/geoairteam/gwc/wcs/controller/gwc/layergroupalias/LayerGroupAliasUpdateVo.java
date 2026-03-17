package cn.geoair.geoairteam.gwc.wcs.controller.gwc.layergroupalias;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.annotation.JSONField;
import cn.geoair.base.data.model.annotation.GaModel;
import cn.geoair.base.data.model.annotation.GaModelField;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 修改图层组别名表(LayerGroupAlias)
 *
 * @author geoairteam
 * @date 2026-03-11 15:44:18
 */
@Data
@GaModel(text = "修改图层组别名表")
public class LayerGroupAliasUpdateVo {

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
 
    public static LayerGroupAliasUpdateVo empty() {
        return new LayerGroupAliasUpdateVo();
    }

    public LayerGroupAliasUpdateVo copy() {
            LayerGroupAliasUpdateVo copy = new LayerGroupAliasUpdateVo();
        BeanUtil.copyProperties(this, copy);
        return copy;
    }
}
