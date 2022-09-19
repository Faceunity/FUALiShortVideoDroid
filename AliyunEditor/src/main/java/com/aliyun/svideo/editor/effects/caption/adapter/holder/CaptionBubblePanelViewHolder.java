/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.caption.adapter.holder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.base.Form.PasterForm;
import com.aliyun.svideo.common.utils.ThreadUtils;
import com.aliyun.svideo.downloader.FileDownloaderModel;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effectmanager.MoreCaptionActivity;
import com.aliyun.svideo.editor.effects.caption.adapter.CaptionBubbleAdapter;
import com.aliyun.svideo.editor.effects.caption.adapter.CaptionBubbleCategoryAdapter;
import com.aliyun.svideo.editor.effects.caption.manager.CaptionManager;
import com.aliyun.svideo.editor.effects.caption.listener.OnCaptionChooserStateChangeListener;
import com.aliyun.svideosdk.editor.impl.AliyunPasterControllerCompoundCaption;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.aliyun.svideo.editor.effects.control.BaseChooser.CAPTION_REQUEST_CODE;

/**
 * 字幕气泡
 */
public class CaptionBubblePanelViewHolder extends BaseCaptionViewHolder {
    private List<FileDownloaderModel> mCategoryData = new ArrayList<>();
    private Map<Integer, List<PasterForm>> mPastersMap = new HashMap<>();
    private RecyclerView mCategoryList;
    private RecyclerView mCaptionList;
    private CaptionBubbleCategoryAdapter mCategoryAdapter;
    private CaptionBubbleAdapter mCaptionAdapter;
    protected int mCurrID = 0;
    private int currentCaptionControlId;


    public CaptionBubblePanelViewHolder(Context context, String title, OnCaptionChooserStateChangeListener onCaptionChooserStateChangeListener) {
        super(context, title, onCaptionChooserStateChangeListener);
    }


    @Override
    public View onCreateView(Context context) {
        return LayoutInflater.from(context).inflate(R.layout.alivc_editor_caption_bubble_container, null, false);
    }

    @Override
    public void onBindViewHolder() {
    }

    @Override
    public void onTabClick() {
        lazyInit();
    }

