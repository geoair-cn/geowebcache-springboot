package cn.geoair.geoairteam.gwc.model.sys.entity;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import cn.geoair.base.data.common.GemDatePattern;
import cn.geoair.base.data.model.annotation.GaModel;
import cn.geoair.base.data.model.annotation.GaModelField;
import cn.geoair.base.gpa.entity.GiCrudEntity;
import cn.geoair.base.gpa.id.GiEntityIdGenerator;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;


/**
 * 配置属性表(ConfigProperty)实体类
 *
 * @author zhangjun
 * @date 2024-09-04
 */
@GaModel(text = "配置属性表")
@Table(name = "config_property")
public class ConfigPropertyPo implements GiCrudEntity<String>, GiEntityIdGenerator<String> {

    public static ConfigPropertyPo empty() {
        return new ConfigPropertyPo();
    }

    public ConfigPropertyPo copy() {
        ConfigPropertyPo po = empty();
        BeanUtil.copyProperties(this, po);
        return po;
    }


    private static final long serialVersionUID = 1725436188396L;
    @Id
    @Column(name = "id")
    @GaModelField(text = "主键", isID = true)
    private String id;

    @Column(name = "key")
    @GaModelField(text = "key")
    private String key;

    @Column(name = "value")
    @GaModelField(text = "值")
    private String value;

    @Column(name = "remark")
    @GaModelField(text = "备注")
    private String remark;

    @Column(name = "update_time")
    @GaModelField(text = "更新时间", datePattern = GemDatePattern.ISO8601Long)
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date updateTime;

    @Column(name = "group_name")
    @GaModelField(text = "组名")
    private String groupName;

    public ConfigPropertyPo() {
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

    public ConfigPropertyPo(String id) {
        if (id == null) {
            id = this.generatorId();
        }
        this.id = id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }


    public String id() {
        return id;
    }

    public String generatorId() {
        return UUID.randomUUID().toString();
    }

    public void setNotDel() {
    }

    public void setDel() {
    }

    public void initCreateMeta() {
        setNotDel();
    }

    public void initUpdateMeta() {
        setNotDel();
        setUpdateTime(new Date());
    }
}
