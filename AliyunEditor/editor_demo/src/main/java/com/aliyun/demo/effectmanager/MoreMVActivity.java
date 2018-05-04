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
import com.aliyun.demo.actionbar.ActionBarActivity;
import com.aliyun.demo.editor.R;
import com.aliyun.demo.http.EffectService;
import com.aliyun.downloader.DownloaderManager;
import com.aliyun.downloader.FileDownloaderCallback;
import com.aliyun.downloader.FileDownloaderModel;
import com.aliyun.struct.form.AspectForm;
import com.aliyun.struct.form.IMVForm;
import com.liulishuo.filedownloader.BaseDownloadTask;

import java.util.ArrayList;
import java.util.List;

public class MoreMVActivity extends ActionBarActivity implements
        MoreMVAdapter.OnItemRightButtonClickListener, View.OnClickListener {
    private static final String TAG = MoreMVActivity.class.getName();
    public static final String SELECTD_ID = "selected_id";
    private RecyclerView mRvMoreMV;
    private MoreMVAdapter mAdapter;
    private EffectLoader mMVLoader = new EffectLoader();
    private List<IMVForm> mLoadingMv = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aliyun_svideo_activity_more_paster);
        setActionBarLeftText(getString(R.string.cancel_more_mv_edit));
        setActionBarLeftViewVisibility(View.VISIBLE);
        setActionBarTitle(getString(R.string.more_mv_nav_edit));
        setActionBarTitleVisibility(View.VISIBLE);
        setActionBarRightView(R.mipmap.aliyun_svideo_icon_edit);
        setActionBarRightViewVisibility(View.VISIBLE);
        setActionBarLeftClickListener(this);
        setActionBarRightClickListener(this);
        mRvMoreMV = (RecyclerView) findViewById(R.id.rv_more_paster);

        mAdapter = new MoreMVAdapter(this);
        mRvMoreMV.setAdapter(mAdapter);
        mRvMoreMV.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,
                false));
        mMVLoader.init(this);
        mAdapter.setRightBtnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        new AsyncTask<Void, Void, List<IMVForm>>() {
            @Override
            protected List<IMVForm> doInBackground(Void... voids) {
                List<IMVForm> list = mMVLoader.loadLocalMV();
                return list;
            }

            @Override
            protected void onPostExecute(List<IMVForm> list) {
                if(list != null) {
                    List<EffectBody<IMVForm>> localData = new ArrayList<>();
                    EffectBody<IMVForm> body;
                    for(IMVForm paster:list) {
                        body = new EffectBody<IMVForm>(paster, true);
                        localData.add(body);
                    }
                    mAdapter.syncData(localData);
                }
            }
        }.execute();

        mMVLoader.loadAllMV(AppInfo.getInstance().obtainAppSignature(getApplicationContext()),
                new EffectLoader.LoadCallback<IMVForm>() {
            @Override
            public void onLoadCompleted(List<IMVForm> localInfos,
                                        List<IMVForm> remoteInfos,
                                        Throwable e) {
                List<EffectBody<IMVForm>> remoteData = new ArrayList<>();
                List<EffectBody<IMVForm>> localData = new ArrayList<>();
                if(localInfos != null) {
                    EffectBody<IMVForm> body;
                    for(IMVForm form:localInfos) {
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
        if(!CommonUtil.hasNetwork(this)) {
            ToastUtil.showToast(this, R.string.has_no_network);
            return;
        }

        if(CommonUtil.SDFreeSize() < 10 * 1000 *1000) {
            Toast.makeText(this, R.string.no_free_memory, Toast.LENGTH_SHORT).show();
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
            List<FileDownloaderModel> list = new ArrayList<>();
            for (final AspectForm aspect : aspects) {
                model = new FileDownloaderModel();
                model.setEffectType(EffectService.EFFECT_MV);
                model.setTag(mv.getTag());
                model.setKey(mv.getKey());
                model.setName(mv.getName());
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
                tasksManager.addTask(task.getTaskId(), new FileDownloaderCallback() {
                    @Override
                    public void onStart(int downloadId, long soFarBytes, long totalBytes, int preProgress) {
                        super.onStart(downloadId, soFarBytes, totalBytes, preProgress);
                        mAdapter.notifyDownloadingStart(data);
                    }

                    @Override
                    public void onFinish(int downloadId, String path) {
                        super.onFinish(downloadId, path);
                        mLoadingMv.remove(data.getData());
                        Log.d(TAG, "下载完成");
                        mAdapter.notifyDownloadingComplete(data, position);
                    }

                    @Override
                    public void onProgress(int downloadId, long soFarBytes, long totalBytes, long speed, int progress) {
                        super.onProgress(downloadId, soFarBytes, totalBytes, speed, progress);
                        Log.d(TAG, "当前下载了" + (soFarBytes * 1.0f / totalBytes));
                        mAdapter.updateProcess((MoreMVAdapter.ImvViewHolder) mRvMoreMV.findViewHolderForAdapterPosition(position), progress, position);
                    }

                    @Override
                    public void onError(BaseDownloadTask task, Throwable e) {
                        super.onError(task, e);
                        ToastUtil.showToast(MoreMVActivity.this, R.string.material_downloading_failure);
                        synchronized (tasks) {
                            for (FileDownloaderModel t : tasks) {//删除该套MV的所有Task
                                DownloaderManager.getInstance().deleteTaskByTaskId(t.getTaskId());
                            }
                            tasks.clear();
                        }
                        //清空已插入到数据库中的该套MV的信息
                        DownloaderManager.getInstance().getDbController().deleteTaskById(data.getData().getId());
                    }
                });
            }
            tasksManager.startTask();
        }
    }

    @Override
    public void onLocalItemClick(int position, EffectBody<IMVForm> data) {
        //TODO 使用该MV
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
    protected void onDestroy() {
        super.onDestroy();
        DownloaderManager.getInstance().pauseAllTask();
    }
}
