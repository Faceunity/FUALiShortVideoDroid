package com.aliyun.race.sample.view.shape;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.aliyun.race.sample.R;
import com.aliyun.race.sample.bean.BeautyShapeConstants;
import com.aliyun.race.sample.bean.BeautyShapeParams;
import com.aliyun.race.sample.view.BeautySeekBar;
import com.aliyun.race.sample.view.listener.OnBeautyShapeParamsChangeListener;
import com.aliyun.race.sample.view.listener.OnProgresschangeListener;
import com.aliyun.race.sample.view.listener.OnViewClickListener;

/**
 * 美型微调view
 *
 * @author xlx
 */
public class BeautyShapeDetailSettingView extends LinearLayout {
    /**
     * 美颜美肌参数, 包括磨皮, 美白, 红润, 大眼, 瘦脸
     */
    private BeautyShapeParams mParams;
    /**
     * 当前微调item的下标
     */
    private int mCheckedPosition;

    private BeautySeekBar mSeekBar;
    private LinearLayout mLlBeautyFaceGroup;

    /**
     * back按钮点击listener
     */
    private OnViewClickListener mBackClickListener;
    /**
     * 美颜美肌参数改变listener
     */
    private OnBeautyShapeParamsChangeListener mBeautyShapeParamsChangeListener;
    /**
     * 空白区域点击listener
     */
    private OnBlanckViewClickListener mOnBlanckViewClickListener;

    private TextView mTvBack;
    /**
     * 美颜等级
     */
    private BeautyShapeParams mDefaultParams;
    private int mBeautyLevel = 0;
    private Context mContext;


    public BeautyShapeDetailSettingView(Context context) {
        this(context, null);

    }

    public BeautyShapeDetailSettingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BeautyShapeDetailSettingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.alivc_race_beauty_shape_detail, this);
        mTvBack = findViewById(R.id.tv_back);
        mSeekBar = findViewById(R.id.beauty_seekbar);
        mSeekBar.setMin(-100f);
        mSeekBar.setBackSeekMin(-100);
        View blankView = findViewById(R.id.blank_view);
        ImageView mIvReset = findViewById(R.id.iv_reset);
        mLlBeautyFaceGroup = findViewById(R.id.alivc_beauty_face);
        RadioGroup rgBeautyFaceGroup = findViewById(R.id.beauty_detail_shape_group);
        rgBeautyFaceGroup.check(R.id.beauty_cut_face);

        blankView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnBlanckViewClickListener != null) {
                    mOnBlanckViewClickListener.onBlankClick();
                }
            }
        });
        rgBeautyFaceGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (mBeautyShapeParamsChangeListener != null) {
                    mBeautyShapeParamsChangeListener.onBeautyChange(mParams);
                }
                if (checkedId == R.id.beauty_cut_face) {
                    // 窄脸
                    mSeekBar.setMin(-100f);
                    mSeekBar.setBackSeekMin(-100);
                    mCheckedPosition = BeautyShapeConstants.CUT_FACE;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.mBeautyCutFace);
                    }
                } else if (checkedId == R.id.beauty_thin_face) {
                    // 瘦脸
                    mSeekBar.setMin(0);
                    mSeekBar.setBackSeekMin(0);
                    mCheckedPosition = BeautyShapeConstants.THIN_FACE;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.mBeautyThinFace);
                    }
                } else if (checkedId == R.id.beauty_long_face) {
                    // 脸长
                    mSeekBar.setMin(-100f);
                    mSeekBar.setBackSeekMin(-100);
                    mCheckedPosition = BeautyShapeConstants.LONG_FACE;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.mBeautyLongFace);
                    }
                } else if (checkedId == R.id.beauty_lower_jaw) {
                    // 缩下巴
                    mSeekBar.setMin(-100f);
                    mSeekBar.setBackSeekMin(-100);
                    mCheckedPosition = BeautyShapeConstants.LOWER_JAW;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.mBeautyLowerJaw);
                    }
                } else if (checkedId == R.id.beauty_big_eye) {
                    // 大眼
                    mSeekBar.setMin(0);
                    mSeekBar.setBackSeekMin(0);
                    mCheckedPosition = BeautyShapeConstants.BIG_EYE;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.mBeautyBigEye);
                    }
                } else if (checkedId == R.id.beauty_thin_nose) {
                    // 瘦鼻
                    mSeekBar.setMin(-100f);
                    mSeekBar.setBackSeekMin(-100);
                    mCheckedPosition = BeautyShapeConstants.THIN_NOSE;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.mBeautyThinNose);
                    }
                } else if (checkedId == R.id.beauty_mouth_width) {
                    // 唇宽
                    mSeekBar.setMin(-100f);
                    mSeekBar.setBackSeekMin(-100);
                    mCheckedPosition = BeautyShapeConstants.MOUTH_WIDTH;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.mBeautyMouthWidth);
                    }
                } else if (checkedId == R.id.beauty_thin_mandible) {
                    // 下颌
                    mSeekBar.setMin(-100f);
                    mSeekBar.setBackSeekMin(-100);
                    mCheckedPosition = BeautyShapeConstants.THIN_MANDIBLE;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.mBeautyThinMandible);
                    }
                } else if (checkedId == R.id.beauty_cut_cheek) {
                    // 颧骨
                    mSeekBar.setMin(-100f);
                    mSeekBar.setBackSeekMin(-100);
                    mCheckedPosition = BeautyShapeConstants.CUT_CHEEK;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.mBeautyCutCheek);
                    }
                }
                setBeautyLevel(mBeautyLevel);
            }
        });



        mSeekBar.setProgressChangeListener(new OnProgresschangeListener() {
            @Override
            public void onProgressChange(int progress) {
                if (mParams != null) {
                    switch (mCheckedPosition) {
                    case BeautyShapeConstants.CUT_FACE:
                        if (mParams.mBeautyCutFace == progress) {
                            return;
                        }
                        mParams.mBeautyCutFace = progress;
                        break;

                    case BeautyShapeConstants.THIN_FACE:
                        if (mParams.mBeautyThinFace == progress) {
                            return;
                        }
                        mParams.mBeautyThinFace = progress;
                        break;

                    case BeautyShapeConstants.LONG_FACE:
                        if (mParams.mBeautyLongFace == progress) {
                            return;
                        }
                        mParams.mBeautyLongFace = progress;
                        break;

                    case BeautyShapeConstants.LOWER_JAW:
                        if (mParams.mBeautyLowerJaw == progress) {
                            return;
                        }
                        mParams.mBeautyLowerJaw = progress;
                        break;

                    case BeautyShapeConstants.BIG_EYE:
                        if (mParams.mBeautyBigEye == progress) {
                            return;
                        }
                        mParams.mBeautyBigEye = progress;
                        break;

                    case BeautyShapeConstants.THIN_NOSE:
                        if (mParams.mBeautyThinNose == progress) {
                            return;
                        }
                        mParams.mBeautyThinNose = progress;
                        break;

                    case BeautyShapeConstants.MOUTH_WIDTH:
                        if (mParams.mBeautyMouthWidth == progress) {
                            return;
                        }
                        mParams.mBeautyMouthWidth = progress;
                        break;

                    case BeautyShapeConstants.THIN_MANDIBLE:
                        if (mParams.mBeautyThinMandible == progress) {
                            return;
                        }
                        mParams.mBeautyThinMandible = progress;
                        break;

                    case BeautyShapeConstants.CUT_CHEEK:
                        if (mParams.mBeautyCutCheek == progress) {
                            return;
                        }
                        mParams.mBeautyCutCheek = progress;
                        break;
                    default:
                        break;
                    }
                }

                if (mBeautyShapeParamsChangeListener != null) {
                    mBeautyShapeParamsChangeListener.onBeautyChange(mParams);
                }
            }
        });

        mTvBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBackClickListener != null) {
                    mBackClickListener.onClick();
                }
            }
        });

        mIvReset.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSeekBar.resetProgress();

            }
        });
    }


    public void setParams(BeautyShapeParams params) {
        mParams = params;
        saveProgress();
    }



    public void saveProgress() {

        switch (mCheckedPosition) {
        case BeautyShapeConstants.CUT_FACE:
            mSeekBar.setLastProgress(mParams.mBeautyCutFace);
            break;
        case BeautyShapeConstants.THIN_FACE:
            mSeekBar.setLastProgress(mParams.mBeautyThinFace);
            break;
        case BeautyShapeConstants.LONG_FACE:
            mSeekBar.setLastProgress(mParams.mBeautyLongFace);
            break;
        case BeautyShapeConstants.LOWER_JAW:
            mSeekBar.setLastProgress(mParams.mBeautyLowerJaw);
            break;
        case BeautyShapeConstants.BIG_EYE:
            mSeekBar.setLastProgress(mParams.mBeautyBigEye);
            break;
        case BeautyShapeConstants.THIN_NOSE:
            mSeekBar.setLastProgress(mParams.mBeautyThinNose);
            break;
        case BeautyShapeConstants.MOUTH_WIDTH:
            mSeekBar.setLastProgress(mParams.mBeautyMouthWidth);
            break;
        case BeautyShapeConstants.THIN_MANDIBLE:
            mSeekBar.setLastProgress(mParams.mBeautyThinMandible);
            break;
        case BeautyShapeConstants.CUT_CHEEK:
            mSeekBar.setLastProgress(mParams.mBeautyCutCheek);
            break;
        default:
            break;
        }
    }

    public void setBackClickListener(OnViewClickListener listener) {
        mBackClickListener = listener;
    }

    public void setBeautyParamsChangeListener(OnBeautyShapeParamsChangeListener listener) {
        mBeautyShapeParamsChangeListener = listener;
    }



    public void setBeautyConstants(int beautyConstants) {
        this.mCheckedPosition = beautyConstants;
    }



    public void setBeautyLevel(int beautyLevel) {
        this.mBeautyLevel = beautyLevel;
        switch (beautyLevel){
            case 0:
                mTvBack.setText(R.string.alivc_base_beauty_custom_face);
                break;
            case 1:
                mTvBack.setText(R.string.alivc_base_beauty_grace_face);
                break;
            case 2:
                mTvBack.setText(R.string.alivc_base_beauty_fine_face);
                break;
            case 3:
                mTvBack.setText(R.string.alivc_base_beauty_celebrity_face);
                break;
            case 4:
                mTvBack.setText(R.string.alivc_base_beauty_lovely_face);
                break;
            case 5:
                mTvBack.setText(R.string.alivc_base_beauty_baby_face);
                break;
            case 6:
                mTvBack.setText(R.string.alivc_base_beauty_natural_face);
                break;
            case 7:
                mTvBack.setText(R.string.alivc_base_beauty_square_face);
                break;
            case 8:
                mTvBack.setText(R.string.alivc_base_beauty_round_face);
                break;
            case 9:
                mTvBack.setText(R.string.alivc_base_beauty_long_face);
                break;
            case 10:
                mTvBack.setText(R.string.alivc_base_beauty_pear_face);
                break;
            default:
                mTvBack.setText(R.string.alivc_base_beauty_custom_face);
                break;
        }
        mDefaultParams = BeautyShapeConstants.BEAUTY_MAP.get(beautyLevel);
        switch (mCheckedPosition) {
        case BeautyShapeConstants.CUT_FACE:
            mSeekBar.setSeekIndicator(mDefaultParams.mBeautyCutFace);
            break;

        case BeautyShapeConstants.THIN_FACE:
            mSeekBar.setSeekIndicator(mDefaultParams.mBeautyThinFace);
            break;

        case BeautyShapeConstants.LONG_FACE:
            mSeekBar.setSeekIndicator(mDefaultParams.mBeautyLongFace);
            break;

        case BeautyShapeConstants.LOWER_JAW:
            mSeekBar.setSeekIndicator(mDefaultParams.mBeautyLowerJaw);
            break;

        case BeautyShapeConstants.BIG_EYE:
            mSeekBar.setSeekIndicator(mDefaultParams.mBeautyBigEye);
            break;

        case BeautyShapeConstants.THIN_NOSE:
            mSeekBar.setSeekIndicator(mDefaultParams.mBeautyThinNose);
            break;

        case BeautyShapeConstants.MOUTH_WIDTH:
            mSeekBar.setSeekIndicator(mDefaultParams.mBeautyMouthWidth);
            break;

        case BeautyShapeConstants.THIN_MANDIBLE:
            mSeekBar.setSeekIndicator(mDefaultParams.mBeautyThinMandible);
            break;

        case BeautyShapeConstants.CUT_CHEEK:
            mSeekBar.setSeekIndicator(mDefaultParams.mBeautyCutCheek);
            break;

        default:
            break;
        }
    }

    /**
     * dialog空白区域点击接口
     */
    public interface OnBlanckViewClickListener {
        void onBlankClick();
    }

    public void setOnBlanckViewClickListener(OnBlanckViewClickListener listener) {
        this.mOnBlanckViewClickListener = listener;
    }
}
