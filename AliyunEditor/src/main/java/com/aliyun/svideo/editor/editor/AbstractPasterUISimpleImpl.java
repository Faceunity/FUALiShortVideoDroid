/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.editor;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.editor.thumblinebar.OverlayThumbLineBar;
import com.aliyun.svideo.editor.editor.thumblinebar.ThumbLineOverlay;
import com.aliyun.svideo.editor.effects.caption.TextDialog;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.svideo.editor.effects.overlay.AnimationDialog;
import com.aliyun.svideo.editor.widget.AutoResizingTextView;
import com.aliyun.svideo.editor.widget.BaseAliyunPasterView;
import com.aliyun.svideosdk.common.struct.effect.EffectBase;
import com.aliyun.svideosdk.common.struct.project.Source;
import com.aliyun.svideosdk.editor.AliyunPasterController;
import com.aliyun.svideosdk.editor.pplayer.AnimPlayerView;
import com.aliyun.svideosdk.common.struct.effect.ActionBase;
import com.aliyun.svideosdk.common.struct.effect.EffectPaster;

import java.util.concurrent.TimeUnit;

public abstract class AbstractPasterUISimpleImpl extends AliyunBasePasterController {

    private static final String TAG = AbstractPasterUISimpleImpl.class.getName();

    protected AutoResizingTextView mText;
    public BaseAliyunPasterView mPasterView;
    protected AnimPlayerView animPlayerView;
    protected UIEditorPage mEditorPage;//区分贴纸是动图、字幕还是纯字体

    public AliyunPasterController mController;
    protected OverlayThumbLineBar mThumbLBar;
    protected ThumbLineOverlay mThumbLineOverlay;
    private boolean isDeleted;
    protected boolean isEditStarted;
    protected boolean mMoveDelay;
    protected ActionBase mFrameAction;
    protected ActionBase mTempFrameAction;
    protected ActionBase mOldFrameAction;

    private int mFrameActionSelect = -1;//字体动画选择的selectPosition
    private float mCenterX;
    private float mCenterY;
    private float[] originalCenter;

    private boolean isFirstTouch = true;



