/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.recorder.view.effects.manager;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import com.aliyun.common.utils.CommonUtil;
import com.aliyun.common.utils.ToastUtil;
import com.aliyun.demo.R;
import com.aliyun.demo.recorder.util.FixedToastUtils;
import com.aliyun.demo.recorder.view.effects.http.EffectService;
import com.aliyun.demo.recorder.view.effects.http.HttpCallback;
import com.aliyun.demo.recorder.view.effects.mv.MVDownloadListener;
import com.aliyun.downloader.DownloaderManager;
import com.aliyun.downloader.FileDownloaderCallback;
import com.aliyun.downloader.FileDownloaderModel;
import com.aliyun.downloader.zipprocessor.DownloadFileUtils;
import com.aliyun.svideo.sdk.external.struct.form.AspectForm;
import com.aliyun.svideo.sdk.external.struct.form.IMVForm;
import com.aliyun.svideo.sdk.external.struct.form.PreviewPasterForm;
import com.liulishuo.filedownloader.BaseDownloadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EffectLoader {

    public static final int ASPECT_1_1 = 1;
    public static final int ASPECT_4_3 = 2;
    public static final int ASPECT_9_16 = 3;
    public EffectService mService = new EffectService();
    private String mPackageName;
    private Context mContext;
    private ArrayList<IMVForm> mLoadingMv;
    private ArrayList<PreviewPasterForm> mLoadingPaster;
    public interface LoadCallback<T> {
        void onLoadCompleted(List<T> localInfos, List<T> remoteInfos, Throwable e);
    }

    public EffectLoader(Context mContext) {
        this.mContext = mContext;
        mPackageName = mContext.getApplicationInfo().packageName;
        mLoadingMv = new ArrayList<>();
        mLoadingPaster = new ArrayList<>();
    }
    public List<FileDownloaderModel> loadLocalEffect(int effectType) {
        List<FileDownloaderModel> localPasters = new ArrayList<FileDownloaderModel>();
        List<String> selectedColumns = new ArrayList<String>();
        selectedColumns.add(FileDownloaderModel.ICON);
        selectedColumns.add(FileDownloaderModel.DESCRIPTION);
        selectedColumns.add(FileDownloaderModel.ID);
        selectedColumns.add(FileDownloaderModel.ISNEW);
        selectedColumns.add(FileDownloaderModel.LEVEL);
        selectedColumns.add(FileDownloaderModel.NAME);
        selectedColumns.add(FileDownloaderModel.PREVIEW);
        selectedColumns.add(FileDownloaderModel.SORT);

        selectedColumns.add(FileDownloaderModel.PREVIEWMP4);
        selectedColumns.add(FileDownloaderModel.PREVIEWPIC);
        selectedColumns.add(FileDownloaderModel.KEY);
        selectedColumns.add(FileDownloaderModel.DURATION);
        //selectedColumns.add(FileDownloaderModel.SUBTYPE);
        HashMap<String, String> conditionMap = new HashMap<String, String>();
        conditionMap.put(FileDownloaderModel.EFFECTTYPE, String.valueOf(effectType));
        Cursor cursor = DownloaderManager.getInstance()
                .getDbController().getResourceColums(conditionMap, selectedColumns);

        while (cursor.moveToNext()) {
            FileDownloaderModel paster = new FileDownloaderModel();
            paster.setIcon(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.ICON)));
            paster.setDescription(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.DESCRIPTION)));
            paster.setId(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.ID)));
            paster.setIsnew(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.ISNEW)));
            paster.setLevel(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.LEVEL)));
            paster.setName(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.NAME)));
            paster.setPreview(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.PREVIEW)));
            paster.setSort(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.SORT)));

            paster.setPreviewmp4(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.PREVIEWMP4)));
            paster.setPreviewpic(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.PREVIEWPIC)));
            paster.setKey(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.KEY)));
            paster.setDuration(cursor.getLong(cursor.getColumnIndex(FileDownloaderModel.DURATION)));
            //paster.setSubtype(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.SUBTYPE)));

            localPasters.add(paster);
        }
        cursor.close();
        return localPasters;
    }

    public int loadAllPaster(String signature, final LoadCallback<PreviewPasterForm> callback) {

        mService.loadEffectPaster(signature
                , mPackageName, new HttpCallback<List<PreviewPasterForm>>() {
                    @Override
                    public void onSuccess(List<PreviewPasterForm> result) {
                        if (callback != null) {
                            List<PreviewPasterForm> localPasters = loadLocalPaster();
                            List<Integer> localIds = null;
                            if (localPasters != null && localPasters.size() > 0) {
                                localIds = new ArrayList<Integer>(localPasters.size());
                                for (PreviewPasterForm paster : localPasters) {
                                    localIds.add(paster.getId());
                                }
                            }
                            if(localIds != null && localIds.size() > 0) {
                                for (int i = 0; i < result.size(); i++) {
                                    if (localIds.contains(result.get(i).getId())) {
                                        result.remove(i);
                                        i--;
                                    }
                                }
                            }

                            callback.onLoadCompleted(localPasters, result, null);
                        }
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        List<PreviewPasterForm> localPasters = loadLocalPaster();
                        if (callback != null) {
                            callback.onLoadCompleted(localPasters, null, e);
                        }
                    }
                });
        return 0;
    }

    public List<PreviewPasterForm> loadLocalPaster() {
        List<PreviewPasterForm> localPasters = new ArrayList<PreviewPasterForm>();
        List<String> selectedColumns = new ArrayList<String>();
        selectedColumns.add(FileDownloaderModel.ICON);
        selectedColumns.add(FileDownloaderModel.DESCRIPTION);
        selectedColumns.add(FileDownloaderModel.ID);
        selectedColumns.add(FileDownloaderModel.ISNEW);
        selectedColumns.add(FileDownloaderModel.LEVEL);
        selectedColumns.add(FileDownloaderModel.NAME);
        selectedColumns.add(FileDownloaderModel.PREVIEW);
        selectedColumns.add(FileDownloaderModel.PATH);
        selectedColumns.add(FileDownloaderModel.SORT);
        HashMap<String, String> conditionMap = new HashMap<String, String>();
        conditionMap.put(FileDownloaderModel.EFFECTTYPE, String.valueOf(EffectService.EFFECT_FACE_PASTER));
        Cursor cursor = DownloaderManager.getInstance()
                .getDbController().getResourceColums(conditionMap, selectedColumns);

        while (cursor.moveToNext()) {
            PreviewPasterForm paster = new PreviewPasterForm();
            paster.setIcon(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.ICON)));
            paster.setId(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.ID)));
            paster.setLevel(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.LEVEL)));
            paster.setName(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.NAME)));
            paster.setSort(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.SORT)));
            paster.setPath(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.PATH)));

            //pasterid = 150 是本地asset打入的id号.目前需求认为本地打入的动图资源不应该显示再更多动图列表中
            //if (paster.getId() != 150){
            //    localPasters.add(paster);
            //}
            localPasters.add(paster);
        }
        cursor.close();
        return localPasters;
    }

    /**
     * 加载所有的mv，先网络请求mv列表，再加载本地缓存，并通过id进行匹配
     * @param signature
     * @param callback
     * @return
     */
    public int loadAllMV(String signature, final LoadCallback<IMVForm> callback) {

        mService.loadEffectMv(signature
                , mPackageName, new HttpCallback<List<IMVForm>>() {
                    @Override
                    public void onSuccess(List<IMVForm> result) {
                        if (callback != null) {

                            List<IMVForm> localMvs = loadLocalMV();
                            List<Integer> localIds = null;
                            if (localMvs != null && localMvs.size() > 0) {
                                localIds = new ArrayList<Integer>(localMvs.size());
                                for (IMVForm mv : localMvs) {
                                    localIds.add(mv.getId());
                                }
                            }
                            if(localIds != null && localIds.size() > 0) {
                                for (int i = 0; i < result.size(); i++) {
                                    if (localIds.contains(result.get(i).getId())) {
                                        result.remove(i);
                                        i--;
                                    }
                                }
                            }

                            callback.onLoadCompleted(localMvs, result, null);
                        }
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        List<IMVForm> localMvs = loadLocalMV();
                        if (callback != null) {
                            callback.onLoadCompleted(localMvs, null, e);
                        }
                    }
                });
        return 0;
    }


    private int downloadSize;
    /**
     * 下载mv
     * @param mvData
     * @param mvDownloadListener
     */
    public void downloadMV(final IMVForm mvData, final MVDownloadListener mvDownloadListener){
        if (!CommonUtil.hasNetwork((mContext))) {
            ToastUtil.showToast(mContext, R.string.aliyun_network_not_connect);
            return;
        }
        if (CommonUtil.SDFreeSize() < 10 * 1000 * 1000) {
            Toast.makeText(mContext, R.string.aliyun_no_free_memory, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mLoadingMv.contains(mvData)) {//如果已经在下载中了，则不能重复下载
            return;
        }

        final List<AspectForm> aspects = mvData.getAspectList();
        final List<FileDownloaderModel> tasks = new ArrayList<>();
        final int size = aspects.size();
        downloadSize += size;
        if (downloadSize >= 50) {
            downloadSize-=size;
            FixedToastUtils.show(mContext, "当前下载任务过多, 请稍后~~!");
            return;
        }
        mLoadingMv.add(mvData);

        TasksManager tasksManager = new TasksManager();
        if (aspects != null) {
            FileDownloaderModel model;
            List<FileDownloaderModel> list = new ArrayList<>();
            for (final AspectForm aspect : aspects) {
                model = new FileDownloaderModel();
                model.setEffectType(EffectService.EFFECT_MV);
                model.setTag(mvData.getTag());
                model.setKey(mvData.getKey());
                model.setName(mvData.getName());
                model.setId(mvData.getId());
                model.setCat(mvData.getCat());
                model.setLevel(mvData.getLevel());
                model.setPreviewpic(mvData.getPreviewPic());
                model.setIcon(mvData.getIcon());
                model.setPreviewmp4(mvData.getPreviewMp4());
                model.setSort(mvData.getSort());
                model.setSubtype(mvData.getType());
                model.setMd5(aspect.getMd5());
                model.setDownload(aspect.getDownload());
                model.setUrl(aspect.getDownload());
                model.setAspect(aspect.getAspect());
                model.setDuration(mvData.getDuration());
                model.setIsunzip(1);
                final FileDownloaderModel task = DownloaderManager.getInstance().addTask(model, model.getDownload());
                tasksManager.addTask(task.getTaskId(), new MVDownloadListener() {
                    @Override
                    public void onStart(int downloadId, long soFarBytes, long totalBytes, int preProgress) {
                        super.onStart(downloadId, soFarBytes, totalBytes, preProgress);
                        if (mvDownloadListener!=null){
                            mvDownloadListener.onStart(downloadId, soFarBytes, totalBytes, preProgress);
                        }
                    }
                    @Override
                    public void onFinish(int downloadId, String path,boolean allFinish) {
                        aspect.setPath(path);
                        downloadSize -= size;
                        if (allFinish){
                            mLoadingMv.remove(mvData);
                        }
                        if (mvDownloadListener!=null) {
                            mvDownloadListener.onFinish(downloadId, path, allFinish);
                        }

                    }

                    @Override
                    public void onProgress(int downloadId, long soFarBytes, long totalBytes, long speed, int progress) {
                        super.onProgress(downloadId, soFarBytes, totalBytes, speed, progress);
                        if (mvDownloadListener!=null) {
                            mvDownloadListener.onProgress(downloadId, soFarBytes, totalBytes, speed, progress);
                        }
                    }

                    @Override
                    public void onError(BaseDownloadTask task1, Throwable e) {
                        super.onError(task1, e);
                        mLoadingMv.remove(mvData);
                        if (mvDownloadListener!=null){
                            mvDownloadListener.onError(task1, e);
                        }
                        ToastUtil.showToast(mContext, R.string.aliyun_download_failed);
                        synchronized (tasks) {
                            for (FileDownloaderModel t : tasks) {
                                //删除该套MV的所有Task
                                DownloaderManager.getInstance().deleteTaskByTaskId(t.getTaskId());
                            }
                            tasks.clear();
                        }
                        //清空已插入到数据库中的该套MV的信息
                        DownloaderManager.getInstance().getDbController().deleteTaskById(mvData.getId());
                    }
                });
            }
            tasksManager.startTask();
        }

    }

    /**
     * 下载动图
     * @param pasterForm
     * @param callback
     */
    public void downloadPaster(final PreviewPasterForm pasterForm, final FileDownloaderCallback callback){
        if (mLoadingPaster.contains(pasterForm)) {//如果已经在下载中了，则不能重复下载
            return;
        }
        mLoadingPaster.add(pasterForm);
        FileDownloaderModel fileDownloaderModel = new FileDownloaderModel();
        fileDownloaderModel.setUrl(pasterForm.getUrl());
        fileDownloaderModel.setEffectType(EffectService.EFFECT_FACE_PASTER);
        fileDownloaderModel.setPath(DownloadFileUtils.getAssetPackageDir(mContext,
            pasterForm.getName(), pasterForm.getId()).getAbsolutePath());
        fileDownloaderModel.setId(pasterForm.getId());
        fileDownloaderModel.setIsunzip(1);
        fileDownloaderModel.setName(pasterForm.getName());
        fileDownloaderModel.setIcon(pasterForm.getIcon());
        final FileDownloaderModel model = DownloaderManager.getInstance().addTask(fileDownloaderModel, fileDownloaderModel.getUrl());
        if (DownloaderManager.getInstance().isDownloading(model.getTaskId(), model.getPath())) {
            return;
        }
        DownloaderManager.getInstance().startTask(model.getTaskId(),new FileDownloaderCallback(){
            @Override
            public void onStart(int downloadId, long soFarBytes, long totalBytes, int preProgress) {
                if (callback!=null){
                    callback.onStart(downloadId, soFarBytes, totalBytes, preProgress);
                }

            }

            @Override
            public void onProgress(int downloadId, long soFarBytes, long totalBytes, long speed, int progress) {
                if (callback!=null){
                    callback.onProgress(downloadId, soFarBytes, totalBytes, speed, progress);
                }
            }

            @Override
            public void onFinish(int downloadId, String path) {
                mLoadingPaster.remove(pasterForm);
                if (callback!=null){
                    callback.onFinish(downloadId, path);
                }
            }

            @Override
            public void onError(BaseDownloadTask task, Throwable e) {
                mLoadingPaster.remove(pasterForm);
                super.onError(task, e);
                ToastUtil.showToast(mContext, R.string.aliyun_download_failed);
                DownloaderManager.getInstance().deleteTaskByTaskId(model.getTaskId());
                DownloaderManager.getInstance().getDbController().deleteTaskById(pasterForm.getId());
                if (callback!=null){
                    callback.onError(task,e );
                }
            }
        });

    }
    /**
     * 加载已经下载的mv效果
     * @return
     */
    public List<IMVForm> loadLocalMV() {

        List<FileDownloaderModel> modelsTemp = DownloaderManager.getInstance().getDbController().getResourceByType(EffectService.EFFECT_MV);
        ArrayList<IMVForm> resourceForms = new ArrayList<>();
        ArrayList<Integer> ids = new ArrayList<>();
        List<FileDownloaderModel> models = new ArrayList<>();
        if(modelsTemp != null && modelsTemp.size() > 0){
            for (FileDownloaderModel model : modelsTemp) {
                if (new File(model.getPath()).exists()) {
                    models.add(model);
                }
            }
            for(FileDownloaderModel model :models){
                IMVForm form = null;
                if(!ids.contains(model.getId())){
                    ids.add(model.getId());
                    form = new IMVForm();
                    form.setId(model.getId());
                    form.setName(model.getName());
                    form.setKey(model.getKey());
                    form.setLevel(model.getLevel());
                    form.setTag(model.getTag());
                    form.setCat(model.getCat());
                    form.setIcon(model.getIcon());
                    form.setPreviewPic(model.getPreviewpic());
                    form.setPreviewMp4(model.getPreviewmp4());
                    form.setDuration(model.getDuration());
                    form.setType(model.getSubtype());
                    form.setAspectList(new ArrayList<AspectForm>());
                    resourceForms.add(form);
                }else {
                    for (IMVForm imvForm:resourceForms){
                        if (imvForm.getId()==model.getId()){
                            form = imvForm;
                        }
                    }
                }
                AspectForm pasterForm = addAspectForm(model);
                form.getAspectList().add(pasterForm);
            }
        }
        return resourceForms;
    }

    private AspectForm addAspectForm(FileDownloaderModel model) {
        AspectForm aspectForm = new AspectForm();
        aspectForm.setAspect(model.getAspect());
        aspectForm.setDownload(model.getDownload());
        aspectForm.setMd5(model.getMd5());
        aspectForm.setPath(model.getPath());
        return aspectForm;
    }

}
