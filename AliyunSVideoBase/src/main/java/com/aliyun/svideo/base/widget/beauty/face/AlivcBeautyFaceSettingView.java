package com.aliyun.svideo.base.widget.beauty.face;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.aliyun.svideo.base.R;
import com.aliyun.svideo.base.CopyrightWebActivity;
import com.aliyun.svideo.base.widget.beauty.BeautyConstants;
import com.aliyun.svideo.base.widget.beauty.BeautyDetailSettingView;
import com.aliyun.svideo.base.widget.beauty.BeautyParams;
import com.aliyun.svideo.base.widget.beauty.BeautyRaceConstants;
import com.aliyun.svideo.base.widget.beauty.enums.BeautyLevel;
import com.aliyun.svideo.base.widget.beauty.enums.BeautyMode;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautyDetailClickListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautyFaceItemSeletedListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautyModeChangeListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautySkinItemSeletedListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnViewClickListener;

import java.util.Map;

public class AlivcBeautyFaceSettingView extends FrameLayout {
    private static final int BEAUTY_FACE = 1;
    private static final int BEAUTY_SKIN = 2;

    private Context mContext;
    private BeautyDefaultFaceSettingView mDefaultSettingView;
    private BeautyDetailSettingView mDetailSettingView;
    private BeautyParams mParams;
    private RadioGroup rgBeautyModeCheck;
    private RadioButton rbNormalLevel;
    private RadioButton rbHeighLevel;
    private BeautyMode mBeautyMode = BeautyMode.Advanced;
    private boolean isRaceMode = false;

    /**
     * 底部tab的下标控制
     */
    private int bottomTabIndex = 0;
    /**
     * 高级
     */
    private static final int BOTTOM_TAB_INDEX_HEIGH = 0;
    /**
     * 普通
     */
    private static final int BOTTOM_TAB_INDEX_NORMAL = 1;

    /**
     * 美颜item选中
     */
    private OnBeautyFaceItemSeletedListener onBeautyFaceItemSelecedtListener;
    /**
     * 美肌item选中
     */
    private OnBeautySkinItemSeletedListener onBeautySkinItemSelecedtListener;

    /**
     * 微调按钮点击
     */
    private OnBeautyDetailClickListener onBeautyDetailClickListener;

    /**
     * 美颜模式改变监听
     */
    private OnBeautyModeChangeListener onBeautyModeChangeListener;
    private Map<String, Integer> defaultMap;

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
        LayoutInflater.from(mContext).inflate(R.layout.alivc_beauty_face_layout, this);
        mDefaultSettingView = findViewById(R.id.default_face_setting);
        rgBeautyModeCheck = findViewById(R.id.rg_beauty_mode);
        rbNormalLevel = findViewById(R.id.rb_level_normal);
        rbHeighLevel = findViewById(R.id.rb_level_advanced);
        // 默认选中高级美颜
        bottomTabChange(BOTTOM_TAB_INDEX_HEIGH);
        mDefaultSettingView.setBeautyMode(BeautyMode.Advanced, true);
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("svideo",
                                              Activity.MODE_PRIVATE);
        isRaceMode = sharedPreferences.getBoolean("is_race_mode", false);

        mDefaultSettingView.setItemSelectedListener(new OnBeautyFaceItemSeletedListener() {
            @Override
            public void onNormalSelected(int postion, BeautyLevel beautyLevel) {
                mDefaultSettingView.hideDetailBtn();
                if (onBeautySkinItemSelecedtListener != null) {
                    onBeautySkinItemSelecedtListener.onItemSelected(postion);
                }
                if (onBeautyFaceItemSelecedtListener != null) {
                    onBeautyFaceItemSelecedtListener.onNormalSelected(postion, beautyLevel);
                }

            }

            @Override
            public void onAdvancedSelected(int postion, BeautyLevel beautyLevel) {

                if (isRaceMode) {
                    if (postion < BeautyRaceConstants.BEAUTY_MAP.size()) {
                        if (mParams != null) {
                            mParams.beautyBuffing = BeautyRaceConstants.BEAUTY_MAP.get(postion).beautyBuffing;
                            mParams.beautyRuddy = BeautyRaceConstants.BEAUTY_MAP.get(postion).beautyRuddy;
                            mParams.beautyBigEye = BeautyRaceConstants.BEAUTY_MAP.get(postion).beautyBigEye;
                            mParams.beautySlimFace = BeautyRaceConstants.BEAUTY_MAP.get(postion).beautySlimFace;
                            mParams.beautyWhite = BeautyRaceConstants.BEAUTY_MAP.get(postion).beautyWhite;
                        }
                    }
                } else {
                    if (postion < BeautyConstants.BEAUTY_MAP.size()) {
                        if (mParams != null) {
                            mParams.beautyBuffing = BeautyConstants.BEAUTY_MAP.get(postion).beautyBuffing;
                            mParams.beautyRuddy = BeautyConstants.BEAUTY_MAP.get(postion).beautyRuddy;
                            mParams.beautyBigEye = BeautyConstants.BEAUTY_MAP.get(postion).beautyBigEye;
                            mParams.beautySlimFace = BeautyConstants.BEAUTY_MAP.get(postion).beautySlimFace;
                            mParams.beautyWhite = BeautyConstants.BEAUTY_MAP.get(postion).beautyWhite;
                        }
                    }
                }
                if (onBeautySkinItemSelecedtListener != null) {
                    onBeautySkinItemSelecedtListener.onItemSelected(postion);
                }
                if (onBeautyFaceItemSelecedtListener != null) {
                    onBeautyFaceItemSelecedtListener.onAdvancedSelected(postion, beautyLevel);
                }
            }
        });

