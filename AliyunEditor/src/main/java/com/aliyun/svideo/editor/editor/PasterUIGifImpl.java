/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.editor;

import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.editor.thumblinebar.OverlayThumbLineBar;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.svideo.editor.widget.BaseAliyunPasterView;
import com.aliyun.svideosdk.common.struct.effect.EffectPaster;
import com.aliyun.svideosdk.editor.AliyunIEditor;
import com.aliyun.svideosdk.editor.AliyunPasterController;
import com.aliyun.svideosdk.common.struct.effect.ActionBase;
import com.aliyun.svideosdk.common.struct.effect.ActionTranslate;

import java.util.concurrent.TimeUnit;

public class PasterUIGifImpl extends AbstractPasterUISimpleImpl {
    protected AliyunIEditor mAliyunIEditor;

    public PasterUIGifImpl(BaseAliyunPasterView pasterView, AliyunPasterController controller, OverlayThumbLineBar thumbLineBar) {
        super(pasterView, controller, thumbLineBar);
        mEditorPage = UIEditorPage.OVERLAY;
        mText = pasterView.getContentView().findViewById(R.id.qupai_overlay_content_text);
        int width = controller.getPasterWidth();
        int height = controller.getPasterHeight();

        mPasterView.setContentWidth(width);
        mPasterView.setContentHeight(height);
        mirrorPaster(mController.isPasterMirrored());
        mPasterView.rotateContent(mController.getPasterRotation());
    }
    public PasterUIGifImpl(BaseAliyunPasterView pasterView, AliyunPasterController controller, OverlayThumbLineBar thumbLineBar, AliyunIEditor iEditor) {
        this(pasterView, controller, thumbLineBar);
        this.mAliyunIEditor = iEditor;
    }

    public void moveToCenter() {
        mMoveDelay = true;
        mPasterView.post(new Runnable() {
            @Override
            public void run() {
                EffectPaster paster = (EffectPaster) mController.getEffect();
                float cx = mController.getPasterCenterX();
                float cy = mController.getPasterCenterY();
                mPasterView.moveContent(cx - paster.displayWidth / 2, cy - paster.displayHeight / 2);
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
        if (mMoveDelay) {
            return 0;
        }
        float[] center = mPasterView.getCenter();
        if (center == null) {
            return 0;
        }
        float y = center[1];
        return (int)(y);
    }

    @Override
    public int getPasterCenterX() {
        if (mMoveDelay) {
            return 0;
        }
        float[] center = mPasterView.getCenter();
        if (center == null) {
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

    /**
     * 目前只有字幕支持动效
     */
    @Override
    public void editTimeCompleted() {
        if (!isEditStarted || !isPasterExists() || isPasterRemoved()) {
            return;
        }
        super.editTimeCompleted();
        //super.editTimeCompleted()可能会移除paster，这里还要再做一次判断
        if (!mController.isOnlyApplyUI() && !isPasterRemoved() && mAliyunIEditor != null) {
            if (mOldFrameAction != null) {
                mAliyunIEditor.removeFrameAnimation(mOldFrameAction);
                mOldFrameAction = null;
            }
            mFrameAction = mTempFrameAction;

            if (mFrameAction != null) {
                applyAnimation(mFrameAction);
            }
            mOldFrameAction = mFrameAction;
        }
        isEditStarted = false;
    }

    private void applyAnimation(ActionBase actionBase) {

        ActionTranslate actionTranslate = null;
        if (actionBase instanceof ActionTranslate) {
            ActionTranslate tempActionTranslate = ((ActionTranslate) actionBase);
            //如果已经处理过则不需要处理
            if (tempActionTranslate.getFromPointX() == 0
                    && tempActionTranslate.getFromPointY() == 0
                    && tempActionTranslate.getToPointX() == 0
                    && tempActionTranslate.getToPointY() == 0) {
                actionTranslate = new ActionTranslate();
            }

        }

        long pasterStartTimeInMills = mController.getPasterStartTime(TimeUnit.MILLISECONDS);
        //long pasterDuration = mController.getPasterDuration();
        long pasterDuration = 1000;

        actionBase.setTargetId(mController.getEffect().getViewId());
        actionBase.setStartTime(pasterStartTimeInMills, TimeUnit.MILLISECONDS);
        actionBase.setDuration(pasterDuration, TimeUnit.MILLISECONDS);
        if (actionTranslate != null) {
            actionTranslate.setTargetId(mController.getEffect().getViewId());
            actionTranslate.setStartTime(pasterStartTimeInMills, TimeUnit.MILLISECONDS);
            actionTranslate.setDuration(pasterDuration, TimeUnit.MILLISECONDS);
            setTranslateParams(actionBase, actionTranslate);
            mAliyunIEditor.addFrameAnimation(actionTranslate);
            mFrameAction = actionTranslate;
        } else {
            mAliyunIEditor.addFrameAnimation(actionBase);
        }

    }
    /**
     * 因为dialog中无法获取准确的位移位置，需要在这里对位移参数重新设定
     * @param actionBase ActionBase
     */
    private void setTranslateParams(ActionBase animation, ActionBase actionBase) {

        ActionTranslate actionTranslate = (ActionTranslate) actionBase;
        ViewParent parent = mPasterView.getParent();
        if (parent == null) {
            return;
        }

        float toPointX = ((ActionTranslate) animation).getToPointX();
        float toPointY = ((ActionTranslate) animation).getToPointY();
        float widthUnit = mPasterView.getWidth() / 2f;
        float heightUnit = mPasterView.getHeight() / 2f;
        float left = mPasterView.getContentView().getLeft();
        float right = mPasterView.getContentView().getRight();
        float top = mPasterView.getContentView().getTop();
        float bottom = mPasterView.getContentView().getBottom();

        float x = ((right + left) / 2 - widthUnit) / widthUnit;
        float y = -((top + bottom) / 2 - heightUnit) / heightUnit;
        float detY = 0;

        //出场
//        actionTranslate.setFromPointX(x);
//        actionTranslate.setFromPointY(y);
//        if (toPointX == 1f){
//            //向右平移
//            actionTranslate.setToPointY(y);
//            actionTranslate.setToPointX(1);
//        }else if (toPointX == -1f){
//            //向左平移
//            actionTranslate.setToPointY(y);
//            actionTranslate.setToPointX(-1);
//        }else if (toPointY == -1f){
//            //向下平移
//            actionTranslate.setToPointX(x);
//            actionTranslate.setToPointY(-1 + detY);
//        }else if (toPointY == 1f){
//            //向上平移
//            actionTranslate.setToPointX(x);
//            actionTranslate.setToPointY(1 + detY);
//        }

        //入场1s结束
        actionTranslate.setToPointX(x);
        actionTranslate.setToPointY(y);
        if (toPointX == 1f) {
            //向右平移
            actionTranslate.setFromPointY(y);
            actionTranslate.setFromPointX(-1);
        } else if (toPointX == -1f) {
            //向左平移
            actionTranslate.setFromPointY(y);
            actionTranslate.setFromPointX(1);
        } else if (toPointY == -1f) {
            //向下平移
            actionTranslate.setFromPointX(x);
            actionTranslate.setFromPointY(1 + detY);
        } else if (toPointY == 1f) {
            //向上平移
            actionTranslate.setFromPointX(x);
            actionTranslate.setFromPointY(-1f + detY);
        }

    }



}
