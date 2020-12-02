package com.aliyun.svideo.base.widget.beauty.face;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.aliyun.svideo.base.CopyrightWebActivity;
import com.aliyun.svideo.base.R;
import com.aliyun.svideo.base.widget.AlivcPopupView;
import com.aliyun.svideo.base.widget.beauty.enums.BeautyLevel;
import com.aliyun.svideo.base.widget.beauty.enums.BeautyMode;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautyFaceItemSeletedListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnViewClickListener;

import java.util.Map;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class BeautyDefaultFaceSettingView extends LinearLayout {
    private Context mContext;
    private RadioGroup mRgNormalGroup;
    private RadioGroup mRgAdvancedGroup;
    private ImageView mBtBeautyDetail;
    private BeautyLevel beautyLevel = BeautyLevel.BEAUTY_LEVEL_THREE;
    private int normalPosition = 3;
    private int advancedPosition = 3;
    private BeautyMode beautyMode;
    private AlivcPopupView alivcPopupView;
    private Map<String, Integer> beautyMap;
    private TextView mTvCopyright;

    public BeautyDefaultFaceSettingView(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public BeautyDefaultFaceSettingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    public BeautyDefaultFaceSettingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    public void setDefaultSelect(Map<String, Integer> map) {
        if (map == null) {
            return;
        }
        this.beautyMap = map;
        advancedPosition = map.get("advance") == null ? 3 : map.get("advance");
        normalPosition = map.get("normal") == null ? 3 : map.get("normal");

        if (beautyMode == BeautyMode.Normal) {
            normalCheck(normalPosition);
        } else if (beautyMode == BeautyMode.Advanced) {
            advancedCheck(advancedPosition);
        }
    }

    private void advancedCheck(int position) {
        int advanceId;
        switch (position) {
        case 0:
            advanceId = R.id.beauty_advanced_0;
            break;
        case 1:
            advanceId = R.id.beauty_advanced_1;
            break;

        case 2:
            advanceId = R.id.beauty_advanced_2;
            break;

        case 3:
            advanceId = R.id.beauty_advanced_3;
            break;

        case 4:
            advanceId = R.id.beauty_advanced_4;
            break;

        case 5:
            advanceId = R.id.beauty_advanced_5;
            break;

        default:
            advanceId = R.id.beauty_advanced_3;
            break;
        }
        mRgAdvancedGroup.check(advanceId);
    }

    private void normalCheck(int position) {
        int normalId;
        switch (position) {
        case 0:
            normalId = R.id.beauty0;
            break;
        case 1:
            normalId = R.id.beauty1;
            break;

        case 2:
            normalId = R.id.beauty2;
            break;

        case 3:
            normalId = R.id.beauty3;
            break;

        case 4:
            normalId = R.id.beauty4;
            break;

        case 5:
            normalId = R.id.beauty5;
            break;

        default:
            normalId = R.id.beauty3;
            break;
        }
        mRgNormalGroup.check(normalId);
    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.alivc_beauty_default, this);

        mRgNormalGroup = findViewById(R.id.beauty_normal_group);
        mRgAdvancedGroup = findViewById(R.id.beauty_advanced_group);
        mBtBeautyDetail = findViewById(R.id.iv_beauty_detail);

        alivcPopupView  = new AlivcPopupView(mContext);
        TextView textView = new TextView(getContext());
        textView.setLayoutParams(new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));

        textView.setPadding(10, 0, 10, 0);
        textView.setText(getResources().getString(R.string.alivc_base_tips_fine_tuning));
        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.alivc_common_font_white));
        alivcPopupView.setContentView(textView);

        mBtBeautyDetail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSettingClickListener != null) {
                    mSettingClickListener.onClick();
                }
            }
        });
        mRgNormalGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                checkedPosition(checkedId);
                if (mListener != null) {
                    mListener.onNormalSelected(normalPosition, beautyLevel);
                }
            }
        });

        mRgAdvancedGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                checkedPosition(checkedId);
                if (mListener != null) {
                    mListener.onAdvancedSelected(advancedPosition, beautyLevel);
                }
            }
        });
        mTvCopyright = findViewById(R.id.tv_def_copyright);
        initCopyRight(mTvCopyright);
    }

    /**
     * 单位转换: dp -> px
     *
     * @param dp
     * @return
     */
    public static int dp2px(Context context, int dp) {
        return (int) (getDensity(context) * dp + 0.5);
    }

    public static float getDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    private void checkedPosition(int checkedId) {

        if (checkedId == R.id.beauty0 || checkedId == R.id.beauty_advanced_0) {
            normalPosition = 0;
            advancedPosition = 0;
            beautyLevel = BeautyLevel.BEAUTY_LEVEL_ZERO;
        } else if (checkedId == R.id.beauty1 || checkedId == R.id.beauty_advanced_1) {
            normalPosition = 1;
            advancedPosition = 1;
            beautyLevel = BeautyLevel.BEAUTY_LEVEL_ONE;
        } else if (checkedId == R.id.beauty2 || checkedId == R.id.beauty_advanced_2) {
            normalPosition = 2;
            advancedPosition = 2;
            beautyLevel = BeautyLevel.BEAUTY_LEVEL_TWO;
        } else if (checkedId == R.id.beauty3 || checkedId == R.id.beauty_advanced_3) {
            normalPosition = 3;
            advancedPosition = 3;
            beautyLevel = BeautyLevel.BEAUTY_LEVEL_THREE;
        } else if (checkedId == R.id.beauty4 || checkedId == R.id.beauty_advanced_4) {
            normalPosition = 4;
            advancedPosition = 4;
            beautyLevel = BeautyLevel.BEAUTY_LEVEL_FOUR;
        } else if (checkedId == R.id.beauty5 || checkedId == R.id.beauty_advanced_5) {
            normalPosition = 5;
            advancedPosition = 5;
            beautyLevel = BeautyLevel.BEAUTY_LEVEL_FIVE;
        }
    }
    /**
     * 详情按钮点击监听
     */
    private OnViewClickListener mSettingClickListener;

    public void setSettingClickListener(OnViewClickListener listener) {
        mSettingClickListener = listener;
    }

    /**
     * item选择监听
     */
    private OnBeautyFaceItemSeletedListener mListener;

    public void setItemSelectedListener(OnBeautyFaceItemSeletedListener listener) {
        mListener = listener;
        if (beautyMode == BeautyMode.Normal) {
            checkedPosition(normalPosition);
        } else if (beautyMode == BeautyMode.Advanced) {
            checkedPosition(advancedPosition);
        }
    }

    /**
     * 显示详情按钮
     */
    public void showDetailBtn() {
        mBtBeautyDetail.setVisibility(VISIBLE);
        mTvCopyright.setVisibility(VISIBLE);
    }

    /**
     * 显示详情提示
     */
    public void showDetailTips() {
        alivcPopupView.show(mBtBeautyDetail);
    }

    /**
     * 隐藏详情按钮
     */
    public void hideDetailBtn() {
        // 不要使用GONE, 否则会引起整体高度变化
        mBtBeautyDetail.setVisibility(INVISIBLE);
        mTvCopyright.setVisibility(INVISIBLE);
    }

    private void initCopyRight(TextView tvCopyright) {
        tvCopyright.setVisibility(VISIBLE);
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

    public void setBeautyMode(BeautyMode beautyMode, boolean isBeautyFace) {
        this.beautyMode = beautyMode;
        if (beautyMode == BeautyMode.Normal) {
            mRgNormalGroup.setVisibility(VISIBLE);
            mRgAdvancedGroup.setVisibility(GONE);
            if (isBeautyFace) {
                if (beautyMap != null) {
                    normalPosition = beautyMap.get("normal") == null ? 3 : beautyMap.get("normal");
                    normalCheck(normalPosition);
                }
            }
        } else if (beautyMode == BeautyMode.Advanced) {
            mRgNormalGroup.setVisibility(GONE);
            mRgAdvancedGroup.setVisibility(VISIBLE);
        }
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
}
