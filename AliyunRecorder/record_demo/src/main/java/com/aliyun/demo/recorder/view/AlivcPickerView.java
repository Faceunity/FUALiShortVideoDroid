package com.aliyun.demo.recorder.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.aliyun.demo.recorder.util.ColorUtil;

import java.util.ArrayList;
import java.util.List;

public class AlivcPickerView extends View {
    private TextPaint mPaint;
    private List<String> mData;
    private int mVisibleItemCount = 3; // 可见的item数量
    private int mTextSize = 28;
    private int mSelected; // 当前选中的item下标
    private int mItemWidth = 0; // 每个条目的宽度，当水平滚动时，宽度=mMeasureWidth／mVisibleItemCount
    private int mItemHeight = 0; // 每个条目的高度,当垂直滚动时，高度=mMeasureHeight／mVisibleItemCount
    private int mCenterPosition = -1; // 中间item的位置，0<=mCenterPosition＜mVisibleItemCount，默认为 mVisibleItemCount / 2
    private int mCenterX; // 中间item的起始坐标x(不考虑偏移),当垂直滚动时，x = mCenterPosition*mItemWidth
    private int mCenterY; // 中间item的起始坐标y(不考虑偏移),当垂直滚动时，y= mCenterPosition*mItemHeight
    private float mLastMoveX; // 触摸的坐标X
    private float mLastMoveY; // 触摸的坐标y
    private float mMoveLength = 0; // item移动长度，负数表示向上移动，正数表示向下移动
    private OnSelectedListener mListener;
    private Scroller mScroller;
    private boolean mIsMovingCenter; // 是否正在滑向中间
    private Drawable mCenterItemBackground = null; // 中间选中item的背景色
    private boolean mDisallowTouch = false; // 不允许触摸
    private ValueAnimator mAutoScrollAnimator;
    private int mLastScrollY = 0; // Scroller的坐标y
    private int mLastScrollX = 0; // Scroller的坐标x

    private Layout.Alignment mAlignment = Layout.Alignment.ALIGN_CENTER; // 对齐方式,默认居中
    // 字体渐变颜色
    private int mStartColor = Color.WHITE; // 中间选中ｉｔｅｍ的颜色
    private int mEndColor = Color.WHITE; // 上下两边的颜色

    public AlivcPickerView(Context context) {
        super(context);
    }

    public AlivcPickerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlivcPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(getContext());
        mAutoScrollAnimator = ValueAnimator.ofInt(0, 0);

        mPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mData == null || mData.size() <= 0) {
            return;
        }
        // 选中item的背景色
        if (mCenterItemBackground != null) {
            mCenterItemBackground.draw(canvas);
        }
        // 只绘制可见的item
        int length = Math.max(mCenterPosition + 1, mVisibleItemCount - mCenterPosition);
        int position;
        int start = Math.min(length, mData.size());
        for (int i = start; i >= 1; i--) {
            if (i <= mCenterPosition + 1 && mSelected - i >= 0) {
                position = mSelected - i < 0 ? mData.size() + mSelected - i
                    : mSelected - i;
                drawItem(canvas, position, -i, mMoveLength, mCenterX + mMoveLength - i * mItemWidth);
            }
            if (i <= mVisibleItemCount - mCenterPosition && mSelected + i < mData.size()) {
                position = mSelected + i >= mData.size() ? mSelected + i
                    - mData.size() : mSelected + i;
                drawItem(canvas, position, i, mMoveLength, mCenterX + mMoveLength + i * mItemWidth);
            }
        }
        // 选中的item
        drawItem(canvas, mSelected, 0, mMoveLength, mCenterX + mMoveLength);
    }

    /**
     * 绘制子view
     *
     * @param canvas
     * @param position
     * @param relative
     * @param moveLength
     * @param top
     */
    private void drawItem(Canvas canvas, int position, int relative, float moveLength, float top) {
        String text = mData.get(position);
        mPaint.setTextSize(mTextSize);
        StaticLayout layout = new StaticLayout(text, 0, text.length(), mPaint, mItemWidth, mAlignment, 1.0F, 0.0F, true,
            null, 0);
        float x = 0;
        float y = 0;
        float lineWidth = layout.getWidth();
        x = top + (mItemWidth - lineWidth) / 2;
        y = (mItemHeight - layout.getHeight()) / 2;

        // 计算渐变颜色
        computeColor(relative, mItemWidth, moveLength);
        canvas.save();
        canvas.translate(100, 100);
        layout.draw(canvas);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mDisallowTouch) { // 不允许触摸
            return true;
        }
        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_UP:
                mLastMoveY = event.getY();
                mLastMoveX = event.getX();

                //moveToCenter(); // 滚动到中间位置

                break;
            default:
                break;
        }
        return true;
    }

    //// 移动到中间位置
    //private void moveToCenter() {
    //
    //    if (!mScroller.isFinished()) {
    //        return;
    //    }
    //    cancelScroll();
    //
    //    // 向下滑动
    //    if (mMoveLength > 0) {
    //
    //            if (mMoveLength < mItemHeight / 2) {
    //                scroll(mMoveLength, 0);
    //            } else {
    //                scroll(mMoveLength, mItemHeight);
    //            }
    //
    //    } else {
    //
    //            if (-mMoveLength < mItemHeight / 2) {
    //                scroll(mMoveLength, 0);
    //            } else {
    //                scroll(mMoveLength, -mItemHeight);
    //            }
    //
    //    }
    //}
    //public void cancelScroll() {
    //    mLastScrollY = 0;
    //    mLastScrollX = 0;
    //    mIsFling = mIsMovingCenter = false;
    //    mScroller.abortAnimation();
    //    stopAutoScroll();
    //}
    public void setCenterItemBackground(Drawable centerItemBackground) {
        mCenterItemBackground = centerItemBackground;
        mCenterItemBackground.setBounds(mCenterX, mCenterY, mCenterX + mItemWidth, mCenterY + mItemHeight);
        invalidate();
    }

    public void setCenterItemBackground(int centerItemBackgroundColor) {
        mCenterItemBackground = new ColorDrawable(centerItemBackgroundColor);
        mCenterItemBackground.setBounds(mCenterX, mCenterY, mCenterX + mItemWidth, mCenterY + mItemHeight);
        invalidate();
    }

    public interface OnSelectedListener {
        void onSelected(AlivcPickerView pickerView, int position);
    }

    public void setVisibleItemCount(int visibleItemCount) {
        mVisibleItemCount = visibleItemCount;
        reset();
        invalidate();
    }

    private void reset() {
        if (mCenterPosition < 0) {
            mCenterPosition = mVisibleItemCount / 2;
        }

        mItemHeight = getMeasuredHeight();
        mItemWidth = getMeasuredWidth() / mVisibleItemCount;

        mCenterY = 0;
        mCenterX = mCenterPosition * mItemWidth;
        if (mCenterItemBackground != null) {
            mCenterItemBackground.setBounds(mCenterX, mCenterY, mCenterX + mItemWidth, mCenterY + mItemHeight);
        }

    }

    private boolean mIsAutoScrolling = false;
    ///**
    // * @param endY         　需要滚动到的位置
    // * @param duration     　滚动时间
    // * @param interpolator
    // * @param canIntercept 能否终止滚动，比如触摸屏幕终止滚动
    // */
    //public void autoScrollTo(final int endY, long duration, final Interpolator interpolator, boolean canIntercept) {
    //    if (mIsAutoScrolling) {
    //        return;
    //    }
    //    final boolean temp = mDisallowTouch;
    //    mDisallowTouch = !canIntercept;
    //    mIsAutoScrolling = true;
    //    mAutoScrollAnimator.cancel();
    //    mAutoScrollAnimator.setIntValues(0, endY);
    //    mAutoScrollAnimator.setInterpolator(interpolator);
    //    mAutoScrollAnimator.setDuration(duration);
    //    mAutoScrollAnimator.removeAllUpdateListeners();
    //    mAutoScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
    //        @Override
    //        public void onAnimationUpdate(ValueAnimator animation) {
    //            float rate = 0;
    //            rate = animation.getCurrentPlayTime() * 1f / animation.getDuration();
    //            computeScroll((int) animation.getAnimatedValue(), endY, rate);
    //        }
    //    });
    //    mAutoScrollAnimator.removeAllListeners();
    //    mAutoScrollAnimator.addListener(new AnimatorListenerAdapter() {
    //        @Override
    //        public void onAnimationEnd(Animator animation) {
    //            super.onAnimationEnd(animation);
    //            mIsAutoScrolling = false;
    //            mDisallowTouch = temp;
    //        }
    //    });
    //    mAutoScrollAnimator.start();
    //}
    ///**
    // * @param curr
    // * @param end
    // */
    //private void computeScroll(int curr, int end, float rate) {
    //    if (rate < 1) { // 正在滚动
    //
    //            // 可以把scroller看做模拟的触屏滑动操作，mLastScrollX为上次滑动的坐标
    //            mMoveLength = mMoveLength + curr - mLastScrollX;
    //            mLastScrollX = curr;
    //
    //        checkCirculation();
    //        invalidate();
    //    } else { // 滚动完毕
    //        mIsMovingCenter = false;
    //        mLastScrollY = 0;
    //        mLastScrollX = 0;
    //
    //        // 直接居中，不通过动画
    //        if (mMoveLength > 0) { //// 向下滑动
    //            if (mMoveLength < mItemSize / 2) {
    //                mMoveLength = 0;
    //            } else {
    //                mMoveLength = mItemSize;
    //            }
    //        } else {
    //            if (-mMoveLength < mItemSize / 2) {
    //                mMoveLength = 0;
    //            } else {
    //                mMoveLength = -mItemSize;
    //            }
    //        }
    //        checkCirculation();
    //        notifySelected();
    //        invalidate();
    //    }
    //
    //}

    /**
     * 计算字体颜色，渐变
     *
     * @param relative 　相对中间item的位置
     */
    private void computeColor(int relative, int itemSize, float moveLength) {

        int color = mEndColor; // 　其他默认为ｍEndColor

        if (relative == -1 || relative == 1) { // 上一个或下一个
            // 处理上一个item且向上滑动　或者　处理下一个item且向下滑动　，颜色为mEndColor
            if ((relative == -1 && moveLength < 0)
                || (relative == 1 && moveLength > 0)) {
                color = mEndColor;
            } else { // 计算渐变的颜色
                float rate = (itemSize - Math.abs(moveLength))
                    / itemSize;
                color = ColorUtil.computeGradientColor(mStartColor, mEndColor, rate);
            }
        } else if (relative == 0) { // 中间item
            float rate = Math.abs(moveLength) / itemSize;
            color = ColorUtil.computeGradientColor(mStartColor, mEndColor, rate);
        }

        mPaint.setColor(color);
    }

    public void setData(List<String> data) {
        if (data == null) {
            mData = new ArrayList<String>();
        } else {
            this.mData = data;
        }
        mSelected = mData.size() / 2;
        invalidate();
    }

    private static class SlotInterpolator implements Interpolator {
        @Override
        public float getInterpolation(float input) {
            return (float)(Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
        }
    }

}
