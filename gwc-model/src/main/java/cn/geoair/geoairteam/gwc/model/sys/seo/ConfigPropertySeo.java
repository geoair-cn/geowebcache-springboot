package cn.geoair.geoairteam.gwc.model.sys.seo;

import cn.hutool.core.bean.BeanUtil;
import cn.geoair.base.data.model.annotation.GaModel;
import cn.geoair.base.data.model.annotation.GaModelField;
import cn.geoair.geoairteam.gwc.model.sys.entity.ConfigPropertyPo;


import java.util.Date;

/**
 * 配置属性表(ConfigProperty)Seo
 *
 * @author zhangjun
 * @date 2024-09-04
 */
@GaModel(text = "配置属性表SearchDto")
public class ConfigPropertySeo extends ConfigPropertyPo {
    private static final long serialVersionUID = 1725436188414L;

    public static ConfigPropertySeo empty() {
        return new ConfigPropertySeo();
    }


    public ConfigPropertySeo copy() {
        ConfigPropertySeo po = empty();
        BeanUtil.copyProperties(this, po);
        return po;
    }

    @GaModelField(text = "模糊查询")
    private String[] andQueryContentIn;

    @GaModelField(text = "查询多个主键数据")
    private String[] andIdsIn;

    @GaModelField(text = "查询排除多个主键数据")
    private String[] andIdsNotIn;

    @GaModelField(text = "更新时间始")
    private Date updateTimeStart;

    @GaModelField(text = "更新时间止")
    private Date updateTimeEnd;

    public String[] getAndQueryContentIn() {
        return andQueryContentIn;
    }

    public void setAndQueryContentIn(String[] andQueryContentIn) {
        this.andQueryContentIn = andQueryContentIn;
    }

    public void setAndIdsIn(String[] andIdsIn) {
        this.andIdsIn = andIdsIn;
    }

    public String[] getAndIdsIn() {
        return this.andIdsIn;
    }

    public void setAndIdsNotIn(String[] andIdsNotIn) {
        this.andIdsNotIn = andIdsNotIn;
    }

    public String[] getAndIdsNotIn() {
        return andIdsNotIn;
    }
}
