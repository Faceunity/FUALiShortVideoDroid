package com.aliyun.svideo.recorder.view.effects.shape;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.svideo.base.widget.beauty.BeautyDetailSettingView;
import com.aliyun.svideo.base.widget.beauty.BeautyShapeConstants;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautyShapeParamsChangeListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnViewClickListener;
import com.aliyun.svideo.base.widget.beauty.sharp.BeautyShapeDetailSettingView;
import com.aliyun.svideo.base.widget.beauty.sharp.BeautyShapeParams;
import com.aliyun.svideo.record.R;
import com.aliyun.svideo.recorder.view.dialog.BaseChooser;

/**
 * 美型微调
 * @author xlx
 */
public class BeautyShapeDetailChooser extends BaseChooser implements OnBeautyShapeParamsChangeListener, OnViewClickListener,
    BeautyShapeDetailSettingView.OnBlanckViewClickListener {
    private BeautyShapeParams beautyParams;

    /**
     * 微调返回按钮点击
     */
    private OnViewClickListener onBackClickListener;
    private BeautyShapeDetailSettingView beautyShapeDetailSettingView;
    /**
     * 美型微调参数改变监听
     */
    private OnBeautyShapeParamsChangeListener onBeautyShapeParamsChangeListener;
    /**
     * 空白区域点击监听
     */
    private BeautyDetailSettingView.OnBlanckViewClickListener onBlankClickListener;
    private int beautyLevel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.QUDemoFullStyle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        beautyShapeDetailSettingView = new BeautyShapeDetailSettingView(getContext());
        return beautyShapeDetailSettingView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        beautyShapeDetailSettingView.setParams(beautyParams);
        beautyShapeDetailSettingView.setBeautyParamsChangeListener(this);
        beautyShapeDetailSettingView.setBackClickListener(this);
        beautyShapeDetailSettingView.setOnBlanckViewClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        beautyShapeDetailSettingView.setBeautyConstants(BeautyShapeConstants.CUT_FACE);
        beautyShapeDetailSettingView.setParams(beautyParams);
        beautyShapeDetailSettingView.setBeautyLevel(beautyLevel);
    }

    @Override
    public void onBeautyChange(BeautyShapeParams param) {
        if (onBeautyShapeParamsChangeListener != null) {
            onBeautyShapeParamsChangeListener.onBeautyChange(param);
        }
    }

    public void setOnBeautyParamsChangeListener(OnBeautyShapeParamsChangeListener listener) {
        this.onBeautyShapeParamsChangeListener = listener;
    }

    public void setBeautyShapeParams(BeautyShapeParams beautyParams) {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (beautyShapeDetailSettingView != null) {
            beautyShapeDetailSettingView.setBackClickListener(null);
            beautyShapeDetailSettingView.setBeautyParamsChangeListener(null);
            beautyShapeDetailSettingView.setOnBlanckViewClickListener(null);
            beautyShapeDetailSettingView = null;
        }
    }
}
