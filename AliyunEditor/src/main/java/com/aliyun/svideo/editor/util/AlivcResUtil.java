package com.aliyun.svideo.editor.util;


import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.aliyun.common.utils.StorageUtils;
import com.aliyun.common.utils.StringUtils;
import com.aliyun.common.qupaiokhttp.FileDownloadCallback;
import com.aliyun.common.qupaiokhttp.HttpRequest;
import com.aliyun.svideo.base.Form.AnimationEffectForm;
import com.aliyun.svideo.base.Form.AspectForm;
import com.aliyun.svideo.base.Form.FontForm;
import com.aliyun.svideo.base.Form.IMVForm;
import com.aliyun.svideo.base.Form.PasterForm;
import com.aliyun.svideo.base.Form.ResourceForm;
import com.aliyun.svideo.base.http.EffectService;
import com.aliyun.svideo.base.http.HttpCallback;
import com.aliyun.svideo.downloader.DownloaderManager;
import com.aliyun.svideo.downloader.FileDownloaderCallback;
import com.aliyun.svideo.downloader.FileDownloaderModel;
import com.aliyun.svideo.editor.contant.CaptionConfig;
import com.aliyun.svideo.editor.contant.EditorConstants;
import com.aliyun.svideo.editor.effectmanager.CaptionLoader;
import com.aliyun.svideo.editor.effectmanager.EffectLoader;
import com.aliyun.svideo.editor.effectmanager.TasksManager;
import com.liulishuo.filedownloader.BaseDownloadTask;

import java.io.File;
import java.net.URLEncoder;
import java.util.List;

/**
 * 平台资源加载工具类
 */
public class AlivcResUtil {
    public static final String SCHEME = "alivc_resource";

    /**
     * 内置资源
     */
    public static final String HOST_APP = "app";
    /**
     * 线上资源
     */
    public static final String HOST_CLOUD = "cloud";
    /**
     * 本地相对资源
     */
    public static final String HOST_RELATION = "relation";

    /**
     * mv资源
     */
    public static final String TYPE_MV = "mv";
    /**
     * 字体资源
     */
    public static final String TYPE_FONT = "font";
    /**
     * 音乐资源
     */
    public static final String TYPE_MUSIC = "music";
    /**
     * 气泡字幕资源
     */
    public static final String TYPE_BUBBLE = "bubble";
    /**
     * 花字资源
     */
    public static final String TYPE_CAPTION = "caption";
    /**
     * 动态滤镜资源
     */
    public static final String TYPE_ANIMATION_EFFECTS = "animation_effects";
    /**
     * 普通滤镜资源
     */
    public static final String TYPE_FILTER = "filter";
    /**
     * 动图资源
     */
    public static final String TYPE_STICKER = "sticker";
    /**
     * 转场资源
     */
    public static final String TYPE_TRANSITION= "transition";

    /**
     * LUT资源
     */
    public static final String TYPE_LUT = "lut_filter";

    public interface LoadCallback {
        public void onSuccess(String path);

        public void onFailure(String type, String msg);
    }

    public static String getResUri(String host, String type, String groupId, String id) {
        try {
            groupId = URLEncoder.encode(groupId, "utf-8");
            id = URLEncoder.encode(id, "utf-8");
        } catch (Exception ignored) {
        }
        return String.format("alivc_resource://%s?type=%s&gid=%s&id=%s", host, type, groupId, id);
    }

    public static String getResUri(String host, String type, String id) {
        try {
            id = URLEncoder.encode(id, "utf-8");
        } catch (Exception ignored) {
        }
        return String.format("alivc_resource://%s?type=%s&id=%s", host, type, id);
    }

    public static String getAppResUri(String type, String groupId, String id) {
        return getResUri(HOST_APP, type, groupId, id);
    }

    public static String getAppResUri(String type, String id) {
        return getResUri(HOST_APP, type, id);
    }

