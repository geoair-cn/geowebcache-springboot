package cn.geoair.geoairteam.gwc.mapper.gwc;

import cn.geoair.geoairteam.gwc.dao.gwc.LayerInfoDao;
import cn.geoair.geoairteam.gwc.model.gwc.dto.LayerInfoDto;
import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerInfoPo;
import cn.geoair.geoairteam.gwc.model.gwc.seo.LayerInfoSeo;
import cn.geoair.orm.tkmapper.impls.TkEntityMapper;
import org.apache.ibatis.annotations.Param;
import cn.geoair.base.data.page.GfunPageExcute;
import cn.geoair.base.data.page.GiPageParam;
import cn.geoair.base.data.page.GiPager;

import java.util.List;

/**
 * 图层信息表Mapper接口
 *
 * @author geoairteam
 * @date 2026-03-11 15:45:16
 */
public interface LayerInfoMapper extends LayerInfoDao, TkEntityMapper<LayerInfoPo, String> {
    @Override
    List<LayerInfoDto> searchList(@Param("param" ) LayerInfoSeo layerInfoSeo);


    @Override
    default GiPager<LayerInfoDto> searchListPage(@Param("param" ) LayerInfoSeo layerInfoSeo, GiPageParam pageParam) {

        GfunPageExcute<LayerInfoDto> exec = new GfunPageExcute<LayerInfoDto>() {
            @Override
            public Iterable<LayerInfoDto> excute() {
                return searchList(layerInfoSeo);
            }
        };

        return pageExcuter().excutePage(exec, pageParam);

    }
}
