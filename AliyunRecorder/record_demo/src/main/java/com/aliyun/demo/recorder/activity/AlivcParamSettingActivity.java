/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.recorder.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.aliyun.common.utils.StorageUtils;
import com.aliyun.demo.R;
import com.aliyun.demo.recorder.util.Common;
import com.aliyun.svideo.base.utils.FastClickUtil;
import com.aliyun.svideo.sdk.external.struct.common.VideoDisplayMode;
import com.aliyun.svideo.sdk.external.struct.common.VideoQuality;
import com.aliyun.svideo.sdk.external.struct.encoder.VideoCodecs;
import com.aliyun.svideo.sdk.external.struct.recorder.CameraType;
import com.aliyun.svideo.sdk.external.struct.recorder.FlashType;
import com.aliyun.svideo.sdk.external.struct.snap.AliyunSnapVideoParam;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * 视频录制模块的 录制参数设置界面 原RecorderSettingTest Created by Administrator on 2017/3/2.
 */

public class AlivcParamSettingActivity extends Activity implements View.OnClickListener {

    private EditText minDurationEt, maxDurationEt, gopEt, mBitrateEdit;
    private ImageView mBackBtn;
    //视频质量选择按钮
    private Button mQualitySuperBtn, mQualityHighBtn, mQualityNomalBtn, mQualityLowBtn;
    //视频比例选择按钮
    private Button mRecordRatio9P16Btn, mRecordRatio3P4Btn, mRecordRatio1P1Btn;
    //视频分辨率选择按钮
    private Button mRecordResolutionP360Btn, mRecordResolutionP480Btn, mRecordResolutionP540Btn,
        mRecordResolutionP720Btn;
    //视频编码方式选择按钮
    private Button mEncorderHardwareBtn, mEncorderOpenh264Btn, mEncorderFfmpegBtn;
    private Button mStartRecord;

    private int mResolutionMode, mRatioMode;
    private VideoQuality mVideoQuality;
    private VideoCodecs mVideoCodec = VideoCodecs.H264_SOFT_FFMPEG;
    private String[] mEffDirs;
    private AsyncTask<Void, Void, Void> copyAssetsTask;
    private AsyncTask<Void, Void, Void> initAssetPath;
    /**
     *  判断是编辑模块进入还是通过社区模块的编辑功能进入
     *
     */
    private static final String INTENT_PARAM_KEY_ENTRANCE = "entrance";

