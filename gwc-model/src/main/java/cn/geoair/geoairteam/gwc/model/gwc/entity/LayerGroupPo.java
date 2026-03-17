package cn.geoair.geoairteam.gwc.model.gwc.entity;

import cn.hutool.core.bean.BeanUtil;
import lombok.Data;
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
 * ${tableComment}(LayerGroup)实体类
 *
 * @author geoairteam
 * @date 2026-03-11 15:59:37
 */
@GaModel(text = "${tableComment}")
@Data
@Table(name = "tgwc_layer_group")
public class LayerGroupPo implements GiCrudEntity<String>, GiEntityIdGenerator<String> {

    private static final long serialVersionUID = 1773215977737L;

    @Id
    @Column(name = "id")
    @GaModelField(text = "主键", isID = true)
    private String id;

    @Column(name = "group_name")
    @GaModelField(text = "图层组名称")
    private String groupName;
    @Column(name = "time_create")
    @GaModelField(text = "创建时间", datePattern = GemDatePattern.ISO8601Long)
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date timeCreate;
    @Column(name = "preview_url")
    @GaModelField(text = "默认预览地址")
    private String previewUrl;

    public LayerGroupPo() {
    }

    public LayerGroupPo(String id) {
        if (id == null) {
            id = this.generatorId();
        }
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String generatorId() {
        return UUID.randomUUID().toString();
    }

    public void setNotDel() {
    }

    public void setDel() {
    }

    public void initCreateMeta() {
        setNotDel();
        // setTimeCreate(new Date());
    }

    public void initUpdateMeta() {
        setNotDel();
        // setTimeUpdate(new Date());
    }

    public static LayerGroupPo empty() {
        return new LayerGroupPo();
    }

    public LayerGroupPo copy() {
        LayerGroupPo copy = new LayerGroupPo();
        BeanUtil.copyProperties(this, copy);
        return copy;
    }
}
