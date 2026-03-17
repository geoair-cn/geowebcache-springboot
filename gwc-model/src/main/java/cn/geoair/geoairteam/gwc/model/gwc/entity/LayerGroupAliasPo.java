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
import java.util.UUID;

/**
 * 图层组别名表(LayerGroupAlias)实体类
 *
 * @author geoairteam
 * @date 2026-03-11 15:44:18
 */
@GaModel(text = "图层组别名表")
@Data
@Table(name = "tgwc_layer_group_alias")
public class LayerGroupAliasPo implements GiCrudEntity<String>, GiEntityIdGenerator<String> {

    private static final long serialVersionUID = 1773215058699L;

    @Id
    @Column(name = "id")
    @GaModelField(text = "主键", isID = true)
    private String id;

    @Column(name = "layer_group_alias")
    @GaModelField(text = "图层组别名")
    private String layerGroupAlias;
    @Column(name = "layer_string")
    @GaModelField(text = "图层排序字符串")
    private String layerString;
    @Column(name = "remark")
    @GaModelField(text = "备注")
    private String remark;
    @Column(name = "wmts_url")
    @GaModelField(text = "wmts的访问地址")
    private String wmtsUrl;

    public LayerGroupAliasPo() {
    }

    public LayerGroupAliasPo(String id) {
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

    public static LayerGroupAliasPo empty() {
        return new LayerGroupAliasPo();
    }

    public LayerGroupAliasPo copy() {
        LayerGroupAliasPo copy = new LayerGroupAliasPo();
        BeanUtil.copyProperties(this, copy);
        return copy;
    }
}
