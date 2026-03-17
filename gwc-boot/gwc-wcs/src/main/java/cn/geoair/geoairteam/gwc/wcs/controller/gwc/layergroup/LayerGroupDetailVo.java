package cn.geoair.geoairteam.gwc.wcs.controller.gwc.layergroup;

import com.alibaba.fastjson.annotation.JSONField;
import cn.hutool.core.collection.ListUtil;
import cn.geoair.base.util.GutilObject;
import org.springframework.beans.BeanUtils;
import cn.geoair.base.data.model.annotation.GaModel;
import cn.geoair.base.data.model.annotation.GaModelField;
import cn.geoair.geoairteam.gwc.model.gwc.dto.LayerGroupDto;
import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerGroupPo;
import org.springframework.format.annotation.DateTimeFormat;
import javax.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.List;
import java.util.Date;

/**
 * ${tableComment}详情VO tgwc_layer_group
 *
 * @author geoairteam
 * @date 2026-03-11 15:59:37
 */
@Data
@GaModel(text = "${tableComment}详情")
public class LayerGroupDetailVo {

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


    public static LayerGroupDetailVo fromDto(LayerGroupDto dto) {
        return fromPo(dto);
    }

    public static LayerGroupDetailVo fromPo(LayerGroupPo po) {
            LayerGroupDetailVo vo = new LayerGroupDetailVo();
        BeanUtils.copyProperties(po, vo);
        return vo;
    }

    public static List<LayerGroupDetailVo> fromDtos(Iterable<LayerGroupDto> dtos) {
        if (GutilObject.isEmpty(dtos)) {
            return ListUtil.empty();
        }

        List<LayerGroupDetailVo> list = ListUtil.list(false);
        for (LayerGroupDto dto : dtos) {
            list.add(fromDto(dto));
        }
        return list;
    }

    public static List<LayerGroupDetailVo> fromPos(Iterable<LayerGroupPo> pos) {
        if (GutilObject.isEmpty(pos)) {
            return ListUtil.empty();
        }

        List<LayerGroupDetailVo> list = ListUtil.list(false);
        for (LayerGroupPo po : pos) {
            list.add(fromPo(po));
        }
        return list;
    }
}
