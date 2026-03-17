package cn.geoair.geoairteam.gwc.wcs.controller.gwc.layerinfo;

import com.alibaba.fastjson.annotation.JSONField;
import cn.hutool.core.collection.ListUtil;
import cn.geoair.base.util.GutilObject;
import org.springframework.beans.BeanUtils;
import cn.geoair.base.data.model.annotation.GaModel;
import cn.geoair.base.data.model.annotation.GaModelField;
import cn.geoair.geoairteam.gwc.model.gwc.dto.LayerInfoDto;
import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerInfoPo;

import javax.validation.constraints.NotBlank;

import lombok.Data;
import java.util.List;

                                                                                
/**
 * 图层信息表详情VO tgwc_layer_info
 *
 * @author geoairteam
 * @date 2026-03-11 15:45:16
 */
@Data
@GaModel(text = "图层信息表详情")
public class LayerInfoDetailVo {

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


    public static LayerInfoDetailVo fromDto(LayerInfoDto dto) {
        return fromPo(dto);
    }

    public static LayerInfoDetailVo fromPo(LayerInfoPo po) {
            LayerInfoDetailVo vo = new LayerInfoDetailVo();
        BeanUtils.copyProperties(po, vo);
        return vo;
    }

    public static List<LayerInfoDetailVo> fromDtos(Iterable<LayerInfoDto> dtos) {
        if (GutilObject.isEmpty(dtos)) {
            return ListUtil.empty();
        }

        List<LayerInfoDetailVo> list = ListUtil.list(false);
        for (LayerInfoDto dto : dtos) {
            list.add(fromDto(dto));
        }
        return list;
    }

    public static List<LayerInfoDetailVo> fromPos(Iterable<LayerInfoPo> pos) {
        if (GutilObject.isEmpty(pos)) {
            return ListUtil.empty();
        }

        List<LayerInfoDetailVo> list = ListUtil.list(false);
        for (LayerInfoPo po : pos) {
            list.add(fromPo(po));
        }
        return list;
    }
}
