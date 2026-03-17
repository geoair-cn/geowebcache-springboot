package cn.geoair.geoairteam.geowebcache.tianditu.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.GeoWebCacheExtensions;
import org.geowebcache.service.Service;

/**
 * @description:
 * @author: zhang_jun
 * @create: 2021-09-24 17:55
 */
public class GeowebcacheUtils {

    private static Log log =
            LogFactory.getLog(GeowebcacheUtils.class);

    Map<String, Service> services;

    public Service findService(String serviceStr) throws GeoWebCacheException {

        if (this.services == null) {
            this.services = loadServices();
        }

        // E.g. /wms/test -> /wms
        Service service = services.get(serviceStr);
        if (service == null) {
            if (serviceStr == null || serviceStr.length() == 0) {
                serviceStr = ", try service/&lt;name of service&gt;";
            } else {
                serviceStr = " \"" + serviceStr + "\"";
            }
            throw new GeoWebCacheException("无法找到服务" + serviceStr);
        }
        return service;
    }

    private Map<String, Service> loadServices() {
        log.info("GeoAir正在加载所有服务");
        List<Service> plugins = GeoWebCacheExtensions.extensions(Service.class);
        Map<String, Service> services = new HashMap<>();
        // Give all service objects direct access to the tileLayerDispatcher
        for (Service aService : plugins) {
            services.put(aService.getPathName(), aService);
        }
        log.info("完成加载 GWC 服务扩展。成立 : " + new ArrayList<>(services.keySet()));
        return services;
    }
}
