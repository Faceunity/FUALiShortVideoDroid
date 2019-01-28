package com.aliyun.demo.recorder.view.effects.skin;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.common.global.AliyunTag;
import com.aliyun.demo.R;
import com.aliyun.demo.recorder.view.dialog.IPageTab;
import com.aliyun.svideo.base.widget.beauty.AlivcBeautySettingView;
import com.aliyun.svideo.base.widget.beauty.BeautyParams;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautyDetailClickListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautySkinItemSeletedListener;

/**
 * 美肌对话框页面
 * @author xlx
 */
public class AlivcBeautySkinFragment extends Fragment implements IPageTab,OnBeautySkinItemSeletedListener {

    private AlivcBeautySettingView beautySettingView;

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

    @Override
    public String getTabTitle() {
        return "美肌";
    }
    @Override
    public int getTabIcon() {
        return R.mipmap.alivc_svideo_icon_tab_beauty_skin;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        beautySettingView = new AlivcBeautySettingView(getContext(), false);

        beautySettingView.setOnBeautySkinItemSelecedtListener(this);
        beautySettingView.setParams(beautyParams);
        beautySettingView.setOnBeautyDetailClickListener(new OnBeautyDetailClickListener() {
            @Override
            public void onDetailClick() {
                if (onBeautyDetailClickListener != null) {
                    onBeautyDetailClickListener.onDetailClick();
                }
            }
        });
        return beautySettingView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(AliyunTag.TAG,"yds-------AlivcBeautySkin：onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(AliyunTag.TAG,"yds-------AlivcBeautySkin：onDetach");
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
     * 美肌item选中监听
     * @param position 选中下标
     */
    @Override
    public void onItemSelected(int position) {
        if (onBeautySkinItemSeletedListener != null) {
            onBeautySkinItemSeletedListener.onItemSelected(position);
        }
    }

    /**
     * 当前tab索引
     * @param position 当前tab索引
     */
    public void updatePageIndex(int position) {
        beautySettingView.updateTabIndex(position);
    }

    /**
     * 美肌参数
     * @param beautyParams BeautyParams
     */
    public void setBeautyParams(BeautyParams beautyParams) {
        this.beautyParams = beautyParams;
    }


}
