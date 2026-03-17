package cn.geoair.geoairteam.gwc.mapper.gwc;

import cn.geoair.geoairteam.gwc.dao.gwc.LayerGroupDao;
import cn.geoair.geoairteam.gwc.model.gwc.dto.LayerGroupDto;
import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerGroupPo;
import cn.geoair.geoairteam.gwc.model.gwc.seo.LayerGroupSeo;
import cn.geoair.orm.tkmapper.impls.TkEntityMapper;
import org.apache.ibatis.annotations.Param;
import cn.geoair.base.data.page.GfunPageExcute;
import cn.geoair.base.data.page.GiPageParam;
import cn.geoair.base.data.page.GiPager;

import java.util.List;

/**
 * ${tableComment}Mapper接口
 *
 * @author geoairteam
 * @date 2026-03-11 15:59:37
 */
public interface LayerGroupMapper extends LayerGroupDao, TkEntityMapper<LayerGroupPo, String> {
    @Override
    List<LayerGroupDto> searchList(@Param("param" ) LayerGroupSeo layerGroupSeo);


    @Override
    default GiPager<LayerGroupDto> searchListPage(@Param("param" ) LayerGroupSeo layerGroupSeo, GiPageParam pageParam) {

        GfunPageExcute<LayerGroupDto> exec = new GfunPageExcute<LayerGroupDto>() {
            @Override
            public Iterable<LayerGroupDto> excute() {
                return searchList(layerGroupSeo);
            }
        };

        return pageExcuter().excutePage(exec, pageParam);

    }
}
