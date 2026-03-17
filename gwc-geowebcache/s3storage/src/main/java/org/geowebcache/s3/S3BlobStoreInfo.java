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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geowebcache.config.BlobStoreInfo;
import org.geowebcache.layer.TileLayerDispatcher;
import org.geowebcache.locks.LockProvider;
import org.geowebcache.storage.BlobStore;
import org.geowebcache.storage.StorageException;

/** 表示 S3 blob 存储配置的普通旧 Java 对象。 */
@SuppressWarnings("deprecation")
public class S3BlobStoreInfo extends BlobStoreInfo {

    static Log log = LogFactory.getLog(S3BlobStoreInfo.class);

    private static final long serialVersionUID = 9072751143836460389L;

    private String bucket;

    private String prefix;

    private String awsAccessKey;

    private String awsSecretKey;

    private Access access = Access.PUBLIC;

    private Integer maxConnections;

    private Boolean useHTTPS = true;

    private String proxyDomain;

    private String proxyWorkstation;

    private String proxyHost;

    private Integer proxyPort;

    private String proxyUsername;

    private String proxyPassword;

    private Boolean useGzip;

    private String endpoint;

    public S3BlobStoreInfo() {
        super();
    }

    public S3BlobStoreInfo(String id) {
        super(id);
    }

    /** @return 存储切片的 AWS S3 存储桶的名称 */
    public String getBucket() {
        return bucket;
    }

    /** 设置存储切片的 AWS S3 存储桶的名称 */
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    /** @return S3 兼容服务器的主机（如果不是 AWS） */
    public String getEndpoint() {
        return endpoint;
    }

    /** 设置 S3 兼容服务器的主机（如果不是 AWS） */
    public void setEndpoint(String host) {
        this.endpoint = host;
    }

    /**
     * 返回基本前缀，这是用作根的前缀路径，用于在存储桶下存储切片。
     *
     * @return “基本前缀”的可选字符串
     */
    @Nullable
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getAwsAccessKey() {
        return awsAccessKey;
    }

    public void setAwsAccessKey(String awsAccessKey) {
        this.awsAccessKey = awsAccessKey;
    }

    public String getAwsSecretKey() {
        return awsSecretKey;
    }

    public void setAwsSecretKey(String awsSecretKey) {
        this.awsSecretKey = awsSecretKey;
    }

    /** @return 允许的最大打开 HTTP 连接数。 */
    public Integer getMaxConnections() {
        return maxConnections;
    }