    public static String getCloudResUri(String type, String groupId, String id) {
        return getResUri(HOST_CLOUD, type, groupId, id);
    }

    public static String getCloudResUri(String type, String id) {
        return getResUri(HOST_CLOUD, type, id);
    }

    public static String getRelationResUri(String path) {
        try {
            path = URLEncoder.encode(path, "utf-8");
        } catch (Exception ignored) {
        }
        return String.format("alivc_resource://%s?path=%s", HOST_RELATION, path);
    }
    /**
     * 加载平台资源
     * @param context 上下文
     * @param url 资源地址
     * @param callback 加载回调
     */
    public static void loadRes(Context context, String url, LoadCallback callback) {
        loadRes(context, url, null, callback);
    }

    /**
     * 加载平台资源
     * @param context 上下文
     * @param url 资源地址
     * @param relationDir 相对地址目录
     * @param callback 加载回调
     */
    public static void loadRes(Context context, String url, String relationDir, LoadCallback callback) {
        if (callback == null) {
            return;
        }
        Uri uri = Uri.parse(url);
        if (AlivcResUtil.HOST_APP.equals(uri.getHost())) {
            loadAppRes(uri, callback);
        } else if (AlivcResUtil.HOST_CLOUD.equals(uri.getHost())) {
            loadCloudRes(context, uri, callback);
        } else if (AlivcResUtil.HOST_RELATION.equals(uri.getHost())) {
            if (StringUtils.isEmpty(relationDir)) {
                callback.onFailure(null, "relation dir is null");
            } else {
                String path = uri.getQueryParameter("path");
                callback.onSuccess(relationDir + File.separator + path);
            }
        }
        Log.d("AlivcRes", "downloadDraft>resumeRes>" + uri.getScheme() + ">" + uri.getHost() + ">" + uri.getPath() + ">" + uri.getQueryParameter("type"));
    }

    /**
     * 下载网络资源
     *
     * @param url 资源地址
     * @param path 资源存放路径
     * @param callback 回调
     */
    public static void downloadRes(String url, final String path, final LoadCallback callback) {
        HttpRequest.download(url, new File(path), new FileDownloadCallback() {
            @Override
            public void onFailure() {
                callback.onFailure(null,"download failure");
            }

            @Override
            public void onDone() {
                callback.onSuccess(path);
            }
        });
    }

    /**
     * 加载内置资源
     * @param uri 资源路径
     * @param callback 处理回调
     */
    private static void loadAppRes(Uri uri, LoadCallback callback) {
        String type = uri.getQueryParameter("type");
        String groupId = uri.getQueryParameter("gid");
        String id = uri.getQueryParameter("id");
        String path = null;
        switch (type) {
            case TYPE_MV:
                path = getLocalPath(EffectService.EFFECT_MV, -1, Integer.parseInt(groupId));
                try {
                    path = path.substring(0, path.lastIndexOf(File.separator)+1) + id;
                } catch (Exception ignored) {
                }
                break;
            case TYPE_BUBBLE:
                path = getLocalPath(EffectService.EFFECT_CAPTION, Integer.parseInt(groupId), Integer.parseInt(id));
                break;
            case TYPE_STICKER:
                path = getLocalPath(EffectService.EFFECT_PASTER, Integer.parseInt(groupId), Integer.parseInt(id));
                break;
            case TYPE_FILTER:
                path = new File(EditorCommon.SD_DIR + EditorCommon.QU_NAME + File.separator + EditorCommon.QU_COLOR_FILTER, id).getAbsolutePath();
                break;
            case TYPE_CAPTION:
                path = new File(EditorCommon.SD_DIR + CaptionConfig.COOL_TEXT_FILE_DIR, id).getAbsolutePath();
                break;
            case TYPE_ANIMATION_EFFECTS:
                String groupPath = "";
                if (String.valueOf(EditorCommon.QU_ANIMATION_FILTER_ID).equals(groupId)) {
                    groupPath = EditorCommon.QU_ANIMATION_FILTER;
                } else if (String.valueOf(EditorCommon.QU_ANIMATION_SPLIT_SCREEN_FILTER_ID).equals(groupId)) {
                    groupPath = EditorCommon.QU_ANIMATION_SPLIT_SCREEN_FILTER;
                }
                path = new File(EditorCommon.SD_DIR + EditorCommon.QU_NAME + File.separator + groupPath, id).getAbsolutePath();
                break;
            case TYPE_LUT:
                path = new File(EditorCommon.SD_DIR + EditorConstants.LUT_FILE_DIR
                        +File.separator +EditorCommon.QU_LUT_FILTER_DEFAULT_GROUP
                        +File.separator+id+File.separator+"lookup.png").getAbsolutePath();
                break;
        }
        Log.d("AlivcRes", "loadAppRes>" + path);
        if (!StringUtils.isEmpty(path)) {
            callback.onSuccess(path);
        } else {
            callback.onFailure(type, "Not found");
        }
    }

