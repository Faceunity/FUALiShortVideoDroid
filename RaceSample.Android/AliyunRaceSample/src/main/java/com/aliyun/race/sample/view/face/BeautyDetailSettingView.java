package com.aliyun.race.sample.view.face;

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
import com.aliyun.race.sample.bean.BeautyParams;
import com.aliyun.race.sample.utils.constants.BeautyRaceConstants;
import com.aliyun.race.sample.view.BeautyConstants;
import com.aliyun.race.sample.view.BeautySeekBar;
import com.aliyun.race.sample.view.listener.OnBeautyParamsChangeListener;
import com.aliyun.race.sample.view.listener.OnProgresschangeListener;
import com.aliyun.race.sample.view.listener.OnViewClickListener;

/**
 * 美颜美肌微调view
 *
 * @author xlx
 */
public class BeautyDetailSettingView extends LinearLayout {
    /**
     * 美颜美肌参数, 包括磨皮, 美白, 红润, 大眼, 瘦脸
     */
    private BeautyParams mParams;
    /**
     * 当前微调item的下标
     */
    private int mCheckedPosition;

    private BeautySeekBar mSeekBar;
    private LinearLayout mLlBeautyFaceGroup;
    private LinearLayout mLlBeautySkinGroup;

    /**
     * back按钮点击listener
     */
    private OnViewClickListener mBackClickListener;
    /**
     * 美颜美肌参数改变listener
     */
    private OnBeautyParamsChangeListener mBeautyParamsChangeListener;
    /**
     * 空白区域点击listener
     */
    private OnBlanckViewClickListener mOnBlanckViewClickListener;
    /**
     * 美颜, 美肌tab下标
     */
    private static final int TAB_BEAUTY_FACE_INDEX = 1;
    private static final int TAB_BEAUTY_SKIN_INDEX = 2;
    private TextView mTvBack;
    /**
     * 美颜等级
     */
    private BeautyParams mDefaultParams;
    private int mBeautyLevel = 3;
    private Context mContext;

    /**
     * 红润/锐化
     */
    private TextView mBlushTv;

    public BeautyDetailSettingView(Context context) {
        this(context, null);

    }

