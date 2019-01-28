/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.effectmanager;

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

import com.aliyun.common.global.AppInfo;
import com.aliyun.common.utils.CommonUtil;
import com.aliyun.common.utils.ToastUtil;
import com.aliyun.demo.editor.R;
import com.aliyun.demo.http.EffectService;
import com.aliyun.demo.util.FixedToastUtils;
import com.aliyun.downloader.DownloaderManager;
import com.aliyun.downloader.FileDownloaderCallback;
import com.aliyun.downloader.FileDownloaderModel;
import com.aliyun.svideo.sdk.external.struct.form.PasterForm;
import com.aliyun.svideo.sdk.external.struct.form.ResourceForm;
import com.liulishuo.filedownloader.BaseDownloadTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 动图--更多
 */
public class MorePasterActivity extends AbstractActionBarActivity {

    public static final String SELECTED_ID = "selected_id";
    private static final String TAG = MorePasterActivity.class.getName();
    private RecyclerView mRvMorePaster;
    private MorePasterAdapter mAdapter;
    private EffectLoader mPasterLoader = new EffectLoader();
    private List<ResourceForm> mLoadingPaster = null;
    private List<FileDownloaderModel> mLoadingTasks = new ArrayList<>();

    private AsyncTask<Void, Void, List<EffectBody<ResourceForm>>> loadResource;
    /**
     * 用于记录当前下载的动图数量(不是组数, 而是一组中包含的个数)
     */
    private int downloadSize = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aliyun_svideo_activity_more_paster);
        setActionBarLeftText(getString(R.string.cancel_more_mv_edit));
        setActionBarLeftViewVisibility(View.VISIBLE);
        setActionBarTitle(getString(R.string.more_motion_sticker_nav_edit));
        setActionBarTitleVisibility(View.VISIBLE);
        setActionBarRightView(R.mipmap.aliyun_svideo_icon_edit);
        setActionBarRightViewVisibility(View.VISIBLE);
        setActionBarLeftClickListener(mOnClickListener);
        setActionBarRightClickListener(mOnClickListener);
        mRvMorePaster = (RecyclerView) findViewById(R.id.rv_more_paster);
        mRvMorePaster.setItemAnimator(null);
        mAdapter = new MorePasterAdapter(this);
        mRvMorePaster.setAdapter(mAdapter);
        mPasterLoader.init(this);
        mRvMorePaster.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter.setRightBtnClickListener(mOnItemRightButtonClickListener);
    }


    @Override
    protected void onStart() {
        super.onStart();
        //加载本地数据
        loadResource = new LoadResourceTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        //加载网络资源列表(会和本地对比)
        mPasterLoader.loadAllPaster(AppInfo.getInstance().obtainAppSignature(getApplicationContext()),
                new EffectLoader.LoadCallback<ResourceForm>() {
            @Override
            public void onLoadCompleted(List<ResourceForm> localInfos,
                                        List<ResourceForm> remoteInfos,
                                        Throwable e) {
                List<EffectBody<ResourceForm>> remoteData = new ArrayList<>();
                List<EffectBody<ResourceForm>> localData = new ArrayList<>();
                if(localInfos != null) {
                    EffectBody<ResourceForm> body;
                    for(ResourceForm paster:localInfos) {
                        body = new EffectBody<>(paster, true);
                        localData.add(body);
                    }
                }
                if(remoteInfos != null) {
                    EffectBody<ResourceForm> body;
                    for(ResourceForm paster:remoteInfos) {
                        body = new EffectBody<>(paster, false);
                        remoteData.add(body);
                    }
                }
                remoteData.addAll(localData);
                mAdapter.syncData(remoteData);
                mLoadingPaster = new ArrayList<>(remoteData.size());
            }
        });
    }


    private MorePasterAdapter.OnItemRightButtonClickListener mOnItemRightButtonClickListener = new MorePasterAdapter
        .OnItemRightButtonClickListener() {
        @Override
        public void onRemoteItemClick(final int position, final EffectBody<ResourceForm> data) {
            if(!CommonUtil.hasNetwork(MorePasterActivity.this)) {
                Toast.makeText(MorePasterActivity.this, R.string.has_no_network, Toast.LENGTH_SHORT).show();
                return;
            }

            if(CommonUtil.SDFreeSize() < 10 * 1000 *1000) {
                Toast.makeText(MorePasterActivity.this, R.string.no_free_memory, Toast.LENGTH_SHORT).show();
                return;
            }
            //如果已经在下载中了，则不能重复下载
            if(mLoadingPaster.contains(data.getData())){
                return;
            }
            mLoadingPaster.add(data.getData());

            //下载
            final ResourceForm paster =  data.getData();
            final List<PasterForm> materials = paster.getPasterList();
            final int size = materials.size();
            downloadSize += size;
            // 目前使用的下载框架里面, 最大下载数量为50, 如果超出这个数值, 那么会引起一些问题, 在这里做限制,
            if (downloadSize >= 50) {
                // 如果加上点击的item中的文件超过50, 那么久return, 并且要减去这个数量, 不然会一直积累
                downloadSize -= size;
                // 同时集合也应该移除掉这个下载的item, 否则能够下载时, 当前集合已经包含了这个资源, 会在签名的代码处return掉
                if (mLoadingPaster.contains(data.getData())) {
                    mLoadingPaster.remove(data.getData());
                }
                FixedToastUtils.show(getApplicationContext(), "当前下载任务太多, 请稍等片刻~~!");
                return;
            }
            mAdapter.notifyDownloadingStart(data);
            mAdapter.notifyItemChanged(position,MorePasterAdapter.DOWNLOAD_START);
            FileDownloaderModel model ;
            TasksManager tasksManager = new TasksManager();
            final List<FileDownloaderModel> currLoadingTask = new ArrayList<>();

            for(int i=0;i< size;i++) {
                final PasterForm material = materials.get(i);
                model = new FileDownloaderModel();
                model.setEffectType(EffectService.EFFECT_PASTER);
                model.setName(paster.getName());
                model.setDescription(paster.getDescription());
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

                tasksManager.addTask(task.getTaskId(), new FileDownloaderCallback(){
                    @Override
                    public void onStart(int downloadId, long soFarBytes, long totalBytes, int preProgress) {
                        super.onStart(downloadId, soFarBytes, totalBytes, preProgress);
                    }

                    @Override
                    public void onFinish(int downloadId, String path) {
                        super.onFinish(downloadId, path);
                        mLoadingPaster.remove(data.getData());
                        Log.d(TAG, "下载完成");
                        //Log.w(TAG, "下载完成  xxxx  position = " + position);
                        // 下载完成后, 要在总的下载数中减去已经下载完成的数量
                        downloadSize -= size;
                        mAdapter.notifyDownloadingComplete(data,position);
                    }

                    @Override
                    public void onProgress(int downloadId, long soFarBytes, long totalBytes, long speed, int progress) {
                        super.onProgress(downloadId, soFarBytes, totalBytes, speed, progress);
                        synchronized (this){

                            Log.d(TAG, "素材["+data.getData().getName()+"]当前下载了 progress");
                            //Log.d(TAG, "onProgress: xxxx : " + progress + "  ，position = " + position);
                            mAdapter.updateProcess((MorePasterAdapter.PasterViewHolder)mRvMorePaster.findViewHolderForAdapterPosition(position),
                                progress,
                                position);
                        }

                    }

                    @Override
                    public void onError(BaseDownloadTask task, Throwable e) {
                        super.onError(task, e);
                        ToastUtil.showToast(MorePasterActivity.this, R.string.material_downloading_failure);
                        synchronized (mLoadingTasks) {
                            List<FileDownloaderModel> delTasks = new ArrayList<>();
                            for (FileDownloaderModel t : mLoadingTasks) {//删除该套动图的所有Task
                                if(t.getId() == data.getData().getId()) {
                                    DownloaderManager.getInstance().deleteTaskByTaskId(t.getTaskId());
                                    delTasks.add(t);
                                }
                            }
                            mLoadingTasks.remove(delTasks);
                            delTasks.clear();
                        }
                        //清空已插入到数据库中的该套动图的信息
                        DownloaderManager.getInstance().getDbController().deleteTaskById(data.getData().getId());
                    }
                });
                mLoadingTasks.add(task); //总的下载列表
                currLoadingTask.add(task);//当前这套动图的下载列表
            }
            tasksManager.startTask();
            }

        @Override
        public void onLocalItemClick(int position, EffectBody<ResourceForm> data) {
            //使用该动图
            Intent intent = new Intent();
            intent.putExtra(SELECTED_ID,data.getData().getId());
            setResult(Activity.RESULT_OK,intent);
            finish();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if(mLoadingPaster != null && mLoadingPaster.size() > 0) {
            synchronized (mLoadingTasks) {
                for(FileDownloaderModel task:mLoadingTasks) {
                    DownloaderManager.getInstance().startTask(task.getTaskId());
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mLoadingPaster != null && mLoadingPaster.size() > 0) {
            synchronized (mLoadingTasks) {
                for(FileDownloaderModel task:mLoadingTasks) {
                    DownloaderManager.getInstance().pauseTask(task.getTaskId());
                }
            }
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == getActionBarLeftViewID()) {
                onBackPressed();
            }else if(v.getId() == getActionBarRightViewID()) {
                Intent intent = new Intent(MorePasterActivity.this, EffectManagerActivity.class);
                intent.putExtra(EffectManagerActivity.KEY_TAB, EffectManagerActivity.PASTER);
                startActivity(intent);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //DownloaderManager.getInstance().pauseAllTask();
        if (loadResource != null) {
            loadResource.cancel(true);
            loadResource = null;
        }
    }

    private static class LoadResourceTask extends AsyncTask<Void, Void, List<EffectBody<ResourceForm>>> {
        WeakReference<MorePasterActivity> weakReference;
        public LoadResourceTask(MorePasterActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        protected List<EffectBody<ResourceForm>> doInBackground(Void... voids) {
            MorePasterActivity activity = weakReference.get();
            List<ResourceForm> list = null;
            if (activity != null) {
                list = activity.mPasterLoader.loadLocalPaster();
            }
            List<EffectBody<ResourceForm>> localData = null;
            if(list != null) {
                localData = new ArrayList<>();
                EffectBody<ResourceForm> body;
                for(ResourceForm paster:list) {
                    body = new EffectBody<>(paster, true);
                    localData.add(body);
                }
            }

            return localData;
        }

        @Override
        protected void onPostExecute(List<EffectBody<ResourceForm>> localData) {
            MorePasterActivity activity = weakReference.get();
            if (activity != null ){
                if (localData != null) {
                    activity.mAdapter.syncData(localData);
                }
            }

        }
    }
}
