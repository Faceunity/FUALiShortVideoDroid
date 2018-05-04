/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.recorder;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aliyun.common.utils.StorageUtils;
import com.aliyun.demo.R;
import com.aliyun.demo.recorder.util.Common;
import com.aliyun.struct.common.ScaleMode;
import com.aliyun.struct.common.VideoQuality;
import com.aliyun.struct.encoder.VideoCodecs;
import com.aliyun.struct.recorder.CameraType;
import com.aliyun.struct.recorder.FlashType;
import com.aliyun.struct.snap.AliyunSnapVideoParam;

import java.io.File;

/**
 * Created by Administrator on 2017/3/2.
 */

public class RecorderSettingTest extends Activity implements View.OnClickListener{

    private static final int PROGRESS_LOW = 25;
    private static final int PROGRESS_MEIDAN = 50;
    private static final int PROGRESS_HIGH = 75;
    private static final int PROGRESS_SUPER = 100;


    private static final int PROGRESS_360P = 25;
    private static final int PROGRESS_480P = 50;
    private static final int PROGRESS_540P = 75;
    private static final int PROGRESS_720P = 100;

    private static final int PROGRESS_3_4 = 33;
    private static final int PROGRESS_1_1 = 66;
    private static final int PROGRESS_9_16 = 100;

    private static final int VIDEO_CODEC_HARDWARE = 0;
    private static final int VIDEO_CODEC_OPENH264 = 1;
    private static final int VIDEO_CODEC_FFMPEG = 2;

    private static final int REQUEST_RECORD = 2001;

    private TextView mStartRecordTxt, mRecordResolutionTxt, mVideoQualityTxt, mVideoRatioTxt;
    private SeekBar mResolutionBar, mVideoQualityBar, mVideoRatioBar; /*mVideoCodecBar*/
    private EditText minDurationEt,maxDurationEt,gopEt,mBitrateEdit;
    private ImageView mBackBtn;

