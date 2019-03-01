/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;

import com.aliyun.svideo.base.AlivcSvideoEditParam;
import com.aliyun.svideo.base.utils.FastClickUtil;
import com.aliyun.svideo.sdk.external.struct.common.VideoDisplayMode;
import com.aliyun.svideo.sdk.external.struct.common.VideoQuality;
import com.aliyun.svideo.sdk.external.struct.encoder.VideoCodecs;
import com.aliyun.video.common.utils.PermissionUtils;
import com.aliyun.video.common.utils.ToastUtils;

/**
 * 视频编辑模块, 参数设置界面
 */
public class EditorSettingActivity extends Activity implements View.OnClickListener,CompoundButton.OnCheckedChangeListener{


    /**
     *  判断是编辑模块进入还是通过社区模块的编辑功能进入
     */
    private static final String INTENT_PARAM_KEY_ENTRANCE = "entrance";
    /**
     *  intent 是否添加了片尾水印的key
     */
    private static final String INTENT_PARAM_KEY_HAS_TAIL_ANIMATION = "hasTailAnimation";

    /**
     * 默认帧率
     */
    private static final int DEFAULT_FRAME_RATE = 30;
    private static final int DEFAULT_GOP = 250;
    /**
     * 视频原比例
     */
    public static final int RATIO_ORIGINAL = 3;


    private ImageView back;

    /**
     * 视频比例 9:16,3:4,1:1,原比例
     */
    private int mRatio = AlivcSvideoEditParam.RATIO_MODE_9_16;
    /**
     * 分辨率设置 360P、480P、540P、720P
     */

    private int mResolutionMode;
    /**
     * 视频质量 SSD:HD:SD:LD
     */
    private VideoQuality videoQuality;

    /**
     * 视频/图片的显示风格 CROP（裁剪）、FILL（填充）
     */
    private VideoDisplayMode scaleMode = VideoDisplayMode.SCALE;

    /**
     * 视频编码模式，默认硬编
     */
    private VideoCodecs mVideoCodec = VideoCodecs.H264_HARDWARE;

    /**
     * 是否由片尾水印，默认无
     */
    private boolean mHasTailAnimation = false;

    //视频质量选择按钮
    private Button mQualitySuperBtn, mQualityHighBtn, mQualityNormalBtn, mQualityLowBtn;
    //视频比例选择按钮
    private Button mRecordRatio9P16Btn, mRecordRatio3P4Btn, mRecordRatio1P1Btn,mRecordRatioOriginalBtn;
    //视频分辨率选择按钮
    private Button mRecordResolutionP360Btn, mRecordResolutionP480Btn, mRecordResolutionP540Btn,
        mRecordResolutionP720Btn;
    //视频编码方式选择按钮
    private Button mEncorderHardwareBtn, mEncorderOpenh264Btn, mEncorderFfmpegBtn;
    /**
     * 帧率 default {@link #DEFAULT_FRAME_RATE}
     */
    private EditText mFrameRateEdit;

    /**
     * 关键帧间隔 default {@link #DEFAULT_GOP}
     */
    private EditText mGopEdit;

    /**
     * 码率 默认0由视频质量参数计算
     */
    private EditText mKBpsEdit;

    private Button mRadioFill;//填充
    private Button mRadioCrop;//裁剪
    /**
     * 片尾水印选择开关
     */
    private SwitchCompat mVideoTailSwitch;
    private Button mStartImport;


