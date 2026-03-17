package cn.geoair.geoairteam.gwc.wcs.controller.gwc.layergroup;

import com.alibaba.fastjson.annotation.JSONField;
import cn.geoair.base.data.model.annotation.GaModel;
import cn.geoair.base.data.model.annotation.GaModelField;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.Data;
import java.util.Date;

/**
 * 新增${tableComment}(LayerGroup)
 *
 * @author geoairteam
 * @date 2026-03-11 15:59:37
 */
@Data
@GaModel(text = "新增${tableComment}")
public class LayerGroupAddVo {

@GaModelField(text = "主键", isID = true)
private String id;

@GaModelField(text = "图层组名称")
private String groupName;

@JSONField(format = "yyyy-MM-dd HH:mm:ss")
@GaModelField(text = "创建时间")
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
private Date timeCreate;

@GaModelField(text = "默认预览地址")
private String previewUrl;

}
