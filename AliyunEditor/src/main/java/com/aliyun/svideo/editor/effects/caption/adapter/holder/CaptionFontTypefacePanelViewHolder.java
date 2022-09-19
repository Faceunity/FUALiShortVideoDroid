/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.caption.adapter.holder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.common.utils.ThreadUtils;
import com.aliyun.svideo.downloader.FileDownloaderModel;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.contant.CaptionConfig;
import com.aliyun.svideo.editor.effects.caption.adapter.CaptionFontAdapter;
import com.aliyun.svideo.editor.effects.caption.listener.OnCaptionChooserStateChangeListener;
import com.aliyun.svideo.editor.effects.caption.manager.CaptionManager;
import com.aliyun.svideosdk.common.ISource;
import com.aliyun.svideosdk.editor.impl.AliyunPasterControllerCompoundCaption;

import java.util.List;

public class CaptionFontTypefacePanelViewHolder extends BaseCaptionViewHolder {

    private CaptionFontAdapter mCaptionAdapter;
    private int currentCaptionControlId;

    public CaptionFontTypefacePanelViewHolder(Context context, String title, OnCaptionChooserStateChangeListener onCaptionChooserStateChangeListener) {
        super(context, title, onCaptionChooserStateChangeListener);
    }


    @Override
    public View onCreateView(Context context) {
        return LayoutInflater.from(context).inflate(R.layout.alivc_editor_caption_style_font_container, null, false);
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
            ThreadUtils.runOnSubThread(new Runnable() {
                @Override
                public void run() {
                    int captionControllerId = CaptionManager.getCaptionControllerId(getCaptionChooserStateChangeListener());
                    if (currentCaptionControlId != captionControllerId) {
                        final int currentIndex = getCurrentIndex();
                        currentCaptionControlId = captionControllerId;
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mCaptionAdapter.setmCurrectSelectIndex(currentIndex);
                            }
                        });
                    }
                }
            });
        }
    }
    private void lazyInit() {
        View itemView = getItemView();
        if (itemView != null && mCaptionAdapter == null) {
            RecyclerView captionList = itemView.findViewById(R.id.effect_list);
            captionList.setLayoutManager(new GridLayoutManager(getContext(), 4, GridLayoutManager.VERTICAL, false));
            mCaptionAdapter = new CaptionFontAdapter();
            mCaptionAdapter.setOnItemClickListener(getCaptionChooserStateChangeListener());
            captionList.setAdapter(mCaptionAdapter);
            currentCaptionControlId = CaptionManager.getCaptionControllerId(getCaptionChooserStateChangeListener());
            loadLocalPaster();
        } else {
            notifyDataSetChanged();
        }
    }

    private int getCurrentIndex() {
        int currentIndex = 0;
        AliyunPasterControllerCompoundCaption captionController = CaptionManager.getCaptionController(getCaptionChooserStateChangeListener());
        if (captionController != null && mCaptionAdapter != null) {
            ISource fontSource = captionController.getFontPath();
            if (fontSource != null) {
                String fontPath = fontSource.getPath();
                if (fontPath != null) {
                    List<FileDownloaderModel> fontData = mCaptionAdapter.getFontData();
                    if (fontData != null) {
                        for (int i = 0; i < fontData.size(); i++) {
                            FileDownloaderModel fileDownloaderModel = fontData.get(i);
                            if (fileDownloaderModel != null) {
                                String url = fileDownloaderModel.getPath() + CaptionConfig.FONT_NAME;
                                if (fontPath.equals(url)) {
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

    /**
     * 加载本地资源
     */
    private void loadLocalPaster() {
        ThreadUtils.runOnSubThread(new Runnable() {
            @Override
            public void run() {
                final List<FileDownloaderModel> fontFromLocal = CaptionManager.getFontFromLocal();
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCaptionAdapter != null) {
                            mCaptionAdapter.setData(fontFromLocal);
                        }
                    }
                });
            }
        });

    }

}