        // 默认设置页面点击详情
        mDefaultSettingView.setSettingClickListener(new OnViewClickListener() {
            @Override
            public void onClick() {
                if (onBeautyDetailClickListener != null) {
                    onBeautyDetailClickListener.onDetailClick();
                }
            }
        });

        rgBeautyModeCheck.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (onBeautyModeChangeListener != null) {
                    onBeautyModeChangeListener.onModeChange(group, checkedId);
                }

                if (checkedId == R.id.rb_level_advanced) {
                    mDefaultSettingView.showDetailBtn();
                    bottomTabIndex = BOTTOM_TAB_INDEX_HEIGH;
                    mDefaultSettingView.setBeautyMode(BeautyMode.Advanced, true);
                } else if (checkedId == R.id.rb_level_normal) {
                    mDefaultSettingView.hideDetailBtn();
                    mDefaultSettingView.setBeautyMode(BeautyMode.Normal, true);
                    bottomTabIndex = BOTTOM_TAB_INDEX_NORMAL;
                }

                bottomTabChange(bottomTabIndex);
            }
        });

    }

    public void setParams(BeautyParams params) {
        mParams = params;
    }

    /**
     * 底部tab的三角形drawable显示和隐藏
     *
     * @param tabIndex
     */
    public void bottomTabChange(int tabIndex) {
        Drawable arrowdrawable = ContextCompat.getDrawable(mContext, R.drawable.alivc_beauty_level_tab_checked);
        arrowdrawable.setBounds(0, 0, arrowdrawable.getMinimumWidth(), arrowdrawable.getMinimumHeight());
        // 非选中状态不要设置为null, 否则整体高度会变化
        Drawable transparent = ContextCompat.getDrawable(mContext, android.R.color.transparent);
        transparent.setBounds(0, 0, arrowdrawable.getMinimumWidth(), arrowdrawable.getMinimumHeight());

        if (tabIndex == BOTTOM_TAB_INDEX_NORMAL) {
            rbHeighLevel.setCompoundDrawables(null, null, null, transparent);
            rbNormalLevel.setCompoundDrawables(null, null, null, arrowdrawable);
        } else if (tabIndex == BOTTOM_TAB_INDEX_HEIGH) {
            rbNormalLevel.setCompoundDrawables(null, null, null, transparent);
            rbHeighLevel.setCompoundDrawables(null, null, null, arrowdrawable);
        }
        if (mDefaultSettingView != null) {
            mDefaultSettingView.setDefaultSelect(defaultMap);
        }
    }

    boolean isShowTips = false;

    public void showTips() {
        if (!isShowTips) {
            mDefaultSettingView.showDetailTips();
        }
    }

    public void setOnBeautyLevelChangeListener(OnBeautyModeChangeListener listener) {
        this.onBeautyModeChangeListener = listener;
    }

    /**
     * 美颜item选中监听
     *
     * @param listener
     */
    public void setOnBeautyItemSelecedtListener(OnBeautyFaceItemSeletedListener listener) {
        this.onBeautyFaceItemSelecedtListener = listener;
    }

    /**
     * 美肌item选中监听
     *
     * @param listener
     */
    public void setOnBeautySkinItemSelecedtListener(OnBeautySkinItemSeletedListener listener) {
        this.onBeautySkinItemSelecedtListener = listener;
    }

    /**
     * 设置当前dialog中tab的下标
     *
     * @param tabIndex
     */
    public void updateTabIndex(int tabIndex) {
        if (mDetailSettingView != null) {
            mDetailSettingView.updateDetailLayout(tabIndex);
        }
    }

    public void setDefaultSelect(Map<String, Integer> map) {
        this.defaultMap = map;
        if (mDefaultSettingView != null) {
            mDefaultSettingView.setDefaultSelect(map);
        }
        bottomTabChange(mBeautyMode == BeautyMode.Normal ? BOTTOM_TAB_INDEX_NORMAL : BOTTOM_TAB_INDEX_HEIGH);
        //rgBeautyModeCheck.check(mBeautyMode == BeautyMode.Normal ? R.id.rb_level_normal : R.id.rb_level_advanced);
        setBeautyMode(mBeautyMode, true);
    }

    public void setRadioChecked(int checkId) {

        rgBeautyModeCheck.check(checkId);
    }

    public void setOnBeautyDetailClickListener(
        OnBeautyDetailClickListener onBeautyDetailClickListener) {
        this.onBeautyDetailClickListener = onBeautyDetailClickListener;
    }

    /**
     * 获取跳转到版权页面的字符串
     *
     * @return
     */
    private SpannableString getClickableSpan() {
        String copyright = getContext().getResources().getString(R.string.alivc_base_beauty_copyright);
        String copyrightLink = getContext().getResources().getString(R.string.alivc_base_beauty_copyright_link);
        final int start = copyright.length();
        int end = copyright.length() + copyrightLink.length();
        SpannableString spannableString = new SpannableString(copyright + copyrightLink);
        spannableString.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(getContext(), CopyrightWebActivity.class);
                getContext().startActivity(intent);
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(
            new ForegroundColorSpan(getContext().getResources().getColor(R.color.alivc_svideo_bg_balloon_tip_cyan)), start,
            end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    public void setBeautyMode(BeautyMode beautyMode, boolean isBeautyFace) {
        mDefaultSettingView.setBeautyMode(beautyMode, isBeautyFace);
    }
}
