package com.aliyun.race.sample.view;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import com.aliyun.race.sample.R;
import com.aliyun.race.sample.bean.BeautyLevel;
import com.aliyun.race.sample.bean.BeautyMode;
import com.aliyun.race.sample.bean.BeautyParams;
import com.aliyun.race.sample.view.face.AlivcBeautyFaceFragment;
import com.aliyun.race.sample.view.listener.OnBeautyDetailClickListener;
import com.aliyun.race.sample.view.listener.OnBeautyFaceItemSeletedListener;
import com.aliyun.race.sample.view.listener.OnBeautyShapeItemSeletedListener;
import com.aliyun.race.sample.view.listener.OnBeautyTableItemSeletedListener;
import com.aliyun.race.sample.view.shape.AlivcBeautyShapeFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 美颜模块整体dialog, 包含滤镜, 美颜, 美肌
 * @author xlx
 */
public class BeautyEffectChooser extends BasePageChooser {

    /**
     * 美颜参数
     */
    private BeautyParams mBeautyParams;

    /**
     * 美颜fragment
     */
    private AlivcBeautyFaceFragment mBeautyFaceFragment;
    /**
     * 美型fragment
     */
    private AlivcBeautyShapeFragment mBeautySharpFragment;

    /**
     * 美颜微调点击listener
     */
    private OnBeautyDetailClickListener mOnBeautyFaceDetailClickListener;
    /**
     * 美型微调点击listener
     */
    private OnBeautyDetailClickListener mOnBeautyShapeDetailClickListener;

    /**
     * 美颜item选中listener
     */
    private OnBeautyFaceItemSeletedListener mOnItemSeletedListener;
    /**
     * tab选中listener
     */
    private OnBeautyTableItemSeletedListener mOnTableSeletedListener;
    /**
     * 美型item选中listener
     */
    private OnBeautyShapeItemSeletedListener mOnBeautyShapeItemSeletedListener;

    /**
     * 当前viewpager的选中下标
     */
    public int mCurrentTabPosition;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //适配有底部导航栏的手机，在full的style下会盖住部分视图的bug
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.QUDemoFullStyle);
    }

    @Override
    public List<Fragment> createPagerFragmentList() {
        List<Fragment> fragments = new ArrayList<>();

        mBeautyFaceFragment = new AlivcBeautyFaceFragment();
        mBeautyFaceFragment.setTabTitle(getResources().getString(R.string.alivc_base_beauty));
        fragments.add(mBeautyFaceFragment);

//        增加race的美型选择功能
        mBeautySharpFragment = new AlivcBeautyShapeFragment();
        fragments.add(mBeautySharpFragment);
        // rece美型
        mBeautySharpFragment.setTabTitle(getResources().getString(R.string.alivc_base_beauty_shape));
        initBeautySharp();
        initBeautyFace();


        // dialog的tab切换监听
        setOnUpdatePageSelectedListener(new OnUpdatePageSelectedListener() {
            @Override
            public void onPageSelected(int position) {
                mCurrentTabPosition = position;
                mBeautyFaceFragment.updatePageIndex(position);
                if (mOnTableSeletedListener != null){
                    switch (mCurrentTabPosition){
                        case 0:
                            mOnTableSeletedListener.onNormalSelected(BeautyMode.Advanced);
                            break;
                        case 1:
                            mOnTableSeletedListener.onNormalSelected(BeautyMode.SHAPE);
                            break;
                        default:
                            mOnTableSeletedListener.onNormalSelected(BeautyMode.Advanced);
                            break;
                    }

                }
            }
        });
        return fragments;
    }

    private void initBeautySharp() {

        mBeautySharpFragment.setOnBeautySharpItemSelectedlistener(new OnBeautyShapeItemSeletedListener() {
            @Override
            public void onItemSelected(int postion) {
                if (mOnBeautyShapeItemSeletedListener != null) {
                    mOnBeautyShapeItemSeletedListener.onItemSelected(postion);
                }
            }
        });
        // 详情
        mBeautySharpFragment.setOnBeautyDetailClickListener(new OnBeautyDetailClickListener() {
            @Override
            public void onDetailClick() {
                if (mOnBeautyShapeDetailClickListener != null) {
                    mOnBeautyShapeDetailClickListener.onDetailClick();
                }
            }
        });
    }

    private void initBeautyFace() {
        mBeautyFaceFragment.setBeautyParams(mBeautyParams);

        // 档位选择
        mBeautyFaceFragment.setOnBeautyFaceItemSeletedListener(new OnBeautyFaceItemSeletedListener() {
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
        });


        // 高级详情
        mBeautyFaceFragment.setOnBeautyDetailClickListener(new OnBeautyDetailClickListener() {
            @Override
            public void onDetailClick() {
                if (mOnBeautyFaceDetailClickListener != null) {
                    mOnBeautyFaceDetailClickListener.onDetailClick();
                }
            }
        });
    }



    /**
     * 设置美颜美肌参数
     * @param beautyParams 美颜美肌参数
     */
    public void setBeautyParams(BeautyParams beautyParams) {
        this.mBeautyParams = beautyParams;
    }

    /**
     * 美颜微调按钮点击listener
     * @param listener OnBeautyDetailClickListener
     */
    public void setOnBeautyFaceDetailClickListener(
        OnBeautyDetailClickListener listener) {
        this.mOnBeautyFaceDetailClickListener = listener;
    }


    /**
     * 美型微调按钮点击listener
     * @param listener OnBeautyDetailClickListener
     */
    public void setOnBeautyShapeDetailClickListener(
        OnBeautyDetailClickListener listener) {
        this.mOnBeautyShapeDetailClickListener = listener;
    }

    /**
     * 设置美颜item点击listener
     * @param listener OnBeautyFaceItemSeletedListener
     */
    public void setOnBeautyFaceItemSeletedListener(OnBeautyFaceItemSeletedListener listener) {
        this.mOnItemSeletedListener = listener;
    }
    /**
     * 设置美型item点击listener
     * @param listener OnBeautyFaceItemSeletedListener
     */
    public void setOnBeautyShapeItemSeletedListener(OnBeautyShapeItemSeletedListener listener) {
        this.mOnBeautyShapeItemSeletedListener = listener;
    }




    public int getCurrentTabIndex() {
        return mCurrentTabPosition;
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

}
