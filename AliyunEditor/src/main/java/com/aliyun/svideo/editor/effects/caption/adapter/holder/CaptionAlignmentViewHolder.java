/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.caption.adapter.holder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.caption.listener.OnCaptionChooserStateChangeListener;
import com.aliyun.svideo.editor.effects.caption.manager.CaptionManager;
import com.aliyun.svideosdk.editor.impl.AliyunPasterControllerCompoundCaption;

public class CaptionAlignmentViewHolder extends BaseCaptionViewHolder {

    private RadioButton alignmentLeft;
    private RadioButton alignmentMiddle;
    private RadioButton alignmentRight;
    private int currentCaptionControlId;

    public CaptionAlignmentViewHolder(Context context, String title, OnCaptionChooserStateChangeListener onCaptionChooserStateChangeListener) {
        super(context, title, onCaptionChooserStateChangeListener);
    }

    @Override
    public View onCreateView(Context context) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.alivc_editor_caption_text_alignment_container, null, false);
        alignmentLeft = rootView.findViewById(R.id.btn_alignment_left);
        alignmentMiddle = rootView.findViewById(R.id.btn_alignment_middle);
        alignmentRight = rootView.findViewById(R.id.btn_alignment_right);
        return rootView;
    }

    @Override
    public void onBindViewHolder() {
        alignmentLeft.setOnCheckedChangeListener(onCheckedChangeListener);
        alignmentMiddle.setOnCheckedChangeListener(onCheckedChangeListener);
        alignmentRight.setOnCheckedChangeListener(onCheckedChangeListener);
        currentCaptionControlId = CaptionManager.getCaptionControllerId(getCaptionChooserStateChangeListener());
    }

    @Override
    public void notifyDataSetChanged() {
        if (alignmentLeft != null) {
            int captionControllerId = CaptionManager.getCaptionControllerId(getCaptionChooserStateChangeListener());
            if (currentCaptionControlId != captionControllerId) {
                AliyunPasterControllerCompoundCaption captionController = CaptionManager.getCaptionController(getCaptionChooserStateChangeListener());
                if (captionController != null) {
                    int textAlignment = captionController.getTextAlignment();
                    alignmentLeft.setOnCheckedChangeListener(null);
                    alignmentMiddle.setOnCheckedChangeListener(null);
                    alignmentRight.setOnCheckedChangeListener(null);
                    switch (textAlignment){
                        case AliyunPasterControllerCompoundCaption.AlignLeft:
                            alignmentLeft.setChecked(true);
                            alignmentMiddle.setChecked(false);
                            alignmentRight.setChecked(false);
                            break;
                        case AliyunPasterControllerCompoundCaption.AlignCenter:
                            alignmentLeft.setChecked(false);
                            alignmentMiddle.setChecked(true);
                            alignmentRight.setChecked(false);
                            break;
                        case AliyunPasterControllerCompoundCaption.AlignRight:
                            alignmentLeft.setChecked(false);
                            alignmentMiddle.setChecked(false);
                            alignmentRight.setChecked(true);
                            break;
                        default:
                            break;
                    }
                    alignmentLeft.setOnCheckedChangeListener(onCheckedChangeListener);
                    alignmentMiddle.setOnCheckedChangeListener(onCheckedChangeListener);
                    alignmentRight.setOnCheckedChangeListener(onCheckedChangeListener);
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
            if (isChecked){
                int id = buttonView.getId();
                int align = AliyunPasterControllerCompoundCaption.AlignLeft;
                if ( id== R.id.btn_alignment_left) {
                    align = AliyunPasterControllerCompoundCaption.AlignLeft;
                }else if (id ==R.id.btn_alignment_middle){
                    align = AliyunPasterControllerCompoundCaption.AlignCenter;
                }else if (id == R.id.btn_alignment_right){
                    align = AliyunPasterControllerCompoundCaption.AlignRight;
                }
                if (getCaptionChooserStateChangeListener() != null) {
                    getCaptionChooserStateChangeListener().onCaptionTextAlignmentChanged(align);
                }
            }


        }
    };

}
