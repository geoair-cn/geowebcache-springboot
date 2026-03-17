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
import cn.geoair.geoairteam.gwc.dao.gwc.LayerInfoDao;
import cn.geoair.geoairteam.gwc.model.gwc.dto.LayerInfoDto;
import cn.geoair.geoairteam.gwc.model.gwc.entity.LayerInfoPo;
import cn.geoair.geoairteam.gwc.model.gwc.seo.LayerInfoSeo;
import cn.geoair.geoairteam.gwc.servface.gwc.LayerInfoService;
import cn.geoair.geoairteam.gwc.wcs.controller.gwc.layerinfo.LayerInfoAddVo;
import cn.geoair.geoairteam.gwc.wcs.controller.gwc.layerinfo.LayerInfoUpdateVo;
import cn.geoair.geoairteam.gwc.wcs.controller.gwc.layerinfo.LayerInfoSearchVo;
import cn.geoair.geoairteam.gwc.wcs.controller.gwc.layerinfo.LayerInfoDetailVo;
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
 * 图层信息表Controller
 *
 * @author geoairteam
 * @date 2026-03-11 15:45:16
 */
@RestController
@RequestMapping("/gwc/TgwcLayerInfo")
@GaApi(tags = "图层信息表")
public class LayerInfoController {
    private static final GiLogger logger = GirLogger.getLoger(LayerInfoController.class);
    @Resource
    private LayerInfoDao layerInfoDao;
    @Resource
    private LayerInfoService layerInfoService;

            
            /**
             * 新增图层信息表
             */
            @GaApiAction(text = "新增图层信息表")
            @PostMapping("/addLayerInfo")
            public GiResult<LayerInfoDetailVo> addLayerInfo(@Validated @RequestBody LayerInfoAddVo param) {
                // 转换为PO
                    LayerInfoPo record = new LayerInfoPo();
                BeanUtils.copyProperties(param, record);

                // 生成ID
                record.setId("LayerInfo_".toLowerCase() + IdUtil.getSnowflakeNextIdStr());

                // 调用Service
                    LayerInfoPo result = layerInfoService.add(record);

                return GiResult.successValue(LayerInfoDetailVo.fromPo(result))
                        .andAlertMsg("操作成功")
                        .andAlertTypeEnum(GirEmAlertType.无需关闭的提示1);
            }

            /**
             * 修改图层信息表
             */
            @GaApiAction(text = "修改图层信息表")
            @PostMapping("/updateLayerInfo")
            public GiResult<LayerInfoDetailVo> updateLayerInfo(@Validated @RequestBody LayerInfoUpdateVo param) {
                // 转换为PO
                    LayerInfoPo record = new LayerInfoPo();
                BeanUtils.copyProperties(param, record);

                // 调用Service
                    LayerInfoPo result = layerInfoService.update(record);

                return GiResult.successValue(LayerInfoDetailVo.fromPo(result))
                        .andAlertMsg("操作成功")
                        .andAlertTypeEnum(GirEmAlertType.无需关闭的提示1);
            }

            /**
             * 图层信息表详情
             */
            @GaApiAction(text = "图层信息表详情")
            @GetMapping("/loadLayerInfo")
            public GiResult<LayerInfoDetailVo> loadLayerInfo(String id) {
                    LayerInfoDto dto = layerInfoService.getDtoById(id);
                if (dto == null) {
                    throw new GirValidateException("图层信息表不存在");
                }

                // 转换为VO
                    LayerInfoDetailVo vo = LayerInfoDetailVo.fromDto(dto);
                return GiResult.successValue(vo);
            }

            /**
             * 删除图层信息表
             */
            @GaApiAction(text = "删除图层信息表")
            @PostMapping("/delLayerInfo")
            public GiResult<?> delLayerInfo(String id, boolean confirmed) {
                if (confirmed) {
                        LayerInfoPo po = layerInfoService.getPoById(id);
                    if (po == null) {
                        throw new GirValidateException("图层信息表不存在");
                    }
                        layerInfoService.deleteById(po.id());

                    return GiResult.getResult(null)
                            .andAlertMsg("操作成功")
                            .andAlertTypeEnum(GirEmAlertType.无需关闭的提示1);
                } else {
                    return GiResult.getResult(null)
                            .andAlertMsg("确定要删除图层信息表吗?")
                            .andAlertTypeEnum(GirEmAlertType.弹出确认警告5);
                }
            }

            /**
             * 逻辑删除图层信息表
             */
            @GaApiAction(text = "逻辑删除图层信息表")
            @PostMapping("/logicDelLayerInfo/{ids}")
            public GiResult<?> logicDelLayerInfo(@PathVariable String[] ids, boolean confirmed) {
                if (confirmed) {
                    if (GutilObject.isEmpty(ids)) {
                        throw new GirException("ids为空!");
                    }

                        LayerInfoSeo seo = new LayerInfoSeo();
                    seo.setAndIdsIn(ids);
                    List<LayerInfoDto> dtos = layerInfoService.getDtoListBySeo(seo);

                    if (GutilObject.isEmpty(dtos)) {
                        throw new GirException("删除内容为空!");
                    }

                    for (LayerInfoDto dto : dtos) {
                            LayerInfoPo po = layerInfoService.getPoById(dto.getId());
                            layerInfoService.logicDeleteById(po.id());
                    }

                    return GiResult.getResult(null)
                            .andAlertMsg("操作成功")
                            .andAlertTypeEnum(GirEmAlertType.无需关闭的提示1);
                } else {
                    return GiResult.getResult(null)
                            .andAlertMsg("确定要删除图层信息表吗?")
                            .andAlertTypeEnum(GirEmAlertType.弹出确认警告5);
                }
            }

            /**
             * 分页列出图层信息表
             */
            @GaApiAction(text = "分页列出图层信息表")
            @PostMapping("/listLayerInfoPage")
            public GiResult<GirPager<LayerInfoDetailVo>> listLayerInfoPage(@Validated @RequestBody LayerInfoSearchVo param) {
                // 构建查询条件
                    LayerInfoSeo seo = new LayerInfoSeo();
                BeanUtils.copyProperties(param, seo);
                seo.setNotDel();

                if (GutilObject.isNotEmpty(param.getQueryContent())) {
                    seo.setAndQueryContentIn(ArrayUtil.toArray(ListUtil.of(param.getQueryContent()), String.class));
                }

                // 分页查询
                GiPager<LayerInfoDto> giPager = layerInfoService.getDtoPageBySeo(seo, GiPageParam.of());

                // 转换为VO
                List<LayerInfoDetailVo> voList = LayerInfoDetailVo.fromDtos(giPager.value());

                // 封装分页结果
                GirPager<LayerInfoDetailVo> result = new GirPager<>();
                result.put(voList, giPager.total(), giPager.pageParam());

                return GiResult.successValue(result);
            }

            /**
             * 刷新缓存
             */
            @GaApiAction(text = "刷新缓存")
            @PostMapping("/refreshCache")
            public GiResult<?> refreshCache() {
                    layerInfoService.refreshCache();
                return GiResult.successValue(null).andAlertMsg("缓存刷新成功");
            }
                                                                                                                                                                                                                                                }
