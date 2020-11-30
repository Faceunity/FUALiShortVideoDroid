/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.editor.thumblinebar;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;

/**
 * 此类主要功能为控制
 */
public class ThumbLineOverlay {

    private static final String TAG = ThumbLineOverlay.class.getName();
    public static final byte STATE_ACTIVE = 1; //激活态（编辑态）
    public static final byte STATE_FIX = 2;    //固定态(非编辑态)
    private byte mState;
    private long mMinDuration = 2000000;   //最小时长，到达最小时长内再无法缩减, 默认2s
    private long mMaxDuration = 0;

    public long mDuration;     //时长
    private int mDistance;      //距离（TailView和HeadView的距离）（与时长对应）
    private boolean mIsInvert;  //是否反向

    private ThumbLineOverlayHandleView mTailView;
    private ThumbLineOverlayHandleView mHeadView;
    private View mSelectedMiddleView;   //Tail和Head中间的View，编辑态和非编辑态呈现不同颜色
    private Context mContext;
    private OverlayThumbLineBar mOverlayThumbLineBar;
    private ViewGroup mOverlayContainer;
    private ThumbLineOverlayView mOverlayView;
    private OnSelectedDurationChangeListener mSelectedDurationChange;
    public long startTime;
    private UIEditorPage mUIEditorPage;

    /**
     * 提供给动图、字幕等能够调整缩略图覆盖时长的场景使用
     */
    public ThumbLineOverlay(OverlayThumbLineBar overlayThumbBar, long startTime, long duration, ThumbLineOverlayView overlayView, long maxDuration, long minDuration, boolean isInvert, OnSelectedDurationChangeListener listener) {
        this.mDuration = duration;
        this.mOverlayThumbLineBar = overlayThumbBar;
        this.mState = STATE_ACTIVE;
        this.mOverlayView = overlayView;
        this.mMaxDuration = maxDuration;
        this.mMinDuration = minDuration;
        this.mIsInvert = isInvert;
        this.startTime = startTime;
        this.mSelectedDurationChange = listener;
        initView(startTime);
        invalidate();
    }

    private void initView(long startTime) {
        mSelectedMiddleView = mOverlayView.getMiddleView();
        if (!mIsInvert) {
            if (mDuration < mMinDuration) {//不满足最小时长，则默认设置为最小时长
                mDuration = mMinDuration;
            } else if (mMaxDuration - startTime <= 100000) {
                //如果startTime比最大时长大，则要向前移动，保证不超出范围。
                startTime = mMaxDuration  - 100000;
                mDuration = mMaxDuration - startTime;
            } else if (mDuration + startTime > mMaxDuration) {
                //如果动图时长+startTime比最大时长大，则要向前移动，保证不超出范围。
                mDuration = mMaxDuration - startTime;
            }
        } else {
            //目前只有特效有invert处理，而且时间特效和转场特效存在互斥关系，目前没有时长超出的问题
        }

        if (mSelectedDurationChange != null) {
            mSelectedDurationChange.onDurationChange(startTime, startTime + mDuration, mDuration);
        }
        Log.d(TAG, "add TimelineBar Overlay startTime:" + startTime + " ,endTime:" + (startTime + mDuration) + " ,duration:" + mDuration);
        mTailView = new ThumbLineOverlayHandleView(mOverlayView.getTailView(), startTime);
        mHeadView = new ThumbLineOverlayHandleView(mOverlayView.getHeadView(), mDuration + startTime);
        mOverlayContainer = mOverlayView.getContainer();
        mOverlayContainer.setTag(this);
        setVisibility(false);
        mOverlayThumbLineBar.addOverlayView(mOverlayContainer, mTailView, this, mIsInvert);
        this.mContext = mSelectedMiddleView.getContext();
        mHeadView.setPositionChangeListener(new ThumbLineOverlayHandleView.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(float distance) {
                if (mState == STATE_FIX) {
                    return;
                }
                long duration = mOverlayThumbLineBar.distance2Duration(distance);
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
                layoutParams.width = mOverlayThumbLineBar.duration2Distance(mDuration);
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
                if (mState == STATE_ACTIVE) {
                    //处于激活态的时候定位到滑动处
                    mOverlayThumbLineBar.seekTo(mHeadView.getDuration(), true);
                }
            }
        });

        mTailView.setPositionChangeListener(new ThumbLineOverlayHandleView.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(float distance) {
                if (mState == STATE_FIX) {
                    return;
                }
                long duration = mOverlayThumbLineBar.distance2Duration(distance);
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
                    int dx = 0;
                    if (mIsInvert) {
                        dx = layoutParams.rightMargin;
                    } else {
                        dx = layoutParams.leftMargin;
                    }
                    requestLayout();
                    if (mIsInvert) {
                        dx = layoutParams.rightMargin - dx;
                    } else {
                        dx = layoutParams.leftMargin - dx;
                    }

                    mTailView.getView().setLayoutParams(layoutParams);
                    layoutParams = (ViewGroup.MarginLayoutParams) mSelectedMiddleView.getLayoutParams();
                    layoutParams.width -= dx; // mOverlayThumbLineBar.duration2Distance(mDuration);
                    mSelectedMiddleView.setLayoutParams(layoutParams);
                }
                if (mSelectedDurationChange != null) {
                    mSelectedDurationChange.onDurationChange(mTailView.getDuration(), mHeadView.getDuration(),
                            mDuration);
                }
            }

