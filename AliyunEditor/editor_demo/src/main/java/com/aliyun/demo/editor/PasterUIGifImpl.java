/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.editor;

import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.demo.editor.timeline.TimelineBar;
import com.aliyun.demo.widget.AutoResizingTextView;
import com.aliyun.demo.widget.AliyunPasterView;
import com.aliyun.qupai.editor.AliyunPasterController;

public class PasterUIGifImpl extends PasterUISimpleImpl {

    public PasterUIGifImpl(AliyunPasterView pasterView, AliyunPasterController controller, TimelineBar timelineBar) {
        super(pasterView, controller, timelineBar);

        mText = (AutoResizingTextView)pasterView.getContentView().findViewById(R.id.qupai_overlay_content_text);
        int width = controller.getPasterWidth();
        int height = controller.getPasterHeight();

        mPasterView.setContentWidth(width);
        mPasterView.setContentHeight(height);
        mirrorPaster(mController.isPasterMirrored());
        mPasterView.rotateContent(mController.getPasterRotation());
    }

    @Override
    public void moveToCenter() {
        mMoveDelay = true;
        mPasterView.post(new Runnable() {
            @Override
            public void run() {
                int cx = mController.getPasterCenterX();
                int cy = mController.getPasterCenterY();
                int pcx = ((ViewGroup)mPasterView.getParent()).getWidth();
                int pcy = ((ViewGroup)mPasterView.getParent()).getHeight();
                mPasterView.moveContent(cx - pcx / 2, cy - pcy / 2);
            }
        });
    }

    @Override
    public int getPasterWidth() {
        float[] scale = mPasterView.getScale();
        float scaleX = scale[0];
        int width = mPasterView.getContentWidth();
        return (int)(width * scaleX);
    }

    @Override
    public int getPasterHeight() {
        float[] scale = mPasterView.getScale();
        float scaleY = scale[1];
        int height = mPasterView.getContentHeight();
        return (int)(height * scaleY);
    }

    @Override
    public int getPasterCenterY() {
        if(mMoveDelay){
            return 0;
        }
        float[] center = mPasterView.getCenter();
        if(center == null){
            return 0;
        }
        float y = center[1];
        return (int)(y);
    }

    @Override
    public int getPasterCenterX() {
        if(mMoveDelay){
            return 0;
        }
        float[] center = mPasterView.getCenter();
        if(center == null){
            return 0;
        }
        float x = center[0];
        return (int)(x);
    }

    @Override
    public void mirrorPaster(boolean mirror) {
        super.mirrorPaster(mirror);
        animPlayerView.setMirror(mirror);
    }

    @Override
    protected void playPasterEffect() {
        TextureView pv = new TextureView(mPasterView.getContext());
        animPlayerView = mController.createPasterPlayer(pv);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ViewGroup vg = (ViewGroup) mPasterView.getContentView();
        vg.addView(pv, lp);
    }

    @Override
    protected void stopPasterEffect() {
        ViewGroup vg = (ViewGroup) mPasterView.getContentView();
        vg.removeAllViews();
        animPlayerView = null;
    }

    @Override
    public float getPasterRotation() {
        return mPasterView.getRotation();
    }

    @Override
    public View getPasterView() {
        return mPasterView;
    }
}
