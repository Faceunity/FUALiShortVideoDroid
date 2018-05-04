/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.editor;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import android.view.ViewGroup;

import android.view.ViewParent;

import com.aliyun.demo.editor.timeline.TimelineBar;
import com.aliyun.demo.editor.timeline.TimelineOverlay;
import com.aliyun.demo.widget.AutoResizingTextView;
import com.aliyun.demo.widget.AliyunPasterView;
import com.aliyun.qupai.editor.AliyunPasterBaseView;
import com.aliyun.qupai.editor.AliyunPasterController;
import com.aliyun.qupai.editor.pplayer.AnimPlayerView;
import com.aliyun.struct.effect.EffectPaster;


public class PasterUISimpleImpl implements AliyunPasterBaseView {

    protected AutoResizingTextView mText;
    protected AliyunPasterView mPasterView;
    protected AnimPlayerView animPlayerView;
    protected AliyunPasterController mController;
    protected TimelineBar mTimelineBar;
    protected TimelineOverlay mTimelineOverlay;
    private boolean isDeleted;
    private boolean isEditStarted;
    protected boolean mMoveDelay;
    public PasterUISimpleImpl(AliyunPasterView pasterView, AliyunPasterController controller, TimelineBar timelineBar){
        mPasterView = pasterView;
        mController = controller;
        this.mTimelineBar = timelineBar;

        pasterView.setTag(this);
        mController.setPasterView(this);

        View transform = mPasterView.findViewById(R.id.qupai_btn_edit_overlay_transform);

        if(transform != null){
            View.OnTouchListener rotationScaleBinding = new View.OnTouchListener() {
                private float mLastX;
                private float mLastY;

                private void update(float x, float y) {

                    View content = mPasterView.getContentView();
                    float x0 = content.getLeft() + content.getWidth() / 2;
                    float y0 = content.getTop() + content.getHeight() / 2;

                    float dx = x - x0;
                    float dy = y - y0;
                    float dx0 = mLastX - x0;
                    float dy0 = mLastY - y0;

                    float scale = PointF.length(dx, dy) / PointF.length(dx0, dy0);

                    float rot = (float) (Math.atan2(y - y0, x - x0) - Math.atan2(mLastY
                            - y0, mLastX - x0));

                    if (Float.isInfinite(scale) || Float.isNaN(scale)
                            || Float.isInfinite(rot) || Float.isNaN(rot)) {
                        return;
                    }

                    mLastX = x;
                    mLastY = y;

                    mPasterView.scaleContent(scale, scale);
                    mPasterView.rotateContent(rot);
                }

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            mLastX = v.getLeft() + event.getX();
                            mLastY = v.getTop() + event.getY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            update(v.getLeft() + event.getX(), v.getTop() + event.getY());
                            break;
                        case MotionEvent.ACTION_CANCEL:
                        case MotionEvent.ACTION_UP:
                            break;
                    }

                    return true;
                }
            };
            transform.setOnTouchListener(rotationScaleBinding);
        }

        View cancel = pasterView.findViewById(R.id.qupai_btn_edit_overlay_cancel);
        if(cancel != null){
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePaster();
                }
            });
        }

        View textEdit = pasterView.findViewById(R.id.qupai_btn_edit_overlay_text);
        if(textEdit != null){
            textEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTextEdit();
                }
            });
        }

        View mirror = pasterView.findViewById(R.id.qupai_btn_edit_overlay_mirror);
        if(mirror != null){
            mirror.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isMirror = mPasterView.isMirror();
                    mirrorPaster(!isMirror);
                }
            });
        }

        editTimeStart();
    }

    public void moveToCenter(){

    }

    public void mirrorPaster(boolean mirror){
        mPasterView.setMirror(mirror);
    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public int getTextColor() {
        return 0;
    }

    @Override
    public String getPasterTextFont() {
        return null;
    }

    @Override
    public int getTextStrokeColor() {
        return 0;
    }

    @Override
    public boolean isTextHasStroke() {
        return false;
    }

    @Override
    public boolean isTextHasLabel() {
        return false;
    }

    @Override
    public int getTextBgLabelColor() {
        return 0;
    }

    @Override
    public int getPasterTextOffsetX() {
        return 0;
    }

    @Override
    public int getPasterTextOffsetY() {
        return 0;
    }

    @Override
    public int getPasterTextWidth() {
        return 0;
    }

    @Override
    public int getPasterTextHeight() {
        return 0;
    }

    @Override
    public float getPasterTextRotation() {
        return 0;
    }

    @Override
    public int getPasterWidth() {
        return 0;
    }

    @Override
    public int getPasterHeight() {
        return 0;
    }

    @Override
    public int getPasterCenterY() {
        return 0;
    }

    @Override
    public int getPasterCenterX() {
        return 0;
    }

    @Override
    public float getPasterRotation() {
        return 0;
    }

    @Override
    public Bitmap transToImage() {
        return null;
    }

    @Override
    public View getPasterView() {
        return mPasterView;
    }

    @Override
    public boolean isPasterMirrored(){
        return mPasterView.isMirror();
    }

    public void removePaster(){
        isDeleted = true;
        mController.removePaster();
        ViewParent parent = mPasterView.getParent();
        if(parent != null) {
            ((ViewGroup)parent).removeView(mPasterView);
        }
        mTimelineBar.removeOverlay(mTimelineOverlay);
    }

    public boolean isPasterRemoved(){
        return isDeleted;
    }

    public boolean isPasterExists(){
        return mController.isPasterExists();
    }

    public void editTimeStart(){
        if(isEditStarted){
            return ;
        }
        isEditStarted = true;
        mPasterView.setVisibility(View.VISIBLE);
        mPasterView.bringToFront();
        playPasterEffect();

        mController.editStart();
        if(mTimelineOverlay != null) {
            mTimelineOverlay.switchState(TimelineOverlay.STATE_ACTIVE);
        }
    }

    protected void playPasterEffect(){

    }

    protected void stopPasterEffect(){

    }

    public void editTimeCompleted(){
        if(!isEditStarted || !isPasterExists() || isPasterRemoved()){
            return ;
        }
        isEditStarted = false;
        mPasterView.setVisibility(View.GONE);
        stopPasterEffect();

        mController.editCompleted();
        mMoveDelay = false;
        if(mTimelineOverlay != null) {
            mTimelineOverlay.switchState(TimelineOverlay.STATE_FIX);
        }
    }

    public boolean isEditCompleted(){
        return !isPasterRemoved() && !isEditStarted;
    }

    public boolean contentContains(float x, float y){
        Log.e("TVT", "ontouch contentContains : " + mPasterView.contentContains(x, y));
        return mPasterView.contentContains(x, y);
    }

    public void moveContent(float dx, float dy){
        mPasterView.moveContent(dx, dy);
    }

    public boolean isVisibleInTime(long time){
        Log.e("TVT", "ontouch aliyun_svideo_play time : " + time);
        long start = mController.getPasterStartTime();
        long duration = mController.getPasterDuration();
        Log.e("TVT", "paster start time : " + start + " end time : " + (start + duration));
        return time >= start &&
                time <= start + duration;
    }

    public void showTextEdit(){
        if(mText == null){
            return ;
        }
        mText.setEditCompleted(true);
        mPasterView.setEditCompleted(true);
        TextDialog.EditTextInfo info = new TextDialog.EditTextInfo();
        info.dTextColor = mController.getConfigTextColor();
        info.dTextStrokeColor = mController.getConfigTextStrokeColor();
        info.isTextOnly = mController.getPasterType() == EffectPaster.PASTER_TYPE_TEXT;
        info.text = mText.getText().toString();
        info.textColor = mText.getCurrentTextColor();
        info.textStrokeColor = mText.getTextStrokeColor();
        info.font = mText.getFontPath();
        if(info.isTextOnly){
            info.textWidth = getPasterWidth();
            info.textHeight = getPasterHeight();
        }else{
            info.textWidth = getPasterTextWidth();
            info.textHeight = getPasterTextHeight();
        }

        ((ViewGroup)mPasterView.getParent()).setEnabled(false);
        mPasterView.setVisibility(View.GONE);
        TextDialog textDialog = TextDialog.newInstance(info);
        textDialog.setOnStateChangeListener(new TextDialog.OnStateChangeListener() {
            @Override
            public void onTextEditCompleted(TextDialog.EditTextInfo result) {
                ViewGroup vg = (ViewGroup)mPasterView.getParent();
                if(vg == null){
                    return ;
                }
                vg.setEnabled(true);
                if(TextUtils.isEmpty(result.text)){
                    removePaster();
                    return ;
                }
//                mPasterView.setVisibility(View.VISIBLE);
                mText.setText(result.text);
                mText.setCurrentColor(result.textColor);
                mText.setTextStrokeColor(result.textStrokeColor);
                if(result.isTextOnly){
                    mPasterView.setContentWidth(result.textWidth);
                    mPasterView.setContentHeight(result.textHeight);
                }
                mText.setFontPath(result.font);

                mText.setEditCompleted(true);
                mPasterView.setEditCompleted(true);
                if(isEditStarted){
                    mPasterView.setVisibility(View.VISIBLE);
                }
            }
        });

        textDialog.show(((Activity)mPasterView.getContext()).getFragmentManager(), "textedit");
    }

    public void showTimeEdit(){
        if(!isPasterExists()){
            return;
        }
        if(mTimelineOverlay == null) {
            TimelineOverlay.TimelineOverlayView overlayView = new TimelineOverlay.TimelineOverlayView() {
                private View rootView = LayoutInflater.from(mPasterView.getContext()).inflate(R.layout.aliyun_svideo_layout_timeline_overlay, null);

                @Override
                public ViewGroup getContainer() {
                    return (ViewGroup) rootView;
                }

                @Override
                public View getHeadView() {
                    return rootView.findViewById(R.id.head_view);
                }

                @Override
                public View getTailView() {
                    return rootView.findViewById(R.id.tail_view);
                }

                @Override
                public View getMiddleView() {
                    return rootView.findViewById(R.id.middle_view);
                }
            };
            mTimelineOverlay = mTimelineBar.addOverlay(mController.getPasterStartTime(),
                    mController.getPasterDuration(),
                    overlayView, 0);

            mTimelineOverlay.setOnSelectedDurationChangeListener(new TimelineOverlay.OnSelectedDurationChangeListener() {
                @Override
                public void onDurationChange(long startTime, long endTime, long duration) {
                    mController.setPasterStartTime(startTime);
                    mController.setPasterDuration(duration);
                    if(animPlayerView != null){
                        animPlayerView.setPlayTime(startTime, endTime);
                    }
                }
            });
        }
        mTimelineOverlay.switchState(TimelineOverlay.STATE_ACTIVE);
    }

//    public void setImageView(ImageView imageView) {
//        if(mText != null) {
//            mText.setImageView(imageView);
//        }
//    }

}

