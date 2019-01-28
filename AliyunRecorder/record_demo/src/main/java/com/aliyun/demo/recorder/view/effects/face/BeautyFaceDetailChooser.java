package com.aliyun.demo.recorder.view.effects.face;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.demo.recorder.view.dialog.BaseChooser;
import com.aliyun.svideo.base.widget.beauty.BeautyConstants;
import com.aliyun.svideo.base.widget.beauty.BeautyDetailSettingView;
import com.aliyun.svideo.base.widget.beauty.BeautyParams;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautyParamsChangeListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnViewClickListener;

/**
 * 美颜微调
 * @author xlx
 */
public class BeautyFaceDetailChooser extends BaseChooser implements OnBeautyParamsChangeListener, OnViewClickListener,
    BeautyDetailSettingView.OnBlanckViewClickListener {
    /**
     * 美颜tab所在下标: 1
     */
    private static final int TAB_POSITION_BEAUTY_FACE = 1;
    private BeautyParams beautyParams;
    /**
     * 美颜微调参数改变监听
     */
    private OnBeautyParamsChangeListener onBeautyParamsChangeListener;
    /**
     * 微调返回按钮点击
     */
    private OnViewClickListener onBackClickListener;
    private BeautyDetailSettingView beautyDetailSettingView;
    private BeautyDetailSettingView.OnBlanckViewClickListener onBlankClickListener;
    private int beautyLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return new BeautyDetailSettingView(getContext());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        beautyDetailSettingView = (BeautyDetailSettingView)view;
        beautyDetailSettingView.setBeautyParamsChangeListener(this);
        beautyDetailSettingView.setBackClickListener(this);
        beautyDetailSettingView.setOnBlanckViewClickListener(this);
        beautyDetailSettingView.updateDetailLayout( TAB_POSITION_BEAUTY_FACE);
    }

    @Override
    public void onBeautyChange(BeautyParams param) {
        if (onBeautyParamsChangeListener != null) {
            onBeautyParamsChangeListener.onBeautyChange(param);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        beautyDetailSettingView.setBeautyConstants(BeautyConstants.BUFFING);
        beautyDetailSettingView.setParams(beautyParams);
        beautyDetailSettingView.setBeautyLevel(beautyLevel);
    }

    public void setOnBeautyParamsChangeListener(OnBeautyParamsChangeListener listener) {
        this.onBeautyParamsChangeListener = listener;
    }

    public void setBeautyParams(BeautyParams beautyParams) {
        this.beautyParams = beautyParams;
    }
    /**
     * 微调返回按钮点击
     */
    @Override
    public void onClick() {
        if (onBackClickListener != null) {
            onBackClickListener.onClick();
        }
    }

    public void setOnBackClickListener(OnViewClickListener listener) {
        this.onBackClickListener = listener;
    }

    /**
     * dialog空白区域点击事件
     */
    @Override
    public void onBlankClick() {
        if (onBlankClickListener != null) {
            onBlankClickListener.onBlankClick();
        }
        dismiss();
    }

    public void setOnBlankClickListener(BeautyDetailSettingView.OnBlanckViewClickListener listener) {
        this.onBlankClickListener = listener;
    }

    public void setBeautyLevel(int beautyLevel) {
        this.beautyLevel = beautyLevel;
    }
}
