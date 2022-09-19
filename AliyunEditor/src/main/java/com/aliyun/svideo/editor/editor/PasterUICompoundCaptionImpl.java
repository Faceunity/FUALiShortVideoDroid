/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.editor;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.common.utils.DensityUtil;
import com.aliyun.svideo.base.widget.beauty.seekbar.IndicatorUtils;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.bean.AlivcCaptionBorderBean;
import com.aliyun.svideo.editor.contant.CaptionConfig;
import com.aliyun.svideo.editor.editor.thumblinebar.OverlayThumbLineBar;
import com.aliyun.svideo.editor.editor.thumblinebar.ThumbLineOverlay;
import com.aliyun.svideo.editor.effects.caption.component.CaptionEditorPanelView;
import com.aliyun.svideo.editor.effects.caption.listener.OnCaptionChooserStateChangeListener;
import com.aliyun.svideo.editor.effects.caption.listener.OnVideoUpdateDurationListener;
import com.aliyun.svideo.editor.effects.caption.manager.AlivcEditorViewFactory;
import com.aliyun.svideo.editor.effects.caption.manager.CaptionManager;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.svideo.editor.util.CaptionFrameAnimationUtil;
import com.aliyun.svideo.editor.view.AlivcEditView;
import com.aliyun.svideo.editor.view.IAlivcEditView;
import com.aliyun.svideo.editor.widget.AliyunPasterCaptionBorderView;
import com.aliyun.svideosdk.common.AliyunColor;
import com.aliyun.svideosdk.common.AliyunFontStyle;
import com.aliyun.svideosdk.common.AliyunTypeface;
import com.aliyun.svideosdk.common.struct.project.Source;
import com.aliyun.svideosdk.common.struct.effect.ActionBase;
import com.aliyun.svideosdk.editor.AliyunIEditor;
import com.aliyun.svideosdk.editor.AliyunPasterManager;
import com.aliyun.svideosdk.editor.impl.AliyunPasterControllerCompoundCaption;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PasterUICompoundCaptionImpl extends AliyunBasePasterController implements OnCaptionChooserStateChangeListener {

    private static final String TAG = PasterUICompoundCaptionImpl.class.getName();
    private AliyunPasterControllerCompoundCaption mController;
    private AliyunPasterManager mAliyunPasterManager;
    protected OverlayThumbLineBar mThumbLBar;
    protected ThumbLineOverlay mThumbLineOverlay;
    protected Map<Integer, ThumbLineOverlay> lineOverlayMap = new HashMap<>();
    protected ActionBase mFrameAction;
    protected ActionBase mTempFrameAction;
    protected ActionBase mOldFrameAction;
    private int mControllerId;
    /**
     * 反转模式，部分功能不支持
     */
    private boolean mIsInvert;

    private int mFrameActionSelect;//字体动画选择的selectPosition


    protected boolean isEditStarted;
    private AliyunPasterCaptionBorderView mCaptionBorderView;
    private IAlivcEditView mIAliEditView;


    private final View.OnClickListener mOnBorderClickListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Object tag = v.getTag();
            if (tag instanceof Integer) {
                Integer tagIndex = (Integer) tag;
                if (tagIndex == 0) {
                    removePaster();
                } else if (tagIndex == 1) {
                    if (mCaptionBorderView != null) {
                        boolean isMirror = mCaptionBorderView.isMirror();
                        mirrorPaster(!isMirror);
                    }
                }
            }
        }
    };

    private final PointF pasterPostionPointF = new PointF();
    private final AliyunPasterCaptionBorderView.OnCaptionControllerChangeListener mOnBorderChangeListener = new AliyunPasterCaptionBorderView.OnCaptionControllerChangeListener() {
        @Override
        public void onControllerChanged(float roation, float[] scale, int left, int top, int right, int bottom) {
            if (mController != null && mIAliEditView != null) {
                pasterPostionPointF.x = (left + right) >> 1;
                pasterPostionPointF.y = (top + bottom) >> 1;
                CaptionManager.applyCaptionBorderChanged(mController, -roation, scale, pasterPostionPointF);
            }
        }

    };

    private OnVideoUpdateDurationListener mVideoUpdateDurationListener = new OnVideoUpdateDurationListener() {
        @Override
        public void onUpdateDuration(long duration) {
            if (mController != null) {
                //demo 视频播放进度 50ms更新一次 ，这里去掉误差
                long endTime = mController.getStartTime(TimeUnit.MILLISECONDS) + mController.getDuration(TimeUnit.MILLISECONDS) - 100;
                AlivcEditView alivcEditView = mIAliEditView.getAlivcEditView();
                AliyunIEditor aliyunIEditor = mIAliEditView.getAliyunIEditor();
                if (aliyunIEditor != null) {
                    long currentPlayPosition = aliyunIEditor.getPlayerController().getCurrentPlayPosition();
                    Log.d(TAG, "UpdateDuration: " + currentPlayPosition + "   duration:" + duration + "  endTime:" + endTime);
                    if (currentPlayPosition > endTime) {
                        alivcEditView.playingPause();
                        aliyunIEditor.seek(mController.getStartTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
                        mIAliEditView.removeVideoUpdateListener(mVideoUpdateDurationListener);
                    }
                }
            }
        }
    };

    public void updateParams(IAlivcEditView iAlivcEditView, AliyunPasterControllerCompoundCaption controllerCompoundCaption,
                             AliyunPasterManager aliyunPasterManager, OverlayThumbLineBar thumbLineBar) {
        mIAliEditView = iAlivcEditView;
        mController = controllerCompoundCaption;
        mControllerId = System.identityHashCode(mController);
        mAliyunPasterManager = aliyunPasterManager;
        mThumbLBar = thumbLineBar;
    }

    /**
     * 调整边框大小
     */
    private void adjustBorder() {
        if (mIAliEditView != null && mCaptionBorderView != null) {
            View sufaceView = mIAliEditView.getSufaceView();
            if (sufaceView != null) {
                AlivcCaptionBorderBean captionSize = CaptionManager.getCaptionSize(sufaceView.getLayoutParams(), mController);
                if (captionSize != null) {
                    mCaptionBorderView.bind(captionSize, mOnBorderChangeListener);
                }
            }
        }

    }

    /**
     * 添加字幕边框
     *
     * @param pasterContainer
     * @param surfaceView
     */
    public void showCaptionBorderView(ViewGroup pasterContainer, View surfaceView) {
        if (mController != null && surfaceView != null) {
            mCaptionBorderView = AlivcEditorViewFactory.obtainCaptionBorderView(pasterContainer);
            if (mCaptionBorderView != null) {
                AlivcCaptionBorderBean captionSize = CaptionManager.getCaptionSize(surfaceView.getLayoutParams(), mController);
                mCaptionBorderView.bind(captionSize, mOnBorderChangeListener);
                mCaptionBorderView.setTag(this);
                View cancel = mCaptionBorderView.findViewById(R.id.qupai_btn_edit_overlay_cancel);
                if (cancel != null) {
                    cancel.setTag(0);
                    cancel.setOnClickListener(mOnBorderClickListner);
                }
                View mirror = mCaptionBorderView.findViewById(R.id.qupai_btn_edit_overlay_mirror);
                if (mirror != null) {
                    mirror.setTag(1);
                    mirror.setOnClickListener(mOnBorderClickListner);
                }

            }
        }


    }


    public void hideCaptionBorderView() {
        if (mCaptionBorderView != null) {
            mCaptionBorderView.setVisibility(View.GONE);
        }

    }


    public AliyunPasterControllerCompoundCaption getController() {
        return mController;
    }

    public void mirrorPaster(boolean mirror) {
        if (mCaptionBorderView != null) {
            mCaptionBorderView.setMirror(mirror);
        }
    }


    @Override
    public boolean isPasterMirrored() {
        return mCaptionBorderView != null && mCaptionBorderView.isMirror();
    }

    @Override
    public void removePaster() {
        CaptionManager.removeCaption(mAliyunPasterManager, mController);
        hideTextEdit();
        if (mCaptionBorderView != null) {
            mCaptionBorderView.setVisibility(View.GONE);
        }
        lineOverlayMap.remove(mControllerId);

        if (mThumbLBar != null) {
            mThumbLBar.removeOverlay(mThumbLineOverlay);
        }

    }



    @Override
    public void editTimeStart() {

        if (mController != null) {
            mThumbLineOverlay = lineOverlayMap.get(mControllerId);
            if (mThumbLineOverlay != null) {
                mThumbLineOverlay.switchState(ThumbLineOverlay.STATE_ACTIVE);
            }
        }
        isEditStarted = true;

    }


    @Override
    public void editTimeCompleted() {
        if (mController == null) {
            Log.d(TAG, "editTimeCompleted: mController is null");
            return;
        }

        if (mCaptionBorderView != null) {
            mCaptionBorderView.setVisibility(View.GONE);
        }
        if (mThumbLineOverlay != null) {
            mThumbLineOverlay.switchState(ThumbLineOverlay.STATE_FIX);
        }

        hideTextEdit();

        if (TextUtils.isEmpty(mController.getText())) {
            removePaster();
        }
        isEditStarted = false;

    }


    /**
     * 隐藏缩略条覆盖视图
     */
    @Override
    public void hideOverlayView() {
        if (mThumbLineOverlay != null) {
            mThumbLineOverlay.getOverlayView().setVisibility(View.GONE);
        }
    }

    /**
     * 未完成将被移除
     *
     * @return false 移除
     */
    @Override
    public boolean isEditCompleted() {
        return !isEditStarted;
    }

    @Override
    public boolean contentContains(float x, float y) {
        if (mCaptionBorderView != null) {
            return mCaptionBorderView.contentContains(x, y);
        }
        return false;
    }

    @Override
    public void moveContent(float dx, float dy) {
        if (mCaptionBorderView != null) {
            mCaptionBorderView.moveContent(dx, dy);
        }
    }

    @Override
    public boolean isVisibleInTime(long time) {
        if (mController != null) {
            long start = mController.getStartTime(TimeUnit.MILLISECONDS);
            long duration = mController.getDuration(TimeUnit.MILLISECONDS);
            return time >= start &&
                   time <= start + duration;
        }
        return false;
    }

    @Override
    public void setPasterViewVisibility(int visibility) {
        if (mCaptionBorderView != null) {
            mCaptionBorderView.setVisibility(visibility);
        }
    }

    @Override
    public boolean canDrag() {
        if (mController != null) {
            List<ActionBase> frameAnimations = mController.getFrameAnimations();
            return frameAnimations == null || frameAnimations.size() == 0;
        }
        return super.canDrag();
    }

    @Override
    public void moveToCenter() {

    }

    @Override
    public boolean isPasterExists() {
        return true;
    }

    @Override
    public boolean isPasterRemoved() {
        return false;
    }

    @Override
    public void setOnlyApplyUI(boolean b) {
    }

    @Override
    public boolean isAddedAnimation() {
        if (mController != null) {
            List<ActionBase> frameAnimations = mController.getFrameAnimations();
            return frameAnimations != null && frameAnimations.size() > 0;
        }
        return false;
    }

    @Override
    public UIEditorPage getEditorPage() {
        return UIEditorPage.COMPOUND_CAPTION;
    }


    @Override
    public void showTextEdit(boolean isInvert) {
        mIsInvert = isInvert;
        showTextEdit();
    }

    public void showTextEdit() {
        CaptionEditorPanelView captionEditorPanelView = AlivcEditorViewFactory.obtainCaptionEditorPanelView(getRootView(), this);
        if (captionEditorPanelView != null) {
            captionEditorPanelView.refreshData();
            captionEditorPanelView.bringToFront();
            captionEditorPanelView.setVisibility(View.VISIBLE);
        }
    }

    public void hideTextEdit() {
        CaptionEditorPanelView captionEditorPanelView = AlivcEditorViewFactory.findCaptionEditorPanelView(getRootView());
        if (captionEditorPanelView != null) {
            captionEditorPanelView.setVisibility(View.GONE);
        }
    }


    public ViewGroup getRootView() {
        return mIAliEditView != null ? mIAliEditView.getAlivcEditView() : null;
    }


    @Override
    public void showTimeEdit() {
        if (mThumbLBar == null || mController == null) {
            return;
        }

        mThumbLineOverlay = lineOverlayMap.get(mControllerId);
        if (mThumbLineOverlay == null) {
            ThumbLineOverlay.ThumbLineOverlayView overlayView = new ThumbLineOverlay.ThumbLineOverlayView() {
                private final View rootView = LayoutInflater.from(mThumbLBar.getContext()).inflate(R.layout.alivc_editor_view_timeline_overlay, null);

                @Override
                public ViewGroup getContainer() {
                    return (ViewGroup) rootView;
                }

                @Override
                public View getHeadView() {
                    return rootView.findViewById(R.id.head_view);
                }

                @Override
                public View getTailView() {
                    return rootView.findViewById(R.id.tail_view);
                }

                @Override
                public View getMiddleView() {
                    return rootView.findViewById(R.id.middle_view);
                }
            };
            mThumbLineOverlay = mThumbLBar.addOverlay(mController.getStartTime(TimeUnit.MILLISECONDS),
                                mController.getDuration(TimeUnit.MILLISECONDS),
                                overlayView, CaptionConfig.CAPTION_MIN_DURATION/*最小限制500毫秒*/, false, UIEditorPage.COMPOUND_CAPTION,
            new ThumbLineOverlay.OnSelectedDurationChangeListener() {
                @Override
                public void onDurationChange(long startTime, long endTime, long duration) {
                    CaptionManager.applyDurationChanged(mController, startTime, duration);
                }
            });

            lineOverlayMap.put(mControllerId, mThumbLineOverlay);
        }
        mThumbLineOverlay.switchState(ThumbLineOverlay.STATE_ACTIVE);
    }


    @Override
    public void onCaptionTextChanged(String text) {
        CaptionManager.applyCaptionTextChanged(mController, text);
        if (mCaptionBorderView != null) {
            RectF captionSize = CaptionManager.getCaptionRectF(mIAliEditView.getSufaceView().getLayoutParams(), mController);
            View contentView = mCaptionBorderView.getContentView();
            ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
            layoutParams.width = (int) captionSize.width();
            layoutParams.height = (int) captionSize.height();
            contentView.setLayoutParams(layoutParams);
        }

    }

    @Override
    public void onCaptionTextColorChanged(AliyunColor aliyunColor) {
        CaptionManager.applyCaptionTextColorChanged(mController, aliyunColor);

    }

    @Override
    public void onCaptionTextBackgroundColorChanged(AliyunColor aliyunColor) {
        CaptionManager.applyCaptionTextBackgroundColorChanged(mController, aliyunColor);
    }

    @Override
    public void onCaptionTextBackgroundCornerRadiusChanged(int radiusInPx) {
        CaptionManager.applyCaptionTextBackgroundCornerRadiusChanged(mController, DensityUtil.sp2px(radiusInPx));
    }

    @Override
    public void onCaptionTextAlignmentChanged(int alignment) {
        CaptionManager.applyCaptionTextAlignmentChanged(mController, alignment);
    }

    @Override
    public void onCaptionTextFontTypeFaceChanged(AliyunTypeface aliyunTypeface) {
        CaptionManager.applyCaptionTextFontTypeFaceChanged(mController, aliyunTypeface);
    }


    @Override
    public void onCaptionTextFontTtfChanged(final Source fontSource) {
        CaptionManager.applyCaptionTextFontTtfChanged(mController, fontSource);
    }

    @Override
    public void onCaptionTextStrokeColorChanged(AliyunColor aliyunColor) {
        CaptionManager.applyCaptionTextStrokeColorChanged(mController, aliyunColor);
    }

    @Override
    public void onCaptionTextStrokeWidthChanged(int width) {
        CaptionManager.applyCaptionTextStrokeWidthChanged(mController, width);
    }

    @Override
    public void onCaptionTextShandowColorChanged(AliyunColor aliyunColor) {
        CaptionManager.applyCaptionTextShandowColorChanged(mController, aliyunColor);
    }

    @Override
    public void onCaptionTextShandowOffsetChanged(PointF shadowOffset) {
        CaptionManager.applyCaptionTextShandowOffsetChanged(mController, shadowOffset);
    }


    @Override
    public void onBubbleEffectTemplateChanged(final Source bubbleSource, final Source fontSource) {
        long currentDuration = mController.getDuration(TimeUnit.MILLISECONDS);
        //气泡需要同时设置图片和字体资源路径
        CaptionManager.applyBubbleEffectTemplateChanged(mController, bubbleSource);
        CaptionManager.applyCaptionTextFontTtfChanged(mController, fontSource);
        adjustBorder();
        CaptionManager.applyDurationChanged(mController, mController.getStartTime(TimeUnit.MILLISECONDS), currentDuration);
        if (mIAliEditView != null && bubbleSource != null && bubbleSource.getPath() != null) {
            mIAliEditView.addVideoUpdateListener(mVideoUpdateDurationListener);
            AlivcEditView alivcEditView = mIAliEditView.getAlivcEditView();
            AliyunIEditor aliyunIEditor = mIAliEditView.getAliyunIEditor();
            if (alivcEditView != null) {
                alivcEditView.playingPause();
                aliyunIEditor.seek(mController.getStartTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
                alivcEditView.playingResume();
            }
        }
    }


    @Override
    public void onFontEffectTemplateChanged(final Source templateSource) {
        CaptionManager.applyFontEffectTemplateChanged(mController, templateSource);
    }

    @Override
    public void onCaptionFrameAnimation(int animationIndex) {
        View contentView = mCaptionBorderView.getContentView();
        int left = contentView.getLeft();
        int top = contentView.getTop();
        int cx = left + contentView.getWidth() / 2;
        int cy = top + contentView.getHeight() / 2;
        View sufaceView = mIAliEditView.getSufaceView();
        ViewGroup.LayoutParams layoutParams = sufaceView.getLayoutParams();
        int width = layoutParams.width;
        int height = layoutParams.height;
        ActionBase action = CaptionFrameAnimationUtil.createAction(mCaptionBorderView.getContext(), animationIndex, mController.getDuration(), mController.getStartTime(), width, height,
                            cx, cy);
        CaptionManager.applyCaptionFrameAnimation(mIAliEditView, mController, action);
    }

    @Override
    public void onCaptionCancel() {

    }

    @Override
    public void onCaptionConfirm() {
        editTimeCompleted();
    }

    @Override
    public AliyunPasterControllerCompoundCaption getAliyunPasterController() {
        return getController();
    }

    @Override
    public boolean isInvert() {
        return mIsInvert;
    }

    @Override
    public int getTextMaxLines() {
        return 0;
    }

    @Override
    public Layout.Alignment getTextAlign() {
        return null;
    }

    @Override
    public int getTextPaddingX() {
        return 0;
    }

    @Override
    public int getTextPaddingY() {
        return 0;
    }

    @Override
    public int getTextFixSize() {
        return 0;
    }

    @Override
    public Bitmap getBackgroundBitmap() {
        return null;
    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public int getTextColor() {
        return 0;
    }

    @Override
    public String getPasterTextFont() {
        return null;
    }

    @Override
    public Source getPasterTextFontSource() {
        return null;
    }

    @Override
    public int getTextStrokeColor() {
        return 0;
    }

    @Override
    public boolean isTextHasStroke() {
        return false;
    }

    @Override
    public boolean isTextHasLabel() {
        return false;
    }

    @Override
    public int getTextBgLabelColor() {
        return 0;
    }

    @Override
    public int getPasterTextOffsetX() {
        return 0;
    }

    @Override
    public int getPasterTextOffsetY() {
        return 0;
    }

    @Override
    public int getPasterTextWidth() {
        return 0;
    }

    @Override
    public int getPasterTextHeight() {
        return 0;
    }

    @Override
    public float getPasterTextRotation() {
        return 0;
    }

    @Override
    public int getPasterWidth() {
        return 0;
    }

    @Override
    public int getPasterHeight() {
        return 0;
    }

    @Override
    public int getPasterCenterY() {
        return 0;
    }

    @Override
    public int getPasterCenterX() {
        return 0;
    }

    @Override
    public float getPasterRotation() {
        return 0;
    }

    @Override
    public Bitmap transToImage() {
        return null;
    }

    @Override
    public View getPasterView() {
        return mCaptionBorderView;
    }

    @Override
    public View getTextView() {
        return null;
    }


}

