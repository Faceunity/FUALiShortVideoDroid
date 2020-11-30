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

import com.aliyun.common.utils.CommonUtil;
import com.aliyun.svideo.common.utils.ToastUtils;
import com.aliyun.svideo.downloader.DownloaderManager;
import com.aliyun.svideo.downloader.FileDownloaderCallback;
import com.aliyun.svideo.downloader.FileDownloaderModel;
import com.aliyun.svideo.base.Form.FontForm;
import com.aliyun.svideo.base.Form.PasterForm;
import com.aliyun.svideo.base.Form.ResourceForm;
import com.aliyun.svideo.base.http.EffectService;
import com.aliyun.svideo.base.http.HttpCallback;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.util.EditorCommon;
import com.liulishuo.filedownloader.BaseDownloadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * 字幕--更多
 */
public class MoreCaptionActivity extends AbstractActionBarActivity implements
    MoreCaptionAdapter.OnItemRightButtonClickListener, View.OnClickListener {
    private static final String TAG = MoreCaptionActivity.class.getName();

    public static final String SELECTED_ID = "selected_id";
    private RecyclerView mRvMorePaster;
    private MoreCaptionAdapter mAdapter;
    private List<EffectBody<ResourceForm>> mData = new ArrayList<>();
    private CaptionLoader mCaptionLoader = new CaptionLoader();
    private Hashtable<Integer, Boolean> mIsBreak = null;
    private int mFontResIndex = 0;
    private Map<Integer, Integer> mFontInfoRequestCountMap = new HashMap<>();
    private AsyncTask<Void, Void, List<EffectBody<ResourceForm>>> loadCaptionResource;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alivc_editor_activity_more_effect);
        setActionBarLeftText(getString(R.string.alivc_common_cancel));
        setActionBarLeftViewVisibility(View.VISIBLE);
        setActionBarTitle(getString(R.string.alivc_editor_more_title_caption));
        setActionBarTitleVisibility(View.VISIBLE);
        setActionBarRightView(R.mipmap.aliyun_svideo_icon_edit);
        setActionBarRightViewVisibility(View.VISIBLE);
        setActionBarLeftClickListener(this);
        setActionBarRightClickListener(this);
        mRvMorePaster = (RecyclerView) findViewById(R.id.rv_more_paster);
        mAdapter = new MoreCaptionAdapter(mData, this);
        mRvMorePaster.setAdapter(mAdapter);
        mRvMorePaster.setLayoutManager(new LinearLayoutManager(this,
                                       LinearLayoutManager.VERTICAL,
                                       false));
        mAdapter.setRightBtnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCaptionLoader.loadAllCaption(MoreCaptionActivity.this, new CaptionLoader.LoadCallback() {
            @Override
            public void onLoadCompleted(List<ResourceForm> localInfos,
                                        List<ResourceForm> remoteInfos,
                                        Throwable e) {
                List<EffectBody<ResourceForm>> localData = new ArrayList<>();
                List<EffectBody<ResourceForm>> remoteData = new ArrayList<>();
                if (localInfos != null) {
                    EffectBody<ResourceForm> body;
                    for (ResourceForm paster : localInfos) {
                        localData.add(new EffectBody<>(paster, true));
                    }
                    mAdapter.syncData(localData);
                }
                if (remoteInfos != null) {
                    EffectBody<ResourceForm> body;
                    for (ResourceForm paster : remoteInfos) {
                        body = new EffectBody<>(paster, false);
                        remoteData.add(body);
                    }
                }
                remoteData.addAll(localData);
                mAdapter.syncData(remoteData);
                mIsBreak = new Hashtable<Integer, Boolean>(remoteData.size());
            }
        });
    }

    private void downloadAll(final EffectBody<ResourceForm> data, final List<FontForm> fontForms, final int position) {
        if (CommonUtil.SDFreeSize() < 10 * 1000 * 1000) {
            ToastUtils.show(this, R.string.alivc_common_no_free_memory);
            return;
        }
        ResourceForm caption = data.getData();
        final List<PasterForm> materials = caption.getPasterList();
        final List<FileDownloaderModel> tasks = new ArrayList<>();
        if (materials != null) {
            final int[] process = new int[materials.size() + fontForms.size()];
            Log.e(TAG, "process size... " + process.length);
            FileDownloaderModel model;
            for (final PasterForm material : materials) {
                model = new FileDownloaderModel();
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
                        Log.e(TAG, "downloadId ..." + downloadId + "threadId..." + Thread.currentThread().getId() + "task size..." + tasks.size());
                        if (isAllCompleted
                                && (mIsBreak == null
                                    || mIsBreak.size() == 0
                                    || !mIsBreak.get(data.getData().getId()))) {
                            mAdapter.changeToLocal(data);
                            Log.d(TAG, "下载完成");
                        }
                    }

                    @Override
                    public void onProgress(int downloadId, long soFarBytes, long totalBytes, long speed, int progress) {
                        super.onProgress(downloadId, soFarBytes, totalBytes, speed, progress);
                        Log.d(TAG, "当前下载了" + (soFarBytes * 1.0f / totalBytes));
                        process[materials.indexOf(material)] = progress;
                        progress = Math.round((float) EditorCommon.getTotal(process) / process.length);
                        mAdapter.updateProcess((MoreCaptionAdapter.CaptionViewHolder) mRvMorePaster.findViewHolderForAdapterPosition(position), progress, position);
                    }

                    @Override
                    public void onError(BaseDownloadTask task, Throwable e) {
                        super.onError(task, e);
                        ToastUtils.show(MoreCaptionActivity.this, e.getMessage());
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
                        Log.e(TAG, "downloadId font..." + downloadId + "threadId..." + Thread.currentThread().getId() + "task size..." + tasks.size());
                        if (isAllCompleted
                                && (mIsBreak == null
                                    || mIsBreak.size() == 0
                                    || !mIsBreak.get(data.getData().getId()))) {
                            mAdapter.changeToLocal(data);
                            Log.d(TAG, "下载完成");
                        }
                    }

                    @Override
                    public void onProgress(int downloadId, long soFarBytes, long totalBytes, long speed, int progress) {
                        super.onProgress(downloadId, soFarBytes, totalBytes, speed, progress);
                        Log.d(TAG, "当前下载了" + (soFarBytes * 1.0f / totalBytes));
                        process[fontForms.indexOf(fontForm) + materials.size()] = progress;
                        progress = Math.round((float) EditorCommon.getTotal(process) / process.length);
                        mAdapter.updateProcess((MoreCaptionAdapter.CaptionViewHolder) mRvMorePaster.findViewHolderForAdapterPosition(position), progress, position);
                    }

                    @Override
                    public void onError(BaseDownloadTask task, Throwable e) {
                        super.onError(task, e);
                        ToastUtils.show(MoreCaptionActivity.this, e.getMessage());
                        mIsBreak.put(data.getData().getId(), true);//将当前字幕的素材循环下载中断
                        synchronized (tasks) {
                            for (FileDownloaderModel t : tasks) {//删除该套字幕的所有Task
                                DownloaderManager.getInstance().deleteTaskByTaskId(t.getTaskId());
                            }
                            tasks.clear();
                            mAdapter.onDownloadFailure(data);
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
        if (!CommonUtil.hasNetwork(this)) {
            ToastUtils.show(this, R.string.alivc_editor_more_no_network);
            return;
        }
        mFontResIndex = 0;
        //下载
        ResourceForm caption = data.getData();

        final List<FontForm> fontForms = new ArrayList<>();
        final List<Integer> ids = new ArrayList<>();
        mCaptionLoader.mService.getCaptionListById(MoreCaptionActivity.this.getPackageName(), caption.getId(), new HttpCallback<List<PasterForm>>() {
            @Override
            public void onSuccess(List<PasterForm> result) {
                data.getData().setPasterList(result);
                final List<PasterForm> materials = result;
                final int index = 0;
                mFontInfoRequestCountMap.put(position, 0 );
                for (PasterForm pasterForm : materials) {
                    if (!ids.contains(pasterForm.getFontId())) {
                        ids.add(pasterForm.getFontId());
                        int count = mFontInfoRequestCountMap.get(position);
                        count++;
                        mFontInfoRequestCountMap.put(position, count );
                        mCaptionLoader.mService.getFontById(MoreCaptionActivity.this.getPackageName(), pasterForm.getFontId(), new HttpCallback<FontForm>() {
                            @Override
                            public void onSuccess(FontForm result) {
                                fontForms.add(result);
                                int count = mFontInfoRequestCountMap.get(position);
                                count--;
                                mFontInfoRequestCountMap.put(position, count );
                                if (count == 0) {
                                    downloadAll(data, fontForms, position);
                                }
                            }

                            @Override
                            public void onFailure(Throwable e) {
                                int count = mFontInfoRequestCountMap.get(position);
                                count--;
                                mFontInfoRequestCountMap.put(position, count );
                                if (count == 0) {
                                    downloadAll(data, fontForms, position);
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Throwable e) {

            }
        });


    }

    @Override
    public void onLocalItemClick(int position, EffectBody<ResourceForm> data) {
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
        // 需求要求, 下载过程中返回上层页面, 继续在后台下载
        //DownloaderManager.getInstance().pauseAllTask();

        if (loadCaptionResource != null) {
            loadCaptionResource.cancel(true);
            loadCaptionResource = null;
        }
    }
}
