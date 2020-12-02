package com.aliyun.svideo.recorder.view.effects.face;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.aliyun.svideo.record.R;
import com.aliyun.svideo.recorder.util.SharedPreferenceUtils;
import com.aliyun.svideo.recorder.view.dialog.IPageTab;
import com.aliyun.svideo.base.widget.beauty.BeautyParams;
import com.aliyun.svideo.base.widget.beauty.enums.BeautyLevel;
import com.aliyun.svideo.base.widget.beauty.enums.BeautyMode;
import com.aliyun.svideo.base.widget.beauty.face.AlivcBeautyFaceSettingView;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautyDetailClickListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautyFaceItemSeletedListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautyModeChangeListener;

import java.util.HashMap;
import java.util.Map;

/**
 * 美颜对话框页面
 *
 * @author xlx
 */
public class AlivcBeautyFaceFragment extends Fragment implements IPageTab, OnBeautyModeChangeListener,
    OnBeautyFaceItemSeletedListener {

    private static final String TAG = AlivcBeautyFaceFragment.class.getSimpleName();

    private AlivcBeautyFaceSettingView beautySettingView;
    /**
     * 普通or高级改变listener
     */
    private OnBeautyModeChangeListener onBeautyModeChangeListener;
    /**
     * item选中listener 0~5
     */
    private OnBeautyFaceItemSeletedListener onItemSeletedListener;
    /**
     * 美颜微调按钮点击listener
     */
    private OnBeautyDetailClickListener onBeautyDetailClickListener;

    private BeautyParams beautyParams;

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

        beautySettingView = new AlivcBeautyFaceSettingView(getContext(), true, SharedPreferenceUtils.getBeautyMode(getContext()));
        beautySettingView.setOnBeautyLevelChangeListener(this);
        beautySettingView.setOnBeautyItemSelecedtListener(this);
        beautySettingView.setParams(beautyParams);
        beautySettingView.setBeautyMode(BeautyMode.Advanced, true);
        beautySettingView.setOnBeautyDetailClickListener(new OnBeautyDetailClickListener() {
            @Override
            public void onDetailClick() {
                if (onBeautyDetailClickListener != null) {
                    onBeautyDetailClickListener.onDetailClick();
                }
            }
        });
        Log.e(TAG, "onCreateView: " );
        return beautySettingView;
    }


    public void setOnBeautyDetailClickListener(OnBeautyDetailClickListener listener) {
        this.onBeautyDetailClickListener = listener;
    }


    public void setOnBeautyFaceItemSeletedListener(OnBeautyFaceItemSeletedListener listener) {
        this.onItemSeletedListener = listener;
    }

    public void updatePageIndex(int position) {
        beautySettingView.updateTabIndex(position);

        boolean showTips = SharedPreferenceUtils.getBeautyFineTuningTips(getContext().getApplicationContext());
        if (showTips) {
            beautySettingView.showTips();
            SharedPreferenceUtils.setBeautyFineTuningTips(getContext().getApplicationContext(), false);
        }
    }

    @Override
    public void onNormalSelected(int postion, BeautyLevel beautyLevel) {
        if (onItemSeletedListener != null) {
            onItemSeletedListener.onNormalSelected(postion, beautyLevel);
        }
    }

    @Override
    public void onAdvancedSelected(int postion, BeautyLevel beautyLevel) {
        if (onItemSeletedListener != null) {
            onItemSeletedListener.onAdvancedSelected(postion, beautyLevel);
        }
    }

    /**
     * 设置美颜参数
     *
     * @param beautyParams
     */
    public void setBeautyParams(BeautyParams beautyParams) {
        this.beautyParams = beautyParams;
    }

    /**
     * 美颜模式改变
     *
     * @param group     RadioGroup
     * @param checkedId 选中的id
     */
    @Override
    public void onModeChange(RadioGroup group, int checkedId) {
        if (onBeautyModeChangeListener != null) {
            onBeautyModeChangeListener.onModeChange(group, checkedId);
        }

        map.put("advance", SharedPreferenceUtils.getBeautyFaceLevel(getContext()));
        map.put("normal", SharedPreferenceUtils.getBeautyNormalFaceLevel(getContext()));
        if (checkedId == R.id.rb_level_advanced) {
            beautySettingView.setBeautyMode(BeautyMode.Advanced, true);
        } else {
            beautySettingView.setBeautyMode(BeautyMode.Normal, true);
        }
        beautySettingView.setDefaultSelect(map);
    }

    /**
     * 设置美颜模式改变listener
     *
     * @param listener OnBeautyModeChangeListener
     */
    public void setOnBeautyModeChangeListener(OnBeautyModeChangeListener listener) {
        this.onBeautyModeChangeListener = listener;
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
        beautySettingView.setDefaultSelect(map);
    }
}
