package com.aliyun.svideo.base.widget.beauty.skin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.aliyun.svideo.base.CopyrightWebActivity;
import com.aliyun.svideo.base.R;
import com.aliyun.svideo.base.widget.beauty.BeautyDetailSettingView;
import com.aliyun.svideo.base.widget.beauty.BeautyParams;
import com.aliyun.svideo.base.widget.beauty.enums.BeautyMode;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautyDetailClickListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautySkinItemSeletedListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnViewClickListener;

import java.util.Map;

public class AlivcBeautySkinSettingView extends FrameLayout {
    private static final int BEAUTY_FACE = 1;
    private static final int BEAUTY_SKIN = 2;

    private Context mContext;
    private BeautyDefaultSkinSettingView mBeautySkinSettingView;
    private BeautyDetailSettingView mDetailSettingView;
    private BeautyParams mParams;
    private TextView tvCopyright;
    private RadioButton rbNormalLevel;
    private RadioButton rbHeighLevel;
    private BeautyMode mBeautyMode = BeautyMode.Advanced;


    /**
     * 美肌item选中
     */
    private OnBeautySkinItemSeletedListener onBeautySkinItemSelecedtListener;

    /**
     * 微调按钮点击
     */
    private OnBeautyDetailClickListener onBeautyDetailClickListener;



    public AlivcBeautySkinSettingView(Context context, boolean isShowTab, BeautyMode beautyMode) {
        super(context, null);
        this.mBeautyMode = beautyMode;
        mContext = context;
        initView();
    }

    public AlivcBeautySkinSettingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
        mContext = context;
        initView();
    }

    public AlivcBeautySkinSettingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.alivc_beauty_skin_layout, this);
        mBeautySkinSettingView = findViewById(R.id.default_skin_setting);
        tvCopyright = findViewById(R.id.tv_copyright);
        tvCopyright.setMovementMethod(LinkMovementMethod.getInstance());
//        tvCopyright.setText(getClickableSpan());
        rbNormalLevel = findViewById(R.id.rb_level_normal);
        rbHeighLevel = findViewById(R.id.rb_level_advanced);

        mBeautySkinSettingView.setItemSelectedListener(new OnBeautySkinItemSeletedListener() {
            @Override
            public void onItemSelected(int postion) {
                if (onBeautySkinItemSelecedtListener != null) {
                    onBeautySkinItemSelecedtListener.onItemSelected(postion);
                }
            }
        });
        // 默认设置页面点击详情
        mBeautySkinSettingView.setSettingClickListener(new OnViewClickListener() {
            @Override
            public void onClick() {
                if (onBeautyDetailClickListener != null) {
                    onBeautyDetailClickListener.onDetailClick();
                }
            }
        });
        initCopyRight();
    }

    private void initCopyRight() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("svideo",
                                              Activity.MODE_PRIVATE);
        boolean isRaceMode = sharedPreferences.getBoolean("is_race_mode", false);
        if (isRaceMode) {
            String copyright = getContext().getResources().getString(R.string.alivc_base_beauty_race_copyright);
            tvCopyright.setText(copyright);
        } else {
            tvCopyright.setText(getClickableSpan());
        }
    }


    public void setParams(BeautyParams params) {
        mParams = params;
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
     * @param tabIndex
     */
    public void updateTabIndex(int tabIndex) {
        if (mDetailSettingView != null) {
            mDetailSettingView.updateDetailLayout(tabIndex);
        }
    }

    public void setDefaultSelect(Map<String, Integer> map) {
        if (mBeautySkinSettingView != null) {
            mBeautySkinSettingView.setDefaultSelect(map);
        }
    }


    public void setOnBeautyDetailClickListener(
        OnBeautyDetailClickListener onBeautyDetailClickListener) {
        this.onBeautyDetailClickListener = onBeautyDetailClickListener;
    }
    /**
     * 获取跳转到版权页面的字符串
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
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE );
        spannableString.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(R.color.alivc_svideo_bg_balloon_tip_cyan)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    public void setBeautyMode(BeautyMode beautyMode, boolean isBeautyFace) {
        mBeautySkinSettingView.setBeautyMode(beautyMode, isBeautyFace);
    }
}
