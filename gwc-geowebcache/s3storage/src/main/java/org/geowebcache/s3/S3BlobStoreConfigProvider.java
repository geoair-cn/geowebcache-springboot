/**
 * 该程序是免费软件：您可以根据自由软件基金会发布的 GNU 宽松通用公共许可证 （许可证的第 3 版或（由您选择）任何更高版本）的条款重新分发和/或修改它.
 *
 * <p>分发此程序是希望它有用，但不作任何保证； 甚至没有对适销性或针对特定目的的适用性的暗示保证。 有关更多详细信息，请参阅 GNU 通用公共许可证.
 *
 * <p>您应该已经收到一份 GNU 宽松通用公共许可证以及该程序。如果没有，请参阅 <http:www.gnu.orglicenses>。
 *
 * @author geoair，版权所有 2021
 */
package org.geowebcache.s3;

import com.google.common.base.Strings;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;
import com.thoughtworks.xstream.converters.basic.IntConverter;
import com.thoughtworks.xstream.converters.basic.StringConverter;
import org.geowebcache.GeoWebCacheEnvironment;
import org.geowebcache.GeoWebCacheExtensions;
import org.geowebcache.config.BlobStoreInfo;
import org.geowebcache.config.Info;
import org.geowebcache.config.XMLConfigurationProvider;

public class S3BlobStoreConfigProvider implements XMLConfigurationProvider {

    private static GeoWebCacheEnvironment gwcEnvironment = null;

    private static SingleValueConverter EnvironmentNullableIntConverter =
            new IntConverter() {

                @Override
                public Object fromString(String str) {
                    str = resolveFromEnv(str);
                    if (Strings.isNullOrEmpty(str)) {
                        return null;
                    }
                    return super.fromString(str);
                }
            };

    private static SingleValueConverter EnvironmentNullableBooleanConverter =
            new BooleanConverter() {

                @Override
                public Object fromString(String str) {
                    str = resolveFromEnv(str);
                    if (Strings.isNullOrEmpty(str)) {
                        return null;
                    }
                    return super.fromString(str);
                }
            };

    private static SingleValueConverter EnvironmentStringConverter =
            new StringConverter() {
                @Override
                public Object fromString(String str) {
                    str = resolveFromEnv(str);
                    if (Strings.isNullOrEmpty(str)) {
                        return null;
                    }
                    return str;
                }
            };

    private static String resolveFromEnv(String str) {
        if (gwcEnvironment == null) {
            gwcEnvironment = GeoWebCacheExtensions.bean(GeoWebCacheEnvironment.class);
        }
        if (gwcEnvironment != null
                && str != null
                && GeoWebCacheEnvironment.ALLOW_ENV_PARAMETRIZATION) {
            Object result = gwcEnvironment.resolveValue(str);
            if (result == null) {
                return null;
            }
            return result.toString();
        }
        return str;
    }

    @Override
    public XStream getConfiguredXStream(XStream xs) {
        xs.alias("S3BlobStore", S3BlobStoreInfo.class);
        xs.registerLocalConverter(
                S3BlobStoreInfo.class, "maxConnections", EnvironmentNullableIntConverter);
        xs.registerLocalConverter(
                S3BlobStoreInfo.class, "proxyPort", EnvironmentNullableIntConverter);
        xs.registerLocalConverter(
                S3BlobStoreInfo.class, "useHTTPS", EnvironmentNullableBooleanConverter);
        xs.registerLocalConverter(
                S3BlobStoreInfo.class, "useGzip", EnvironmentNullableBooleanConverter);
        xs.registerLocalConverter(S3BlobStoreInfo.class, "bucket", EnvironmentStringConverter);
        xs.registerLocalConverter(
                S3BlobStoreInfo.class, "awsAccessKey", EnvironmentStringConverter);
        xs.registerLocalConverter(
                S3BlobStoreInfo.class, "awsSecretKey", EnvironmentStringConverter);
        xs.registerLocalConverter(S3BlobStoreInfo.class, "prefix", EnvironmentStringConverter);
        xs.registerLocalConverter(S3BlobStoreInfo.class, "proxyHost", EnvironmentStringConverter);
        xs.registerLocalConverter(
                BlobStoreInfo.class, "enabled", EnvironmentNullableBooleanConverter);
        xs.registerLocalConverter(S3BlobStoreInfo.class, "endpoint", EnvironmentStringConverter);
        xs.aliasField("id", S3BlobStoreInfo.class, "name");
        return xs;
    }

    @Override
    public boolean canSave(Info i) {
        return i instanceof S3BlobStoreInfo;
    }
}
