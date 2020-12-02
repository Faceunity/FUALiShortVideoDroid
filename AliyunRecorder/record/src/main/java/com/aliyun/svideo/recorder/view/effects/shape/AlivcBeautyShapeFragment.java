package com.aliyun.svideo.recorder.view.effects.shape;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.svideo.base.widget.beauty.BeautyParams;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautyDetailClickListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautyShapeItemSeletedListener;
import com.aliyun.svideo.base.widget.beauty.sharp.AlivcBeautyShapeSettingView;
import com.aliyun.svideo.record.R;
import com.aliyun.svideo.recorder.util.SharedPreferenceUtils;
import com.aliyun.svideo.recorder.view.dialog.IPageTab;

import java.util.HashMap;
import java.util.Map;

/**
 * 美型对话框页面
 * @author xlx
 */
public class AlivcBeautyShapeFragment extends Fragment implements IPageTab {

    private AlivcBeautyShapeSettingView mBeautySharpView;

    /**
     * 美型微调按钮点击listener
     */
    private OnBeautyDetailClickListener onBeautyDetailClickListener;
    /**
     * item选中listener 0~5
     */
    private OnBeautyShapeItemSeletedListener onBeautyShapeItemSeletedListener;
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

        mBeautySharpView = new AlivcBeautyShapeSettingView(getContext(), SharedPreferenceUtils.getBeautyMode(getContext()));
        mBeautySharpView.setOnBeautySharpItemSelecedtListener(new OnBeautyShapeItemSeletedListener() {
            @Override
            public void onItemSelected(int position) {
                if (onBeautyShapeItemSeletedListener != null) {
                    onBeautyShapeItemSeletedListener.onItemSelected(position);
                }
            }
        });
        mBeautySharpView.setOnBeautyDetailClickListener(new OnBeautyDetailClickListener() {
            @Override
            public void onDetailClick() {
                if (onBeautyDetailClickListener != null) {
                    onBeautyDetailClickListener.onDetailClick();
                }
            }
        });
        return mBeautySharpView;
    }


    /**
     * 设置美型item选中listener
     * @param listener OnBeautySkinItemSeletedListener
     */
    public void setOnBeautySharpItemSelectedlistener(OnBeautyShapeItemSeletedListener listener) {
        this.onBeautyShapeItemSeletedListener = listener;
    }
    /**
     * 设置微调按钮点击listener
     * @param listener OnBeautyDetailClickListener
     */
    public void setOnBeautyDetailClickListener(OnBeautyDetailClickListener listener) {
        this.onBeautyDetailClickListener = listener;
    }




    Map<String, Integer> map = new HashMap<>();
    @Override
    public void onStart() {
        super.onStart();
        map.put("shape", SharedPreferenceUtils.getBeautyShapeLevel(getContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        mBeautySharpView.setDefaultSelect(map);
    }
}
