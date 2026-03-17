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
import cn.geoair.geoairteam.gwc.dao.gwc.LayerGroupDao;
import cn.geoair.geoairteam.gwc.model.gwc.dto.LayerGroupDto;
import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerGroupPo;
import cn.geoair.geoairteam.gwc.model.gwc.seo.LayerGroupSeo;
import cn.geoair.geoairteam.gwc.servface.gwc.LayerGroupService;
import cn.geoair.geoairteam.gwc.wcs.controller.gwc.layergroup.LayerGroupAddVo;
import cn.geoair.geoairteam.gwc.wcs.controller.gwc.layergroup.LayerGroupUpdateVo;
import cn.geoair.geoairteam.gwc.wcs.controller.gwc.layergroup.LayerGroupSearchVo;
import cn.geoair.geoairteam.gwc.wcs.controller.gwc.layergroup.LayerGroupDetailVo;
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
 * ${tableComment}Controller
 *
 * @author geoairteam
 * @date 2026-03-11 15:59:37
 */
@RestController
@RequestMapping("/gwc/TgwcLayerGroup")
@GaApi(tags = "${tableComment}")
public class LayerGroupController {
    private static final GiLogger logger = GirLogger.getLoger(LayerGroupController.class);
    @Resource
    private LayerGroupDao layerGroupDao;
    @Resource
    private LayerGroupService layerGroupService;

            
            /**
             * 新增${tableComment}
             */
            @GaApiAction(text = "新增${tableComment}")
            @PostMapping("/addLayerGroup")
            public GiResult<LayerGroupDetailVo> addLayerGroup(@Validated @RequestBody LayerGroupAddVo param) {
                // 转换为PO
                    LayerGroupPo record = new LayerGroupPo();
                BeanUtils.copyProperties(param, record);

                // 生成ID
                record.setId("LayerGroup_".toLowerCase() + IdUtil.getSnowflakeNextIdStr());

                // 调用Service
                    LayerGroupPo result = layerGroupService.add(record);

                return GiResult.successValue(LayerGroupDetailVo.fromPo(result))
                        .andAlertMsg("操作成功")
                        .andAlertTypeEnum(GirEmAlertType.无需关闭的提示1);
            }

            /**
             * 修改${tableComment}
             */
            @GaApiAction(text = "修改${tableComment}")
            @PostMapping("/updateLayerGroup")
            public GiResult<LayerGroupDetailVo> updateLayerGroup(@Validated @RequestBody LayerGroupUpdateVo param) {
                // 转换为PO
                    LayerGroupPo record = new LayerGroupPo();
                BeanUtils.copyProperties(param, record);

                // 调用Service
                    LayerGroupPo result = layerGroupService.update(record);

                return GiResult.successValue(LayerGroupDetailVo.fromPo(result))
                        .andAlertMsg("操作成功")
                        .andAlertTypeEnum(GirEmAlertType.无需关闭的提示1);
            }

            /**
             * ${tableComment}详情
             */
            @GaApiAction(text = "${tableComment}详情")
            @GetMapping("/loadLayerGroup")
            public GiResult<LayerGroupDetailVo> loadLayerGroup(String id) {
                    LayerGroupDto dto = layerGroupService.getDtoById(id);
                if (dto == null) {
                    throw new GirValidateException("${tableComment}不存在");
                }

                // 转换为VO
                    LayerGroupDetailVo vo = LayerGroupDetailVo.fromDto(dto);
                return GiResult.successValue(vo);
            }

            /**
             * 删除${tableComment}
             */
            @GaApiAction(text = "删除${tableComment}")
            @PostMapping("/delLayerGroup")
            public GiResult<?> delLayerGroup(String id, boolean confirmed) {
                if (confirmed) {
                        LayerGroupPo po = layerGroupService.getPoById(id);
                    if (po == null) {
                        throw new GirValidateException("${tableComment}不存在");
                    }
                        layerGroupService.deleteById(po.id());

                    return GiResult.getResult(null)
                            .andAlertMsg("操作成功")
                            .andAlertTypeEnum(GirEmAlertType.无需关闭的提示1);
                } else {
                    return GiResult.getResult(null)
                            .andAlertMsg("确定要删除${tableComment}吗?")
                            .andAlertTypeEnum(GirEmAlertType.弹出确认警告5);
                }
            }

            /**
             * 逻辑删除${tableComment}
             */
            @GaApiAction(text = "逻辑删除${tableComment}")
            @PostMapping("/logicDelLayerGroup/{ids}")
            public GiResult<?> logicDelLayerGroup(@PathVariable String[] ids, boolean confirmed) {
                if (confirmed) {
                    if (GutilObject.isEmpty(ids)) {
                        throw new GirException("ids为空!");
                    }

                        LayerGroupSeo seo = new LayerGroupSeo();
                    seo.setAndIdsIn(ids);
                    List<LayerGroupDto> dtos = layerGroupService.getDtoListBySeo(seo);

                    if (GutilObject.isEmpty(dtos)) {
                        throw new GirException("删除内容为空!");
                    }

                    for (LayerGroupDto dto : dtos) {
                            LayerGroupPo po = layerGroupService.getPoById(dto.getId());
                            layerGroupService.logicDeleteById(po.id());
                    }

                    return GiResult.getResult(null)
                            .andAlertMsg("操作成功")
                            .andAlertTypeEnum(GirEmAlertType.无需关闭的提示1);
                } else {
                    return GiResult.getResult(null)
                            .andAlertMsg("确定要删除${tableComment}吗?")
                            .andAlertTypeEnum(GirEmAlertType.弹出确认警告5);
                }
            }

            /**
             * 分页列出${tableComment}
             */
            @GaApiAction(text = "分页列出${tableComment}")
            @PostMapping("/listLayerGroupPage")
            public GiResult<GirPager<LayerGroupDetailVo>> listLayerGroupPage(@Validated @RequestBody LayerGroupSearchVo param) {
                // 构建查询条件
                    LayerGroupSeo seo = new LayerGroupSeo();
                BeanUtils.copyProperties(param, seo);
                seo.setNotDel();

                if (GutilObject.isNotEmpty(param.getQueryContent())) {
                    seo.setAndQueryContentIn(ArrayUtil.toArray(ListUtil.of(param.getQueryContent()), String.class));
                }

                // 分页查询
                GiPager<LayerGroupDto> giPager = layerGroupService.getDtoPageBySeo(seo, GiPageParam.of());

                // 转换为VO
                List<LayerGroupDetailVo> voList = LayerGroupDetailVo.fromDtos(giPager.value());

                // 封装分页结果
                GirPager<LayerGroupDetailVo> result = new GirPager<>();
                result.put(voList, giPager.total(), giPager.pageParam());

                return GiResult.successValue(result);
            }

            /**
             * 刷新缓存
             */
            @GaApiAction(text = "刷新缓存")
            @PostMapping("/refreshCache")
            public GiResult<?> refreshCache() {
                    layerGroupService.refreshCache();
                return GiResult.successValue(null).andAlertMsg("缓存刷新成功");
            }
                                                }
