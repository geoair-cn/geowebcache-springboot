package cn.geoair.geoairteam.gwc.wcs.config.file;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import cn.geoair.geoairteam.gwc.servface.sys.FileControlApi;

import cn.geoair.geoairteam.gwc.servface.sys.dto.OmFileInfoApo;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.dromara.x.file.storage.core.upload.UploadPretreatment;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;

/**
 * @author ：zfj
 * @date ：Created in 2023/7/11 14:38
 * @description： TODO
 */
@Service
public class OmFileControlApiImpl implements FileControlApi {
    @Resource
    private FileStorageService fileStorageService;

    ApplicationContext applicationContext;


    String profile;

    public OmFileControlApiImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    @Override
    public OmFileInfoApo upload(File file, String path) {
        if (!StrUtil.isEmpty(path)) {
            path = StrUtil.removePrefix(path, "/" );
            path = StrUtil.addSuffixIfNot(path, "/" );
        }
        FileInfo upload1 = fileStorageService.of(file).setPath(path).upload();
        OmFileInfoApo omFileInfoApo = new OmFileInfoApo();
        BeanUtil.copyProperties(upload1, omFileInfoApo);
        return omFileInfoApo;
    }

    @Override
    public OmFileInfoApo upload(byte[] filebyte, String fileName, String path) {
        if (!StrUtil.isEmpty(path)) {
            path = StrUtil.removePrefix(path, "/" );
            path = StrUtil.addSuffixIfNot(path, "/" );
        }
        return upload(filebyte, fileName, StrUtil.format("{}{}" , profile, path), false);
    }

    @Override
    public OmFileInfoApo upload(Pair<Boolean, Object> file, String path) {
        if (!StrUtil.isEmpty(path)) {
            path = StrUtil.removePrefix(path, "/" );
            path = StrUtil.addSuffixIfNot(path, "/" );
        }

        FileInfo upload1 = fileStorageService.of(file.getValue()).setPath(path).upload();
        OmFileInfoApo omFileInfoApo = new OmFileInfoApo();
        BeanUtil.copyProperties(upload1, omFileInfoApo);
        return omFileInfoApo;
    }

    @Override
    public OmFileInfoApo upload(byte[] filebyte, String fileName, String path, boolean useOriginalFilename) {
        if (!StrUtil.isEmpty(path)) {
            path = StrUtil.removePrefix(path, "/" );
            path = StrUtil.addSuffixIfNot(path, "/" );
        }
        OmFileInfoApo omFileInfoApo = new OmFileInfoApo();
        UploadPretreatment uploadPretreatment = fileStorageService.of(filebyte).setPath(path);
        if (useOriginalFilename) {
            uploadPretreatment.setSaveFilename(fileName);
        }
        FileInfo upload = uploadPretreatment.upload();
        omFileInfoApo.setFilename(upload.getFilename());
        omFileInfoApo.setUrl(upload.getUrl());
        omFileInfoApo.setSize(upload.getSize());
        omFileInfoApo.setOriginalFilename(upload.getOriginalFilename());
        return omFileInfoApo;
    }


    @Override
    public Boolean delByFileUrl(String fileUrl) {
        fileStorageService.delete(fileUrl);
        return Boolean.TRUE;
    }

    @Override
    public Boolean delByFileId(String fileId) {
        fileStorageService.delete(fileId);
        return Boolean.TRUE;
    }

}
