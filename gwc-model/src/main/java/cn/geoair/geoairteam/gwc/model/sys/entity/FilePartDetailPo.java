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
 * 文件分片信息表实体类，仅在手动分片上传时使用
 *
 * @author your name
 * @date 2024-01-01
 */
@GaModel(text = "文件分片信息表")
@Table(name = "file_part_detail")
public class FilePartDetailPo implements GiCrudEntity<String>, GiEntityIdGenerator<String> {

    public static FilePartDetailPo empty() {
        return new FilePartDetailPo();
    }

    public FilePartDetailPo copy() {
        FilePartDetailPo po = empty();
        BeanUtil.copyProperties(this, po);
        return po;
    }

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    @GaModelField(text = "分片id", isID = true)
    private String id;

    @Column(name = "platform")
    @GaModelField(text = "存储平台")
    private String platform;

    @Column(name = "upload_id")
    @GaModelField(text = "上传ID，仅在手动分片上传时使用")
    private String uploadId;

    @Column(name = "e_tag")
    @GaModelField(text = "分片 ETag")
    private String eTag;

    @Column(name = "part_number")
    @GaModelField(text = "分片号，取值范围1~10000")
    private Integer partNumber;

    @Column(name = "part_size")
    @GaModelField(text = "分片大小，单位字节")
    private Long partSize;

    @Column(name = "hash_info")
    @GaModelField(text = "哈希信息")
    private String hashInfo;

    @Column(name = "create_time")
    @GaModelField(text = "创建时间", datePattern = GemDatePattern.ISO8601Long)
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date createTime;

    public FilePartDetailPo() {
    }

    public FilePartDetailPo(String id) {
        if (id == null) {
            id = this.generatorId();
        }
        this.id = id;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public String getETag() {
        return eTag;
    }

    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    public Integer getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(Integer partNumber) {
        this.partNumber = partNumber;
    }

    public Long getPartSize() {
        return partSize;
    }

    public void setPartSize(Long partSize) {
        this.partSize = partSize;
    }

    public String getHashInfo() {
        return hashInfo;
    }

    public void setHashInfo(String hashInfo) {
        this.hashInfo = hashInfo;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String generatorId() {
        return UUID.randomUUID().toString();
    }

    // 业务方法
    public void initCreateMeta() {
        setCreateTime(new Date());
    }

    public void initUpdateMeta() {
        // 可以根据需要添加更新时的逻辑
    }
}
