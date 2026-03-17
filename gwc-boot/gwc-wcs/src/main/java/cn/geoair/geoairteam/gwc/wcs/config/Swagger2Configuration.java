package cn.geoair.geoairteam.gwc.wcs.config;

import cn.geoair.comp.knife4j.ext.core.config.GirSwaggerApiConfig;
import cn.geoair.comp.knife4j.ext.core.model.ApiModelInfo;
import cn.geoair.comp.knife4j.ext.core.model.DocketInfo;
import cn.geoair.spi.bean.SpringContextBean4Gir;
import cn.hutool.core.collection.ListUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;


@Configuration
@Import(SpringContextBean4Gir.class)
public class Swagger2Configuration implements GirSwaggerApiConfig {
    @Override
    public List<DocketInfo> getDocketInfos() {
        return ListUtil.of(new DocketInfo("用户中心", "cn.geoair.geoairteam.gwc"));
    }

    @Override
    public ApiModelInfo getApiModelInfo() {
        return new ApiModelInfo("gwc在线文档", "gwc在线文档", "gwc", "1.0");
    }
}
