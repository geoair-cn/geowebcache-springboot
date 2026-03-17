package cn.geoair.geoairteam.gwc.servface.gwc.event;
import cn.geoair.base.Gir;
import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerGroupPo;
import java.util.*;
import cn.hutool.core.collection.ListUtil;
import java.util.Map;
/**
 * ${tableComment}事件接口
 *
 * @author geoairteam
 * @date 2026-03-11 15:59:37
 */
public interface LayerGroupEvent {

    static List<LayerGroupEvent> getInstances() {
        List<LayerGroupEvent> events = new ArrayList<>();
        Map<String, LayerGroupEvent> beans = Gir.beans.getBeans(LayerGroupEvent.class);
        for (Map.Entry<String, LayerGroupEvent> event : beans.entrySet()) {
            events.add(event.getValue());
        }
        return   ListUtil.sort(events, new Comparator<LayerGroupEvent>() {
            @Override
            public int compare(LayerGroupEvent o1, LayerGroupEvent o2) {
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
* @param layerGroupPo
*/
    default void addLayerGroupEvent(LayerGroupPo layerGroupPo) {
    }

    /**
 * 删除事件
 *
     * @param layerGroupPo
 */
    void delLayerGroupEvent(LayerGroupPo layerGroupPo);



    /**
     * 更新事件
     *
     * @param oldPo 旧的对象
     * @param newPo 新的对象
     */
    default void updateLayerGroupEvent(LayerGroupPo oldPo, LayerGroupPo newPo) {
    }

    void getLayerGroupRelationEvent(LayerGroupPo po,Map<String, Object> relationObj) ;
}
