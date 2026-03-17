package cn.geoair.geoairteam.gwc.dao.sys;

import cn.geoair.base.data.page.GiPageParam;
import cn.geoair.base.data.page.GiPager;
import cn.geoair.base.gpa.dao.GiEntityDao;
import cn.geoair.geoairteam.gwc.model.sys.dto.ConfigPropertyDto;
import cn.geoair.geoairteam.gwc.model.sys.entity.ConfigPropertyPo;
import cn.geoair.geoairteam.gwc.model.sys.seo.ConfigPropertySeo;


import java.util.List;

/**
 * 配置属性表Dao接口
 *
 * @author zhangjun
 * @date 2024-09-04
 */
public interface ConfigPropertyDao extends GiEntityDao<ConfigPropertyPo, String> {
    List<ConfigPropertyDto> searchList(ConfigPropertySeo configPropertySeo);

    ConfigPropertyPo getConfigByKey(String key);

    String getConfigValueByKey(String key);

    String getConfigValueByKey(String key, String ifNullValue);

    void setConfigValueByKey(String key, String value);

    GiPager<ConfigPropertyDto> searchListPage(ConfigPropertySeo configPropertySeo, GiPageParam pageParam);
}
