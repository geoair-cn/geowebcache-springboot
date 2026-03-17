package cn.geoair.geoairteam.gwc.wcs.controller.gwc.layergroupalias;

import com.alibaba.fastjson.annotation.JSONField;
import cn.geoair.base.data.model.annotation.GaModel;
import cn.geoair.base.data.model.annotation.GaModelField;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.Data;

                    
/**
 * 新增图层组别名表(LayerGroupAlias)
 *
 * @author geoairteam
 * @date 2026-03-11 15:44:18
 */
@Data
@GaModel(text = "新增图层组别名表")
public class LayerGroupAliasAddVo {

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
