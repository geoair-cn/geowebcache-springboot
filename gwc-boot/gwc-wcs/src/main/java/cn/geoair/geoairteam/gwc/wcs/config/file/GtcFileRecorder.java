package cn.geoair.geoairteam.gwc.wcs.config.file;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson.JSONObject;
import cn.geoair.geoairteam.gwc.dao.sys.FileDetailDao;
import cn.geoair.geoairteam.gwc.dao.sys.FilePartDetailDao;
import cn.geoair.geoairteam.gwc.model.sys.entity.FileDetailPo;
import cn.geoair.geoairteam.gwc.model.sys.entity.FilePartDetailPo;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.hash.HashInfo;
import org.dromara.x.file.storage.core.recorder.FileRecorder;
import org.dromara.x.file.storage.core.upload.FilePartInfo;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author ：张逢吉
 * @date ：Created in   15:47
 * @description：
 */
@Component
public class GtcFileRecorder implements FileRecorder {
    @Resource
    FileDetailDao fileDetailDao;
    @Resource
    FilePartDetailDao filePartDetailDao;

    @Override
    public boolean save(FileInfo fileInfo) {
        FileDetailPo detail = toFileDetail(fileInfo);
        detail.setId(IdUtil.getSnowflakeNextIdStr());
        fileInfo.setId(detail.getId());
        fileDetailDao.gtcAccess(detail);
        return true;
    }

    @Override
    public void update(FileInfo fileInfo) {
        String id = fileInfo.getId();
        FileDetailPo detail = toFileDetail(fileInfo);
        fileDetailDao.gtcUpdateByPKSelective(detail);
    }

    @Override
    public FileInfo getByUrl(String url) {
        FileDetailPo fileDetailPo = new FileDetailPo();
        fileDetailPo.setUrl(url);
        FileDetailPo fileDetailPo1 = fileDetailDao.gtcSearchOne(fileDetailPo);
        FileInfo fileInfo = toFileInfo(fileDetailPo1);
        return fileInfo;
    }

    @Override
    public boolean delete(String url) {
        FileInfo byUrl = getByUrl(url);
        if (byUrl != null) {
            fileDetailDao.gtcDeleteByPK(byUrl.getId());
        }
        return true;
    }

    @Override
    public void saveFilePart(FilePartInfo filePartInfo) {
        FilePartDetailPo filePartDetail = toFilePartDetail(filePartInfo);
        filePartDetail.setId(IdUtil.getSnowflakeNextIdStr());
        filePartDetailDao.gtcAccess(filePartDetail);
    }

    @Override
    public void deleteFilePartByUploadId(String uploadId) {
        if (StrUtil.isEmpty(uploadId)) {
            return;
        }
        FilePartDetailPo filePartDetail = new FilePartDetailPo();
        filePartDetail.setUploadId(uploadId);
        filePartDetailDao.gtcDeleteBy(filePartDetail);
    }


    /**
     * 将 FileInfo 转为 FileDetail
     */
    public FileDetailPo toFileDetail(FileInfo info) {
        FileDetailPo detail = BeanUtil.copyProperties(
                info, FileDetailPo.class, "metadata" , "userMetadata" , "thMetadata" , "thUserMetadata" , "attr" , "hashInfo" );
        detail.setMetadata(valueToJson(info.getMetadata()));
        detail.setUserMetadata(valueToJson(info.getUserMetadata()));
        detail.setThMetadata(valueToJson(info.getThMetadata()));
        detail.setThUserMetadata(valueToJson(info.getThUserMetadata()));
        detail.setAttr(valueToJson(info.getAttr()));
        detail.setHashInfo(valueToJson(info.getHashInfo()));
        return detail;
    }


    /**
     * 将 FileDetail 转为 FileInfo
     */
    public FileInfo toFileInfo(FileDetailPo detail) {
        if (detail == null) {
            return null;
        }
        FileInfo info = BeanUtil.copyProperties(
                detail, FileInfo.class, "metadata" , "userMetadata" , "thMetadata" , "thUserMetadata" , "attr" , "hashInfo" );

//        // 这里手动获取数据库中的 json 字符串 并转成 元数据，方便使用
        info.setMetadata(jsonToMetadata(detail.getMetadata()));
        info.setUserMetadata(jsonToMetadata(detail.getUserMetadata()));
        info.setThMetadata(jsonToMetadata(detail.getThMetadata()));
        info.setThUserMetadata(jsonToMetadata(detail.getThUserMetadata()));
//        // 这里手动获取数据库中的 json 字符串 并转成 附加属性字典，方便使用
        info.setAttr(jsonToDict(detail.getAttr()));
//        // 这里手动获取数据库中的 json 字符串 并转成 哈希信息，方便使用
        info.setHashInfo(jsonToHashInfo(detail.getHashInfo()));
        return info;
    }

    public FilePartDetailPo toFilePartDetail(FilePartInfo info) {
        FilePartDetailPo detail = new FilePartDetailPo();
        detail.setPlatform(info.getPlatform());
        detail.setUploadId(info.getUploadId());
        detail.setETag(info.getETag());
        detail.setPartNumber(info.getPartNumber());
        detail.setPartSize(info.getPartSize());
        detail.setHashInfo(valueToJson(info.getHashInfo()));
        detail.setCreateTime(info.getCreateTime());
        return detail;
    }

    public String valueToJson(Object value) {
        if (value == null) return null;
        return JSONObject.toJSONString(value);
    }

    public Map<String, String> jsonToMetadata(String json) {
        if (StrUtil.isBlank(json)) return null;
        return JSONObject.parseObject(json, Map.class);
    }

    public Dict jsonToDict(String json) {
        if (StrUtil.isBlank(json)) return null;
        return JSONObject.parseObject(json, Dict.class);
    }

    public HashInfo jsonToHashInfo(String json) {
        if (StrUtil.isBlank(json)) return null;
        return JSONObject.parseObject(json, HashInfo.class);
    }
}
