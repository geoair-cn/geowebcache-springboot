package cn.geoair.geoairteam.gwc.servface.sys;


import cn.geoair.base.api.annotation.GaApiAction;
import cn.hutool.core.lang.Pair;
import cn.geoair.geoairteam.gwc.servface.sys.dto.OmFileInfoApo;


import java.io.File;

/**
 * @author ：zhangjun
 * @date ：Created in 2023/7/11 14:25
 * @description： 文件服务api
 */
public interface FileControlApi {

    @GaApiAction(text = "单个上传,文件对象" )
    OmFileInfoApo upload(File file, String path);


    @GaApiAction(text = "单个上传,字节对象" )
    OmFileInfoApo upload(byte[] filebyte, String fileName, String path);

    OmFileInfoApo upload(Pair<Boolean, Object> file, String path);


    @GaApiAction(text = "单个上传,字节对象" )
    OmFileInfoApo upload(byte[] filebyte, String fileName, String path, boolean useOriginalFilename);


    @GaApiAction(text = "删除文件 通过文件url" )
    Boolean delByFileUrl(String fileUrl);

    @GaApiAction(text = "删除文件 通过文件节点id" )
    Boolean delByFileId(String fileId);

}
