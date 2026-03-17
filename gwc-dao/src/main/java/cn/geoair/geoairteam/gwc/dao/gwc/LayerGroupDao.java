package cn.geoair.geoairteam.gwc.dao.gwc;

import cn.geoair.geoairteam.gwc.model.gwc.dto.LayerGroupDto;
import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerGroupPo;
import cn.geoair.geoairteam.gwc.model.gwc.seo.LayerGroupSeo;
import cn.geoair.base.gpa.dao.GiEntityDao;
import cn.geoair.base.data.page.GiPageParam;
import cn.geoair.base.data.page.GiPager;
import java.util.List;

/**
 * ${tableComment}Dao接口
 *
 * @author geoairteam
 * @date 2026-03-11 15:59:37
 */
public interface LayerGroupDao extends GiEntityDao<LayerGroupPo,String> {

    List<LayerGroupDto> searchList(LayerGroupSeo layerGroupSeo);


    GiPager<LayerGroupDto> searchListPage(LayerGroupSeo layerGroupSeo,GiPageParam pageParam);
}
