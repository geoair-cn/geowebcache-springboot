package cn.geoair.geoairteam.gwc.mapper.sys;

import cn.geoair.base.data.page.GfunPageExcute;
import cn.geoair.base.data.page.GiPageParam;
import cn.geoair.base.data.page.GiPager;
import cn.geoair.base.gpa.dao.GiEntityDao;
import cn.geoair.orm.tkmapper.impls.TkEntityMapper;
import cn.geoair.geoairteam.gwc.dao.sys.FileDetailDao;
import cn.geoair.geoairteam.gwc.model.sys.dto.FileDetailDto;
import cn.geoair.geoairteam.gwc.model.sys.entity.FileDetailPo;
import cn.geoair.geoairteam.gwc.model.sys.seo.FileDetailSeo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件记录表Mapper接口
 *
 * @author your name
 * @date 2024-01-01
 */
public interface FileDetailMapper extends FileDetailDao, TkEntityMapper<FileDetailPo, String> {

    @Override
    List<FileDetailDto> searchList(@Param("param") FileDetailSeo fileDetailSeo);

    @Override
    default GiPager<FileDetailDto> searchListPage(@Param("param") FileDetailSeo fileDetailSeo, GiPageParam pageParam) {
        GfunPageExcute<FileDetailDto> exec = new GfunPageExcute<FileDetailDto>() {
            @Override
            public Iterable<FileDetailDto> excute() {
                return searchList(fileDetailSeo);
            }
        };
        return pageExcuter().excutePage(exec, pageParam);
    }
}
