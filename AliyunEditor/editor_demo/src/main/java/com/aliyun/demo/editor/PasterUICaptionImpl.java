/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.editor;

import android.graphics.Bitmap;
import android.view.TextureView;
import android.view.ViewGroup;

import com.aliyun.demo.editor.timeline.TimelineBar;
import com.aliyun.demo.widget.AliyunPasterView;
import com.aliyun.qupai.editor.AliyunPasterController;

public class PasterUICaptionImpl extends PasterUIGifImpl {

    private int textCenterOffsetX, textCenterOffsetY;
    private int textWidth, textHeight;
    public PasterUICaptionImpl(AliyunPasterView pasterView, AliyunPasterController controller, TimelineBar timelineBar) {
        super(pasterView, controller, timelineBar);

        textWidth = controller.getPasterTextWidth();
        textHeight = controller.getPasterTextHeight();
        textCenterOffsetX = controller.getPasterTextOffsetX();
        textCenterOffsetY = controller.getPasterTextOffsetY();
        int width = controller.getPasterWidth();
        int height = controller.getPasterHeight();

        int left = textCenterOffsetX - textWidth / 2;
        int top = textCenterOffsetY - textHeight / 2;
        int right = width - textCenterOffsetX - textWidth / 2;
        int bottom = height - textCenterOffsetY - textHeight / 2;
//        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)mText.getLayoutParams();
//        lp.leftMargin = textCenterOffsetX - textWidth / 2;
//        lp.topMargin = textCenterOffsetY - textHeight / 2;

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
        return (int)(textCenterOffsetX * scale[0]);
    }

    @Override
    public int getPasterTextOffsetY() {
        float[] scale = mPasterView.getScale();
        return (int)(textCenterOffsetY * scale[1]);
    }

    @Override
    public int getPasterTextWidth() {
        float[] scale = mPasterView.getScale();
        return (int)(textWidth * scale[0]);
    }

    @Override
    public int getPasterTextHeight() {
        float[] scale = mPasterView.getScale();
        return (int)(textHeight * scale[1]);
    }

    @Override
    public float getPasterTextRotation() {
        return mText.getTextRotation();
    }

    @Override
    protected void playPasterEffect() {
        TextureView pv = new TextureView(mPasterView.getContext());
        animPlayerView = mController.createPasterPlayer(pv);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
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
