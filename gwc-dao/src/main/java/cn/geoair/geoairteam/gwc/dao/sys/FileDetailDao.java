package cn.geoair.geoairteam.gwc.dao.sys;



import cn.geoair.base.data.page.GiPageParam;
import cn.geoair.base.data.page.GiPager;
import cn.geoair.base.gpa.dao.GiEntityDao;
import cn.geoair.geoairteam.gwc.model.sys.dto.FileDetailDto;
import cn.geoair.geoairteam.gwc.model.sys.entity.FileDetailPo;
import cn.geoair.geoairteam.gwc.model.sys.seo.FileDetailSeo;

import java.util.List;

/**
 * 文件记录表Dao接口
 *
 * @author your name
 * @date 2024-01-01
 */
public interface FileDetailDao extends GiEntityDao<FileDetailPo, String> {

    /**
     * 查询列表
     *
     * @param fileDetailSeo 查询条件
     * @return 结果列表
     */
    List<FileDetailDto> searchList(FileDetailSeo fileDetailSeo);

    /**
     * 分页查询
     *
     * @param fileDetailSeo 查询条件
     * @param pageParam 分页参数
     * @return 分页结果
     */
    GiPager<FileDetailDto> searchListPage(FileDetailSeo fileDetailSeo, GiPageParam pageParam);
}