    private static String getLocalPath(int type,int groupId,int id){
        List<FileDownloaderModel> models = DownloaderManager.getInstance().getDbController().getResourceByType(type);
        for (FileDownloaderModel item : models) {
            //没有分组
            if (groupId == -1) {
                if (item.getId() == id) {
                    return item.getPath();
                }
            } else if (groupId == item.getId() && item.getSubid() == id) {
                return item.getPath();
            }
        }
        return null;
    }

    /**
     * 加载平台云端资源
     * @param context 上下文
     * @param uri 资源路径
     * @param callback 处理回调
     */
    public static void loadCloudRes(Context context, Uri uri, final LoadCallback callback) {
        String type = uri.getQueryParameter("type");
        String groupId = uri.getQueryParameter("gid");
        String id = uri.getQueryParameter("id");
        switch (type) {
            case TYPE_MV:
                loadMv(context, Integer.parseInt(groupId), id, callback);
                return;
            case TYPE_MUSIC:
                loadMusicRes(context, id, uri.getQueryParameter("name"), callback);
                return;
            case TYPE_TRANSITION:
                loadTransition(context, Integer.parseInt(groupId), id, callback);
                return;
            case TYPE_ANIMATION_EFFECTS:
                loadAnimationFilter(context, Integer.parseInt(groupId), id, callback);
                return;
            case TYPE_BUBBLE:
                loadBubble(context, Integer.parseInt(groupId), Integer.parseInt(id), callback);
                return;
            case TYPE_STICKER:
                loadSticker(context, Integer.parseInt(groupId), Integer.parseInt(id), callback);
                return;
            case TYPE_FONT:
                loadFont(context, Integer.parseInt(id), callback);
                return;
        }
        callback.onFailure(null,"Type not found");
    }

    /**
     * 加载音乐资源
     *
     * @param context 上下文
     * @param musicId 音乐ID
     * @param title 音乐名称
     * @param callback 处理回调
     */
    public static void loadMusicRes(final Context context, String musicId, final String title, final LoadCallback callback) {
        String path = null;
        List<FileDownloaderModel> modelsTemp = DownloaderManager.getInstance().getDbController().getResourceByType(EffectService.EFFECT_MUSIC);
        for (FileDownloaderModel fileDownloaderModel : modelsTemp) {
            if (musicId.equals(fileDownloaderModel.getDownload()) && new File(fileDownloaderModel.getPath()).exists()) {
                path = fileDownloaderModel.getPath();
                break;
            }
        }
        if (!StringUtils.isEmpty(path)) {
            callback.onSuccess(path);
        } else {
            new EffectService().getMusicDownloadUrlById(context.getPackageName(), musicId, new HttpCallback<String>() {
                @Override
                public void onSuccess(final String url) {
                    final File file = new File(StorageUtils.getFilesDirectory(context) + "/svideo_res/cloud/music/" + title);
                    HttpRequest.download(url, file, new FileDownloadCallback() {
                        @Override
                        public void onDone() {
                            callback.onSuccess(file.getAbsolutePath());
                        }

                        @Override
                        public void onFailure() {
                            callback.onFailure(TYPE_MUSIC, "music download failure");
                        }
                    });
                }

                @Override
                public void onFailure(final Throwable e) {
                    callback.onFailure(TYPE_MUSIC, "music download failure " + e.getMessage());
                }
            });
        }
    }

