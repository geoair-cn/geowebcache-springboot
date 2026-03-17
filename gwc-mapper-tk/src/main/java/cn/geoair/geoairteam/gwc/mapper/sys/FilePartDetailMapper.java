package cn.geoair.geoairteam.gwc.mapper.sys;

import cn.geoair.base.data.page.GfunPageExcute;
import cn.geoair.base.data.page.GiPageParam;
import cn.geoair.base.data.page.GiPager;
import cn.geoair.base.gpa.dao.GiEntityDao;
import cn.geoair.orm.tkmapper.impls.TkEntityMapper;
import cn.geoair.geoairteam.gwc.dao.sys.FilePartDetailDao;
import cn.geoair.geoairteam.gwc.model.sys.dto.FilePartDetailDto;
import cn.geoair.geoairteam.gwc.model.sys.entity.FilePartDetailPo;
import cn.geoair.geoairteam.gwc.model.sys.seo.FilePartDetailSeo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件分片信息表Mapper接口
 *
 * @author your name
 * @date 2024-01-01
 */
public interface FilePartDetailMapper extends FilePartDetailDao, TkEntityMapper<FilePartDetailPo, String> {

    @Override
    List<FilePartDetailDto> searchList(@Param("param" ) FilePartDetailSeo filePartDetailSeo);

    @Override
    default GiPager<FilePartDetailDto> searchListPage(@Param("param" ) FilePartDetailSeo filePartDetailSeo, GiPageParam pageParam) {
        GfunPageExcute<FilePartDetailDto> exec = new GfunPageExcute<FilePartDetailDto>() {
            @Override
            public Iterable<FilePartDetailDto> excute() {
                return searchList(filePartDetailSeo);
            }
        };
        return pageExcuter().excutePage(exec, pageParam);
    }
}
