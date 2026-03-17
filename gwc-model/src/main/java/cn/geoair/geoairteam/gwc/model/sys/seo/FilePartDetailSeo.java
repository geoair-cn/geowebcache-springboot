package cn.geoair.geoairteam.gwc.model.sys.seo;

import cn.geoair.base.data.model.annotation.GaModel;
import cn.geoair.base.data.model.annotation.GaModelField;
import cn.hutool.core.bean.BeanUtil;
import cn.geoair.geoairteam.gwc.model.sys.entity.FilePartDetailPo;


import java.util.Date;

/**
 * 文件分片信息表Seo
 *
 * @author your name
 * @date 2024-01-01
 */
@GaModel(text = "文件分片信息表SearchDto")
public class FilePartDetailSeo extends FilePartDetailPo {

    public static FilePartDetailSeo empty() {
        return new FilePartDetailSeo();
    }

    public FilePartDetailSeo copy() {
        FilePartDetailSeo seo = empty();
        BeanUtil.copyProperties(this, seo);
        return seo;
    }

    private static final long serialVersionUID = 1L;

    @GaModelField(text = "模糊查询")
    private String[] andQueryContentIn;

    @GaModelField(text = "查询多个主键数据")
    private String[] andIdsIn;

    @GaModelField(text = "查询排除多个主键数据")
    private String[] andIdsNotIn;

    @GaModelField(text = "分片号范围-最小值")
    private Integer partNumberMin;

    @GaModelField(text = "分片号范围-最大值")
    private Integer partNumberMax;

    @GaModelField(text = "创建时间始")
    private Date createTimeStart;

    @GaModelField(text = "创建时间止")
    private Date createTimeEnd;

    @GaModelField(text = "平台列表")
    private String[] platforms;

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

    public Integer getPartNumberMin() {
        return partNumberMin;
    }

    public void setPartNumberMin(Integer partNumberMin) {
        this.partNumberMin = partNumberMin;
    }

    public Integer getPartNumberMax() {
        return partNumberMax;
    }

    public void setPartNumberMax(Integer partNumberMax) {
        this.partNumberMax = partNumberMax;
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

    public String[] getPlatforms() {
        return platforms;
    }

    public void setPlatforms(String[] platforms) {
        this.platforms = platforms;
    }
}
