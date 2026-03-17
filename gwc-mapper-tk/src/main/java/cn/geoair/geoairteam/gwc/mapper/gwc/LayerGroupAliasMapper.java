package cn.geoair.geoairteam.gwc.mapper.gwc;

import cn.geoair.base.util.GutilObject;
import cn.geoair.geoairteam.gwc.dao.gwc.LayerGroupAliasDao;
import cn.geoair.geoairteam.gwc.model.gwc.dto.LayerGroupAliasDto;
import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerGroupAliasPo;
import cn.geoair.geoairteam.gwc.model.gwc.seo.LayerGroupAliasSeo;
import cn.geoair.orm.tkmapper.impls.TkEntityMapper;
import org.apache.ibatis.annotations.Param;
import cn.geoair.base.data.page.GfunPageExcute;
import cn.geoair.base.data.page.GiPageParam;
import cn.geoair.base.data.page.GiPager;

import java.util.List;

/**
 * 图层组别名表Mapper接口
 *
 * @author geoairteam
 * @date 2026-03-11 15:44:18
 */
public interface LayerGroupAliasMapper extends LayerGroupAliasDao, TkEntityMapper<LayerGroupAliasPo, String> {
    @Override
    List<LayerGroupAliasDto> searchList(@Param("param") LayerGroupAliasSeo layerGroupAliasSeo);

    default String searchLayerStringByLayerGroupAlias(String layerGroupAlias) {
        if (GutilObject.isEmpty(layerGroupAlias)) {
            return null;
        }
        LayerGroupAliasSeo layerGroupAliasSeo = new LayerGroupAliasSeo();
        layerGroupAliasSeo.setLayerGroupAlias(layerGroupAlias);
        List<LayerGroupAliasDto> layerGroupAliasDtos = searchList(layerGroupAliasSeo);
        if (GutilObject.isEmpty(layerGroupAliasDtos)) {
            return null;
        }
        return layerGroupAliasDtos.get(0).getLayerString();
    }

    @Override
    default GiPager<LayerGroupAliasDto> searchListPage(@Param("param") LayerGroupAliasSeo layerGroupAliasSeo, GiPageParam pageParam) {

        GfunPageExcute<LayerGroupAliasDto> exec = new GfunPageExcute<LayerGroupAliasDto>() {
            @Override
            public Iterable<LayerGroupAliasDto> excute() {
                return searchList(layerGroupAliasSeo);
            }
        };

        return pageExcuter().excutePage(exec, pageParam);

    }
}
