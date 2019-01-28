/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.importer;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
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

/**
 * 视频编辑模块, 参数设置界面
 */
public class ImportEditSettingActivity extends Activity implements View.OnClickListener,CompoundButton.OnCheckedChangeListener{


    /**
     *  判断是编辑模块进入还是通过社区模块的编辑功能进入
     */
    private static final String INTENT_PARAM_KEY_ENTRANCE = "entrance";
    /**
     *  intent 是否添加了片尾水印的key
     */
    private static final String INTENT_PARAM_KEY_HAS_TAIL_ANIMATION = "hasTailAnimation";

    private static final int DEFAULT_FRAME_RATE = 25;
    private static final int DEFAULT_GOP = 125;
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
        onQualitySelected(mQualitySuperBtn);
        onScaleModeSelected(mRadioFill);

    }



    @Override
    public void onClick(View v) {
        if(v == mStartImport){
            if (FastClickUtil.isFastClick()){
                return;
            }

            String inputFrame = mFrameRateEdit.getText().toString();
            int frameRate = DEFAULT_FRAME_RATE;
            if(!TextUtils.isEmpty(inputFrame)){
                frameRate = Integer.parseInt(inputFrame);
            }
            String inputGop = mGopEdit.getText().toString();
            int gop = DEFAULT_GOP;
            if(!TextUtils.isEmpty(inputGop)){
                gop = Integer.parseInt(inputGop);
            }
            int bitrate = 0;
            String inputKBps = mKBpsEdit.getText().toString();
            if(!TextUtils.isEmpty(inputKBps)){
                bitrate = Integer.parseInt(inputKBps);
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
