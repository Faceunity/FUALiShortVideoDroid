package com.aliyun.race.sample.view.face;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.aliyun.race.sample.R;
import com.aliyun.race.sample.bean.BeautyLevel;
import com.aliyun.race.sample.bean.BeautyMode;
import com.aliyun.race.sample.bean.BeautyParams;
import com.aliyun.race.sample.utils.constants.BeautyRaceConstants;
import com.aliyun.race.sample.view.listener.OnBeautyDetailClickListener;
import com.aliyun.race.sample.view.listener.OnBeautyFaceItemSeletedListener;
import com.aliyun.race.sample.view.listener.OnBeautySkinItemSeletedListener;
import com.aliyun.race.sample.view.listener.OnViewClickListener;

import java.util.Map;

public class AlivcBeautyFaceSettingView extends FrameLayout {
    private static final int BEAUTY_FACE = 1;
    private static final int BEAUTY_SKIN = 2;

    private Context mContext;
    private BeautyDefaultFaceSettingView mDefaultSettingView;
    private BeautyParams mParams;
    private BeautyMode mBeautyMode = BeautyMode.Advanced;


    /**
     * 美颜item选中
     */
    private OnBeautyFaceItemSeletedListener mOnBeautyFaceItemSelecedtListener;
    /**
     * 美肌item选中
     */
    private OnBeautySkinItemSeletedListener mOnBeautySkinItemSelecedtListener;

    /**
     * 微调按钮点击
     */
    private OnBeautyDetailClickListener mOnBeautyDetailClickListener;

    private Map<String, Integer> mDefaultMap;

    public AlivcBeautyFaceSettingView(Context context, boolean isShowTab, BeautyMode beautyMode) {
        super(context, null);
        this.mBeautyMode = beautyMode;
        mContext = context;
        initView();
    }

    public AlivcBeautyFaceSettingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
        mContext = context;
        initView();
    }

    public AlivcBeautyFaceSettingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.alivc_race_beauty_face_layout, this);
        mDefaultSettingView = findViewById(R.id.default_face_setting);
        // 默认选中高级美颜
        mDefaultSettingView.setBeautyMode(BeautyMode.Advanced, true);
        mDefaultSettingView.setItemSelectedListener(new OnBeautyFaceItemSeletedListener() {
            @Override
            public void onNormalSelected(int postion, BeautyLevel beautyLevel) {
                if (mOnBeautySkinItemSelecedtListener != null) {
                    mOnBeautySkinItemSelecedtListener.onItemSelected(postion);
                }
                if (mOnBeautyFaceItemSelecedtListener != null) {
                    mOnBeautyFaceItemSelecedtListener.onNormalSelected(postion, beautyLevel);
                }

            }

            @Override
            public void onAdvancedSelected(int postion, BeautyLevel beautyLevel) {

                if (postion < BeautyRaceConstants.BEAUTY_MAP.size()) {
                    if (mParams != null) {
                        mParams.mBeautyBuffing = BeautyRaceConstants.BEAUTY_MAP.get(postion).mBeautyBuffing;
                        mParams.mBeautyRuddy = BeautyRaceConstants.BEAUTY_MAP.get(postion).mBeautyRuddy;
                        mParams.mBeautyBigEye = BeautyRaceConstants.BEAUTY_MAP.get(postion).mBeautyBigEye;
                        mParams.mBeautySlimFace = BeautyRaceConstants.BEAUTY_MAP.get(postion).mBeautySlimFace;
                        mParams.mBeautyWhite = BeautyRaceConstants.BEAUTY_MAP.get(postion).mBeautyWhite;
                    }
                }
                if (mOnBeautySkinItemSelecedtListener != null) {
                    mOnBeautySkinItemSelecedtListener.onItemSelected(postion);
                }
                if (mOnBeautyFaceItemSelecedtListener != null) {
                    mOnBeautyFaceItemSelecedtListener.onAdvancedSelected(postion, beautyLevel);
                }
            }
        });

        // 默认设置页面点击详情
        mDefaultSettingView.setSettingClickListener(new OnViewClickListener() {
            @Override
            public void onClick() {
                if (mOnBeautyDetailClickListener != null) {
                    mOnBeautyDetailClickListener.onDetailClick();
                }
            }
        });


    }

    public void setParams(BeautyParams params) {
        mParams = params;
    }


    boolean isShowTips = false;

    public void showTips() {
        if (!isShowTips) {
            mDefaultSettingView.showDetailTips();
        }
    }

    /**
     * 美颜item选中监听
     *
     * @param listener
     */
    public void setOnBeautyItemSelecedtListener(OnBeautyFaceItemSeletedListener listener) {
        this.mOnBeautyFaceItemSelecedtListener = listener;
    }


    /**
     * 设置当前dialog中tab的下标
     *
     * @param tabIndex
     */
    public void updateTabIndex(int tabIndex) {
    }

    public void setDefaultSelect(Map<String, Integer> map) {
        this.mDefaultMap = map;
        if (mDefaultSettingView != null) {
            mDefaultSettingView.setDefaultSelect(map);
        }
        //rgBeautyModeCheck.check(mBeautyMode == BeautyMode.Normal ? R.id.rb_level_normal : R.id.rb_level_advanced);
        setBeautyMode(mBeautyMode, true);
    }


    public void setOnBeautyDetailClickListener(
        OnBeautyDetailClickListener onBeautyDetailClickListener) {
        this.mOnBeautyDetailClickListener = onBeautyDetailClickListener;
    }


    public void setBeautyMode(BeautyMode beautyMode, boolean isBeautyFace) {
        mDefaultSettingView.setBeautyMode(beautyMode, isBeautyFace);
    }
}
