/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.editor.timeline;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class TimelineBar {
    public static final String TAG = TimelineBar.class.getName();
    private static final int WHAT_THUMBNAIL_VIEW_AUTO_MOVE = 1;
    private static final int WHAT_TIMELINE_ON_SEEK = 2;
    private static final int WHAT_TIMELINE_FINISH_SEEK = 3;

    private static final String KEY_RATE = "rate";
    private static final String KEY_DURATION = "duration";
    private static final String KEY_NEED_CALLBACK = "need_callback";
    private ThumbnailView mThumbnailView;
    private RecyclerView mRecyclerView;
    private ViewGroup mThumbnailParentView;
    private long mTotalDuration;
    private long mCurrDuration = 0;
    private Object mCurrDurationLock = new Object();
    private int mThumbnailNum;
    private float mTimelineBarViewWidth;    //整个时间轴View的宽度（缩略图个数 * 单个缩略图的宽度）
    private float mTimelineBarViewDisplayWidth;    //整个时间轴View的可显示宽度（屏幕内有效显示宽度）


    private TimelinePlayer mPlayer;
    private PlayThread mPlayThread;
    private TimelineBarSeekListener mBarSeekListener;
    private int mSingleViewWidth;
    private float mCurrScroll;
    private float mErrorDis;
    private boolean mIsTouching = false;
    private List<TimelineOverlay> mOverlayList = new ArrayList<>();
    private int mScrollState;
    private Handler mUIHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            long duration = msg.getData().getLong(KEY_DURATION);
            switch (msg.what) {
                case WHAT_THUMBNAIL_VIEW_AUTO_MOVE:
                    float rate = msg.getData().getFloat(KEY_RATE);
                    boolean needCallback = msg.getData().getBoolean(KEY_NEED_CALLBACK);
                    if (mBarSeekListener != null &&
                            needCallback && !mIsTouching) {
                        mBarSeekListener.onTimelineBarSeek(duration);
                    }
                    scroll(rate);
                    mThumbnailView.updateDuration(duration);
                    break;
                case WHAT_TIMELINE_ON_SEEK:
                    mBarSeekListener.onTimelineBarSeek(duration);
                    break;
                case WHAT_TIMELINE_FINISH_SEEK:
                    mBarSeekListener.onTimelineBarSeekFinish(duration);
                    break;

            }
        }
    };

    private void scroll(float rate) {
        float scrollBy = rate * getTimelineBarViewWidth() - mCurrScroll;
        if (mErrorDis >= 1) {
            scrollBy += 1;
            mErrorDis -= 1;
        }
        mErrorDis = scrollBy - (int) scrollBy;
        mRecyclerView.scrollBy((int) scrollBy, 0);
    }


    public TimelineBar(long totalDuration,
                       int singleThumbnailImageWidth,
                       TimelinePlayer player) {
        this.mTotalDuration = totalDuration;
        this.mPlayer = player;
        this.mSingleViewWidth = singleThumbnailImageWidth;
    }


    public void setTimelineBarDisplayWidth(int width) {
        this.mTimelineBarViewDisplayWidth = width;
    }


    public void setThumbnailView(ThumbnailView thumbnailView) {
        this.mThumbnailView = thumbnailView;
        this.mRecyclerView = thumbnailView.getThumbnailView();
        this.mThumbnailParentView = thumbnailView.getThumbnailParentView();
        this.mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int actionMasked = MotionEventCompat.getActionMasked(event);
                switch (actionMasked) {
                    case MotionEvent.ACTION_DOWN:
                        mIsTouching = true;
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mIsTouching = false;
                        break;
                }
                return false;
            }
        });
        this.mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
//                        if (mScrollState == RecyclerView.SCROLL_STATE_SETTLING) {//快速拖拽导致的自动移动，即将结束
                        Message msg = mUIHandler.obtainMessage(WHAT_TIMELINE_FINISH_SEEK);
                        Bundle data = new Bundle();
                        data.putLong(KEY_DURATION, mCurrDuration);
                        msg.setData(data);
                        mUIHandler.sendMessage(msg);
                        mThumbnailView.updateDuration(mCurrDuration);
                        for (TimelineOverlay overlay : mOverlayList) {
                            overlay.requestLayout();
                        }
