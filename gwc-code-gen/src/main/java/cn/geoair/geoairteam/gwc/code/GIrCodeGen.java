package cn.geoair.geoairteam.gwc.code;

import cn.geoair.comp.code.generator.multi.config.GirGeneratorConfig;
import cn.geoair.comp.code.generator.multi.run.GirGenerator;
import cn.geoair.comp.dynamic.ds.simple.DriverManagerDataSource;


import javax.sql.DataSource;


public class GIrCodeGen {

    public static void main(String[] args) {
        DataSource dataSource = new DriverManagerDataSource("jdbc:postgresql://192.168.0.110/gtc_geowebcache" , "postgres" , "geoair2019" );
        GirGeneratorConfig globalConfig = new GirGeneratorConfig();
        globalConfig
                .setAuthor("geoairteam" )
                .setModuleName("gwc" )
                .setProjectName("gwc" )
                .setRemovePre(true)
                .setSourceRootPackage("cn.geoair.geoairteam.gwc" )
                .setSourceRootPath("" )
                .setTablePrefix("tgwc_" ).setSpringCacheUse(false);
        GirGenerator generator = new GirGenerator(dataSource, globalConfig);
        generator.genCode("tgwc_layer_group" );
    }
}
