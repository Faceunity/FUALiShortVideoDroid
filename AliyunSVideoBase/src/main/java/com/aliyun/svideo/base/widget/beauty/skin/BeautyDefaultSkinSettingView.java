package com.aliyun.svideo.base.widget.beauty.skin;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.aliyun.svideo.base.R;
import com.aliyun.svideo.base.widget.AlivcPopupView;
import com.aliyun.svideo.base.widget.beauty.enums.BeautyLevel;
import com.aliyun.svideo.base.widget.beauty.enums.BeautyMode;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautySkinItemSeletedListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnViewClickListener;

import java.util.Map;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class BeautyDefaultSkinSettingView extends LinearLayout {
    private Context mContext;
    private RadioGroup mRgSkinGroup;
    private ImageView mBtBeautyDetail;
    private BeautyLevel beautyLevel = BeautyLevel.BEAUTY_LEVEL_THREE;
    private int skinPosition = 3;
    private BeautyMode beautyMode;
    private AlivcPopupView alivcPopupView;
    private Map<String, Integer> beautyMap;

    public BeautyDefaultSkinSettingView(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public BeautyDefaultSkinSettingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    public BeautyDefaultSkinSettingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    public void setDefaultSelect(Map<String, Integer> map) {
        this.beautyMap = map;
        if (map == null) {
            return;
        }
        skinPosition = map.get("skin") == null ? 3 : map.get("skin");
        normalCheck(skinPosition);
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
        mRgSkinGroup.check(normalId);
    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.alivc_beauty_default, this);

        mRgSkinGroup = findViewById(R.id.beauty_normal_group);
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
        mRgSkinGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                checkedPosition(checkedId);
                if (mListener != null) {
                    mListener.onItemSelected(skinPosition);
                }
            }
        });
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
            skinPosition = 0;
            beautyLevel = BeautyLevel.BEAUTY_LEVEL_ZERO;
        } else if (checkedId == R.id.beauty1 || checkedId == R.id.beauty_advanced_1) {
            skinPosition = 1;
            beautyLevel = BeautyLevel.BEAUTY_LEVEL_ONE;
        } else if (checkedId == R.id.beauty2 || checkedId == R.id.beauty_advanced_2) {
            skinPosition = 2;
            beautyLevel = BeautyLevel.BEAUTY_LEVEL_TWO;
        } else if (checkedId == R.id.beauty3 || checkedId == R.id.beauty_advanced_3) {
            skinPosition = 3;
            beautyLevel = BeautyLevel.BEAUTY_LEVEL_THREE;
        } else if (checkedId == R.id.beauty4 || checkedId == R.id.beauty_advanced_4) {
            skinPosition = 4;
            beautyLevel = BeautyLevel.BEAUTY_LEVEL_FOUR;
        } else if (checkedId == R.id.beauty5 || checkedId == R.id.beauty_advanced_5) {
            skinPosition = 5;
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
    private OnBeautySkinItemSeletedListener mListener;

    public void setItemSelectedListener(OnBeautySkinItemSeletedListener listener) {
        mListener = listener;
        if (beautyMode == BeautyMode.SKIN) {
            checkedPosition(skinPosition);
        }
    }

    /**
     * 显示详情按钮
     */
    public void showDetailBtn() {
        mBtBeautyDetail.setVisibility(VISIBLE);
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
    }

    public void setBeautyMode(BeautyMode beautyMode, boolean isBeautyFace) {
        this.beautyMode = beautyMode;
        mRgSkinGroup.setVisibility(VISIBLE);
    }
}
