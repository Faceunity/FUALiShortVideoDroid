/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.recorder.view.effects.manager;

import android.util.SparseArray;

import com.aliyun.demo.recorder.view.effects.mv.MVDownloadListener;
import com.aliyun.downloader.DownloaderManager;
import com.aliyun.downloader.FileDownloaderCallback;
import com.liulishuo.filedownloader.BaseDownloadTask;

import java.util.LinkedList;

public class TasksManager {
    private LinkedList<BaseDownloadTask> list = new LinkedList<BaseDownloadTask>();

    private int mProgress = 0;
    private int mFinishProgress = 0;
    private int size = 1;
    private SparseArray<MVDownloadListener> mCallbackList = new SparseArray<>();

    public void addTask(final int taskid, MVDownloadListener callback) {
        mCallbackList.put(taskid, callback);
        BaseDownloadTask baseDownloadTask = DownloaderManager.getInstance().createTask(taskid, new FileDownloaderCallback() {

            @Override
            public void onStart(int downloadId, long soFarBytes, long totalBytes, int preProgress) {
                super.onStart(downloadId, soFarBytes, totalBytes, preProgress);
                if(mCallbackList != null) {
                    mCallbackList.get(downloadId).onStart(downloadId, soFarBytes, totalBytes, preProgress);
                }
            }

            @Override
            public void onProgress(int downloadId, long soFarBytes, long totalBytes, long speed, int progress) {
                super.onProgress(downloadId, soFarBytes, totalBytes, speed, progress);
                mProgress = mFinishProgress + progress/size;
                if(mCallbackList.get(downloadId) != null) {
                    mCallbackList.get(downloadId).onProgress(downloadId, soFarBytes, totalBytes,  speed, mProgress);
                }
            }

            @Override
            public void onFinish(int downloadId, String path) {
                mFinishProgress = mProgress;
                super.onFinish(downloadId, path);
                if(!startTask()) {
                    if(mCallbackList.get(downloadId) != null) {
                        mCallbackList.get(downloadId).onFinish(downloadId, path,true);
                    }
                }else {
                    if(mCallbackList.get(downloadId) != null) {
                        mCallbackList.get(downloadId).onFinish(downloadId, path,false);
                    }
                }
            }

            @Override
            public void onError(BaseDownloadTask task, Throwable e) {
                super.onError(task, e);
                if(mCallbackList.get(task.getDownloadId()) != null) {
                    mCallbackList.get(task.getDownloadId()).onError(task, e);
                }
            }
        });
        if(baseDownloadTask != null) {
            list.add(baseDownloadTask);
        }
        if(list.size() > 0) {
            size = list.size();
        }
    }

    public boolean startTask() {
        if(!list.isEmpty()) {
            list.poll().start();
            return true;
        } else {
            return false;
        }
    }

}
