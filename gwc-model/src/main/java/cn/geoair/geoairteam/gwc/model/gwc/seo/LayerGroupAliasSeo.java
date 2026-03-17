package cn.geoair.geoairteam.gwc.model.gwc.seo;
import lombok.Data;
import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerGroupAliasPo;
import cn.geoair.base.data.model.annotation.GaModel;
import cn.geoair.base.data.model.annotation.GaModelField;
import cn.hutool.core.bean.BeanUtil;
/**
 * 图层组别名表(LayerGroupAlias)Seo
 *
 * @author geoairteam
 * @date 2026-03-11 15:44:18
 */
@GaModel(text = "图层组别名表SearchDto")
@Data
public class LayerGroupAliasSeo extends LayerGroupAliasPo {
    private static final long serialVersionUID = 1773215058719L;
    public static LayerGroupAliasSeo emptySeo() {
        return new LayerGroupAliasSeo();
    }

    public   LayerGroupAliasSeo copy() {
         LayerGroupAliasSeo copy =   new LayerGroupAliasSeo();
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
