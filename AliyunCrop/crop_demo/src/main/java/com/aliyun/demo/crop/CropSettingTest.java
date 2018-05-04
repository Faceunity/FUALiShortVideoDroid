/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.crop;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;


import com.aliyun.common.utils.StorageUtils;

import com.aliyun.demo.crop.util.Common;
import com.aliyun.struct.common.ScaleMode;
import com.aliyun.struct.common.VideoQuality;
import com.aliyun.struct.snap.AliyunSnapVideoParam;

import java.io.File;



public class CropSettingTest extends Activity implements View.OnClickListener,CompoundButton.OnCheckedChangeListener{
    String[] eff_dirs;

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

    private static final int DEFAULT_FRAMR_RATE = 25;
    private static final int DEFAULT_GOP = 5;
    private static final int REQUEST_CROP = 2002;

    private TextView startImport;
    private SeekBar resolutionSeekbar,qualitySeekbar, ratioSeekbar;
    private TextView ratioTxt,qualityTxt, resolutionTxt;
    private RadioButton radioFill,radioCrop;
    private EditText frameRateEdit, gopEdit, mBitrateEdit;
    private ImageView back;
    private int resolutionMode ,ratioMode;
    private VideoQuality videoQuality;
    private ScaleMode cropMode = AliyunVideoCrop.SCALE_CROP;
    private ToggleButton mGpuSwitch = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.aliyun_svideo_activity_crop_demo);
        initView();
        initAssetPath();
        copyAssets();
    }

    private void copyAssets() {
        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
                Common.copyAll(CropSettingTest.this);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                startImport.setEnabled(true);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void initAssetPath(){
        String path = StorageUtils.getCacheDirectory(this).getAbsolutePath() + File.separator+ Common.QU_NAME + File.separator;
        File filter = new File(new File(path), "filter");

        String[] list = filter.list();
        if(list == null || list.length == 0){
            return ;
        }
        eff_dirs = new String[list.length + 1];
        eff_dirs[0] = null;
        for(int i = 0; i < list.length; i++){
            eff_dirs[i + 1] = filter.getPath() + "/" + list[i];
        }
//        eff_dirs = new String[]{
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
//                path + "filter/zhaoyang"
//        };
    }


    private void initView(){
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
                if(progress <= PROGRESS_1_1){
                    ratioMode = AliyunSnapVideoParam.RATIO_MODE_1_1;
                    ratioTxt.setText(R.string.aliyun_record_ratio_1_1);
                }else if(progress > PROGRESS_1_1 && progress <= PROGRESS_3_4){
                    ratioMode = AliyunSnapVideoParam.RATIO_MODE_3_4;
                    ratioTxt.setText(R.string.aliyun_record_ratio_3_4);
                }else if(progress > PROGRESS_3_4){
                    ratioMode = AliyunSnapVideoParam.RATIO_MODE_9_16;
                    ratioTxt.setText(R.string.aliyun_record_ratio_9_16);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if(progress <= PROGRESS_1_1){
                    seekBar.setProgress(PROGRESS_1_1);
                }else if(progress > PROGRESS_1_1 && progress <= PROGRESS_3_4){
                    seekBar.setProgress(PROGRESS_3_4);
                }else if(progress > PROGRESS_3_4){
                    seekBar.setProgress(PROGRESS_9_16);
                }
            }
        });
        qualitySeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress <= PROGRESS_LOW){
                    videoQuality = VideoQuality.LD;
                    qualityTxt.setText(R.string.aliyun_video_quality_low);
                }else if(progress > PROGRESS_LOW && progress <= PROGRESS_MEIDAN){
                    videoQuality = VideoQuality.SD;
                    qualityTxt.setText(R.string.aliyun_video_quality_median);
                }else if(progress > PROGRESS_MEIDAN && progress <= PROGRESS_HIGH){
                    videoQuality = VideoQuality.HD;
                    qualityTxt.setText(R.string.aliyun_video_quality_high);
                }else if(progress > PROGRESS_HIGH){
                    videoQuality = VideoQuality.SSD;
                    qualityTxt.setText(R.string.aliyun_video_quality_super);
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
        resolutionSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress <= PROGRESS_360P){
                    resolutionMode = AliyunSnapVideoParam.RESOLUTION_360P;
                    resolutionTxt.setText(R.string.aliyun_crop_resolution_360p);
                }else if(progress > PROGRESS_360P && progress <= PROGRESS_480P){
                    resolutionMode = AliyunSnapVideoParam.RESOLUTION_480P;
                    resolutionTxt.setText(R.string.aliyun_crop_resolution_480p);
                }else if(progress > PROGRESS_480P && progress <= PROGRESS_540P){
                    resolutionMode = AliyunSnapVideoParam.RESOLUTION_540P;
                    resolutionTxt.setText(R.string.aliyun_crop_resolution_540p);
                }else if(progress > PROGRESS_540P){
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
        ratioSeekbar.setProgress(PROGRESS_3_4);
        resolutionSeekbar.setProgress(PROGRESS_540P);
        qualitySeekbar.setProgress(PROGRESS_HIGH);

    }

    @Override
    public void onClick(View v) {
        if(v == startImport){
            String g = gopEdit.getText().toString();
            int gop ;
            if(g == null || g.isEmpty()){
                gop = DEFAULT_GOP;
            }else{
                gop = Integer.parseInt(gopEdit.getText().toString());
            }
            String f = frameRateEdit.getText().toString();
            int frameRate ;
            if(f == null || f.isEmpty()){
                frameRate = DEFAULT_FRAMR_RATE;
            }else{
                frameRate = Integer.parseInt(frameRateEdit.getText().toString());
            }
            int bitrate = 0;
            if(!TextUtils.isEmpty(mBitrateEdit.getText())){
                bitrate = Integer.parseInt(mBitrateEdit.getText().toString());
            }
            AliyunSnapVideoParam mCropParam = new AliyunSnapVideoParam.Builder()
                    .setFrameRate(frameRate)
                    .setGop(gop)
                    .setVideoBitrate(bitrate)
                    .setFilterList(eff_dirs)
                    .setCropMode(cropMode)
                    .setVideoQuality(videoQuality)
                    .setResolutionMode(resolutionMode)
                    .setRatioMode(ratioMode)
                    .setNeedRecord(true)
                    .setMinVideoDuration(2000)
                    .setMaxVideoDuration(60 * 1000 * 1000)
                    .setMinCropDuration(3000)
                    .setSortMode(AliyunSnapVideoParam.SORT_MODE_MERGE)
                    .setCropUseGPU(mGpuSwitch.isChecked())
                    .build();
            AliyunVideoCrop.startCropForResult(this,REQUEST_CROP,mCropParam);
        }else if(v ==  back){
            finish();
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked){
            if(buttonView == radioCrop){
                cropMode = AliyunVideoCrop.SCALE_CROP;
                if(radioFill != null){
                    radioFill.setChecked(false);
                }
            }else if(buttonView ==  radioFill){
                cropMode = AliyunVideoCrop.SCALE_FILL;
                if(radioCrop != null){
                    radioCrop.setChecked(false);
                }
            }
        }
    }
}
