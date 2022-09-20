/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.caption.adapter.holder;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.common.utils.ThreadUtils;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.caption.adapter.CaptionCoolTextAdapter;
import com.aliyun.svideo.editor.effects.caption.adapter.holder.BaseCaptionViewHolder;
import com.aliyun.svideo.editor.effects.caption.manager.CaptionManager;
import com.aliyun.svideo.editor.effects.control.SpaceItemDecoration;
import com.aliyun.svideo.editor.util.EditorCommon;
import com.aliyun.svideo.editor.effects.caption.listener.OnCaptionChooserStateChangeListener;
import com.aliyun.svideosdk.editor.impl.AliyunPasterControllerCompoundCaption;

import java.util.List;


/**
 * 文本样式
 */
public class CaptionCoolTextPanelViewHolder extends BaseCaptionViewHolder {
    private CaptionCoolTextAdapter mCaptionCoolTextAdapter;
    private List<String> coolTextFileList;
    private int currentCaptionControlId;

    public CaptionCoolTextPanelViewHolder(Context context, String title, OnCaptionChooserStateChangeListener onCaptionChooserStateChangeListener) {
        super(context, title, onCaptionChooserStateChangeListener);
    }

    @Override
    public View onCreateView(Context context) {
        return LayoutInflater.from(context).inflate(R.layout.alivc_editor_caption_animation_container, null, false);
    }

    @Override
    public void onBindViewHolder() {
    }

    @Override
    public void onTabClick() {
        lazyInit();
    }


    private void lazyInit() {
        View itemView = getItemView();
        if (itemView != null && mCaptionCoolTextAdapter == null) {
            RecyclerView recyclerView = itemView.findViewById(R.id.font_animation_view);
            mCaptionCoolTextAdapter = new CaptionCoolTextAdapter();
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 4);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.addItemDecoration(new SpaceItemDecoration(
                    getContext().getResources().getDimensionPixelSize(R.dimen.list_item_space)));
            recyclerView.setAdapter(mCaptionCoolTextAdapter);
            mCaptionCoolTextAdapter.setOnItemClickListener(getCaptionChooserStateChangeListener());
            currentCaptionControlId = CaptionManager.getCaptionControllerId(getCaptionChooserStateChangeListener());
            loadFiles();
        } else {
            notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (mCaptionCoolTextAdapter != null) {
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
                                mCaptionCoolTextAdapter.setSelectPosition(currentIndex);
                            }
                        });

                    }
                }
            });

        }


    }

    private int getCurrentIndex() {
        int currentIndex = 0;
        OnCaptionChooserStateChangeListener captionChooserStateChangeListener = getCaptionChooserStateChangeListener();
        if (captionChooserStateChangeListener != null) {
            AliyunPasterControllerCompoundCaption aliyunPasterController = captionChooserStateChangeListener.getAliyunPasterController();
            if (aliyunPasterController != null) {
                String fontPath = aliyunPasterController.getFontEffectTemplate();
                if (!TextUtils.isEmpty(fontPath)) {
                    for (int i = 0; i < coolTextFileList.size(); i++) {
                        String path = coolTextFileList.get(i);
                        if (fontPath.equals(path)) {
                            currentIndex = i;
                            break;
                        }

                    }

                }
            }
        }
        return currentIndex;
    }

    public void loadFiles() {
        ThreadUtils.runOnSubThread(new Runnable() {
            @Override
            public void run() {
                coolTextFileList = EditorCommon.getCoolTextFileList();
                coolTextFileList.add(0, null);
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCaptionCoolTextAdapter != null) {
                            mCaptionCoolTextAdapter.setData(coolTextFileList);
                        }
                    }
                });
            }
        });

    }
}
