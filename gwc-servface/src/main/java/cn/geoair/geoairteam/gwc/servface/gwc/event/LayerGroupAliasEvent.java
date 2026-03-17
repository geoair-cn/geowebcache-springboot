package cn.geoair.geoairteam.gwc.servface.gwc.event;
import cn.geoair.base.Gir;
import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerGroupAliasPo;
import java.util.*;
import cn.hutool.core.collection.ListUtil;
import java.util.Map;
/**
 * 图层组别名表事件接口
 *
 * @author geoairteam
 * @date 2026-03-11 15:44:18
 */
public interface LayerGroupAliasEvent {

    static List<LayerGroupAliasEvent> getInstances() {
        List<LayerGroupAliasEvent> events = new ArrayList<>();
        Map<String, LayerGroupAliasEvent> beans = Gir.beans.getBeans(LayerGroupAliasEvent.class);
        for (Map.Entry<String, LayerGroupAliasEvent> event : beans.entrySet()) {
            events.add(event.getValue());
        }
        return   ListUtil.sort(events, new Comparator<LayerGroupAliasEvent>() {
            @Override
            public int compare(LayerGroupAliasEvent o1, LayerGroupAliasEvent o2) {
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
* @param layerGroupAliasPo
*/
    default void addLayerGroupAliasEvent(LayerGroupAliasPo layerGroupAliasPo) {
    }

    /**
 * 删除事件
 *
     * @param layerGroupAliasPo
 */
    void delLayerGroupAliasEvent(LayerGroupAliasPo layerGroupAliasPo);



    /**
     * 更新事件
     *
     * @param oldPo 旧的对象
     * @param newPo 新的对象
     */
    default void updateLayerGroupAliasEvent(LayerGroupAliasPo oldPo, LayerGroupAliasPo newPo) {
    }

    void getLayerGroupAliasRelationEvent(LayerGroupAliasPo po,Map<String, Object> relationObj) ;
}
