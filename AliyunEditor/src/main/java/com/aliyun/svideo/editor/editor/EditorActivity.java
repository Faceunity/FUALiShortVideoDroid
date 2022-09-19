/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.editor;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.fragment.app.FragmentActivity;

import com.aliyun.common.utils.StringUtils;
import com.aliyun.svideo.music.utils.NotchScreenUtil;
import com.aliyun.svideosdk.editor.draft.AliyunDraft;
import com.aliyun.svideosdk.importer.AliyunIImport;
import com.aliyun.svideosdk.importer.impl.AliyunImportCreator;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.bean.AlivcEditInputParam;
import com.aliyun.svideo.editor.bean.AlivcEditOutputParam;
import com.aliyun.svideo.editor.publish.PublishActivity;
import com.aliyun.svideo.editor.view.AlivcEditView;
import com.aliyun.svideo.media.MediaInfo;
import com.aliyun.svideosdk.common.struct.common.AliyunImageClip;
import com.aliyun.svideosdk.common.struct.common.AliyunVideoClip;
import com.aliyun.svideosdk.common.struct.common.AliyunVideoParam;
import com.aliyun.svideosdk.common.struct.common.VideoDisplayMode;
import com.aliyun.svideosdk.common.struct.common.VideoQuality;
import com.aliyun.svideosdk.common.struct.encoder.VideoCodecs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 视频编辑界面, 主要负责显示AlivcEditView
 */
