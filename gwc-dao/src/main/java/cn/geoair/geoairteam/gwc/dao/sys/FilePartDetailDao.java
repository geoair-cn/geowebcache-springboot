package cn.geoair.geoairteam.gwc.dao.sys;


import cn.geoair.base.data.page.GiPageParam;
import cn.geoair.base.data.page.GiPager;
import cn.geoair.base.gpa.dao.GiEntityDao;
import cn.geoair.geoairteam.gwc.model.sys.dto.FilePartDetailDto;
import cn.geoair.geoairteam.gwc.model.sys.entity.FilePartDetailPo;
import cn.geoair.geoairteam.gwc.model.sys.seo.FilePartDetailSeo;

import java.util.List;

/**
 * 文件分片信息表Dao接口
 *
 * @author your name
 * @date 2024-01-01
 */
public interface FilePartDetailDao extends GiEntityDao<FilePartDetailPo, String> {

    /**
     * 查询列表
     *
     * @param filePartDetailSeo 查询条件
     * @return 结果列表
     */
    List<FilePartDetailDto> searchList(FilePartDetailSeo filePartDetailSeo);

    /**
     * 分页查询
     *
     * @param filePartDetailSeo 查询条件
     * @param pageParam         分页参数
     * @return 分页结果
     */
    GiPager<FilePartDetailDto> searchListPage(FilePartDetailSeo filePartDetailSeo, GiPageParam pageParam);
}
