package com.aliyun.race.sample.view.face;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.race.sample.R;
import com.aliyun.race.sample.bean.BeautyParams;
import com.aliyun.race.sample.view.BaseChooser;
import com.aliyun.race.sample.view.BeautyConstants;
import com.aliyun.race.sample.view.listener.OnBeautyParamsChangeListener;
import com.aliyun.race.sample.view.listener.OnViewClickListener;

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
    private BeautyParams mBeautyParams;
    /**
     * 美颜微调参数改变监听
     */
    private OnBeautyParamsChangeListener mOnBeautyParamsChangeListener;
    /**
     * 微调返回按钮点击
     */
    private OnViewClickListener mOnBackClickListener;
    private BeautyDetailSettingView mBeautyDetailSettingView;
    private BeautyDetailSettingView.OnBlanckViewClickListener mOnBlankClickListener;
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
        return new BeautyDetailSettingView(getContext());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBeautyDetailSettingView = (BeautyDetailSettingView)view;
        mBeautyDetailSettingView.setBeautyParamsChangeListener(this);
        mBeautyDetailSettingView.setBackClickListener(this);
        mBeautyDetailSettingView.setOnBlanckViewClickListener(this);
        mBeautyDetailSettingView.updateDetailLayout( TAB_POSITION_BEAUTY_FACE);
    }

    @Override
    public void onBeautyChange(BeautyParams param) {
        if (mOnBeautyParamsChangeListener != null) {
            mOnBeautyParamsChangeListener.onBeautyChange(param);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mBeautyDetailSettingView.setBeautyConstants(BeautyConstants.BUFFING);
        mBeautyDetailSettingView.setParams(mBeautyParams);
        mBeautyDetailSettingView.setBeautyLevel(mBeautyLevel);
    }

    public void setOnBeautyParamsChangeListener(OnBeautyParamsChangeListener listener) {
        this.mOnBeautyParamsChangeListener = listener;
    }

    public void setBeautyParams(BeautyParams beautyParams) {
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

    /**
     * dialog空白区域点击事件
     */
    @Override
    public void onBlankClick() {
        if (mOnBlankClickListener != null) {
            mOnBlankClickListener.onBlankClick();
        }
        dismiss();
    }

    public void setOnBlankClickListener(BeautyDetailSettingView.OnBlanckViewClickListener listener) {
        this.mOnBlankClickListener = listener;
    }

    public void setBeautyLevel(int mBeautyLevel) {
        this.mBeautyLevel = mBeautyLevel;
    }
}
