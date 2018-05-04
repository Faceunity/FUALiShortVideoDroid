/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.effectmanager;

import android.content.Context;
import android.database.Cursor;

import com.aliyun.demo.http.EffectService;
import com.aliyun.demo.http.HttpCallback;
import com.aliyun.downloader.DownloaderManager;
import com.aliyun.downloader.FileDownloaderModel;
import com.aliyun.struct.form.ResourceForm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CaptionLoader {

    public EffectService mService = new EffectService();
    private String mPackageName;

    interface LoadCallback {
        void onLoadCompleted(List<ResourceForm> localInfos, List<ResourceForm> remoteInfos, Throwable e);
    }

    public void init(Context context) {
        mPackageName = context.getApplicationInfo().packageName;
    }


    public int loadAllCaption(String signature, final CaptionLoader.LoadCallback callback) {

        mService.loadEffectCaption(EffectService.EFFECT_CAPTION, signature
                , mPackageName, new HttpCallback<List<ResourceForm>>() {
                    @Override
                    public void onSuccess(List<ResourceForm> result) {
                        if (callback != null) {

                            List<ResourceForm> localCaptions = loadLocalCaptions();
                            List<Integer> localIds = null;
                            if (localCaptions != null && localCaptions.size() > 0) {
                                localIds = new ArrayList<Integer>(localCaptions.size());
                                for (ResourceForm caption : localCaptions) {
                                    localIds.add(caption.getId());
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

                            callback.onLoadCompleted(localCaptions, result, null);
                        }

                    }

                    @Override
                    public void onFailure(Throwable e) {
                        List<ResourceForm> localCaptions = loadLocalCaptions();
                        if (callback != null) {
                            callback.onLoadCompleted(localCaptions, null, e);
                        }
                    }
                });
        return 0;
    }

    public List<ResourceForm> loadLocalCaptions() {
        List<ResourceForm> localCaptions = new ArrayList<>();
        List<String> selectedColumns = new ArrayList<String>();
        selectedColumns.add(FileDownloaderModel.ICON);
        selectedColumns.add(FileDownloaderModel.DESCRIPTION);
        selectedColumns.add(FileDownloaderModel.ID);
        selectedColumns.add(FileDownloaderModel.ISNEW);
        selectedColumns.add(FileDownloaderModel.LEVEL);
        selectedColumns.add(FileDownloaderModel.NAME);
        selectedColumns.add(FileDownloaderModel.PREVIEW);
        selectedColumns.add(FileDownloaderModel.SORT);
        HashMap<String, String> conditionMap = new HashMap<String, String>();
        conditionMap.put(FileDownloaderModel.EFFECTTYPE, String.valueOf(EffectService.EFFECT_CAPTION));
        Cursor cursor  = DownloaderManager.getInstance()
                .getDbController().getResourceColums(conditionMap, selectedColumns);

        while(cursor.moveToNext()) {
            ResourceForm paster = new ResourceForm();
            paster.setIcon(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.ICON)));
            paster.setDescription(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.DESCRIPTION)));
            paster.setId(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.ID)));
            paster.setIsNew(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.ISNEW)));
            paster.setLevel(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.LEVEL)));
            paster.setName(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.NAME)));
            paster.setPreviewUrl(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.PREVIEW)));
            paster.setSort(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.SORT)));
            localCaptions.add(paster);
        }
        cursor.close();
        return localCaptions;
    }

}
