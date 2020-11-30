/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.editor;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.Layout;

import com.aliyun.svideo.editor.editor.thumblinebar.OverlayThumbLineBar;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.svideo.editor.widget.AutoResizingTextView;
import com.aliyun.svideo.editor.widget.BaseAliyunPasterView;
import com.aliyun.qupai.editor.AliyunIEditor;
import com.aliyun.qupai.editor.AliyunPasterController;

public class PasterUITextImpl extends PasterUIGifImpl {

    public PasterUITextImpl(BaseAliyunPasterView pasterView, AliyunPasterController controller, OverlayThumbLineBar thumbLineBar, AliyunIEditor editor, boolean completed) {
        super(pasterView, controller, thumbLineBar);
        mEditorPage = UIEditorPage.FONT;
        mAliyunIEditor = editor;
        if (mText == null) {
            mText = (AutoResizingTextView) mPasterView.getContentView();
        }

        mText.setText(controller.getText());
        mText.setTextOnly(true);
        mText.setFontPath(controller.getPasterTextFont());
        mText.setTextAngle(controller.getPasterTextRotation());
        mText.setTextStrokeColor(controller.getTextStrokeColor());
        mText.setCurrentColor(controller.getTextColor());
        if (completed) {
            mText.setTextWidth(controller.getPasterWidth());
            mText.setTextHeight(controller.getPasterHeight());
            mText.setEditCompleted(true);
            pasterView.setEditCompleted(true);
        } else {
            mText.setEditCompleted(false);
            pasterView.setEditCompleted(false);
        }

    }

    @Override
    public void mirrorPaster(boolean mirror) {

    }

    @Override
    protected void playPasterEffect() {

    }

    @Override
    protected void stopPasterEffect() {

    }

    @Override
    public String getText() {
        return mText.getText().toString();
    }

    @Override
    public int getTextColor() {
        return mText.getTextColor();
    }

    @Override
    public String getPasterTextFont() {
        return mText.getFontPath();
    }

    @Override
    public int getTextStrokeColor() {
        return mText.getTextStrokeColor();
    }

    @Override
    public boolean isTextHasStroke() {
        return getTextStrokeColor() == 0;
    }

    @Override
    public boolean isTextHasLabel() {
        return mPasterView.getTextLabel() != null;
    }

    @Override
    public int getTextBgLabelColor() {
        return Color.parseColor("#00000000");
    }

    @Override
    public Bitmap getBackgroundBitmap() {
        return null;
    }

    @Override
    public Bitmap transToImage() {
        return mText.layoutToBitmap();
    }

    @Override
    public int getPasterTextWidth() {
        return mText.getTextWidth();
    }

    @Override
    public int getPasterTextHeight() {
        return mText.getTextHeight();
    }

    @Override
    public int getTextFixSize() {
        return 0;
    }

    @Override
    public int getTextPaddingX() {
        return 0;
    }

    @Override
    public int getTextPaddingY() {
        return 0;
    }

    @Override
    public Layout.Alignment getTextAlign() {
        Layout layout = mText.getLayout();
        if (layout != null) {
            return layout.getAlignment();
        }
        return Layout.Alignment.ALIGN_CENTER;
    }

}
