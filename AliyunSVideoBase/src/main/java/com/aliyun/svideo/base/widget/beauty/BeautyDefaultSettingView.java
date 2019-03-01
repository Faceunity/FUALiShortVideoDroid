package com.aliyun.svideo.base.widget.beauty;

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

import com.aliyun.svideo.base.widget.AlivcPopupView;
import com.aliyun.svideo.R;
import com.aliyun.svideo.base.widget.beauty.enums.BeautyLevel;
import com.aliyun.svideo.base.widget.beauty.enums.BeautyMode;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautyFaceItemSeletedListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnViewClickListener;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class BeautyDefaultSettingView extends LinearLayout {
    private Context mContext;
    private RadioGroup mRgNormalGroup;
    private RadioGroup mRgAdvancedGroup;
    private ImageView mBtBeautyDetail;
    private BeautyLevel beautyLevel = BeautyLevel.BEAUTY_LEVEL_THREE;
    private int normalPosition = 3;
    private int advancedPosition = 3;
    private BeautyMode beautyMode;
    private AlivcPopupView alivcPopupView;

    public BeautyDefaultSettingView(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public BeautyDefaultSettingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    public BeautyDefaultSettingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.alivc_beauty_default, this);

        mRgNormalGroup = findViewById(R.id.beauty_normal_group);
        mRgAdvancedGroup = findViewById(R.id.beauty_advanced_group);
        mBtBeautyDetail = findViewById(R.id.iv_beauty_detail);

        alivcPopupView  = new AlivcPopupView(mContext);
        TextView textView = new TextView(getContext());
        textView.setLayoutParams(new FrameLayout.LayoutParams(WRAP_CONTENT,WRAP_CONTENT));

//        textView.setLayoutParams(alivcPopupView.generateLayoutParam(
//                dp2px(getContext(), 150),
//                WRAP_CONTENT
//        ));
        textView.setPadding(10,0,10,0);
        textView.setText(getResources().getString(R.string.alivc_tips_fine_tuning));
        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.alivc_base_white));
        alivcPopupView.setContentView(textView);
        mRgNormalGroup.check(R.id.beauty3);
        mRgAdvancedGroup.check(R.id.beauty_advanced_3);

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
    public void showDetailBtn(){
        mBtBeautyDetail.setVisibility(VISIBLE);
    }

    /**
     * 显示详情提示
     */
    public void showDetailTips(){
        alivcPopupView.show(mBtBeautyDetail);
    }

    /**
     * 隐藏详情按钮
     */
    public void hideDetailBtn() {
        // 不要使用GONE, 否则会引起整体高度变化
        mBtBeautyDetail.setVisibility(INVISIBLE);
    }

    public void setBeautyMode(BeautyMode beautyMode) {
        this.beautyMode = beautyMode;
        if (beautyMode == BeautyMode.Normal) {
            mRgNormalGroup.setVisibility(VISIBLE);
            mRgAdvancedGroup.setVisibility(GONE);
        } else if (beautyMode == BeautyMode.Advanced){
            mRgNormalGroup.setVisibility(GONE);
            mRgAdvancedGroup.setVisibility(VISIBLE);
        }
    }
}
