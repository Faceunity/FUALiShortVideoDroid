/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effectmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.common.utils.CommonUtil;
import com.aliyun.common.utils.ToastUtil;
import com.aliyun.svideo.base.Form.AnimationEffectForm;
import com.aliyun.svideo.base.http.EffectService;
import com.aliyun.svideo.downloader.DownloaderManager;
import com.aliyun.svideo.downloader.FileDownloaderCallback;
import com.aliyun.svideo.downloader.FileDownloaderModel;
import com.aliyun.svideo.editor.R;
import com.liulishuo.filedownloader.BaseDownloadTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 滤镜特效更多界面
 */
public class MoreAnimationEffectActivity extends AbstractActionBarActivity implements
    MoreAnimationEffectAdapter.OnItemRightButtonClickListener, View.OnClickListener {
    private static final String TAG = MoreAnimationEffectActivity.class.getName();
    public static final String SELECTD_ID = "selected_id";
    private RecyclerView mRvMoreAnimationFilter;
    private MoreAnimationEffectAdapter mAdapter;
    private EffectLoader mAnimFilterLoader = new EffectLoader();
    private List<AnimationEffectForm> afFormList = null;
    private AsyncTask<Void, Void, List<EffectBody<AnimationEffectForm>>> loadResourceTask;
    private int selectId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        setContentView(R.layout.alivc_editor_activity_more_effect);
        setActionBarLeftText(getString(R.string.alivc_common_cancel));
        setActionBarLeftViewVisibility(View.VISIBLE);
        setActionBarTitle(getString(R.string.alivc_editor_more_title_animation_effect));
        setActionBarTitleVisibility(View.VISIBLE);
        setActionBarRightView(R.mipmap.aliyun_svideo_icon_edit);
        setActionBarRightViewVisibility(View.VISIBLE);
        setActionBarLeftClickListener(this);
        setActionBarRightClickListener(this);
        mRvMoreAnimationFilter = (RecyclerView)findViewById(R.id.rv_more_paster);

        mAdapter = new MoreAnimationEffectAdapter(this);
        mRvMoreAnimationFilter.setAdapter(mAdapter);
        mRvMoreAnimationFilter.setLayoutManager(new LinearLayoutManager(this,
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

        mAnimFilterLoader.loadAllAnimationFilter(MoreAnimationEffectActivity.this, new EffectLoader.LoadCallback<AnimationEffectForm>() {
            @Override
            public void onLoadCompleted(List<AnimationEffectForm> localInfos,
                                        List<AnimationEffectForm> remoteInfos,
                                        Throwable e) {
                int localSize = localInfos == null ? 0 : localInfos.size();
                int remoteSize = remoteInfos == null ? 0 : remoteInfos.size();
                Log.d("moreMv", "localSize : " + localSize + " ,remoteSize : " + remoteSize);
                List<EffectBody<AnimationEffectForm>> remoteData = new ArrayList<>();
                List<EffectBody<AnimationEffectForm>> localData = new ArrayList<>();
                if (localInfos != null) {
                    EffectBody<AnimationEffectForm> body;
                    for (AnimationEffectForm form : localInfos) {
                        body = new EffectBody<AnimationEffectForm>(form, true);
                        localData.add(body);
                    }
                }
                if (remoteInfos != null) {
                    EffectBody<AnimationEffectForm> body;
                    for (AnimationEffectForm mv : remoteInfos) {
                        body = new EffectBody<AnimationEffectForm>(mv, false);
                        remoteData.add(body);
                    }
                }
                remoteData.addAll(localData);
                mAdapter.syncData(remoteData);
                afFormList = new ArrayList<AnimationEffectForm>(remoteData.size());
            }
        });
    }

    @Override
    public void onRemoteItemClick(final int position, final EffectBody<AnimationEffectForm> data) {
        if (!CommonUtil.hasNetwork(this)) {
            ToastUtil.showToast(this, R.string.alivc_editor_more_no_network);
            return;
        }

        if (CommonUtil.SDFreeSize() < 10 * 1000 * 1000) {
            Toast.makeText(this, R.string.alivc_common_no_free_memory, Toast.LENGTH_SHORT).show();
            return;
        }
        if (afFormList.contains(data.getData())) {//如果已经在下载中了，则不能重复下载
            return;
        }
        afFormList.add(data.getData());
        //下载
        AnimationEffectForm af = data.getData();
        final List<FileDownloaderModel> tasks = new ArrayList<>();

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
        tasksManager.addTask(task.getTaskId(), new FileDownLoadCallBack(MoreAnimationEffectActivity.this, data, position, tasks));

        tasksManager.startTask();

    }

    @Override
    public void onLocalItemClick(int position, EffectBody<AnimationEffectForm> data) {
        //使用该特效
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
            Intent intent = new Intent(MoreAnimationEffectActivity.this, EffectManagerActivity.class);
            intent.putExtra(EffectManagerActivity.KEY_TAB, EffectManagerActivity.ANIM_EFFECT);
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

        private WeakReference<MoreAnimationEffectActivity> weakReference;
        private EffectBody<AnimationEffectForm> data;
        private int position;
        private List<FileDownloaderModel> tasks;

        public FileDownLoadCallBack(MoreAnimationEffectActivity activity, EffectBody<AnimationEffectForm> data,
                                    int position, List<FileDownloaderModel> tasks) {
            weakReference = new WeakReference<>(activity);
            this.data = data;
            this.position = position;
            this.tasks = tasks;
        }

        @Override
        public void onStart(int downloadId, long soFarBytes, long totalBytes, int preProgress) {
            super.onStart(downloadId, soFarBytes, totalBytes, preProgress);
            MoreAnimationEffectActivity activity = weakReference.get();
            if (activity != null) {
                activity.mAdapter.notifyDownloadingStart(data);
            }
        }

        @Override
        public void onFinish(int downloadId, String path) {
            super.onFinish(downloadId, path);
            MoreAnimationEffectActivity activity = weakReference.get();
            if (activity != null) {
                activity.afFormList.remove(data.getData());
                Log.d(TAG, "下载完成");
                activity.mAdapter.notifyDownloadingComplete(data, position);
            }
        }

        @Override
        public void onProgress(int downloadId, long soFarBytes, long totalBytes, long speed, int progress) {
            super.onProgress(downloadId, soFarBytes, totalBytes, speed, progress);
            Log.d(TAG, "当前下载了" + (soFarBytes * 1.0f / totalBytes));
            MoreAnimationEffectActivity activity = weakReference.get();
            if (activity != null) {
                activity.mAdapter.updateProcess(
                    (MoreAnimationEffectAdapter.ImvViewHolder)activity.mRvMoreAnimationFilter.findViewHolderForAdapterPosition(position), progress,
                    position);
            }
        }

        @Override
        public void onError(BaseDownloadTask task, Throwable e) {
            super.onError(task, e);
            MoreAnimationEffectActivity activity = weakReference.get();
            if (activity != null) {
                activity.mAdapter.notifyDownloadingFailure(data);
                activity.afFormList.remove(data.getData());
                ToastUtil.showToast(activity, e.getMessage());
                synchronized (tasks) {
                    for (FileDownloaderModel t : tasks) {//删除该套特效的所有Task
                        DownloaderManager.getInstance().deleteTaskByTaskId(t.getTaskId());
                    }
                    tasks.clear();
                }
                //清空已插入到数据库中的该套特效的信息
                DownloaderManager.getInstance().getDbController().deleteTaskById(data.getData().getId());
            }
        }
    }
}
