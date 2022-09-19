/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.overlay;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.effects.control.SpaceItemDecoration;
import com.aliyun.svideo.editor.util.FixedToastUtils;
import com.aliyun.svideosdk.common.struct.effect.ActionBase;
import com.aliyun.svideosdk.common.struct.effect.ActionFade;
import com.aliyun.svideosdk.common.struct.effect.ActionScale;
import com.aliyun.svideosdk.common.struct.effect.ActionTranslate;
import com.aliyun.svideosdk.common.struct.effect.ActionWipe;
import java.io.Serializable;

public class AnimationDialog extends DialogFragment {

    private View mConfirm;
    ActionBase mActionBase;
    private OnConfirmListener onConfirmListener;

    //字幕动效
    public static final int EFFECT_NONE = 0, EFFECT_UP = 1, EFFECT_RIGHT = 4, EFFECT_LEFT = 3, EFFECT_DOWN = 2,
                            EFFECT_LINEARWIPE = 6, EFFECT_FADE = 5, EFFECT_SCALE = 7;
    public static final int[] POSITION_FONT_ANIM_ARRAY = {EFFECT_NONE, EFFECT_UP, EFFECT_RIGHT, EFFECT_LEFT,
                                                          EFFECT_DOWN, EFFECT_LINEARWIPE, EFFECT_FADE, EFFECT_SCALE
                                                         };
    private int mAnimationSelectPosition;



    private PasterInfo pasterInfo;


    private View mBack;
    public static boolean sIsShowing = false;

    /**
     * 是否倒放，倒放时特效不支持
     */
    private boolean mUseInvert = false;

    public static AnimationDialog newInstance(PasterInfo editInfo,boolean isInvert) {
        if (sIsShowing) {
            return null;
        }
        sIsShowing = true;
        AnimationDialog dialog = new AnimationDialog();
        Bundle b = new Bundle();
        b.putSerializable("edit", editInfo);
        b.putBoolean("invert", isInvert);
        dialog.setArguments(b);
        return dialog;
    }

    public static class PasterInfo implements Serializable {

