package com.aliyun.svideo.base.widget.beauty.sharp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.aliyun.svideo.base.R;
import com.aliyun.svideo.base.widget.beauty.BeautyShapeConstants;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautyShapeParamsChangeListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnProgresschangeListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnViewClickListener;
import com.aliyun.svideo.base.widget.beauty.seekbar.BeautySeekBar;

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
    private LinearLayout llBeautyFaceGroup;

    /**
     * back按钮点击listener
     */
    private OnViewClickListener mBackClickListener;
    /**
     * 美颜美肌参数改变listener
     */
    private OnBeautyShapeParamsChangeListener beautyShapeParamsChangeListener;
    /**
     * 空白区域点击listener
     */
    private OnBlanckViewClickListener onBlanckViewClickListener;

    private TextView mTvBack;
    /**
     * 美颜等级
     */
    private BeautyShapeParams defaultParams;
    private int beautyLevel = 0;
    private Context context;


    public BeautyShapeDetailSettingView(Context context) {
        this(context, null);

    }

    public BeautyShapeDetailSettingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BeautyShapeDetailSettingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.alivc_beauty_shape_detail, this);
        mTvBack = findViewById(R.id.tv_back);
        mSeekBar = findViewById(R.id.beauty_seekbar);
        mSeekBar.setMin(-100f);
        mSeekBar.setBackSeekMin(-100);
        View blankView = findViewById(R.id.blank_view);
        ImageView mIvReset = findViewById(R.id.iv_reset);
        llBeautyFaceGroup = findViewById(R.id.alivc_beauty_face);
        RadioGroup rgBeautyFaceGroup = findViewById(R.id.beauty_detail_shape_group);
        rgBeautyFaceGroup.check(R.id.beauty_cut_face);

        blankView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onBlanckViewClickListener != null) {
                    onBlanckViewClickListener.onBlankClick();
                }
            }
        });
        rgBeautyFaceGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (beautyShapeParamsChangeListener != null) {
                    beautyShapeParamsChangeListener.onBeautyChange(mParams);
                }
                if (checkedId == R.id.beauty_cut_face) {
                    // 窄脸
                    mSeekBar.setMin(-100f);
                    mSeekBar.setBackSeekMin(-100);
                    mCheckedPosition = BeautyShapeConstants.CUT_FACE;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.beautyCutFace);
                    }
                } else if (checkedId == R.id.beauty_thin_face) {
                    // 瘦脸
                    mSeekBar.setMin(0);
                    mSeekBar.setBackSeekMin(0);
                    mCheckedPosition = BeautyShapeConstants.THIN_FACE;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.beautyThinFace);
                    }
                } else if (checkedId == R.id.beauty_long_face) {
                    // 脸长
                    mSeekBar.setMin(-100f);
                    mSeekBar.setBackSeekMin(-100);
                    mCheckedPosition = BeautyShapeConstants.LONG_FACE;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.beautyLongFace);
                    }
                } else if (checkedId == R.id.beauty_lower_jaw) {
                    // 缩下巴
                    mSeekBar.setMin(-100f);
                    mSeekBar.setBackSeekMin(-100);
                    mCheckedPosition = BeautyShapeConstants.LOWER_JAW;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.beautyLowerJaw);
                    }
                } else if (checkedId == R.id.beauty_big_eye) {
                    // 大眼
                    mSeekBar.setMin(0);
                    mSeekBar.setBackSeekMin(0);
                    mCheckedPosition = BeautyShapeConstants.BIG_EYE;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.beautyBigEye);
                    }
                } else if (checkedId == R.id.beauty_thin_nose) {
                    // 瘦鼻
                    mSeekBar.setMin(-100f);
                    mSeekBar.setBackSeekMin(-100);
                    mCheckedPosition = BeautyShapeConstants.THIN_NOSE;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.beautyThinNose);
                    }
                } else if (checkedId == R.id.beauty_mouth_width) {
                    // 唇宽
                    mSeekBar.setMin(-100f);
                    mSeekBar.setBackSeekMin(-100);
                    mCheckedPosition = BeautyShapeConstants.MOUTH_WIDTH;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.beautyMouthWidth);
                    }
                } else if (checkedId == R.id.beauty_thin_mandible) {
                    // 下颌
                    mSeekBar.setMin(-100f);
                    mSeekBar.setBackSeekMin(-100);
                    mCheckedPosition = BeautyShapeConstants.THIN_MANDIBLE;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.beautyThinMandible);
                    }
                } else if (checkedId == R.id.beauty_cut_cheek) {
                    // 颧骨
                    mSeekBar.setMin(-100f);
                    mSeekBar.setBackSeekMin(-100);
                    mCheckedPosition = BeautyShapeConstants.CUT_CHEEK;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.beautyCutCheek);
                    }
                }
                setBeautyLevel(beautyLevel);
            }
        });



        mSeekBar.setProgressChangeListener(new OnProgresschangeListener() {
            @Override
            public void onProgressChange(int progress) {
                if (mParams != null) {
                    switch (mCheckedPosition) {
                    case BeautyShapeConstants.CUT_FACE:
                        if (mParams.beautyCutFace == progress) {
                            return;
                        }
                        mParams.beautyCutFace = progress;
                        break;

                    case BeautyShapeConstants.THIN_FACE:
                        if (mParams.beautyThinFace == progress) {
                            return;
                        }
                        mParams.beautyThinFace = progress;
                        break;

                    case BeautyShapeConstants.LONG_FACE:
                        if (mParams.beautyLongFace == progress) {
                            return;
                        }
                        mParams.beautyLongFace = progress;
                        break;

                    case BeautyShapeConstants.LOWER_JAW:
                        if (mParams.beautyLowerJaw == progress) {
                            return;
                        }
                        mParams.beautyLowerJaw = progress;
                        break;

                    case BeautyShapeConstants.BIG_EYE:
                        if (mParams.beautyBigEye == progress) {
                            return;
                        }
                        mParams.beautyBigEye = progress;
                        break;

                    case BeautyShapeConstants.THIN_NOSE:
                        if (mParams.beautyThinNose == progress) {
                            return;
                        }
                        mParams.beautyThinNose = progress;
                        break;

                    case BeautyShapeConstants.MOUTH_WIDTH:
                        if (mParams.beautyMouthWidth == progress) {
                            return;
                        }
                        mParams.beautyMouthWidth = progress;
                        break;

                    case BeautyShapeConstants.THIN_MANDIBLE:
                        if (mParams.beautyThinMandible == progress) {
                            return;
                        }
                        mParams.beautyThinMandible = progress;
                        break;

                    case BeautyShapeConstants.CUT_CHEEK:
                        if (mParams.beautyCutCheek == progress) {
                            return;
                        }
                        mParams.beautyCutCheek = progress;
                        break;
                    default:
                        break;
                    }
                }

                if (beautyShapeParamsChangeListener != null) {
                    beautyShapeParamsChangeListener.onBeautyChange(mParams);
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
            mSeekBar.setLastProgress(mParams.beautyCutFace);
            break;
        case BeautyShapeConstants.THIN_FACE:
            mSeekBar.setLastProgress(mParams.beautyThinFace);
            break;
        case BeautyShapeConstants.LONG_FACE:
            mSeekBar.setLastProgress(mParams.beautyLongFace);
            break;
        case BeautyShapeConstants.LOWER_JAW:
            mSeekBar.setLastProgress(mParams.beautyLowerJaw);
            break;
        case BeautyShapeConstants.BIG_EYE:
            mSeekBar.setLastProgress(mParams.beautyBigEye);
            break;
        case BeautyShapeConstants.THIN_NOSE:
            mSeekBar.setLastProgress(mParams.beautyThinNose);
            break;
        case BeautyShapeConstants.MOUTH_WIDTH:
            mSeekBar.setLastProgress(mParams.beautyMouthWidth);
            break;
        case BeautyShapeConstants.THIN_MANDIBLE:
            mSeekBar.setLastProgress(mParams.beautyThinMandible);
            break;
        case BeautyShapeConstants.CUT_CHEEK:
            mSeekBar.setLastProgress(mParams.beautyCutCheek);
            break;
        default:
            break;
        }
    }

    public void setBackClickListener(OnViewClickListener listener) {
        mBackClickListener = listener;
    }

    public void setBeautyParamsChangeListener(OnBeautyShapeParamsChangeListener listener) {
        beautyShapeParamsChangeListener = listener;
    }



    public void setBeautyConstants(int beautyConstants) {
        this.mCheckedPosition = beautyConstants;
    }



    public void setBeautyLevel(int beautyLevel) {
        this.beautyLevel = beautyLevel;
        switch (beautyLevel) {
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
        defaultParams = BeautyShapeConstants.BEAUTY_MAP.get(beautyLevel);
        switch (mCheckedPosition) {
        case BeautyShapeConstants.CUT_FACE:
            mSeekBar.setSeekIndicator(defaultParams.beautyCutFace);
            break;

        case BeautyShapeConstants.THIN_FACE:
            mSeekBar.setSeekIndicator(defaultParams.beautyThinFace);
            break;

        case BeautyShapeConstants.LONG_FACE:
            mSeekBar.setSeekIndicator(defaultParams.beautyLongFace);
            break;

        case BeautyShapeConstants.LOWER_JAW:
            mSeekBar.setSeekIndicator(defaultParams.beautyLowerJaw);
            break;

        case BeautyShapeConstants.BIG_EYE:
            mSeekBar.setSeekIndicator(defaultParams.beautyBigEye);
            break;

        case BeautyShapeConstants.THIN_NOSE:
            mSeekBar.setSeekIndicator(defaultParams.beautyThinNose);
            break;

        case BeautyShapeConstants.MOUTH_WIDTH:
            mSeekBar.setSeekIndicator(defaultParams.beautyMouthWidth);
            break;

        case BeautyShapeConstants.THIN_MANDIBLE:
            mSeekBar.setSeekIndicator(defaultParams.beautyThinMandible);
            break;

        case BeautyShapeConstants.CUT_CHEEK:
            mSeekBar.setSeekIndicator(defaultParams.beautyCutCheek);
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
        this.onBlanckViewClickListener = listener;
    }
}
