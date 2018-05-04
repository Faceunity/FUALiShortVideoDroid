/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.editor.timeline;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.demo.editor.R;

public class TimelineOverlay {
    public static final byte STATE_ACTIVE = 1; //激活态（编辑态）
    public static final byte STATE_FIX = 2;    //固定态(非编辑态)
    private byte mState;
    private long mMinDuration = 2000000;   //最小时长，到达最小时长内再无法缩减, 默认2s
    private long mMaxDuration = 0;

    private long mDuration;     //时长
    private int mDistance;      //距离（TailView和HeadView的距离）（与时长对应）

    private TimelineOverlayHandleView mTailView;
    private TimelineOverlayHandleView mHeadView;
    private View mSelectedMiddleView;   //Tail和Head中间的View，编辑态和非编辑态呈现不同颜色
    private Context mContext;
    private TimelineBar mTimelineBar;
    private ViewGroup mOverlayContainer;
    private TimelineOverlayView mOverlayView;
    private OnSelectedDurationChangeListener mSelectedDurationChange;

    public TimelineOverlay(TimelineBar timelineBar,
                           long startTime,
                           long duration,
                           TimelineOverlayView overlayView,
                           long maxDuration,
                           long minDuration) {
        this.mDuration = duration;
        this.mTimelineBar = timelineBar;
        this.mState = STATE_ACTIVE;
        this.mOverlayView = overlayView;
        this.mMaxDuration = maxDuration;
        this.mMinDuration = minDuration;
        initView(startTime);
        invalidate();
    }

    private void initView(long startTime) {
        mSelectedMiddleView = mOverlayView.getMiddleView();
        if (mDuration < mMinDuration) {//不满足最小时长，则默认设置为最小时长
            mDuration = mMinDuration;
        } else if (mMaxDuration <= mDuration) {//如果动图时长，比最大时长（一般为视频时长）还要长，则默认设置为最大时长
            startTime = 0;
            mDuration = mMaxDuration;
        }
        if (mDuration + startTime > mMaxDuration) {//如果动图时长+startTime比最大时长大，则要向前移动，保证不超出范围。
//            startTime = mMaxDuration - mDuration;//这里不能用这个策略，因为外面动图的起始时间没有更新
            mDuration = mMaxDuration - startTime;
        }
        mTailView = new TimelineOverlayHandleView(mOverlayView.getTailView(), startTime);
        mHeadView = new TimelineOverlayHandleView(mOverlayView.getHeadView(), mDuration + startTime);
        mOverlayContainer = mOverlayView.getContainer();
        setVisibility(false);
        mTimelineBar.addOverlayView(mOverlayContainer, mTailView, this);
        this.mContext = mSelectedMiddleView.getContext();
        mHeadView.setPositionChangeListener(new TimelineOverlayHandleView.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(float distance) {
                long duration = mTimelineBar.distance2Duration(distance);
                if (duration < 0 &&
                        (mHeadView.getDuration() + duration - mTailView.getDuration() < mMinDuration)) {
                    //先计算可以减少的duration
                    duration = mMinDuration + mTailView.getDuration() - mHeadView.getDuration();
                } else if (duration > 0 && mHeadView.getDuration() + duration > mMaxDuration) {
                    duration = mMaxDuration - mHeadView.getDuration();
                }
                if (duration == 0) {
                    return;
                }
                mDuration += duration;

                ViewGroup.LayoutParams layoutParams = mSelectedMiddleView.getLayoutParams();
                layoutParams.width = mTimelineBar.duration2Distance(mDuration);
                mHeadView.changeDuration(duration);
                mSelectedMiddleView.setLayoutParams(layoutParams);
                if (mSelectedDurationChange != null) {
                    mSelectedDurationChange.onDurationChange(mTailView.getDuration(),
                            mHeadView.getDuration(),
                            mDuration);
                }
            }

            @Override
            public void onChangeComplete() {
                mTimelineBar.seekTo(mHeadView.getDuration(), true);
            }
        });