        public ActionBase mAnimation;
        public int mAnimationSelect = -1;//字体动画选择的selectPosition
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        sIsShowing = false;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog != null) {
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dialog.dismiss();
                        return true;
                    }
                    return false;
                }
            });
            dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        View contentView = View.inflate(getActivity(), R.layout.alivc_editor_animation_dialog_text, null);

        pasterInfo = (PasterInfo) getArguments().getSerializable("edit");
        mUseInvert = getArguments().getBoolean("invert");
        if (pasterInfo == null) {
            dismiss();
            return contentView;
        }

        mActionBase = pasterInfo.mAnimation;
        mConfirm = contentView.findViewById(R.id.iv_confirm);
        mBack = contentView.findViewById(R.id.iv_back);
        initFontAnimationView(contentView);
        LayoutParams localLayoutParams = getDialog().getWindow().getAttributes();
        localLayoutParams.gravity = Gravity.BOTTOM;
        localLayoutParams.width = LayoutParams.MATCH_PARENT;
        setOnClick();
        return contentView;
    }

    /**
     * 初始化字体动画View
     *
     * @param contentView 布局根容器
     */
    private void initFontAnimationView(View contentView) {

        RecyclerView recyclerView = contentView.findViewById(R.id.font_animation_view);
        AnimationAdapter animationAdapter = new AnimationAdapter(contentView.getContext());
        if (pasterInfo.mAnimationSelect == -1 && mActionBase != null) {
            if (mActionBase instanceof ActionTranslate) {
                if (((ActionTranslate) mActionBase).getToPointY() == 1f) {
                    pasterInfo.mAnimationSelect = EFFECT_UP;
                } else if (((ActionTranslate) mActionBase).getToPointY() == -1f) {
                    pasterInfo.mAnimationSelect = EFFECT_DOWN;
                } else if (((ActionTranslate) mActionBase).getToPointX() == 1f) {
                    pasterInfo.mAnimationSelect = EFFECT_RIGHT;
                } else if (((ActionTranslate) mActionBase).getToPointX() == -1f) {
                    pasterInfo.mAnimationSelect = EFFECT_LEFT;
                } else {
                    if (((ActionTranslate) mActionBase).getFromPointX() == -1f) {
                        //向右平移
                        pasterInfo.mAnimationSelect = EFFECT_RIGHT;
                    } else if (((ActionTranslate) mActionBase).getFromPointX() == 1f) {
                        //向左平移
                        pasterInfo.mAnimationSelect = EFFECT_LEFT;
                    } else if (((ActionTranslate) mActionBase).getFromPointY() > 0) {
                        //向下平移
                        pasterInfo.mAnimationSelect = EFFECT_DOWN;
                    } else if (((ActionTranslate) mActionBase).getFromPointY() < 0) {
                        //向上平移
                        pasterInfo.mAnimationSelect = EFFECT_UP;
                    } else {
                        pasterInfo.mAnimationSelect = EFFECT_NONE;
                    }
                }
            } else if (mActionBase instanceof ActionScale) {
                pasterInfo.mAnimationSelect = EFFECT_SCALE;
            } else if (mActionBase instanceof ActionWipe) {
                pasterInfo.mAnimationSelect = EFFECT_LINEARWIPE;
            } else if (mActionBase instanceof ActionFade) {
                pasterInfo.mAnimationSelect = EFFECT_FADE;
            } else {
                pasterInfo.mAnimationSelect = EFFECT_NONE;
            }
        }
        if (pasterInfo.mAnimationSelect != -1) {
            mAnimationSelectPosition = pasterInfo.mAnimationSelect;
            animationAdapter.setSelectPosition(pasterInfo.mAnimationSelect);
        }
        animationAdapter.setOnItemClickListener(mOnItemClickListener);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(contentView.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new SpaceItemDecoration(
                                           contentView.getContext().getResources().getDimensionPixelSize(R.dimen.list_item_space)));
        recyclerView.setAdapter(animationAdapter);

    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(int animPosition) {
            if (mUseInvert) {
                FixedToastUtils.show(mConfirm.getContext(), mConfirm.getContext().getString(R.string.alivc_editor_dialog_caption_tip_not_support));
                return;
            }
            mAnimationSelectPosition = animPosition;

            switch (animPosition) {
            case EFFECT_NONE:
                mActionBase = null;
                break;
            case EFFECT_UP:
                mActionBase = new ActionTranslate();
                ((ActionTranslate) mActionBase).setToPointY(1f);
                break;
            case EFFECT_RIGHT:
                mActionBase = new ActionTranslate();
                ((ActionTranslate) mActionBase).setToPointX(1f);
                break;
            case EFFECT_LEFT:
                mActionBase = new ActionTranslate();
                ((ActionTranslate) mActionBase).setToPointX(-1f);
                break;
            case EFFECT_DOWN:
                mActionBase = new ActionTranslate();
                ((ActionTranslate) mActionBase).setToPointY(-1f);
                break;
            case EFFECT_SCALE:
                mActionBase = new ActionScale();
                ((ActionScale) mActionBase).setFromScale(1f);
                ((ActionScale) mActionBase).setToScale(0.25f);
                break;
            case EFFECT_LINEARWIPE:
                mActionBase = new ActionWipe();
                ((ActionWipe) mActionBase).setWipeMode(ActionWipe.WIPE_MODE_DISAPPEAR);
                ((ActionWipe) mActionBase).setDirection(ActionWipe.DIRECTION_RIGHT);
                break;
            case EFFECT_FADE:
                mActionBase = new ActionFade();
                ((ActionFade) mActionBase).setFromAlpha(1.0f);
                ((ActionFade) mActionBase).setToAlpha(0.2f);
                break;
            default:
                break;
            }
        }
    };



    @SuppressLint("ClickableViewAccessibility")
    private void setOnClick() {

        if (mBack != null) {
            mBack.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }

        mConfirm.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                pasterInfo.mAnimation = mActionBase;
                pasterInfo.mAnimationSelect = mAnimationSelectPosition;
                if(onConfirmListener != null){
                    onConfirmListener.onCompleted(pasterInfo);
                }
                dismiss();
            }
        });

    }


    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        Log.d("Dialog", "Dialog oncreate的时间：" + System.currentTimeMillis());
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AnimationDlgStyle);

    }

    @Override
    public void onResume() {
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
        super.onResume();
    }

    public void setOnStateChangeListener(OnConfirmListener listener) {
        this.onConfirmListener = listener;
    }
    public interface OnConfirmListener {
        void onCompleted(PasterInfo result);

    }


    public interface OnItemClickListener {
        void onItemClick(int animPosition);
    }


}
