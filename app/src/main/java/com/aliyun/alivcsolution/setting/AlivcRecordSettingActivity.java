/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.alivcsolution.setting;

import android.Manifest;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.aliyun.alivcsolution.R;
import com.aliyun.svideo.base.utils.FastClickUtil;
import com.aliyun.svideo.base.widget.ProgressDialog;
import com.aliyun.svideo.common.utils.PermissionUtils;
import com.aliyun.svideo.common.utils.ToastUtils;
import com.aliyun.svideo.recorder.activity.AlivcMixMediaActivity;
import com.aliyun.svideo.recorder.activity.AlivcSvideoRecordActivity;
import com.aliyun.svideo.recorder.bean.AlivcRecordInputParam;
import com.aliyun.svideo.recorder.bean.RenderingMode;
import com.aliyun.svideo.recorder.util.RecordCommon;
import com.aliyun.svideosdk.common.struct.common.AliyunSnapVideoParam;
import com.aliyun.svideosdk.common.struct.common.VideoQuality;
import com.aliyun.svideosdk.common.struct.encoder.VideoCodecs;
import com.aliyun.svideosdk.mixrecorder.AliyunMixBorderParam;
import com.aliyun.svideosdk.mixrecorder.MixAudioSourceType;

import java.lang.ref.WeakReference;

/**
 * 视频录制模块的 录制参数设置界面 原RecorderSettingTest Created by Administrator on 2017/3/2.
 */

public class AlivcRecordSettingActivity extends Activity implements View.OnClickListener {

    private EditText minDurationEt, maxDurationEt, gopEt;
    private ImageView mBackBtn;
    //视频质量选择按钮
    private Button mQualitySuperBtn, mQualityHighBtn, mQualityNomalBtn, mQualityLowBtn;
    //视频比例选择按钮
    private Button mRecordRatio9P16Btn, mRecordRatio3P4Btn, mRecordRatio1P1Btn;
    //视频分辨率选择按钮
    private Button mRecordResolutionP360Btn, mRecordResolutionP480Btn, mRecordResolutionP540Btn,
            mRecordResolutionP720Btn;
    /**
     * 视频编码方式选择按钮
     */
    private Button mEncorderHardwareBtn, mEncorderOpenh264Btn, mEncorderFfmpegBtn;
    private Button mStartRecord;

    /**
     * 拍摄方式选择按钮 普通，合拍
     */
    private Button mRecordGeneral, mRecordMix;
    /**
     * 渲染方式选择按钮 faceunity，race
     */
    private Button mRecordFaceUnity, mRecordRace;

    private int mResolutionMode, mRatioMode;
    private VideoQuality mVideoQuality;
    private VideoCodecs mVideoCodec = VideoCodecs.H264_SOFT_FFMPEG;
    private RenderingMode mRenderingMode = RenderingMode.Race;
    private Switch mSwitchFlip;
    private AsyncTask<Void, Void, Void> copyAssetsTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.aliyun_svideo_activity_recorder_setting);
