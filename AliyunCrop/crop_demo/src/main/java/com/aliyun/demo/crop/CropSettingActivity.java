/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.crop;

import android.Manifest;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.aliyun.common.utils.StorageUtils;
import com.aliyun.demo.crop.util.Common;
import com.aliyun.svideo.sdk.external.struct.common.VideoDisplayMode;
import com.aliyun.svideo.sdk.external.struct.common.VideoQuality;
import com.aliyun.svideo.sdk.external.struct.encoder.VideoCodecs;
import com.aliyun.svideo.sdk.external.struct.snap.AliyunSnapVideoParam;
import com.aliyun.video.common.utils.FastClickUtil;
import com.aliyun.video.common.utils.PermissionUtils;
import com.aliyun.video.common.utils.ToastUtils;

import java.io.File;



public class CropSettingActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    String[] effDirs;

    private static final int PROGRESS_1_1 = 33;
    private static final int PROGRESS_3_4 = 66;
    private static final int PROGRESS_9_16 = 100;

    private static final int PROGRESS_360P = 25;
    private static final int PROGRESS_480P = 50;
    private static final int PROGRESS_540P = 75;
    private static final int PROGRESS_720P = 100;

    private static final int PROGRESS_LOW = 25;
    private static final int PROGRESS_MEIDAN = 50;
    private static final int PROGRESS_HIGH = 75;
    private static final int PROGRESS_SUPER = 100;

    /**
     * 默认帧率
     */
    private static final int DEFAULT_FRAMR_RATE = 30;
    /**
     * 默认Gop参数
     */
    private static final int DEFAULT_GOP = 250;
    private static final int REQUEST_CROP = 2002;

    private TextView startImport;
    private SeekBar resolutionSeekbar, qualitySeekbar, ratioSeekbar;
    private TextView ratioTxt, qualityTxt, resolutionTxt;
    private RadioButton radioFill, radioCrop;
    private EditText frameRateEdit, gopEdit, mBitrateEdit;
    private ImageView back;
    private int resolutionMode, ratioMode;
    private VideoQuality videoQuality;
    private VideoDisplayMode cropMode = AliyunVideoCropActivity.SCALE_CROP;
    private VideoCodecs mVideoCodec = VideoCodecs.H264_HARDWARE;
    private ToggleButton mGpuSwitch = null;
    private boolean ifPaused = false;
    //视频编码方式选择按钮
    private RadioButton mEncorderHardwareBtn, mEncorderOpenh264Btn, mEncorderFfmpegBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_crop_demo);
        initView();
        copyAssets();

    }

    private void copyAssets() {
        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
                Common.copyAll(CropSettingActivity.this);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                startImport.setEnabled(true);
            }
        } .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void initAssetPath() {
        String path = StorageUtils.getCacheDirectory(this).getAbsolutePath() + File.separator + Common.QU_NAME + File.separator;
        File filter = new File(new File(path), "filter");

        String[] list = filter.list();
        if (list == null || list.length == 0) {
            return ;
        }
        effDirs = new String[list.length + 1];
        effDirs[0] = null;
        for (int i = 0; i < list.length; i++) {
            effDirs[i + 1] = filter.getPath() + "/" + list[i];
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ifPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ifPaused) {
            //editor.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        startImport = (TextView) findViewById(R.id.aliyun_start_import);
        startImport.setOnClickListener(this);
        startImport.setEnabled(false);
        resolutionSeekbar = (SeekBar) findViewById(R.id.aliyun_resolution_seekbar);
        qualitySeekbar = (SeekBar) findViewById(R.id.aliyun_quality_seekbar);
        ratioSeekbar = (SeekBar) findViewById(R.id.aliyun_ratio_seekbar);
        ratioTxt = (TextView) findViewById(R.id.aliyun_ratio_txt);
        qualityTxt = (TextView) findViewById(R.id.aliyun_quality_txt);
        resolutionTxt = (TextView) findViewById(R.id.aliyun_resolution_txt);
        radioCrop = (RadioButton) findViewById(R.id.aliyun_radio_crop);
        radioCrop.setOnCheckedChangeListener(this);
        radioFill = (RadioButton) findViewById(R.id.aliyun_radio_fill);
        radioFill.setOnCheckedChangeListener(this);
        back = (ImageView) findViewById(R.id.aliyun_back);
        back.setOnClickListener(this);
        mGpuSwitch = (ToggleButton) findViewById(R.id.tbtn_gpu);
        frameRateEdit = (EditText) findViewById(R.id.aliyun_frame_rate_edit);
        gopEdit = (EditText) findViewById(R.id.aliyun_gop_edit);
        mBitrateEdit = (EditText) findViewById(R.id.aliyun_bitrate_edit);
        ratioSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress <= PROGRESS_1_1) {
                    ratioMode = AliyunSnapVideoParam.RATIO_MODE_1_1;
                    ratioTxt.setText(R.string.aliyun_crop_ratio_1_1);
                } else if (progress > PROGRESS_1_1 && progress <= PROGRESS_3_4) {
                    ratioMode = AliyunSnapVideoParam.RATIO_MODE_3_4;
                    ratioTxt.setText(R.string.aliyun_crop_ratio_3_4);
                } else if (progress > PROGRESS_3_4) {
                    ratioMode = AliyunSnapVideoParam.RATIO_MODE_9_16;
                    ratioTxt.setText(R.string.aliyun_crop_ratio_9_16);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (progress <= PROGRESS_1_1) {
                    seekBar.setProgress(PROGRESS_1_1);
                } else if (progress > PROGRESS_1_1 && progress <= PROGRESS_3_4) {
                    seekBar.setProgress(PROGRESS_3_4);
                } else if (progress > PROGRESS_3_4) {
                    seekBar.setProgress(PROGRESS_9_16);
                }
            }
        });
        qualitySeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress <= PROGRESS_LOW) {
                    videoQuality = VideoQuality.LD;
                    qualityTxt.setText(R.string.aliyun_svideo_crop_quality_low);
                } else if (progress > PROGRESS_LOW && progress <= PROGRESS_MEIDAN) {
                    videoQuality = VideoQuality.SD;
                    qualityTxt.setText(R.string.aliyun_svideo_crop_quality_median);
                } else if (progress > PROGRESS_MEIDAN && progress <= PROGRESS_HIGH) {
                    videoQuality = VideoQuality.HD;
                    qualityTxt.setText(R.string.aliyun_svideo_crop_quality_high);
                } else if (progress > PROGRESS_HIGH) {
                    videoQuality = VideoQuality.SSD;
                    qualityTxt.setText(R.string.aliyun_svideo_crop_quality_super);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (progress <= PROGRESS_LOW) {
                    seekBar.setProgress(PROGRESS_LOW);
                } else if (progress > PROGRESS_LOW && progress <= PROGRESS_MEIDAN) {
                    seekBar.setProgress(PROGRESS_MEIDAN);
                } else if (progress > PROGRESS_MEIDAN && progress <= PROGRESS_HIGH) {
                    seekBar.setProgress(PROGRESS_HIGH);
                } else if (progress > PROGRESS_HIGH) {
                    seekBar.setProgress(PROGRESS_SUPER);
                }
            }
        });
        resolutionSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress <= PROGRESS_360P) {
                    resolutionMode = AliyunSnapVideoParam.RESOLUTION_360P;
                    resolutionTxt.setText(R.string.aliyun_crop_resolution_360p);
                } else if (progress > PROGRESS_360P && progress <= PROGRESS_480P) {
                    resolutionMode = AliyunSnapVideoParam.RESOLUTION_480P;
                    resolutionTxt.setText(R.string.aliyun_crop_resolution_480p);
                } else if (progress > PROGRESS_480P && progress <= PROGRESS_540P) {
                    resolutionMode = AliyunSnapVideoParam.RESOLUTION_540P;
                    resolutionTxt.setText(R.string.aliyun_crop_resolution_540p);
                } else if (progress > PROGRESS_540P) {
                    resolutionMode = AliyunSnapVideoParam.RESOLUTION_720P;
                    resolutionTxt.setText(R.string.aliyun_crop_resolution_720p);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (progress < PROGRESS_360P) {
                    seekBar.setProgress(PROGRESS_360P);
                } else if (progress > PROGRESS_360P && progress <= PROGRESS_480P) {
                    seekBar.setProgress(PROGRESS_480P);
                } else if (progress > PROGRESS_480P && progress <= PROGRESS_540P) {
                    seekBar.setProgress(PROGRESS_540P);
                } else if (progress > PROGRESS_540P) {
                    seekBar.setProgress(PROGRESS_720P);
                }
            }
        });
        ratioSeekbar.setProgress(PROGRESS_9_16);
        resolutionSeekbar.setProgress(PROGRESS_720P);
        qualitySeekbar.setProgress(PROGRESS_HIGH);


        //视频编码相关按钮
        mEncorderHardwareBtn = findViewById(R.id.alivc_crop_encoder_hardware);
        mEncorderOpenh264Btn = findViewById(R.id.alivc_crop_encoder_openh264);
        mEncorderFfmpegBtn = findViewById(R.id.alivc_crop_encoder_ffmpeg);
        mEncorderHardwareBtn.setOnCheckedChangeListener(this);
        mEncorderOpenh264Btn.setOnCheckedChangeListener(this);
        mEncorderFfmpegBtn.setOnCheckedChangeListener(this);

        onEncoderSelected(mEncorderHardwareBtn);
    }

    @Override
    public void onClick(View v) {
        if (v == startImport) {

            if (FastClickUtil.isFastClickActivity(this.getClass().getSimpleName())) {
                return;
            }
            //判断是否有权限，如果没有则不会打开MediaActivity
            boolean externalStorage = PermissionUtils.checkPermissionsGroup(this,
                                      new String[] {Manifest.permission.READ_EXTERNAL_STORAGE});
            if (!externalStorage) {
                ToastUtils.show(this, R.string.no_read_external_storage_permission);
                return ;
            }

            String inputGop = gopEdit.getText().toString();
            int gop = DEFAULT_GOP;
            if (!TextUtils.isEmpty(inputGop)) {
                try {
                    gop = Integer.parseInt(inputGop);
                } catch (Exception e) {
                    Log.e("ERROR", "input error");
                }
            }

            String inputFrameRate = frameRateEdit.getText().toString();
            int frameRate  = DEFAULT_FRAMR_RATE;
            if (!TextUtils.isEmpty(inputFrameRate)) {
                try {
                    frameRate = Integer.parseInt(inputFrameRate);
                } catch (Exception e) {
                    Log.e("ERROR", "input error");
                }
            }

            int bitrate = 0;
            String inputBitrate = mBitrateEdit.getText().toString();
            if (!TextUtils.isEmpty(inputBitrate)) {
                try {
                    bitrate = Integer.parseInt(inputBitrate);
                } catch (Exception e) {
                    Log.e("ERROR", "input error");
                }
            }

            initAssetPath();
            AliyunSnapVideoParam mCropParam = new AliyunSnapVideoParam.Builder()
            .setFrameRate(frameRate)
            .setGop(gop)
            .setVideoBitrate(bitrate)
            .setFilterList(effDirs)
            .setCropMode(cropMode)
            .setVideoQuality(videoQuality)
            .setVideoCodec(mVideoCodec)
            .setResolutionMode(resolutionMode)
            .setRatioMode(ratioMode)
            .setNeedRecord(false)
            .setMinVideoDuration(2000)
            .setMaxVideoDuration(60 * 1000 * 1000)
            .setMinCropDuration(3000)
            .setSortMode(AliyunSnapVideoParam.SORT_MODE_MERGE)
            .setCropUseGPU(mGpuSwitch.isChecked())
            .build();
            AliyunVideoCropActivity.startCropForResult(this, REQUEST_CROP, mCropParam);
        }
        if (v ==  back) {
            finish();
        }
    }

    /**
     * 视频编码方式选择
     *
     * @param view
     */
    private void onEncoderSelected(View view) {

        if (view == mEncorderFfmpegBtn) {
            mVideoCodec = VideoCodecs.H264_SOFT_FFMPEG;
            mEncorderFfmpegBtn.setChecked(true);
            mEncorderHardwareBtn.setChecked(false);
            mEncorderOpenh264Btn.setChecked(false);
        } else if (view == mEncorderOpenh264Btn) {
            mEncorderOpenh264Btn.setChecked(true);
            mEncorderFfmpegBtn.setChecked(false);
            mEncorderHardwareBtn.setChecked(false);
            mVideoCodec = VideoCodecs.H264_SOFT_OPENH264;
        } else {
            mEncorderHardwareBtn.setChecked(true);
            mEncorderFfmpegBtn.setChecked(false);
            mEncorderOpenh264Btn.setChecked(false);
            mVideoCodec = VideoCodecs.H264_HARDWARE;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (buttonView == radioCrop) {
                cropMode = AliyunVideoCropActivity.SCALE_CROP;
                if (radioFill != null) {
                    radioFill.setChecked(false);
                }
            } else if (buttonView ==  radioFill) {
                cropMode = AliyunVideoCropActivity.SCALE_FILL;
                if (radioCrop != null) {
                    radioCrop.setChecked(false);
                }
            } else if (buttonView == mEncorderFfmpegBtn || buttonView == mEncorderOpenh264Btn || buttonView == mEncorderHardwareBtn ) {
                onEncoderSelected(buttonView);
            }
        }
    }
}
