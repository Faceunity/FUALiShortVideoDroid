package com.aliyun.svideo.common.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import java.io.File;

/**
 * @author cross_ly DATE 2019/01/24
 * <p>描述:
 */
public class FileUtils {

    /**
     * 获取sdcard剩余内存
     * @return 单位b
     */
    public static long getSdcardAvailableSize() {

        File directory = Environment.getExternalStorageDirectory();

        StatFs statFs = new StatFs(directory.getPath());
        //获取可供程序使用的Block数量
        long blockAvailable = statFs.getAvailableBlocks();
        //获得Sdcard上每个block的size
        long blockSize = statFs.getBlockSize();

        return blockAvailable * blockSize;
    }

    /**
     * 获取sdcard总内存大小
     * @return 单位b
     */
    public static long getSdcardTotalSize() {

        File directory = Environment.getExternalStorageDirectory();

        StatFs statFs = new StatFs(directory.getPath());
        //获得sdcard上 block的总数
        long blockCount = statFs.getBlockCount();
        //获得sdcard上每个block 的大小
        long blockSize = statFs.getBlockSize();

        return blockCount * blockSize;
    }

    public static File getApplicationSdcardPath(Context context) {
        File var1 = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        if (var1 == null) {
            var1 = context.getFilesDir();
        }

        return var1;
    }

    public static boolean deleteFD(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        } else {
            File var1 = new File(path);
            File var2 = new File(var1.getAbsolutePath() + System.currentTimeMillis());
            var1.renameTo(var2);
            return deleteFD(var2);
        }
    }

    public static boolean deleteFD(File fd) {
        if (!fd.exists()) {
            return false;
        } else {
            return fd.isDirectory() ? deleteDirectory(fd) : fd.delete();
        }
    }

    public static boolean deleteDirectory(File dir) {
        clearDirectory(dir);
        return dir.delete();
    }

    public static void clearDirectory(File dir) {
        File[] var1 = dir.listFiles();
        if (var1 != null) {
            File[] var2 = var1;
            int var3 = var1.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                File var5 = var2[var4];
                if (var5.isDirectory()) {
                    deleteDirectory(var5);
                } else {
                    var5.delete();
                }
            }

        }
    }
}
