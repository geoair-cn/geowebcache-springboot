package cn.geoair.geoairteam.gwc.service.gwc;

import cn.geoair.base.log.GiLogger;
import cn.geoair.base.log.GirLogger;
import cn.geoair.base.data.page.GiPageParam;
import cn.geoair.base.data.page.GiPager;
import cn.geoair.base.util.GutilObject;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.XmlUtil;
import cn.geoair.geoairteam.gwc.dao.gwc.LayerInfoDao;
import cn.geoair.geoairteam.gwc.model.gwc.dto.LayerInfoDto;
import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerInfoPo;
import cn.geoair.geoairteam.gwc.model.gwc.seo.LayerInfoSeo;
import cn.geoair.geoairteam.gwc.servface.gwc.LayerInfoService;
import cn.geoair.geoairteam.gwc.servface.gwc.event.LayerInfoEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;

/**
 * 图层信息表Service业务层处理
 *
 * @author geoairteam
 * @date 2026-03-11 15:45:16
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class LayerInfoServiceImpl implements LayerInfoService {
    private static final GiLogger log = GirLogger.getLoger(LayerInfoServiceImpl.class);
    @Resource
    private LayerInfoDao layerInfoDao;

    @Override
    public LayerInfoPo add(LayerInfoPo po) {
        // 初始化创建元数据
        po.initCreateMeta();
        // 插入数据
        layerInfoDao.gtcAccessSelective(po);
        // 触发新增事件
        triggerAddEvent(po);
        return po;
    }

    @Override
    public LayerInfoPo update(LayerInfoPo po) {
        // 获取原对象
        LayerInfoPo oldPo = layerInfoDao.gtcSearchByPK(po.id());
        if (oldPo == null) {
            throw new RuntimeException("数据不存在，ID：" + po.id());
        }
        // 初始化更新元数据
        po.initUpdateMeta();
        // 触发更新事件
        triggerUpdateEvent(oldPo, po);
        // 更新数据
        layerInfoDao.gtcUpdateByPKSelective(po);
        return po;
    }

    @Override
    public void deleteById(String id) {
        // 获取原对象
        LayerInfoPo po = layerInfoDao.gtcSearchByPK(id);
        if (po == null) {
            return;
        }
        // 触发删除事件
        triggerDeleteEvent(po);
        // 物理删除
        layerInfoDao.gtcDeleteByPK(id);
    }

    @Override
    public void logicDeleteById(String id) {
        // 获取原对象
        LayerInfoPo po = layerInfoDao.gtcSearchByPK(id);
        if (po == null) {
            return;
        }
        // 触发删除事件
        triggerDeleteEvent(po);
        // 逻辑删除
        po.setDel();
        layerInfoDao.gtcUpdateByPKSelective(po);
    }

    @Override
    public LayerInfoDto getDtoById(String id) {
        LayerInfoPo po = layerInfoDao.gtcSearchByPK(id);
        return po != null ? LayerInfoDto.ofLayerInfoPo(po) : null;
    }

    @Override
    public LayerInfoPo getPoById(String id) {
        return layerInfoDao.gtcSearchByPK(id);
    }

    @Override
    public List<LayerInfoDto> getDtoListBySeo(LayerInfoSeo seo) {
        seo.setNotDel();
        return layerInfoDao.searchList(seo);
    }

    @Override
    public GiPager<LayerInfoDto> getDtoPageBySeo(LayerInfoSeo seo, GiPageParam pageParam) {
        seo.setNotDel();
        return layerInfoDao.searchListPage(seo, pageParam);
    }

    @Override
    public List<LayerInfoPo> getPoListBySeo(LayerInfoSeo seo) {
        seo.setNotDel();
        return layerInfoDao.gtcSearch(seo);
    }

    @Override
    public <T> Map<String, LayerInfoDto> getDtoMapByField(List<T> beanList, String fieldName) {
        Map<String, LayerInfoDto> resultMap = new HashMap<>();
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
        LayerInfoSeo seo = new LayerInfoSeo();
        seo.setNotDel();
        seo.setAndIdsIn(ArrayUtil.toArray(fieldValues, String.class));
        List<LayerInfoDto> dtos = layerInfoDao.searchList(seo);
        for (LayerInfoDto dto : dtos) {
            resultMap.put(dto.getId(), dto);
        }
        return resultMap;
    }

    @Override
    public void refreshCache() {
        // 缓存已禁用，无需刷新
        log.debug("缓存已禁用，跳过刷新：{}", "layerInfo");
    }


    /**
     * 触发新增事件
     */
    private void triggerAddEvent(LayerInfoPo po) {
        List<LayerInfoEvent> events = LayerInfoEvent.getInstances();
        for (LayerInfoEvent event : events) {
            try {
                event.addLayerInfoEvent(po);
            } catch (Exception e) {
                log.error("触发新增事件失败", e);
            }
        }
    }

    /**
     * 触发更新事件
     */
    private void triggerUpdateEvent(LayerInfoPo oldPo, LayerInfoPo newPo) {
        List<LayerInfoEvent> events = LayerInfoEvent.getInstances();
        for (LayerInfoEvent event : events) {
            try {
                event.updateLayerInfoEvent(oldPo, newPo);
            } catch (Exception e) {
                log.error("触发更新事件失败", e);
            }
        }
    }

    /**
     * 触发删除事件
     */
    private void triggerDeleteEvent(LayerInfoPo po) {
        List<LayerInfoEvent> events = LayerInfoEvent.getInstances();
        for (LayerInfoEvent event : events) {
            try {
                event.delLayerInfoEvent(po);
            } catch (Exception e) {
                log.error("触发删除事件失败", e);
            }
        }
    }

    public void readXml(File xmlFile) {

        if (!xmlFile.exists()) {
            return;
        }

        Map<String, Object> stringObjectMap = XmlUtil.xmlToMap(XmlUtil.toStr(XmlUtil.readXML(xmlFile)));
        Object layers1 = stringObjectMap.get("layers");
        if (GutilObject.isEmpty(layers1)) {
            return;
        }
        if (!(layers1 instanceof Map)) {
            return;
        }
        Map layers = (Map) layers1;
        try {
            ArrayList<Map> arcgisLayers = (ArrayList<Map>) layers.get("tc-arcgisLayer");
            if (GutilObject.isEmpty(arcgisLayers)) {
                return;
            }
            for (Map<String, String> arcgisLayer : arcgisLayers) {
                String name = arcgisLayer.get("name");
                String sortkey = arcgisLayer.get("sortkey");
                String tilingScheme = arcgisLayer.get("tilingScheme");
                String tileCachePath = arcgisLayer.get("tileCachePath");
                String hexZoom = arcgisLayer.get("hexZoom");
                String group = arcgisLayer.get("group");
                LayerInfoPo layerInfoPo = new LayerInfoPo();
                layerInfoPo.setArcLayerType("tc-arcgisLayer");
                layerInfoPo.setEnableIs("启用");
                layerInfoPo.setId(name);
                layerInfoPo.setLayerName(name);
                layerInfoPo.setSortKey(sortkey);
                layerInfoPo.setTileCachePath(tileCachePath);
                layerInfoPo.setTilingScheme(tilingScheme);
                layerInfoPo.setTimeCreate(new Date());
                layerInfoPo.setGroupId(group);
                layerInfoPo.setHexZoom(hexZoom);
                LayerInfoPo layerInfoPo1 = layerInfoDao.gtcSearchByPK(name);
                if (layerInfoPo1 == null) {
                    layerInfoDao.gtcAccessSelective(layerInfoPo);
                } else {
                    layerInfoDao.gtcUpdateByPKSelective(layerInfoPo);
                }
            }
        } catch (Exception e) {
            return;
        }


    }

    @Override
    public void updateBbox(String id) {
        layerInfoDao.updateBbox(id);
    }
}