    /**
     * 加载转场资源
     * @param context 上下文
     * @param groupId 转场资源包ID
     * @param id 转场资源ID
     * @param callback 处理回调
     */
    public static void loadTransition(final Context context, final int groupId, final String id, final LoadCallback callback) {
        String path = null;
        List<FileDownloaderModel> modelsTemp = DownloaderManager.getInstance().getDbController().getResourceByType(EffectService.EFFECT_TRANSITION);
        for (FileDownloaderModel fileDownloaderModel : modelsTemp) {
            if (fileDownloaderModel.getId() == groupId && new File(fileDownloaderModel.getPath()).exists()) {
                path = fileDownloaderModel.getPath() + File.separator + id;
                break;
            }
        }
        if(!StringUtils.isEmpty(path)){
            callback.onSuccess(path);
        } else {
            new EffectLoader().loadAllTransition(context,new EffectLoader.LoadCallback<AnimationEffectForm>(){

                @Override
                public void onLoadCompleted(final List<AnimationEffectForm> localInfos, final List<AnimationEffectForm> remoteInfos, final Throwable e) {
                    if (remoteInfos != null) {
                        for (AnimationEffectForm af : remoteInfos) {
                            if (af.getId() == groupId) {
                                FileDownloaderModel model = new FileDownloaderModel();
                                model.setEffectType(EffectService.EFFECT_TRANSITION);
                                model.setName(af.getName());
                                model.setNameEn(af.getNameEn());
                                model.setId(af.getId());
                                model.setPreviewpic(af.getPreviewImageUrl());
                                model.setIcon(af.getIconUrl());
                                model.setPreviewmp4(af.getPreviewMediaUrl());
                                model.setSort(af.getSort());
                                model.setSubtype(af.getType());
                                model.setDownload(af.getResourceUrl());
                                model.setUrl(af.getResourceUrl());
                                model.setDuration(af.getDuration());
                                model.setIsunzip(1);
                                final FileDownloaderModel task = DownloaderManager.getInstance().addTask(model, model.getDownload());
                                TasksManager tasksManager = new TasksManager();
                                tasksManager.addTask(task.getTaskId(), new FileDownloaderCallback(){
                                    @Override
                                    public void onFinish(final int downloadId, final String path) {
                                        String localPath = path + File.separator + id;
                                        callback.onSuccess(localPath);
                                    }

                                    @Override
                                    public void onError(final BaseDownloadTask task, final Throwable e) {
                                        callback.onFailure(TYPE_TRANSITION, e.getMessage());
                                    }
                                });
                                tasksManager.startTask();
                                return;
                            }
                        }
                    }
                    callback.onFailure(TYPE_TRANSITION, "Not found");
                }
            });
        }
    }

