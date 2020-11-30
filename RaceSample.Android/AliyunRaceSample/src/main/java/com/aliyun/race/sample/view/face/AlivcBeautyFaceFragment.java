package com.aliyun.race.sample.view.face;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.race.sample.R;
import com.aliyun.race.sample.bean.BeautyLevel;
import com.aliyun.race.sample.bean.BeautyMode;
import com.aliyun.race.sample.bean.BeautyParams;
import com.aliyun.race.sample.utils.SharedPreferenceUtils;
import com.aliyun.race.sample.view.IPageTab;
import com.aliyun.race.sample.view.listener.OnBeautyDetailClickListener;
import com.aliyun.race.sample.view.listener.OnBeautyFaceItemSeletedListener;

import java.util.HashMap;
import java.util.Map;

/**
 * 美颜对话框页面
 *
 * @author xlx
 */
public class AlivcBeautyFaceFragment extends Fragment implements IPageTab,
        OnBeautyFaceItemSeletedListener {

    private static final String TAG = AlivcBeautyFaceFragment.class.getSimpleName();

    private AlivcBeautyFaceSettingView mBeautySettingView;
    /**
    /**
     * item选中listener 0~5
     */
    private OnBeautyFaceItemSeletedListener mOnItemSeletedListener;
    /**
     * 美颜微调按钮点击listener
     */
    private OnBeautyDetailClickListener mOnBeautyDetailClickListener;

    private BeautyParams mBeautyParams;

    private String mTabTitle;

    @Override
    public void setTabTitle(String tabTitle) {
        mTabTitle = tabTitle;
    }

    @Override
    public String getTabTitle() {
        return mTabTitle;
    }

    @Override
    public int getTabIcon() {
        return R.mipmap.alivc_svideo_icon_tab_beauty_face;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mBeautySettingView = new AlivcBeautyFaceSettingView(getContext(), true, SharedPreferenceUtils.getBeautyMode(getContext()));
        mBeautySettingView.setOnBeautyItemSelecedtListener(this);
        mBeautySettingView.setParams(mBeautyParams);
        mBeautySettingView.setBeautyMode(BeautyMode.Advanced, true);
        mBeautySettingView.setOnBeautyDetailClickListener(new OnBeautyDetailClickListener() {
            @Override
            public void onDetailClick() {
                if (mOnBeautyDetailClickListener != null) {
                    mOnBeautyDetailClickListener.onDetailClick();
                }
            }
        });
        Log.e(TAG, "onCreateView: " );
        return mBeautySettingView;
    }


    public void setOnBeautyDetailClickListener(OnBeautyDetailClickListener listener) {
        this.mOnBeautyDetailClickListener = listener;
    }


    public void setOnBeautyFaceItemSeletedListener(OnBeautyFaceItemSeletedListener listener) {
        this.mOnItemSeletedListener = listener;
    }

    public void updatePageIndex(int position) {
        mBeautySettingView.updateTabIndex(position);

        boolean showTips = SharedPreferenceUtils.getBeautyFineTuningTips(getContext().getApplicationContext());
        if (showTips) {
            mBeautySettingView.showTips();
            SharedPreferenceUtils.setBeautyFineTuningTips(getContext().getApplicationContext(), false);
        }
    }

    @Override
    public void onNormalSelected(int postion, BeautyLevel beautyLevel) {
        if (mOnItemSeletedListener != null) {
            mOnItemSeletedListener.onNormalSelected(postion, beautyLevel);
        }
    }

    @Override
    public void onAdvancedSelected(int postion, BeautyLevel beautyLevel) {
        if (mOnItemSeletedListener != null) {
            mOnItemSeletedListener.onAdvancedSelected(postion, beautyLevel);
        }
    }

    /**
     * 设置美颜参数
     *
     * @param beautyParams
     */
    public void setBeautyParams(BeautyParams beautyParams) {
        this.mBeautyParams = beautyParams;
    }


    Map<String, Integer> map = new HashMap<>();

    @Override
    public void onStart() {
        super.onStart();
        map.put("advance", SharedPreferenceUtils.getBeautyFaceLevel(getContext()));
        map.put("normal", SharedPreferenceUtils.getBeautyNormalFaceLevel(getContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        mBeautySettingView.setDefaultSelect(map);
    }
}
