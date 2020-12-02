/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effectmanager;

import android.content.Context;
import android.database.Cursor;

import com.aliyun.svideo.downloader.DownloaderManager;
import com.aliyun.svideo.downloader.FileDownloaderModel;
import com.aliyun.svideo.base.Form.ResourceForm;
import com.aliyun.svideo.base.http.EffectService;
import com.aliyun.svideo.base.http.HttpCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CaptionLoader {

    public EffectService mService;

    interface LoadCallback {
        void onLoadCompleted(List<ResourceForm> localInfos, List<ResourceForm> remoteInfos, Throwable e);
    }
    public CaptionLoader() {
        mService = new EffectService();
    }


    public int loadAllCaption(Context context, final LoadCallback callback) {

        mService.loadEffectCaption(context.getPackageName(), new HttpCallback<List<ResourceForm>>() {
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
                    if (localIds != null && localIds.size() > 0) {
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
        selectedColumns.add(FileDownloaderModel.DESCRIPTION_EN);
        selectedColumns.add(FileDownloaderModel.ID);
        selectedColumns.add(FileDownloaderModel.ISNEW);
        selectedColumns.add(FileDownloaderModel.LEVEL);
        selectedColumns.add(FileDownloaderModel.NAME);
        selectedColumns.add(FileDownloaderModel.NAME_EN);
        selectedColumns.add(FileDownloaderModel.PREVIEW);
        selectedColumns.add(FileDownloaderModel.SORT);
        HashMap<String, String> conditionMap = new HashMap<String, String>();
        conditionMap.put(FileDownloaderModel.EFFECTTYPE, String.valueOf(EffectService.EFFECT_CAPTION));
        Cursor cursor  = DownloaderManager.getInstance()
                         .getDbController().getResourceColumns(conditionMap, selectedColumns);

        while (cursor.moveToNext()) {
            ResourceForm paster = new ResourceForm();
            paster.setIcon(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.ICON)));
            String description = cursor.getString(cursor.getColumnIndex(FileDownloaderModel.DESCRIPTION));
            if ("assets".equals(description)) {
                continue;
            } else {
                paster.setDescription(description);
                paster.setDescriptionEn(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.DESCRIPTION_EN)));
            }
            paster.setId(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.ID)));
            paster.setIsNew(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.ISNEW)));
            paster.setLevel(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.LEVEL)));
            paster.setName(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.NAME)));
            paster.setNameEn(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.NAME_EN)));
            paster.setPreviewUrl(cursor.getString(cursor.getColumnIndex(FileDownloaderModel.PREVIEW)));
            paster.setSort(cursor.getInt(cursor.getColumnIndex(FileDownloaderModel.SORT)));
            localCaptions.add(paster);
        }
        cursor.close();
        return localCaptions;
    }

}
