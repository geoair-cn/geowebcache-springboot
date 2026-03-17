package cn.geoair.geoairteam.gwc.wcs.controller.sys;

import cn.geoair.map.dynamic.adv.spring.GirSpringAdvExecutor;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.geoair.base.api.annotation.GaApi;
import cn.geoair.base.api.annotation.GaApiAction;
import cn.geoair.base.data.page.GiPageParam;
import cn.geoair.base.data.page.GiPager;
import cn.geoair.base.data.page.support.GirPager;
import cn.geoair.base.data.result.GiResult;
import cn.geoair.base.log.GiLogger;
import cn.geoair.base.log.GirLogger;
import cn.geoair.base.util.GutilObject;

import cn.geoair.geoairteam.gwc.wcs.controller.sys.configproperty.ConfigPropertyDetailVo;
import cn.geoair.geoairteam.gwc.wcs.controller.sys.configproperty.ConfigPropertySearchVo;
import cn.geoair.geoairteam.gwc.dao.sys.ConfigPropertyDao;
import cn.geoair.geoairteam.gwc.model.sys.dto.ConfigPropertyDto;
import cn.geoair.geoairteam.gwc.model.sys.seo.ConfigPropertySeo;
import cn.geoair.geoairteam.gwc.servface.sys.ConfigPropertyService;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 配置属性表Controller
 *
 * @author zhangjun
 * @date 2024-09-04
 */
@RestController
@RequestMapping("/common/ConfigProperty")
@GaApi(tags = "配置属性表")
public class ConfigPropertyController {
    private static final GiLogger logger = GirLogger.getLoger(ConfigPropertyController.class);

    @Resource
    private ConfigPropertyService configPropertyService;
    @Resource
    private ConfigPropertyDao configPropertyDao;


    @GaApiAction(text = "分页列出配置属性表")
    @RequestMapping(value = "/listConfigPropertyPage", method = {RequestMethod.POST})
    @ResponseBody
    public GiResult<GiPager<ConfigPropertyDetailVo>> listConfigPropertyPage(@Validated @RequestBody ConfigPropertySearchVo param) {
        ConfigPropertySeo seo = new ConfigPropertySeo();
        BeanUtils.copyProperties(param, seo);
        seo.setNotDel();
        if (GutilObject.isNotEmpty(param.getQueryContent())) {
            seo.setAndQueryContentIn(ArrayUtil.toArray(ListUtil.of(param.getQueryContent()), String.class));
        }
        GiPager<ConfigPropertyDto> giPager =
                configPropertyDao.searchListPage(seo, GiPageParam.of());
        Iterable<ConfigPropertyDto> value = giPager.value();
        GirPager<ConfigPropertyDetailVo> reg = new GirPager<>();
        List<ConfigPropertyDetailVo> vdvos = ConfigPropertyDetailVo.fromDtos(value);
        reg.put(vdvos, giPager.total(), giPager.pageParam());
        return GiResult.successValue(reg);
    }


    @GaApiAction(text = "通过key获取配置")
    @RequestMapping(value = "/getConfigValueByKey", method = {RequestMethod.POST})
    @ResponseBody
    public GiResult<String> getConfigValueByKey(String key, String ifNullValue) {
        return GiResult.successValue(configPropertyDao.getConfigValueByKey(key, ifNullValue));
    }


}
