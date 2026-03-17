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
 * 图层信息表(LayerInfo)实体类
 *
 * @author geoairteam
 * @date 2026-03-11 15:45:16
 */
@GaModel(text = "图层信息表")
@Data
@Table(name = "tgwc_layer_info")
public class LayerInfoPo implements GiCrudEntity<String>, GiEntityIdGenerator<String> {

    private static final long serialVersionUID = 1773215116557L;

    @Id
    @Column(name = "id")
    @GaModelField(text = "主键", isID = true)
    private String id;

    @Column(name = "layer_name")
    @GaModelField(text = "图层名称")
    private String layerName;
    @Column(name = "wkid")
    @GaModelField(text = "坐标系")
    private Integer wkid;
    @Column(name = "arc_layer_type")
    @GaModelField(text = "arcgis图层类型")
    private String arcLayerType;
    @Column(name = "tiling_scheme")
    @GaModelField(text = "瓦片配置文件")
    private String tilingScheme;
    @Column(name = "tile_cache_path")
    @GaModelField(text = "瓦片地址")
    private String tileCachePath;
    @Column(name = "group_id")
    @GaModelField(text = "组id")
    private String groupId;
    @Column(name = "sort_key")
    @GaModelField(text = "排序键")
    private String sortKey;
    @Column(name = "enable_is")
    @GaModelField(text = "是否启用有效")
    private String enableIs;
    @Column(name = "preview_url")
    @GaModelField(text = "预览地址")
    private String previewUrl;
    @Column(name = "minx")
    @GaModelField(text = "BoundingBox")
    private Double minx;
    @Column(name = "miny")
    @GaModelField(text = "BoundingBox")
    private Double miny;
    @Column(name = "maxx")
    @GaModelField(text = "BoundingBox")
    private Double maxx;
    @Column(name = "maxy")
    @GaModelField(text = "BoundingBox")
    private Double maxy;
    @Column(name = "storage_format")
    @GaModelField(text = "切片规范")
    private String storageFormat;
    @Column(name = "time_create")
    @GaModelField(text = "创建时间")
    private Date timeCreate;
    @Column(name = "tile_origin")
    @GaModelField(text = "切片方案中心店")
    private String tileOrigin;
    @Column(name = "dpi")
    @GaModelField(text = "切片方案分辨率")
    private String dpi;
    @Column(name = "hex_zoom")
    @GaModelField(text = "一个参数")
    private String hexZoom;


    public LayerInfoPo() {
    }

    public LayerInfoPo(String id) {
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

    public static LayerInfoPo empty() {
        return new LayerInfoPo();
    }

    public LayerInfoPo copy() {
        LayerInfoPo copy = new LayerInfoPo();
        BeanUtil.copyProperties(this, copy);
        return copy;
    }
}
