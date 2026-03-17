package cn.geoair.geoairteam.gwc.servface.gwc;

import cn.geoair.geoairteam.gwc.model.gwc.dto.LayerInfoDto;
import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerInfoPo;
import cn.geoair.geoairteam.gwc.model.gwc.seo.LayerInfoSeo;
import cn.geoair.base.data.page.GiPageParam;
import cn.geoair.base.data.page.GiPager;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 图层信息表服务接口
 *
 * @author geoairteam
 * @date 2026-03-11 15:45:16
 */
public interface LayerInfoService {

    /**
     * 新增图层信息表
     *
     * @param po 实体对象
     * @return 新增后的实体对象
     */
        LayerInfoPo add(LayerInfoPo po);

    /**
     * 修改图层信息表
     *
     * @param po 实体对象
     * @return 修改后的实体对象
     */
        LayerInfoPo update(LayerInfoPo po);

    /**
     * 根据ID删除图层信息表
     *
     * @param id 主键ID
     */
    void deleteById(String id);

    /**
     * 根据ID逻辑删除图层信息表
     *
     * @param id 主键ID
     */
    void logicDeleteById(String id);

    /**
     * 根据ID查询图层信息表
     *
     * @param id 主键ID
     * @return DTO对象
     */
        LayerInfoDto getDtoById(String id);

    /**
     * 根据ID查询图层信息表实体
     *
     * @param id 主键ID
     * @return 实体对象
     */
        LayerInfoPo getPoById(String id);

    /**
     * 根据条件查询图层信息表列表
     *
     * @param seo 查询条件
     * @return DTO对象列表
     */
    List<LayerInfoDto> getDtoListBySeo(LayerInfoSeo seo);

    /**
     * 根据条件查询图层信息表列表（带分页）
     *
     * @param seo 查询条件
     * @param pageParam 分页信息
     * @return 分页结果
     */
    GiPager<LayerInfoDto> getDtoPageBySeo(LayerInfoSeo seo,GiPageParam pageParam);

    /**
     * 根据条件查询图层信息表实体列表
     *
     * @param seo 查询条件
     * @return 实体对象列表
     */
    List<LayerInfoPo> getPoListBySeo(LayerInfoSeo seo);

    /**
     * 批量获取DTO映射（按指定字段映射）
     *
     * @param beanList Bean列表
     * @param fieldName 字段名（支持多级字段，如：user.id）
     * @param <T> Bean类型
     * @return 字段值到DTO的映射
     */
    <T> Map<String, LayerInfoDto> getDtoMapByField(List<T> beanList, String fieldName);

    /**
     * 刷新缓存
     */
    void refreshCache();


    void updateBbox(String id);

    void readXml(File xmlFile);
}
