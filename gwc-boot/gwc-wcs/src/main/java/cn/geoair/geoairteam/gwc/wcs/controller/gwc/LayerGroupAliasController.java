package cn.geoair.geoairteam.gwc.wcs.controller.gwc;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.geoair.base.util.GutilObject;
import cn.geoair.base.data.result.GirEmAlertType;
import java.util.List;
import cn.geoair.base.exception.GirException;
import cn.geoair.base.api.annotation.GaApi;
import cn.geoair.base.api.annotation.GaApiAction;
import cn.geoair.base.data.page.support.GirPager;
import cn.geoair.geoairteam.gwc.dao.gwc.LayerGroupAliasDao;
import cn.geoair.geoairteam.gwc.model.gwc.dto.LayerGroupAliasDto;
import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerGroupAliasPo;
import cn.geoair.geoairteam.gwc.model.gwc.seo.LayerGroupAliasSeo;
import cn.geoair.geoairteam.gwc.servface.gwc.LayerGroupAliasService;
import cn.geoair.geoairteam.gwc.wcs.controller.gwc.layergroupalias.LayerGroupAliasAddVo;
import cn.geoair.geoairteam.gwc.wcs.controller.gwc.layergroupalias.LayerGroupAliasUpdateVo;
import cn.geoair.geoairteam.gwc.wcs.controller.gwc.layergroupalias.LayerGroupAliasSearchVo;
import cn.geoair.geoairteam.gwc.wcs.controller.gwc.layergroupalias.LayerGroupAliasDetailVo;
import cn.geoair.base.data.page.GiPager;
import cn.geoair.base.data.page.GiPageParam;
import cn.geoair.base.data.result.GiResult;
import cn.geoair.base.data.GirValidateException;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import cn.geoair.base.log.GiLogger;
import cn.geoair.base.log.GirLogger;

/**
 * 图层组别名表Controller
 *
 * @author geoairteam
 * @date 2026-03-11 15:44:18
 */
@RestController
@RequestMapping("/gwc/TgwcLayerGroupAlias")
@GaApi(tags = "图层组别名表")
public class LayerGroupAliasController {
    private static final GiLogger logger = GirLogger.getLoger(LayerGroupAliasController.class);
    @Resource
    private LayerGroupAliasDao layerGroupAliasDao;
    @Resource
    private LayerGroupAliasService layerGroupAliasService;

            
            /**
             * 新增图层组别名表
             */
            @GaApiAction(text = "新增图层组别名表")
            @PostMapping("/addLayerGroupAlias")
            public GiResult<LayerGroupAliasDetailVo> addLayerGroupAlias(@Validated @RequestBody LayerGroupAliasAddVo param) {
                // 转换为PO
                    LayerGroupAliasPo record = new LayerGroupAliasPo();
                BeanUtils.copyProperties(param, record);

                // 生成ID
                record.setId("LayerGroupAlias_".toLowerCase() + IdUtil.getSnowflakeNextIdStr());

                // 调用Service
                    LayerGroupAliasPo result = layerGroupAliasService.add(record);

                return GiResult.successValue(LayerGroupAliasDetailVo.fromPo(result))
                        .andAlertMsg("操作成功")
                        .andAlertTypeEnum(GirEmAlertType.无需关闭的提示1);
            }

            /**
             * 修改图层组别名表
             */
            @GaApiAction(text = "修改图层组别名表")
            @PostMapping("/updateLayerGroupAlias")
            public GiResult<LayerGroupAliasDetailVo> updateLayerGroupAlias(@Validated @RequestBody LayerGroupAliasUpdateVo param) {
                // 转换为PO
                    LayerGroupAliasPo record = new LayerGroupAliasPo();
                BeanUtils.copyProperties(param, record);

                // 调用Service
                    LayerGroupAliasPo result = layerGroupAliasService.update(record);

                return GiResult.successValue(LayerGroupAliasDetailVo.fromPo(result))
                        .andAlertMsg("操作成功")
                        .andAlertTypeEnum(GirEmAlertType.无需关闭的提示1);
            }

