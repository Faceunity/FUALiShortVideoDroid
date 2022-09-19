package com.aliyun.svideo.editor.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    /**
     * 文件读取缓冲区大小
     */
    private static final int CACHE_SIZE = 1024;

    /**
     * <p>
     * 压缩文件
     * </p>
     *
     * @param sourceFolder 压缩文件夹
     * @param zipFilePath 压缩文件输出路径
     * @throws Exception
     */
    public static void zip(String sourceFolder, String zipFilePath) throws Exception {
        OutputStream out = new FileOutputStream(zipFilePath);
        BufferedOutputStream bos = new BufferedOutputStream(out);
        ZipOutputStream zos = new ZipOutputStream(bos);
        File file = new File(sourceFolder);
        String basePath = null;
        if (file.isDirectory()) {
            basePath = file.getPath();
        } else {
            basePath = file.getParent();
        }
        zipFile(file, basePath, zos);
        zos.closeEntry();
        zos.close();
        bos.close();
        out.close();
    }

    /**
     * 递归压缩文件
     *
     * @param parentFile
     * @param basePath
     * @param zos
     * @throws Exception
     */
    private static void zipFile(File parentFile, String basePath, ZipOutputStream zos) throws Exception {
        File[] files = new File[0];
        if (parentFile.isDirectory()) {
            files = parentFile.listFiles();
        } else {
            files = new File[1];
            files[0] = parentFile;
        }
        String pathName;
        InputStream is;
        BufferedInputStream bis;
        byte[] cache = new byte[CACHE_SIZE];
        for (File file : files) {
            if (file.isDirectory()) {
                pathName = file.getPath().substring(basePath.length() + 1) + "/";
                zos.putNextEntry(new ZipEntry(pathName));
                zipFile(file, basePath, zos);
            } else {
                pathName = file.getPath().substring(basePath.length() + 1);
                is = new FileInputStream(file);
                bis = new BufferedInputStream(is);
                zos.putNextEntry(new ZipEntry(pathName));
                int nRead = 0;
                while ((nRead = bis.read(cache, 0, CACHE_SIZE)) != -1) {
                    zos.write(cache, 0, nRead);
                }
                bis.close();
                is.close();
            }
        }
    }

    /**
     * 解压压缩包
     *
     * @param zipFilePath 压缩文件路径
     * @param destDir 压缩包释放目录
     * @throws Exception
     */
    public static void unZip(String zipFilePath, String destDir) throws Exception {
        ZipFile zipFile = new ZipFile(zipFilePath);
        Enumeration<?> emu = zipFile.entries();
        BufferedInputStream bis;
        FileOutputStream fos;
        BufferedOutputStream bos;
        File file, parentFile;
        ZipEntry entry;
        String entryName = null;
        byte[] cache = new byte[CACHE_SIZE];
        StringBuffer sb = new StringBuffer();
        while (emu.hasMoreElements()) {
            entry = (ZipEntry) emu.nextElement();
            if (entry.getName().contains("/")) {
                entryName = entry.getName().substring(entry.getName().lastIndexOf("/"), entry.getName().length());
            } else {
                entryName = entry.getName();
            }
            if (entry.isDirectory()) {
                new File(destDir + entryName).mkdirs();
                continue;
            }
            bis = new BufferedInputStream(zipFile.getInputStream(entry));
            sb.delete(0, sb.length());
            sb.append(destDir).append(File.separator).append(entryName);
            file = new File(sb.toString());
            parentFile = file.getParentFile();
            if (parentFile != null && (!parentFile.exists())) {
                parentFile.mkdirs();
            }
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos, CACHE_SIZE);
            int nRead = 0;
            while ((nRead = bis.read(cache, 0, CACHE_SIZE)) != -1) {
                fos.write(cache, 0, nRead);
            }
            bos.flush();
            bos.close();
            fos.close();
            bis.close();
        }
        zipFile.close();
    }

}