            @Override
            public void onChangeComplete() {
                if (mState == STATE_ACTIVE) {
                    //处于激活态的时候定位到滑动处
                    mOverlayThumbLineBar.seekTo(mTailView.getDuration(), true);
                }
            }
        });
    }

    public void switchState(byte state) {
        mState = state;
        switch (state) {
        case STATE_ACTIVE://显示HeadView和TailView
            mTailView.active();
            mHeadView.active();
            if (middleViewColor != 0) {
                mSelectedMiddleView.setBackgroundColor(mContext.getResources()
                                                       .getColor(middleViewColor));
            } else {
                mSelectedMiddleView.setBackgroundColor(mContext.getResources()
                                                       .getColor(R.color.alivc_edit_timeline_bar_active_overlay));
            }
            break;
        case STATE_FIX:
            mTailView.fix();
            mHeadView.fix();
            if (middleViewColor != 0) {
                mSelectedMiddleView.setBackgroundColor(mContext.getResources()
                                                       .getColor(middleViewColor));
            } else {
                mSelectedMiddleView.setBackgroundColor(mContext.getResources()
                                                       .getColor(R.color.alivc_edit_timeline_bar_active_overlay));
            }

            break;
        default:
            break;
        }
    }
    public int middleViewColor = 0;
    public void updateMiddleViewColor(int middleViewColor) {
        if (this.middleViewColor != middleViewColor) {
            this.middleViewColor = middleViewColor;
            mSelectedMiddleView.setBackgroundColor(mContext.getResources()
                                                   .getColor(middleViewColor));
        }

    }
    public void invalidate() {
        //首先根据duration 计算middleView 的宽度
        mDistance = mOverlayThumbLineBar.duration2Distance(mDuration);
        ViewGroup.LayoutParams layoutParams = mSelectedMiddleView.getLayoutParams();
        layoutParams.width = mDistance;
        mSelectedMiddleView.setLayoutParams(layoutParams);
        switch (mState) {
        case STATE_ACTIVE://显示HeadView和TailView
            mTailView.active();
            mHeadView.active();
            if (middleViewColor != 0) {
                mSelectedMiddleView.setBackgroundColor(mContext.getResources()
                                                       .getColor(middleViewColor));
            } else {
                mSelectedMiddleView.setBackgroundColor(mContext.getResources()
                                                       .getColor(R.color.alivc_edit_timeline_bar_active_overlay));
            }
            break;
        case STATE_FIX:
            mTailView.fix();
            mHeadView.fix();
            if (middleViewColor != 0) {
                mSelectedMiddleView.setBackgroundColor(mContext.getResources()
                                                       .getColor(middleViewColor));
            } else {
                mSelectedMiddleView.setBackgroundColor(mContext.getResources()
                                                       .getColor(R.color.alivc_edit_timeline_bar_active_overlay));
            }
            break;
        default:
            break;
        }
    }

    void setVisibility(boolean isVisible) {
        if (isVisible) {
            mTailView.getView().setAlpha(1);
            mHeadView.getView().setAlpha(1);
            mSelectedMiddleView.setAlpha(1);
        } else {
            mTailView.getView().setAlpha(0);
            mHeadView.getView().setAlpha(0);
            mSelectedMiddleView.setAlpha(0);
        }
    }

    public void requestLayout() {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mTailView.getView().getLayoutParams();
        int margin;
        if (mIsInvert) {
            margin = layoutParams.rightMargin = mOverlayThumbLineBar.calculateTailViewInvertPosition(mTailView);
        } else {
            margin = layoutParams.leftMargin = mOverlayThumbLineBar.calculateTailViewPosition(mTailView);
        }
        mTailView.getView().setLayoutParams(layoutParams);

        Log.d(TAG, "TailView Margin = " + margin + "timeline over" + this);
    }

    private void setLeftMargin(View view, int leftMargin) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        if (layoutParams != null) {
            layoutParams.leftMargin = leftMargin;
            view.requestLayout();
        }
    }

    public void setUIEditorPage(UIEditorPage uiEditorPage) {
        this.mUIEditorPage = uiEditorPage;
    }

    public UIEditorPage getUIEditorPage() {
        return mUIEditorPage;
    }

    public interface ThumbLineOverlayView {
        ViewGroup getContainer();

        View getHeadView();

        View getTailView();

        View getMiddleView();
    }

    public View getOverlayView() {
        return mOverlayContainer;
    }
    public ThumbLineOverlayView getThumbLineOverlayView() {
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
