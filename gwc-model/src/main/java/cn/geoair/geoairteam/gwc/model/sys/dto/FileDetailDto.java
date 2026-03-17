package cn.geoair.geoairteam.gwc.model.sys.dto;


import cn.geoair.base.data.common.GemDatePattern;
import cn.geoair.base.data.model.annotation.GaModel;
import cn.geoair.base.data.model.annotation.GaModelField;
import cn.geoair.base.gpa.entity.GiCrudEntity;
import cn.geoair.base.gpa.id.GiEntityIdGenerator;
import cn.geoair.geoairteam.gwc.model.sys.entity.FileDetailPo;

import static cn.hutool.core.bean.BeanUtil.copyProperties;

/**
 * 文件记录表DTO
 *
 * @author your name
 * @date 2024-01-01
 */
@GaModel(text = "文件记录表DTO" )
public class FileDetailDto extends FileDetailPo {
    private static final long serialVersionUID = 1L;

    public static FileDetailDto empty() {
        return new FileDetailDto();
    }

    public FileDetailDto copy() {
        FileDetailDto dto = empty();
        copyProperties(this, dto);
        return dto;
    }

    public static FileDetailDto ofFileDetailPo(FileDetailPo source) {
        FileDetailDto target = new FileDetailDto();
        copyProperties(source, target);
        return target;
    }

    public static FileDetailPo toPo(FileDetailDto source) {
        FileDetailPo target = new FileDetailPo();
        copyProperties(source, target);
        return target;
    }
}