    public BeautyDetailSettingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BeautyDetailSettingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.alivc_race_beauty_detail, this);
        mTvBack = findViewById(R.id.tv_back);
        mSeekBar = findViewById(R.id.beauty_seekbar);
        mBlushTv = findViewById(R.id.alivc_base_beauty_blush_textview);
        View blankView = findViewById(R.id.blank_view);
        ImageView mIvReset = findViewById(R.id.iv_reset);
        mLlBeautyFaceGroup = findViewById(R.id.alivc_beauty_face);
        mLlBeautySkinGroup = findViewById(R.id.alivc_beauty_skin);
        RadioGroup rgBeautyFaceGroup = findViewById(R.id.beauty_detail_group);
        RadioGroup rgBeautySkinGroup = findViewById(R.id.beauty_skin_detail_group);
        rgBeautyFaceGroup.check(R.id.beauty_buffing);
        rgBeautySkinGroup.check(R.id.beauty_bigeye);

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
                if (mBeautyParamsChangeListener != null) {
                    mBeautyParamsChangeListener.onBeautyChange(mParams);
                }

                if (checkedId == R.id.beauty_buffing) {
                    // 磨皮
                    mCheckedPosition = BeautyConstants.BUFFING;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.mBeautyBuffing);
                    }
                } else if (checkedId == R.id.beauty_whitening) {
                    // 美白
                    mCheckedPosition = BeautyConstants.WHITENING;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.mBeautyWhite);
                    }
                } else if (checkedId == R.id.beauty_ruddy) {
                    // 红润
                    mCheckedPosition = BeautyConstants.RUDDY;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.mBeautyRuddy);
                    }
                }
                setBeautyLevel(mBeautyLevel);
            }
        });

        rgBeautySkinGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (mBeautyParamsChangeListener != null) {
                    mBeautyParamsChangeListener.onBeautyChange(mParams);
                }

                if (checkedId == R.id.beauty_bigeye) {
                    // 大眼
                    mCheckedPosition = BeautyConstants.BIG_EYE;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.mBeautyBigEye);
                    }
                } else if (checkedId == R.id.beauty_thin_face) {
                    // 瘦脸
                    mCheckedPosition = BeautyConstants.THIN_FACE;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.mBeautySlimFace);
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
                    case BeautyConstants.BUFFING:
                        if (mParams.mBeautyBuffing == progress) {
                            return;
                        }
                        mParams.mBeautyBuffing = progress;
                        break;

                    case BeautyConstants.WHITENING:
                        if (mParams.mBeautyWhite == progress) {
                            return;
                        }
                        mParams.mBeautyWhite = progress;
                        break;

                    case BeautyConstants.RUDDY:
                        if (mParams.mBeautyRuddy == progress) {
                            return;
                        }
                        mParams.mBeautyRuddy = progress;
                        break;

                    case BeautyConstants.BIG_EYE:
                        if (mParams.mBeautyBigEye == progress) {
                            return;
                        }
                        mParams.mBeautyBigEye = progress;
                        break;

                    case BeautyConstants.THIN_FACE:
                        if (mParams.mBeautySlimFace == progress) {
                            return;
                        }
                        mParams.mBeautySlimFace = progress;
                        break;
                    default:
                        break;
                    }
                }

                if (mBeautyParamsChangeListener != null) {
                    mBeautyParamsChangeListener.onBeautyChange(mParams);
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

    public void setParams(BeautyParams params) {
        mParams = params;
        saveProgress();
    }

    public void saveProgress() {

        switch (mCheckedPosition) {
        case BeautyConstants.BUFFING:
            mSeekBar.setLastProgress(mParams.mBeautyBuffing);
            break;
        case BeautyConstants.WHITENING:
            mSeekBar.setLastProgress(mParams.mBeautyWhite);
            break;
        case BeautyConstants.RUDDY:
            mSeekBar.setLastProgress(mParams.mBeautyRuddy);
            break;
        case BeautyConstants.BIG_EYE:
            mSeekBar.setLastProgress(mParams.mBeautyBigEye);
            break;
        case BeautyConstants.THIN_FACE:
            mSeekBar.setLastProgress(mParams.mBeautySlimFace);
            break;
        default:
            break;
        }
    }

    public void setBackClickListener(OnViewClickListener listener) {
        mBackClickListener = listener;
    }

    public void setBeautyParamsChangeListener(OnBeautyParamsChangeListener listener) {
        mBeautyParamsChangeListener = listener;
    }

    /**
     * B 根据不同的tab, 微调界面显示不同内容
     *
     * @param position
     */
    public void updateDetailLayout(int position) {
        // 如果当前tab是美颜就显示美颜, 隐藏美肌
        if (TAB_BEAUTY_FACE_INDEX == position) {
            mLlBeautyFaceGroup.setVisibility(VISIBLE);
            mLlBeautySkinGroup.setVisibility(GONE);
            mTvBack.setText(getResources().getString(R.string.alivc_base_beauty));
        } else if (TAB_BEAUTY_SKIN_INDEX == position) {
            mLlBeautyFaceGroup.setVisibility(GONE);
            mLlBeautySkinGroup.setVisibility(VISIBLE);
            mTvBack.setText(getResources().getString(R.string.alivc_base_beauty_shape));
        }
    }

    public void setBeautyConstants(int beautyConstants) {
        this.mCheckedPosition = beautyConstants;
    }

    public void setBeautyLevel(int beautyLevel) {
        this.mBeautyLevel = beautyLevel;
            mDefaultParams = BeautyRaceConstants.BEAUTY_MAP.get(beautyLevel);
        switch (mCheckedPosition) {
        case BeautyConstants.BUFFING:
            mSeekBar.setSeekIndicator(mDefaultParams.mBeautyBuffing);
            break;

        case BeautyConstants.WHITENING:
            mSeekBar.setSeekIndicator(mDefaultParams.mBeautyWhite);
            break;

        case BeautyConstants.RUDDY:
            mSeekBar.setSeekIndicator(mDefaultParams.mBeautyRuddy);
            break;

        case BeautyConstants.BIG_EYE:
            mSeekBar.setSeekIndicator(mDefaultParams.mBeautyBigEye);
            break;

        case BeautyConstants.THIN_FACE:
            mSeekBar.setSeekIndicator(mDefaultParams.mBeautySlimFace);
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
