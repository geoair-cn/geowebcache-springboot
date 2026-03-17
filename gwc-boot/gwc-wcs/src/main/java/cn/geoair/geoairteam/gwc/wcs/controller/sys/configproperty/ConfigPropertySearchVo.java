package cn.geoair.geoairteam.gwc.wcs.controller.sys.configproperty;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import cn.geoair.base.data.model.annotation.GaModel;
import cn.geoair.base.data.model.annotation.GaModelField;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;


/**
 * 配置属性表对象 om_config_property
 *
 * @author zhangjun
 * @date 2024-09-04
 */
@GaModel(text = "查询配置属性表")
@JsonPropertyOrder(value = {"queryContent"})
public class ConfigPropertySearchVo {

    @GaModelField(text = "查询的字符串")
    private String queryContent;

    public String getQueryContent() {
        return queryContent;
    }

    public void setQueryContent(String queryContent) {
        this.queryContent = queryContent;
    }


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
