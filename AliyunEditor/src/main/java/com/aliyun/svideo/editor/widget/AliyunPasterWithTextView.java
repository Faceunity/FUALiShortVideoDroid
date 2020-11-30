/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.aliyun.svideo.editor.R;

public class AliyunPasterWithTextView extends BaseAliyunPasterView {

    private boolean isEditCompleted;
    private boolean isCouldShowLabel;

    public AliyunPasterWithTextView(Context context) {
        this(context, null);
    }

    public AliyunPasterWithTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AliyunPasterWithTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean isEditCompleted() {
        return isEditCompleted;
    }

    @Override
    public void setEditCompleted(boolean isEditCompleted) {
        int width = getContentWidth();
        int height = getContentHeight();
        this.isEditCompleted = isEditCompleted;
        if (width == 0 || height == 0) {
            return;
        }
        if (isEditCompleted) {
            //mContentWidth = width;
            //mContentHeight = height;

            //mMatrixUtil.decomposeTSR(mTransform);
            //float scaleX = 1 / mMatrixUtil.scaleX;
            //float scaleY = 1 / mMatrixUtil.scaleY;
            //mTransform.postScale(scaleX, scaleY);

            //if(width > getWidth()){
            //    float scale = (float)getWidth() / (float)width;
            //scale = scale == 0 ? 1 : scale;
            //    mTransform.postScale(scale, scale);
            //}

            requestLayout();
        }
        Log.d("EDIT", "EditCompleted : " + isEditCompleted + "mContentWidth : " + mContentWidth
              + " mContentHeight : " + mContentHeight);
    }

    @Override protected
    void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        validateTransform();
        mMatrixUtil.decomposeTSR(mTransform);
        if (isEditCompleted) {
            int width = (int) (mMatrixUtil.scaleX * mContentWidth);
            int height = (int) (mMatrixUtil.scaleY * mContentHeight);

            Log.d("EDIT", "Measure width : " + width + "scaleX : " + mMatrixUtil.scaleX +
                  "mContentWidth : " + mContentWidth
                  + " mContentHeight : " + mContentHeight);
            int w = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            int h = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            measureChildren(w, h);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            measureChildren(widthMeasureSpec, heightMeasureSpec);
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mTextLabel == null) {
            isCouldShowLabel = false;
            return ;
        }

        mMatrixUtil.decomposeTSR(mTransform);

        if (mMatrixUtil.getRotationDeg() == 0) {
            isCouldShowLabel = true;
            float[] center = getCenter();
            float cy = center[1];

            float hh = mMatrixUtil.scaleY * getContentHeight() / 2;

            mTextLabel.layout(0, (int)(cy - hh), getWidth(), (int)(cy + hh));
        } else {
            mTextLabel.layout(0, 0, 0, 0);
            isCouldShowLabel = false;
        }
    }

    private View mContentView;
    private View mTextLabel;

    private int mContentWidth;
    private int mContentHeight;

    @Override
    public void setContentWidth(int contentWidth) {
        this.mContentWidth = contentWidth;
    }

    @Override
    public void setContentHeight(int contentHeight) {
        this.mContentHeight = contentHeight;
    }

    @Override
    public boolean isCouldShowLabel() {
        return isCouldShowLabel;
    }

    @Override protected
    void onFinishInflate() {
        super.onFinishInflate();

        mContentView = findViewById(android.R.id.content);
        mTextLabel = findViewById(R.id.qupai_bg_overlay_text_label);
    }

    @Override
    public void setShowTextLabel(boolean isShow) {
        if (mTextLabel != null) {
            mTextLabel.setVisibility(isShow ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public View getTextLabel() {
        return mTextLabel;
    }

    @Override
    public int getContentWidth() {
        if (isEditCompleted) {
            return mContentWidth;
        }
        return mContentView.getMeasuredWidth();
    }

    @Override
    public int getContentHeight() {
        if (isEditCompleted) {
            return mContentHeight;
        }
        return mContentView.getMeasuredHeight();
    }

    @Override
    public View getContentView() {
        return mContentView;
    }

}