    @Override
    public void resourceChanged() {
        if (mCaptionAdapter != null) {
            loadLocalPaster();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (mCaptionAdapter != null) {
            final int captionControllerId = CaptionManager.getCaptionControllerId(getCaptionChooserStateChangeListener());
            ThreadUtils.runOnSubThread(new Runnable() {
                @Override
                public void run() {
                    if (currentCaptionControlId != captionControllerId) {
                        final int currentIndex = getCurrentIndex();
                        currentCaptionControlId = captionControllerId;
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mCaptionAdapter.setSelectPosition(currentIndex);
                            }
                        });

                    }
                }
            });

        }

    }


    private void lazyInit() {
        View itemView = getItemView();
        if (itemView != null && mCategoryAdapter == null) {
            mCaptionList = itemView.findViewById(R.id.effect_list);
            mCaptionList.setLayoutManager(new GridLayoutManager(getContext(), 4, GridLayoutManager.VERTICAL, false));
            mCaptionAdapter = new CaptionBubbleAdapter();
            mCaptionAdapter.setOnItemClickListener(getCaptionChooserStateChangeListener());
            mCaptionList.setAdapter(mCaptionAdapter);
            mCategoryList = itemView.findViewById(R.id.category_list);
            mCategoryList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            mCategoryAdapter = new CaptionBubbleCategoryAdapter();
            mCategoryList.setAdapter(mCategoryAdapter);
            mCategoryAdapter.setOnItemClickListener(onItemClickListener);
            currentCaptionControlId = CaptionManager.getCaptionControllerId(getCaptionChooserStateChangeListener());
            loadLocalPaster();
        } else {
            notifyDataSetChanged();
        }
    }

    private void loadLocalPaster() {
        ThreadUtils.runOnSubThread(new Runnable() {
            @Override
            public void run() {
                final List<FileDownloaderModel> bubbleFromLocal = CaptionManager.getBubbleFromLocal();
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initResourceLocalWithSelectId(bubbleFromLocal);
                    }
                });
            }
        });

    }

    public void initResourceLocalWithSelectId(List<FileDownloaderModel> modelsTemp) {
        mCategoryData.clear();
        mPastersMap.clear();
        ArrayList<Integer> ids = new ArrayList<>();
        if (modelsTemp != null && modelsTemp.size() > 0) {
            for (FileDownloaderModel model : modelsTemp) {
                if (model != null && new File(model.getPath()).exists()) {
                    int id = model.getId();
                    if (!ids.contains(id)) {
                        ids.add(id);
                        mCategoryData.add(model);
                    }
                    List<PasterForm> pasteList = mPastersMap.get(id);
                    if (pasteList == null) {
                        pasteList = new ArrayList<>();
                        mPastersMap.put(id, pasteList);
                    }
                    pasteList.add(addPasterForm(model));
                }
            }

        }

        FileDownloaderModel moreModel = new FileDownloaderModel();
        moreModel.setCategory(-1);
        mCategoryData.add(null);
        mCategoryData.add(moreModel);
        mCategoryAdapter.setData(mCategoryData);
        if (mCategoryData.size() > 0) {
            FileDownloaderModel fileDownloaderModel = mCategoryData.get(0);
            if (fileDownloaderModel != null) {
                mCurrID = fileDownloaderModel.getId();
                mCategoryList.smoothScrollToPosition(0);
                mCategoryAdapter.setData(mCategoryData);
                if (mPastersMap.size() > 0) {
                    mCaptionAdapter.setData(fileDownloaderModel, mPastersMap.get(mCurrID), mCurrID);
                }
            }

        }

    }

    private final CaptionBubbleCategoryAdapter.OnItemClickListener onItemClickListener = new CaptionBubbleCategoryAdapter.OnItemClickListener() {
        @Override
        public void onMoreClick() {
            Intent moreIntent = new Intent(getContext(), MoreCaptionActivity.class);
            ((Activity) getContext()).startActivityForResult(moreIntent, CAPTION_REQUEST_CODE);
        }

        @Override
        public void onItemClick(FileDownloaderModel fileDownloaderModel) {
            if (fileDownloaderModel == null) {
                //清除气泡
                if (getCaptionChooserStateChangeListener() != null) {
                    getCaptionChooserStateChangeListener().onBubbleEffectTemplateChanged(null, null);
                }
                mCaptionAdapter.clearSelectedView();
            } else {
                mCurrID = fileDownloaderModel.getId();
                mCaptionAdapter.setData(fileDownloaderModel, mPastersMap.get(mCurrID), mCurrID);
            }
        }
    };

    private int getCurrentIndex() {
        int currentIndex = -1;
        OnCaptionChooserStateChangeListener captionChooserStateChangeListener = getCaptionChooserStateChangeListener();
        if (captionChooserStateChangeListener != null && mPastersMap != null) {
            AliyunPasterControllerCompoundCaption aliyunPasterController = captionChooserStateChangeListener.getAliyunPasterController();
            if (aliyunPasterController != null) {
                String bubbleEffectTemplate = aliyunPasterController.getBubbleEffectTemplate();
                if (!TextUtils.isEmpty(bubbleEffectTemplate)) {
                    List<PasterForm> pasterForms = mPastersMap.get(mCurrID);
                    if (pasterForms != null) {
                        for (int i = 0; i < pasterForms.size(); i++) {
                            PasterForm pasterForm = pasterForms.get(i);
                            if (pasterForm != null) {
                                String downloadUrl = pasterForm.getDownloadUrl();
                                if (bubbleEffectTemplate.equals(downloadUrl)) {
                                    currentIndex = i;
                                    break;
                                }
                            }


                        }
                    }

                }
            }
        }
        return currentIndex;
    }

    private PasterForm addPasterForm(FileDownloaderModel model) {
        PasterForm pasterForm = new PasterForm();
        pasterForm.setPreviewUrl(model.getSubpreview());
        pasterForm.setSort(model.getSubsort());
        pasterForm.setId(model.getSubid());
        pasterForm.setFontId(model.getFontid());
        pasterForm.setMD5(model.getMd5());
        pasterForm.setType(model.getSubtype());
        pasterForm.setIcon(model.getSubicon());
        pasterForm.setDownloadUrl(model.getUrl());
        pasterForm.setName(model.getSubname());
        return pasterForm;
    }

}
