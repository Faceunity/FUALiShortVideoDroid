package com.aliyun.svideo.recorder.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import com.aliyun.common.utils.ToastUtil;
import com.aliyun.svideo.base.widget.ProgressDialog;
import com.aliyun.svideo.common.utils.FastClickUtil;
import com.aliyun.svideo.common.utils.ToastUtils;
import com.aliyun.svideo.media.MediaInfo;
import com.aliyun.svideo.media.MediaStorage;
import com.aliyun.svideo.media.MutiMediaView;
import com.aliyun.svideo.record.R;
import com.aliyun.svideo.recorder.bean.AlivcMixBorderParam;
import com.aliyun.svideo.recorder.bean.AlivcRecordInputParam;
import com.aliyun.svideo.recorder.bean.RenderingMode;
import com.aliyun.svideo.recorder.bean.VideoDisplayParam;
import com.aliyun.svideo.recorder.util.MixVideoTranscoder;
import com.aliyun.svideosdk.common.AliyunErrorCode;
import com.aliyun.svideosdk.common.struct.common.AliyunSnapVideoParam;
import com.aliyun.svideosdk.common.struct.common.VideoQuality;
import com.aliyun.svideosdk.common.struct.encoder.VideoCodecs;
import com.aliyun.svideosdk.mixrecorder.MixAudioSourceType;
import com.duanqu.transcode.NativeParser;

import java.lang.ref.WeakReference;

import static com.aliyun.svideo.recorder.util.MixVideoTranscoder.HEIGHT;
import static com.aliyun.svideo.recorder.util.MixVideoTranscoder.WIDTH;

/**
 * 合拍选择视频界面，目前只能选择30秒以下的视频
 */
