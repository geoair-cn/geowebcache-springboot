package cn.geoair.geoairteam.gwc.wcs.controller.gwc.layergroup;
import cn.hutool.core.bean.BeanUtil;
import lombok.Data;
import com.alibaba.fastjson.annotation.JSONField;
import cn.geoair.base.data.model.annotation.GaModel;
import cn.geoair.base.data.model.annotation.GaModelField;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.NotBlank;
import java.util.Date;


/**
 * ${tableComment}对象 tgwc_layer_group
 *
 * @author geoairteam
 * @date 2026-03-11 15:59:37
 */
@Data
@GaModel(text = "查询${tableComment}")
@JsonPropertyOrder(value = {"queryContent"})
public class LayerGroupSearchVo {
    public static LayerGroupSearchVo empty() {
        return new LayerGroupSearchVo();
    }

    public   LayerGroupSearchVo copy() {
         LayerGroupSearchVo copy =   new LayerGroupSearchVo();
        BeanUtil.copyProperties(this, copy);
        return copy;
    }
    @GaModelField(text = "查询的字符串")
    private String queryContent;

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


}
