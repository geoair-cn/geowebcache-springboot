package cn.geoair.geoairteam.gwc.servface.gwc;

import cn.geoair.geoairteam.gwc.model.gwc.dto.LayerGroupDto;
import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerGroupPo;
import cn.geoair.geoairteam.gwc.model.gwc.seo.LayerGroupSeo;
import cn.geoair.base.data.page.GiPageParam;
import cn.geoair.base.data.page.GiPager;

import java.util.List;
import java.util.Map;

/**
 * ${tableComment}服务接口
 *
 * @author geoairteam
 * @date 2026-03-11 15:59:37
 */
public interface LayerGroupService {

    /**
     * 新增${tableComment}
     *
     * @param po 实体对象
     * @return 新增后的实体对象
     */
        LayerGroupPo add(LayerGroupPo po);

    /**
     * 修改${tableComment}
     *
     * @param po 实体对象
     * @return 修改后的实体对象
     */
        LayerGroupPo update(LayerGroupPo po);

    /**
     * 根据ID删除${tableComment}
     *
     * @param id 主键ID
     */
    void deleteById(String id);

    /**
     * 根据ID逻辑删除${tableComment}
     *
     * @param id 主键ID
     */
    void logicDeleteById(String id);

    /**
     * 根据ID查询${tableComment}
     *
     * @param id 主键ID
     * @return DTO对象
     */
        LayerGroupDto getDtoById(String id);

    /**
     * 根据ID查询${tableComment}实体
     *
     * @param id 主键ID
     * @return 实体对象
     */
        LayerGroupPo getPoById(String id);

    /**
     * 根据条件查询${tableComment}列表
     *
     * @param seo 查询条件
     * @return DTO对象列表
     */
    List<LayerGroupDto> getDtoListBySeo(LayerGroupSeo seo);

    /**
     * 根据条件查询${tableComment}列表（带分页）
     *
     * @param seo 查询条件
     * @param pageParam 分页信息
     * @return 分页结果
     */
    GiPager<LayerGroupDto> getDtoPageBySeo(LayerGroupSeo seo,GiPageParam pageParam);

    /**
     * 根据条件查询${tableComment}实体列表
     *
     * @param seo 查询条件
     * @return 实体对象列表
     */
    List<LayerGroupPo> getPoListBySeo(LayerGroupSeo seo);

    /**
     * 批量获取DTO映射（按指定字段映射）
     *
     * @param beanList Bean列表
     * @param fieldName 字段名（支持多级字段，如：user.id）
     * @param <T> Bean类型
     * @return 字段值到DTO的映射
     */
    <T> Map<String, LayerGroupDto> getDtoMapByField(List<T> beanList, String fieldName);

    /**
     * 刷新缓存
     */
    void refreshCache();
}