        mTailView.setPositionChangeListener(new TimelineOverlayHandleView.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(float distance) {
                long duration = mTimelineBar.distance2Duration(distance);
                if (duration > 0 && mDuration - duration < mMinDuration) {
                    duration = mDuration - mMinDuration;
                } else if (duration < 0 && mTailView.getDuration() + duration < 0) {
                    duration = -mTailView.getDuration();
                }
                if (duration == 0) {
                    return;
                }
                mDuration -= duration;
                mTailView.changeDuration(duration);
                if (mTailView.getView() != null) {
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mTailView.getView().getLayoutParams();
                    int dx = layoutParams.leftMargin;
//                layoutParams.leftMargin = mTimelineBar.calculateTailViewPosition(mTailView);
                    requestLayout();
                    dx = layoutParams.leftMargin - dx;
//                Log.d(TimelineBar.TAG, "TailView X = "+mTailView.getView().getX());
                    mTailView.getView().setLayoutParams(layoutParams);
                    layoutParams = (ViewGroup.MarginLayoutParams) mSelectedMiddleView.getLayoutParams();
                    layoutParams.width -= dx; // mTimelineBar.duration2Distance(mDuration);
//                Log.d(TimelineBar.TAG, "MiddleView width = "+layoutParams.width);
                    mSelectedMiddleView.setLayoutParams(layoutParams);
                }
                if (mSelectedDurationChange != null) {
                    mSelectedDurationChange.onDurationChange(mTailView.getDuration(), mHeadView.getDuration(),
                            mDuration);
                }
            }

            @Override
            public void onChangeComplete() {
                mTimelineBar.seekTo(mTailView.getDuration(), true);
            }
        });
    }

    public void switchState(byte state) {
        mState = state;
        switch (state) {
            case STATE_ACTIVE://显示HeadView和TailView
                mTailView.active();
                mHeadView.active();
                mSelectedMiddleView.setBackgroundColor(mContext.getResources()
                        .getColor(R.color.timeline_bar_active_overlay));
                break;
            case STATE_FIX:
                mTailView.fix();
                mHeadView.fix();
                mSelectedMiddleView.setBackgroundColor(mContext.getResources()
                        .getColor(R.color.timeline_bar_active_overlay));
                break;
        }
    }

    public void invalidate() {
        //首先根据duration 计算middleView 的宽度
        mDistance = mTimelineBar.duration2Distance(mDuration);
        Log.d("TimelineBar", "mDistance "+mDistance);
        ViewGroup.LayoutParams layoutParams = mSelectedMiddleView.getLayoutParams();
        layoutParams.width = mDistance;
        mSelectedMiddleView.setLayoutParams(layoutParams);
        switch (mState) {
            case STATE_ACTIVE://显示HeadView和TailView
                mTailView.active();
                mHeadView.active();
                mSelectedMiddleView.setBackgroundColor(mContext.getResources()
                        .getColor(R.color.timeline_bar_active_overlay));
                break;
            case STATE_FIX:
                mTailView.fix();
                mHeadView.fix();
                mSelectedMiddleView.setBackgroundColor(mContext.getResources()
                        .getColor(R.color.timeline_bar_active_overlay));
                break;
        }
    }

    void setVisibility(boolean isVisible) {
        Log.d("TimelineBar", "set visibility visible "+isVisible);
        if (isVisible) {
//            if(mTailView.getView() != null) {
                mTailView.getView().setAlpha(1);
//            }
//            if(mHeadView.getView() != null) {
                mHeadView.getView().setAlpha(1);
//            }
            mSelectedMiddleView.setAlpha(1);
        } else {
//            if(mTailView.getView() != null) {
                mTailView.getView().setAlpha(0);
//            }
//            if(mHeadView.getView() != null) {
                mHeadView.getView().setAlpha(0);
//            }
            mSelectedMiddleView.setAlpha(0);
        }
    }

    public void requestLayout() {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mTailView.getView().getLayoutParams();
        layoutParams.leftMargin = mTimelineBar.calculateTailViewPosition(mTailView);
        mTailView.getView().setLayoutParams(layoutParams);

//        Log.d(TimelineBar.TAG, "TailView leftMargin = "+layoutParams.leftMargin);
//        Log.d(TimelineBar.TAG, "TailView X = "+mTailView.getView().getX());
//        Log.d(TimelineBar.TAG, "TailView SelectedMiddleView.width = "+mSelectedMiddleView.getMeasuredWidth());
//        Log.d(TimelineBar.TAG, "TailView HeadView.X = "+mHeadView.getView().getX());
//        Log.d(TimelineBar.TAG, "TailView SelectedMiddleView.leftMargin = "+
//                ((ViewGroup.MarginLayoutParams)mSelectedMiddleView.getLayoutParams()).leftMargin+"\n");
    }

    private void setLeftMargin(View view, int leftMargin) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        if (layoutParams != null) {
            layoutParams.leftMargin = leftMargin;
            view.requestLayout();
        }
    }


    public interface TimelineOverlayView {
        ViewGroup getContainer();

        View getHeadView();

        View getTailView();

        View getMiddleView();
    }

    public View getOverlayView() {
        return mOverlayContainer;
    }
    public TimelineOverlayView getTimelineOverlayView() {
        return mOverlayView;
    }

    public interface OnSelectedDurationChangeListener {
        void onDurationChange(long startTime, long endTime, long duration);
    }

    public void setOnSelectedDurationChangeListener(OnSelectedDurationChangeListener
                                                            selectedDurationChange) {
        mSelectedDurationChange = selectedDurationChange;
    }

    public void updateDuration(long duration) {
        mDuration = duration;
        invalidate();
        requestLayout();
    }
}
