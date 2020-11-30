/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class AliyunPasterWithImageView extends BaseAliyunPasterView {

    public AliyunPasterWithImageView(Context context) {
        this(context, null);
    }

    public AliyunPasterWithImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AliyunPasterWithImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private int contentWidth;
    private int contentHeight;

    @Override
    public void setContentWidth(int contentWidth) {
        this.contentWidth = contentWidth;
    }

    @Override
    public void setContentHeight(int contentHeight) {
        this.contentHeight = contentHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        validateTransform();
        int width, height;
        mMatrixUtil.decomposeTSR(mTransform);

        width = (int) (mMatrixUtil.scaleX * contentWidth);
        height = (int) (mMatrixUtil.scaleY * contentHeight);
        Log.d("EDIT", "Measure width : " + width + "scaleX : "
              + " screen width : " + getWidth() + " 1/8 width : " + getWidth() / 8);
        int w = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int h = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(w, h);
    }

    private View mContentView;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mContentView = findViewById(android.R.id.content);
    }

    @Override
    public int getContentWidth() {
        //Auto-generated method stub
        return contentWidth;
    }

    @Override
    public int getContentHeight() {
        //Auto-generated method stub
        return contentHeight;
    }

    @Override
    public View getContentView() {
        //Auto-generated method stub
        return mContentView;
    }

}
