package com.aliyun.svideo.editor.effects.filter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.editor.thumblinebar.OverlayThumbLineBar;
import com.aliyun.svideo.editor.editor.thumblinebar.ThumbLineOverlay;
import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.svideo.editor.msg.Dispatcher;
import com.aliyun.svideo.editor.msg.body.ClearAnimationFilter;
import com.aliyun.svideo.editor.msg.body.ConfirmAnimationFilter;
import com.aliyun.svideo.editor.msg.body.DeleteLastAnimationFilter;
import com.aliyun.svideo.editor.msg.body.LongClickAnimationFilter;
import com.aliyun.svideo.editor.msg.body.LongClickUpAnimationFilter;
import com.aliyun.editor.TimeEffectType;
import com.aliyun.qupai.editor.AliyunIEditor;
import com.aliyun.svideo.sdk.external.struct.effect.EffectFilter;

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

    private OverlayThumbLineBar mThumbLineBar;
    private AliyunIEditor mAliyunIEditor;
    private long mLastStartTime = 0;
    private boolean mInvert = false;
    private Stack<EffectFilter> mAddedFilter = new Stack<>();
    private Stack<EffectFilter> mAddedFilterTemp;
    private Stack<ThumbLineOverlay> mAddedOverlay = new Stack<>();
    private Stack<ThumbLineOverlay> mAddedOverlayTemp;
    private ThumbLineOverlay mCurrOverlay;
    private Context mContext;
    private OverlayView mCurrOverlayView;
    private Handler mOverlayHandler = new TimelineBarOverlayHandler(Looper.getMainLooper());
    private int mOverlayColor = 0;

    public AnimationFilterController(
        Context context,
        AliyunIEditor editor) {
        this.mAliyunIEditor = editor;
        Dispatcher.getInstance().register(this);
        mContext = context;
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventAnimationFilterLongClick(LongClickAnimationFilter filter) {

        if (mAliyunIEditor.getTimeEffect() == TimeEffectType.TIME_EFFECT_TYPE_INVERT) {
            mInvert = true;
        } else {
            mInvert = false;
        }

        EffectInfo info = filter.getEffectInfo();
        long retainTime = mAliyunIEditor.getDuration() - info.streamStartTime;
        //if (retainTime< AlivcEditView.USE_ANIMATION_REMAIN_TIME){
        //    mLastStartTime = mAliyunIEditor.getCurrentStreamPosition();
        //}else {
        //    mLastStartTime = info.streamStartTime;
        //}
        mLastStartTime = info.streamStartTime;

        EffectFilter ef = new EffectFilter.Builder()
        .path(info.getPath())
        .startTime(mLastStartTime / 1000)
        .duration(Integer.MAX_VALUE)
        .build();
        mAliyunIEditor.addAnimationFilter(ef);
        selectOverlayColor(ef);
        mOverlayHandler.sendEmptyMessage(MESSAGE_ADD_OVERLAY);
        if (mAddedFilterTemp == null) {
            mAddedFilterTemp = new Stack<>();
            mAddedFilterTemp.addAll(mAddedFilter);
        }
        mAddedFilterTemp.push(ef);
    }


    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventAnimationFilterClickUp(LongClickUpAnimationFilter filter) {

        if (mAddedFilterTemp != null && !mAddedFilterTemp.empty()) {
            EffectFilter lastFilter = mAddedFilterTemp.get(mAddedFilterTemp.size() - 1);
            mAliyunIEditor.removeAnimationFilter(lastFilter);
            long duration = Math.abs(mAliyunIEditor.getCurrentStreamPosition() / 1000 - lastFilter.getStartTime());
            lastFilter.setDuration(duration);
            mAliyunIEditor.addAnimationFilter(lastFilter);
            mOverlayHandler.sendEmptyMessage(MESSAGE_STOP_TO_UPDATE_OVERLAY);
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventAnimationFilterDelete(DeleteLastAnimationFilter d) {
        if (mAddedFilterTemp == null) {
            mAddedFilterTemp = new Stack<>();
            mAddedFilterTemp.addAll(mAddedFilter);
        }
        if (!mAddedFilterTemp.empty()) {
            EffectFilter lastFilter = mAddedFilterTemp.pop();
            mAliyunIEditor.removeAnimationFilter(lastFilter);
            if (mAliyunIEditor.isPlaying()) {
                mAliyunIEditor.seek(lastFilter.getStartTime() * 1000);
                mAliyunIEditor.resume();
            } else {
                mAliyunIEditor.seek(lastFilter.getStartTime() * 1000);
            }
            mOverlayHandler.sendEmptyMessage(MESSAGE_REMOVE_OVERLAY);
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventClearAnimationFilter(ClearAnimationFilter cl) {
        if (mAddedFilterTemp != null) {
            mAddedFilterTemp.clear();
            mAddedFilterTemp = null;
            mAliyunIEditor.clearAllAnimationFilter();
            mOverlayHandler.sendEmptyMessage(MESSAGE_CLEAR_ALL_ANIMATION_FILTER);
        }
    }
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventConfirmAnimationFilter(ConfirmAnimationFilter confirm) {
        if (mAddedFilterTemp != null && mAddedOverlayTemp != null) {
            mAddedOverlay.clear();
            mAddedFilter.clear();
            mAddedOverlay.addAll(mAddedOverlayTemp);
            mAddedFilter.addAll(mAddedFilterTemp);
            mAddedFilterTemp.clear();
            mAddedOverlayTemp.clear();
            mAddedOverlayTemp = null;
            mAddedFilterTemp = null;

        }
    }

    /**
     * 恢复所有的动效滤镜
     */
    public void restoreAnimationFilters(List<EffectFilter> filters) {
        if (filters != null && filters.size() > 0) {
            mAddedFilter.clear();
            for (ThumbLineOverlay o : mAddedOverlay) {
                mThumbLineBar.removeOverlay(o);
            }
            mAddedOverlay.clear();
            for (EffectFilter ef : filters) {

                if (ef.getStartTime() >= mAliyunIEditor.getStreamDuration() / 1000) {
                    //在添加转场导致总时长减少时，处于最后被减少时段的特效不恢复
                    continue;
                }
                mAddedFilter.push(ef);
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

    class OverlayView implements ThumbLineOverlay.ThumbLineOverlayView {
        public Context mContext;
        private ViewGroup mRootView;
        private View mMiddleView;
        private View mHeadView;
        private View mTailView;
        public OverlayView(Context context, boolean isInvert) {
            mContext = context;
            mRootView = null;
            if (isInvert) {
                mRootView = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.aliyun_svideo_layout_timeline_invert_overlay, null,
                            false);
            } else {
                mRootView = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.aliyun_svideo_layout_timeline_overlay, null,
                            false);
            }
            mMiddleView = mRootView.findViewById(R.id.middle_view);
            mHeadView = mRootView.findViewById(R.id.head_view);
            mTailView = mRootView.findViewById(R.id.tail_view);
            mHeadView.setVisibility(View.INVISIBLE);
            ViewGroup.LayoutParams lpHead = mHeadView.getLayoutParams();
            ViewGroup.LayoutParams lpTail = mTailView.getLayoutParams();
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

    }

    class TimelineBarOverlayHandler extends Handler {
        public TimelineBarOverlayHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mThumbLineBar == null) {
                return;
            }
            long duration;

            duration = Math.abs(mAliyunIEditor.getCurrentStreamPosition() - mLastStartTime);//变流长的情况需要
            switch (msg.what) {
            case MESSAGE_ADD_OVERLAY:
                mCurrOverlayView = new OverlayView(mContext, mInvert);
                mCurrOverlay = mThumbLineBar.addOverlay(mLastStartTime, duration, mCurrOverlayView, 0, mInvert, UIEditorPage.FILTER_EFFECT);
                mCurrOverlay.updateMiddleViewColor(mOverlayColor);
                obtainMessage(MESSAGE_UPDATE_PROGRESS).sendToTarget();
                if (mAddedOverlayTemp == null) {
                    mAddedOverlayTemp = new Stack<>();
                    mAddedOverlayTemp.addAll(mAddedOverlay);
                }
                mAddedOverlayTemp.push(mCurrOverlay);
                break;
            case MESSAGE_UPDATE_PROGRESS:
                if (mCurrOverlay != null) {
                    mCurrOverlay.updateDuration(duration);

                }
                obtainMessage(MESSAGE_UPDATE_PROGRESS).sendToTarget();
                break;
            case MESSAGE_STOP_TO_UPDATE_OVERLAY:
                removeMessages(MESSAGE_UPDATE_PROGRESS);
                Log.d(TAG, "removeMessages: MESSAGE_UPDATE_PROGRESS :");
                break;
            case MESSAGE_REMOVE_OVERLAY:
                if (mAddedOverlayTemp == null) {
                    mAddedOverlayTemp = new Stack<>();
                    mAddedOverlayTemp.addAll(mAddedOverlay);
                }
                if (!mAddedOverlayTemp.empty()) {
                    ThumbLineOverlay overlay = mAddedOverlayTemp.pop();
                    mThumbLineBar.removeOverlay(overlay);
                    mThumbLineBar.seekTo(overlay.startTime, false );
                    mCurrOverlay = null;
                    mCurrOverlayView = null;
                    if (!mAddedOverlayTemp.empty()) {
                        mCurrOverlay = mAddedOverlayTemp.peek();
                        mCurrOverlayView = (OverlayView) mCurrOverlay.getThumbLineOverlayView();
                    }

                    Dispatcher.getInstance().postMsg(MESSAGE_REMOVE_OVERLAY);
                }
                break;
            case MESSAGE_CLEAR_ALL_ANIMATION_FILTER:
                //撤销恢复
                mCurrOverlayView = null;
                mLastStartTime = 0;
                mCurrOverlay = null;
                if (mAddedOverlayTemp != null) {

                    for (ThumbLineOverlay o : mAddedOverlayTemp) {
                        mThumbLineBar.removeOverlay(o);
                    }
                    mAddedOverlayTemp.clear();
                    mAddedOverlayTemp = null;
                }
                for (int i = 0; i < mAddedFilter.size(); i++) {
                    // 恢复刚进入的时候的特效
                    mAliyunIEditor.addAnimationFilter(mAddedFilter.get(i));
                    ThumbLineOverlay overlay = mAddedOverlay.get(i);
                    ThumbLineOverlay t = mThumbLineBar.addOverlay(overlay.startTime, overlay.mDuration, overlay.getThumbLineOverlayView(), 0, mInvert,
                                         UIEditorPage.FILTER_EFFECT);
                    t.updateMiddleViewColor(overlay.middleViewColor);

                }
                break;
            case MESSAGE_RESTORE_ANIMATION_FILTER:
                mCurrOverlayView = new OverlayView(mContext, mInvert);
                Bundle bundle = msg.getData();
                mLastStartTime = bundle.getLong(KEY_START_TIME) * 1000;
                mCurrOverlay = mThumbLineBar.addOverlay(mLastStartTime, bundle.getLong(KEY_DURATION) * 1000, mCurrOverlayView, 0, mInvert, UIEditorPage.FILTER_EFFECT);
                mCurrOverlay.getOverlayView().setVisibility(View.INVISIBLE);
                mCurrOverlay.updateMiddleViewColor(bundle.getInt(KEY_OVERLAY_COLOR));
                Log.d(TAG, "handleMessage: RESTORE ,startTime :" + mLastStartTime + " ,endTime :" + (mLastStartTime + bundle.getLong(KEY_DURATION) * 1000));
                mAddedOverlay.push(mCurrOverlay);
                break;
            default:
                Log.w(TAG, "Unknown message");
                break;
            }
        }
    }

    public void setThumbLineBar(OverlayThumbLineBar thumbLineBar) {
        mThumbLineBar = thumbLineBar;
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
        mOverlayColor = colorRes;
    }
}
