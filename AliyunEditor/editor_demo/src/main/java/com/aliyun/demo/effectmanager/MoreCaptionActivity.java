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
import com.aliyun.common.logger.Logger;
import com.aliyun.common.utils.CommonUtil;
import com.aliyun.common.utils.ToastUtil;
import com.aliyun.demo.actionbar.ActionBarActivity;
import com.aliyun.demo.editor.R;
import com.aliyun.demo.http.EffectService;
import com.aliyun.demo.util.Common;
import com.aliyun.downloader.DownloaderManager;
import com.aliyun.downloader.FileDownloaderCallback;
import com.aliyun.downloader.FileDownloaderModel;
import com.aliyun.jasonparse.JSONSupportImpl;
import com.aliyun.qupaiokhttp.HttpRequest;
import com.aliyun.qupaiokhttp.StringHttpRequestCallback;
import com.aliyun.struct.form.FontForm;
import com.aliyun.struct.form.PasterForm;
import com.aliyun.struct.form.ResourceForm;
import com.liulishuo.filedownloader.BaseDownloadTask;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class MoreCaptionActivity extends ActionBarActivity implements
        MoreCaptionAdapter.OnItemRightButtonClickListener, View.OnClickListener {
    private static final String TAG = MoreCaptionActivity.class.getName();

    public static final String SELECTED_ID = "selected_id";
    private RecyclerView mRvMorePaster;
    private MoreCaptionAdapter mAdapter;
    private List<EffectBody<ResourceForm>> mData = new ArrayList<>();
    private CaptionLoader mCaptionLoader = new CaptionLoader();
    private Hashtable<Integer, Boolean> mIsBreak = null;
    private List<ResourceForm> mLoadingCaption = null;
    private int mFontResIndex = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aliyun_svideo_activity_more_paster);
        setActionBarLeftText(getString(R.string.cancel_more_mv_edit));
        setActionBarLeftViewVisibility(View.VISIBLE);
        setActionBarTitle(getString(R.string.more_caption_nav_edit));
        setActionBarTitleVisibility(View.VISIBLE);
        setActionBarRightView(R.mipmap.aliyun_svideo_icon_edit);
        setActionBarRightViewVisibility(View.VISIBLE);
        setActionBarLeftClickListener(this);
        setActionBarRightClickListener(this);
        mRvMorePaster = (RecyclerView) findViewById(R.id.rv_more_paster);
        mAdapter = new MoreCaptionAdapter(mData, new ArrayList<EffectBody<ResourceForm>>(), this);
        mRvMorePaster.setAdapter(mAdapter);
        mRvMorePaster.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,
                false));
        mCaptionLoader.init(this);
        mAdapter.setRightBtnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        new AsyncTask<Void, Void, List<ResourceForm>>() {
            @Override
            protected List<ResourceForm> doInBackground(Void... voids) {
                List<ResourceForm> list = mCaptionLoader.loadLocalCaptions();
                return list;
            }

            @Override
            protected void onPostExecute(List<ResourceForm> list) {
                if(list != null) {
                    List<EffectBody<ResourceForm>> localData = new ArrayList<>();
                    EffectBody<ResourceForm> body;
                    for(ResourceForm paster:list) {
                        body = new EffectBody<ResourceForm>(paster, true);
                        localData.add(body);
                    }
                    mAdapter.setLocalData(localData);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        mCaptionLoader.loadAllCaption(AppInfo.getInstance().obtainAppSignature(getApplicationContext()),
                new CaptionLoader.LoadCallback() {
            @Override
            public void onLoadCompleted(List<ResourceForm> localInfos,
                                        List<ResourceForm> remoteInfos,
                                        Throwable e) {
                List<EffectBody<ResourceForm>> remoteData = new ArrayList<>();
                if (remoteInfos != null) {
                    EffectBody<ResourceForm> body;
                    for (ResourceForm paster : remoteInfos) {
                        body = new EffectBody<>(paster, false);
                        remoteData.add(body);
                    }
                }
                mAdapter.setRemoteData(remoteData);
                mIsBreak = new Hashtable<Integer, Boolean>(remoteData.size());
                mLoadingCaption = new ArrayList<ResourceForm>(remoteData.size());
            }
        });
    }

    private void downloadAll(final EffectBody<ResourceForm> data, final List<FontForm> fontForms, final int position) {
        if(CommonUtil.SDFreeSize() < 10 * 1000 *1000) {
            Toast.makeText(this, R.string.no_free_memory, Toast.LENGTH_SHORT).show();
            return;
        }
        ResourceForm caption = data.getData();
        final List<PasterForm> materials = caption.getPasterList();
        final List<FileDownloaderModel> tasks = new ArrayList<>();
        if (materials != null) {
            final int[] process = new int[materials.size() + fontForms.size()];
            Log.e(TAG,"process size... "+ process.length);
            FileDownloaderModel model;
            for (final PasterForm material : materials) {
                model = new FileDownloaderModel();
                model.setEffectType(EffectService.EFFECT_CAPTION);
                model.setName(caption.getName());
                model.setDescription(caption.getDescription());
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
                DownloaderManager.getInstance().startTask(task.getTaskId(), new FileDownloaderCallback() {
                    @Override
                    public void onFinish(int downloadId, String path) {
                        super.onFinish(downloadId, path);
                        boolean isAllCompleted = false;
                        synchronized (tasks) {
                            tasks.remove(task);
                            if (tasks.size() == 0) {
                                isAllCompleted = true;
                            }
                        }
                        Log.e(TAG,"downloadId ..."+downloadId+"threadId..."+ Thread.currentThread().getId() + "task size..."+tasks.size());
                        if (isAllCompleted
                                && (mIsBreak == null
                                || mIsBreak.size() == 0
                                || !mIsBreak.get(data.getData().getId()))) {
                            mAdapter.changeToLocal(data);
                            mLoadingCaption.remove(data.getData());
                            Log.d(TAG, "下载完成");
                        }
                    }

                    @Override
                    public void onProgress(int downloadId, long soFarBytes, long totalBytes, long speed, int progress) {
                        super.onProgress(downloadId, soFarBytes, totalBytes, speed, progress);
                        Log.d(TAG, "当前下载了" + (soFarBytes * 1.0f / totalBytes));
                        process[materials.indexOf(material)] = progress;
                        progress = Math.round((float) Common.getTotal(process) / process.length);
                        mAdapter.updateProcess((MoreCaptionAdapter.CaptionViewHolder) mRvMorePaster.findViewHolderForAdapterPosition(position), progress, position);
                    }

                    @Override
                    public void onError(BaseDownloadTask task, Throwable e) {
                        super.onError(task, e);
                        ToastUtil.showToast(MoreCaptionActivity.this, R.string.material_downloading_failure);
                        mIsBreak.put(data.getData().getId(), true);//将当前字幕的素材循环下载中断
                        synchronized (tasks) {
                            for (FileDownloaderModel t : tasks) {//删除该套字幕的所有Task
                                DownloaderManager.getInstance().deleteTaskByTaskId(t.getTaskId());
                            }
                            tasks.clear();
                        }
                        //清空已插入到数据库中的该套字幕的信息
                        DownloaderManager.getInstance().getDbController().deleteTaskById(data.getData().getId());
                    }
                });
                tasks.add(task);
            }
            for (final FontForm fontForm : fontForms) {
                model = new FileDownloaderModel();
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
                final FileDownloaderModel task = DownloaderManager.getInstance().addTask(model, model.getUrl());
                DownloaderManager.getInstance().startTask(task.getTaskId(), new FileDownloaderCallback() {
                    @Override
                    public void onFinish(int downloadId, String path) {
                        super.onFinish(downloadId, path);
                        boolean isAllCompleted = false;
                        synchronized (tasks) {
                            tasks.remove(task);
                            if (tasks.size() == 0) {
                                isAllCompleted = true;
                            }
                        }
                        Log.e(TAG,"downloadId font..."+downloadId+"threadId..."+ Thread.currentThread().getId()+ "task size..."+tasks.size());
                        if (isAllCompleted
                                && (mIsBreak == null
                                || mIsBreak.size() == 0
                                || !mIsBreak.get(data.getData().getId()))) {
                            mAdapter.changeToLocal(data);
                            mLoadingCaption.remove(data.getData());
                            Log.d(TAG, "下载完成");
                        }
                    }

                    @Override
                    public void onProgress(int downloadId, long soFarBytes, long totalBytes, long speed, int progress) {
                        super.onProgress(downloadId, soFarBytes, totalBytes, speed, progress);
                        Log.d(TAG, "当前下载了" + (soFarBytes * 1.0f / totalBytes));
                        process[fontForms.indexOf(fontForm) + materials.size()] = progress;
                        progress = Math.round((float) Common.getTotal(process) / process.length);
                        mAdapter.updateProcess((MoreCaptionAdapter.CaptionViewHolder) mRvMorePaster.findViewHolderForAdapterPosition(position), progress, position);
                    }

                    @Override
                    public void onError(BaseDownloadTask task, Throwable e) {
                        super.onError(task, e);
                        ToastUtil.showToast(MoreCaptionActivity.this, R.string.material_downloading_failure);
                        mIsBreak.put(data.getData().getId(), true);//将当前字幕的素材循环下载中断
                        synchronized (tasks) {
                            for (FileDownloaderModel t : tasks) {//删除该套字幕的所有Task
                                DownloaderManager.getInstance().deleteTaskByTaskId(t.getTaskId());
                            }
                            tasks.clear();
                        }
                        //清空已插入到数据库中的该套字幕的信息
                        DownloaderManager.getInstance().getDbController().deleteTaskById(data.getData().getId());
                    }
                });
                tasks.add(task);
            }
        }
    }

    @Override
    public void onRemoteItemClick(final int position, final EffectBody<ResourceForm> data) {
        if(!CommonUtil.hasNetwork(this)) {
            ToastUtil.showToast(this, R.string.has_no_network);
            return;
        }
        if (mLoadingCaption.contains(data.getData())) {//如果已经在下载中了，则不能重复下载
            return;
        }
        mFontResIndex = 0;
        mLoadingCaption.add(data.getData());
        //下载
        ResourceForm caption = data.getData();
        final List<PasterForm> materials = caption.getPasterList();
        final List<FontForm> fontForms = new ArrayList<>();
        final List<Integer> ids = new ArrayList<>();
        final int index = 0;
        StringBuilder requestUrl = new StringBuilder();
        for (PasterForm pasterForm : materials) {
            requestUrl.delete(0, requestUrl.length());
            requestUrl.append(Common.BASE_URL)
                    .append("/api/res/get/1/")
                    .append(pasterForm.getFontId())
                    .append("?packageName=")
                    .append(getApplicationInfo().packageName)
                    .append("&signature=")
                    .append(AppInfo.getInstance().obtainAppSignature(getApplicationContext()));
            Logger.getDefaultLogger().d("pasterUrl url = " + requestUrl.toString());
            HttpRequest.get(requestUrl.toString(),
                    new StringHttpRequestCallback() {
                        @Override
                        protected void onSuccess(String s) {
                            super.onSuccess(s);
                            mFontResIndex++;
                            JSONSupportImpl jsonSupport = new JSONSupportImpl();
                            FontForm fontForm = null;
                            try {
                                fontForm = jsonSupport.readValue(s, FontForm.class);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (fontForm != null) {
                                if(!ids.contains(fontForm.getId())){
                                    fontForms.add(fontForm);
                                    ids.add(fontForm.getId());
                                }
                            }
                            Log.e(TAG,"currentIndex... "+ mFontResIndex);
                            if (mFontResIndex == materials.size()) {
                                downloadAll(data, fontForms, position);
                            }
                        }

                        @Override
                        public void onFailure(int errorCode, String msg) {
                            super.onFailure(errorCode, msg);
                            mFontResIndex++;
                            Log.e(TAG,"currentIndex ffff... "+ mFontResIndex);
                            if (mFontResIndex == materials.size()) {
                                downloadAll(data, fontForms, position);
                            }
                        }
                    });
        }

    }

    @Override
    public void onLocalItemClick(int position, EffectBody<ResourceForm> data) {
        //TODO 使用该字幕
        Intent intent = new Intent();
        intent.putExtra(SELECTED_ID, data.getData().getId());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == getActionBarLeftViewID()) {
            onBackPressed();
        } else if (v.getId() == getActionBarRightViewID()) {
            Intent intent = new Intent(MoreCaptionActivity.this, EffectManagerActivity.class);
            intent.putExtra(EffectManagerActivity.KEY_TAB, EffectManagerActivity.CAPTION);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloaderManager.getInstance().pauseAllTask();
    }
}