    /** 设置允许打开的 HTTP 连接的最大数量 */
    public void setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
    }

    /** @return 与 S3 交谈时是否使用 HTTPS (true) 或 HTTP (false)（默认为 true） */
    public Boolean isUseHTTPS() {
        return useHTTPS;
    }

    /** @param useHTTPS – 与 S3 交谈时是使用 HTTPS (true) 还是 HTTP (false) */
    public void setUseHTTPS(Boolean useHTTPS) {
        this.useHTTPS = useHTTPS;
    }

    /**
     * 返回用于配置 NTLM 代理的可选 Windows 域名。
     *
     * <p>如果您不使用 Windows NTLM 代理，则无需设置此字段。
     *
     * @return 用于配置 NTLM 代理的可选 Windows 域名.
     */
    @Nullable
    public String getProxyDomain() {
        return proxyDomain;
    }

    /**
     * 设置用于配置 NTLM 代理的可选 Windows 域名。 如果您不使用 Windows NTLM 代理，则无需设置此字段。
     *
     * @param proxyDomain – 用于配置 NTLM 代理的可选 Windows 域名
     */
    public void setProxyDomain(String proxyDomain) {
        this.proxyDomain = proxyDomain;
    }

    /**
     * 返回用于配置 NTLM 代理支持的可选 Windows 工作站名称。 如果您不使用 Windows NTLM 代理，则无需设置此字段
     *
     * @return 返回用于配置 NTLM 代理支持的可选 Windows 工作站名称.
     */
    @Nullable
    public String getProxyWorkstation() {
        return proxyWorkstation;
    }

    /**
     * 设置用于配置 NTLM 代理支持的可选 Windows 工作站名称。 如果您不使用 Windows NTLM 代理，则无需设置此字段。
     *
     * @param proxyWorkstation 用于配置 NTLM 代理支持的可选 Windows 工作站名称。
     */
    public void setProxyWorkstation(String proxyWorkstation) {
        this.proxyWorkstation = proxyWorkstation;
    }

    /**
     * 返回客户端将通过的可选代理主机。
     *
     * @return 客户端将通过的代理主机。
     */
    @Nullable
    public String getProxyHost() {
        return proxyHost;
    }

    /**
     * 设置客户端将通过的可选代理主机。
     *
     * @param proxyHost – 客户端将通过的代理主机。
     */
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    /**
     * 返回客户端将通过的可选代理端口。
     *
     * @return 客户端将通过的代理端口。
     */
    public Integer getProxyPort() {
        return proxyPort;
    }

    /**
     * 设置客户端将通过的可选代理端口。
     *
     * @param proxyPort – 客户端将通过的代理端口。
     */
    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    /**
     * 如果通过代理连接，则返回要使用的可选代理用户名。
     *
     * @return T如果通过代理连接，配置的客户端将使用的可选代理用户名。
     */
    @Nullable
    public String getProxyUsername() {
        return proxyUsername;
    }

    /**
     * 设置通过代理连接时要使用的可选代理用户名。
     *
     * @param proxyUsername – 通过代理连接时使用的代理用户名。
     */
    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    /**
     * 返回通过代理连接时要使用的可选代理密码。
     *
     * @return 通过代理连接时使用的密码。
     */
    @Nullable
    public String getProxyPassword() {
        return proxyPassword;
    }

    /**
     * 设置通过代理连接时要使用的可选代理密码。
     *
     * @param proxyPassword – 通过代理连接时使用的密码。
     */
    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    /**
     * 检查访问类型
     *
     * @return 公共或私人访问
     */
    public CannedAccessControlList getAccessControlList() {
        CannedAccessControlList accessControlList;
        if (access == Access.PRIVATE) {
            accessControlList = CannedAccessControlList.BucketOwnerFullControl;
        } else {
            accessControlList = CannedAccessControlList.PublicRead;
        }
        return accessControlList;
    }

    /**
     * 设置访问应该是私有的还是公共的
     *
     * @param access 访问是私有的还是公共的
     */
    public void setAccess(Access access) {
        this.access = access;
    }

    /**
     * 获取访问应该是私有的还是公共的
     *
     * @return 访问是私有的还是公共的
     */
    public Access getAccess() {
        return this.access;
    }

    /**
     * 检查是否使用 gzip 压缩
     *
     * @return 如果使用 gzip 压缩
     */
    public Boolean isUseGzip() {
        return useGzip;
    }

    /**
     * 设置是否应使用 gzip 压缩
     *
     * @param use 是否应使用 gzip 压缩
     */
    public void setUseGzip(Boolean use) {
        this.useGzip = use;
    }

    @Override
    public BlobStore createInstance(TileLayerDispatcher layers, LockProvider lockProvider)
            throws StorageException {

        checkNotNull(layers);
        checkState(getName() != null);
        checkState(
                isEnabled(),
                "Can't call S3BlobStoreConfig.createInstance() is blob store is not enabled");
        return new S3BlobStore(this, layers, lockProvider);
    }

    @Override
    public String getLocation() {
        String bucket = this.getBucket();
        String prefix = this.getPrefix();
        if (prefix == null) {
            return String.format("bucket: %s", bucket);
        } else {
            return String.format("bucket: %s prefix: %s", bucket, prefix);
        }
    }

    /** @return {@link AmazonS3Client} 构造的 {@link S3BlobStoreInfo}. */
    public AmazonS3Client buildClient() {
        ClientConfiguration clientConfig = new ClientConfiguration();
        if (null != useHTTPS) {
            clientConfig.setProtocol(useHTTPS ? Protocol.HTTPS : Protocol.HTTP);
        }
        if (null != maxConnections && maxConnections > 0) {
            clientConfig.setMaxConnections(maxConnections);
        }
        clientConfig.setProxyDomain(proxyDomain);
        clientConfig.setProxyWorkstation(proxyWorkstation);
        clientConfig.setProxyHost(proxyHost);
        if (null != proxyPort) {
            clientConfig.setProxyPort(proxyPort);
        }
        clientConfig.setProxyUsername(proxyUsername);
        clientConfig.setProxyPassword(proxyPassword);
        if (null != useGzip) {
            clientConfig.setUseGzip(useGzip);
        }
        log.debug("Initializing AWS S3 connection");
        AmazonS3Client client = new AmazonS3Client(getCredentialsProvider(), clientConfig);
        if (endpoint != null && !"".equals(endpoint)) {
            S3ClientOptions s3ClientOptions = new S3ClientOptions();
            s3ClientOptions.setPathStyleAccess(true);
            client.setS3ClientOptions(s3ClientOptions);
            client.setEndpoint(endpoint);
        }
        if (!client.doesBucketExist(bucket)) {
            client.createBucket(bucket);
        }
        return client;
    }

    private AWSCredentialsProvider getCredentialsProvider() {
        if (null != awsSecretKey && null != awsAccessKey) {
            return new AWSCredentialsProvider() {

                @Override
                public AWSCredentials getCredentials() {
                    if ("".equals(awsAccessKey) && "".equals(awsSecretKey)) {
                        return new AnonymousAWSCredentials();
                    }
                    return new BasicAWSCredentials(awsAccessKey, awsSecretKey);
                }

                @Override
                public void refresh() {}
            };
        }
        return new DefaultAWSCredentialsProviderChain();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((access == null) ? 0 : access.hashCode());
        result = prime * result + ((awsAccessKey == null) ? 0 : awsAccessKey.hashCode());
        result = prime * result + ((awsSecretKey == null) ? 0 : awsSecretKey.hashCode());
        result = prime * result + ((bucket == null) ? 0 : bucket.hashCode());
        result = prime * result + ((endpoint == null) ? 0 : endpoint.hashCode());
        result = prime * result + ((maxConnections == null) ? 0 : maxConnections.hashCode());
        result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
        result = prime * result + ((proxyDomain == null) ? 0 : proxyDomain.hashCode());
        result = prime * result + ((proxyHost == null) ? 0 : proxyHost.hashCode());
        result = prime * result + ((proxyPassword == null) ? 0 : proxyPassword.hashCode());
        result = prime * result + ((proxyPort == null) ? 0 : proxyPort.hashCode());
        result = prime * result + ((proxyUsername == null) ? 0 : proxyUsername.hashCode());
        result = prime * result + ((proxyWorkstation == null) ? 0 : proxyWorkstation.hashCode());
        result = prime * result + ((useGzip == null) ? 0 : useGzip.hashCode());
        result = prime * result + ((useHTTPS == null) ? 0 : useHTTPS.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        S3BlobStoreInfo other = (S3BlobStoreInfo) obj;
        if (access != other.access) return false;
        if (awsAccessKey == null) {
            if (other.awsAccessKey != null) return false;
        } else if (!awsAccessKey.equals(other.awsAccessKey)) return false;
        if (awsSecretKey == null) {
            if (other.awsSecretKey != null) return false;
        } else if (!awsSecretKey.equals(other.awsSecretKey)) return false;
        if (bucket == null) {
            if (other.bucket != null) return false;
        } else if (!bucket.equals(other.bucket)) return false;
        if (endpoint == null) {
            if (other.endpoint != null) return false;
        } else if (!endpoint.equals(other.endpoint)) return false;
        if (maxConnections == null) {
            if (other.maxConnections != null) return false;
        } else if (!maxConnections.equals(other.maxConnections)) return false;
        if (prefix == null) {
            if (other.prefix != null) return false;
        } else if (!prefix.equals(other.prefix)) return false;
        if (proxyDomain == null) {
            if (other.proxyDomain != null) return false;
        } else if (!proxyDomain.equals(other.proxyDomain)) return false;
        if (proxyHost == null) {
            if (other.proxyHost != null) return false;
        } else if (!proxyHost.equals(other.proxyHost)) return false;
        if (proxyPassword == null) {
            if (other.proxyPassword != null) return false;
        } else if (!proxyPassword.equals(other.proxyPassword)) return false;
        if (proxyPort == null) {
            if (other.proxyPort != null) return false;
        } else if (!proxyPort.equals(other.proxyPort)) return false;
        if (proxyUsername == null) {
            if (other.proxyUsername != null) return false;
        } else if (!proxyUsername.equals(other.proxyUsername)) return false;
        if (proxyWorkstation == null) {
            if (other.proxyWorkstation != null) return false;
        } else if (!proxyWorkstation.equals(other.proxyWorkstation)) return false;
        if (useGzip == null) {
            if (other.useGzip != null) return false;
        } else if (!useGzip.equals(other.useGzip)) return false;
        if (useHTTPS == null) {
            if (other.useHTTPS != null) return false;
        } else if (!useHTTPS.equals(other.useHTTPS)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "S3BlobStoreInfo [bucket="
                + bucket
                + ", prefix="
                + prefix
                + ", awsAccessKey="
                + awsAccessKey
                + ", awsSecretKey="
                + awsSecretKey
                + ", access="
                + access
                + ", maxConnections="
                + maxConnections
                + ", useHTTPS="
                + useHTTPS
                + ", proxyDomain="
                + proxyDomain
                + ", proxyWorkstation="
                + proxyWorkstation
                + ", proxyHost="
                + proxyHost
                + ", proxyPort="
                + proxyPort
                + ", proxyUsername="
                + proxyUsername
                + ", proxyPassword="
                + proxyPassword
                + ", useGzip="
                + useGzip
                + ", endpoint="
                + endpoint
                + ", getName()="
                + getName()
                + ", getId()="
                + getId()
                + ", isEnabled()="
                + isEnabled()
                + ", isDefault()="
                + isDefault()
                + "]";
    }
}
