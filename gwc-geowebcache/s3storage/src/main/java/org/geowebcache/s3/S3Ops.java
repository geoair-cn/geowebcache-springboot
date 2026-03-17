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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.iterable.S3Objects;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import org.apache.commons.io.IOUtils;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.locks.LockProvider;
import org.geowebcache.locks.LockProvider.Lock;
import org.geowebcache.locks.NoOpLockProvider;
import org.geowebcache.storage.StorageException;

class S3Ops {

    private final AmazonS3Client conn;

    private final String bucketName;

    private final TMSKeyBuilder keyBuilder;

    private final LockProvider locks;

    private ExecutorService deleteExecutorService;

    private Map<String, Long> pendingDeletesKeyTime = new ConcurrentHashMap<>();

    public S3Ops(
            AmazonS3Client conn, String bucketName, TMSKeyBuilder keyBuilder, LockProvider locks)
            throws StorageException {
        this.conn = conn;
        this.bucketName = bucketName;
        this.keyBuilder = keyBuilder;
        this.locks = locks == null ? new NoOpLockProvider() : locks;
        this.deleteExecutorService = createDeleteExecutorService();
        issuePendingBulkDeletes();
    }

    private ExecutorService createDeleteExecutorService() {
        ThreadFactory tf =
                new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat("GWC S3BlobStore 批量删除线程-%d。 桶: " + bucketName)
                        .setPriority(Thread.MIN_PRIORITY)
                        .build();
        return Executors.newCachedThreadPool(tf);
    }

    public void shutDown() {
        deleteExecutorService.shutdownNow();
    }

    private void issuePendingBulkDeletes() throws StorageException {
        final String pendingDeletesKey = keyBuilder.pendingDeletes();
        Lock lock;
        try {
            lock = locks.getLock(pendingDeletesKey);
        } catch (GeoWebCacheException e) {
            throw new StorageException("无法锁定挂起的删除", e);
        }

        try {
            Properties deletes = getProperties(pendingDeletesKey);
            for (Entry<Object, Object> e : deletes.entrySet()) {
                final String prefix = e.getKey().toString();
                final long timestamp = Long.parseLong(e.getValue().toString());
                S3BlobStore.log.info(
                        String.format("重新启动“%s/%s”上的挂起批量删除:%d", bucketName, prefix, timestamp));
                asyncDelete(prefix, timestamp);
            }
        } finally {
            try {
                lock.release();
            } catch (GeoWebCacheException e) {
                throw new StorageException("无法解锁挂起的删除", e);
            }
        }
    }

    private void clearPendingBulkDelete(final String prefix, final long timestamp)
            throws GeoWebCacheException {
        Long taskTime = pendingDeletesKeyTime.get(prefix);
        if (taskTime == null) {
            return; // 其他人为我们清理了它。 一项在此之后运行但在此之前完成的任务？
        }
        if (taskTime.longValue() > timestamp) {
            return; // 其他人在此之后针对相同的键前缀发出了批量删除
        }
        final String pendingDeletesKey = keyBuilder.pendingDeletes();
        final Lock lock = locks.getLock(pendingDeletesKey);

        try {
            Properties deletes = getProperties(pendingDeletesKey);
            String storedVal = (String) deletes.remove(prefix);
            long storedTimestamp = storedVal == null ? Long.MIN_VALUE : Long.parseLong(storedVal);
            if (timestamp >= storedTimestamp) {
                putProperties(pendingDeletesKey, deletes);
            } else {
                S3BlobStore.log.info(
                        String.format("批量删除已完成，但有一个更新的存储桶“%s/%s”正在进行中", bucketName, prefix));
            }
        } catch (StorageException e) {
            throw new RuntimeException(e);
        } finally {
            lock.release();
        }
    }