    private int mResolutionMode, mRatioMode;
    private VideoQuality mVideoQuality;
    private VideoCodecs mVideoCodec = VideoCodecs.H264_HARDWARE;
    private String[] mEffDirs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aliyun_svideo_activity_recorder_setting);
        initView();
        initAssetPath();
        copyAssets();
    }

    private void initAssetPath(){
        String path = StorageUtils.getCacheDirectory(this).getAbsolutePath() + File.separator+ Common.QU_NAME + File.separator;
        File filter = new File(new File(path), "filter");

        String[] list = filter.list();
        if(list == null || list.length == 0){
            return ;
        }
        mEffDirs = new String[list.length + 1];
        mEffDirs[0] = null;
        for(int i = 0; i < list.length; i++){
            mEffDirs[i + 1] = filter.getPath() + "/" + list[i];
        }
//        mEffDirs = new String[]{
//                null,
//                path + "filter/chihuang",
//                path + "filter/fentao",
//                path + "filter/hailan",
//                path + "filter/hongrun",
//                path + "filter/huibai",
//                path + "filter/jingdian",
//                path + "filter/maicha",
//                path + "filter/nonglie",
//                path + "filter/rourou",
//                path + "filter/shanyao",
//                path + "filter/xianguo",
//                path + "filter/xueli",
//                path + "filter/yangguang",
//                path + "filter/youya",
//                path + "filter/zhaoyang",
//                path + "filter/mosaic",
//                path + "filter/blur",
//                path + "filter/bulge",
//                path + "filter/false",
//                path + "filter/gray",
//                path + "filter/haze",
//                path + "filter/invert",
//                path + "filter/miss",
//                path + "filter/pixellate",
//                path + "filter/rgb",
//                path + "filter/sepiatone",
//                path + "filter/threshold",
//                path + "filter/tone",
//                path + "filter/vignette"
//
//        };
    }



    private void copyAssets() {
        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
                Common.copyAll(RecorderSettingTest.this);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                mStartRecordTxt.setEnabled(true);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void initView(){
        minDurationEt = (EditText) findViewById(R.id.aliyun_min_duration_edit);
        maxDurationEt = (EditText) findViewById(R.id.aliyun_max_duration_edit);
        gopEt = (EditText)findViewById(R.id.aliyun_gop_edit);
        mBitrateEdit = (EditText) findViewById(R.id.aliyun_bitrate_edit);
        mStartRecordTxt = (TextView)findViewById(R.id.aliyun_start_record);
        mStartRecordTxt.setOnClickListener(this);
        mStartRecordTxt.setEnabled(false);
        mBackBtn = (ImageView) findViewById(R.id.aliyun_back);
        mBackBtn.setOnClickListener(this);
        mRecordResolutionTxt = (TextView) findViewById(R.id.aliyun_resolution_txt);
        mVideoQualityTxt = (TextView) findViewById(R.id.aliyun_quality_txt);
        mVideoRatioTxt = (TextView) findViewById(R.id.aliyun_ratio_txt);
        mResolutionBar = (SeekBar) findViewById(R.id.aliyun_resolution_seekbar);
        mVideoQualityBar = (SeekBar) findViewById(R.id.aliyun_quality_seekbar);
        mVideoRatioBar = (SeekBar) findViewById(R.id.aliyun_ratio_seekbar);
//        mVideoCodecBar = (SeekBar) findViewById(R.id.aliyun_video_codec_seekbar);
        mResolutionBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress <= PROGRESS_360P){
                    mResolutionMode = AliyunSnapVideoParam.RESOLUTION_360P;
                    mRecordResolutionTxt.setText(R.string.aliyun_record_resolution_360p);
                }else if(progress > PROGRESS_360P && progress <= PROGRESS_480P){
                    mResolutionMode = AliyunSnapVideoParam.RESOLUTION_480P;
                    mRecordResolutionTxt.setText(R.string.aliyun_record_resolution_480p);
                }else if(progress > PROGRESS_480P && progress <= PROGRESS_540P){
                    mResolutionMode = AliyunSnapVideoParam.RESOLUTION_540P;
                    mRecordResolutionTxt.setText(R.string.aliyun_record_resolution_540p);
                }else if(progress > PROGRESS_540P){
                    mResolutionMode = AliyunSnapVideoParam.RESOLUTION_720P;
                    mRecordResolutionTxt.setText(R.string.aliyun_record_resolution_720p);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if(progress < PROGRESS_360P){
                    seekBar.setProgress(PROGRESS_360P);
                } else if (progress > PROGRESS_360P && progress <= PROGRESS_480P) {
                    seekBar.setProgress(PROGRESS_480P);
                } else if(progress > PROGRESS_480P && progress <= PROGRESS_540P){
                    seekBar.setProgress(PROGRESS_540P);
                } else if(progress > PROGRESS_540P){
                    seekBar.setProgress(PROGRESS_720P);
                }
            }
        });
        mVideoQualityBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress <= PROGRESS_LOW){
                    mVideoQuality = VideoQuality.LD;
                    mVideoQualityTxt.setText(R.string.aliyun_video_quality_low);
                }else if(progress > PROGRESS_LOW && progress <= PROGRESS_MEIDAN){
                    mVideoQuality = VideoQuality.SD;
                    mVideoQualityTxt.setText(R.string.aliyun_video_quality_meidan);
                }else if(progress > PROGRESS_MEIDAN && progress <= PROGRESS_HIGH){
                    mVideoQuality = VideoQuality.HD;
                    mVideoQualityTxt.setText(R.string.aliyun_video_quality_high);
                }else if(progress > PROGRESS_HIGH){
                    mVideoQuality = VideoQuality.SSD;
                    mVideoQualityTxt.setText(R.string.aliyun_video_quality_super);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if(progress <= PROGRESS_LOW){
                    seekBar.setProgress(PROGRESS_LOW);
                }else if(progress > PROGRESS_LOW && progress <= PROGRESS_MEIDAN){
                    seekBar.setProgress(PROGRESS_MEIDAN);
                }else if(progress > PROGRESS_MEIDAN && progress <= PROGRESS_HIGH){
                    seekBar.setProgress(PROGRESS_HIGH);
                }else if(progress > PROGRESS_HIGH){
                    seekBar.setProgress(PROGRESS_SUPER);
                }
            }
        });
        mVideoRatioBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress <= PROGRESS_3_4){
                    mRatioMode = AliyunSnapVideoParam.RATIO_MODE_3_4;
                    mVideoRatioTxt.setText(R.string.aliyun_record_ratio_3_4);
                }else if(progress > PROGRESS_3_4 && progress <= PROGRESS_1_1){
                    mRatioMode = AliyunSnapVideoParam.RATIO_MODE_1_1;
                    mVideoRatioTxt.setText(R.string.aliyun_record_ratio_1_1);
                }else if(progress > PROGRESS_1_1 && progress <= PROGRESS_9_16){
                    mRatioMode = AliyunSnapVideoParam.RATIO_MODE_9_16;
                    mVideoRatioTxt.setText(R.string.aliyun_reocrd_ratio_9_16);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if(progress < PROGRESS_3_4){
                    seekBar.setProgress(PROGRESS_3_4);
                } else if (progress > PROGRESS_3_4 && progress <= PROGRESS_1_1) {
                    seekBar.setProgress(PROGRESS_1_1);
                } else if(progress > PROGRESS_1_1 && progress <= PROGRESS_9_16){
                    seekBar.setProgress(PROGRESS_9_16);
                }
            }
        });
