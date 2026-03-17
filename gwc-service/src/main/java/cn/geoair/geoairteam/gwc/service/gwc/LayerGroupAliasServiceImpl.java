package cn.geoair.geoairteam.gwc.service.gwc;

import cn.geoair.base.log.GiLogger;
import cn.geoair.base.log.GirLogger;
import cn.geoair.base.data.page.GiPageParam;
import cn.geoair.base.data.page.GiPager;
import cn.geoair.base.util.GutilObject;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.geoair.geoairteam.gwc.dao.gwc.LayerGroupAliasDao;
import cn.geoair.geoairteam.gwc.model.gwc.dto.LayerGroupAliasDto;
import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerGroupAliasPo;
import cn.geoair.geoairteam.gwc.model.gwc.seo.LayerGroupAliasSeo;
import cn.geoair.geoairteam.gwc.servface.gwc.LayerGroupAliasService;
import cn.geoair.geoairteam.gwc.servface.gwc.event.LayerGroupAliasEvent;
import cn.geoair.geoairteam.gwc.service.utils.GtcGwcAliasNameUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * 图层组别名表Service业务层处理
 *
 * @author geoairteam
 * @date 2026-03-11 15:44:18
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class LayerGroupAliasServiceImpl implements LayerGroupAliasService {
    private static final GiLogger log = GirLogger.getLoger(LayerGroupAliasServiceImpl.class);
    @Resource
    private LayerGroupAliasDao layerGroupAliasDao;

    @Override
    public LayerGroupAliasPo add(LayerGroupAliasPo po) {
        // 初始化创建元数据
        po.initCreateMeta();
        // 插入数据
        layerGroupAliasDao.gtcAccessSelective(po);
        // 触发新增事件
        triggerAddEvent(po);
        return po;
    }

    @Override
    public LayerGroupAliasPo update(LayerGroupAliasPo po) {
        // 获取原对象
        LayerGroupAliasPo oldPo = layerGroupAliasDao.gtcSearchByPK(po.id());
        if (oldPo == null) {
            throw new RuntimeException("数据不存在，ID：" + po.id());
        }
        // 初始化更新元数据
        po.initUpdateMeta();
        // 触发更新事件
        triggerUpdateEvent(oldPo, po);
        // 更新数据
        layerGroupAliasDao.gtcUpdateByPKSelective(po);
        return po;
    }

    @Override
    public void deleteById(String id) {
        // 获取原对象
        LayerGroupAliasPo po = layerGroupAliasDao.gtcSearchByPK(id);
        if (po == null) {
            return;
        }
        // 触发删除事件
        triggerDeleteEvent(po);
        // 物理删除
        layerGroupAliasDao.gtcDeleteByPK(id);
    }

    @Override
    public void logicDeleteById(String id) {
        // 获取原对象
        LayerGroupAliasPo po = layerGroupAliasDao.gtcSearchByPK(id);
        if (po == null) {
            return;
        }
        // 触发删除事件
        triggerDeleteEvent(po);
        // 逻辑删除
        po.setDel();
        layerGroupAliasDao.gtcUpdateByPKSelective(po);
    }

    @Override
    public LayerGroupAliasDto getDtoById(String id) {
        LayerGroupAliasPo po = layerGroupAliasDao.gtcSearchByPK(id);
        return po != null ? LayerGroupAliasDto.ofLayerGroupAliasPo(po) : null;
    }

    @Override
    public LayerGroupAliasPo getPoById(String id) {
        return layerGroupAliasDao.gtcSearchByPK(id);
    }

    @Override
    public List<LayerGroupAliasDto> getDtoListBySeo(LayerGroupAliasSeo seo) {
        seo.setNotDel();
        return layerGroupAliasDao.searchList(seo);
    }

    @Override
    public GiPager<LayerGroupAliasDto> getDtoPageBySeo(LayerGroupAliasSeo seo, GiPageParam pageParam) {
        seo.setNotDel();
        return layerGroupAliasDao.searchListPage(seo, pageParam);
    }

    @Override
    public List<LayerGroupAliasPo> getPoListBySeo(LayerGroupAliasSeo seo) {
        seo.setNotDel();
        return layerGroupAliasDao.gtcSearch(seo);
    }

    @Override
    public <T> Map<String, LayerGroupAliasDto> getDtoMapByField(List<T> beanList, String fieldName) {
        Map<String, LayerGroupAliasDto> resultMap = new HashMap<>();
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
        LayerGroupAliasSeo seo = new LayerGroupAliasSeo();
        seo.setNotDel();
        seo.setAndIdsIn(ArrayUtil.toArray(fieldValues, String.class));
        List<LayerGroupAliasDto> dtos = layerGroupAliasDao.searchList(seo);
        for (LayerGroupAliasDto dto : dtos) {
            resultMap.put(dto.getId(), dto);
        }
        return resultMap;
    }

    @Override
    public void refreshCache() {
        // 缓存已禁用，无需刷新
        log.debug("缓存已禁用，跳过刷新：{}", "layerGroupAlias");
    }

    @Override
    public String searchLayerStringByLayerGroupAlias(String layerGroupAlias) {
        GtcGwcAliasNameUtils gtcGwcAliasNameUtils = GtcGwcAliasNameUtils.of(layerGroupAlias);
        String realName = gtcGwcAliasNameUtils.getRealName();
        if (realName == null) {
            String s = layerGroupAliasDao.searchLayerStringByLayerGroupAlias(layerGroupAlias);
            if (s == null) {
                gtcGwcAliasNameUtils.putRealName("empty");
            } else {
                gtcGwcAliasNameUtils.putRealName(s);
            }
            return s;
        } else {
            if (realName.equals("empty")) {
                return null;
            }
            return realName;
        }
    }


    /**
     * 触发新增事件
     */
    private void triggerAddEvent(LayerGroupAliasPo po) {
        List<LayerGroupAliasEvent> events = LayerGroupAliasEvent.getInstances();
        for (LayerGroupAliasEvent event : events) {
            try {
                event.addLayerGroupAliasEvent(po);
            } catch (Exception e) {
                log.error("触发新增事件失败", e);
            }
        }
    }

    /**
     * 触发更新事件
     */
    private void triggerUpdateEvent(LayerGroupAliasPo oldPo, LayerGroupAliasPo newPo) {
        List<LayerGroupAliasEvent> events = LayerGroupAliasEvent.getInstances();
        for (LayerGroupAliasEvent event : events) {
            try {
                event.updateLayerGroupAliasEvent(oldPo, newPo);
            } catch (Exception e) {
                log.error("触发更新事件失败", e);
            }
        }
    }

    /**
     * 触发删除事件
     */
    private void triggerDeleteEvent(LayerGroupAliasPo po) {
        List<LayerGroupAliasEvent> events = LayerGroupAliasEvent.getInstances();
        for (LayerGroupAliasEvent event : events) {
            try {
                event.delLayerGroupAliasEvent(po);
            } catch (Exception e) {
                log.error("触发删除事件失败", e);
            }
        }
    }
}
