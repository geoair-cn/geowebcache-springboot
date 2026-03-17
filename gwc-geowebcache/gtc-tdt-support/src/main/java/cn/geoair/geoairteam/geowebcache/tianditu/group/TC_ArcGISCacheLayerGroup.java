package cn.geoair.geoairteam.geowebcache.tianditu.group;


import cn.geoair.base.util.GutilObject;
import cn.geoair.geoairteam.geowebcache.tianditu.layer.TC_ArcGISCacheLayer;
import java.util.HashMap;

/**
 * @description: arcgislayer的图层组
 * @author: zhang_jun
 * @create: 2021-09-24 13:38
 */
public class TC_ArcGISCacheLayerGroup {
    private static HashMap<String, GroupMeta> layerHashMap = new HashMap<>();

    public static HashMap<String, GroupMeta> getGroupMetas() {
        return layerHashMap;
    }

    public static void putGroup(String group, TC_ArcGISCacheLayer tc_arcGISCacheLayer) {
        GroupMeta groupMeta = layerHashMap.get(group);
        if (groupMeta == null) {
            GroupMeta groupMeta1 = new GroupMeta(group, tc_arcGISCacheLayer);
            layerHashMap.put(group, groupMeta1);
        } else {
            GroupMeta groupMeta1 = layerHashMap.get(group);
            groupMeta1.addTC_ArcGISCacheLayer(tc_arcGISCacheLayer);
            layerHashMap.put(group, groupMeta1);
        }
        //        System.out.println(tc_arcGISCacheLayers);
    }

    public static GroupMeta getLayerGroup(String group) {
        return layerHashMap.get(group);
    }

    public static String getLayerGroupRemark(String group) {
        GroupMeta groupMeta = layerHashMap.get(group);
        if (GutilObject.isEmpty(groupMeta)) {
            return "";
        } else {
            return groupMeta.getRemark();
        }
    }
}
