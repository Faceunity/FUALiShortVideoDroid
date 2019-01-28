/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.effects.control;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.aliyun.demo.editor.R;
import com.aliyun.demo.effectmanager.MoreCaptionActivity;
import com.aliyun.demo.effectmanager.MoreMVActivity;
import com.aliyun.demo.effects.audiomix.MusicChooser;
import com.aliyun.demo.effects.caption.CaptionChooserView;
import com.aliyun.demo.effects.filter.AnimationFilterChooserView;
import com.aliyun.demo.effects.filter.ColorFilterChooserView;
import com.aliyun.demo.effects.imv.ImvChooserMediator;
import com.aliyun.demo.effects.overlay.OverlayChooserView;
import com.aliyun.demo.effects.paint.PaintChooserView;
import com.aliyun.demo.effects.time.TimeChooserView;
import com.aliyun.demo.effects.transition.TransitionChooserView;
import com.aliyun.demo.util.FixedToastUtils;
import com.aliyun.demo.util.SharedPreferenceUtils;
import com.aliyun.demo.view.AlivcEditView;
import com.aliyun.demo.viewoperate.ViewOperator;
import com.aliyun.svideo.sdk.external.struct.AliyunIClipConstructor;

/**
 * 底部导航栏的 view stack
 */
public class ViewStack {

    private final static String TAG = ViewStack.class.getName();
    private AlivcEditView rootView;
    private ViewOperator mViewOperator;
    private final Context mContext;
    private EditorService mEditorService;
    private OnEffectChangeListener mOnEffectChangeListener;
    private TransitionChooserView.OnPreviewListener mOnPreviewListener;

    private ColorFilterChooserView mColorFilterCHoosrView;
    private MusicChooser mAudioMixChooserMediator;
    private ImvChooserMediator mImvChooserMediator;
    private TimeChooserView mTimeChooserView;
    private PaintChooserView mPaintChooserView;

    private TransitionChooserView mTransitionChooserView;

    private OverlayChooserView mOverlayChooserView;//动图
    private CaptionChooserView mCaptionChooserView;//字幕
    private AnimationFilterChooserView mAnimationChooserView;//特效
    private OnEffectActionLister mOnEffectActionLister;
    private AlivcEditView.PlayerListener mPlayerListener;

    public ViewStack(Context context, AlivcEditView editView, ViewOperator viewOperator) {

        mContext = context;
        rootView = editView;
        mViewOperator = viewOperator;
    }

