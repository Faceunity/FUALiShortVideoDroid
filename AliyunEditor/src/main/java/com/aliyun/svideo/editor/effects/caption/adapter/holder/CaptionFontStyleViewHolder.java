/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.caption.adapter.holder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.caption.adapter.holder.BaseCaptionViewHolder;
import com.aliyun.svideo.editor.effects.caption.manager.CaptionManager;
import com.aliyun.svideo.editor.effects.caption.listener.OnCaptionChooserStateChangeListener;
import com.aliyun.svideosdk.common.AliyunFontStyle;
import com.aliyun.svideosdk.common.AliyunTypeface;
import com.aliyun.svideosdk.editor.impl.AliyunPasterControllerCompoundCaption;

public class CaptionFontStyleViewHolder extends BaseCaptionViewHolder {


    private CheckBox checkBoxFontItalic;
    private CheckBox checkBoxFontWeight;
    private int currentCaptionControlId;

    public CaptionFontStyleViewHolder(Context context, String title, OnCaptionChooserStateChangeListener onCaptionChooserStateChangeListener) {
        super(context, title, onCaptionChooserStateChangeListener);
    }

    @Override
    public View onCreateView(Context context) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.alivc_editor_caption_font_style_container, null, false);
        checkBoxFontItalic = rootView.findViewById(R.id.btn_font_italic);
        checkBoxFontWeight = rootView.findViewById(R.id.btn_font_weight);
        return rootView;
    }

    @Override
    public void onBindViewHolder() {
        checkBoxFontItalic.setOnCheckedChangeListener(onCheckedChangeListener);
        checkBoxFontWeight.setOnCheckedChangeListener(onCheckedChangeListener);
        currentCaptionControlId = CaptionManager.getCaptionControllerId(getCaptionChooserStateChangeListener());
    }

    @Override
    public void notifyDataSetChanged() {
        if (checkBoxFontItalic != null) {
            int captionControllerId = CaptionManager.getCaptionControllerId(getCaptionChooserStateChangeListener());
            if (currentCaptionControlId != captionControllerId) {
                AliyunPasterControllerCompoundCaption captionController = CaptionManager.getCaptionController(getCaptionChooserStateChangeListener());
                if (captionController != null) {
                    AliyunTypeface typeface = captionController.getFontTypeface();
                    if (typeface != null) {
                        checkBoxFontItalic.setOnCheckedChangeListener(null);
                        checkBoxFontWeight.setOnCheckedChangeListener(null);
                        switch (typeface) {
                        case NORMAL:
                            checkBoxFontItalic.setChecked(false);
                            checkBoxFontWeight.setChecked(false);
                            break;
                        case BOLD_ITALIC:
                            checkBoxFontItalic.setChecked(true);
                            checkBoxFontWeight.setChecked(true);
                            break;
                        case ITALIC:
                            checkBoxFontItalic.setChecked(true);
                            checkBoxFontWeight.setChecked(false);
                            break;
                        case BOLD:
                            checkBoxFontItalic.setChecked(false);
                            checkBoxFontWeight.setChecked(true);
                            break;
                        default:
                            break;
                        }
                    }
                    checkBoxFontItalic.setOnCheckedChangeListener(onCheckedChangeListener);
                    checkBoxFontWeight.setOnCheckedChangeListener(onCheckedChangeListener);
                    currentCaptionControlId = captionControllerId;
                }

            }
        }

    }

    @Override
    public void onTabClick() {
        notifyDataSetChanged();
    }

    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            boolean italicChecked = checkBoxFontItalic.isChecked();
            boolean weightChecked = checkBoxFontWeight.isChecked();
            AliyunTypeface aliyunTypeface = AliyunTypeface.NORMAL;
            if (italicChecked) {
                aliyunTypeface = AliyunTypeface.ITALIC;
            }
            if (weightChecked) {
                aliyunTypeface = AliyunTypeface.BOLD;
            }
            if (italicChecked && weightChecked) {
                aliyunTypeface = AliyunTypeface.BOLD_ITALIC;
            }
            if (getCaptionChooserStateChangeListener() != null) {
                getCaptionChooserStateChangeListener().onCaptionTextFontTypeFaceChanged(aliyunTypeface);
            }

        }
    };

}
