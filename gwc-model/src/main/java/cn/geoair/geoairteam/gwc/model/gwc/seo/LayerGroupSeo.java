package cn.geoair.geoairteam.gwc.model.gwc.seo;
import lombok.Data;
import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerGroupPo;
import cn.geoair.base.data.model.annotation.GaModel;
import cn.geoair.base.data.model.annotation.GaModelField;
import java.util.Date;
import cn.hutool.core.bean.BeanUtil;
/**
 * ${tableComment}(LayerGroup)Seo
 *
 * @author geoairteam
 * @date 2026-03-11 15:59:37
 */
@GaModel(text = "${tableComment}SearchDto")
@Data
public class LayerGroupSeo extends LayerGroupPo {
    private static final long serialVersionUID = 1773215977762L;
    public static LayerGroupSeo emptySeo() {
        return new LayerGroupSeo();
    }

    public   LayerGroupSeo copy() {
         LayerGroupSeo copy =   new LayerGroupSeo();
        BeanUtil.copyProperties(this, copy);
        return copy;
    }

    @GaModelField(text = "模糊查询")
    private String[] andQueryContentIn;

    @GaModelField(text = "查询多个主键数据")
    private String [] andIdsIn;

    @GaModelField(text = "查询排除多个主键数据")
    private String [] andIdsNotIn;



    @GaModelField(text = "创建时间始")
    private Date timeCreateStart;

    @GaModelField(text = "创建时间止")
    private Date timeCreateEnd;


}
