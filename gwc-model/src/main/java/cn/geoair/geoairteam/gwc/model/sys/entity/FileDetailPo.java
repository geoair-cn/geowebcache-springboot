package cn.geoair.geoairteam.gwc.model.sys.entity;

import cn.geoair.base.data.common.GemDatePattern;
import cn.geoair.base.data.model.annotation.GaModel;
import cn.geoair.base.data.model.annotation.GaModelField;
import cn.geoair.base.gpa.entity.GiCrudEntity;
import cn.geoair.base.gpa.id.GiEntityIdGenerator;
import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;


import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

/**
 * 文件记录表实体类
 *
 * @author your name
 * @date 2024-01-01
 */
@GaModel(text = "文件记录表" )
@Table(name = "file_detail" )
public class FileDetailPo implements GiCrudEntity<String>, GiEntityIdGenerator<String> {

    public static FileDetailPo empty() {
        return new FileDetailPo();
    }

    public FileDetailPo copy() {
        FileDetailPo po = empty();
        BeanUtil.copyProperties(this, po);
        return po;
    }

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id" )
    @GaModelField(text = "文件id" , isID = true)
    private String id;

    @Column(name = "url" )
    @GaModelField(text = "文件访问地址" )
    private String url;

    @Column(name = "size" )
    @GaModelField(text = "文件大小，单位字节" )
    private Long size;

    @Column(name = "filename" )
    @GaModelField(text = "文件名称" )
    private String filename;

    @Column(name = "original_filename" )
    @GaModelField(text = "原始文件名" )
    private String originalFilename;

    @Column(name = "base_path" )
    @GaModelField(text = "基础存储路径" )
    private String basePath;

    @Column(name = "path" )
    @GaModelField(text = "存储路径" )
    private String path;

    @Column(name = "ext" )
    @GaModelField(text = "文件扩展名" )
    private String ext;

    @Column(name = "content_type" )
    @GaModelField(text = "MIME类型" )
    private String contentType;

    @Column(name = "platform" )
    @GaModelField(text = "存储平台" )
    private String platform;

    @Column(name = "th_url" )
    @GaModelField(text = "缩略图访问路径" )
    private String thUrl;

    @Column(name = "th_filename" )
    @GaModelField(text = "缩略图名称" )
    private String thFilename;

    @Column(name = "th_size" )
    @GaModelField(text = "缩略图大小，单位字节" )
    private Long thSize;

    @Column(name = "th_content_type" )
    @GaModelField(text = "缩略图MIME类型" )
    private String thContentType;

    @Column(name = "object_id" )
    @GaModelField(text = "文件所属对象id" )
    private String objectId;

    @Column(name = "object_type" )
    @GaModelField(text = "文件所属对象类型，例如用户头像，评价图片" )
    private String objectType;

    @Column(name = "metadata" )
    @GaModelField(text = "文件元数据" )
    private String metadata;

    @Column(name = "user_metadata" )
    @GaModelField(text = "文件用户元数据" )
    private String userMetadata;

    @Column(name = "th_metadata" )
    @GaModelField(text = "缩略图元数据" )
    private String thMetadata;

    @Column(name = "th_user_metadata" )
    @GaModelField(text = "缩略图用户元数据" )
    private String thUserMetadata;

    @Column(name = "attr" )
    @GaModelField(text = "附加属性" )
    private String attr;

    @Column(name = "file_acl" )
    @GaModelField(text = "文件ACL" )
    private String fileAcl;

    @Column(name = "th_file_acl" )
    @GaModelField(text = "缩略图文件ACL" )
    private String thFileAcl;

    @Column(name = "hash_info" )
    @GaModelField(text = "哈希信息" )
    private String hashInfo;

    @Column(name = "upload_id" )
    @GaModelField(text = "上传ID，仅在手动分片上传时使用" )
    private String uploadId;

    @Column(name = "upload_status" )
    @GaModelField(text = "上传状态，仅在手动分片上传时使用，1：初始化完成，2：上传完成" )
    private Integer uploadStatus;

    @Column(name = "create_time" )
    @GaModelField(text = "创建时间" , datePattern = GemDatePattern.ISO8601Long)
    @JSONField(format = "yyyy-MM-dd HH:mm:ss" )
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss" , timezone = "Asia/Shanghai" )
    private Date createTime;

    public FileDetailPo() {
    }

    public FileDetailPo(String id) {
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getThUrl() {
        return thUrl;
    }

    public void setThUrl(String thUrl) {
        this.thUrl = thUrl;
    }

    public String getThFilename() {
        return thFilename;
    }

    public void setThFilename(String thFilename) {
        this.thFilename = thFilename;
    }

    public Long getThSize() {
        return thSize;
    }

    public void setThSize(Long thSize) {
        this.thSize = thSize;
    }

    public String getThContentType() {
        return thContentType;
    }

    public void setThContentType(String thContentType) {
        this.thContentType = thContentType;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getUserMetadata() {
        return userMetadata;
    }

    public void setUserMetadata(String userMetadata) {
        this.userMetadata = userMetadata;
    }

    public String getThMetadata() {
        return thMetadata;
    }

    public void setThMetadata(String thMetadata) {
        this.thMetadata = thMetadata;
    }

    public String getThUserMetadata() {
        return thUserMetadata;
    }

    public void setThUserMetadata(String thUserMetadata) {
        this.thUserMetadata = thUserMetadata;
    }

    public String getAttr() {
        return attr;
    }

    public void setAttr(String attr) {
        this.attr = attr;
    }

    public String getFileAcl() {
        return fileAcl;
    }

    public void setFileAcl(String fileAcl) {
        this.fileAcl = fileAcl;
    }

    public String getThFileAcl() {
        return thFileAcl;
    }

    public void setThFileAcl(String thFileAcl) {
        this.thFileAcl = thFileAcl;
    }

    public String getHashInfo() {
        return hashInfo;
    }

    public void setHashInfo(String hashInfo) {
        this.hashInfo = hashInfo;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public Integer getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(Integer uploadStatus) {
        this.uploadStatus = uploadStatus;
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
