package com.aliyun.race.sample.view.shape;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.aliyun.race.sample.R;
import com.aliyun.race.sample.bean.BeautyLevel;
import com.aliyun.race.sample.bean.BeautyMode;
import com.aliyun.race.sample.view.AlivcPopupView;
import com.aliyun.race.sample.view.listener.OnBeautyDetailClickListener;
import com.aliyun.race.sample.view.listener.OnBeautyShapeItemSeletedListener;

import java.util.Map;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class AlivcBeautyShapeSettingView extends FrameLayout {

    private Context mContext;
    private BeautyMode mBeautyMode = BeautyMode.Advanced;
    private Map<String, Integer> mBeautyMap;
    private RadioGroup mRgSkinGroup;
    private AlivcPopupView mAlivcPopupView;
    private BeautyLevel mBeautyLevel = BeautyLevel.BEAUTY_LEVEL_ZERO;
    private int mSkinPosition = 0;
    private OnBeautyDetailClickListener mOnBeautyDetailClickListener;
    private ImageView mBtBeautyDetail;

    /**
     * 美型item选中
     */
    private OnBeautyShapeItemSeletedListener mOnBeautyShapeItemSeletedListener;




    public AlivcBeautyShapeSettingView(Context context , BeautyMode beautyMode) {
        super(context, null);
        this.mBeautyMode = beautyMode;
        mContext = context;
        initView();
    }

    public AlivcBeautyShapeSettingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
        mContext = context;
        initView();
    }

    public AlivcBeautyShapeSettingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.alivc_race_beauty_shape_layout, this);

        mRgSkinGroup = findViewById(R.id.beauty_normal_group);
        mBtBeautyDetail = findViewById(R.id.iv_beauty_detail);
        mAlivcPopupView = new AlivcPopupView(mContext);
        TextView textView = new TextView(getContext());
        textView.setLayoutParams(new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));

        textView.setPadding(10, 0, 10, 0);
        textView.setText(getResources().getString(R.string.alivc_base_tips_fine_tuning));
        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.alivc_common_font_white));
        mAlivcPopupView.setContentView(textView);
        mRgSkinGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                checkedPosition(checkedId);
                if (mOnBeautyShapeItemSeletedListener != null) {
                    mOnBeautyShapeItemSeletedListener.onItemSelected(mSkinPosition);
                }
            }
        });
        mBtBeautyDetail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnBeautyDetailClickListener != null) {
                    mOnBeautyDetailClickListener.onDetailClick();
                }
            }
        });
    }

    private void checkedPosition(int checkedId) {

        if (checkedId == R.id.beauty0 || checkedId == R.id.beauty_advanced_0) {
            mSkinPosition = 0;
            mBeautyLevel = BeautyLevel.BEAUTY_LEVEL_ZERO;
        } else if (checkedId == R.id.beauty1 || checkedId == R.id.beauty_advanced_1) {
            mSkinPosition = 1;
            mBeautyLevel = BeautyLevel.BEAUTY_LEVEL_ONE;
        } else if (checkedId == R.id.beauty2 || checkedId == R.id.beauty_advanced_2) {
            mSkinPosition = 2;
            mBeautyLevel = BeautyLevel.BEAUTY_LEVEL_TWO;
        } else if (checkedId == R.id.beauty3 || checkedId == R.id.beauty_advanced_3) {
            mSkinPosition = 3;
            mBeautyLevel = BeautyLevel.BEAUTY_LEVEL_THREE;
        } else if (checkedId == R.id.beauty4 || checkedId == R.id.beauty_advanced_4) {
            mSkinPosition = 4;
            mBeautyLevel = BeautyLevel.BEAUTY_LEVEL_FOUR;
        } else if (checkedId == R.id.beauty5 || checkedId == R.id.beauty_advanced_5) {
            mSkinPosition = 5;
            mBeautyLevel = BeautyLevel.BEAUTY_LEVEL_FIVE;
        }
    }

    public void setDefaultSelect(Map<String, Integer> map) {
        this.mBeautyMap = map;
        if (map == null) {
            return;
        }
        mSkinPosition = map.get("shape") == null ? 0 : map.get("shape");
        normalCheck(mSkinPosition);
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
                normalId = R.id.beauty0;
                break;
        }
        mRgSkinGroup.check(normalId);
    }

    /**
     * 美肌item选中监听
     *
     * @param listener
     */
    public void setOnBeautySharpItemSelecedtListener(OnBeautyShapeItemSeletedListener listener) {
        this.mOnBeautyShapeItemSeletedListener = listener;
    }

    /**
     * 详情选中监听
     * @param onBeautyDetailClickListener
     */
    public void setOnBeautyDetailClickListener(
            OnBeautyDetailClickListener onBeautyDetailClickListener) {
        this.mOnBeautyDetailClickListener = onBeautyDetailClickListener;
    }

}