            /**
             * 图层组别名表详情
             */
            @GaApiAction(text = "图层组别名表详情")
            @GetMapping("/loadLayerGroupAlias")
            public GiResult<LayerGroupAliasDetailVo> loadLayerGroupAlias(String id) {
                    LayerGroupAliasDto dto = layerGroupAliasService.getDtoById(id);
                if (dto == null) {
                    throw new GirValidateException("图层组别名表不存在");
                }

                // 转换为VO
                    LayerGroupAliasDetailVo vo = LayerGroupAliasDetailVo.fromDto(dto);
                return GiResult.successValue(vo);
            }

            /**
             * 删除图层组别名表
             */
            @GaApiAction(text = "删除图层组别名表")
            @PostMapping("/delLayerGroupAlias")
            public GiResult<?> delLayerGroupAlias(String id, boolean confirmed) {
                if (confirmed) {
                        LayerGroupAliasPo po = layerGroupAliasService.getPoById(id);
                    if (po == null) {
                        throw new GirValidateException("图层组别名表不存在");
                    }
                        layerGroupAliasService.deleteById(po.id());

                    return GiResult.getResult(null)
                            .andAlertMsg("操作成功")
                            .andAlertTypeEnum(GirEmAlertType.无需关闭的提示1);
                } else {
                    return GiResult.getResult(null)
                            .andAlertMsg("确定要删除图层组别名表吗?")
                            .andAlertTypeEnum(GirEmAlertType.弹出确认警告5);
                }
            }

            /**
             * 逻辑删除图层组别名表
             */
            @GaApiAction(text = "逻辑删除图层组别名表")
            @PostMapping("/logicDelLayerGroupAlias/{ids}")
            public GiResult<?> logicDelLayerGroupAlias(@PathVariable String[] ids, boolean confirmed) {
                if (confirmed) {
                    if (GutilObject.isEmpty(ids)) {
                        throw new GirException("ids为空!");
                    }

                        LayerGroupAliasSeo seo = new LayerGroupAliasSeo();
                    seo.setAndIdsIn(ids);
                    List<LayerGroupAliasDto> dtos = layerGroupAliasService.getDtoListBySeo(seo);

                    if (GutilObject.isEmpty(dtos)) {
                        throw new GirException("删除内容为空!");
                    }

                    for (LayerGroupAliasDto dto : dtos) {
                            LayerGroupAliasPo po = layerGroupAliasService.getPoById(dto.getId());
                            layerGroupAliasService.logicDeleteById(po.id());
                    }

                    return GiResult.getResult(null)
                            .andAlertMsg("操作成功")
                            .andAlertTypeEnum(GirEmAlertType.无需关闭的提示1);
                } else {
                    return GiResult.getResult(null)
                            .andAlertMsg("确定要删除图层组别名表吗?")
                            .andAlertTypeEnum(GirEmAlertType.弹出确认警告5);
                }
            }

            /**
             * 分页列出图层组别名表
             */
            @GaApiAction(text = "分页列出图层组别名表")
            @PostMapping("/listLayerGroupAliasPage")
            public GiResult<GirPager<LayerGroupAliasDetailVo>> listLayerGroupAliasPage(@Validated @RequestBody LayerGroupAliasSearchVo param) {
                // 构建查询条件
                    LayerGroupAliasSeo seo = new LayerGroupAliasSeo();
                BeanUtils.copyProperties(param, seo);
                seo.setNotDel();

                if (GutilObject.isNotEmpty(param.getQueryContent())) {
                    seo.setAndQueryContentIn(ArrayUtil.toArray(ListUtil.of(param.getQueryContent()), String.class));
                }

                // 分页查询
                GiPager<LayerGroupAliasDto> giPager = layerGroupAliasService.getDtoPageBySeo(seo, GiPageParam.of());

                // 转换为VO
                List<LayerGroupAliasDetailVo> voList = LayerGroupAliasDetailVo.fromDtos(giPager.value());

                // 封装分页结果
                GirPager<LayerGroupAliasDetailVo> result = new GirPager<>();
                result.put(voList, giPager.total(), giPager.pageParam());

                return GiResult.successValue(result);
            }

            /**
             * 刷新缓存
             */
            @GaApiAction(text = "刷新缓存")
            @PostMapping("/refreshCache")
            public GiResult<?> refreshCache() {
                    layerGroupAliasService.refreshCache();
                return GiResult.successValue(null).andAlertMsg("缓存刷新成功");
            }
                                                            }
