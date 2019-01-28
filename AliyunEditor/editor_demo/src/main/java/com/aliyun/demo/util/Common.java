/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.util;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import com.aliyun.common.logger.Logger;
import com.aliyun.common.utils.StorageUtils;
import com.aliyun.demo.editor.R;
import com.aliyun.demo.http.EffectService;
import com.aliyun.downloader.DownloaderManager;
import com.aliyun.downloader.FileDownloaderModel;
import com.aliyun.jasonparse.JSONSupport;
import com.aliyun.jasonparse.JSONSupportImpl;
import com.aliyun.svideo.sdk.external.struct.form.AspectForm;
import com.aliyun.svideo.sdk.external.struct.form.PasterForm;
import com.aliyun.svideo.sdk.external.struct.form.ResourceForm;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class Common {

//    public final static String SD_DIR = Environment.getExternalStorageDirectory().getPath()
//            + "/";

    public static String SD_DIR ;
    //    public static final String BASE_URL = "http://m.api.inner.alibaba.net"; //内网地址（开发环境）
    public static final String BASE_URL = "https://m-api.qupaicloud.com";   //外网地址（正式环境）TODO:上线前要干掉

    public final static String QU_NAME = "AliyunEditorDemo";
    public static String QU_DIR;
    private static Object object = new Object();
    private static WeakReference<View> mView;

    public final static String QU_COLOR_FILTER = "aliyun_svideo_filter";
    public final static String QU_ANIMATION_FILTER = "aliyun_svideo_animation_filter";
    public final static String QU_MV = "aliyun_svideo_mv";
    public final static String QU_CAPTION = "aliyun_svideo_caption";
    public final static String QU_OVERLAY = "aliyun_svideo_overlay";
    public final static String QU_TAIL_IMG = "tail_img/qupai-logo.png";

    private final static String MV1_1 = "folder1.1";
    private final static String MV3_4 = "folder3.4";
    private final static String MV4_3 = "folder4.3";
    private final static String MV9_16 = "folder9.16";
    private final static String MV16_9 = "folder16.9";

    public static String[] mv_dirs = {
            MV1_1,
            MV3_4,
            MV4_3,
            MV9_16,
            MV16_9
    };

    public static String getMVPath(List<AspectForm> list, int w, int h) {
        String path = null;
        if(list == null || list.size() == 0) {
            return path;
        }
        path = calculatePercent(list, w, h);
        return path;
    }

    public static String calculatePercent(List<AspectForm> list, int w, int h) {
        int result = 0;
        String path = null;
        if(list == null || list.size() == 0 || h <= 0 || w <= 0) {
            return path;
        }
        float percent = (float)w/h;
        int aspect = 0;
        Map map = new IdentityHashMap();
        for(int i = 0; i < list.size(); i++) {
            aspect = list.get(i).getAspect();
            path = list.get(i).getPath();
            if(aspect == 1 && exits(path + File.separator + MV1_1)) {
                map.put(new Integer(1), (float)1);
            } else if(aspect == 2) {
                if(exits(path + File.separator + MV3_4)) {
                    map.put(new Integer(2), (float)3/4);
                }
                if(exits(path + File.separator + MV4_3)) {
                    map.put(new Integer(3), (float)4/3);
                }
            } else if(aspect == 3) {
                if(exits(path + File.separator + MV9_16)) {
                    map.put(new Integer(4), (float)9/16);
                }
                if(exits(path + File.separator + MV16_9)) {
                    map.put(new Integer(5), (float)16/9);
                }
            }
        }

        float diffNum = -1;
        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            if(diffNum == -1) {
                diffNum = Math.abs(percent - (float)entry.getValue());
                result = (Integer) entry.getKey();
                continue;
            }

            float diffNumTemp = Math.abs(percent - (float)entry.getValue());
            if(diffNum >= diffNumTemp) {
                diffNum = diffNumTemp;
                result = (Integer) entry.getKey();

            }
        }
        if(result != 0) {
            for(AspectForm form : list) {
                if(result == 1 && form.getAspect() == 1) {
                    path = form.getPath();
                    break;
                } else if ((result == 2 || result == 3) && form.getAspect() == 2) {
                    path = form.getPath();
                    break;
                } else if ((result == 4 || result == 5) && form.getAspect() == 3) {
                    path = form.getPath();
                    break;
                }
            }
            path = path + File.separator + mv_dirs[result - 1];
        }
        return path;
    }

    public static boolean exits(String path) {
        boolean isExits = false;
        if(path == null || "".equals(path)) {
            return isExits;
        }
        File file = new File(path);
        if(file.exists()) {
            isExits = true;
        }
        return isExits;
    }

    public static int getTotal(int[] src) {
        int total = 0;
        for(int i = 0; i < src.length; i++) {
            total += src[i];
        }
        return total;
    }

    public static void copySelf(Context cxt, String root) {
        try {
            String[] files = cxt.getAssets().list(root);
            if(files.length > 0) {
                File subdir = new File(Common.SD_DIR + root);
                if (!subdir.exists()) {
                    subdir.mkdirs();
                }

                for (String fileName : files) {
                    File fileTemp = new File(Common.SD_DIR + root + File.separator + fileName);
                    if (fileTemp.exists()){
                        continue;
                    }
                    copySelf(cxt,root + File.separator + fileName);
                }
            }else{
                Logger.getDefaultLogger().d("copy...."+Common.SD_DIR+root);
                OutputStream myOutput = new FileOutputStream(Common.SD_DIR+root);
                InputStream myInput = cxt.getAssets().open(root);
                byte[] buffer = new byte[1024 * 8];
                int length = myInput.read(buffer);
                while(length > 0)
                {
                    myOutput.write(buffer, 0, length);
                    length = myInput.read(buffer);
                }

                myOutput.flush();
                myInput.close();
                myOutput.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyAll(Context cxt, View view) {
        SD_DIR = StorageUtils.getCacheDirectory(cxt).getAbsolutePath() + File.separator;
        QU_DIR = SD_DIR + QU_NAME + File.separator;
        mView = new WeakReference<>(view);
        File dir = new File(Common.QU_DIR);
        copySelf(cxt,QU_NAME);
        dir.mkdirs();
        unZip();
    }

    private static boolean isViewDestroy(){
        return mView.get() == null || !(mView.get().getVisibility() == View.VISIBLE);
    }

    private static void insertDB(String name) {
        if(name.endsWith(QU_MV)) {
            insertMV();
        } else if (name.endsWith(QU_CAPTION)) {
            insertCaption();
        } else if (name.endsWith(QU_OVERLAY)) {
            insertOverlay();
        }
    }
    public static List<String> getColorFilterList(Context context) {
        String[] colorFilterList = context.getResources().getStringArray(R.array.filter_order);
        List<String> list = new ArrayList<>();
        for (String filter : colorFilterList) {
            File file = new File(QU_DIR+File.separator+QU_COLOR_FILTER,filter);
            if (file.exists()&&file.isDirectory()){
                list.add(file.getAbsolutePath());
            }
        }
        return list;
    }

    public static List<String> getAnimationFilterList() {
        List<String> list = new ArrayList<>();
        File file = new File(QU_DIR, QU_ANIMATION_FILTER);
        if(file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            for(File fileTemp : files) {
                if(fileTemp.exists()) {
                    list.add(fileTemp.getAbsolutePath());
                }
            }
        }
        return list;
    }

    public static void insertMV() {
        File file = new File(QU_DIR, QU_MV);
        if(file.exists() && file.isDirectory()) {
            String path = "";
            File[] files = file.listFiles();
            if(files == null) {
                return;
            }

            for(File fs : files) {
                if(fs.exists() && fs.isDirectory()) {
                    String name = fs.getName();
                    File[] filesTemp = fs.listFiles();
                    if(filesTemp == null) {
                        return;
                    }
                    int id = 103;
                    for(File fileTemp : filesTemp) {
                        FileDownloaderModel model = new FileDownloaderModel();
                        model.setEffectType(EffectService.EFFECT_MV);
                        model.setName(name);
                        model.setId(id);
                        model.setPath(fs.getAbsolutePath());
                        if(path == null || "".equals(path)) {
                            path = fileTemp.getAbsolutePath() + File.separator + "icon.png";
                        }
                        model.setPreviewpic(path);
                        model.setIcon(path);
                        String pathTemp = fileTemp.getAbsolutePath();
                        if(pathTemp.endsWith(MV1_1)) {
                            model.setAspect(1);
                        } else if(pathTemp.endsWith(MV3_4) || pathTemp.endsWith(MV4_3)) {
                            model.setAspect(2);
                        } else if(pathTemp.endsWith(MV9_16) || pathTemp.endsWith(MV16_9)) {
                            model.setAspect(3);
                        }
                        model.setIsunzip(1);
                        model.setTaskId(FileDownloadUtils.generateId(String.valueOf(model.getAspect()), pathTemp));
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put(FileDownloaderModel.TASK_ID, String.valueOf(FileDownloadUtils.generateId(String.valueOf(model.getAspect()), pathTemp)));
                        hashMap.put(FileDownloaderModel.ID, String.valueOf(model.getId()));
                        hashMap.put(FileDownloaderModel.ASPECT, String.valueOf(model.getAspect()));
                        DownloaderManager.getInstance().getDbController().insertDb(model, hashMap);
                    }
                }
            }
        }
    }

    public static void insertCaption() {
        File file = new File(QU_DIR, QU_CAPTION);
        if(file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if(files == null) {
                return;
            }
            for(File fs : files) {
                FileDownloaderModel model = new FileDownloaderModel();
                model.setEffectType(EffectService.EFFECT_CAPTION);
                model.setId(166);
                model.setDescription("assets");//用于区分打包还是下载
                model.setIcon(fs.getAbsolutePath() + File.separator + "icon.png");
                model.setSubicon(fs.getAbsolutePath() + File.separator + "icon.png");
                model.setIsunzip(1);
                model.setName(fs.getName());
                model.setPath(fs.getAbsolutePath());
                model.setUrl(fs.getAbsolutePath());
                model.setTaskId(FileDownloadUtils.generateId(String.valueOf(model.getEffectType()), fs.getAbsolutePath()));

                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(FileDownloaderModel.ID, String.valueOf(model.getId()));
                hashMap.put(FileDownloaderModel.TASK_ID, String.valueOf(model.getTaskId()));
                DownloaderManager.getInstance().getDbController().insertDb(model, hashMap);
            }
        }
    }

    public static void insertOverlay() {
        File file = new File(QU_DIR, QU_OVERLAY);
        JSONSupport jsonSupport = new JSONSupportImpl();
        ResourceForm paster = null;
        if(file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            for(File fs : files) {
                if(fs.exists() && !fs.isDirectory()) {
                    try {
                        paster = jsonSupport.readValue(fs, ResourceForm.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if(paster != null) {
                    List<PasterForm> mPasterlList = paster.getPasterList();
                    if(mPasterlList == null) {
                        return;
                    }
                    String icon = "";
                    for(PasterForm pasterForm : mPasterlList) {
                        FileDownloaderModel model = new FileDownloaderModel();
                        model.setId(paster.getId());
                        model.setPath(QU_DIR + QU_OVERLAY + File.separator + pasterForm.getName());
                        icon = model.getPath() + "/icon.png";
                        model.setIcon(icon);
                        model.setDescription("assets");
                        model.setIsnew(paster.getIsNew());
                        model.setName(paster.getName());
                        model.setLevel(paster.getLevel());
                        model.setEffectType(EffectService.EFFECT_PASTER);

                        model.setSubid(pasterForm.getId());
                        model.setFontid(pasterForm.getFontId());
                        model.setSubicon(icon);
                        model.setSubname(pasterForm.getName());
                        model.setIsunzip(1);

                        model.setTaskId(FileDownloadUtils.generateId(String.valueOf(pasterForm.getId()), model.getPath()));

                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put(FileDownloaderModel.SUBID, String.valueOf(pasterForm.getId()));
                        hashMap.put(FileDownloaderModel.TASK_ID, String.valueOf(model.getTaskId()));
                        DownloaderManager.getInstance().getDbController().insertDb(model, hashMap);
                    }
                }
            }
        }
    }

    private static int length;
    public static void unZip() {
        File[] files = new File(Common.SD_DIR + QU_NAME).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if(name != null && name.endsWith(".zip")) {
                    return true;
                }
                return false;
            }
        });
        length = files.length;
        if(length == 0 && !isViewDestroy()) {
            mView.get().setVisibility(View.GONE);
            return;
        }
        for(final File file : files) {
            new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] objects) {
                    try {
                        int len = file.getAbsolutePath().length();
                        //判断解压后的文件是否存在,截取.zip之前的字符串
                        if (!new File(file.getAbsolutePath().substring(0, len - 4)).exists()) {
                            unZipFolder(file.getAbsolutePath(), Common.SD_DIR + QU_NAME);
                            insertDB(file.getAbsolutePath().substring(0, len - 4));
                        }
                        synchronized (object) {
                            length--;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Object o) {
                    synchronized (object) {
                        if (length == 0) {
                            if (!isViewDestroy()){
                                mView.get().setVisibility(View.GONE);
                            }
                        }
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public static void unZipFolder(String zipFileString, String outPathString) throws Exception {
        ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFileString));
        ZipEntry zipEntry;
        String szName = "";
        while ((zipEntry = inZip.getNextEntry()) != null) {
            szName = zipEntry.getName();
            if (zipEntry.isDirectory()) {
                // get the folder name of the widget
                szName = szName.substring(0, szName.length() - 1);
                File folder = new File(outPathString + File.separator + szName);
                folder.mkdirs();
            } else {

                File file = new File(outPathString + File.separator + szName);
                file.createNewFile();
                FileOutputStream out = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[1024];
                while ((len = inZip.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                    out.flush();
                }
                out.close();
            }
        }
        inZip.close();
    }
}