    public boolean scheduleAsyncDelete(final String prefix) throws GeoWebCacheException {
        final long timestamp = currentTimeSeconds();
        String msg = String.format("在 '%s/%s' 上对 %d 之前的对象发出批量删除", bucketName, prefix, timestamp);
        S3BlobStore.log.info(msg);

        Lock lock = locks.getLock(prefix);
        try {
            boolean taskRuns = asyncDelete(prefix, timestamp);
            if (taskRuns) {
                final String pendingDeletesKey = keyBuilder.pendingDeletes();
                Properties deletes = getProperties(pendingDeletesKey);
                deletes.setProperty(prefix, String.valueOf(timestamp));
                putProperties(pendingDeletesKey, deletes);
            }
            return taskRuns;
        } catch (StorageException e) {
            throw new RuntimeException(e);
        } finally {
            lock.release();
        }
    }

    // S3 将时间戳截断为秒精度，
    // 并且不允许以编程方式设置上次修改时间
    private long currentTimeSeconds() {
        final long timestamp = (long) Math.ceil(System.currentTimeMillis() / 1000D) * 1000L;
        return timestamp;
    }

    private synchronized boolean asyncDelete(final String prefix, final long timestamp) {
        if (!prefixExists(prefix)) {
            return false;
        }

        Long currentTaskTime = pendingDeletesKeyTime.get(prefix);
        if (currentTaskTime != null && currentTaskTime.longValue() > timestamp) {
            return false;
        }

        BulkDelete task = new BulkDelete(conn, bucketName, prefix, timestamp);
        deleteExecutorService.submit(task);
        pendingDeletesKeyTime.put(prefix, timestamp);

        return true;
    }

    @Nullable
    public ObjectMetadata getObjectMetadata(String key) throws StorageException {
        ObjectMetadata obj = null;
        try {
            obj = conn.getObjectMetadata(bucketName, key);
        } catch (AmazonS3Exception e) {
            if (404 != e.getStatusCode()) { // 404 == 未找到
                throw new StorageException("错误检查存在" + key + ": " + e.getMessage(), e);
            }
        }
        return obj;
    }

    public void putObject(PutObjectRequest putObjectRequest) throws StorageException {
        try {
            conn.putObject(putObjectRequest);
        } catch (RuntimeException e) {
            throw new StorageException("存储错误 " + putObjectRequest.getKey(), e);
        }
    }

    @Nullable
    public S3Object getObject(String key) throws StorageException {
        final S3Object object;
        try {
            object = conn.getObject(bucketName, key);
        } catch (AmazonS3Exception e) {
            if (404 == e.getStatusCode()) { // 404 == 未找到
                return null;
            }
            throw new StorageException(" 获取错误" + key + ": " + e.getMessage(), e);
        }
        if (isPendingDelete(object)) {
            closeObject(object);
            return null;
        }
        return object;
    }

    private void closeObject(S3Object object) throws StorageException {
        try {
            object.close();
        } catch (IOException e) {
            throw new StorageException("关闭连接时出错" + object.getKey() + ": " + e.getMessage(), e);
        }
    }

    public boolean deleteObject(final String key) {
        try {
            conn.deleteObject(bucketName, key);
        } catch (AmazonS3Exception e) {
            return false;
        }
        return true;
    }

    private boolean isPendingDelete(S3Object object) {
        if (pendingDeletesKeyTime.isEmpty()) {
            return false;
        }
        final String key = object.getKey();
        final long lastModified = object.getObjectMetadata().getLastModified().getTime();
        for (Map.Entry<String, Long> e : pendingDeletesKeyTime.entrySet()) {
            String parentKey = e.getKey();
            if (key.startsWith(parentKey)) {
                long deleteTime = e.getValue().longValue();
                return deleteTime >= lastModified;
            }
        }
        return false;
    }

    @Nullable
    public byte[] getBytes(String key) throws StorageException {
        try (S3Object object = getObject(key)) {
            if (object == null) {
                return null;
            }
            try (S3ObjectInputStream in = object.getObjectContent()) {
                byte[] bytes = IOUtils.toByteArray(in);
                return bytes;
            }
        } catch (IOException e) {
            throw new StorageException("获取错误 " + key, e);
        }
    }

