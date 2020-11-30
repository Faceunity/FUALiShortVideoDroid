/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class HorizontalScaleView extends View {

    private Rect mPreviousRect = new Rect();
    public static final int LAYOUT_LEFT_TO_RIGHT = 1 ;
    public static final int LAYOUT_RIGHT_TO_LEFT = 2;

    private int mLayoutMode = LAYOUT_LEFT_TO_RIGHT;

    public HorizontalScaleView(Context context) {
        super(context);
    }

    public HorizontalScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalScaleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mLayoutMode == LAYOUT_LEFT_TO_RIGHT) {
            super.onLayout(changed, left, top, right, bottom);
        }
        mPreviousRect.left = left;
        mPreviousRect.top = top;
        mPreviousRect.bottom = bottom;
        mPreviousRect.right = right;

    }

    public void setWidthRight(int width) {
        //左边不动，右边变化
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams != null) {
            layoutParams.width = width;
            setLayoutParams(layoutParams);
        }
    }

    public void setWidthLeft(int width) {
        //右边不动, 左边变化
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams != null) {
            layoutParams.width = width;
        }
    }


}
