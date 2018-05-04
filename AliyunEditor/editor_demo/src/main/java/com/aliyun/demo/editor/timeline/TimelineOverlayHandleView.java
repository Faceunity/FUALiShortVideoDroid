/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.editor.timeline;

import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;

public class TimelineOverlayHandleView implements View.OnTouchListener{
    interface OnPositionChangeListener{
        void onPositionChanged(float distance);

        void onChangeComplete();
    }
    private View mView;             //对应的View
    private long mDuration;         //所处的时长
    private OnPositionChangeListener mPositionChangeListener;
    private float mStartX;

    public TimelineOverlayHandleView(View view, long duration) {
        this.mView = view;
        if(mView != null) {
            this.mView.setOnTouchListener(this);
        }
        this.mDuration = duration;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int actionMasked = MotionEventCompat.getActionMasked(event);
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
                mStartX = event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getRawX() - mStartX;
                mStartX = event.getRawX();
                if(mPositionChangeListener != null) {
                    mPositionChangeListener.onPositionChanged(dx);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(mPositionChangeListener != null) {
                    mPositionChangeListener.onChangeComplete();
                }
                mStartX = 0;
                break;
            default:
                mStartX = 0;
        }
        return true;
    }

    public void setPositionChangeListener(OnPositionChangeListener positionChangeListener) {
        mPositionChangeListener = positionChangeListener;
    }

    public void active() {
        if(mView != null) {
            mView.setVisibility(View.VISIBLE);
        }
    }

    public void fix() {
        if(mView != null) {
            mView.setVisibility(View.INVISIBLE);
        }
    }


    public void changeDuration(long duration) {
        mDuration += duration;
    }

    public long getDuration() {
        return mDuration;
    }

    public View getView() {
        return mView;
    }
}

