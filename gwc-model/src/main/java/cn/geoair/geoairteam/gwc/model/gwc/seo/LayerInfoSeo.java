package cn.geoair.geoairteam.gwc.model.gwc.seo;
import lombok.Data;
import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerInfoPo;
import cn.geoair.base.data.model.annotation.GaModel;
import cn.geoair.base.data.model.annotation.GaModelField;
import cn.hutool.core.bean.BeanUtil;
/**
 * 图层信息表(LayerInfo)Seo
 *
 * @author geoairteam
 * @date 2026-03-11 15:45:16
 */
@GaModel(text = "图层信息表SearchDto")
@Data
public class LayerInfoSeo extends LayerInfoPo {
    private static final long serialVersionUID = 1773215116580L;
    public static LayerInfoSeo emptySeo() {
        return new LayerInfoSeo();
    }

    public   LayerInfoSeo copy() {
         LayerInfoSeo copy =   new LayerInfoSeo();
        BeanUtil.copyProperties(this, copy);
        return copy;
    }

    @GaModelField(text = "模糊查询")
    private String[] andQueryContentIn;

    @GaModelField(text = "查询多个主键数据")
    private String [] andIdsIn;

    @GaModelField(text = "查询排除多个主键数据")
    private String [] andIdsNotIn;





















}
