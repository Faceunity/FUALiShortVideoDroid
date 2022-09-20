/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.caption.adapter.holder;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.common.utils.ThreadUtils;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.caption.adapter.CaptionColorAdapter;
import com.aliyun.svideo.editor.effects.caption.listener.OnCaptionChooserStateChangeListener;
import com.aliyun.svideo.editor.effects.caption.manager.CaptionManager;
import com.aliyun.svideosdk.common.AliyunColor;
import com.aliyun.svideosdk.editor.impl.AliyunPasterControllerCompoundCaption;

import java.util.ArrayList;
import java.util.List;

public class CaptionBackgroundColorViewHolder extends BaseCaptionViewHolder {
    private RecyclerView mListView;
    private CaptionColorAdapter colorAdapter;
    private SeekBar mRadiusSeekBar;
    private int currentCaptionControlId;

    public CaptionBackgroundColorViewHolder(Context context, String title, OnCaptionChooserStateChangeListener onCaptionChooserStateChangeListener) {
        super(context, title, onCaptionChooserStateChangeListener);
    }

    private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener;



    @Override
    public View onCreateView(Context context) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.alivc_editor_caption_text_background_color_container, null, false);
        mListView = (RecyclerView) rootView.findViewById(R.id.color_list);
        View clearView = rootView.findViewById(R.id.img_clean);
        clearView.setVisibility(View.VISIBLE);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mListView.setLayoutManager(layoutManager);
        colorAdapter = new CaptionColorAdapter();
        colorAdapter.setModel(3);
        mRadiusSeekBar = rootView.findViewById(R.id.radius_seekBar);
        mListView.setAdapter(colorAdapter);
        colorAdapter.setColorList(initColors());
        mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (getCaptionChooserStateChangeListener() != null) {
                    getCaptionChooserStateChangeListener().onCaptionTextBackgroundCornerRadiusChanged(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
        currentCaptionControlId = CaptionManager.getCaptionControllerId(getCaptionChooserStateChangeListener());
        clearView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getCaptionChooserStateChangeListener()!= null){
                    AliyunColor aliyunColor = new AliyunColor(Color.TRANSPARENT);
                    getCaptionChooserStateChangeListener().onCaptionTextBackgroundColorChanged(aliyunColor);
                }
            }
        });
        mRadiusSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
        return rootView;
    }

    @Override
    public void onBindViewHolder() {
        if (colorAdapter != null) {
            colorAdapter.setOnCaptionColorItemClickListener(getCaptionChooserStateChangeListener());
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (colorAdapter != null) {
            final int captionControllerId = CaptionManager.getCaptionControllerId(getCaptionChooserStateChangeListener());
            ThreadUtils.runOnSubThread(new Runnable() {
                @Override
                public void run() {
                    if (currentCaptionControlId != captionControllerId) {
                        final int currentIndex = getCurrentIndex();
                        AliyunPasterControllerCompoundCaption captionController = CaptionManager.getCaptionController(getCaptionChooserStateChangeListener());
                        int cornerRadius = 0;
                        if (captionController != null) {
                            cornerRadius = (int) captionController.getBackgroundCornerRadius();
                        }
                        currentCaptionControlId = captionControllerId;
                        final int finalCornerRadius = cornerRadius;
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                colorAdapter.setSelectPosition(currentIndex);
                                mRadiusSeekBar.setOnSeekBarChangeListener(null);
                                mRadiusSeekBar.setProgress(finalCornerRadius);
                                mRadiusSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
                            }
                        });

                    }
                }
            });

        }
    }

    private int getCurrentIndex() {
        int currentIndex = -1;
        AliyunPasterControllerCompoundCaption aliyunPasterControllerCompoundCaption = CaptionManager.getCaptionController(getCaptionChooserStateChangeListener());
        if (colorAdapter != null && aliyunPasterControllerCompoundCaption != null) {
            AliyunColor color = aliyunPasterControllerCompoundCaption.getBackgroundColor();
            if (color != null) {
                int cl = color.toArgb();
                List<Integer> data = colorAdapter.getData();
                if (data != null) {
                    for (int i = 0; i < data.size(); i++) {
                        Integer integer = data.get(i);
                        if (integer == cl) {
                            currentIndex = i;
                            break;
                        }

                    }
                }
            }

        }
        return currentIndex;
    }

    @Override
    public void onTabClick() {
        notifyDataSetChanged();
    }

    private List<Integer> initColors() {
        List<Integer> list = new ArrayList<>();
        TypedArray colors = getContext().getResources().obtainTypedArray(R.array.qupai_text_edit_colors);
        for (int i = 0; i < 35; i++) {
            int color = colors.getColor(i, Color.WHITE);
            list.add(color);
        }
        colors.recycle();
        return list;
    }

}
