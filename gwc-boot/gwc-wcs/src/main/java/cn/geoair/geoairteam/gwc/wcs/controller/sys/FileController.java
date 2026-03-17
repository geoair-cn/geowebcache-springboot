package cn.geoair.geoairteam.gwc.wcs.controller.sys;

import cn.geoair.base.api.annotation.GaApi;
import cn.geoair.base.api.annotation.GaApiAction;
import cn.geoair.base.data.result.GiResult;
import cn.geoair.base.data.result.GirEmAlertType;
import cn.geoair.base.log.GiLogger;
import cn.geoair.base.log.GirLogger;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.IdUtil;

import com.alibaba.fastjson.JSONObject;
import cn.geoair.geoairteam.gwc.servface.sys.FileControlApi;
import cn.geoair.geoairteam.gwc.servface.sys.dto.OmFileInfoApo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * 字典数据表Controller
 *
 * @author zhangjun
 * @date 2023-05-31
 */
@RestController
@RequestMapping("/sys/file" )
@GaApi(tags = "文件操作" )
public class FileController {
    private static final GiLogger logger = GirLogger.getLoger(FileController.class);

    @Resource
    FileControlApi omFileControlApi;

    @GaApiAction(text = "上传" )
    @RequestMapping(value = "/upload" , method = {RequestMethod.POST})
    @ResponseBody
    public GiResult<JSONObject> upload(MultipartFile file) {
        OmFileInfoApo onemap = omFileControlApi.upload(Pair.of(true, file), "/gwc/temp" );
        JSONObject returnObj = new JSONObject();
        returnObj.put("originalFilename" , onemap.getOriginalFilename());
        returnObj.put("httpUrl" , onemap.getUrl());
        String name = file.getOriginalFilename();
        returnObj.put("fileName" , onemap.getFilename());
        returnObj.put("fileNamePrefix" , FileNameUtil.getPrefix(name));
        returnObj.put("nanoId" , IdUtil.nanoId(6));
        return GiResult.successValue(returnObj).andAlertMsg("操作成功" ).andAlertTypeEnum(GirEmAlertType.无需关闭的提示1);
    }

    @GaApiAction(text = "删除上传的文件" )
    @RequestMapping(value = "/delFile" , method = {RequestMethod.POST})
    @ResponseBody

    public GiResult<Boolean> delFile(String fileUrl) {
        omFileControlApi.delByFileUrl(fileUrl);
        return GiResult.successValue(true).andAlertMsg("操作成功" ).andAlertTypeEnum(GirEmAlertType.无需关闭的提示1);
    }




}
