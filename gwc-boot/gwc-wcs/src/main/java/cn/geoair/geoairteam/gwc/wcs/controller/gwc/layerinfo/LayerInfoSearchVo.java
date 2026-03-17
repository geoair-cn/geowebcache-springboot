package cn.geoair.geoairteam.gwc.wcs.controller.gwc.layerinfo;
import cn.hutool.core.bean.BeanUtil;
import lombok.Data;
import com.alibaba.fastjson.annotation.JSONField;
import cn.geoair.base.data.model.annotation.GaModel;
import cn.geoair.base.data.model.annotation.GaModelField;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.NotBlank;


/**
 * 图层信息表对象 tgwc_layer_info
 *
 * @author geoairteam
 * @date 2026-03-11 15:45:16
 */
@Data
@GaModel(text = "查询图层信息表")
@JsonPropertyOrder(value = {"queryContent"})
public class LayerInfoSearchVo {
    public static LayerInfoSearchVo empty() {
        return new LayerInfoSearchVo();
    }

    public   LayerInfoSearchVo copy() {
         LayerInfoSearchVo copy =   new LayerInfoSearchVo();
        BeanUtil.copyProperties(this, copy);
        return copy;
    }
    @GaModelField(text = "查询的字符串")
    private String queryContent;

    @NotBlank(message = "主键不能为空")
  @GaModelField(text = "主键", isID = true)
    private String id;

    @GaModelField(text = "图层名称")
    private String layerName;

    @GaModelField(text = "坐标系")
    private Integer wkid;

    @GaModelField(text = "arcgis图层类型")
    private String arcLayerType;

    @GaModelField(text = "瓦片配置文件")
    private String tilingScheme;

    @GaModelField(text = "瓦片地址")
    private String tileCachePath;

    @GaModelField(text = "组id")
    private String groupId;

    @GaModelField(text = "排序键")
    private String sortKey;

    @GaModelField(text = "是否启用有效")
    private String enableIs;

    @GaModelField(text = "预览地址")
    private String previewUrl;

    @GaModelField(text = "BoundingBox")
    private Long minx;

    @GaModelField(text = "BoundingBox")
    private Long miny;

    @GaModelField(text = "BoundingBox")
    private Long maxx;

    @GaModelField(text = "BoundingBox")
    private Long maxy;

    @GaModelField(text = "切片规范")
    private String storageFormat;

    @GaModelField(text = "创建时间")
    private String timeCreate;

    @GaModelField(text = "切片方案中心店")
    private String tileOrigin;

    @GaModelField(text = "切片方案分辨率")
    private String dpi;

    @GaModelField(text = "一个参数")
    private String hexZoom;

    @GaModelField(text = "边界范围")
    private String theGeom;


}
