package cn.geoair.geoairteam.gwc.service.gwc;

import cn.geoair.base.log.GiLogger;
import cn.geoair.base.log.GirLogger;
import cn.hutool.core.img.Img;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import cn.geoair.geoairteam.gwc.model.gwc.enums.GtcWmtsType;
import cn.geoair.geoairteam.gwc.servface.gwc.LayerCacheService;
import cn.geoair.geoairteam.gwc.service.utils.GtcFileTileCacheUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 瓦片缓存Service（纯文件系统实现，无Redis依赖）
 * 最终优化版：
 * 1. 先重命名目录（含随机值）再异步删除，彻底隔离删除与业务请求
 * 2. Linux下用rm -rf命令，Windows用Hutool，提升删除效率
 * 3. 异步删除，无阻塞主线程
 * 4. 重命名后缀含随机值，避免目录名冲突
 *
 * @author zhangjun
 * @date 2023-10-24
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class LayerCacheWithFileServiceImpl implements LayerCacheService {

    private static final GiLogger loger = GirLogger.getLoger(LayerCacheWithFileServiceImpl.class);
    private final String cacheRootDir;
    // 异步删除线程池（核心数=CPU核心数，平衡IO并发）
    private final ExecutorService deleteExecutor = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors())
    );
    // 系统类型标识
    private static final boolean IS_LINUX = System.getProperty("os.name").toLowerCase().contains("linux");
    // 待删除目录的重命名前缀（固定）
    private static final String TO_DELETE_PREFIX = "_to_delete_";

    /**
     * 构造函数初始化缓存根目录
     */
    public LayerCacheWithFileServiceImpl() {
        String baseCacheDir = System.getProperty("GTC_CACHE_DIR");
        this.cacheRootDir = FileUtil.normalize(baseCacheDir + File.separator + "gtcCache");
        File mkdir = FileUtil.mkdir(this.cacheRootDir);
        loger.info("瓦片缓存根目录初始化完成：{}，系统类型：{}", mkdir.getAbsolutePath(), IS_LINUX ? "Linux" : "Windows");
    }

    /**
     * 获取分组+排序键对应的缓存目录
     */
    private String getGroupCacheDir(String rootDirName) {
        return FileUtil.normalize(cacheRootDir + File.separator + rootDirName);
    }

    /**
     * 构建完整的瓦片文件路径
     */
    private String buildTileFilePath(GtcFileTileCacheUtils cacheUtils) {
        String groupDir = getGroupCacheDir(cacheUtils.getRootDirName());
        String tilePath = cacheUtils.getTilePath();
        return FileUtil.normalize(groupDir + File.separator + tilePath);
    }

    /**
     * 创建缓存目录（层级目录）
     */
    private void createCacheDir(GtcFileTileCacheUtils cacheUtils) {
        String tileFilePath = buildTileFilePath(cacheUtils);
        String parentDir = FileUtil.getParent(tileFilePath, 1);
        if (!FileUtil.exist(parentDir)) {
            FileUtil.mkdir(parentDir);
            loger.info("创建瓦片缓存目录成功：{}", parentDir);
        }
    }

    /**
     * 生成唯一的重命名后缀（时间戳 + 随机UUID）
     * 核心优化：随机值采用UUID，保证全局唯一，避免重名冲突
     */
    private String generateUniqueDeleteSuffix() {
        // 时间戳：保证按删除时间排序，便于排查
        // UUID前8位：短且足够随机，避免冲突
        return TO_DELETE_PREFIX + System.currentTimeMillis() + "_" + IdUtil.fastSimpleUUID().substring(0, 8);
    }

    /**
     * 重命名目录（原子操作）
     *
     * @param originalDir 原目录
     * @return 重命名后的目录，null表示失败
     */
    private File renameDirForDelete(File originalDir) {
        if (!originalDir.exists() || !originalDir.isDirectory()) {
            return null;
        }

        // 生成唯一重命名后缀
        String uniqueSuffix = generateUniqueDeleteSuffix();
        String newDirPath = originalDir.getParent() + File.separator + originalDir.getName() + uniqueSuffix;
        File newDir = new File(newDirPath);

        // 原子性重命名（文件系统层面保证，不会中断）
        boolean renameSuccess = originalDir.renameTo(newDir);
        if (renameSuccess) {
            loger.info("目录重命名成功（待删除）：{} -> {}", originalDir.getAbsolutePath(), newDir.getAbsolutePath());
            return newDir;
        } else {
            loger.error("目录重命名失败（待删除）：{} -> {}", originalDir.getAbsolutePath(), newDir.getAbsolutePath());
            return null;
        }
    }

    /**
     * 按系统类型删除目录
     * Linux：调用rm -rf（效率远高于Java IO）
     * Windows：使用Hutool的递归删除
     */
    private boolean deleteDirByOs(File dir) {
        if (!dir.exists()) {
            loger.info("待删除目录不存在：{}", dir.getAbsolutePath());
            return true;
        }

        try {
            if (IS_LINUX) {
                // Linux下执行rm -rf命令（异步执行，避免阻塞）
                String cmd = String.format("rm -rf %s", dir.getAbsolutePath());
                Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
                // 等待命令执行完成，超时1分钟
                boolean isSuccess = process.waitFor(10, TimeUnit.MINUTES) && process.exitValue() == 0;
                if (isSuccess) {
                    loger.info("Linux系统删除目录成功：{}", dir.getAbsolutePath());
                } else {
                    loger.error("Linux系统删除目录失败，退出码：{}，目录：{}", process.exitValue(), dir.getAbsolutePath());
                }
                return isSuccess;
            } else {
                // Windows系统使用Hutool删除
                boolean isSuccess = FileUtil.del(dir);
                if (isSuccess) {
                    loger.info("Windows系统删除目录成功：{}", dir.getAbsolutePath());
                } else {
                    loger.error("Windows系统删除目录失败：{}", dir.getAbsolutePath());
                }
                return isSuccess;
            }
        } catch (Exception e) {
            loger.error(e, "删除目录异常：{}", dir.getAbsolutePath());
            return false;
        }
    }

    /**
     * 异步删除目录（重命名后执行）
     */
    private void asyncDeleteDir(File originalDir, String desc) {
        // 先重命名
        File renamedDir = renameDirForDelete(originalDir);
        if (renamedDir == null) {
            loger.info("{}对应的目录无需删除（不存在或重命名失败）：{}", desc, originalDir.getAbsolutePath());
            return;
        }

        // 提交异步删除任务
        deleteExecutor.submit(() -> deleteDirByOs(renamedDir));
    }

    /**
     * 批量异步删除目录
     */
    private void batchAsyncDeleteDirs(List<File> dirs, String groupName) {
        if (dirs.isEmpty()) {
            loger.info("未找到分组{}对应的缓存目录", groupName);
            return;
        }
        dirs.forEach(dir -> asyncDeleteDir(dir, "分组" + groupName));
    }

    @Override
    public void delCacheByGroupName(String groupName) {
        String dirPrefix = "cache_" + groupName ;
        File rootDir = new File(cacheRootDir);

        if (!rootDir.exists() || !rootDir.isDirectory()) {
            loger.info("缓存根目录不存在，分组{}无缓存可删", groupName);
            return;
        }
        File[] allSubDirs = new File(cacheRootDir).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() && pathname.getName().startsWith(dirPrefix);
            }
        });


        if (allSubDirs != null) {
            batchAsyncDeleteDirs(Arrays.asList(allSubDirs), groupName);
        }
    }

    @Override
    public void delCacheByGroupNameNAndSortKey(String groupName, String sortKey) {
        String sortKeyMd5 = SecureUtil.md5().digestHex16(sortKey);
        String targetDirName = "cache_" + groupName + "_" + sortKeyMd5;
        File targetDir = new File(getGroupCacheDir(targetDirName));
        asyncDeleteDir(targetDir, "分组" + groupName + "+排序键" + sortKey);
    }

    @Override
    public void putCache(GtcWmtsType gtcWmtsType, String groupName, String sortKey, String[] xyz, BufferedImage bufferedImage, Long timeout) {
        String x = xyz[0];
        String y = xyz[1];
        String z = xyz[2];
        String sortKeyMd5 = SecureUtil.md5().digestHex16(sortKey);

        GtcFileTileCacheUtils cacheUtils = GtcFileTileCacheUtils.of(groupName, sortKeyMd5, z, x, y);
        String tileFilePath = buildTileFilePath(cacheUtils);

        try {
            createCacheDir(cacheUtils);
            // 写入图片（PNG格式）
            Img.from(bufferedImage)
                    .setQuality(0.5)
                    .setTargetImageType(ImgUtil.IMAGE_TYPE_PNG)
                    .write(new File(tileFilePath));

            loger.debug("瓦片缓存写入成功：{}", tileFilePath);
        } catch (Exception e) {
            loger.error(e, "缓存文件写入异常-groupName-{},sortKey-{},路径-{}",
                    groupName, sortKey, tileFilePath);
        }
    }

    @Override
    public boolean hasCache(GtcWmtsType gtcWmtsType, String groupName, String sortKey, String[] xyz) {
        String x = xyz[0];
        String y = xyz[1];
        String z = xyz[2];
        String sortKeyMd5 = SecureUtil.md5().digestHex16(sortKey);

        GtcFileTileCacheUtils cacheUtils = GtcFileTileCacheUtils.of(groupName, sortKeyMd5, z, x, y);
        String tileFilePath = buildTileFilePath(cacheUtils);

        boolean exists = FileUtil.exist(tileFilePath);
        if (exists) {
            loger.trace("瓦片缓存存在：{}", tileFilePath);
            return true;
        } else {
            loger.trace("瓦片缓存不存在：{}", tileFilePath);
            synchronized (this) {
                createCacheDir(cacheUtils);
            }
            return false;
        }
    }

    @Override
    public BufferedImage getCacheImg(GtcWmtsType gtcWmtsType, String groupName, String sortKey, String[] xyz) {
        String x = xyz[0];
        String y = xyz[1];
        String z = xyz[2];
        String sortKeyMd5 = SecureUtil.md5().digestHex16(sortKey);

        GtcFileTileCacheUtils cacheUtils = GtcFileTileCacheUtils.of(groupName, sortKeyMd5, z, x, y);
        String tileFilePath = buildTileFilePath(cacheUtils);

        File tileFile = new File(tileFilePath);
        if (!tileFile.exists()) {
            loger.warn("获取缓存图片失败，文件不存在：{}", tileFilePath);
            return null;
        }

        try {
            return ImgUtil.read(tileFile);
        } catch (Exception e) {
            loger.error(e, "读取缓存图片异常：{}", tileFilePath);
            return null;
        }
    }

    /**
     * 优雅关闭线程池（应用停止时调用）
     */
    @PreDestroy
    public void destroy() throws Exception {
        deleteExecutor.shutdown();
        if (!deleteExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
            deleteExecutor.shutdownNow();
            loger.warn("瓦片缓存删除线程池强制关闭");
        }
        loger.info("瓦片缓存删除线程池已关闭");
    }
}
