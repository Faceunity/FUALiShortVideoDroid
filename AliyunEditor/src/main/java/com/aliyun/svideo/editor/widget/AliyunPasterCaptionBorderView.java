/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.bean.AlivcCaptionBorderBean;

public class AliyunPasterCaptionBorderView extends BaseAliyunPasterView {
    private float mCenterX;
    private float mCenterY;
    private float[] originalCenter;
    private boolean isFirstTouch = true;
    private OnCaptionControllerChangeListener mOnCaptionControllerChangeListener;

    public AliyunPasterCaptionBorderView(Context context) {
        this(context, null);
    }

    public AliyunPasterCaptionBorderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AliyunPasterCaptionBorderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public void setEditCompleted(boolean isEditCompleted) {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        validateTransform();
        mMatrixUtil.decomposeTSR(mTransform);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mMatrixUtil.decomposeTSR(mTransform);
    }

    private View mContentView;
    private int mContentWidth;
    private int mContentHeight;


    public void setContentSize(RectF contentSize) {
        if (contentSize == null) {
            return;
        }
        mContentWidth = (int) contentSize.width();
        mContentHeight = (int) contentSize.height();
        setContentSize(mContentWidth, mContentHeight);

    }

    private void setContentSize(int contentWidth, int contentHeight) {
        if (mContentView != null) {
            ViewGroup.LayoutParams layoutParams = mContentView.getLayoutParams();
            layoutParams.width = contentWidth;
            layoutParams.height = contentHeight;
            mContentView.setLayoutParams(layoutParams);
        }
    }

    @Override
    public void setContentWidth(int contentWidth) {
        this.mContentWidth = contentWidth;
    }

    @Override
    public void setContentHeight(int contentHeight) {
        this.mContentHeight = contentHeight;
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContentView = findViewById(android.R.id.content);
        View transform = findViewById(R.id.qupai_btn_edit_overlay_transform);
        if (transform != null) {
            transform.setOnTouchListener(mRotationScaleBinding);
        }
        setOnTouchListener(onTouchListener);
        setContentSize(mContentWidth, mContentHeight);
        if (mContentView instanceof AliyunPasterBorderControllerView) {
            ((AliyunPasterBorderControllerView) mContentView).setOnLayoutChangeListener(mContentLayoutChange);
        }
    }


    @Override
    public void setShowTextLabel(boolean isShow) {

    }

    @Override
    public View getTextLabel() {
        return null;
    }

    @Override
    public int getContentWidth() {
        return mContentView.getMeasuredWidth();
    }

    @Override
    public int getContentHeight() {
        return mContentView.getMeasuredHeight();
    }

    @Override
    public View getContentView() {
        return mContentView;
    }

    public interface OnCaptionControllerChangeListener {
        void onControllerChanged(float roation, float[] scale, int left, int top, int right, int bottom);
    }


    private AliyunPasterBorderControllerView.OnLayoutChangeListener mContentLayoutChange = new AliyunPasterBorderControllerView.OnLayoutChangeListener() {
        @Override
        public void onLayoutChanged(int left, int top, int right, int bottom) {
            if (mOnCaptionControllerChangeListener != null) {
                mOnCaptionControllerChangeListener.onControllerChanged(getRotation(), getScale(), left, top, right, bottom);
            }
        }
    };




    public void bind(final AlivcCaptionBorderBean captionSize, final OnCaptionControllerChangeListener onCaptionControllerChangeListener) {
        mOnCaptionControllerChangeListener = null;
        int width = getWidth();
        if (width == 0) {
            post(new Runnable() {
                @Override
                public void run() {
                    reSizeBorder(captionSize);
                }
            });
        } else {
            reSizeBorder(captionSize);
        }
        post(new Runnable() {
            @Override
            public void run() {
                mOnCaptionControllerChangeListener = onCaptionControllerChangeListener;
            }
        });


    }


    private void reSizeBorder(final AlivcCaptionBorderBean captionSize) {
        //数据清除
        mMatrixUtil.clear();
        Matrix matrix = getTransform();
        matrix.reset();
        if (getCenter() != null) {
            originalCenter = getCenter();
            isFirstTouch = false;
        }
        //设置尺寸
        int aimCx = (int) (captionSize.rectF.left + captionSize.rectF.width() / 2);
        int animCy = (int) (captionSize.rectF.top + captionSize.rectF.height() / 2);
        int baseWidth = getWidth() / 2;
        int baseHeight = getHeight() / 2;
        setContentSize(captionSize.rectF);
        int dx = aimCx - baseWidth;
        int dy = animCy - baseHeight;

        if (getWidth() != 0) {
            matrix.postTranslate(dx, dy);
        }
        scaleContent(captionSize.scale, captionSize.scale);
        float v = (float) (captionSize.roation );
        float[] center = getCenter();
        if (center != null) {
            mCenterX = center[0] - originalCenter[0];
            mCenterY = center[1] - originalCenter[1];
        }
        rotateContent(-v, mCenterX, mCenterY);
        invalidateTransform();
        setVisibility(View.VISIBLE);
        bringToFront();
        if (getWidth() == 0) {
            post(new Runnable() {
                @Override
                public void run() {
                    reSizeBorder(captionSize);
                }
            });
        }
    }


    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (isFirstTouch) {
                    originalCenter = getCenter();
                    isFirstTouch = false;
                }
                break;
            default:
                break;
            }
            return false;
        }
    };


    private final View.OnTouchListener mRotationScaleBinding = new View.OnTouchListener() {
        private float mLastX;
        private float mLastY;

        private void update(float x, float y) {

            View content = getContentView();
            float x0 = content.getLeft() + content.getWidth() / 2;
            float y0 = content.getTop() + content.getHeight() / 2;


            float dx = x - x0;
            float dy = y - y0;

            float dx0 = mLastX - x0;
            float dy0 = mLastY - y0;


            float scale = PointF.length(dx, dy) / Math.max(PointF.length(dx0, dy0), PointF.length(content.getWidth() / 2, content.getHeight() / 2));

            float rot = (float) (Math.atan2(y - y0, x - x0) - Math.atan2(mLastY
                                 - y0, mLastX - x0));

            if (Float.isInfinite(scale) || Float.isNaN(scale)
                    || Float.isInfinite(rot) || Float.isNaN(rot)) {
                return;
            }

            mLastX = x;
            mLastY = y;

            scaleContent(scale, scale);
            rotateContent(rot, mCenterX, mCenterY);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (isFirstTouch) {
                    originalCenter = getCenter();
                    isFirstTouch = false;
                }
                mLastX = v.getLeft() + event.getX();
                mLastY = v.getTop() + event.getY();

                float[] center = getCenter();
                mCenterX = center[0] - originalCenter[0];
                mCenterY = center[1] - originalCenter[1];
                break;
            case MotionEvent.ACTION_MOVE:
                update(v.getLeft() + event.getX(), v.getTop() + event.getY());
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
            }

            return true;
        }
    };


}
