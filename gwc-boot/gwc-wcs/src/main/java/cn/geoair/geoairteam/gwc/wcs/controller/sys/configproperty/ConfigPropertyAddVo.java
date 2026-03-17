package cn.geoair.geoairteam.gwc.wcs.controller.sys.configproperty;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import cn.geoair.base.data.model.annotation.GaModel;
import cn.geoair.base.data.model.annotation.GaModelField;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.util.Date;


/**
 * 新增配置属性表(ConfigProperty)
 *
 * @author zhangjun
 * @date 2024-09-04
 */
@GaModel(text = "新增配置属性表")
public class ConfigPropertyAddVo {


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
    @GaModelField(text = "更新时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;


    @GaModelField(text = "组名")
    private String groupName;


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

}
