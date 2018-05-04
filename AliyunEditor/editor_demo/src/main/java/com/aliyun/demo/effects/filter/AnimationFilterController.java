package com.aliyun.demo.effects.filter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ClientCertRequest;
import android.widget.LinearLayout;

import com.aliyun.demo.editor.R;
import com.aliyun.demo.editor.timeline.TimelineBar;
import com.aliyun.demo.editor.timeline.TimelineOverlay;
import com.aliyun.demo.effects.control.EffectInfo;
import com.aliyun.demo.msg.Dispatcher;
import com.aliyun.demo.msg.body.ClearAnimationFilter;
import com.aliyun.demo.msg.body.DeleteLastAnimationFilter;
import com.aliyun.demo.msg.body.LongClickAnimationFilter;
import com.aliyun.demo.msg.body.LongClickUpAnimationFilter;
import com.aliyun.qupai.editor.AliyunIEditor;
import com.aliyun.qupai.editor.AliyunIPlayer;
import com.aliyun.struct.effect.EffectFilter;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Stack;

public class AnimationFilterController {
    private static final String TAG = AnimationFilterController.class.getName();
    private static final int MESSAGE_ADD_OVERLAY = 0;
    private static final int MESSAGE_UPDATE_PROGRESS = 1;
    private static final int MESSAGE_REMOVE_OVERLAY = 2;
    private static final int MESSAGE_STOP_TO_UPDATE_OVERLAY = 3;
    private static final int MESSAGE_CLEAR_ALL_ANIMATION_FILTER = 4;
    private static final int MESSAGE_RESTORE_ANIMATION_FILTER = 5;

    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_DURATION = "duration";
    private static final String KEY_OVERLAY_COLOR = "color";

    private TimelineBar mTimelineBar;
    private AliyunIPlayer mAliyunIPlayer;
    private AliyunIEditor mAliyunIEditor;
    private long mLastStartTime = 0;
    private Stack<EffectFilter> mAddedFilter = new Stack<>();
    private Stack<TimelineOverlay> mAddedOverlay = new Stack<>();
    private TimelineOverlay mCurrOverlay;
    private Context mContext;
    private OverlayView mCurrOverlayView;
    private Handler mOverlayHandler = new TimelineBarOverlayHandler(Looper.getMainLooper());
    private int mOverlayColor = 0;

