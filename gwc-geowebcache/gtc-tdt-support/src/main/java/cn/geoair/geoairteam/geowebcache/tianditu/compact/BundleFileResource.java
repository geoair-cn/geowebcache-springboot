/**
 * 该程序是免费软件：您可以根据自由软件基金会发布的 GNU 宽松通用公共许可证 （许可证的第 3 版或（由您选择）任何更高版本）的条款重新分发和/或修改它.
 *
 * <p>分发此程序是希望它有用，但不作任何保证； 甚至没有对适销性或针对特定目的的适用性的暗示保证。 有关更多详细信息，请参阅 GNU 通用公共许可证.
 *
 * <p>您应该已经收到一份 GNU 宽松通用公共许可证以及该程序。如果没有，请参阅 <http:www.gnu.orglicenses>。
 *
 * @author geoair，版权所有 2021
 */
package cn.geoair.geoairteam.geowebcache.tianditu.compact;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geowebcache.io.Resource;

/** @author geoair */
public class BundleFileResource implements Resource {
    private static Log log = LogFactory.getLog(BundleFileResource.class);

    private final String bundleFilePath;

    private final long tileOffset;

    private final int tileSize;

    public BundleFileResource(String bundleFilePath, long tileOffset, int tileSize) {
        this.bundleFilePath = bundleFilePath;
        this.tileOffset = tileOffset;
        this.tileSize = tileSize;
    }

    /** @see Resource#getSize() */
    public long getSize() {
        return tileSize;
    }

    /** @see Resource#transferTo(WritableByteChannel) */
    @SuppressWarnings("PMD.EmptyWhileStmt")
    public long transferTo(WritableByteChannel target) throws IOException {
        try (FileInputStream fin = new FileInputStream(new File(bundleFilePath));
                FileChannel in = fin.getChannel()) {
            final long size = tileSize;
            long written = 0;
            while ((written += in.transferTo(tileOffset + written, size, target)) < size) ;
            return size;
        }
    }

    /**
     * 不支持 ArcGIS 缓存，因为它们是只读的
     *
     * @see Resource#transferFrom(ReadableByteChannel)
     */
    public long transferFrom(ReadableByteChannel channel) throws IOException {
        // 不支持
        return 0;
    }

    /** @see Resource#getInputStream() */
    public InputStream getInputStream() throws IOException {
        FileInputStream fis = new FileInputStream(bundleFilePath);
        long skipped = fis.skip(tileOffset);
        if (skipped != tileOffset) {
            log.error(
                    "tried to skip to tile offset "
                            + tileOffset
                            + " in "
                            + bundleFilePath
                            + " but skipped "
                            + skipped
                            + " instead.");
        }
        return fis;
    }

    /**
     * 不支持 ArcGIS 缓存，因为它们是只读的。
     *
     * @see Resource#getOutputStream()
     */
    public OutputStream getOutputStream() throws IOException {
        // unsupported
        return null;
    }

    /** @see Resource#getLastModified() */
    public long getLastModified() {
        File f = new File(bundleFilePath);

        return f.lastModified();
    }
}