public class AlivcMixMediaActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    /**
     * 目前支持 2秒-30秒之内的视频
     */
    private static final int MIN_VIDEO_DURATION = 2000;
    private static final int MAX_VIDEO_DURATION = 30 * 1000;
    private ProgressDialog mProgressDialog;
    private AlivcRecordInputParam mAliyunVideoParam;

    private MixVideoTranscoder mMixVideoTranscoder;
    private MutiMediaView mMutiMediaView;

    private RenderingMode renderingMode;
    private boolean isSvideoRace = false;
    private MixAudioSourceType mMixAudioSourceType;
    /**
     * 设置合成窗口非填充模式下的背景颜色
     */
    private int mBackgroundColor;
    /**
     * 设置合成窗口非填充模式下的背景图片路径
     */
    private String mBackgroundImagePath;
    /**
     * displayMode 0：裁切 1：填充 2：拉伸
     */
    private int mBackgroundImageDisplayMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.alivc_recorder_activity_mix_media);

        getData();
        initView();
        initMixVideoTranscoder();
    }

    private void initMixVideoTranscoder() {
        mMixVideoTranscoder = new MixVideoTranscoder();
        mMixVideoTranscoder.init(this);
        mMixVideoTranscoder.setTranscodeListener(new MixVideoTranscoder.TranscoderListener() {
            @Override
            public void onError(Throwable e, final int errorCode) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                        }
                        switch (errorCode) {
                            case AliyunErrorCode.ALIVC_SVIDEO_ERROR_MEDIA_NOT_SUPPORTED_AUDIO:
                                ToastUtil.showToast(AlivcMixMediaActivity.this,
                                        R.string.alivc_record_not_supported_audio);
                                break;
                            case AliyunErrorCode.ALIVC_SVIDEO_ERROR_MEDIA_NOT_SUPPORTED_VIDEO:
                                ToastUtil.showToast(AlivcMixMediaActivity.this,
                                        R.string.alivc_record_video_crop_failed);
                                break;
                            case AliyunErrorCode.ALIVC_COMMON_UNKNOWN_ERROR_CODE:
                            default:
                                ToastUtil.showToast(AlivcMixMediaActivity.this,
                                        R.string.alivc_record_video_crop_failed);
                        }
                    }
                });

            }

            @Override
            public void onProgress(int progress) {
                if (mProgressDialog != null) {
                    mProgressDialog.setProgress(progress);
                }
            }

            @Override
            public void onComplete(MediaInfo resultVideos) {
                Log.d(TAG, "ONCOMPLETED, dialog : " + (mProgressDialog == null));
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
                mAliyunVideoParam.setMixVideoFilePath(resultVideos.filePath);
                //跳转AlivcSvideoRecordActivity
                AlivcSvideoMixRecordActivity.startMixRecord(AlivcMixMediaActivity.this,
                        mAliyunVideoParam, renderingMode, isSvideoRace);

            }

            @Override
            public void onCancelComplete() {
                //取消完成
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMutiMediaView.setNextEnable(true);
                    }
                });
            }
        });
    }

    private void getData() {
        //录制参数
        int mResolutionMode = getIntent().getIntExtra(
                AlivcRecordInputParam.INTENT_KEY_RESOLUTION_MODE, AliyunSnapVideoParam.RESOLUTION_540P);
        int mRatioMode = getIntent().getIntExtra(AlivcRecordInputParam.INTENT_KEY_RATION_MODE,
                AliyunSnapVideoParam.RATIO_MODE_3_4);
        int mGop = getIntent().getIntExtra(AlivcRecordInputParam.INTENT_KEY_GOP, 250);
        VideoQuality mVideoQuality = (VideoQuality)getIntent().getSerializableExtra(
                AlivcRecordInputParam.INTENT_KEY_QUALITY);
        if (mVideoQuality == null) {
            mVideoQuality = VideoQuality.HD;
        }
        VideoCodecs mVideoCodec = (VideoCodecs)getIntent().getSerializableExtra(
                AlivcRecordInputParam.INTENT_KEY_CODEC);
        if (mVideoCodec == null) {
            mVideoCodec = VideoCodecs.H264_HARDWARE;
        }
        renderingMode = (RenderingMode)getIntent().getSerializableExtra(
                AlivcRecordInputParam.INTENT_KEY_VIDEO_RENDERING_MODE);
        if (renderingMode == null) {
            renderingMode = RenderingMode.FaceUnity;
        }
        isSvideoRace = getIntent().getBooleanExtra(AlivcRecordInputParam.INTENT_KEY_IS_SVIDEO_RACE,
                false);
        //帧率裁剪参数,默认30
        int frame = getIntent().getIntExtra(AlivcRecordInputParam.INTENT_KEY_FRAME, 30);
        mMixAudioSourceType = (MixAudioSourceType)getIntent().getSerializableExtra(
                AlivcRecordInputParam.INTENT_KEY_MIX_AUDIO_SOURCE_TYPE);
        if (mMixAudioSourceType == null) {
            mMixAudioSourceType = MixAudioSourceType.Original;
        }
        mBackgroundColor = getIntent().getIntExtra(
                AlivcRecordInputParam.INTENT_KEY_MIX_BACKGROUND_COLOR, Color.BLACK);
        mBackgroundImagePath = getIntent().getStringExtra(
                AlivcRecordInputParam.INTENT_KEY_MIX_BACKGROUND_IMAGE_PATH);
        mBackgroundImageDisplayMode = getIntent().getIntExtra(
                AlivcRecordInputParam.INTENT_KEY_MIX_BACKGROUND_IMAGE_MODE, 0);
        VideoDisplayParam mPlayDisplayParam = (VideoDisplayParam)getIntent().getSerializableExtra(
                AlivcRecordInputParam.INTENT_KEY_DISPLAY_PARAM_PLAY);
        VideoDisplayParam mRecordDisplayParam = (VideoDisplayParam)getIntent().getSerializableExtra(
                AlivcRecordInputParam.INTENT_KEY_DISPLAY_PARAM_RECORD);
        AlivcMixBorderParam borderParam = (AlivcMixBorderParam)getIntent().getSerializableExtra(AlivcRecordInputParam.INTENT_KEY_MIX_BORDER_PARAM_RECORD);

        mAliyunVideoParam = new AlivcRecordInputParam.Builder()
                .setResolutionMode(mResolutionMode)
                .setRatioMode(mRatioMode)
                .setGop(mGop)
                .setVideoCodec(mVideoCodec)
                .setFrame(frame)
                .setVideoQuality(mVideoQuality)
                .setMixAudioSourceType(mMixAudioSourceType)
                .setMixBackgroundColor(mBackgroundColor)
                .setMixBackgroundImagePath(mBackgroundImagePath)
                .setMixBackgroundImageMode(mBackgroundImageDisplayMode)
                .setPlayDisplayParam(mPlayDisplayParam)
                .setRecordDisplayParam(mRecordDisplayParam)
                .setMixVideoBorderParam(borderParam)
                .build();
    }

    private void initView() {

        mMutiMediaView = findViewById(R.id.mix_mediaview);
        mMutiMediaView.setMediaSortMode(MediaStorage.SORT_MODE_VIDEO);//只显示视频
        mMutiMediaView.setVideoDurationRange(MIN_VIDEO_DURATION,
                MAX_VIDEO_DURATION);//设置显示的最小时长和最大时长
        mMutiMediaView.enableSelectView(MAX_VIDEO_DURATION);
        mMutiMediaView.setOnMediaClickListener(new MutiMediaView.OnMediaClickListener() {
            @Override
            public void onClick(MediaInfo info) {
                Log.i(TAG, "log_editor_video_path : " + info.filePath);
                MediaInfo infoCopy = new MediaInfo();
                infoCopy.addTime = info.addTime;
                infoCopy.mimeType = info.mimeType;
                if (info.mimeType.startsWith("image")) {
                    //合拍只显示视频
                    return;
                } else {
                    infoCopy.duration = info.duration;
                }
                infoCopy.filePath = info.filePath;
                infoCopy.id = info.id;
                infoCopy.isSquare = info.isSquare;
                infoCopy.thumbnailPath = info.thumbnailPath;
                infoCopy.title = info.title;
                infoCopy.type = info.type;
                //mMediaInfoList.add(infoCopy);
                /*目前合拍是单选*/
                mMutiMediaView.addOnlyFirstMedia(infoCopy);
                mMixVideoTranscoder.addMedia(infoCopy);
            }
        });

        mMutiMediaView.setOnActionListener(new MutiMediaView.OnActionListener() {
            @Override
            public void onNext(boolean isReachedMaxDuration) {
                if (FastClickUtil.isFastClickActivity(
                        AlivcMixMediaActivity.class.getSimpleName())) {
                    return;
                }
                MediaInfo mediaInfo = mMutiMediaView.getOnlyOneMedia();

                if (null == mediaInfo) {
                    ToastUtils.show(AlivcMixMediaActivity.this,
                            getResources().getString(R.string.alivc_media_please_select_video));
                    return;
                }

                if (isMixTranscoder(mediaInfo)) {
                    if (mProgressDialog == null || !mProgressDialog.isShowing()) {
                        mProgressDialog = ProgressDialog.show(AlivcMixMediaActivity.this, null,
                                getResources().getString(R.string.alivc_media_wait));
                        mProgressDialog.setCancelable(true);
                        mProgressDialog.setCanceledOnTouchOutside(false);
                        mProgressDialog.setOnCancelListener(
                                new OnCancelListener(AlivcMixMediaActivity.this));
                        mMixVideoTranscoder.start();
                    }
                } else {
                    mAliyunVideoParam.setMixVideoFilePath(mediaInfo.filePath);
                    AlivcSvideoMixRecordActivity.startMixRecord(AlivcMixMediaActivity.this,
                            mAliyunVideoParam, renderingMode, isSvideoRace);

                }
            }

            @Override
            public void onBack() {
                finish();
            }
        });

        mMutiMediaView.loadMedia();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMutiMediaView.onDestroy();
        mMixVideoTranscoder.release();
    }

    /**
     * 开启录制
     *
     * @param context          上下文
     * @param recordInputParam 录制输入参数
     */
    public static void startRecord(Context context, AlivcRecordInputParam recordInputParam) {
        Intent intent = new Intent(context, AlivcMixMediaActivity.class);
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_RESOLUTION_MODE,
                recordInputParam.getResolutionMode());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_MAX_DURATION,
                recordInputParam.getMaxDuration());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_MIN_DURATION,
                recordInputParam.getMinDuration());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_RATION_MODE,
                recordInputParam.getRatioMode());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_GOP, recordInputParam.getGop());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_FRAME, recordInputParam.getFrame());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_QUALITY,
                recordInputParam.getVideoQuality());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_CODEC, recordInputParam.getVideoCodec());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_VIDEO_OUTPUT_PATH,
                recordInputParam.getVideoOutputPath());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_VIDEO_RENDERING_MODE,
                recordInputParam.getmRenderingMode());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_IS_SVIDEO_RACE,
                recordInputParam.isSvideoRace());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_MIX_AUDIO_SOURCE_TYPE,
                recordInputParam.getMixAudioSourceType());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_MIX_BACKGROUND_COLOR,
                recordInputParam.getMixBackgroundColor());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_MIX_BACKGROUND_IMAGE_PATH,
                recordInputParam.getMixBackgroundImagePath());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_MIX_BACKGROUND_IMAGE_MODE,
                recordInputParam.getMixBackgroundImageMode());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_DISPLAY_PARAM_PLAY,
                recordInputParam.getPlayDisplayParam());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_DISPLAY_PARAM_RECORD,
                recordInputParam.getRecordDisplayParam());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_MIX_BORDER_PARAM_RECORD,
                recordInputParam.getMixBorderParam());
        context.startActivity(intent);
    }

    /**
     * progressDialog cancel listener
     */
    private static class OnCancelListener implements DialogInterface.OnCancelListener {

        private WeakReference<AlivcMixMediaActivity> weakReference;

        public OnCancelListener(AlivcMixMediaActivity mediaActivity) {
            weakReference = new WeakReference<>(mediaActivity);
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            AlivcMixMediaActivity mediaActivity = weakReference.get();
            if (mediaActivity != null) {
                //为了防止未取消成功的情况下就开始下一次转码，这里在取消转码成功前会禁用下一步按钮
                mediaActivity.mMutiMediaView.setNextEnable(false);
                mediaActivity.mMixVideoTranscoder.cancel();
            }
        }
    }

    /**
     * 如果大于360p需要转码
     *
     * @param mediaInfo
     */
    private boolean isMixTranscoder(MediaInfo mediaInfo) {
        int frameWidth = 0;
        int frameHeight = 0;
        if (mediaInfo != null) {
            try {
                NativeParser nativeParser = new NativeParser();
                nativeParser.init(mediaInfo.filePath);
                try {
                    frameWidth = Integer.parseInt(nativeParser.getValue(NativeParser.VIDEO_WIDTH));
                    frameHeight = Integer.parseInt(
                            nativeParser.getValue(NativeParser.VIDEO_HEIGHT));
                } catch (Exception e) {
                    Log.e(TAG, "parse rotation failed");
                }
                nativeParser.release();
                nativeParser.dispose();
                return frameWidth * frameHeight > WIDTH * HEIGHT;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Log.w(TAG, getString(R.string.alivc_media_please_select_video));
        return false;
    }
}