    public void setActiveIndex(int value) {

        UIEditorPage index = UIEditorPage.get(value);

        switch (index) {
            case FILTER:
                // 颜色滤镜
                if (mColorFilterCHoosrView == null) {
                    mColorFilterCHoosrView = new ColorFilterChooserView(mContext);
                }
                mViewOperator.showBottomView(mColorFilterCHoosrView);
                break;
            case FILTER_EFFECT:
                // 特效滤镜
                mAnimationChooserView = new AnimationFilterChooserView(mContext);
                mAnimationChooserView.setEditorService(mEditorService);
                mAnimationChooserView.setFirstShow(SharedPreferenceUtils.isAnimationEffectFirstShow(mContext));
                mAnimationChooserView.setOnEffectChangeListener(mOnEffectChangeListener);
                mAnimationChooserView.setPlayerListener(mPlayerListener);
                mAnimationChooserView.addThumbView(rootView.getThumbLineBar());
                mAnimationChooserView.setOnEffectActionLister(mOnEffectActionLister);
                mViewOperator.showBottomView(mAnimationChooserView);
                SharedPreferenceUtils.setAnimationEffectFirstShow(mContext,false );
                break;
            case MV:
                // MV
                mImvChooserMediator = new ImvChooserMediator(mContext);
                mImvChooserMediator.setEditorService(mEditorService);
                mImvChooserMediator.setOnEffectChangeListener(mOnEffectChangeListener);
                mViewOperator.showBottomView(mImvChooserMediator);
                break;
            case OVERLAY:
                // 动图
                mOverlayChooserView = new OverlayChooserView(mContext);
                mOverlayChooserView.setEditorService(mEditorService);
                mOverlayChooserView.setOnEffectChangeListener(mOnEffectChangeListener);
                mOverlayChooserView.setOnEffectActionLister(mOnEffectActionLister);
                setLayoutParams(mOverlayChooserView);
                mOverlayChooserView.addThumbView(rootView.getThumbLineBar());
                mViewOperator.showBottomView(mOverlayChooserView);
                break;
            case CAPTION:
                // 字幕
                mCaptionChooserView = new CaptionChooserView(mContext);
                mCaptionChooserView.setEditorService(mEditorService);
                mCaptionChooserView.setOnEffectChangeListener(mOnEffectChangeListener);
                mCaptionChooserView.setOnEffectActionLister(mOnEffectActionLister);
                setLayoutParams(mCaptionChooserView);
                mCaptionChooserView.addThumbView(rootView.getThumbLineBar());
                mViewOperator.showBottomView(mCaptionChooserView);
                break;
            case AUDIO_MIX:
                // 音乐
                if (mAudioMixChooserMediator==null){
                    mAudioMixChooserMediator = new MusicChooser(mContext);
                    mAudioMixChooserMediator.setOnEffectChangeListener(mOnEffectChangeListener);
                    mAudioMixChooserMediator.setOnEffectActionLister(mOnEffectActionLister);
                }
                long duration = rootView.getEditor().getStreamDuration() / 1000;
                mAudioMixChooserMediator.setRecordTime(duration);
                mViewOperator.showBottomView(mAudioMixChooserMediator);
                break;
            case PAINT:
                // 涂鸦
                mPaintChooserView = new PaintChooserView(mContext);
                mPaintChooserView.setEditorService(mEditorService);
                mPaintChooserView.setOnEffectActionLister(new OnEffectActionLister() {
                    /**
                     * todo @朱世元 不要在ViewStack中处理业务逻辑、回调到AlivcView中去处理 {@link ViewStack#mOnEffectActionLister}
                     */
                    @Override
                    public void onCancel() {
                        //清除当前操作
                        rootView.mCanvasController.cancel();
                        rootView.mCanvasController.applyPaintCanvas();
                        rootView.mPasterContainer.removeView(rootView.mCanvasController.getCanvas());
                        mViewOperator.hideBottomView();
                    }
                    @Override
                    public void onComplete() {
                        rootView.mCanvasController.confirm();
                        rootView.mCanvasController.applyPaintCanvas();
                        rootView.mPasterContainer.removeView(rootView.mCanvasController.getCanvas());
                        mViewOperator.hideBottomView();
                    }
                });
                mPaintChooserView.setPaintSelectListener(new PaintChooserView.PaintSelect() {
                    @Override
                    public void onColorSelect(int color) {
                        if (rootView.mCanvasController!=null){
                            rootView.mCanvasController.setCurrentColor(color);
                        }
                    }

                    @Override
                    public void onSizeSelect(float size) {
                        if (rootView.mCanvasController!=null){
                            rootView.mCanvasController.setCurrentSize(size);
                        }

                    }

                    @Override
                    public void onUndo() {
                        rootView.mCanvasController.undo();
                    }
                });
                mViewOperator.showBottomView(mPaintChooserView);
                break;
            case TIME:
                // 时间
                if (rootView.getEditor().getSourcePartManager().getAllClips().size() > 1) {
                    FixedToastUtils.show(mContext,
                        mContext.getResources().getString(R.string.aliyun_svideo_time_effect_not_support));
                    break;
                }
                mTimeChooserView = new TimeChooserView(mContext);
                mTimeChooserView.setFirstShow(SharedPreferenceUtils.isTimeEffectFirstShow(mContext));
                mTimeChooserView.setOnEffectChangeListener(mOnEffectChangeListener);
                mTimeChooserView.setEditorService(mEditorService);
                mTimeChooserView.addThumbView(rootView.getThumbLineBar());
                mTimeChooserView.setOnEffectActionLister(mOnEffectActionLister);
                mViewOperator.showBottomView(mTimeChooserView);
                SharedPreferenceUtils.setTimeEffectFirstShow(mContext,false );
                break;
            case TRANSITION:
                // 转场
                AliyunIClipConstructor clipConstructor = TransitionChooserView.isClipLimit(rootView.getEditor());
                if (clipConstructor == null) {
                    Toast.makeText(mContext, mContext.getString(R.string.aliyun_svideo_transition_limit),
                        Toast.LENGTH_SHORT).show();
                    break;
                }
                mTransitionChooserView = new TransitionChooserView(mContext);
                mTransitionChooserView.setEditorService(mEditorService);
                mTransitionChooserView.setOnEffectChangeListener(mOnEffectChangeListener);
                mTransitionChooserView.setOnEffectActionLister(mOnEffectActionLister);
                mTransitionChooserView.setOnPreviewListener(mOnPreviewListener);
                mTransitionChooserView.initTransitionAdapter(clipConstructor);
                setLayoutParams(mTransitionChooserView);
                mViewOperator.showBottomView(mTransitionChooserView);
                break;
            default:
                Log.d(TAG, "点击编辑效果，方法setActiveIndex未匹配");
                return;
        }
        rootView.setPasterDisplayScale(ViewOperator.SCALE_SIZE);

    }

