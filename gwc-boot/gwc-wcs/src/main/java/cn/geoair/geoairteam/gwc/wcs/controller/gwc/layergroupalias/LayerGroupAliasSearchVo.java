package cn.geoair.geoairteam.gwc.wcs.controller.gwc.layergroupalias;
import cn.hutool.core.bean.BeanUtil;
import lombok.Data;
import com.alibaba.fastjson.annotation.JSONField;
import cn.geoair.base.data.model.annotation.GaModel;
import cn.geoair.base.data.model.annotation.GaModelField;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.NotBlank;


/**
 * 图层组别名表对象 tgwc_layer_group_alias
 *
 * @author geoairteam
 * @date 2026-03-11 15:44:18
 */
@Data
@GaModel(text = "查询图层组别名表")
@JsonPropertyOrder(value = {"queryContent"})
public class LayerGroupAliasSearchVo {
    public static LayerGroupAliasSearchVo empty() {
        return new LayerGroupAliasSearchVo();
    }

    public   LayerGroupAliasSearchVo copy() {
         LayerGroupAliasSearchVo copy =   new LayerGroupAliasSearchVo();
        BeanUtil.copyProperties(this, copy);
        return copy;
    }
    @GaModelField(text = "查询的字符串")
    private String queryContent;

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


}
