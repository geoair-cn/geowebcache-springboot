package cn.geoair.geoairteam.gwc.dao.gwc;

import cn.geoair.geoairteam.gwc.model.gwc.dto.LayerGroupAliasDto;
import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerGroupAliasPo;
import cn.geoair.geoairteam.gwc.model.gwc.seo.LayerGroupAliasSeo;
import cn.geoair.base.gpa.dao.GiEntityDao;
import cn.geoair.base.data.page.GiPageParam;
import cn.geoair.base.data.page.GiPager;

import java.util.List;

/**
 * 图层组别名表Dao接口
 *
 * @author geoairteam
 * @date 2026-03-11 15:44:18
 */
public interface LayerGroupAliasDao extends GiEntityDao<LayerGroupAliasPo, String> {

    List<LayerGroupAliasDto> searchList(LayerGroupAliasSeo layerGroupAliasSeo);

    String searchLayerStringByLayerGroupAlias(String layerGroupAlias);

    GiPager<LayerGroupAliasDto> searchListPage(LayerGroupAliasSeo layerGroupAliasSeo, GiPageParam pageParam);
}
