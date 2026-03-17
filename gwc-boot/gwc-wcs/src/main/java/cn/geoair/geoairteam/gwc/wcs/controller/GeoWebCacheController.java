//package cn.geoair.geoairteam.gwc.wcs.controller;
//
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
///**
// * 核心控制器：对应原web.xml中的DispatcherServlet映射
// */
//@RestController
//@RequestMapping("/") // 对应原/*的映射
//public class GeoWebCacheController {
//
//    // 对应原/home的映射
//    @GetMapping("/home")
//    public String home() {
//        return "GeoWebCache Home Page (Spring Boot)";
//    }
//
//    // 通用映射（对应原/*）
//    @GetMapping("/**")
//    public String index() {
//        return "GeoWebCache Spring Boot Running...";
//    }
//}
