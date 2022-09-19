/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.caption.adapter.holder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.svideo.common.utils.ThreadUtils;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.contant.CaptionConfig;
import com.aliyun.svideo.editor.effects.caption.adapter.CaptionAnimationAdapter;
import com.aliyun.svideo.editor.effects.control.SpaceItemDecoration;
import com.aliyun.svideo.editor.effects.caption.manager.CaptionManager;
import com.aliyun.svideo.editor.util.FixedToastUtils;
import com.aliyun.svideo.editor.effects.caption.listener.OnCaptionChooserStateChangeListener;
import com.aliyun.svideosdk.common.struct.effect.ActionBase;
import com.aliyun.svideosdk.common.struct.effect.ActionFade;
import com.aliyun.svideosdk.common.struct.effect.ActionScale;
import com.aliyun.svideosdk.common.struct.effect.ActionTranslate;
import com.aliyun.svideosdk.common.struct.effect.ActionWipe;
import com.aliyun.svideosdk.editor.impl.AliyunPasterControllerCompoundCaption;

import java.util.List;


/**
 * 字幕动画
 */
public class CaptionAnimationPanelViewHolder extends BaseCaptionViewHolder {
    private CaptionAnimationAdapter mCaptionAnimationAdapter;
    private int mCurrentCaptionControlId;

    public CaptionAnimationPanelViewHolder(Context context, String title, OnCaptionChooserStateChangeListener onCaptionChooserStateChangeListener) {
        super(context, title, onCaptionChooserStateChangeListener);
    }

    @Override
    public View onCreateView(Context context) {
        return LayoutInflater.from(context).inflate(R.layout.alivc_editor_caption_animation_container, null, false);
    }

    @Override
    public void onBindViewHolder() {
    }

    @Override
    public void onTabClick() {
        lazyInit();
    }

    private void lazyInit() {
        View itemView = getItemView();
        if (itemView != null && mCaptionAnimationAdapter == null) {
            RecyclerView recyclerView = itemView.findViewById(R.id.font_animation_view);
            mCaptionAnimationAdapter = new CaptionAnimationAdapter(itemView.getContext());
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 4);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.addItemDecoration(new SpaceItemDecoration(
                                               getContext().getResources().getDimensionPixelSize(R.dimen.list_item_space)));
            recyclerView.setAdapter(mCaptionAnimationAdapter);
            mCaptionAnimationAdapter.setOnItemClickListener(mClickListener);
            mCurrentCaptionControlId = CaptionManager.getCaptionControllerId(getCaptionChooserStateChangeListener());

            final int currentIndex = getCurrentIndex();
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCaptionAnimationAdapter.setSelectPosition(currentIndex);
                }
            });

        } else {
            notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (mCaptionAnimationAdapter != null) {
            ThreadUtils.runOnSubThread(new Runnable() {
                @Override
                public void run() {
                    int captionControllerId = CaptionManager.getCaptionControllerId(getCaptionChooserStateChangeListener());
                    if (mCurrentCaptionControlId != captionControllerId) {
                        final int currentIndex = getCurrentIndex();
                        mCurrentCaptionControlId = captionControllerId;
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mCaptionAnimationAdapter.setSelectPosition(currentIndex);
                            }
                        });
                    }
                }
            });
        }
    }


    /**
     * 获取当前选中动画
     *
     * @return int
     */
    private int getCurrentIndex() {
        int currentIndex = 0;
        OnCaptionChooserStateChangeListener listener = getCaptionChooserStateChangeListener();
        if (listener != null) {
            AliyunPasterControllerCompoundCaption aliyunPasterController = listener.getAliyunPasterController();
            if (aliyunPasterController != null) {
                List<ActionBase> frameAnimations = aliyunPasterController.getFrameAnimations();
                if (frameAnimations != null && frameAnimations.size() > 0) {
                    ActionBase actionBase = frameAnimations.get(frameAnimations.size() - 1);
                    currentIndex = coverActionBase(actionBase);
                } else {
                    currentIndex = 0;
                }
            }
        }
        return currentIndex;
    }


    private final CaptionAnimationAdapter.OnItemClickListener mClickListener = new CaptionAnimationAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int animPosition) {
            OnCaptionChooserStateChangeListener captionChooserStateChangeListener = getCaptionChooserStateChangeListener();
            if (captionChooserStateChangeListener != null && captionChooserStateChangeListener.isInvert()) {
                FixedToastUtils.show(getContext(), getContext().getString(R.string.alivc_editor_dialog_caption_tip_not_support));
                return;
            }
            if (getCaptionChooserStateChangeListener() != null) {
                getCaptionChooserStateChangeListener().onCaptionFrameAnimation(animPosition);
            }
        }

    };

    private int coverActionBase(ActionBase actionBase) {
        if (actionBase == null) {
            return CaptionConfig.EFFECT_NONE;
        } else {
            return Integer.parseInt(actionBase.getResId());
        }
    }





}