//                        }
//                        Log.d(TAG, "ScrollStateChanged SCROLL_STATE_IDLE");
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
//                        Log.d(TAG, "ScrollStateChanged SCROLL_STATE_DRAGGING");
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
//                        Log.d(TAG, "ScrollStateChanged SCROLL_STATE_SETTLING");
                        break;
                }
                mScrollState = newState;
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
//                Log.d(TAG, "Scroll dx = " + dx);
                mCurrScroll += dx;
                float rate = mCurrScroll / getTimelineBarViewWidth();
                long duration = (long) (rate * mTotalDuration);
                if (mBarSeekListener != null
                        && (mIsTouching ||
                        mScrollState == RecyclerView.SCROLL_STATE_SETTLING)) {
                    Message msg = mUIHandler.obtainMessage(WHAT_TIMELINE_ON_SEEK);
                    Bundle data = new Bundle();
                    data.putLong(KEY_DURATION, duration);
                    msg.setData(data);
                    mUIHandler.sendMessage(msg);
                }
                mCurrDuration = duration;
                mThumbnailView.updateDuration(duration);
                int length = mOverlayList.size();
                for (int i = 0; i < length; i++) {
                    mOverlayList.get(i).requestLayout();
                }
            }
        });
    }

    private float getTimelineBarViewWidth() {
        if (mThumbnailView.getThumbnailView().getAdapter() == null) {
            return 0;
        }
        if (mTimelineBarViewWidth == 0) {
            this.mThumbnailNum = (mThumbnailView.getThumbnailView().getAdapter().getItemCount() - 2);
            this.mTimelineBarViewWidth = mThumbnailNum * mSingleViewWidth;
        }
        return mTimelineBarViewWidth;
    }

    void addOverlayView(final View overlayView,
                        final TimelineOverlayHandleView tailView,
                        final TimelineOverlay overlay) {
        Log.d("XXX", "add TimelineBar OverlayView");
        mThumbnailParentView.addView(overlayView);
        final View view = tailView.getView();
//        if (view != null) {
            overlayView.post(new Runnable() {
                @Override
                public void run() {
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                    layoutParams.leftMargin = calculateTailViewPosition(tailView);
                    view.requestLayout();
                    overlay.setVisibility(true);

                }
            });
//        }
    }

    int calculateTailViewPosition(TimelineOverlayHandleView tailView) {
        if(tailView.getView() != null) {
            return (int) (mTimelineBarViewDisplayWidth / 2 - tailView.getView().getMeasuredWidth()
                    + duration2Distance(tailView.getDuration()) - mCurrScroll);
        }else {
            return 0;
        }
    }


    long distance2Duration(float distance) {
        float rate = distance / getTimelineBarViewWidth();
        return (long) (mTotalDuration * rate);
    }

    int duration2Distance(long duration) {
        float rate = duration * 1.0f / mTotalDuration;
        return (int) (getTimelineBarViewWidth() * rate);
    }


    public void seekTo(long duration, boolean needCallback) {
        synchronized (mCurrDurationLock) {
            mCurrDuration = duration;
        }
        if (duration == 0) {
            Log.d(TAG, "duration  == 0");
        }
        float rate = duration * 1.0f / mTotalDuration;
        Message msg = mUIHandler.obtainMessage(WHAT_THUMBNAIL_VIEW_AUTO_MOVE);
        Bundle data = new Bundle();
        data.putFloat(KEY_RATE, rate);
        data.putLong(KEY_DURATION, duration);
        data.putBoolean(KEY_NEED_CALLBACK, needCallback);
        msg.setData(data);
        mUIHandler.sendMessage(msg);
//        Log.d(TAG, "TimelineBar seek to duration = " + duration + ", rate = " + rate);
    }

    public void start() {
        Log.d(TAG, "TimelineBar start");
        if (mPlayThread == null) {
            mPlayThread = new PlayThread(mTotalDuration);
            mPlayThread.startPlaying();
        }
    }

    public void resume() {
//        Log.d(TAG, "TimelineBar resume");
        if (mPlayThread != null) {
            mPlayThread.resumePlaying();
        }
    }

    public boolean isPausing() {
//        Log.d(TAG, "TimelineBar isPausing");
        if (mPlayThread != null) {
            return mPlayThread.isPausing();
        }
        return false;
    }

    public void pause() {
        Log.d(TAG, "TimelineBar aliyun_svideo_pause");
        if (mPlayThread != null) {
            mPlayThread.pause();
        }
    }


    public void stop() {
//        Log.d(TAG, "TimelineBar stop");
        if (mPlayThread != null) {
            mPlayThread.stopPlaying();
            mPlayThread = null;
//            seekTo(0, true);
        }
    }

    public void restart() {
//        Log.d(TAG, "TimelineBar restart");
        stop();
        start();
    }


    class PlayThread extends Thread {
        public static final byte STATE_PLAYING = 1;
        public static final byte STATE_PAUSING = 2;
        public static final byte STATE_STOPPING = 3;

        private long mTotalDuration;
        private long mLastDuration = -1;
        private byte mState = STATE_STOPPING;
        private Object mStateLock = new Object();

        public PlayThread(long totalDuration) {
            mTotalDuration = totalDuration;
        }

        @Override
        public void run() {
            super.run();
            mState = STATE_PLAYING;
            mLastDuration = -1;
            while (mCurrDuration <= mTotalDuration) {
                synchronized (mStateLock) {
                    if (mState == STATE_PAUSING) {
                        try {
//                            Log.d(TAG, "TimelineBar pausing");
                            mStateLock.wait();
//                            Log.d(TAG, "TimelineBar resuming");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else if (mState == STATE_STOPPING) {
                        mCurrDuration = 0;
                        break;
                    }
                }
                synchronized (mCurrDurationLock) {
//                    Log.d(TAG, "Call mPlayer.getDuration");
                    mCurrDuration = mPlayer.getCurrDuration();
                }
                Log.d(TAG, "Player currDuration = " + mCurrDuration + ", mTotalDuration=" + mTotalDuration);
                if (mCurrDuration != mLastDuration) {
                    seekTo(mCurrDuration, false);
                    mLastDuration = mCurrDuration;
                }
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void startPlaying() {
            this.start();
        }

        public void resumePlaying() {
            synchronized (mStateLock) {
                mState = STATE_PLAYING;
                mStateLock.notify();
            }
        }

        public void pause() {
            synchronized (mStateLock) {
                mState = STATE_PAUSING;
            }
        }

        public void stopPlaying() {
            synchronized (mStateLock) {
                mState = STATE_STOPPING;
                mStateLock.notify();
            }
            try {
                this.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mCurrDuration = 0;
        }

        public boolean isPausing() {
            return mState == STATE_PAUSING;
        }
    }

    public TimelineOverlay addOverlay(long startTime,
                                      long duration,
                                      TimelineOverlay.TimelineOverlayView view,
                                      long minDuration) {
        if (startTime < 0) {
            startTime = 0;
        }
        TimelineOverlay overlay = new TimelineOverlay(this, startTime,
                duration,
                view, mTotalDuration, minDuration);
        mOverlayList.add(overlay);
        return overlay;
    }


    public void removeOverlay(TimelineOverlay overlay) {
        if (overlay != null) {
            Log.d("XXX", "remove TimelineBar Overlay");
            mThumbnailParentView.removeView(overlay.getOverlayView());
        }
    }

    public void setBarSeekListener(TimelineBarSeekListener barSeekListener) {
        mBarSeekListener = barSeekListener;
    }

    public interface TimelinePlayer {
        long getCurrDuration();
    }

    public interface ThumbnailView {
        RecyclerView getThumbnailView();

        ViewGroup getThumbnailParentView();

        void updateDuration(long duration);
    }

    public interface TimelineBarSeekListener {
        void onTimelineBarSeek(long duration);

        void onTimelineBarSeekFinish(long duration);
    }

}