    /**
     *  判断是编辑模块进入还是通过社区模块的编辑功能进入
     *  svideo: 短视频
     *  community: 社区
     */
    private String entrance;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.aliyun_svideo_activity_recorder_setting);

        entrance = getIntent().getStringExtra(INTENT_PARAM_KEY_ENTRANCE);
        initView();
        initAssetPath();
        copyAssets();
    }

    private void initAssetPath() {
        initAssetPath = new AssetPathInitTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    public static class AssetPathInitTask extends AsyncTask<Void, Void, Void> {

        private final WeakReference<AlivcParamSettingActivity> weakReference;

        AssetPathInitTask(AlivcParamSettingActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            AlivcParamSettingActivity activity = weakReference.get();
            if (activity != null) {
                activity.setAssetPath();
            }
            return null;
        }
    }

    private void setAssetPath() {
        String path = StorageUtils.getCacheDirectory(this).getAbsolutePath() + File.separator + Common.QU_NAME
            + File.separator;
        File filter = new File(new File(path), "filter");
        String[] list = filter.list();
        if (list == null || list.length == 0) {
            return;
        }
        mEffDirs = new String[list.length + 1];
        mEffDirs[0] = null;
        int length = list.length;
        for (int i = 0; i < length; i++) {
            mEffDirs[i + 1] = filter.getPath() + File.separator + list[i];
        }
    }

    private void copyAssets() {
        mStartRecord.setEnabled(false);
        copyAssetsTask = new CopyAssetsTask(this).executeOnExecutor(
            AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static class CopyAssetsTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<AlivcParamSettingActivity> weakReference;
        ProgressDialog progressBar;
        CopyAssetsTask(AlivcParamSettingActivity activity) {
            weakReference = new WeakReference<>(activity);
            progressBar = new ProgressDialog(activity);
            progressBar.setMessage("资源拷贝中....");
            progressBar.setCanceledOnTouchOutside(false);
            progressBar.setCancelable(false);
            progressBar.setProgressStyle(android.app.ProgressDialog.STYLE_SPINNER);
            progressBar.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            AlivcParamSettingActivity activity = weakReference.get();
            if (activity != null) {
                Common.copyAll(activity);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            AlivcParamSettingActivity activity = weakReference.get();
            if (activity != null) {
                activity.recordEnable();
            }
            progressBar.dismiss();
        }
    }

    private void recordEnable() {
        mStartRecord.setEnabled(true);
    }

    private void initView() {
        minDurationEt = (EditText)findViewById(R.id.aliyun_min_duration_edit);
        maxDurationEt = (EditText)findViewById(R.id.aliyun_max_duration_edit);
        gopEt = (EditText)findViewById(R.id.aliyun_gop_edit);
        mBitrateEdit = (EditText)findViewById(R.id.aliyun_bitrate_edit);

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
        //初始化配置
        onRatioSelected(mRecordRatio9P16Btn);
        onEncoderSelected(mEncorderHardwareBtn);
        onResolutionSelected(mRecordResolutionP720Btn);
        onQualitySelected(mQualityHighBtn);
    }

    @Override
    public void onClick(View v) {
        if (v == mStartRecord) {
            if (FastClickUtil.isFastClick()){
                return;
            }
            int min = 2000;
            int max = 15000;
            int gop = 250;
            int bitrate = 0;

            String minDuration = minDurationEt.getText().toString().trim();
            if (!TextUtils.isEmpty(minDuration)) {
                try {
                    min = Integer.parseInt(minDuration) * 1000;
                } catch (Exception e) {
                    Log.e("ERROR", "input error");
                }
            }
            if (min <= 0) {
                Toast.makeText(this, "最小时长必须大于0秒", Toast.LENGTH_SHORT).show();
                return;
            }
            if (min >= 300000) {
                Toast.makeText(this, "最小时长必须小于300秒", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "最大时长必须大于0秒", Toast.LENGTH_SHORT).show();
                return;
            }
            if (max > 300000) {
                Toast.makeText(this, "最大时长必须小于300秒", Toast.LENGTH_SHORT).show();
                return;
            }

            if (min >= max) {
                Toast.makeText(this, getResources().getString(R.string.aliyun_record_duration_error),
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
            String mBiratedValue = mBitrateEdit.getText().toString().trim();
            if (!TextUtils.isEmpty(mBiratedValue)) {
                try {
                    bitrate = Integer.parseInt(mBiratedValue);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            AliyunSnapVideoParam recordParam = new AliyunSnapVideoParam.Builder()
                .setResolutionMode(mResolutionMode)
                .setRatioMode(mRatioMode)
                .setRecordMode(AliyunSnapVideoParam.RECORD_MODE_AUTO)
                .setFilterList(mEffDirs)
                .setBeautyLevel(80)
                .setBeautyStatus(true)
                .setCameraType(CameraType.FRONT)
                .setFlashType(FlashType.ON)
                .setNeedClip(true)
                .setMaxDuration(max)
                .setMinDuration(min)
                .setVideoQuality(mVideoQuality)
                .setGop(gop)
                .setVideoBitrate(bitrate)
                .setVideoCodec(mVideoCodec)
                /**
                 * 裁剪参数
                 */
                .setMinVideoDuration(4000)
                .setMaxVideoDuration(29 * 1000)
                .setMinCropDuration(3000)
                .setFrameRate(30)
                .setCropMode(VideoDisplayMode.SCALE)
                .build();
            AlivcSvideoRecordActivity.startRecord(this, recordParam, entrance);
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
        } else {
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
        if (copyAssetsTask != null) {
            copyAssetsTask.cancel(true);
            copyAssetsTask = null;
        }

        if (initAssetPath != null) {
            initAssetPath.cancel(true);
            initAssetPath = null;
        }
    }
}