    /**
     * 设置LayoutParams
     *
     * @param baseChooser view
     */
    private void setLayoutParams(BaseChooser baseChooser) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        baseChooser.setLayoutParams(layoutParams);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case BaseChooser.CAPTION_REQUEST_CODE:
                if (mCaptionChooserView == null) {
                    break;
                }
                if (resultCode == Activity.RESULT_OK) {
                    int id = data.getIntExtra(MoreCaptionActivity.SELECTED_ID, 0);
                    mCaptionChooserView.setCurrResourceID(id);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    mCaptionChooserView.setCurrResourceID(-1);
                }
                break;
            case BaseChooser.IMV_REQUEST_CODE:
                if (  mImvChooserMediator == null){
                    return;
                }
                if (resultCode == Activity.RESULT_OK) {
                    int id = data.getIntExtra(MoreMVActivity.SELECTD_ID, 0);
                    mImvChooserMediator.setCurrResourceID(id);
                } else if (resultCode == Activity.RESULT_CANCELED){
                    mImvChooserMediator.setCurrResourceID(-1);
                }
                break;
            case BaseChooser.PASTER_REQUEST_CODE:
                if (mOverlayChooserView == null) {
                    return;
                }
                if (resultCode == Activity.RESULT_OK) {
                    int id = data.getIntExtra(MoreCaptionActivity.SELECTED_ID, 0);
                    mOverlayChooserView.setCurrResourceID(id);
                } else {
                    mOverlayChooserView.setCurrResourceID(-1);
                }
                break;
            default:
                break;
        }
    }

    public void setEditorService(EditorService editorService) {
        mEditorService = editorService;
    }

    public void setEffectChange(OnEffectChangeListener onEffectChangeListener) {
        mOnEffectChangeListener = onEffectChangeListener;
    }

    public void setOnEffectActionLister(OnEffectActionLister effectActionLister) {
        mOnEffectActionLister = effectActionLister;
    }

    /**
     * 设置view的可见状态, 会在activity的onStart和onStop中调用
     * @param isVisible true: 可见, false: 不可见
     */
    public void setVisibleStatus(boolean isVisible) {
        if (mAudioMixChooserMediator != null) {
            mAudioMixChooserMediator.setVisibleStatus(isVisible);
        }
    }
    //转场预览监听
    public void setOnTransitionPreviewListener(TransitionChooserView.OnPreviewListener onPreviewListener){
        mOnPreviewListener = onPreviewListener;
    }

    /**
     * 播放时间回调器
     * @param playerListener PlayerListener
     */
    public void setPlayerListener(AlivcEditView.PlayerListener playerListener) {
        mPlayerListener = playerListener;
    }
}
