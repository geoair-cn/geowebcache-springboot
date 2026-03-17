package cn.geoair.geoairteam.gwc.service.utils;

import cn.geoair.base.bean.GirBeanHelper;
import cn.hutool.core.util.StrUtil;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @author ：zfj
 * @date ：Created in 2025/5/13 11:09
 * @description： TODO
 */
public class GtcGwcAliasNameUtils {

    static RedisTemplate gtcRedisTemplate = null;

    String layerGroupAlias;

    String key;

    final static String keyFormat = "gwcAliasName:{}"; // layerGroupAlias


    public static RedisTemplate getRedisTemplate() {
        if (gtcRedisTemplate == null) {
            gtcRedisTemplate = (RedisTemplate)GirBeanHelper.getProvider().getBean("redisTemplate");
        }
        return gtcRedisTemplate;
    }

    public GtcGwcAliasNameUtils() {

    }

    public static GtcGwcAliasNameUtils of(String layerGroupAlias) {
        GtcGwcAliasNameUtils bean = new GtcGwcAliasNameUtils();
        bean.layerGroupAlias = layerGroupAlias;
        bean.key = formatKey(layerGroupAlias);
        return bean;
    }

    public String getRealName() {
        Object o = getRedisTemplate().opsForValue().get(key);
        if (o == null) {
            return null;
        } else {
            return o.toString();
        }
    }

    public void putRealName(String realName) {
        getRedisTemplate().opsForValue().set(key, realName, 1, TimeUnit.DAYS);
    }


    public static String formatKey(String layerGroupAlias) {
        return StrUtil.format(keyFormat, layerGroupAlias);
    }

}


