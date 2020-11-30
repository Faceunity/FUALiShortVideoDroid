package com.aliyun.race.sample.view.face;

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

import com.aliyun.race.sample.R;
import com.aliyun.race.sample.bean.BeautyLevel;
import com.aliyun.race.sample.bean.BeautyMode;
import com.aliyun.race.sample.view.AlivcPopupView;
import com.aliyun.race.sample.view.listener.OnBeautyFaceItemSeletedListener;
import com.aliyun.race.sample.view.listener.OnViewClickListener;

import java.util.Map;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class BeautyDefaultFaceSettingView extends LinearLayout {
    private Context mContext;
    private RadioGroup mRgNormalGroup;
    private RadioGroup mRgAdvancedGroup;
    private ImageView mBtBeautyDetail;
    private BeautyLevel mBeautyLevel = BeautyLevel.BEAUTY_LEVEL_THREE;
    private int mNormalPosition = 3;
    private int mAdvancedPosition = 3;
    private BeautyMode mBeautyMode;
    private AlivcPopupView mAlivcPopupView;
    private Map<String, Integer> mBeautyMap;

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
        this.mBeautyMap = map;
        mAdvancedPosition = map.get("advance") == null ? 3 : map.get("advance");
        mNormalPosition = map.get("normal") == null ? 3 : map.get("normal");

        if (mBeautyMode == BeautyMode.Normal) {
            normalCheck(mNormalPosition);
        } else if (mBeautyMode == BeautyMode.Advanced) {
            advancedCheck(mAdvancedPosition);
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
        LayoutInflater.from(mContext).inflate(R.layout.alivc_race_beauty_default, this);

        mRgNormalGroup = findViewById(R.id.beauty_normal_group);
        mRgAdvancedGroup = findViewById(R.id.beauty_advanced_group);
        mBtBeautyDetail = findViewById(R.id.iv_beauty_detail);

        mAlivcPopupView = new AlivcPopupView(mContext);
        TextView textView = new TextView(getContext());
        textView.setLayoutParams(new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));

        textView.setPadding(10, 0, 10, 0);
        textView.setText(getResources().getString(R.string.alivc_base_tips_fine_tuning));
        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.alivc_common_font_white));
        mAlivcPopupView.setContentView(textView);

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
                    mListener.onNormalSelected(mNormalPosition, mBeautyLevel);
                }
            }
        });

        mRgAdvancedGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                checkedPosition(checkedId);
                if (mListener != null) {
                    mListener.onAdvancedSelected(mAdvancedPosition, mBeautyLevel);
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
            mNormalPosition = 0;
            mAdvancedPosition = 0;
            mBeautyLevel = BeautyLevel.BEAUTY_LEVEL_ZERO;
        } else if (checkedId == R.id.beauty1 || checkedId == R.id.beauty_advanced_1) {
            mNormalPosition = 1;
            mAdvancedPosition = 1;
            mBeautyLevel = BeautyLevel.BEAUTY_LEVEL_ONE;
        } else if (checkedId == R.id.beauty2 || checkedId == R.id.beauty_advanced_2) {
            mNormalPosition = 2;
            mAdvancedPosition = 2;
            mBeautyLevel = BeautyLevel.BEAUTY_LEVEL_TWO;
        } else if (checkedId == R.id.beauty3 || checkedId == R.id.beauty_advanced_3) {
            mNormalPosition = 3;
            mAdvancedPosition = 3;
            mBeautyLevel = BeautyLevel.BEAUTY_LEVEL_THREE;
        } else if (checkedId == R.id.beauty4 || checkedId == R.id.beauty_advanced_4) {
            mNormalPosition = 4;
            mAdvancedPosition = 4;
            mBeautyLevel = BeautyLevel.BEAUTY_LEVEL_FOUR;
        } else if (checkedId == R.id.beauty5 || checkedId == R.id.beauty_advanced_5) {
            mNormalPosition = 5;
            mAdvancedPosition = 5;
            mBeautyLevel = BeautyLevel.BEAUTY_LEVEL_FIVE;
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
        if (mBeautyMode == BeautyMode.Normal) {
            checkedPosition(mNormalPosition);
        } else if (mBeautyMode == BeautyMode.Advanced) {
            checkedPosition(mAdvancedPosition);
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
        mAlivcPopupView.show(mBtBeautyDetail);
    }

    /**
     * 隐藏详情按钮
     */
    public void hideDetailBtn() {
        // 不要使用GONE, 否则会引起整体高度变化
        mBtBeautyDetail.setVisibility(INVISIBLE);
    }

    public void setBeautyMode(BeautyMode beautyMode, boolean isBeautyFace) {
        this.mBeautyMode = beautyMode;
        if (beautyMode == BeautyMode.Normal) {
            mRgNormalGroup.setVisibility(VISIBLE);
            mRgAdvancedGroup.setVisibility(GONE);
            if (isBeautyFace) {
                if (mBeautyMap != null) {
                    mNormalPosition = mBeautyMap.get("normal") == null ? 3 : mBeautyMap.get("normal");
                    normalCheck(mNormalPosition);
                }
            }
        } else if (beautyMode == BeautyMode.Advanced) {
            mRgNormalGroup.setVisibility(GONE);
            mRgAdvancedGroup.setVisibility(VISIBLE);
        }
    }
}
