package cn.geoair.geoairteam.gwc.model.sys.seo;

import cn.hutool.core.bean.BeanUtil;
import cn.geoair.base.data.common.GemDatePattern;
import cn.geoair.base.data.model.annotation.GaModel;
import cn.geoair.base.data.model.annotation.GaModelField;
import cn.geoair.base.gpa.entity.GiCrudEntity;
import cn.geoair.base.gpa.id.GiEntityIdGenerator;
import cn.geoair.geoairteam.gwc.model.sys.entity.FileDetailPo;

import java.util.Date;

/**
 * 文件记录表Seo
 *
 * @author your name
 * @date 2024-01-01
 */
@GaModel(text = "文件记录表SearchDto" )
public class FileDetailSeo extends FileDetailPo {

    public static FileDetailSeo empty() {
        return new FileDetailSeo();
    }

    public FileDetailSeo copy() {
        FileDetailSeo seo = empty();
        BeanUtil.copyProperties(this, seo);
        return seo;
    }

    private static final long serialVersionUID = 1L;

    @GaModelField(text = "模糊查询" )
    private String[] andQueryContentIn;

    @GaModelField(text = "查询多个主键数据" )
    private String[] andIdsIn;

    @GaModelField(text = "查询排除多个主键数据" )
    private String[] andIdsNotIn;

    @GaModelField(text = "文件大小范围-最小值" )
    private Long sizeMin;

    @GaModelField(text = "文件大小范围-最大值" )
    private Long sizeMax;

    @GaModelField(text = "创建时间始" )
    private Date createTimeStart;

    @GaModelField(text = "创建时间止" )
    private Date createTimeEnd;

    @GaModelField(text = "对象类型列表" )
    private String[] objectTypes;

    @GaModelField(text = "上传状态列表" )
    private Integer[] uploadStatusList;

    // Getters and Setters
    public String[] getAndQueryContentIn() {
        return andQueryContentIn;
    }

    public void setAndQueryContentIn(String[] andQueryContentIn) {
        this.andQueryContentIn = andQueryContentIn;
    }

    public String[] getAndIdsIn() {
        return andIdsIn;
    }

    public void setAndIdsIn(String[] andIdsIn) {
        this.andIdsIn = andIdsIn;
    }

    public String[] getAndIdsNotIn() {
        return andIdsNotIn;
    }

    public void setAndIdsNotIn(String[] andIdsNotIn) {
        this.andIdsNotIn = andIdsNotIn;
    }

    public Long getSizeMin() {
        return sizeMin;
    }

    public void setSizeMin(Long sizeMin) {
        this.sizeMin = sizeMin;
    }

    public Long getSizeMax() {
        return sizeMax;
    }

    public void setSizeMax(Long sizeMax) {
        this.sizeMax = sizeMax;
    }

    public Date getCreateTimeStart() {
        return createTimeStart;
    }

    public void setCreateTimeStart(Date createTimeStart) {
        this.createTimeStart = createTimeStart;
    }

    public Date getCreateTimeEnd() {
        return createTimeEnd;
    }

    public void setCreateTimeEnd(Date createTimeEnd) {
        this.createTimeEnd = createTimeEnd;
    }

    public String[] getObjectTypes() {
        return objectTypes;
    }

    public void setObjectTypes(String[] objectTypes) {
        this.objectTypes = objectTypes;
    }

    public Integer[] getUploadStatusList() {
        return uploadStatusList;
    }

    public void setUploadStatusList(Integer[] uploadStatusList) {
        this.uploadStatusList = uploadStatusList;
    }
}
