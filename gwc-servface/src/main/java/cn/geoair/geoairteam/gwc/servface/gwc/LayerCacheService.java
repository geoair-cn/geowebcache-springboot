package cn.geoair.geoairteam.gwc.servface.gwc;

import cn.geoair.geoairteam.gwc.model.gwc.enums.GtcWmtsType;

import java.awt.image.BufferedImage;


/**
 * ${tableComment}Servface接口
 *
 * @author zhangjun
 * @date 2023-10-24
 */
public interface LayerCacheService {


    void delCacheByGroupName(String groupName);


    void delCacheByGroupNameNAndSortKey(String groupName, String sortKey);

    /**
     * 保存缓存
     *
     * @param groupName
     * @param xyz
     * @param bufferedImage
     */
    void putCache(GtcWmtsType gtcWmtsType, String groupName, String sortKey, String[] xyz, BufferedImage bufferedImage, Long timeout);

    /**
     * 是否拥有缓存
     *
     * @param
     * @param xyz
     * @return
     */
    boolean hasCache(GtcWmtsType gtcWmtsType, String groupName, String sortKey, String[] xyz);

    /**
     * 获取缓存切片
     *
     * @param layername
     * @param xyz
     * @return
     */
    BufferedImage getCacheImg(GtcWmtsType gtcWmtsType, String groupName, String sortKey, String[] xyz);
}