    /**
     * 加载动态滤镜资源
     * @param context 上下文
     * @param groupId 动态滤镜资源包ID
     * @param id 动态滤镜ID
     * @param callback 处理回调
     */
    public static void loadAnimationFilter(final Context context, final int groupId, final String id, final LoadCallback callback) {
        String path = null;
        List<FileDownloaderModel> modelsTemp = DownloaderManager.getInstance().getDbController().getResourceByType(EffectService.ANIMATION_FILTER);
        for (FileDownloaderModel fileDownloaderModel : modelsTemp) {
            if (fileDownloaderModel.getId() == groupId && new File(fileDownloaderModel.getPath()).exists()) {
                path = fileDownloaderModel.getPath() + File.separator + id;
                break;
            }
        }
        if (!StringUtils.isEmpty(path)) {
            callback.onSuccess(path);
        } else {
            new EffectLoader().loadAllAnimationFilter(context,new EffectLoader.LoadCallback<AnimationEffectForm>(){

                @Override
                public void onLoadCompleted(final List<AnimationEffectForm> localInfos, final List<AnimationEffectForm> remoteInfos, final Throwable e) {
                    if (remoteInfos != null) {
                        for (AnimationEffectForm af : remoteInfos) {
                            if (af.getId() == groupId) {
                                TasksManager tasksManager = new TasksManager();
                                FileDownloaderModel model = new FileDownloaderModel();
                                model.setEffectType(EffectService.ANIMATION_FILTER);
                                model.setName(af.getName());
                                model.setNameEn(af.getNameEn());
                                model.setId(af.getId());
                                model.setPreviewpic(af.getPreviewImageUrl());
                                model.setIcon(af.getIconUrl());
                                model.setPreviewmp4(af.getPreviewMediaUrl());
                                model.setSort(af.getSort());
                                model.setSubtype(af.getType());
                                model.setDownload(af.getResourceUrl());
                                model.setUrl(af.getResourceUrl());
                                model.setDuration(af.getDuration());
                                model.setIsunzip(1);
                                final FileDownloaderModel task = DownloaderManager.getInstance().addTask(model, model.getDownload());
                                tasksManager.addTask(task.getTaskId(), new FileDownloaderCallback(){
                                    @Override
                                    public void onFinish(final int downloadId, final String path) {
                                        callback.onSuccess(path + File.separator + id);
                                    }

                                    @Override
                                    public void onError(final BaseDownloadTask task, final Throwable e) {
                                        callback.onFailure(TYPE_ANIMATION_EFFECTS, e.getMessage());
                                    }
                                });
                                tasksManager.startTask();
                                return;
                            }
                        }
                    }
                    callback.onFailure(TYPE_ANIMATION_EFFECTS, "Not found");
                }
            });
        }
    }

