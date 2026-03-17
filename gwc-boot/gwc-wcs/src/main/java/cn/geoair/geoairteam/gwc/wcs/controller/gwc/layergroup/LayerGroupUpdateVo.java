package cn.geoair.geoairteam.gwc.wcs.controller.gwc.layergroup;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import cn.geoair.base.data.model.annotation.GaModel;
import cn.geoair.base.data.model.annotation.GaModelField;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * 修改${tableComment}(LayerGroup)
 *
 * @author geoairteam
 * @date 2026-03-11 15:59:37
 */
@Data
@GaModel(text = "修改${tableComment}")
public class LayerGroupUpdateVo {

@NotBlank(message = "主键不能为空")
@GaModelField(text = "主键", isID = true)
private String id;
 @GaModelField(text = "图层组名称")
private String groupName;
 @JSONField(format = "yyyy-MM-dd HH:mm:ss")
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
@GaModelField(text = "创建时间")
private Date timeCreate;
 @GaModelField(text = "默认预览地址")
private String previewUrl;

    public static LayerGroupUpdateVo empty() {
        return new LayerGroupUpdateVo();
    }

    public LayerGroupUpdateVo copy() {
            LayerGroupUpdateVo copy = new LayerGroupUpdateVo();
        BeanUtil.copyProperties(this, copy);
        return copy;
    }
}
