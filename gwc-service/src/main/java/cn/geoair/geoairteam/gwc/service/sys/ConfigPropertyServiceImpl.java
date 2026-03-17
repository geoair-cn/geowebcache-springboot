package cn.geoair.geoairteam.gwc.service.sys;

import cn.geoair.base.log.GiLogger;
import cn.geoair.base.log.GirLogger;
import cn.hutool.core.collection.ListUtil;
import cn.geoair.base.util.GutilObject;

import cn.geoair.geoairteam.gwc.dao.sys.ConfigPropertyDao;
import cn.geoair.geoairteam.gwc.model.sys.entity.ConfigPropertyPo;
import cn.geoair.geoairteam.gwc.servface.sys.ConfigPropertyService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 配置属性表Service业务层处理
 *
 * @author zhangjun
 * @date 2024-09-04
 */
@Service
@Transactional(rollbackFor = Exception.class)
@CacheConfig(cacheNames = "ConfigPropertyService")
public class ConfigPropertyServiceImpl implements ConfigPropertyService {
    private static final GiLogger loger = GirLogger.getLoger(ConfigPropertyServiceImpl.class);

    @Resource
    private ConfigPropertyDao configPropertyDao;

    public ConfigPropertyDao getConfigPropertyDao() {
        return configPropertyDao;
    }

    @Override
    @Cacheable(value = "ConfigPropertyService", key = "#root.methodName+':' +#p0")
    public ConfigPropertyPo getConfigByKey(String key) {
        return configPropertyDao.getConfigByKey(key);
    }

    @Override
    @Cacheable(value = "ConfigPropertyService", key = "#root.methodName+':' +#p0")
    public String getConfigValueByKey(String key) {
        return configPropertyDao.getConfigValueByKey(key);
    }

    @Override
    @Cacheable(value = "ConfigPropertyService", key = "#root.methodName+':' +#p0")
    public List<String> getConfigValueByGroupName(String groupName) {
        ConfigPropertyPo configPropertyPo = new ConfigPropertyPo();
        configPropertyPo.setGroupName(groupName);
        List<ConfigPropertyPo> configPropertyPos = configPropertyDao.gtcSearch(configPropertyPo);
        if (GutilObject.isEmpty(configPropertyPos)) {
            return ListUtil.empty();
        } else {
            ArrayList<String> strings = new ArrayList<>();
            for (ConfigPropertyPo configProperty : configPropertyPos) {
                strings.add(configProperty.getValue());
            }
            return strings;
        }

    }

    @Override
    @Cacheable(value = "ConfigPropertyService", key = "#root.methodName+':' +#p0+#p1")
    public String getConfigValueByKey(String key, String ifNullValue) {
        return configPropertyDao.getConfigValueByKey(key, ifNullValue);
    }

    @Override
    @CacheEvict(value = "ConfigPropertyService", allEntries = true)
    public void refreshCache() {

    }

    public static class ConfigConstant {

    }

}
