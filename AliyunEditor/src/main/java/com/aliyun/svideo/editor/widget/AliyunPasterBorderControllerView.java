package com.aliyun.svideo.editor.widget;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class AliyunPasterBorderControllerView extends View {
    public AliyunPasterBorderControllerView(Context context) {
        super(context);
    }

    public AliyunPasterBorderControllerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AliyunPasterBorderControllerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mOnLayoutChangeListener != null) {
            mOnLayoutChangeListener.onLayoutChanged(left, top, right, bottom);
        }

    }

    @Override
    public void setRotation(float rotation) {
        super.setRotation(rotation);
    }

    @Override
    public void setScaleX(float scaleX) {
        super.setScaleX(scaleX);
    }

    public interface OnLayoutChangeListener {
        void onLayoutChanged(int left, int top, int right, int bottom);
    }

    private OnLayoutChangeListener mOnLayoutChangeListener;

    public void setOnLayoutChangeListener(OnLayoutChangeListener onLayoutChangeListener) {
        this.mOnLayoutChangeListener = onLayoutChangeListener;
    }
}