    /**
     * 加载动图
     * @param context 上下文
     * @param groupId 动图资源包ID
     * @param id 动图资源ID
     * @param callback 处理回调
     */
    public static void loadSticker(final Context context, final int groupId, final int id, final LoadCallback callback) {
        String path = null;
        List<FileDownloaderModel> modelsTemp = DownloaderManager.getInstance().getDbController().getResourceByType(EffectService.EFFECT_PASTER);
        for (FileDownloaderModel fileDownloaderModel : modelsTemp) {
            if (fileDownloaderModel.getId() == groupId && fileDownloaderModel.getSubid() == id && new File(fileDownloaderModel.getPath()).exists()) {
                path = fileDownloaderModel.getPath();
                break;
            }
        }
        if(!StringUtils.isEmpty(path)){
            callback.onSuccess(path);
        } else {
            new EffectLoader().loadAllPaster(context,new EffectLoader.LoadCallback<ResourceForm>(){

                @Override
                public void onLoadCompleted(final List<ResourceForm> localInfos, final List<ResourceForm> remoteInfos, final Throwable e) {
                    if (remoteInfos != null) {
                        for (final ResourceForm paster : remoteInfos) {
                            if (paster.getId() == groupId) {
                                new EffectService().getPasterListById(context.getPackageName(), groupId, new HttpCallback<List<PasterForm>>() {

                                    @Override
                                    public void onSuccess(final List<PasterForm> result) {
                                        if (result != null) {
                                            for (final PasterForm material : result) {
                                                if (material.getId() == id) {
                                                    FileDownloaderModel model = new FileDownloaderModel();
                                                    model.setEffectType(EffectService.EFFECT_PASTER);
                                                    model.setName(paster.getName());
                                                    model.setNameEn(paster.getNameEn());
                                                    model.setDescription(paster.getDescription());
                                                    model.setDescriptionEn(paster.getDescriptionEn());
                                                    model.setIcon(paster.getIcon());
                                                    model.setIsnew(paster.getIsNew());
                                                    model.setId(paster.getId());
                                                    model.setLevel(paster.getLevel());
                                                    model.setPreview(paster.getPreviewUrl());
                                                    model.setSort(paster.getSort());
                                                    model.setSubname(material.getName());
                                                    model.setSubicon(material.getIcon());
                                                    model.setSubid(material.getId());
                                                    model.setPriority(material.getPriority());
                                                    model.setUrl(material.getDownloadUrl());
                                                    model.setMd5(material.getMD5());
                                                    model.setSubpreview(material.getPreviewUrl());
                                                    model.setSubsort(material.getSort());
                                                    model.setSubtype(material.getType());
                                                    model.setIsunzip(1);
                                                    final FileDownloaderModel task = DownloaderManager.getInstance().addTask(model, model.getUrl());
                                                    TasksManager tasksManager = new TasksManager();
                                                    tasksManager.addTask(task.getTaskId(), new FileDownloaderCallback(){
                                                        @Override
                                                        public void onFinish(final int downloadId, final String path) {
                                                            //在数据库中删除记录，不然会导致整组动画不能再下载
                                                            DownloaderManager.getInstance().getDbController().deleteTask(task.getTaskId());
                                                            callback.onSuccess(path);
                                                        }

                                                        @Override
                                                        public void onError(final BaseDownloadTask task, final Throwable e) {
                                                            callback.onFailure(TYPE_STICKER, e.getMessage());
                                                        }
                                                    });
                                                    tasksManager.startTask();
                                                    return;
                                                }
                                            }
                                        }
                                        callback.onFailure(TYPE_STICKER, "Not found");
                                    }

                                    @Override
                                    public void onFailure(final Throwable e) {
                                        callback.onFailure(TYPE_STICKER, e.getMessage());
                                    }
                                });
                                return;
                            }
                        }
                    }
                    callback.onFailure(TYPE_STICKER, "Not found");
                }
            });
        }
    }

