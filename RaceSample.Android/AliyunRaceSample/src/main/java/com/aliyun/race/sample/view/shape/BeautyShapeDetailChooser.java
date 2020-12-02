package com.aliyun.race.sample.view.shape;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.race.sample.R;
import com.aliyun.race.sample.bean.BeautyShapeConstants;
import com.aliyun.race.sample.bean.BeautyShapeParams;
import com.aliyun.race.sample.view.BaseChooser;
import com.aliyun.race.sample.view.listener.OnBeautyShapeParamsChangeListener;
import com.aliyun.race.sample.view.listener.OnViewClickListener;

/**
 * 美型微调
 * @author xlx
 */
public class BeautyShapeDetailChooser extends BaseChooser implements OnBeautyShapeParamsChangeListener, OnViewClickListener,
     BeautyShapeDetailSettingView.OnBlanckViewClickListener {
    private BeautyShapeParams mBeautyParams;

    /**
     * 微调返回按钮点击
     */
    private OnViewClickListener mOnBackClickListener;
    private BeautyShapeDetailSettingView mBeautyShapeDetailSettingView;
    /**
     * 美型微调参数改变监听
     */
    private OnBeautyShapeParamsChangeListener mOnBeautyShapeParamsChangeListener;
    /**
     * 空白区域点击监听
     */
    private int mBeautyLevel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.QUDemoFullStyle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBeautyShapeDetailSettingView = new BeautyShapeDetailSettingView(getContext());
        return mBeautyShapeDetailSettingView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBeautyShapeDetailSettingView.setParams(mBeautyParams);
        mBeautyShapeDetailSettingView.setBeautyParamsChangeListener(this);
        mBeautyShapeDetailSettingView.setBackClickListener(this);
        mBeautyShapeDetailSettingView.setOnBlanckViewClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mBeautyShapeDetailSettingView.setBeautyConstants(BeautyShapeConstants.CUT_FACE);
        mBeautyShapeDetailSettingView.setParams(mBeautyParams);
        mBeautyShapeDetailSettingView.setBeautyLevel(mBeautyLevel);
    }

    @Override
    public void onBeautyChange(BeautyShapeParams param) {
        if (mOnBeautyShapeParamsChangeListener != null) {
            mOnBeautyShapeParamsChangeListener.onBeautyChange(param);
        }
    }

    public void setOnBeautyParamsChangeListener(OnBeautyShapeParamsChangeListener listener) {
        this.mOnBeautyShapeParamsChangeListener = listener;
    }

    public void setBeautyShapeParams(BeautyShapeParams beautyParams) {
        this.mBeautyParams = beautyParams;

    }

    /**
     * 微调返回按钮点击
     */
    @Override
    public void onClick() {
        if (mOnBackClickListener != null) {
            mOnBackClickListener.onClick();
        }
    }

    public void setOnBackClickListener(OnViewClickListener listener) {
        this.mOnBackClickListener = listener;
    }

    @Override
    public void onBlankClick() {
        dismiss();
    }


    public void setBeautyLevel(int beautyLevel) {
        this.mBeautyLevel = beautyLevel;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mBeautyShapeDetailSettingView != null) {
            mBeautyShapeDetailSettingView.setBackClickListener(null);
            mBeautyShapeDetailSettingView.setBeautyParamsChangeListener(null);
            mBeautyShapeDetailSettingView.setOnBlanckViewClickListener(null);
            mBeautyShapeDetailSettingView = null;
        }
    }
}