    /**
     *  判断是编辑模块进入还是通过社区模块的编辑功能进入
     *  svideo: 短视频
     *  community: 社区
     */
    private String entrance;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.aliyun_svideo_import_activity_crop_demo);
        entrance = getIntent().getStringExtra(INTENT_PARAM_KEY_ENTRANCE);
        initView();
    }

    private void initView(){
        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(this);
        mFrameRateEdit = (EditText) findViewById(R.id.frame_rate_edit);
        mGopEdit = (EditText) findViewById(R.id.gop_edit);
        mKBpsEdit = (EditText) findViewById(R.id.kbps_edit);
        //视频质量按钮初始化
        mQualitySuperBtn = (Button) findViewById(R.id.alivc_video_quality_super);
        mQualityHighBtn = (Button) findViewById(R.id.alivc_video_quality_high);
        mQualityNormalBtn = (Button) findViewById(R.id.alivc_video_quality_normal);
        mQualityLowBtn = (Button) findViewById(R.id.alivc_video_quality_low);
        mQualitySuperBtn.setOnClickListener(this);
        mQualityHighBtn.setOnClickListener(this);
        mQualityNormalBtn.setOnClickListener(this);
        mQualityLowBtn.setOnClickListener(this);

        mRecordRatio9P16Btn = (Button) findViewById(R.id.alivc_video_ratio_9_16);
        mRecordRatio3P4Btn = (Button) findViewById(R.id.alivc_video_ratio_3_4);
        mRecordRatio1P1Btn = (Button) findViewById(R.id.alivc_video_ratio_1_1);
        mRecordRatioOriginalBtn = (Button) findViewById(R.id.alivc_video_ratio_original);
        mRecordRatio9P16Btn.setOnClickListener(this);
        mRecordRatio3P4Btn.setOnClickListener(this);
        mRecordRatio1P1Btn.setOnClickListener(this);
        mRecordRatioOriginalBtn.setOnClickListener(this);


        mRecordResolutionP360Btn = (Button) findViewById(R.id.alivc_record_resolution_360p);
        mRecordResolutionP480Btn = (Button) findViewById(R.id.alivc_record_resolution_480p);
        mRecordResolutionP540Btn = (Button) findViewById(R.id.alivc_record_resolution_540p);
        mRecordResolutionP720Btn = (Button) findViewById(R.id.alivc_record_resolution_720p);
        mRecordResolutionP360Btn.setOnClickListener(this);
        mRecordResolutionP480Btn.setOnClickListener(this);
        mRecordResolutionP540Btn.setOnClickListener(this);
        mRecordResolutionP720Btn.setOnClickListener(this);

        //视频编码相关按钮
        mEncorderHardwareBtn = findViewById(R.id.alivc_edit_encoder_hardware);
        mEncorderOpenh264Btn = findViewById(R.id.alivc_edit_encoder_openh264);
        mEncorderFfmpegBtn = findViewById(R.id.alivc_edit_encoder_ffmpeg);
        mEncorderHardwareBtn.setOnClickListener(this);
        mEncorderOpenh264Btn.setOnClickListener(this);
        mEncorderFfmpegBtn.setOnClickListener(this);

        mRadioFill = (Button) findViewById(R.id.radio_fill);
        mRadioCrop = (Button) findViewById(R.id.radio_crop);
        mRadioFill.setOnClickListener(this);
        mRadioCrop.setOnClickListener(this);

        mVideoTailSwitch =findViewById(R.id.video_tail_switch);
        mVideoTailSwitch.setOnCheckedChangeListener(this);
        mVideoTailSwitch.setChecked(mHasTailAnimation);

        mStartImport = (Button) findViewById(R.id.start_import);
        mStartImport.setOnClickListener(this);

        //初始化配置
        onRatioSelected(mRecordRatio9P16Btn);
        onResolutionSelected(mRecordResolutionP720Btn);
        onQualitySelected(mQualityHighBtn);
        onScaleModeSelected(mRadioFill);
        onEncoderSelected(mEncorderHardwareBtn);

    }



    @Override
    public void onClick(View v) {
        if(v == mStartImport){
            if (FastClickUtil.isFastClick()){
                return;
            }
            //判断是否有权限，如果没有则不会打开MediaActivity
            boolean externalStorage = PermissionUtils.checkPermissionsGroup(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
            if(!externalStorage){
                ToastUtils.show(this, com.aliyun.demo.crop.R.string.no_read_external_storage_permission);
                return ;
            }

            String inputFrame = mFrameRateEdit.getText().toString();
            int frameRate = DEFAULT_FRAME_RATE;
            if(!TextUtils.isEmpty(inputFrame)){
                try {
                    frameRate = Integer.parseInt(inputFrame);
                } catch (Exception e) {
                    Log.e("ERROR", "input error");
                }
            }

            String inputGop = mGopEdit.getText().toString();
            int gop = DEFAULT_GOP;
            if(!TextUtils.isEmpty(inputGop)){
                try {
                    gop = Integer.parseInt(inputGop);
                } catch (Exception e) {
                    Log.e("ERROR", "input error");
                }
            }

            String inputKBps = mKBpsEdit.getText().toString();
            int bitrate = 0;
            if(!TextUtils.isEmpty(inputKBps)){
                try {
                    bitrate = Integer.parseInt(inputKBps);
                } catch (Exception e) {
                    Log.e("ERROR", "input error");
                }
            }

            AlivcSvideoEditParam param = new AlivcSvideoEditParam.Build()
                .setRatio(mRatio)//视频比例
                .setCropMode(scaleMode)//裁剪模式
                .setVideoQuality(videoQuality)//视频质量
                .setResolutionMode(mResolutionMode)//裁剪分辨率
                .setHasTailAnimation(mHasTailAnimation)//是否添加片尾水印
                .setFrameRate(frameRate)//裁剪帧率
                .setGop(gop)//关键帧间隔
                .setBitrate(bitrate)//码率
                .setEntrance(entrance)//判断是编辑模块进入还是通过社区模块的编辑功能进入
                .setIsOpenCrop(true)//相册页面是否打开裁剪的入口
                .setVideoCodec(mVideoCodec)
                .build();
            MediaActivity.startImport(this, param);
            //AlicvEditorRoute.startMediaActivity(this,param);

        }else if(v == back){
            finish();
        } else if (v == mRadioCrop || v == mRadioFill ) {
            onScaleModeSelected(v);
        } else if (v == mQualityHighBtn || v == mQualityLowBtn || v == mQualitySuperBtn || v == mQualityNormalBtn) {
            onQualitySelected(v);
        } else if (v == mRecordRatio1P1Btn || v == mRecordRatio3P4Btn || v == mRecordRatio9P16Btn||v==mRecordRatioOriginalBtn) {
            onRatioSelected(v);
        } else if (v == mRecordResolutionP360Btn || v == mRecordResolutionP480Btn || mRecordResolutionP540Btn == v
            || v == mRecordResolutionP720Btn) {
            onResolutionSelected(v);
        } else if (v == mEncorderFfmpegBtn || v == mEncorderHardwareBtn || v == mEncorderOpenh264Btn) {
            onEncoderSelected(v);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mVideoTailSwitch){
            mHasTailAnimation = isChecked;
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
        mQualityNormalBtn.setSelected(false);
        mQualityLowBtn.setSelected(false);
        if (view == mQualitySuperBtn) {
            videoQuality = VideoQuality.SSD;
            mQualitySuperBtn.setSelected(true);
        } else if (view == mQualityHighBtn) {
            mQualityHighBtn.setSelected(true);
            videoQuality = VideoQuality.HD;
        } else if (view == mQualityNormalBtn) {
            mQualityNormalBtn.setSelected(true);
            videoQuality = VideoQuality.SD;
        } else {
            mQualityLowBtn.setSelected(true);
            videoQuality = VideoQuality.LD;
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
        mRecordRatioOriginalBtn.setSelected(false);

        view.setSelected(true);
        if (view == mRecordRatio1P1Btn) {
            mRatio = AlivcSvideoEditParam.RATIO_MODE_1_1;
        } else if (view == mRecordRatio9P16Btn) {

            mRatio = AlivcSvideoEditParam.RATIO_MODE_9_16;


        } else if(view==mRecordRatio3P4Btn){
            mRatio = AlivcSvideoEditParam.RATIO_MODE_3_4;
        }else if (view==mRecordRatioOriginalBtn){
            mRatio = AlivcSvideoEditParam.RATIO_MODE_ORIGINAL;
        }else {
            mRatio = AlivcSvideoEditParam.RATIO_MODE_9_16;
        }
    }

    /**
     * 选择视频分辨率
     * @param view 选择的分辨率button
     */
    private void onResolutionSelected(View view) {
        mRecordResolutionP360Btn.setSelected(false);
        mRecordResolutionP480Btn.setSelected(false);
        mRecordResolutionP540Btn.setSelected(false);
        mRecordResolutionP720Btn.setSelected(false);
        view.setSelected(true);
        if (view == mRecordResolutionP360Btn) {


            mResolutionMode = AlivcSvideoEditParam.RESOLUTION_360P;
        } else if (view == mRecordResolutionP480Btn) {

            mResolutionMode = AlivcSvideoEditParam.RESOLUTION_480P;

        } else if (view == mRecordResolutionP540Btn) {
            mResolutionMode = AlivcSvideoEditParam.RESOLUTION_540P;


        } else {

            mResolutionMode = AlivcSvideoEditParam.RESOLUTION_720P;

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
     * 填充模式选择
     * @param view 选择的button
     */
    private void onScaleModeSelected(View view){
        mRadioFill.setSelected(false);
        mRadioCrop.setSelected(false);
        view.setSelected(true);
        if (view==mRadioFill){
            scaleMode = VideoDisplayMode.FILL;
        }else {
            scaleMode = VideoDisplayMode.SCALE;
        }
    }
}
