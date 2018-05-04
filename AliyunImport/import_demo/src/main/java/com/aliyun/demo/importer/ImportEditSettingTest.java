/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.importer;

import android.app.Activity;
import android.content.Intent;
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

import com.aliyun.struct.common.CropKey;
import com.aliyun.struct.common.ScaleMode;
import com.aliyun.struct.common.VideoQuality;



public class ImportEditSettingTest extends Activity implements View.OnClickListener,CompoundButton.OnCheckedChangeListener{

    private static final int PROGRESS_1_1 = 33;
    private static final int PROGRESS_3_4 = 66;
    private static final int PROGRESS_9_16 = 100;

    private static final int PROGRESS_LOW = 25;
    private static final int PROGRESS_MEIDAN = 50;
    private static final int PROGRESS_HIGH = 75;
    private static final int PROGRESS_SUPER = 100;

    private static final int DEFAULT_FRAMR_RATE = 25;
    private static final int DEFAULT_GOP = 125;

    private TextView startImport;
    private SeekBar resolutionSeekbar,qualitySeekbar;
    private TextView resolutionTxt,qualityTxt;
    private RadioButton radioFill,radioCrop;
    private EditText frameRateEdit, gopEdit, mBitrateEdit;
    private ImageView back;
    private int mRatio;
    private VideoQuality videoQuality;
    private ScaleMode scaleMode = CropKey.SCALE_CROP;
//    private EditText etWidth,etHeight;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.aliyun_svideo_import_activity_crop_demo);
        initView();
    }

    private void initView(){
//        etWidth = (EditText) findViewById(R.id.width);
//        etHeight = (EditText) findViewById(R.id.height);
        startImport = (TextView) findViewById(R.id.start_import);
        startImport.setOnClickListener(this);
        resolutionSeekbar = (SeekBar) findViewById(R.id.resolution_seekbar);
        qualitySeekbar = (SeekBar) findViewById(R.id.quality_seekbar);
        resolutionTxt = (TextView) findViewById(R.id.resolution_txt);
        qualityTxt = (TextView) findViewById(R.id.quality_txt);
        radioCrop = (RadioButton) findViewById(R.id.radio_crop);
        radioCrop.setOnCheckedChangeListener(this);
        radioFill = (RadioButton) findViewById(R.id.radio_fill);
        radioFill.setOnCheckedChangeListener(this);
        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(this);
        frameRateEdit = (EditText) findViewById(R.id.frame_rate_edit);
        gopEdit = (EditText) findViewById(R.id.gop_edit);
        mBitrateEdit = (EditText) findViewById(R.id.kbps_edit);
        resolutionSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress <= PROGRESS_1_1){
                    mRatio = CropKey.RATIO_MODE_1_1;
                    resolutionTxt.setText(R.string.resolution_1_1);
                }else if(progress > PROGRESS_1_1 && progress <= PROGRESS_3_4){
                    mRatio = CropKey.RATIO_MODE_3_4;
                    resolutionTxt.setText(R.string.resolution_3_4);
                }else if(progress > PROGRESS_3_4){
                    mRatio = CropKey.RATIO_MODE_9_16;
                    resolutionTxt.setText(R.string.resolution_9_16);
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
        resolutionSeekbar.setProgress(PROGRESS_3_4);
        qualitySeekbar.setProgress(PROGRESS_HIGH);

    }

    @Override
    public void onClick(View v) {
        if(v == startImport){
            Intent intent = new Intent(this,MediaActivity.class);
            intent.putExtra(CropKey.VIDEO_RATIO, mRatio);
            intent.putExtra(CropKey.VIDEO_SCALE,scaleMode);
            intent.putExtra(CropKey.VIDEO_QUALITY , videoQuality);
            String f = frameRateEdit.getText().toString();
            int frameRate ;
            if(f == null || f.isEmpty()){
                frameRate = DEFAULT_FRAMR_RATE;
            }else{
                frameRate = Integer.parseInt(frameRateEdit.getText().toString());
            }

            intent.putExtra(CropKey.VIDEO_FRAMERATE,frameRate);
            String g = gopEdit.getText().toString();
            int gop ;
            if(g == null || g.isEmpty()){
                gop = DEFAULT_GOP;
            }else{
                gop = Integer.parseInt(gopEdit.getText().toString());
            }
            intent.putExtra(CropKey.VIDEO_GOP,gop);
            int bitrate = 0;
            if(!TextUtils.isEmpty(mBitrateEdit.getText())){
                bitrate = Integer.parseInt(mBitrateEdit.getText().toString());
            }
            intent.putExtra(CropKey.VIDEO_BITRATE, bitrate);

//            intent.putExtra("width",etWidth.getText().toString());
//            intent.putExtra("height",etHeight.getText().toString());
            startActivity(intent);
        }else if(v ==  back){
            finish();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked){
            if(buttonView == radioCrop){
                scaleMode = CropKey.SCALE_CROP;
                if(radioFill != null){
                    radioFill.setChecked(false);
                }
            }else if(buttonView ==  radioFill){
                scaleMode = CropKey.SCALE_FILL;
                if(radioCrop != null){
                    radioCrop.setChecked(false);
                }
            }
        }
    }
}
