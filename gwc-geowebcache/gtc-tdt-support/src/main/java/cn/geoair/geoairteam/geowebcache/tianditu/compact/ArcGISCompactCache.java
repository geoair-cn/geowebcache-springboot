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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geowebcache.io.Resource;

/**
 * ArcGIS 压缩缓存的抽象基类.
 *
 * @author geoair
 */
public abstract class ArcGISCompactCache {
    private static final Log logger = LogFactory.getLog(ArcGISCompactCache.class);

    protected static final String BUNDLX_EXT = ".bundlx";

    protected static final String BUNDLE_EXT = ".bundle";

    protected static final int BUNDLX_MAXIDX = 128;

    protected String pathToCacheRoot = "";

    /**
     * 获取 tile 的 Resource 对象。
     *
     * @param zoom – 缩放级别。
     * @param row - tile行。
     * @param col - tile列。
     * @return 如果 tile 存在，则与 tile 图像数据关联的资源对象； 否则为空
     */
    public abstract Resource getBundleFileResource(int zoom, int row, int col);

    /**
     * 从没有文件扩展名的缩放、列和行构建包的路径。
     *
     * @param zoom – 缩放级别。
     * @param row - tile行。
     * @param col - tile列。
     * @return 包含不带文件扩展名的完整路径的字符串，格式为.../Lzz/RrrrrCcccc，c 和 r 的数量至少为 4。
     */
    protected String buildBundleFilePath(int zoom, int row, int col) {
        StringBuilder bundlePath = new StringBuilder(pathToCacheRoot);

        int baseRow = (row / BUNDLX_MAXIDX) * BUNDLX_MAXIDX;
        int baseCol = (col / BUNDLX_MAXIDX) * BUNDLX_MAXIDX;

        String zoomStr = Integer.toString(zoom);
        if (zoomStr.length() < 2) zoomStr = "0" + zoomStr;

        StringBuilder rowStr = new StringBuilder(Integer.toHexString(baseRow));
        StringBuilder colStr = new StringBuilder(Integer.toHexString(baseCol));

        // 列和行的长度至少为 4 个字符
        final int padding = 4;

        while (colStr.length() < padding) colStr.insert(0, "0");

        while (rowStr.length() < padding) rowStr.insert(0, "0");

        bundlePath
                .append("L")
                .append(zoomStr)
                .append(File.separatorChar)
                .append("R")
                .append(rowStr)
                .append("C")
                .append(colStr);

        return bundlePath.toString();
    }

    /**
     * 从使用小端字节序的文件中读取。
     *
     * @param filePath – 文件路径
     * @param offset – 在偏移处读取
     * @param length – 读取长度字节
     * @return 包含读取字节并将字节顺序设置为小端的 ByteBuffer。 字节缓冲区的长度是 4 的倍数，因此即使读取的字节数较少，也可以使用 getInt() 和
     *     getLong()。
     */
    protected ByteBuffer readFromLittleEndianFile(String filePath, long offset, int length) {
        ByteBuffer result = null;

        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            file.seek(offset);
            // 填充到 4 的倍数，以便我们可以使用 getInt() 和 getLong()
            int padding = 4 - (length % 4);
            byte[] data = new byte[length + padding];

            if (file.read(data, 0, length) != length) throw new IOException("读取的字节数不足或到达文件末尾");

            result = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        } catch (IOException e) {
            logger.warn("无法从小端文件读取", e);
        }

        return result;
    }
}
