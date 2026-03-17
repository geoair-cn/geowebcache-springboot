package cn.geoair.geoairteam.gwc.servface.sys.dto;


import cn.geoair.base.data.model.annotation.GaModelField;

import java.util.Date;

/**
 * @author ：zfj
 * @date ：Created in 2023/7/11 14:38
 * @description： 对sdk里面文件对象进行脱藕
 */
public class OmFileInfoApo {
    /**
     * 文件id
     */
    @GaModelField(text = "文件id" )
    private String id;

    /**
     * 文件访问地址
     */
    @GaModelField(text = "文件访问地址" )
    private String url;

    /**
     * 文件大小，单位字节
     */
    @GaModelField(text = "文件大小，单位字节" )
    private Long size;

    /**
     * 文件名称
     */
    @GaModelField(text = "文件名称" )
    private String filename;

    /**
     * 原始文件名
     */
    @GaModelField(text = "原始文件名" )
    private String originalFilename;

    /**
     * 创建时间
     */
    @GaModelField(text = "创建时间" )
    private Date createTime;

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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