    public AbstractPasterUISimpleImpl(BaseAliyunPasterView pasterView, AliyunPasterController controller, OverlayThumbLineBar thumbLineBar) {
        mPasterView = pasterView;
        mController = controller;
        this.mThumbLBar = thumbLineBar;
        pasterView.setTag(this);
        mController.setPasterView(this);

        View transform = mPasterView.findViewById(R.id.qupai_btn_edit_overlay_transform);


        if (transform != null) {
            View.OnTouchListener rotationScaleBinding = new View.OnTouchListener() {
                private float mLastX;
                private float mLastY;
                private void update(float x, float y) {

                    View content = mPasterView.getContentView();
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

                    mPasterView.scaleContent(scale, scale);
                    mPasterView.rotateContent(rot, mCenterX, mCenterY);
                }

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        if (isFirstTouch) {
                            //原始中心点为view中心点
                            float[] center = new float[2];
                            center[0] = mPasterView.getWidth() / 2;
                            center[1] = mPasterView.getHeight() / 2;
                            if (center[0] != 0 && center[1] != 0) {
                                originalCenter = center;
                                isFirstTouch = false;
                            }
                        }
                        mLastX = v.getLeft() + event.getX();
                        mLastY = v.getTop() + event.getY();

                        float[] center = mPasterView.getCenter();
                        mCenterX =  center[0] - originalCenter[0];
                        mCenterY =  center[1] - originalCenter[1];
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
            transform.setOnTouchListener(rotationScaleBinding);
        }

        mPasterView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (isFirstTouch) {
                        originalCenter = mPasterView.getCenter();
                        isFirstTouch = false;
                    }
                    break;
                default:
                    break;
                }
                return false;
            }
        });

        View cancel = pasterView.findViewById(R.id.qupai_btn_edit_overlay_cancel);
        if (cancel != null) {
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePaster();
                }
            });
        }

        View mirror = pasterView.findViewById(R.id.qupai_btn_edit_overlay_mirror);
        if (mirror != null) {
            mirror.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isMirror = mPasterView.isMirror();
                    mirrorPaster(!isMirror);
                }
            });
        }

        editTimeStart();

    }


    public AliyunPasterController getController() {
        return mController;
    }

    public void mirrorPaster(boolean mirror) {
        mPasterView.setMirror(mirror);
    }

    @Override
    public UIEditorPage getEditorPage() {
        return mEditorPage;
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
        return mText.getFontPath();
    }

    @Override
    public Source getPasterTextFontSource() {
        return mText.getFontSource();
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
        return mPasterView;
    }

    @Override
    public View getTextView() {
        return mText;
    }

    @Override
    public boolean isPasterMirrored() {
        return mPasterView.isMirror();
    }

    @Override
    public void removePaster() {
        Log.i(TAG, "removePaster");
        isDeleted = true;
        mController.removePaster();
        ViewParent parent = mPasterView.getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(mPasterView);
        }
        mThumbLBar.removeOverlay(mThumbLineOverlay);
    }

    public boolean isPasterRemoved() {
        return isDeleted;
    }

    public boolean isPasterExists() {
        return mController.isPasterExists();
    }

    @Override
    public void editTimeStart() {
        if (isEditStarted) {
            return;
        }
        isEditStarted = true;
        mPasterView.setVisibility(View.VISIBLE);
        mPasterView.bringToFront();
        playPasterEffect();

        mController.editStart();
        if (mThumbLineOverlay != null) {
            mThumbLineOverlay.switchState(ThumbLineOverlay.STATE_ACTIVE);
        }
    }

    protected abstract void playPasterEffect();

    protected abstract void stopPasterEffect();


    @Override
    public void editTimeCompleted() {

        if (!mController.isRevert() && !mController.isOnlyApplyUI() && mPasterView.getWidth() == 0 && mPasterView.getHeight() == 0) {
            //1.isRevert为true时锁定了参数 - 动图撤销恢复
            //2.isOnlyApplyUI为true时 - 合成撤销恢复
            //初始化错误的时候，remove掉这个贴纸
            mController.removePaster();
            return;
        }

        mPasterView.setVisibility(View.INVISIBLE);
        stopPasterEffect();
        mController.editCompleted();
        mMoveDelay = false;
        if (mThumbLineOverlay != null) {
            mThumbLineOverlay.switchState(ThumbLineOverlay.STATE_FIX);
        }

    }

    /**
     * 隐藏缩略条覆盖视图
     */
    @Override
    public void hideOverlayView() {
        if (mThumbLineOverlay != null) {
            mThumbLineOverlay.getOverlayView().setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean isEditCompleted() {
        return !isPasterRemoved() && !isEditStarted;
    }

    @Override
    public boolean contentContains(float x, float y) {
        return mPasterView.contentContains(x, y);
    }

    @Override
    public void moveContent(float dx, float dy) {
        mPasterView.moveContent(dx, dy);
    }

    @Override
    public boolean isVisibleInTime(long time) {
        long start = mController.getPasterStartTime(TimeUnit.MILLISECONDS);
        long duration = mController.getPasterDuration(TimeUnit.MILLISECONDS);
        return time >= start &&
               time <= start + duration;
    }

    @Override
    public boolean isAddedAnimation() {
        return mFrameActionSelect != 0;
    }

    @Override
    public void showTextEdit(boolean isInvert) {
        if (mText == null) {
            return;
        }
        mText.setEditCompleted(true);
        mPasterView.setEditCompleted(true);
        TextDialog.EditTextInfo info = new TextDialog.EditTextInfo();
        info.dTextColor = mController.getConfigTextColor();
        info.dTextStrokeColor = mController.getConfigTextStrokeColor();
        info.isTextOnly = mController.getPasterType() == EffectPaster.PASTER_TYPE_TEXT;
        info.isShowAnimation = true;
        info.text = mText.getText().toString();
        info.textColor = mText.getCurrentTextColor();
        info.textStrokeColor = mText.getTextStrokeColor();
        info.font = mText.getFontPath();
        info.mAnimation = mOldFrameAction;
        info.mAnimationSelect = mFrameActionSelect;
        ViewGroup pasterViewPatent = (ViewGroup)getPasterView().getParent();
        if (pasterViewPatent != null) {
            info.layoutWidth = pasterViewPatent.getWidth();
            pasterViewPatent.setEnabled(false);
        }
        if (info.isTextOnly) {
            info.textWidth = getPasterWidth();
            info.textHeight = getPasterHeight();
        } else {
            info.textWidth = getPasterTextWidth();
            info.textHeight = getPasterTextHeight();
        }
        mPasterView.setVisibility(View.GONE);
        TextDialog textDialog = TextDialog.newInstance(info, isInvert);
        if (textDialog == null) {
            return;
        }
        textDialog.setOnStateChangeListener(new TextDialog.OnStateChangeListener() {
            @Override
            public void onTextEditCompleted(TextDialog.EditTextInfo result) {
                ViewGroup vg = (ViewGroup) mPasterView.getParent();
                if (vg == null) {
                    return;
                }
                vg.setEnabled(true);
                if (TextUtils.isEmpty(result.text) && mEditorPage == UIEditorPage.FONT) {
                    removePaster();
                    return;
                }
                mText.setText(result.text);
                mText.setCurrentColor(result.textColor);
                mText.setTextStrokeColor(result.textStrokeColor);
                if (result.isTextOnly) {
                    mPasterView.setContentWidth(result.textWidth);
                    mPasterView.setContentHeight(result.textHeight);
                }
                mText.setFontPath(result.font);
                mFrameAction = result.mAnimation;
                mTempFrameAction = result.mAnimation;
                mFrameActionSelect = result.mAnimationSelect;
                mText.setEditCompleted(true);
                mPasterView.setEditCompleted(true);
                if (isEditStarted) {
                    mPasterView.setVisibility(View.VISIBLE);
                }
            }
        });

        textDialog.show(((Activity) mPasterView.getContext()).getFragmentManager(), "textedit");
    }

    public void showAnimationDialog(boolean isInvert) {
        AnimationDialog.PasterInfo pasterInfo = new AnimationDialog.PasterInfo();
        pasterInfo.mAnimation = mOldFrameAction;
        pasterInfo.mAnimationSelect = mFrameActionSelect;
        AnimationDialog textDialog = AnimationDialog.newInstance(pasterInfo, isInvert);
        textDialog.setOnStateChangeListener(new AnimationDialog.OnConfirmListener() {
            @Override
            public void onCompleted(AnimationDialog.PasterInfo result) {
                mFrameAction = result.mAnimation;
                mTempFrameAction = result.mAnimation;
                mFrameActionSelect = result.mAnimationSelect;
            }
        });
        textDialog.show(((Activity) mPasterView.getContext()).getFragmentManager(), "pasterinfo");
    }


    @Override
    public void showTimeEdit() {
        if (!isPasterExists()) {
            return;
        }
        if (mThumbLineOverlay == null) {
            ThumbLineOverlay.ThumbLineOverlayView overlayView = new ThumbLineOverlay.ThumbLineOverlayView() {
                private View rootView = LayoutInflater.from(mPasterView.getContext()).inflate(R.layout.alivc_editor_view_timeline_overlay, null);

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
            UIEditorPage uiEditorPage = mEditorPage;
            //字体和字幕共用
            if (UIEditorPage.FONT == uiEditorPage) {
                uiEditorPage = UIEditorPage.CAPTION;
            }
            mThumbLineOverlay = mThumbLBar.addOverlay(mController.getPasterStartTime(TimeUnit.MILLISECONDS),
                                mController.getPasterDuration(TimeUnit.MILLISECONDS),
                                overlayView, 500/*最小限制500毫秒*/, false, uiEditorPage,
            new ThumbLineOverlay.OnSelectedDurationChangeListener() {
                @Override
                public void onDurationChange(long startTime, long endTime, long duration) {
                    mController.setPasterStartTime(startTime, TimeUnit.MILLISECONDS);
                    mController.setPasterDuration(duration, TimeUnit.MILLISECONDS);
                    if (animPlayerView != null) {
                        animPlayerView.setPlayTime(TimeUnit.MILLISECONDS.toMicros(startTime), TimeUnit.MILLISECONDS.toMicros(endTime));
                        Log.i(TAG, "showTimeEdit: startTime :" + startTime + " , endTime :" + endTime);
                    }
                }
            });

            Log.i(TAG, "showTimeEdit: duration :" + mController.getPasterDuration());
        }
        mThumbLineOverlay.switchState(ThumbLineOverlay.STATE_ACTIVE);
    }

    public void setFrameAction(ActionBase actionBase) {
        mFrameAction = actionBase;
        mOldFrameAction = actionBase;
    }

    public ActionBase getFrameAction() {
        return mFrameAction;
    }

    public int getFrameSelectPosition() {
        return mFrameActionSelect;
    }

    public void setFrameSelectedPosition(int position) {
        mFrameActionSelect = position;
    }

    public ActionBase getTempFrameAction() {
        return mTempFrameAction;
    }

    public void setTempFrameAction(ActionBase mTempFrameAction) {
        this.mTempFrameAction = mTempFrameAction;
    }

    @Override
    public void setPasterViewVisibility(int visibility) {
        if (mPasterView != null) {
            mPasterView.setVisibility(visibility);
        }
    }

    public void setOnlyApplyUI(boolean bool) {
        if (getController() != null) {
            getController().setOnlyApplyUI(bool);
        }
    }

    public EffectBase getEffect() {
        if (getController() != null) {
            return getController().getEffect();
        }
        return null;
    }
}

