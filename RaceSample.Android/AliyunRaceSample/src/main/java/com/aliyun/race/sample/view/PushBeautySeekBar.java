package com.aliyun.race.sample.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import com.aliyun.race.sample.R;
import com.aliyun.race.sample.view.listener.OnProgresschangeListener;
import com.aliyun.race.sample.view.seekbar.PushIndicatorSeekBar;

/**
 * Created by Akira on 2018/5/30.
 */

public class PushBeautySeekBar extends FrameLayout {
    private Context mContext;

    private PushIndicatorSeekBar mFrontSeekBar;
    private SeekBar mBackSeekBar;

    private boolean mHasHistory;

    public PushBeautySeekBar(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public PushBeautySeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    public PushBeautySeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    public void setProgress(int progress) {
        mFrontSeekBar.setProgress(progress);
        mBackSeekBar.setProgress(progress);
        mBackSeekBar.setVisibility(View.VISIBLE);
    }

    public void setLastProgress(int progress) {
        mHasHistory = true;
        mBackSeekBar.setProgress(progress);
        mFrontSeekBar.setProgress(progress);
        mBackSeekBar.setVisibility(View.VISIBLE);
    }

    public void resetProgress() {
        mFrontSeekBar.setProgress(mBackSeekBar.getProgress());
    }

    private OnProgresschangeListener mListener;

    public void setProgressChangeListener(OnProgresschangeListener listener) {
        mListener = listener;
    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.alivc_race_push_beauty_seekbar, this);
        mFrontSeekBar = findViewById(R.id.push_front_seekbar);
        mBackSeekBar = findViewById(R.id.push_back_seekbar);
        mFrontSeekBar.setIndicatorGap(10);


        mBackSeekBar.setEnabled(false);
        mBackSeekBar.setActivated(false);

        mFrontSeekBar.setOnSeekChangeListener(new PushIndicatorSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(PushIndicatorSeekBar seekBar, int progress, float progressFloat, boolean fromUserTouch) {
                if (mListener != null) {
                    mListener.onProgressChange(progress);
                }
            }

            @Override
            public void onSectionChanged(PushIndicatorSeekBar seekBar, int thumbPosOnTick, String textBelowTick, boolean fromUserTouch) {

            }

            @Override
            public void onStartTrackingTouch(PushIndicatorSeekBar seekBar, int thumbPosOnTick) {

            }

            @Override
            public void onStopTrackingTouch(PushIndicatorSeekBar seekBar) {

            }
        });
    }
}
