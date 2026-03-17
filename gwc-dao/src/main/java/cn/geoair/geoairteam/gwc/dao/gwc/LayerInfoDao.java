package cn.geoair.geoairteam.gwc.dao.gwc;

import cn.geoair.geoairteam.gwc.model.gwc.dto.LayerInfoDto;
import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerInfoPo;
import cn.geoair.geoairteam.gwc.model.gwc.seo.LayerInfoSeo;
import cn.geoair.base.gpa.dao.GiEntityDao;
import cn.geoair.base.data.page.GiPageParam;
import cn.geoair.base.data.page.GiPager;

import java.util.List;

/**
 * 图层信息表Dao接口
 *
 * @author geoairteam
 * @date 2026-03-11 15:45:16
 */
public interface LayerInfoDao extends GiEntityDao<LayerInfoPo, String> {

    List<LayerInfoDto> searchList(LayerInfoSeo layerInfoSeo);

    void updateBbox(String id);

    GiPager<LayerInfoDto> searchListPage(LayerInfoSeo layerInfoSeo, GiPageParam pageParam);
}
