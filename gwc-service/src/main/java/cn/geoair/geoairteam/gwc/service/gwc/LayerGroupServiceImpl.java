package cn.geoair.geoairteam.gwc.service.gwc;

import cn.geoair.base.log.GiLogger;
import cn.geoair.base.log.GirLogger;
import cn.geoair.base.data.page.GiPageParam;
import cn.geoair.base.data.page.GiPager;
import cn.geoair.base.util.GutilObject;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.geoair.geoairteam.gwc.dao.gwc.LayerGroupDao;
import cn.geoair.geoairteam.gwc.model.gwc.dto.LayerGroupDto;
import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerGroupPo;
import cn.geoair.geoairteam.gwc.model.gwc.seo.LayerGroupSeo;
import cn.geoair.geoairteam.gwc.servface.gwc.LayerGroupService;
import cn.geoair.geoairteam.gwc.servface.gwc.event.LayerGroupEvent;
    import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * ${tableComment}Service业务层处理
 *
 * @author geoairteam
 * @date 2026-03-11 15:59:37
 */
@Service
    @Transactional(rollbackFor = Exception.class)
public class LayerGroupServiceImpl implements LayerGroupService {
    private static final GiLogger log = GirLogger.getLoger(LayerGroupServiceImpl.class);
    @Resource
    private LayerGroupDao layerGroupDao;

    @Override
            public LayerGroupPo add(LayerGroupPo po) {
        // 初始化创建元数据
        po.initCreateMeta();
        // 插入数据
            layerGroupDao.gtcAccessSelective(po);
        // 触发新增事件
        triggerAddEvent(po);
        return po;
    }

    @Override
            public LayerGroupPo update(LayerGroupPo po) {
        // 获取原对象
            LayerGroupPo oldPo = layerGroupDao.gtcSearchByPK(po.id());
        if (oldPo == null) {
            throw new RuntimeException("数据不存在，ID：" + po.id());
        }
        // 初始化更新元数据
        po.initUpdateMeta();
        // 触发更新事件
        triggerUpdateEvent(oldPo, po);
        // 更新数据
            layerGroupDao.gtcUpdateByPKSelective(po);
        return po;
    }

    @Override
            public void deleteById(String id) {
        // 获取原对象
            LayerGroupPo po = layerGroupDao.gtcSearchByPK(id);
        if (po == null) {
            return;
        }
        // 触发删除事件
        triggerDeleteEvent(po);
        // 物理删除
            layerGroupDao.gtcDeleteByPK(id);
    }

    @Override
            public void logicDeleteById(String id) {
        // 获取原对象
            LayerGroupPo po = layerGroupDao.gtcSearchByPK(id);
        if (po == null) {
            return;
        }
        // 触发删除事件
        triggerDeleteEvent(po);
        // 逻辑删除
        po.setDel();
            layerGroupDao.gtcUpdateByPKSelective(po);
    }

    @Override
            public LayerGroupDto getDtoById(String id) {
            LayerGroupPo po = layerGroupDao.gtcSearchByPK(id);
        return po != null ? LayerGroupDto.ofLayerGroupPo(po) : null;
    }

    @Override
    public LayerGroupPo getPoById(String id) {
        return layerGroupDao.gtcSearchByPK(id);
    }

    @Override
    public List<LayerGroupDto> getDtoListBySeo(LayerGroupSeo seo) {
        seo.setNotDel();
        return layerGroupDao.searchList(seo);
    }

    @Override
    public GiPager<LayerGroupDto> getDtoPageBySeo(LayerGroupSeo seo, GiPageParam pageParam) {
        seo.setNotDel();
        return layerGroupDao.searchListPage(seo, pageParam);
    }

    @Override
    public List<LayerGroupPo> getPoListBySeo(LayerGroupSeo seo) {
        seo.setNotDel();
        return layerGroupDao.gtcSearch(seo);
    }

    @Override
    public <T> Map<String, LayerGroupDto> getDtoMapByField(List<T> beanList, String fieldName) {
        Map<String, LayerGroupDto> resultMap = new HashMap<>();
        if (GutilObject.isEmpty(beanList)) {
            return resultMap;
        }
        // 收集字段值
        Set<String> fieldValues = new HashSet<>();
        for (T bean : beanList) {
            Object value = ReflectUtil.getFieldValue(bean, fieldName);
            if (value instanceof String) {
                fieldValues.add((String) value);
            }
        }
        if (fieldValues.isEmpty()) {
            return resultMap;
        }
        // 查询数据
            LayerGroupSeo seo = new LayerGroupSeo();
        seo.setNotDel();
        seo.setAndIdsIn(ArrayUtil.toArray(fieldValues, String.class));
        List<LayerGroupDto> dtos = layerGroupDao.searchList(seo);
        for (LayerGroupDto dto : dtos) {
            resultMap.put(dto.getId(), dto);
        }
        return resultMap;
    }

    @Override
            public void refreshCache() {
                    // 缓存已禁用，无需刷新
            log.debug("缓存已禁用，跳过刷新：{}", "layerGroup");
            }


    /**
     * 触发新增事件
     */
    private void triggerAddEvent(LayerGroupPo po) {
        List<LayerGroupEvent> events = LayerGroupEvent.getInstances();
        for (LayerGroupEvent event : events) {
            try {
                event.addLayerGroupEvent(po);
            } catch (Exception e) {
                log.error("触发新增事件失败", e);
            }
        }
    }

    /**
     * 触发更新事件
     */
    private void triggerUpdateEvent(LayerGroupPo oldPo, LayerGroupPo newPo) {
        List<LayerGroupEvent> events = LayerGroupEvent.getInstances();
        for (LayerGroupEvent event : events) {
            try {
                event.updateLayerGroupEvent(oldPo, newPo);
            } catch (Exception e) {
                log.error("触发更新事件失败", e);
            }
        }
    }

    /**
     * 触发删除事件
     */
    private void triggerDeleteEvent(LayerGroupPo po) {
        List<LayerGroupEvent> events = LayerGroupEvent.getInstances();
        for (LayerGroupEvent event : events) {
            try {
                event.delLayerGroupEvent(po);
            } catch (Exception e) {
                log.error("触发删除事件失败", e);
            }
        }
    }
}
