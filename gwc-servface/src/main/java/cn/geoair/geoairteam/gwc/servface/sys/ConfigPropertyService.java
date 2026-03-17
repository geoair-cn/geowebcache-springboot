package cn.geoair.geoairteam.gwc.servface.sys;

import cn.geoair.geoairteam.gwc.model.sys.entity.ConfigPropertyPo;
import cn.geoair.geoairteam.gwc.dao.sys.ConfigPropertyDao;

import java.util.List;

/**
 * 配置属性表Servface接口
 *
 * @author zhangjun
 * @date 2024-09-04
 */
public interface ConfigPropertyService {


    ConfigPropertyDao getConfigPropertyDao();

    ConfigPropertyPo getConfigByKey(String key);

    String getConfigValueByKey(String key);

    List<String> getConfigValueByGroupName(String groupName);

    String getConfigValueByKey(String key, String ifNullValue);

    void refreshCache();

}
