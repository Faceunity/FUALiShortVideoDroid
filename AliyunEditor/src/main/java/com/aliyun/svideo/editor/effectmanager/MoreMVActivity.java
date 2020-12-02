/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effectmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.aliyun.common.utils.CommonUtil;
import com.aliyun.common.utils.ToastUtil;
import com.aliyun.svideo.downloader.DownloaderManager;
import com.aliyun.svideo.downloader.FileDownloaderCallback;
import com.aliyun.svideo.downloader.FileDownloaderModel;
import com.aliyun.svideo.base.Form.AspectForm;
import com.aliyun.svideo.base.Form.IMVForm;
import com.aliyun.svideo.base.http.EffectService;
import com.aliyun.svideo.editor.R;
import com.liulishuo.filedownloader.BaseDownloadTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * mv更多界面
 */
public class MoreMVActivity extends AbstractActionBarActivity implements
    MoreMVAdapter.OnItemRightButtonClickListener, View.OnClickListener {
    private static final String TAG = MoreMVActivity.class.getName();
    public static final String SELECTD_ID = "selected_id";
    private RecyclerView mRvMoreMV;
    private MoreMVAdapter mAdapter;
    private EffectLoader mMVLoader = new EffectLoader();
    private List<IMVForm> mLoadingMv = null;
    private AsyncTask<Void, Void, List<EffectBody<IMVForm>>> loadResourceTask;
    private int selectId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        setContentView(R.layout.alivc_editor_activity_more_effect);
        setActionBarLeftText(getString(R.string.alivc_common_cancel));
        setActionBarLeftViewVisibility(View.VISIBLE);
        setActionBarTitle(getString(R.string.alivc_editor_more_title_mv));
        setActionBarTitleVisibility(View.VISIBLE);
        setActionBarRightView(R.mipmap.aliyun_svideo_icon_edit);
        setActionBarRightViewVisibility(View.VISIBLE);
        setActionBarLeftClickListener(this);
        setActionBarRightClickListener(this);
        mRvMoreMV = (RecyclerView)findViewById(R.id.rv_more_paster);

        mAdapter = new MoreMVAdapter(this);
        mRvMoreMV.setAdapter(mAdapter);
        mRvMoreMV.setLayoutManager(new LinearLayoutManager(this,
                                   LinearLayoutManager.VERTICAL,
                                   false));
        mAdapter.setRightBtnClickListener(this);
    }

    private void initData() {
        selectId = getIntent().getIntExtra(SELECTD_ID, 0);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mMVLoader.loadAllMV(MoreMVActivity.this, new EffectLoader.LoadCallback<IMVForm>() {
            @Override
            public void onLoadCompleted(List<IMVForm> localInfos,
                                        List<IMVForm> remoteInfos,
                                        Throwable e) {
                int localSize = localInfos == null ? 0 : localInfos.size();
                int remoteSize = remoteInfos == null ? 0 : remoteInfos.size();
                Log.d("moreMv", "localSize : " + localSize + " ,remoteSize : " + remoteSize);
                List<EffectBody<IMVForm>> remoteData = new ArrayList<>();
                List<EffectBody<IMVForm>> localData = new ArrayList<>();
                if (localInfos != null) {
                    EffectBody<IMVForm> body;
                    for (IMVForm form : localInfos) {
                        body = new EffectBody<IMVForm>(form, true);
                        localData.add(body);
                    }
                }
                if (remoteInfos != null) {
                    EffectBody<IMVForm> body;
                    for (IMVForm mv : remoteInfos) {
                        body = new EffectBody<IMVForm>(mv, false);
                        remoteData.add(body);
                    }
                }
                remoteData.addAll(localData);
                mAdapter.syncData(remoteData);
                mLoadingMv = new ArrayList<IMVForm>(remoteData.size());
            }
        });
    }

    @Override
    public void onRemoteItemClick(final int position, final EffectBody<IMVForm> data) {
        if (!CommonUtil.hasNetwork(this)) {
            ToastUtil.showToast(this, R.string.alivc_editor_more_no_network);
            return;
        }

        if (CommonUtil.SDFreeSize() < 10 * 1000 * 1000) {
            Toast.makeText(this, R.string.alivc_common_no_free_memory, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mLoadingMv.contains(data.getData())) {//如果已经在下载中了，则不能重复下载
            return;
        }
        mLoadingMv.add(data.getData());
        //下载
        IMVForm mv = data.getData();
        final List<AspectForm> aspects = mv.getAspectList();
        final List<FileDownloaderModel> tasks = new ArrayList<>();

        TasksManager tasksManager = new TasksManager();
        if (aspects != null) {
            FileDownloaderModel model;
            for (final AspectForm aspect : aspects) {
                model = new FileDownloaderModel();
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
                tasksManager.addTask(task.getTaskId(), new FileDownLoadCallBack(MoreMVActivity.this, data, position, tasks));
            }
            tasksManager.startTask();
        }
    }

    @Override
    public void onLocalItemClick(int position, EffectBody<IMVForm> data) {
        //使用该MV
        Intent intent = new Intent();
        intent.putExtra(SELECTD_ID, data.getData().getId());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == getActionBarLeftViewID()) {
            onBackPressed();
        } else if (v.getId() == getActionBarRightViewID()) {
            Intent intent = new Intent(MoreMVActivity.this, EffectManagerActivity.class);
            intent.putExtra(EffectManagerActivity.KEY_TAB, EffectManagerActivity.MV);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(SELECTD_ID, selectId);
        setResult(Activity.RESULT_CANCELED, intent);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 需求要求关闭页面时, 未下载完成的资源继续在后台下载, 除非改需求, 否则不要做停止下载的操作


        if (loadResourceTask != null) {
            loadResourceTask.cancel(true);
            loadResourceTask = null;
        }
    }

    /**
     * 文件下载回调
     */
    private static class FileDownLoadCallBack extends FileDownloaderCallback {

        private WeakReference<MoreMVActivity> weakReference;
        private EffectBody<IMVForm> data;
        private int position;
        private List<FileDownloaderModel> tasks;

        public FileDownLoadCallBack(MoreMVActivity moreMVActivity, EffectBody<IMVForm> data,
                                    int position, List<FileDownloaderModel> tasks) {
            weakReference = new WeakReference<>(moreMVActivity);
            this.data = data;
            this.position = position;
            this.tasks = tasks;
        }

        @Override
        public void onStart(int downloadId, long soFarBytes, long totalBytes, int preProgress) {
            super.onStart(downloadId, soFarBytes, totalBytes, preProgress);
            MoreMVActivity moreMVActivity = weakReference.get();
            if (moreMVActivity != null) {
                moreMVActivity.mAdapter.notifyDownloadingStart(data);
            }
        }

        @Override
        public void onFinish(int downloadId, String path) {
            super.onFinish(downloadId, path);
            MoreMVActivity moreMVActivity = weakReference.get();
            if (moreMVActivity != null) {
                moreMVActivity.mLoadingMv.remove(data.getData());
                Log.d(TAG, "下载完成");
                moreMVActivity.mAdapter.notifyDownloadingComplete(data, position);
            }
        }

        @Override
        public void onProgress(int downloadId, long soFarBytes, long totalBytes, long speed, int progress) {
            super.onProgress(downloadId, soFarBytes, totalBytes, speed, progress);
            Log.d(TAG, "当前下载了" + (soFarBytes * 1.0f / totalBytes));
            MoreMVActivity moreMVActivity = weakReference.get();
            if (moreMVActivity != null) {
                moreMVActivity.mAdapter.updateProcess(
                    (MoreMVAdapter.ImvViewHolder)moreMVActivity.mRvMoreMV.findViewHolderForAdapterPosition(position), progress,
                    position);
            }
        }

        @Override
        public void onError(BaseDownloadTask task, Throwable e) {
            super.onError(task, e);
            MoreMVActivity moreMVActivity = weakReference.get();
            if (moreMVActivity != null) {
                moreMVActivity.mAdapter.notifyDownloadingFailure(data);
                moreMVActivity.mLoadingMv.remove(data.getData());
                ToastUtil.showToast(moreMVActivity, e.getMessage());
                synchronized (tasks) {
                    for (FileDownloaderModel t : tasks) {//删除该套MV的所有Task
                        DownloaderManager.getInstance().deleteTaskByTaskId(t.getTaskId());
                    }
                    tasks.clear();
                }
                //清空已插入到数据库中的该套MV的信息
                DownloaderManager.getInstance().getDbController().deleteTaskById(data.getData().getId());
            }
        }
    }
}
