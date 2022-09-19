/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.caption.adapter.holder;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PointF;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.common.utils.ThreadUtils;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.caption.adapter.CaptionColorAdapter;
import com.aliyun.svideo.editor.effects.caption.manager.CaptionManager;
import com.aliyun.svideo.editor.effects.caption.listener.OnCaptionChooserStateChangeListener;
import com.aliyun.svideosdk.common.AliyunColor;
import com.aliyun.svideosdk.editor.impl.AliyunPasterControllerCompoundCaption;

import java.util.ArrayList;
import java.util.List;

public class CaptionShadowViewHolder extends BaseCaptionViewHolder {
    private CaptionColorAdapter colorAdapter;
    private SeekBar xSeekBar;
    private SeekBar ySeekBar;
    private int currentCaptionControlId;

    public CaptionShadowViewHolder(Context context, String title, OnCaptionChooserStateChangeListener onCaptionChooserStateChangeListener) {
        super(context, title, onCaptionChooserStateChangeListener);
    }

    @Override
    public View onCreateView(Context context) {
        return LayoutInflater.from(context).inflate(R.layout.alivc_editor_caption_style_shadow_container, null, false);
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
        if (itemView != null && colorAdapter == null) {
            RecyclerView mListView = (RecyclerView) itemView.findViewById(R.id.color_list);
            xSeekBar = itemView.findViewById(R.id.x_seekbar);
            ySeekBar = itemView.findViewById(R.id.y_seekbar);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
            mListView.setLayoutManager(layoutManager);
            colorAdapter = new CaptionColorAdapter();
            colorAdapter.setModel(2);
            mListView.setAdapter(colorAdapter);
            colorAdapter.setColorList(initColors());
            colorAdapter.setOnCaptionColorItemClickListener(getCaptionChooserStateChangeListener());
            xSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
            ySeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
            currentCaptionControlId = CaptionManager.getCaptionControllerId(getCaptionChooserStateChangeListener());
        } else {
            notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (colorAdapter != null) {
            ThreadUtils.runOnSubThread(new Runnable() {
                @Override
                public void run() {
                    int captionControllerId = CaptionManager.getCaptionControllerId(getCaptionChooserStateChangeListener());
                    if (currentCaptionControlId != captionControllerId) {
                        final int currentIndex = getCurrentIndex();
                        AliyunPasterControllerCompoundCaption captionController = CaptionManager.getCaptionController(getCaptionChooserStateChangeListener());
                        PointF shadowOffset = null;
                        if (captionController != null) {
                            shadowOffset = captionController.getShadowOffset();
                        }
                        currentCaptionControlId = captionControllerId;
                        final PointF finalShadowOffset = shadowOffset;
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                xSeekBar.setOnSeekBarChangeListener(null);
                                ySeekBar.setOnSeekBarChangeListener(null);
                                if (finalShadowOffset != null){
                                    xSeekBar.setProgress((int) finalShadowOffset.x);
                                    ySeekBar.setProgress((int) finalShadowOffset.y);
                                }else {
                                    xSeekBar.setProgress(0);
                                    ySeekBar.setProgress(0);
                                }
                                xSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
                                ySeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
                                colorAdapter.setSelectPosition(currentIndex);
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
            AliyunColor color = aliyunPasterControllerCompoundCaption.getShadowColor();
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

    private final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (getCaptionChooserStateChangeListener() != null) {
                int x = xSeekBar.getProgress();
                int y = ySeekBar.getProgress();
                getCaptionChooserStateChangeListener().onCaptionTextShandowOffsetChanged(new PointF(x, y));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

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
