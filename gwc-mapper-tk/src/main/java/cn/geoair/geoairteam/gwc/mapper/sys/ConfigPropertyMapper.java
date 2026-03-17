package cn.geoair.geoairteam.gwc.mapper.sys;

import cn.hutool.core.util.IdUtil;
import cn.geoair.base.data.page.GfunPageExcute;
import cn.geoair.base.data.page.GiPageParam;
import cn.geoair.base.data.page.GiPager;
import cn.geoair.base.exception.GirException;
import cn.geoair.base.util.GutilObject;

import cn.geoair.geoairteam.gwc.dao.sys.ConfigPropertyDao;
import cn.geoair.geoairteam.gwc.model.sys.dto.ConfigPropertyDto;
import cn.geoair.geoairteam.gwc.model.sys.entity.ConfigPropertyPo;
import cn.geoair.geoairteam.gwc.model.sys.seo.ConfigPropertySeo;
import cn.geoair.orm.tkmapper.impls.TkEntityMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 配置属性表Mapper接口
 *
 * @author zhangjun
 * @date 2024-09-04
 */
public interface ConfigPropertyMapper extends ConfigPropertyDao, TkEntityMapper<ConfigPropertyPo, String> {
    @Override
    List<ConfigPropertyDto> searchList(@Param("param" ) ConfigPropertySeo configPropertySeo);


    default ConfigPropertyPo getConfigByKey(String key) {
        ConfigPropertyPo addressConfigPo = new ConfigPropertyPo();
        addressConfigPo.setKey(key);
        List<ConfigPropertyPo> select = select(addressConfigPo);
        if (GutilObject.isEmpty(select)) {
            return null;
        } else {
            return select.get(0);
        }
    }

    default String getConfigValueByKey(String key) {
        ConfigPropertyPo configByKey = getConfigByKey(key);
        if (GutilObject.isNotNull(configByKey)) {
            return configByKey.getValue();
        } else {
            throw new GirException("无法找到配置->{}" , key);
        }
    }

    default String getConfigValueByKey(String key, String ifNullValue) {
        ConfigPropertyPo configByKey = getConfigByKey(key);
        if (GutilObject.isNotNull(configByKey)) {
            return configByKey.getValue();
        } else {
            return ifNullValue;
        }
    }

    default void setConfigValueByKey(String key, String value) {
        ConfigPropertyPo configByKey = getConfigByKey(key);
        if (GutilObject.isNotNull(configByKey)) {
            configByKey.setValue(value);
            configByKey.setUpdateTime(new Date());
            updateByPrimaryKey(configByKey);
        } else {
            configByKey = new ConfigPropertyPo();
            configByKey.setId(IdUtil.getSnowflakeNextIdStr());
            configByKey.setKey(key);
            configByKey.setValue(value);
            configByKey.setUpdateTime(new Date());
            insert(configByKey);
        }
    }

    @Override
    default GiPager<ConfigPropertyDto> searchListPage(@Param("param" ) ConfigPropertySeo configPropertySeo, GiPageParam pageParam) {

        GfunPageExcute<ConfigPropertyDto> exec = new GfunPageExcute<ConfigPropertyDto>() {
            @Override
            public Iterable<ConfigPropertyDto> excute() {
                return searchList(configPropertySeo);
            }
        };

        return pageExcuter().excutePage(exec, pageParam);

    }
}
