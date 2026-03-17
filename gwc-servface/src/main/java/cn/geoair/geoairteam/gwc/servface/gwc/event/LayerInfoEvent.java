package cn.geoair.geoairteam.gwc.servface.gwc.event;
import cn.geoair.base.Gir;
import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerInfoPo;
import java.util.*;
import cn.hutool.core.collection.ListUtil;
import java.util.Map;
/**
 * 图层信息表事件接口
 *
 * @author geoairteam
 * @date 2026-03-11 15:45:16
 */
public interface LayerInfoEvent {

    static List<LayerInfoEvent> getInstances() {
        List<LayerInfoEvent> events = new ArrayList<>();
        Map<String, LayerInfoEvent> beans = Gir.beans.getBeans(LayerInfoEvent.class);
        for (Map.Entry<String, LayerInfoEvent> event : beans.entrySet()) {
            events.add(event.getValue());
        }
        return   ListUtil.sort(events, new Comparator<LayerInfoEvent>() {
            @Override
            public int compare(LayerInfoEvent o1, LayerInfoEvent o2) {
                return o1.getOrder() - o2.getOrder();
            }
        });
    }

    /**
 * 获取排序,bean的顺序根据 1->无穷大排列
 *
 * @return
 */
    default   Integer getOrder() {
        return 10;
    }
    /**
* 新增事件
*
* @param layerInfoPo
*/
    default void addLayerInfoEvent(LayerInfoPo layerInfoPo) {
    }

    /**
 * 删除事件
 *
     * @param layerInfoPo
 */
    void delLayerInfoEvent(LayerInfoPo layerInfoPo);



    /**
     * 更新事件
     *
     * @param oldPo 旧的对象
     * @param newPo 新的对象
     */
    default void updateLayerInfoEvent(LayerInfoPo oldPo, LayerInfoPo newPo) {
    }

    void getLayerInfoRelationEvent(LayerInfoPo po,Map<String, Object> relationObj) ;
}
