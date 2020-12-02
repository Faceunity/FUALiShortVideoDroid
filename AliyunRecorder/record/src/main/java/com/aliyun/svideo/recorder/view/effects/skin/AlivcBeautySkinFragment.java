package com.aliyun.svideo.recorder.view.effects.skin;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.svideo.record.R;
import com.aliyun.svideo.recorder.util.SharedPreferenceUtils;
import com.aliyun.svideo.recorder.view.dialog.IPageTab;
import com.aliyun.svideo.base.widget.beauty.BeautyParams;
import com.aliyun.svideo.base.widget.beauty.enums.BeautyMode;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautyDetailClickListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautySkinItemSeletedListener;
import com.aliyun.svideo.base.widget.beauty.skin.AlivcBeautySkinSettingView;

import java.util.HashMap;
import java.util.Map;

/**
 * 美肌对话框页面
 * @author xlx
 */
public class AlivcBeautySkinFragment extends Fragment implements IPageTab {

    private AlivcBeautySkinSettingView mBeautySkinView;

    /**
     * 美肌微调按钮点击listener
     */
    private OnBeautyDetailClickListener onBeautyDetailClickListener;
    /**
     * item选中listener 0~5
     */
    private OnBeautySkinItemSeletedListener onBeautySkinItemSeletedListener;
    /**
     * 美肌参数
     */
    private BeautyParams beautyParams;

    private String mTabTitle;

    @Override
    public void setTabTitle(String tabTitle) {
        this.mTabTitle = tabTitle;
    }

    @Override
    public String getTabTitle() {
        return mTabTitle;
    }
    @Override
    public int getTabIcon() {
        return R.mipmap.alivc_svideo_icon_tab_beauty_skin;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mBeautySkinView = new AlivcBeautySkinSettingView(getContext(), false, SharedPreferenceUtils.getBeautyMode(getContext()));
        mBeautySkinView.setOnBeautySkinItemSelecedtListener(new OnBeautySkinItemSeletedListener() {
            @Override
            public void onItemSelected(int position) {
                if (onBeautySkinItemSeletedListener != null) {
                    onBeautySkinItemSeletedListener.onItemSelected(position);
                }
            }
        });
        mBeautySkinView.setParams(beautyParams);
        mBeautySkinView.setBeautyMode(BeautyMode.SKIN, false);
        mBeautySkinView.setOnBeautyDetailClickListener(new OnBeautyDetailClickListener() {
            @Override
            public void onDetailClick() {
                if (onBeautyDetailClickListener != null) {
                    onBeautyDetailClickListener.onDetailClick();
                }
            }
        });
        return mBeautySkinView;
    }

    /**
     * 设置微调按钮点击listener
     * @param listener OnBeautyDetailClickListener
     */
    public void setOnBeautyDetailClickListener(OnBeautyDetailClickListener listener) {
        this.onBeautyDetailClickListener = listener;
    }

    /**
     * 设置美肌item选中listener
     * @param listener OnBeautySkinItemSeletedListener
     */
    public void setOnBeautySkinItemSelectedlistener(OnBeautySkinItemSeletedListener listener) {
        this.onBeautySkinItemSeletedListener = listener;
    }


    /**
     * 当前tab索引
     * @param position 当前tab索引
     */
    public void updatePageIndex(int position) {
        mBeautySkinView.updateTabIndex(position);
    }

    /**
     * 美肌参数
     * @param beautyParams BeautyParams
     */
    public void setBeautyParams(BeautyParams beautyParams) {
        this.beautyParams = beautyParams;
    }


    Map<String, Integer> map = new HashMap<>();
    @Override
    public void onStart() {
        super.onStart();
        map.put("skin", SharedPreferenceUtils.getBeautySkinLevel(getContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        mBeautySkinView.setDefaultSelect(map);
    }
}
