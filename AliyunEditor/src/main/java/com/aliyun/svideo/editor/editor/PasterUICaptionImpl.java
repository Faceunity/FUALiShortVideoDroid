/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.editor;

import android.graphics.Bitmap;
import android.view.TextureView;
import android.view.ViewGroup;

import com.aliyun.svideo.editor.editor.thumblinebar.OverlayThumbLineBar;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.svideo.editor.widget.BaseAliyunPasterView;
import com.aliyun.qupai.editor.AliyunIEditor;
import com.aliyun.qupai.editor.AliyunPasterController;

public class PasterUICaptionImpl extends PasterUIGifImpl {

    private int textCenterOffsetX, textCenterOffsetY;

    public PasterUICaptionImpl(BaseAliyunPasterView pasterView, AliyunPasterController controller,
                               OverlayThumbLineBar thumbLineBar, AliyunIEditor aliyunIEditor) {
        this(pasterView, controller, thumbLineBar);
        this.mAliyunIEditor = aliyunIEditor;
        mEditorPage = UIEditorPage.CAPTION;
    }

    public PasterUICaptionImpl(BaseAliyunPasterView pasterView, AliyunPasterController controller,
                               OverlayThumbLineBar thumbLineBar) {
        super(pasterView, controller, thumbLineBar);

        int textWidth = controller.getPasterTextWidth();
        int textHeight = controller.getPasterTextHeight();
        textCenterOffsetX = controller.getPasterTextOffsetX();
        textCenterOffsetY = controller.getPasterTextOffsetY();
        int width = controller.getPasterWidth();
        int height = controller.getPasterHeight();

        int left = textCenterOffsetX - textWidth / 2;
        int top = textCenterOffsetY - textHeight / 2;
        int right = width - textCenterOffsetX - textWidth / 2;
        int bottom = height - textCenterOffsetY - textHeight / 2;

        mText.setText(controller.getText());
        mText.setEditCompleted(true);
        mText.setTextStrokeColor(controller.getTextStrokeColor());
        mText.setCurrentColor(controller.getTextColor());
        mText.setFontPath(controller.getPasterTextFont());
        mText.setTextWidth(textWidth);
        mText.setTextHeight(textHeight);
        mText.setTextTop(top);
        mText.setTextLeft(left);
        mText.setTextRight(right);
        mText.setTextBottom(bottom);
        mText.setTextAngle(controller.getPasterTextRotation());
    }

    @Override
    public void mirrorPaster(boolean mirror) {
        mPasterView.setMirror(mirror);
        mText.setMirror(mirror);
        animPlayerView.setMirror(mirror);
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
        return super.getTextBgLabelColor();
    }

    @Override
    public Bitmap transToImage() {
        return mText.layoutToBitmap();
    }

    @Override
    public int getPasterTextOffsetX() {
        float[] scale = mPasterView.getScale();
        return (int) (textCenterOffsetX * scale[0]);
    }

    @Override
    public int getPasterTextOffsetY() {
        float[] scale = mPasterView.getScale();
        return (int) (textCenterOffsetY * scale[1]);
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
    public float getPasterTextRotation() {
        return mText.getTextRotation();
    }

    @Override
    protected void playPasterEffect() {
        TextureView pv = new TextureView(mPasterView.getContext());
        animPlayerView = mController.createPasterPlayer(pv);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        ViewGroup vg = (ViewGroup) mPasterView.getContentView();
        vg.addView(pv, 0, lp);

    }

    @Override
    protected void stopPasterEffect() {
        ViewGroup vg = (ViewGroup) mPasterView.getContentView();
        vg.removeViewAt(0);
        animPlayerView = null;
    }

}