    /**
     * 加载气泡字幕资源
     * @param context 上下文
     * @param groupId 气泡资源包ID
     * @param id 气泡ID
     * @param callback 处理回调
     */
    public static void loadBubble(final Context context, final int groupId, final int id, final LoadCallback callback) {
        String path = null;
        List<FileDownloaderModel> modelsTemp = DownloaderManager.getInstance().getDbController().getResourceByType(EffectService.EFFECT_CAPTION);
        for (FileDownloaderModel fileDownloaderModel : modelsTemp) {
            if (fileDownloaderModel.getId() == groupId && fileDownloaderModel.getSubid() == id && new File(fileDownloaderModel.getPath()).exists()) {
                path = fileDownloaderModel.getPath();
                break;
            }
        }
        if(!StringUtils.isEmpty(path)){
            callback.onSuccess(path);
        } else {
            new CaptionLoader().loadAllCaption(context, new CaptionLoader.LoadCallback() {
                @Override
                public void onLoadCompleted(final List<ResourceForm> localInfos, final List<ResourceForm> remoteInfos, final Throwable e) {
                    if (remoteInfos != null) {
                        for (final ResourceForm caption : remoteInfos) {
                            if (caption.getId() == groupId) {
                                new EffectService().getCaptionListById(context.getPackageName(), caption.getId(), new HttpCallback<List<PasterForm>>(){

                                    @Override
                                    public void onSuccess(final List<PasterForm> materials) {
                                        if (materials != null) {
                                            for (PasterForm material : materials) {
                                                if (material.getId() == id) {
                                                    FileDownloaderModel model = new FileDownloaderModel();
                                                    model.setEffectType(EffectService.EFFECT_CAPTION);
                                                    model.setName(caption.getName());
                                                    model.setNameEn(caption.getNameEn());
                                                    model.setDescription(caption.getDescription());
                                                    model.setDescriptionEn(caption.getDescriptionEn());
                                                    model.setIcon(caption.getIcon());
                                                    model.setIsnew(caption.getIsNew());
                                                    model.setId(caption.getId());
                                                    model.setLevel(caption.getLevel());
                                                    model.setPreview(caption.getPreviewUrl());
                                                    model.setSort(caption.getSort());
                                                    model.setSubname(material.getName());
                                                    model.setSubicon(material.getIcon());
                                                    model.setSubid(material.getId());
                                                    model.setUrl(material.getDownloadUrl());
                                                    model.setMd5(material.getMD5());
                                                    model.setSubpreview(material.getPreviewUrl());
                                                    model.setSubsort(material.getSort());
                                                    model.setSubtype(material.getType());
                                                    model.setFontid(material.getFontId());
                                                    model.setIsunzip(1);
                                                    final FileDownloaderModel task = DownloaderManager.getInstance().addTask(model, model.getUrl());
                                                    TasksManager tasksManager = new TasksManager();
                                                    tasksManager.addTask(task.getTaskId(), new FileDownloaderCallback(){
                                                        @Override
                                                        public void onFinish(final int downloadId, final String path) {
                                                            //在数据库中删除记录，不然会导致整组动画不能再下载
                                                            DownloaderManager.getInstance().getDbController().deleteTask(task.getTaskId());
                                                            callback.onSuccess(path);
                                                        }

                                                        @Override
                                                        public void onError(final BaseDownloadTask task, final Throwable e) {
                                                            callback.onFailure(TYPE_BUBBLE, e.getMessage());
                                                        }
                                                    });
                                                    tasksManager.startTask();
                                                    return;
                                                }
                                            }
                                        }
                                        callback.onFailure(TYPE_BUBBLE, "Not found");
                                    }

                                    @Override
                                    public void onFailure(final Throwable e) {
                                        callback.onFailure(TYPE_BUBBLE, e.getMessage());
                                    }
                                });
                                return;
                            }
                        }
                    }
                    callback.onFailure(TYPE_BUBBLE, "Not found");
                }
            });
        }
    }

    /**
     * 加载字体资源
     * @param context 上下文
     * @param id 字体ID
     * @param callback 处理回调
     */
    public static void loadFont(final Context context, final int id, final LoadCallback callback) {
        String path = null;
        List<FileDownloaderModel> modelsTemp = DownloaderManager.getInstance().getDbController().getResourceByType(EffectService.EFFECT_TEXT);
        for (FileDownloaderModel fileDownloaderModel : modelsTemp) {
            if (fileDownloaderModel.getId() == id && new File(fileDownloaderModel.getPath()).exists()) {
                path = fileDownloaderModel.getPath() + File.separator + "font.ttf";
                break;
            }
        }
        if(!StringUtils.isEmpty(path)){
            callback.onSuccess(path);
        } else {
            new EffectService().getFontById(context.getPackageName(), id, new HttpCallback<FontForm>() {

                @Override
                public void onSuccess(final FontForm fontForm) {
                    if (fontForm != null) {
                        FileDownloaderModel model = new FileDownloaderModel();
                        model.setEffectType(EffectService.EFFECT_TEXT);
                        model.setName(fontForm.getName());
                        model.setIcon(fontForm.getIcon());
                        model.setId(fontForm.getId());
                        model.setLevel(fontForm.getLevel());
                        model.setSort(fontForm.getSort());
                        model.setUrl(fontForm.getUrl());
                        model.setMd5(fontForm.getMd5());
                        model.setBanner(fontForm.getBanner());
                        model.setIsunzip(1);
                        FileDownloaderModel task = DownloaderManager.getInstance().addTask(model, model.getUrl());
                        TasksManager tasksManager = new TasksManager();
                        tasksManager.addTask(task.getTaskId(), new FileDownloaderCallback(){
                            @Override
                            public void onFinish(final int downloadId, final String path) {
                                callback.onSuccess(path + File.separator + "font.ttf");
                            }

                            @Override
                            public void onError(final BaseDownloadTask task, final Throwable e) {
                                callback.onFailure(TYPE_FONT, e.getMessage());
                            }
                        });
                        tasksManager.startTask();
                    } else {
                        callback.onFailure(TYPE_FONT, "Not found");
                    }
                }

                @Override
                public void onFailure(final Throwable e) {
                    callback.onFailure(TYPE_FONT, e.getMessage());
                }
            });
        }
    }