public class EditorActivity extends FragmentActivity {
    private static final String TAG = "EditorActivity";
    public static final String NEXT_ACTIVITY_CLASS_NAME = "com.aliyun.svideo.editor.publish.PublishActivity";
    private AlivcEditView editView;
    /**
     * 编辑输入参数
     */
    private AlivcEditInputParam mInputParam;
    private AliyunVideoParam mVideoParam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 检测是否是全面屏手机, 如果不是, 设置FullScreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (!NotchScreenUtil.checkNotchScreen(this)) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                 WindowManager.LayoutParams.FLAG_FULLSCREEN);

        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.alivc_editor_acitvity_edit);
        initData();
        editView = findViewById(R.id.alivc_edit_view);
        boolean hasTailAnimation = mInputParam.isHasTailAnimation();
        boolean isReplaceMusic = mInputParam.isCanReplaceMusic();
        boolean isMixRecorder = mInputParam.isMixRecorder();
        boolean isDeNoise = mInputParam.getDeNoise();
        String draftPath = getIntent().getStringExtra(AlivcEditInputParam.INTENT_KEY_DRAFT_PATH);
        Uri uri;
        if (!StringUtils.isEmpty(draftPath)) {
            uri = Uri.parse(draftPath);
        } else {
            List<MediaInfo> mediaInfos = mInputParam.getMediaInfos();
            uri = Uri.fromFile(new File(getProjectJsonPath(mediaInfos)));
        }
        editView.setHasDeNoise(isDeNoise);
        editView.setParam(mVideoParam, uri, hasTailAnimation, mInputParam.isHasWaterMark(), mInputParam.isHorizontalFlip());
        editView.setReplaceMusic(isReplaceMusic);
        editView.setHasRecordMusic(isReplaceMusic);
        editView.setIsMixRecord(isMixRecorder);
        editView.setmOnFinishListener(new AlivcEditView.OnFinishListener() {
            @Override
            public void onComplete(AlivcEditOutputParam outputParam) {
                //编辑完成跳转到其他界面
                Intent intent = new Intent();
                intent.setClassName(EditorActivity.this, NEXT_ACTIVITY_CLASS_NAME);
                intent.putExtra(PublishActivity.KEY_PARAM_THUMBNAIL, outputParam.getThumbnailPath());
                intent.putExtra(PublishActivity.KEY_PARAM_CONFIG, outputParam.getConfigPath());
                intent.putExtra(PublishActivity.KEY_PARAM_VIDEO_WIDTH, outputParam.getOutputVideoWidth());
                intent.putExtra(PublishActivity.KEY_PARAM_VIDEO_HEIGHT, outputParam.getOutputVideoHeight());
                //传入视频比列
                intent.putExtra(PublishActivity.KEY_PARAM_VIDEO_RATIO, outputParam.getVideoRatio());
                intent.putExtra("videoParam", outputParam.getVideoParam());
                startActivityForResult(intent, PublishActivity.REQUEST_CODE);
            }
        });
    }

    private void initData() {
        Intent intent = getIntent();
        int mFrameRate = intent.getIntExtra(AlivcEditInputParam.INTENT_KEY_FRAME, 30);
        int mGop = intent.getIntExtra(AlivcEditInputParam.INTENT_KEY_GOP, 250);
        int mRatio = intent.getIntExtra(AlivcEditInputParam.INTENT_KEY_RATION_MODE, AlivcEditInputParam.RATIO_MODE_9_16);
        VideoQuality mVideoQuality = (VideoQuality) intent.getSerializableExtra(AlivcEditInputParam.INTENT_KEY_QUALITY);
        if (mVideoQuality == null) {
            mVideoQuality = VideoQuality.HD;
        }
        int mResolutionMode = intent.getIntExtra(AlivcEditInputParam.INTENT_KEY_RESOLUTION_MODE, AlivcEditInputParam.RESOLUTION_720P);
        VideoCodecs mVideoCodec = (VideoCodecs) intent.getSerializableExtra(AlivcEditInputParam.INTENT_KEY_CODEC);
        if (mVideoCodec == null) {
            mVideoCodec = VideoCodecs.H264_HARDWARE;
        }
        int mCrf = intent.getIntExtra(AlivcEditInputParam.INTETN_KEY_CRF, 23);
        float mScaleRate = intent.getFloatExtra(AlivcEditInputParam.INTETN_KEY_SCANLE_RATE, 1.0f);
        VideoDisplayMode mScaleMode = (VideoDisplayMode) intent.getSerializableExtra(AlivcEditInputParam.INTETN_KEY_SCANLE_MODE);
        if (mScaleMode == null) {
            mScaleMode = VideoDisplayMode.FILL;
        }
        boolean mHasTailAnimation = intent.getBooleanExtra(AlivcEditInputParam.INTENT_KEY_TAIL_ANIMATION, false);
        boolean hasDeNoise = intent.getBooleanExtra(AlivcEditInputParam.INTENT_KEY_DE_NOISE, false);
        boolean canReplaceMusic = intent.getBooleanExtra(AlivcEditInputParam.INTENT_KEY_REPLACE_MUSIC, true);
        ArrayList<MediaInfo> mediaInfos = intent.getParcelableArrayListExtra(AlivcEditInputParam.INTENT_KEY_MEDIA_INFO);
        boolean hasWaterMark = intent.getBooleanExtra(AlivcEditInputParam.INTENT_KEY_WATER_MARK, false);
        boolean horizontalFlip = intent.getBooleanExtra(AlivcEditInputParam.INTENT_KEY_HORIZONTAL_FLIP, false);
        boolean isMixRecord = intent.getBooleanExtra(AlivcEditInputParam.INTENT_KEY_IS_MIX, false);
        AlivcEditInputParam.Builder builder = new AlivcEditInputParam.Builder();
        if (mediaInfos != null) {
            builder.addMediaInfos(mediaInfos);
        }
        mInputParam = builder
        .setFrameRate(mFrameRate)
        .setGop(mGop)
        .setRatio(mRatio)
        .setVideoQuality(mVideoQuality)
        .setResolutionMode(mResolutionMode)
        .setVideoCodec(mVideoCodec)
        .setCrf(mCrf)
        .setScaleRate(mScaleRate)
        .setScaleMode(mScaleMode)
        .setHasTailAnimation(mHasTailAnimation)
        .setCanReplaceMusic(canReplaceMusic)
        .setHasWaterMark(hasWaterMark)
        .setIsMixRecorder(isMixRecord)
        .setDeNoise(hasDeNoise)
        .setHorizontalFlip(horizontalFlip)
        .build();
        if (isMixRecord) {
            mVideoParam = mInputParam.generateMixVideoParam();
        } else {
            mVideoParam = mInputParam.generateVideoParam();
            //草稿跳转的直接用草稿那边传过来的
            int outputWidth = intent.getIntExtra(AlivcEditInputParam.INTENT_KEY_OUTPUT_WIDTH, -1);
            int outputHeight = intent.getIntExtra(AlivcEditInputParam.INTENT_KEY_OUTPUT_HEIGHT, -1);
            if (outputWidth != -1 && outputHeight != -1) {
                mVideoParam.setOutputWidth(outputWidth);
                mVideoParam.setOutputHeight(outputHeight);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (editView != null) {
            editView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (editView != null) {
            editView.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (editView != null) {
            editView.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (editView != null) {
            editView.onDestroy();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (editView != null) {
            editView.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (editView != null) {
            editView.onStart();
        }
    }

    @Override
    public void onBackPressed() {
        boolean isConsume = false;
        if (editView != null) {
            isConsume = editView.onBackPressed();
        }
        if (!isConsume) {
            super.onBackPressed();
            finish();
        }
    }

    /**
     * 通过MediaInfo生成ProjectJson
     *
     * @param mediaInfos List<MediaInfo>
     * @return jsonPath
     */
    private String getProjectJsonPath(List<MediaInfo> mediaInfos) {

        AliyunIImport mImport = AliyunImportCreator.getImportInstance(this);
        mImport.setVideoParam(mVideoParam);
        int size = mediaInfos.size();
        for (int i = 0; i < size; i++) {
            MediaInfo mediaInfo = mediaInfos.get(i);
            if (mediaInfo.mimeType.startsWith("video")) {
                mImport.addMediaClip(new AliyunVideoClip.Builder()
                                     .source(mediaInfo.filePath)
                                     .startTime(mediaInfo.startTime)
                                     .endTime(mediaInfo.startTime + mediaInfo.duration)
                                     .duration(mediaInfo.duration)
                                     .build());
            } else if (mediaInfo.mimeType.startsWith("image")) {
                mImport.addMediaClip(new AliyunImageClip.Builder()
                                     .source(mediaInfo.filePath)
                                     .duration(mediaInfo.duration)
                                     .build());
            }
        }
        String projectConfigure = mImport.generateProjectConfigure();
        mImport.release();

        return projectConfigure;
    }

    public static void startEdit(Context context, AlivcEditInputParam param) {
        startEdit(context, param, null, -1, -1);
    }

    public static void startEdit(Context context, AlivcEditInputParam param, String draftPath, int outputWidth, int outputHeight) {
        if (param == null || ((param.getMediaInfos() == null || param.getMediaInfos().size() == 0) && StringUtils.isEmpty(draftPath))) {
            return;
        }
        Intent intent = new Intent(context, EditorActivity.class);
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_FRAME, param.getFrameRate());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_GOP, param.getGop());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_RATION_MODE, param.getRatio());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_QUALITY, param.getVideoQuality());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_RESOLUTION_MODE, param.getResolutionMode());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_CODEC, param.getVideoCodec());
        intent.putExtra(AlivcEditInputParam.INTETN_KEY_CRF, param.getCrf());
        intent.putExtra(AlivcEditInputParam.INTETN_KEY_SCANLE_RATE, param.getScaleRate());
        intent.putExtra(AlivcEditInputParam.INTETN_KEY_SCANLE_MODE, param.getScaleMode());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_TAIL_ANIMATION, param.isHasTailAnimation());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_DE_NOISE, param.getDeNoise());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_REPLACE_MUSIC, param.isCanReplaceMusic());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_WATER_MARK, param.isHasWaterMark());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_HORIZONTAL_FLIP, param.isHorizontalFlip());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_IS_MIX, param.isMixRecorder());
        if (draftPath != null) {
            intent.putExtra(AlivcEditInputParam.INTENT_KEY_DRAFT_PATH, draftPath);
            intent.putExtra(AlivcEditInputParam.INTENT_KEY_OUTPUT_WIDTH, outputWidth);
            intent.putExtra(AlivcEditInputParam.INTENT_KEY_OUTPUT_HEIGHT, outputHeight);
        } else {
            intent.putParcelableArrayListExtra(AlivcEditInputParam.INTENT_KEY_MEDIA_INFO, param.getMediaInfos());
        }
        context.startActivity(intent);
    }

    /**
     * 使用草稿打开编辑
     * @param context 上下文
     * @param draft 草稿
     */
    public static void startEdit(Context context, AliyunDraft draft) {
        AlivcEditInputParam param = new AlivcEditInputParam.Builder()
                                           .setScaleMode(VideoDisplayMode.valueOf(draft.getConfig().getDisplayMode()))
                                           .setVideoQuality(VideoQuality.values()[draft.getConfig().getVideoQuality()])
                                           .setFrameRate(draft.getConfig().getFps())
                                           .setGop(draft.getConfig().getGop())
                                           .setCrf(draft.getConfig().getCrf())
                                           .setVideoCodec(VideoCodecs.getInstanceByValue(draft.getConfig().getVideoCodec()))
                                           .setScaleRate(draft.getConfig().getScale())
                                           .build();
        startEdit(context, param, draft.getEditorProjectUri(), draft.getConfig().getOutputWidth(), draft.getConfig().getOutputHeight());
    }

}