    /** 只需检查是否有以 {@code prefix} 开头的对象 */
    public boolean prefixExists(String prefix) {
        boolean hasNext =
                S3Objects.withPrefix(conn, bucketName, prefix)
                        .withBatchSize(1)
                        .iterator()
                        .hasNext();
        return hasNext;
    }

    public Properties getProperties(String key) {
        Properties properties = new Properties();
        byte[] bytes;
        try {
            bytes = getBytes(key);
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
        if (bytes != null) {
            try {
                properties.load(
                        new InputStreamReader(
                                new ByteArrayInputStream(bytes), StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return properties;
    }

    public void putProperties(String resourceKey, Properties properties) throws StorageException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            properties.store(out, "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] bytes = out.toByteArray();
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(bytes.length);
        objectMetadata.setContentType("text/plain");

        InputStream in = new ByteArrayInputStream(bytes);
        PutObjectRequest putReq = new PutObjectRequest(bucketName, resourceKey, in, objectMetadata);
        putObject(putReq);
    }

    public Stream<S3ObjectSummary> objectStream(String prefix) {
        return StreamSupport.stream(
                S3Objects.withPrefix(conn, bucketName, prefix).spliterator(), false);
    }

    private class BulkDelete implements Callable<Long> {

        private final String prefix;

        private final long timestamp;

        private final AmazonS3 conn;

        private final String bucketName;

        public BulkDelete(
                final AmazonS3 conn,
                final String bucketName,
                final String prefix,
                final long timestamp) {
            this.conn = conn;
            this.bucketName = bucketName;
            this.prefix = prefix;
            this.timestamp = timestamp;
        }

        @Override
        public Long call() throws Exception {
            long count = 0L;
            try {
                checkInterrupted();
                S3BlobStore.log.info(
                        String.format("在“%s/%s”上运行批量删除:%d", bucketName, prefix, timestamp));
                Predicate<S3ObjectSummary> filter = new TimeStampFilter(timestamp);
                AtomicInteger n = new AtomicInteger(0);
                Iterable<List<S3ObjectSummary>> partitions =
                        objectStream(prefix)
                                .filter(filter)
                                .collect(Collectors.groupingBy((x) -> n.getAndIncrement() % 1000))
                                .values();

                for (List<S3ObjectSummary> partition : partitions) {

                    checkInterrupted();

                    List<KeyVersion> keys = new ArrayList<>(partition.size());
                    for (S3ObjectSummary so : partition) {
                        String key = so.getKey();
                        keys.add(new KeyVersion(key));
                    }

                    checkInterrupted();

                    if (!keys.isEmpty()) {
                        DeleteObjectsRequest deleteReq = new DeleteObjectsRequest(bucketName);
                        deleteReq.setQuiet(true);
                        deleteReq.setKeys(keys);

                        checkInterrupted();

                        conn.deleteObjects(deleteReq);
                        count += keys.size();
                    }
                }
            } catch (InterruptedException | IllegalStateException e) {
                S3BlobStore.log.info(
                        String.format("“%s/%s”的 S3 批量删除中止。 将在下次启动时恢复。", bucketName, prefix));
                throw e;
            } catch (Exception e) {
                S3BlobStore.log.warn(
                        String.format("执行批量 S3 删除“%s/%s”时出现未知错误", bucketName, prefix), e);
                throw e;
            }

            S3BlobStore.log.info(
                    String.format(
                            "已完成对“%s/%s”的批量删除:%d。 已删除 %d 个对象",
                            bucketName, prefix, timestamp, count));

            S3Ops.this.clearPendingBulkDelete(prefix, timestamp);
            return count;
        }

        private void checkInterrupted() throws InterruptedException {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        }
    }

    /** 过滤比给定时间戳更新的对象 */
    private static class TimeStampFilter implements Predicate<S3ObjectSummary> {

        private long timeStamp;

        public TimeStampFilter(long timeStamp) {
            this.timeStamp = timeStamp;
        }

        @Override
        public boolean test(S3ObjectSummary summary) {
            long lastModified = summary.getLastModified().getTime();
            boolean applies = timeStamp >= lastModified;
            return applies;
        }
    }
}