//        copyAssets();
        initView();
    }

    private void recordEnable() {
        mStartRecord.setEnabled(true);
    }

    private void initView() {
        minDurationEt = (EditText)findViewById(R.id.aliyun_min_duration_edit);
        maxDurationEt = (EditText)findViewById(R.id.aliyun_max_duration_edit);
        mSwitchFlip = (Switch)findViewById(R.id.alivc_record_switch_flip);
        gopEt = (EditText)findViewById(R.id.aliyun_gop_edit);

        mStartRecord = findViewById(R.id.aliyun_start_record);
        mStartRecord.setOnClickListener(this);
        mBackBtn = (ImageView)findViewById(R.id.aliyun_back);
        mBackBtn.setOnClickListener(this);

        mQualitySuperBtn = findViewById(R.id.alivc_video_quality_super);
        mQualitySuperBtn.setOnClickListener(this);
        mQualityHighBtn = findViewById(R.id.alivc_video_quality_high);
        mQualityHighBtn.setOnClickListener(this);
        mQualityNomalBtn = findViewById(R.id.alivc_video_quality_normal);
        mQualityNomalBtn.setOnClickListener(this);
        mQualityLowBtn = findViewById(R.id.alivc_video_quality_low);
        mQualityLowBtn.setOnClickListener(this);
        //视频分辨率选择相关按钮
        mRecordResolutionP360Btn = findViewById(R.id.alivc_record_resolution_360p);
        mRecordResolutionP480Btn = findViewById(R.id.alivc_record_resolution_480p);
        mRecordResolutionP540Btn = findViewById(R.id.alivc_record_resolution_540p);
        mRecordResolutionP720Btn = findViewById(R.id.alivc_record_resolution_720p);
        mRecordResolutionP360Btn.setOnClickListener(this);
        mRecordResolutionP480Btn.setOnClickListener(this);
        mRecordResolutionP540Btn.setOnClickListener(this);
        mRecordResolutionP720Btn.setOnClickListener(this);
        //视频编码相关按钮
        mEncorderHardwareBtn = findViewById(R.id.alivc_record_encoder_hardware);
        mEncorderOpenh264Btn = findViewById(R.id.alivc_record_encoder_openh264);
        mEncorderFfmpegBtn = findViewById(R.id.alivc_record_encoder_ffmpeg);
        mEncorderHardwareBtn.setOnClickListener(this);
        mEncorderOpenh264Btn.setOnClickListener(this);
        mEncorderFfmpegBtn.setOnClickListener(this);

        //视频比例相关按钮
        mRecordRatio9P16Btn = findViewById(R.id.alivc_video_ratio_9_16);
        mRecordRatio3P4Btn = findViewById(R.id.alivc_video_ratio_3_4);
        mRecordRatio1P1Btn = findViewById(R.id.alivc_video_ratio_1_1);
        mRecordRatio9P16Btn.setOnClickListener(this);
        mRecordRatio3P4Btn.setOnClickListener(this);
        mRecordRatio1P1Btn.setOnClickListener(this);

        //拍摄方式
        mRecordGeneral = findViewById(R.id.alivc_video_record_general);
        mRecordMix = findViewById(R.id.alivc_video_record_mix);
        mRecordMix.setOnClickListener(this);
        mRecordGeneral.setOnClickListener(this);

        //渲染方式
        mRecordFaceUnity = findViewById(R.id.alivc_video_record_faceunity);
        mRecordRace = findViewById(R.id.alivc_video_record_race);
        mRecordFaceUnity.setOnClickListener(this);
        mRecordRace.setOnClickListener(this);
        mRecordFaceUnity.setVisibility(View.GONE);
        mRecordRace.setVisibility(View.GONE);

        //初始化配置
        onRatioSelected(mRecordRatio9P16Btn);
        onEncoderSelected(mEncorderHardwareBtn);
        onResolutionSelected(mRecordResolutionP720Btn);
        onQualitySelected(mQualityHighBtn);
        onRecordSelected(mRecordGeneral);
        onRenderingSelected(mRecordRace);
    }
    private void copyAssets() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                copyAssetsTask = new CopyAssetsTask(AlivcRecordSettingActivity.this).executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }, 700);

    }

    public class CopyAssetsTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<AlivcRecordSettingActivity> weakReference;
        ProgressDialog progressBar;

        CopyAssetsTask(AlivcRecordSettingActivity activity) {

            weakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AlivcRecordSettingActivity activity = weakReference.get();
            if (activity != null && !activity.isFinishing()) {
                progressBar = new ProgressDialog(activity, R.style.NoBackgroundDlgStyle);
                progressBar.setCanceledOnTouchOutside(false);
                progressBar.setCancelable(false);
                progressBar.setProgressStyle(android.app.ProgressDialog.STYLE_SPINNER);
                progressBar.show();
            }

        }

        @Override
        protected Void doInBackground(Void... voids) {
            AlivcRecordSettingActivity activity = weakReference.get();
            if (activity != null) {
                RecordCommon.copyRace(activity);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            AlivcRecordSettingActivity activity = weakReference.get();
            if (activity != null && !activity.isFinishing()) {
                progressBar.dismiss();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mStartRecord) {
            if (FastClickUtil.isFastClick()) {
                return;
            }
            int min = 2000;
            int max = 15000;
            int gop = 30;

            String minDuration = minDurationEt.getText().toString().trim();
            if (!TextUtils.isEmpty(minDuration)) {
                try {
                    min = Integer.parseInt(minDuration) * 1000;
                } catch (Exception e) {
                    Log.e("ERROR", "input error");
                }
            }
            if (min <= 0) {
                Toast.makeText(this, getResources().getString(R.string.aliyun_min_record_duration_more_than), Toast.LENGTH_SHORT).show();
                return;
            }
            if (min >= 300000) {
                Toast.makeText(this, getResources().getString(R.string.aliyun_min_record_duration_less_than), Toast.LENGTH_SHORT).show();
                return;
            }
            String maxDuration = maxDurationEt.getText().toString().trim();
            if (!TextUtils.isEmpty(maxDuration)) {
                try {
                    max = Integer.parseInt(maxDuration) * 1000;
                } catch (Exception e) {
                    Log.e("ERROR", "input error");
                }
            }
            if (max <= 0) {
                Toast.makeText(this, getResources().getString(R.string.aliyun_max_record_duration_more_than), Toast.LENGTH_SHORT).show();
                return;
            }
            if (max > 300000) {
                Toast.makeText(this, getResources().getString(R.string.aliyun_max_record_duration_less_than), Toast.LENGTH_SHORT).show();
                return;
            }

            if (min >= max) {
                Toast.makeText(this, getResources().getString(R.string.alivc_recorder_setting_tip_duration_error),
                               Toast.LENGTH_SHORT).show();
                return;
            }

            String gopValue = gopEt.getText().toString().trim();
            if (!TextUtils.isEmpty(gopValue)) {
                try {
                    gop = Integer.parseInt(gopValue);
                } catch (Exception e) {
                    Log.e("ERROR", "input error");
                }
            }

            AlivcRecordInputParam recordParam = new AlivcRecordInputParam.Builder()
            .setResolutionMode(mResolutionMode)
            .setRatioMode(mRatioMode)
            .setMaxDuration(max)
            .setMinDuration(min)
            .setVideoQuality(mVideoQuality)
            .setGop(gop)
            .setVideoCodec(mVideoCodec)
            .setIsUseFlip(mSwitchFlip.isChecked())
            .setVideoRenderingMode(mRenderingMode)
            .build();
            if (mRecordGeneral.isSelected()) {
                AlivcSvideoRecordActivity.startRecord(this, recordParam);
            } else {
                //判断是否有权限，如果没有则不会打开MediaActivity
                boolean externalStorage = PermissionUtils.checkPermissionsGroup(this,
                                          new String[] {Manifest.permission.READ_EXTERNAL_STORAGE});
                if (!externalStorage) {
                    ToastUtils.show(this, R.string.alivc_common_no_read_phone_state_permission);
                    return ;
                }
                AlivcMixMediaActivity.startRecord(this, recordParam);
            }
        } else if (v == mBackBtn) {
            finish();
        } else if (v == mEncorderFfmpegBtn || v == mEncorderHardwareBtn || v == mEncorderOpenh264Btn) {
            onEncoderSelected(v);
        } else if (v == mQualityHighBtn || v == mQualityLowBtn || v == mQualitySuperBtn || v == mQualityNomalBtn) {
            onQualitySelected(v);
        } else if (v == mRecordRatio1P1Btn || v == mRecordRatio3P4Btn || v == mRecordRatio9P16Btn) {
            onRatioSelected(v);
        } else if (v == mRecordResolutionP360Btn || v == mRecordResolutionP480Btn || mRecordResolutionP540Btn == v
                   || v == mRecordResolutionP720Btn) {
            onResolutionSelected(v);
        } else if ((v == mRecordGeneral)) {
            onRecordSelected(v);
        } else if (v == mRecordMix) {
            onRecordSelected(v);
        } else if ((v == mRecordFaceUnity)) {
            onRenderingSelected(v);
        } else if (v == mRecordRace) {
            onRenderingSelected(v);
        }
    }

    /**
     * 拍摄方式选择
     */
    private void onRecordSelected(View view) {
        mRecordGeneral.setSelected(false);
        mRecordMix.setSelected(false);
        if (view == mRecordMix) {
            mStartRecord.setText(getResources().getText(R.string.aliyun_start_mix_record));
            mRecordMix.setSelected(true);
            mRecordGeneral.setSelected(false);
        } else {
            mStartRecord.setText(getResources().getText(R.string.alivc_recorder_setting_start_record));
            mRecordMix.setSelected(false);
            mRecordGeneral.setSelected(true);
        }
    }
    /**
     * 渲染方式选择
     */
    private void onRenderingSelected(View view) {
        mRecordFaceUnity.setSelected(false);
        mRecordRace.setSelected(false);
        if (view == mRecordFaceUnity) {
            mRecordFaceUnity.setSelected(true);
            mRecordRace.setSelected(false);
            mRenderingMode = RenderingMode.FaceUnity;
        } else {
            mRecordFaceUnity.setSelected(false);
            mRecordRace.setSelected(true);
            mRenderingMode = RenderingMode.Race;
        }
    }

    /**
     * 视频质量选择
     *
     * @param view 被点击按钮
     */
    private void onQualitySelected(View view) {
        mQualitySuperBtn.setSelected(false);
        mQualityHighBtn.setSelected(false);
        mQualityNomalBtn.setSelected(false);
        mQualityLowBtn.setSelected(false);
        if (view == mQualitySuperBtn) {
            mVideoQuality = VideoQuality.SSD;
            mQualitySuperBtn.setSelected(true);
        } else if (view == mQualityHighBtn) {
            mQualityHighBtn.setSelected(true);
            mVideoQuality = VideoQuality.HD;
        } else if (view == mQualityNomalBtn) {
            mQualityNomalBtn.setSelected(true);
            mVideoQuality = VideoQuality.SD;
        } else {
            mQualityLowBtn.setSelected(true);
            mVideoQuality = VideoQuality.LD;
        }
    }

    /**
     * 视频比例选择
     *
     * @param view
     */
    private void onRatioSelected(View view) {
        mRecordRatio9P16Btn.setSelected(false);
        mRecordRatio3P4Btn.setSelected(false);
        mRecordRatio1P1Btn.setSelected(false);
        if (view == mRecordRatio1P1Btn) {
            mRatioMode = AliyunSnapVideoParam.RATIO_MODE_1_1;
            mRecordRatio1P1Btn.setSelected(true);
        } else if (view == mRecordRatio9P16Btn) {
            mRatioMode = AliyunSnapVideoParam.RATIO_MODE_9_16;
            mRecordRatio9P16Btn.setSelected(true);
        } else {
            mRecordRatio3P4Btn.setSelected(true);
            mRatioMode = AliyunSnapVideoParam.RATIO_MODE_3_4;
        }
    }

    /**
     * 视频编码方式选择
     *
     * @param view
     */
    private void onEncoderSelected(View view) {
        mEncorderFfmpegBtn.setSelected(false);
        mEncorderHardwareBtn.setSelected(false);
        mEncorderOpenh264Btn.setSelected(false);

        if (view == mEncorderFfmpegBtn) {
            mVideoCodec = VideoCodecs.H264_SOFT_FFMPEG;
            mEncorderFfmpegBtn.setSelected(true);
        } else if (view == mEncorderOpenh264Btn) {
            mEncorderOpenh264Btn.setSelected(true);
            mVideoCodec = VideoCodecs.H264_SOFT_OPENH264;
        } else if (view == mEncorderHardwareBtn) {
            mEncorderHardwareBtn.setSelected(true);
            mVideoCodec = VideoCodecs.H264_HARDWARE;
        }
    }

    /**
     * 视频分辨率选择
     *
     * @param view
     */
    private void onResolutionSelected(View view) {
        mRecordResolutionP360Btn.setSelected(false);
        mRecordResolutionP480Btn.setSelected(false);
        mRecordResolutionP540Btn.setSelected(false);
        mRecordResolutionP720Btn.setSelected(false);
        if (view == mRecordResolutionP360Btn) {
            mRecordResolutionP360Btn.setSelected(true);
            mResolutionMode = AliyunSnapVideoParam.RESOLUTION_360P;
        } else if (view == mRecordResolutionP480Btn) {
            mRecordResolutionP480Btn.setSelected(true);
            mResolutionMode = AliyunSnapVideoParam.RESOLUTION_480P;

        } else if (view == mRecordResolutionP540Btn) {
            mResolutionMode = AliyunSnapVideoParam.RESOLUTION_540P;
            mRecordResolutionP540Btn.setSelected(true);

        } else {
            mRecordResolutionP720Btn.setSelected(true);
            mResolutionMode = AliyunSnapVideoParam.RESOLUTION_720P;

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
