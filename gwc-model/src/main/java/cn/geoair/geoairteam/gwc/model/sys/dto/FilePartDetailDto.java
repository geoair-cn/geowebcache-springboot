package cn.geoair.geoairteam.gwc.model.sys.dto;

import cn.geoair.base.data.common.GemDatePattern;
import cn.geoair.base.data.model.annotation.GaModel;
import cn.geoair.base.data.model.annotation.GaModelField;
import cn.geoair.base.gpa.entity.GiCrudEntity;
import cn.geoair.base.gpa.id.GiEntityIdGenerator;
import cn.geoair.geoairteam.gwc.model.sys.entity.FilePartDetailPo;

import static cn.hutool.core.bean.BeanUtil.copyProperties;

/**
 * 文件分片信息表DTO
 *
 * @author your name
 * @date 2024-01-01
 */
@GaModel(text = "文件分片信息表DTO")
public class FilePartDetailDto extends FilePartDetailPo {
    private static final long serialVersionUID = 1L;

    public static FilePartDetailDto empty() {
        return new FilePartDetailDto();
    }

    public FilePartDetailDto copy() {
        FilePartDetailDto dto = empty();
        copyProperties(this, dto);
        return dto;
    }

    public static FilePartDetailDto ofFilePartDetailPo(FilePartDetailPo source) {
        FilePartDetailDto target = new FilePartDetailDto();
        copyProperties(source, target);
        return target;
    }

    public static FilePartDetailPo toPo(FilePartDetailDto source) {
        FilePartDetailPo target = new FilePartDetailPo();
        copyProperties(source, target);
        return target;
    }
}