    public AnimationFilterController(
            Context context,
            TimelineBar timelineBar,
            AliyunIEditor editor,
            AliyunIPlayer player) {
        this.mTimelineBar = timelineBar;
        this.mAliyunIEditor = editor;
        this.mAliyunIPlayer = player;
        Dispatcher.getInstance().register(this);
        mContext = context;
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventAnimationFilterLongClick(LongClickAnimationFilter filter) {
//        if (mLastFilterIndex != filter.getIndex()) {
        mLastStartTime = mAliyunIPlayer.getCurrentPosition();
        EffectInfo info = filter.getEffectInfo();
        EffectFilter ef = new EffectFilter.Builder()
                .path(info.getPath())
                .startTime(mLastStartTime / 1000)
                .duration(Integer.MAX_VALUE)
                .build();
        mAliyunIEditor.addAnimationFilter(ef);
        selectOverlayColor(ef);
        mOverlayHandler.sendEmptyMessage(MESSAGE_ADD_OVERLAY);
        mAddedFilter.push(ef);
//        } else {
//            EffectInfo info = filter.getEffectInfo();
//            EffectFilter ef = new EffectFilter.Builder()
//                    .path(info.getPath())
//                    .startTime(mLastStartTime / 1000)
//                    .duration(Integer.MAX_VALUE)
//                    .build();
//            mAliyunIEditor.addAnimationFilter(ef);
//            mOverlayHandler.sendEmptyMessage(MESSAGE_UPDATE_PROGRESS);
//        }
    }


    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventAnimationFilterClickUp(LongClickUpAnimationFilter filter) {
        if (!mAddedFilter.empty()) {
            EffectFilter lastFilter = mAddedFilter.get(mAddedFilter.size() - 1);
            mAliyunIEditor.removeAnimationFilter(lastFilter);
            lastFilter.setDuration((mAliyunIPlayer.getCurrentPosition() / 1000 - lastFilter.getStartTime()));
            mAliyunIEditor.addAnimationFilter(lastFilter);
            mOverlayHandler.sendEmptyMessage(MESSAGE_STOP_TO_UPDATE_OVERLAY);
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventAnimationFilterDelete(DeleteLastAnimationFilter d) {
        if (!mAddedFilter.empty()) {
            EffectFilter lastFilter = mAddedFilter.pop();
            mAliyunIEditor.removeAnimationFilter(lastFilter);
            mOverlayHandler.sendEmptyMessage(MESSAGE_REMOVE_OVERLAY);
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventClearAnimationFilter(ClearAnimationFilter cl) {
        if (!mAddedFilter.empty()) {
            mAddedFilter.clear();
            mAliyunIEditor.clearAllAnimationFilter();
            mOverlayHandler.sendEmptyMessage(MESSAGE_CLEAR_ALL_ANIMATION_FILTER);
        }
    }

    /**
     * 恢复所有的动效滤镜
     *
     * @param filters
     */
    public void restoreAnimationFilters(List<EffectFilter> filters) {
        if (filters != null && filters.size() > 0) {
            for (EffectFilter ef : filters) {
                mAddedFilter.push(ef);
//                mAliyunIEditor.addAnimationFilter(ef);
                selectOverlayColor(ef);
                Bundle bundle = new Bundle();
                bundle.putInt(KEY_OVERLAY_COLOR, mOverlayColor);
                bundle.putLong(KEY_START_TIME, ef.getStartTime());
                bundle.putLong(KEY_DURATION, ef.getDuration());
                Message msg = mOverlayHandler.obtainMessage(MESSAGE_RESTORE_ANIMATION_FILTER);
                msg.setData(bundle);
                mOverlayHandler.sendMessage(msg);
            }
        }
    }

    public void destroyController() {
        mOverlayHandler.sendEmptyMessage(MESSAGE_STOP_TO_UPDATE_OVERLAY);
        mContext = null;
        Dispatcher.getInstance().unRegister(this);
    }

    class OverlayView implements TimelineOverlay.TimelineOverlayView {
        public Context mContext;
        private ViewGroup mRootView;
        private View mMiddleView;
        private View mHeadView;
        private View mTailView;

        public OverlayView(Context context) {
            mContext = context;
            mRootView = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.aliyun_svideo_layout_timeline_overlay, null,
                    false);
            mMiddleView = mRootView.findViewById(R.id.middle_view);
            mHeadView = mRootView.findViewById(R.id.head_view);
            mTailView = mRootView.findViewById(R.id.tail_view);
            mHeadView.setVisibility(View.INVISIBLE);
            LinearLayout.LayoutParams lpHead = (LinearLayout.LayoutParams) mHeadView.getLayoutParams();
            LinearLayout.LayoutParams lpTail = (LinearLayout.LayoutParams) mTailView.getLayoutParams();
            lpHead.width = 1;
            lpHead.height = 1;
            lpTail.width = 1;
            lpTail.height = 1;
            mTailView.setVisibility(View.INVISIBLE);
            mHeadView.setLayoutParams(lpHead);
            mTailView.setLayoutParams(lpTail);
        }

        @Override
        public ViewGroup getContainer() {
            return mRootView;
        }

        @Override
        public View getHeadView() {
            return mRootView.findViewById(R.id.head_view);
        }

        @Override
        public View getTailView() {
            return mRootView.findViewById(R.id.tail_view);
        }

        @Override
        public View getMiddleView() {
            return mMiddleView;
        }

        public void updateColor(int color) {
            mMiddleView.setBackgroundColor(color);
            mMiddleView.post(new Runnable() {
                @Override
                public void run() {
                    mMiddleView.setAlpha(0.9f);
                }
            });
        }
    }

    class TimelineBarOverlayHandler extends Handler {
        public TimelineBarOverlayHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            long duration = (mAliyunIPlayer.getCurrentPosition() - mLastStartTime);
            switch (msg.what) {
                case MESSAGE_ADD_OVERLAY:
                    mCurrOverlayView = new OverlayView(mContext);
                    mCurrOverlay = mTimelineBar.addOverlay(mLastStartTime, duration, mCurrOverlayView, 0);
                    obtainMessage(MESSAGE_UPDATE_PROGRESS).sendToTarget();
                    mAddedOverlay.push(mCurrOverlay);
                    break;
                case MESSAGE_UPDATE_PROGRESS:
                    if (mCurrOverlay != null) {
                        mCurrOverlay.updateDuration(duration);
                        mCurrOverlayView.updateColor(mOverlayColor);
                        Log.d(TAG, "startTime " + mLastStartTime + ", duration " + duration);
                    }
                    obtainMessage(MESSAGE_UPDATE_PROGRESS).sendToTarget();
                    break;
                case MESSAGE_STOP_TO_UPDATE_OVERLAY:
                    removeMessages(MESSAGE_UPDATE_PROGRESS);
                    break;
                case MESSAGE_REMOVE_OVERLAY:
                    if (!mAddedOverlay.empty()) {
                        mTimelineBar.removeOverlay(mAddedOverlay.pop());
                        mCurrOverlay = null;
                        mCurrOverlayView = null;
                        if (!mAddedOverlay.empty()) {
                            mCurrOverlay = mAddedOverlay.peek();
                            mCurrOverlayView = (OverlayView) mCurrOverlay.getTimelineOverlayView();
                        }
                    }
                    break;
                case MESSAGE_CLEAR_ALL_ANIMATION_FILTER:
                    mCurrOverlayView = null;
                    mLastStartTime = 0;
                    mCurrOverlay = null;
                    for (TimelineOverlay o : mAddedOverlay) {
                        mTimelineBar.removeOverlay(o);
                    }
                    mAddedOverlay.clear();
                    break;
                case MESSAGE_RESTORE_ANIMATION_FILTER:
                    mCurrOverlayView = new OverlayView(mContext);
                    Bundle bundle = msg.getData();
                    mLastStartTime = bundle.getLong(KEY_START_TIME) * 1000;
                    mCurrOverlay = mTimelineBar.addOverlay(mLastStartTime, bundle.getLong(KEY_DURATION) * 1000, mCurrOverlayView, 0);
                    mCurrOverlayView.updateColor(bundle.getInt(KEY_OVERLAY_COLOR));
                    mAddedOverlay.push(mCurrOverlay);
                    break;
                default:
                    Log.w(TAG, "Unknown message");
                    break;
            }
        }
    }

    private void selectOverlayColor(EffectFilter ef) {
        int colorRes = R.color.aliyun_animation_filter_color1;
        String path = ef.getPath();
        if (path != null) {
            if (path.contains("幻影")) {
                colorRes = R.color.aliyun_animation_filter_color1;
            } else if (path.contains("重影")) {
                colorRes = R.color.aliyun_animation_filter_color2;
            } else if (path.contains("抖动")) {
                colorRes = R.color.aliyun_animation_filter_color3;
            } else if (path.contains("朦胧")) {
                colorRes = R.color.aliyun_animation_filter_color4;
            } else if (path.contains("科幻")) {
                colorRes = R.color.aliyun_animation_filter_color5;
            }
        }
        mOverlayColor = mContext.getResources().getColor(colorRes);
    }
}