    /**
     * 加载MV资源
     * @param context 上下文
     * @param groupId MV资源包ID
     * @param id MV ID
     * @param callback 处理回调
     */
    public static void loadMv(final Context context, final int groupId, final String id, final LoadCallback callback) {
        String path = null;
        List<FileDownloaderModel> modelsTemp = DownloaderManager.getInstance().getDbController().getResourceByType(EffectService.EFFECT_MV);
        for (FileDownloaderModel fileDownloaderModel : modelsTemp) {
            if (fileDownloaderModel.getId() == groupId && new File(fileDownloaderModel.getPath(), id).exists()) {
                path = fileDownloaderModel.getPath() + File.separator + id;
                break;
            }
        }
        if(!StringUtils.isEmpty(path)){
            callback.onSuccess(path);
        } else {
            new EffectLoader().loadAllMV(context,new EffectLoader.LoadCallback<IMVForm>(){
                @Override
                public void onLoadCompleted(final List<IMVForm> localInfos, final List<IMVForm> remoteInfos, final Throwable e) {
                    if (remoteInfos != null) {
                        for (final IMVForm mv : remoteInfos) {
                            if (groupId == mv.getId()) {
                                TasksManager tasksManager = new TasksManager();
                                List<AspectForm> aspects = mv.getAspectList();
                                if (aspects != null) {
                                    //只有最后一个执行完的任务才回调
                                    FileDownloaderCallback fileDownloaderCallback = new FileDownloaderCallback() {
                                        @Override
                                        public void onFinish(final int downloadId, final String path) {
                                            callback.onSuccess(new File(new File(path).getParentFile(), id)
                                                                 .getAbsolutePath());
                                        }

                                        @Override
                                        public void onError(final BaseDownloadTask task, final Throwable e) {
                                            callback.onFailure(TYPE_MV, e.getMessage());
                                        }
                                    };
                                    for (final AspectForm aspect : aspects) {
                                        FileDownloaderModel model = new FileDownloaderModel();
                                        model.setEffectType(EffectService.EFFECT_MV);
                                        model.setTag(mv.getTag());
                                        model.setKey(mv.getKey());
                                        model.setName(mv.getName());
                                        model.setNameEn(mv.getNameEn());
                                        model.setId(mv.getId());
                                        model.setCat(mv.getCat());
                                        model.setLevel(mv.getLevel());
                                        model.setPreviewpic(mv.getPreviewPic());
                                        model.setIcon(mv.getIcon());
                                        model.setPreviewmp4(mv.getPreviewMp4());
                                        model.setSort(mv.getSort());
                                        model.setSubtype(mv.getType());
                                        model.setMd5(aspect.getMd5());
                                        model.setDownload(aspect.getDownload());
                                        model.setUrl(aspect.getDownload());
                                        model.setAspect(aspect.getAspect());
                                        model.setDuration(mv.getDuration());
                                        model.setIsunzip(1);
                                        final FileDownloaderModel task = DownloaderManager.getInstance().addTask(model, model.getDownload());
                                        tasksManager.addTask(task.getTaskId(), fileDownloaderCallback);
                                    }
                                    tasksManager.startTask();
                                    return;
                                }
                            }
                        }
                    }
                    callback.onFailure(TYPE_MV, "Not found");
                }
            });
        }
    }

}
