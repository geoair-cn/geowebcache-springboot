package cn.geoair.geoairteam.gwc.wcs.controller.sys.configproperty;

import cn.hutool.core.collection.ListUtil;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import cn.geoair.base.data.model.annotation.GaModel;
import cn.geoair.base.data.model.annotation.GaModelField;
import cn.geoair.base.util.GutilObject;

import cn.geoair.geoairteam.gwc.model.sys.dto.ConfigPropertyDto;
import cn.geoair.geoairteam.gwc.model.sys.entity.ConfigPropertyPo;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;


/**
 * 配置属性表对象 om_config_property
 *
 * @author zhangjun
 * @date 2024-09-04
 */
@GaModel(text = "删除配置属性表")
public class ConfigPropertyDetailVo {


    @NotBlank(message = "主键不能为空")
    @GaModelField(text = "主键", isID = true)
    private String id;


    @GaModelField(text = "key")
    private String key;


    @GaModelField(text = "值")
    private String value;


    @GaModelField(text = "备注")
    private String remark;


    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @GaModelField(text = "更新时间")
    private Date updateTime;


    @GaModelField(text = "组名")
    private String groupName;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }


    public static ConfigPropertyDetailVo fromDto(ConfigPropertyDto dto) {
        ConfigPropertyDetailVo vo = new ConfigPropertyDetailVo();
        BeanUtils.copyProperties(dto, vo);
        return vo;
    }

    public static ConfigPropertyDetailVo fromPo(ConfigPropertyPo po) {
        ConfigPropertyDetailVo vo = new ConfigPropertyDetailVo();
        BeanUtils.copyProperties(po, vo);
        return vo;
    }

    public static List<ConfigPropertyDetailVo> fromDtos(Iterable<ConfigPropertyDto> dtos) {
        if (GutilObject.isNotEmpty(dtos)) {
            List<ConfigPropertyDetailVo> list = ListUtil.list(false);
            for (ConfigPropertyDto dto : dtos) {
                list.add(fromDto(dto));
            }
            return list;
        }
        return ListUtil.empty();
    }

    public static List<ConfigPropertyDetailVo> fromPos(Iterable<ConfigPropertyPo> pos) {
        if (GutilObject.isNotEmpty(pos)) {
            List<ConfigPropertyDetailVo> list = ListUtil.list(false);
            for (ConfigPropertyPo po : pos) {
                list.add(fromPo(po));
            }
            return list;
        }
        return ListUtil.empty();
    }

}
