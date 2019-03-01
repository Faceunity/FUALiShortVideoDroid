package com.aliyun.svideo.base.widget.beauty;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.aliyun.svideo.R;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautyParamsChangeListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnProgresschangeListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnViewClickListener;
import com.aliyun.svideo.base.widget.beauty.seekbar.BeautySeekBar;

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
    private LinearLayout llBeautyFaceGroup;
    private LinearLayout llBeautySkinGroup;

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
    private OnBlanckViewClickListener onBlanckViewClickListener;
    /**
     * 美颜, 美肌tab下标
     */
    private static final int TAB_BEAUTY_FACE_INDEX = 1;
    private static final int TAB_BEAUTY_SKIN_INDEX = 2;
    private TextView mTvBack;
    /**
     * 美颜等级
     */
    private BeautyParams defaultParams;
    private int beautyLevel = 3;

    public BeautyDetailSettingView(Context context) {
        this(context, null);

    }

    public BeautyDetailSettingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BeautyDetailSettingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.alivc_beauty_detail, this);
        mTvBack = findViewById(R.id.tv_back);
        mSeekBar = findViewById(R.id.beauty_seekbar);
        View blankView = findViewById(R.id.blank_view);
        ImageView mIvReset = findViewById(R.id.iv_reset);
        llBeautyFaceGroup = findViewById(R.id.alivc_beauty_face);
        llBeautySkinGroup = findViewById(R.id.alivc_beauty_skin);
        RadioGroup rgBeautyFaceGroup = findViewById(R.id.beauty_detail_group);
        RadioGroup rgBeautySkinGroup = findViewById(R.id.beauty_skin_detail_group);
        rgBeautyFaceGroup.check(R.id.beauty_buffing);
        rgBeautySkinGroup.check(R.id.beauty_bigeye);

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
                if (mBeautyParamsChangeListener != null) {
                    mBeautyParamsChangeListener.onBeautyChange(mParams);
                }

                if (checkedId == R.id.beauty_buffing) {
                    // 磨皮
                    mCheckedPosition = BeautyConstants.BUFFING;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.beautyBuffing);
                    }
                } else if (checkedId == R.id.beauty_whitening) {
                    // 美白
                    mCheckedPosition = BeautyConstants.WHITENING;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.beautyWhite);
                    }
                } else if (checkedId == R.id.beauty_ruddy) {
                    // 红润
                    mCheckedPosition = BeautyConstants.RUDDY;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.beautyRuddy);
                    }
                }
                setBeautyLevel(beautyLevel);
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
                        mSeekBar.setLastProgress(mParams.beautyBigEye);
                    }
                } else if (checkedId == R.id.beauty_thin_face) {
                    // 瘦脸
                    mCheckedPosition = BeautyConstants.THIN_FACE;
                    if (mParams != null) {
                        mSeekBar.setLastProgress(mParams.beautySlimFace);
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
                        case BeautyConstants.BUFFING:
                            if (mParams.beautyBuffing == progress) {
                                return;
                            }
                            mParams.beautyBuffing = progress;
                            break;

                        case BeautyConstants.WHITENING:
                            if (mParams.beautyWhite == progress) {
                                return;
                            }
                            mParams.beautyWhite = progress;
                            break;

                        case BeautyConstants.RUDDY:
                            if (mParams.beautyRuddy == progress) {
                                return;
                            }
                            mParams.beautyRuddy = progress;
                            break;

                        case BeautyConstants.BIG_EYE:
                            if (mParams.beautyBigEye == progress) {
                                return;
                            }
                            mParams.beautyBigEye = progress;
                            break;

                        case BeautyConstants.THIN_FACE:
                            if (mParams.beautySlimFace == progress) {
                                return;
                            }
                            mParams.beautySlimFace = progress;
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
                mSeekBar.setLastProgress(mParams.beautyBuffing);
                break;
            case BeautyConstants.WHITENING:
                mSeekBar.setLastProgress(mParams.beautyWhite);
                break;
            case BeautyConstants.RUDDY:
                mSeekBar.setLastProgress(mParams.beautyRuddy);
                break;
            case BeautyConstants.BIG_EYE:
                mSeekBar.setLastProgress(mParams.beautyBigEye);
                break;
            case BeautyConstants.THIN_FACE:
                mSeekBar.setLastProgress(mParams.beautySlimFace);
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
            llBeautyFaceGroup.setVisibility(VISIBLE);
            llBeautySkinGroup.setVisibility(GONE);
            mTvBack.setText("美颜");
        } else if (TAB_BEAUTY_SKIN_INDEX == position) {
            llBeautyFaceGroup.setVisibility(GONE);
            llBeautySkinGroup.setVisibility(VISIBLE);
            mTvBack.setText("美肌");
        }
    }

    public void setBeautyConstants(int beautyConstants) {
        this.mCheckedPosition = beautyConstants;
    }

    public void setBeautyLevel(int beautyLevel) {
        this.beautyLevel = beautyLevel;
        defaultParams = BeautyConstants.BEAUTY_MAP.get(beautyLevel);
        switch (mCheckedPosition) {
            case BeautyConstants.BUFFING:
                mSeekBar.setSeekIndicator(defaultParams.beautyBuffing);
                break;

            case BeautyConstants.WHITENING:
                mSeekBar.setSeekIndicator(defaultParams.beautyWhite);
                break;

            case BeautyConstants.RUDDY:
                mSeekBar.setSeekIndicator(defaultParams.beautyRuddy);
                break;

            case BeautyConstants.BIG_EYE:
                mSeekBar.setSeekIndicator(defaultParams.beautyBigEye);
                break;

            case BeautyConstants.THIN_FACE:
                mSeekBar.setSeekIndicator(defaultParams.beautySlimFace);
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