//        mVideoCodecBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                switch (progress) {
//                    case VIDEO_CODEC_HARDWARE:
//                        mVideoCodec = VideoCodecs.H264_HARDWARE;
//                        break;
//                    case VIDEO_CODEC_OPENH264:
//                        mVideoCodec = VideoCodecs.H264_SOFT_OPENH264;
//                        break;
//                    case VIDEO_CODEC_FFMPEG:
//                        mVideoCodec = VideoCodecs.H264_SOFT_FFMPEG;
//                        break;
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//            }
//        });
        mResolutionBar.setProgress(PROGRESS_540P);
        mVideoQualityBar.setProgress(PROGRESS_HIGH);
        mVideoRatioBar.setProgress(PROGRESS_3_4);
    }
    @Override
    public void onClick(View v) {
        if(v == mStartRecordTxt){
            int min = 2000;
            int max = 30000;
            int gop = 5;
            int bitrate = 0;
            if(minDurationEt.getText() != null && !minDurationEt.getText().toString().isEmpty()){
                try {
                    min = Integer.parseInt(minDurationEt.getText().toString()) * 1000;
                }catch (Exception e){
                    Log.e("ERROR","input error");
                }
            }
            if(maxDurationEt.getText() != null && !maxDurationEt.getText().toString().isEmpty()){
                try {
                    max = Integer.parseInt(maxDurationEt.getText().toString()) * 1000;
                }catch (Exception e){
                    Log.e("ERROR","input error");
                }
            }
            if(gopEt.getText() != null && !gopEt.getText().toString().isEmpty()){
                try {
                    gop = Integer.parseInt(gopEt.getText().toString());
                }catch (Exception e){
                    Log.e("ERROR","input error");
                }
            }
            if(!TextUtils.isEmpty(mBitrateEdit.getText())){
                try{
                    bitrate = Integer.parseInt(mBitrateEdit.getText().toString());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            AliyunSnapVideoParam recordParam = new AliyunSnapVideoParam.Builder()
                    .setResolutionMode(mResolutionMode)
                    .setRatioMode(mRatioMode)
                    .setRecordMode(AliyunSnapVideoParam.RECORD_MODE_AUTO)
                    .setFilterList(mEffDirs)
                    .setBeautyLevel(80)
                    .setBeautyStatus(false)
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
                    .setFrameRate(25)
                    .setCropMode(ScaleMode.PS)
                    .build();
            AliyunVideoRecorder.startRecord(this,recordParam);
        }else if(v == mBackBtn){
            finish();
        }
    }
}
