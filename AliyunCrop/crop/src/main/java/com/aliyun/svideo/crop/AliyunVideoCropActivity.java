/*
 * Copyright (C) 2010-2013 Alibaba Group Holding Limited
 */

package com.aliyun.svideo.crop;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.aliyun.common.global.Version;
import com.aliyun.common.utils.DensityUtil;
import com.aliyun.common.utils.FileUtils;
import com.aliyun.common.utils.ToastUtil;
import com.aliyun.svideosdk.crop.impl.AliyunCropCreator;
import com.aliyun.svideosdk.crop.CropParam;
import com.aliyun.svideosdk.crop.AliyunICrop;
import com.aliyun.svideosdk.crop.CropCallback;
import com.aliyun.svideo.base.utils.VideoInfoUtils;
import com.aliyun.svideo.common.utils.UriUtils;
import com.aliyun.svideo.crop.bean.AlivcCropInputParam;
import com.aliyun.svideo.crop.bean.AlivcCropOutputParam;

import com.aliyun.svideosdk.common.AliyunErrorCode;
import com.aliyun.svideo.base.AlivcSvideoEditParam;
import com.aliyun.svideo.base.Constants;
import com.aliyun.svideo.base.widget.FanProgressBar;
import com.aliyun.svideo.base.widget.HorizontalListView;
import com.aliyun.svideo.base.widget.SizeChangedNotifier;
import com.aliyun.svideo.base.widget.VideoSliceSeekBar;
import com.aliyun.svideo.base.widget.VideoTrimFrameLayout;
import com.aliyun.svideo.common.utils.DensityUtils;
import com.aliyun.svideo.common.utils.PermissionUtils;
import com.aliyun.svideo.common.utils.ThreadUtils;
import com.aliyun.svideo.common.utils.ToastUtils;
import com.aliyun.svideosdk.player.impl.AliyunSVideoPlayerCreator;
import com.aliyun.svideosdk.player.AliyunISVideoPlayer;
import com.aliyun.svideosdk.player.PlayerCallback;
import com.aliyun.svideosdk.common.struct.common.CropKey;
import com.aliyun.svideosdk.common.struct.common.VideoDisplayMode;
import com.aliyun.svideosdk.common.struct.common.VideoQuality;
import com.aliyun.svideosdk.common.struct.encoder.VideoCodecs;
import com.aliyun.svideosdk.common.struct.common.AliyunSnapVideoParam;
import com.aliyun.svideosdk.common.AliyunIThumbnailFetcher;
import com.aliyun.svideo.common.utils.DateTimeUtils;
import com.aliyun.svideosdk.common.impl.AliyunThumbnailFetcherFactory;
import com.aliyun.svideosdk.transcode.NativeParser;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (C) 2010-2013 Alibaba Group Holding Limited
 * <p>
 * Created by Administrator on 2017/1/16.
 */

public class AliyunVideoCropActivity extends Activity implements TextureView.SurfaceTextureListener, HorizontalListView.OnScrollCallBack, SizeChangedNotifier.Listener,
    MediaPlayer.OnVideoSizeChangedListener, VideoTrimFrameLayout.OnVideoScrollCallBack, View.OnClickListener, CropCallback, Handler.Callback {

    public static final VideoDisplayMode SCALE_CROP = VideoDisplayMode.SCALE;
    public static final VideoDisplayMode SCALE_FILL = VideoDisplayMode.FILL;
    public static final String TAG = AliyunVideoCropActivity.class.getSimpleName();
    public static final int REQUEST_CODE_EDITOR_VIDEO_CROP = 1;
    public static final int REQUEST_CODE_CROP_VIDEO_CROP = 2;

    private static final int PLAY_VIDEO = 1000;
    private static final int PAUSE_VIDEO = 1001;
    private static final int END_VIDEO = 1003;


    private int playState = END_VIDEO;

    private AliyunICrop crop;

    private HorizontalListView listView;
    private VideoTrimFrameLayout frame;
    private TextureView textureview;
    private Surface mSurface;

    /**
     * sdk提供的播放器，支持非关键帧的实时预览
     */
    private AliyunISVideoPlayer mPlayer;
    private ImageView cancelBtn, nextBtn, transFormBtn;
    private TextView dirationTxt;

    private VideoSliceSeekBar seekBar;

    private FanProgressBar mCropProgress;
    private FrameLayout mCropProgressBg;

    private String mInputVideoPath;
    private String outputPath;
    private long duration;
    private int resolutionMode;
    private int ratioMode;
    private VideoQuality quality = VideoQuality.HD;
    private VideoCodecs mVideoCodec = VideoCodecs.H264_HARDWARE;
    private int frameRate;
    private int gop;

    private int screenWidth;
    private int frameWidth;
    private int frameHeight;
    private int mScrollX;
    private int mScrollY;
    private int videoWidth;
    private int videoHeight;
    private int cropDuration = 2000;

    private VideoDisplayMode cropMode = VideoDisplayMode.SCALE;
    private AliyunIThumbnailFetcher mThumbnailFetcher;


    private long mStartTime;
    private long mEndTime;

    private final static int MAX_DURATION = Integer.MAX_VALUE;
    private VideoTrimAdapter adapter;

    private Handler playHandler = new Handler(this);

    private boolean isPause = false;
    private boolean isCropping = false;
    /**
     * 每次修改裁剪结束位置时需要重新播放视频
     */
    private boolean needPlayStart = false;
    private boolean isUseGPU = false;

    private int mAction = CropKey.ACTION_TRANSCODE;

    /**
     * 原比例
     */
    public static final int RATIO_ORIGINAL = 3;
    private VideoDisplayMode mOriginalMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.alivc_crop_activity_video_crop);
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        crop = AliyunCropCreator.createCropInstance(this);
        crop.setCropCallback(this);
        getData();
        initView();
        initSurface();
    }

    private void getData() {

        Intent intent = getIntent();
        mAction = intent.getIntExtra(AlivcCropInputParam.INTENT_KEY_ACTION, CropKey.ACTION_TRANSCODE);
        mInputVideoPath = intent.getStringExtra(AlivcCropInputParam.INTENT_KEY_PATH);
        try {
            duration = crop.getVideoDuration(mInputVideoPath) / 1000;
        } catch (Exception e) {
            ToastUtil.showToast(this, R.string.alivc_crop_video_tip_crop_failed);
        }//获取精确的视频时间
        resolutionMode = intent.getIntExtra(AlivcCropInputParam.INTENT_KEY_RESOLUTION_MODE, AlivcCropInputParam.RESOLUTION_720P);
        cropMode = (VideoDisplayMode)intent.getSerializableExtra(AlivcCropInputParam.INTENT_KEY_CROP_MODE);
        if (cropMode == null) {
            cropMode = VideoDisplayMode.SCALE;
        }
        mOriginalMode = cropMode;
        quality =  (VideoQuality)intent.getSerializableExtra(AlivcCropInputParam.INTENT_KEY_QUALITY);
        if (quality == null) {
            quality = VideoQuality.HD;
        }
        gop = intent.getIntExtra(AlivcCropInputParam.INTENT_KEY_GOP, 250 );
        frameRate = intent.getIntExtra(AlivcCropInputParam.INTENT_KEY_FRAME_RATE, 30);
        ratioMode = intent.getIntExtra(AlivcCropInputParam.INTENT_KEY_RATIO_MODE, AlivcCropInputParam.RATIO_MODE_9_16);
        cropDuration = intent.getIntExtra(AlivcCropInputParam.INTENT_KEY_MIN_DURATION, 2000 );
        isUseGPU = intent.getBooleanExtra(AlivcCropInputParam.INTENT_KEY_USE_GPU, false );
        mVideoCodec = (VideoCodecs)intent.getSerializableExtra(AlivcCropInputParam.INTENT_KEY_CODECS);
        if (mVideoCodec == null) {
            mVideoCodec = VideoCodecs.H264_HARDWARE;
        }
    }


    public static String getVersion() {
        return Version.VERSION;
    }

    private void initView() {
        int mOutStrokeWidth = DensityUtil.dip2px(this, 5);
        seekBar = (VideoSliceSeekBar) findViewById(R.id.aliyun_seek_bar);
        seekBar.setSeekBarChangeListener(mSeekBarListener);
        int minDiff = (int) (cropDuration / (float) duration * 100) + 1;
        seekBar.setProgressMinDiff(minDiff > 100 ? 100 : minDiff);
        listView = (HorizontalListView) findViewById(R.id.aliyun_video_tailor_image_list);
        listView.setOnScrollCallBack(this);
        adapter = new VideoTrimAdapter(AliyunVideoCropActivity.this, new ArrayList<SoftReference<Bitmap>>());
        listView.setAdapter(adapter);
        transFormBtn = (ImageView) findViewById(R.id.aliyun_transform);
        transFormBtn.setOnClickListener(this);
        nextBtn = (ImageView) findViewById(R.id.aliyun_next);
        nextBtn.setOnClickListener(this);
        cancelBtn = (ImageView) findViewById(R.id.aliyun_back);
        cancelBtn.setOnClickListener(this);
        dirationTxt = (TextView) findViewById(R.id.aliyun_duration_txt);
        dirationTxt.setText((float) duration / 1000 + "");
        mCropProgressBg = (FrameLayout) findViewById(R.id.aliyun_crop_progress_bg);
        mCropProgressBg.setVisibility(View.GONE);
        mCropProgress = (FanProgressBar) findViewById(R.id.aliyun_crop_progress);
        mCropProgress.setOutRadius(DensityUtil.dip2px(this, 40) / 2 - mOutStrokeWidth / 2);
        mCropProgress.setOffset(mOutStrokeWidth / 2, mOutStrokeWidth / 2);
        mCropProgress.setOutStrokeWidth(mOutStrokeWidth);
        setListViewHeight();

        requestThumbItemTime();
    }

    private void setListViewHeight() {
        LayoutParams layoutParams = (LayoutParams) listView.getLayoutParams();
        layoutParams.height = (screenWidth - DensityUtils.dip2px(this, 40)) / 10;
        listView.setLayoutParams(layoutParams);
        seekBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, layoutParams.height));
    }

    public void initSurface() {
        frame = (VideoTrimFrameLayout) findViewById(R.id.aliyun_video_surfaceLayout);
        frame.setOnSizeChangedListener(this);
        frame.setOnScrollCallBack(this);
        textureview = (TextureView) findViewById(R.id.aliyun_video_textureview);
        resizeFrame();
        textureview.setSurfaceTextureListener(this);
    }

    private void resizeFrame() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) frame.getLayoutParams();
        switch (ratioMode) {
        case AliyunSnapVideoParam.RATIO_MODE_1_1:
            layoutParams.width = screenWidth;
            layoutParams.height = screenWidth;
            break;
        case AliyunSnapVideoParam.RATIO_MODE_3_4:
            layoutParams.width = screenWidth;
            layoutParams.height = screenWidth * 4 / 3;
            break;
        case AliyunSnapVideoParam.RATIO_MODE_9_16:
            layoutParams.width = screenWidth;
            layoutParams.height = screenWidth * 16 / 9;
            break;
        default:
            layoutParams.width = screenWidth;
            layoutParams.height = screenWidth * 16 / 9;
            break;
        }
        frame.setLayoutParams(layoutParams);
        NativeParser parser = new NativeParser();
        parser.init(mInputVideoPath);
        try {
            videoWidth = Integer.parseInt(parser.getValue(NativeParser.VIDEO_WIDTH));
            videoHeight = Integer.parseInt(parser.getValue(NativeParser.VIDEO_HEIGHT));

        } catch (NumberFormatException ex) {
            Log.e(TAG, ex.getMessage());
            return;
        } finally {
            parser.release();
            parser.dispose();
        }

        if (videoWidth == 0 || videoHeight == 0) {
            Log.e(TAG, "NativeParser parser video width = 0 or height = 0");
            return;
        }

        frameWidth = layoutParams.width;
        frameHeight = layoutParams.height;
        if (cropMode == SCALE_CROP) {
            scaleCrop(videoWidth, videoHeight);
        } else if (cropMode == SCALE_FILL) {
            scaleFill(videoWidth, videoHeight);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mPlayer == null) {
            mSurface = new Surface(surface);
            mPlayer = AliyunSVideoPlayerCreator.createPlayer();
            mPlayer.init(this);

            mPlayer.setPlayerCallback(new PlayerCallback() {
                @Override
                public void onPlayComplete() {

                }

                @Override
                public void onDataSize(int dataWidth, int dataHeight) {
                    if (dataWidth == 0 || dataHeight == 0) {
                        Log.e(TAG, "error , video player onDataSize width = 0 or height = 0");
                        return;
                    }
                    frameWidth = frame.getWidth();
                    frameHeight = frame.getHeight();
                    videoWidth = dataWidth;
                    videoHeight = dataHeight;
                    if (crop != null && mEndTime == 0) {
                        try {
                            mEndTime = (long) (crop.getVideoDuration(mInputVideoPath) * 1.0f / 1000);
                        } catch (Exception e) {
                            ToastUtil.showToast(AliyunVideoCropActivity.this, R.string.alivc_crop_video_tip_error);
                        }
                    }

                    if (cropMode == SCALE_CROP) {
                        scaleCrop(dataWidth, dataHeight);
                    } else if (cropMode == SCALE_FILL) {
                        scaleFill(dataWidth, dataHeight);
                    }
                    mPlayer.setDisplaySize(textureview.getLayoutParams().width, textureview.getLayoutParams().height);
                    playVideo();

                }

                @Override
                public void onError(int i) {
                    Log.e(TAG, "错误码 : " + i);
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(AliyunVideoCropActivity.this, getString(R.string.alivc_crop_video_tip_error));
                        }
                    });

                }
            });
            mPlayer.setDisplay(mSurface);
            mPlayer.setSource(mInputVideoPath);

        }
        Log.i(TAG, "onSurfaceTextureAvailable");
    }


    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        mPlayer.setDisplaySize(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.e(TAG, "onSurfaceTextureDestroyed");
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            playState = END_VIDEO;
            mPlayer = null;
            mSurface.release();
            mSurface = null;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private VideoSliceSeekBar.SeekBarChangeListener mSeekBarListener = new VideoSliceSeekBar.SeekBarChangeListener() {
        @Override
        public void seekBarValueChanged(float leftThumb, float rightThumb, int whitchSide) {
            long seekPos = 0;
            if (whitchSide == 0) {
                seekPos = (long) (duration * leftThumb / 100);
                mStartTime = seekPos;
            } else if (whitchSide == 1) {
                seekPos = (long) (duration * rightThumb / 100);
                mEndTime = seekPos;
            }
            dirationTxt.setText((float) (mEndTime - mStartTime) / 1000 + "");
            if (mPlayer != null) {
                mPlayer.seek((int) seekPos);
            }
            Log.e(TAG, "mStartTime" + mStartTime);
        }

        @Override
        public void onSeekStart() {
            pauseVideo();
        }

        @Override
        public void onSeekEnd() {
            needPlayStart = true;
            if (playState == PAUSE_VIDEO) {
                playVideo();
            }
        }
    };

    private void resetScroll() {
        mScrollX = 0;
        mScrollY = 0;
    }

    @Override
    public void onScrollDistance(Long count, int distanceX) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPause) {
            playVideo();
        }
        if (isCropping && mPlayer != null) {
            long currentPosition = mPlayer.getCurrentPosition() / 1000;
            mPlayer.draw(currentPosition);
        }
    }

    @Override
    protected void onPause() {
        if (playState == PLAY_VIDEO) {
            pauseVideo();
        }
        isPause = true;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (crop != null) {
            crop.dispose();
            crop = null;
        }
        if (mThumbnailFetcher != null) {
            mThumbnailFetcher.release();
        }
    }

    private void scaleFill(int videoWidth, int videoHeight) {
        if (videoWidth == 0 || videoHeight == 0) {
            Log.e(TAG, "error , videoSize width = 0 or height = 0");
            return;
        }
        LayoutParams layoutParams = (LayoutParams) textureview.getLayoutParams();
        int s = Math.min(videoWidth, videoHeight);
        int b = Math.max(videoWidth, videoHeight);
        float videoRatio = (float) b / s;
        float ratio = 1f;
        switch (ratioMode) {
        case AliyunSnapVideoParam.RATIO_MODE_1_1:
            ratio = 1f;
            break;
        case AliyunSnapVideoParam.RATIO_MODE_3_4:
            ratio = (float) 4 / 3;
            break;
        case AliyunSnapVideoParam.RATIO_MODE_9_16:
            ratio = (float) 16 / 9;
            break;
        default:
            ratio = (float) 16 / 9;
            break;
        }
        if (videoWidth > videoHeight) {
            layoutParams.width = frameWidth;
            layoutParams.height = frameWidth * videoHeight / videoWidth;
        } else {
            if (videoRatio >= ratio) {
                layoutParams.height = frameHeight;
                layoutParams.width = frameHeight * videoWidth / videoHeight;
            } else {
                layoutParams.width = frameWidth;
                layoutParams.height = frameWidth * videoHeight / videoWidth;
            }
        }
        layoutParams.setMargins(0, 0, 0, 0);
        textureview.setLayoutParams(layoutParams);
        cropMode = SCALE_FILL;
        transFormBtn.setActivated(false);
        resetScroll();
    }

    private void scaleCrop(int videoWidth, int videoHeight) {
        if (videoWidth == 0 || videoHeight == 0) {
            Log.e(TAG, "error , videoSize width = 0 or height = 0");
            return;
        }
        LayoutParams layoutParams = (LayoutParams) textureview.getLayoutParams();
        int s = Math.min(videoWidth, videoHeight);
        int b = Math.max(videoWidth, videoHeight);
        float videoRatio = (float) b / s;
        float ratio;
        switch (ratioMode) {
        case AliyunSnapVideoParam.RATIO_MODE_1_1:
            ratio = 1f;
            break;
        case AliyunSnapVideoParam.RATIO_MODE_3_4:
            ratio = (float) 4 / 3;
            break;
        case AliyunSnapVideoParam.RATIO_MODE_9_16:
            ratio = (float) 16 / 9;
            break;
        default:
            ratio = (float) 16 / 9;
            break;
        }
        if (ratioMode == AlivcSvideoEditParam.RATIO_MODE_ORIGINAL) {
            //原比例显示逻辑和填充模式一致
            if (videoWidth > videoHeight) {
                layoutParams.width = frameWidth;
                layoutParams.height = frameWidth * videoHeight / videoWidth;
            } else {
                if (videoRatio >= ratio) {
                    layoutParams.height = frameHeight;
                    layoutParams.width = frameHeight * videoWidth / videoHeight;
                } else {
                    layoutParams.width = frameWidth;
                    layoutParams.height = frameWidth * videoHeight / videoWidth;
                }
            }
        } else {
            if (videoWidth > videoHeight) {
                layoutParams.height = frameHeight;
                layoutParams.width = frameHeight * videoWidth / videoHeight;
            } else {
                if (videoRatio >= ratio) {
                    layoutParams.width = frameWidth;
                    layoutParams.height = frameWidth * videoHeight / videoWidth;
                } else {
                    layoutParams.height = frameHeight;
                    layoutParams.width = frameHeight * videoWidth / videoHeight;

                }
            }

        }

        layoutParams.setMargins(0, 0, 0, 0);
        textureview.setLayoutParams(layoutParams);
        cropMode = SCALE_CROP;
        transFormBtn.setActivated(true);
        resetScroll();
    }


    private void scanFile() {
        MediaScannerConnection.scanFile(getApplicationContext(),
                                        new String[] {outputPath}, new String[] {"video/mp4"}, null);
    }

    private void playVideo() {
        if (isCropping) {
            //裁剪过程中点击无效
            return;
        }
        if (mPlayer == null) {
            return;
        }
        mPlayer.seek((int) mStartTime);
        mPlayer.resume();
        playState = PLAY_VIDEO;
        long videoPos = mStartTime;
        playHandler.sendEmptyMessage(PLAY_VIDEO);
        //重新播放之后修改为false，防止暂停、播放的时候重新开始播放
        needPlayStart = false;
    }

    private void pauseVideo() {
        if (mPlayer == null) {
            return;
        }
        mPlayer.pause();
        playState = PAUSE_VIDEO;
        playHandler.removeMessages(PLAY_VIDEO);
        seekBar.showFrameProgress(false);
        seekBar.invalidate();
    }

    private void resumeVideo() {
        if (mPlayer == null) {
            return;
        }
        if (needPlayStart) {
            playVideo();
            needPlayStart = false;
            return;
        }
        mPlayer.resume();
        playState = PLAY_VIDEO;
        playHandler.sendEmptyMessage(PLAY_VIDEO);
    }

    @Override
    public void onBackPressed() {
        if (isCropping) {
            crop.cancel();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        frameWidth = frame.getWidth();
        frameHeight = frame.getHeight();
        videoWidth = width;
        videoHeight = height;
        mStartTime = 0;
        if (crop != null) {
            try {
                mEndTime = (long) (crop.getVideoDuration(mInputVideoPath) * 1.0f / 1000);
            } catch (Exception e) {
                ToastUtil.showToast(this, R.string.alivc_crop_video_tip_crop_failed);
            }
        } else {
            mEndTime = Integer.MAX_VALUE;
        }
        if (cropMode == SCALE_CROP) {
            scaleCrop(width, height);
        } else if (cropMode == SCALE_FILL) {
            scaleFill(width, height);
        }

    }

    @Override
    public void onSizeChanged(View view, int w, int h, int oldw, int oldh) {

    }

    @Override
    public void onVideoScroll(float distanceX, float distanceY) {
        if (isCropping) {
            //裁剪中无法操作
            return;
        }
        LayoutParams lp = (LayoutParams) textureview.getLayoutParams();
        int width = lp.width;
        int height = lp.height;

        if (width > frameWidth || height > frameHeight) {
            int maxHorizontalScroll = width - frameWidth;
            int maxVerticalScroll = height - frameHeight;
            if (maxHorizontalScroll > 0) {
                maxHorizontalScroll = maxHorizontalScroll / 2;
                mScrollX += distanceX;
                if (mScrollX > maxHorizontalScroll) {
                    mScrollX = maxHorizontalScroll;
                }
                if (mScrollX < -maxHorizontalScroll) {
                    mScrollX = -maxHorizontalScroll;
                }
            }
            if (maxVerticalScroll > 0) {
                maxVerticalScroll = maxVerticalScroll / 2;
                mScrollY += distanceY;
                if (mScrollY > maxVerticalScroll) {
                    mScrollY = maxVerticalScroll;
                }
                if (mScrollY < -maxVerticalScroll) {
                    mScrollY = -maxVerticalScroll;
                }
            }
            lp.setMargins(0, 0, mScrollX, mScrollY);
        }

        textureview.setLayoutParams(lp);
    }

    @Override
    public void onVideoSingleTapUp() {
        if (isCropping) {
            //裁剪过程中点击无效
            return;
        }
        if (playState == END_VIDEO) {
            playVideo();
        } else if (playState == PLAY_VIDEO) {
            pauseVideo();
        } else if (playState == PAUSE_VIDEO) {
            resumeVideo();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == transFormBtn) {
            if (isCropping) {
                return;
            }
            if (cropMode == SCALE_FILL) {
                scaleCrop(videoWidth, videoHeight);
            } else if (cropMode == SCALE_CROP) {
                scaleFill(videoWidth, videoHeight);
            }
        } else if (v == nextBtn) {

            if (mScrollX != 0 || mScrollY != 0 || !cropMode.equals(mOriginalMode)) {
                //需要裁剪画面时或者切换裁剪模式时，走真裁剪
                mAction = CropKey.ACTION_TRANSCODE;
            }
            switch (mAction) {
            case CropKey.ACTION_TRANSCODE:
                startCrop();
                break;
            case CropKey.ACTION_SELECT_TIME:
                long duration = mEndTime - mStartTime;
                AlivcCropOutputParam cropOutputParam = new AlivcCropOutputParam();
                //由于只是选择时间，所以文件路径和输入路径保持一致
                cropOutputParam.setOutputPath(mInputVideoPath);
                cropOutputParam.setDuration(duration);
                cropOutputParam.setStartTime(mStartTime);
                onCropComplete(cropOutputParam);
                break;
            default:
                break;
            }
        } else if (v == cancelBtn) {
            onBackPressed();
        }
    }

    /**
     * 裁剪结束
     */
    private void onCropComplete(AlivcCropOutputParam outputParam) {
        //裁剪结束
        Intent intent = getIntent();
        intent.putExtra(AlivcCropOutputParam.RESULT_KEY_OUTPUT_PARAM, outputParam);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
    private void startCrop() {

        if (!PermissionUtils.checkPermissionsGroup(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE})) {
            ToastUtils.show(this, PermissionUtils.NO_PERMISSION_TIP[4]);
            return;
        }


        if (frameWidth == 0 || frameHeight == 0) {
            ToastUtil.showToast(this, R.string.alivc_crop_video_tip_crop_failed);
            isCropping = false;
            return;
        }
        if (isCropping) {
            return;
        }
        //开始裁剪时，暂停视频的播放,提高裁剪效率
        pauseVideo();
        final LayoutParams lp = (LayoutParams) textureview.getLayoutParams();
        int posX;
        int posY;
        int outputWidth = 0;
        int outputHeight = 0;
        int cropWidth;
        int cropHeight;
        outputPath = Constants.SDCardConstants.getDir(this) + DateTimeUtils.getDateTimeFromMillisecond(System.currentTimeMillis()) + Constants.SDCardConstants.CROP_SUFFIX;
        float videoRatio = (float) videoHeight / videoWidth;
        float outputRatio = 1f;
        switch (ratioMode) {
        case AliyunSnapVideoParam.RATIO_MODE_1_1:
            outputRatio = 1f;
            break;
        case AliyunSnapVideoParam.RATIO_MODE_3_4:
            outputRatio = (float) 4 / 3;
            break;
        case AliyunSnapVideoParam.RATIO_MODE_9_16:
            outputRatio = (float) 16 / 9;
            break;
        case RATIO_ORIGINAL:
            outputRatio = videoRatio;
            break;
        default:
            outputRatio = (float) 16 / 9;
            break;
        }
        if (videoRatio > outputRatio) {
            posX = 0;
            posY = ((lp.height - frameHeight) / 2 + mScrollY) * videoWidth / frameWidth;
            while (posY % 4 != 0) {
                posY++;
            }
            switch (resolutionMode) {
            case AliyunSnapVideoParam.RESOLUTION_360P:
                outputWidth = 360;
                break;
            case AliyunSnapVideoParam.RESOLUTION_480P:
                outputWidth = 480;
                break;
            case AliyunSnapVideoParam.RESOLUTION_540P:
                outputWidth = 540;
                break;
            case AliyunSnapVideoParam.RESOLUTION_720P:
                outputWidth = 720;
                break;
            default:
                outputWidth = 720;
                break;
            }
            cropWidth = videoWidth;
            cropHeight = 0;
            switch (ratioMode) {
            case AliyunSnapVideoParam.RATIO_MODE_1_1:
                cropHeight = videoWidth;
                outputHeight = outputWidth;
                break;
            case AliyunSnapVideoParam.RATIO_MODE_3_4:
                cropHeight = videoWidth * 4 / 3;
                outputHeight = outputWidth * 4 / 3;
                break;
            case AliyunSnapVideoParam.RATIO_MODE_9_16:
                cropHeight = videoWidth * 16 / 9;
                outputHeight = outputWidth * 16 / 9;
                break;
            default:
                break;
            }
        } else if (videoRatio < outputRatio) {

            posX = ((lp.width - frameWidth) / 2 + mScrollX) * videoHeight / frameHeight;
            posY = 0;
            while (posX % 4 != 0) {
                posX++;
            }
            switch (resolutionMode) {
            case AliyunSnapVideoParam.RESOLUTION_360P:
                outputWidth = 360;
                break;
            case AliyunSnapVideoParam.RESOLUTION_480P:
                outputWidth = 480;
                break;
            case AliyunSnapVideoParam.RESOLUTION_540P:
                outputWidth = 540;
                break;
            case AliyunSnapVideoParam.RESOLUTION_720P:
                outputWidth = 720;
                break;
            default:
                outputWidth = 720;
                break;
            }
            cropHeight = videoHeight;
            switch (ratioMode) {
            case AliyunSnapVideoParam.RATIO_MODE_1_1:
                cropWidth = videoHeight;
                outputHeight = outputWidth;
                break;
            case AliyunSnapVideoParam.RATIO_MODE_3_4:
                cropWidth = videoHeight * 3 / 4;
                outputHeight = outputWidth * 4 / 3;
                break;
            case AliyunSnapVideoParam.RATIO_MODE_9_16:
                cropWidth = videoHeight * 9 / 16;
                outputHeight = outputWidth * 16 / 9;
                break;
            case RATIO_ORIGINAL:
                cropWidth = (int) (videoHeight / videoRatio);
                outputHeight = (int) (outputWidth * videoRatio);
                break;
            default:
                cropWidth = videoHeight * 9 / 16;
                outputHeight = outputWidth * 16 / 9;
                break;
            }
        } else {
            // 原比例或videoRatio = outputRatio执行else

            posX = 0;
            posY = 0;

            switch (resolutionMode) {
            case AliyunSnapVideoParam.RESOLUTION_360P:
                outputWidth = 360;
                break;
            case AliyunSnapVideoParam.RESOLUTION_480P:
                outputWidth = 480;
                break;
            case AliyunSnapVideoParam.RESOLUTION_540P:
                outputWidth = 540;
                break;
            case AliyunSnapVideoParam.RESOLUTION_720P:
                outputWidth = 720;
                break;
            default:
                outputWidth = 720;
                break;
            }
            cropHeight = videoHeight;
            switch (ratioMode) {
            case AliyunSnapVideoParam.RATIO_MODE_1_1:
                cropWidth = videoHeight;
                outputHeight = outputWidth;
                break;
            case AliyunSnapVideoParam.RATIO_MODE_3_4:
                cropWidth = videoHeight * 3 / 4;
                outputHeight = outputWidth * 4 / 3;
                break;
            case AliyunSnapVideoParam.RATIO_MODE_9_16:
                cropWidth = videoHeight * 9 / 16;
                outputHeight = outputWidth * 16 / 9;
                break;
            case RATIO_ORIGINAL:
                cropWidth = (int) (videoHeight / videoRatio);
                outputHeight = (int) (outputWidth * videoRatio);
                break;
            default:
                cropWidth = videoHeight * 9 / 16;
                outputHeight = outputWidth * 16 / 9;
                break;
            }
        }

        CropParam cropParam = new CropParam();
        cropParam.setOutputPath(outputPath);
        cropParam.setInputPath(mInputVideoPath);
        cropParam.setOutputWidth(outputWidth);
        cropParam.setOutputHeight(outputHeight);
        Rect cropRect = new Rect(posX, posY, posX + cropWidth, posY + cropHeight);
        cropParam.setCropRect(cropRect);
        cropParam.setStartTime(mStartTime, TimeUnit.MILLISECONDS);
        cropParam.setEndTime(mEndTime, TimeUnit.MILLISECONDS);
        cropParam.setScaleMode(cropMode);
        cropParam.setFrameRate(frameRate);
        cropParam.setGop(gop);
        cropParam.setQuality(quality);
        cropParam.setVideoCodec(mVideoCodec);
        cropParam.setFillColor(Color.BLACK);
        cropParam.setCrf(0);

        mCropProgressBg.setVisibility(View.VISIBLE);
        cropParam.setUseGPU(isUseGPU);
        crop.setCropParam(cropParam);



        int ret = crop.startCrop();
        if (ret < 0) {
            ToastUtil.showToast(this, getString(R.string.alivc_crop_video_tip_crop_failed) + "  " + ret);
            return;
        }
        startCropTimestamp = System.currentTimeMillis();
        Log.d("CROP_COST", "start : " + startCropTimestamp);
        isCropping = true;
        seekBar.setSliceBlocked(true);


    }

    long startCropTimestamp;

    private void deleteFile() {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                FileUtils.deleteFile(outputPath);
                return null;
            }
        } .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onProgress(final int percent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCropProgress.setProgress(percent);
            }
        });
    }

    @Override
    public void onError(final int code) {
        Log.d(TAG, "crop failed : " + code);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCropProgressBg.setVisibility(View.GONE);
                seekBar.setSliceBlocked(false);
                switch (code) {
                case AliyunErrorCode.ALIVC_SVIDEO_ERROR_MEDIA_NOT_SUPPORTED_VIDEO:
                    ToastUtil.showToast(AliyunVideoCropActivity.this, R.string.alivc_crop_video_tip_not_supported_video);
                    break;
                case AliyunErrorCode.ALIVC_SVIDEO_ERROR_MEDIA_NOT_SUPPORTED_AUDIO:
                    ToastUtil.showToast(AliyunVideoCropActivity.this, R.string.alivc_crop_video_tip_not_supported_audio);
                    break;
                default:
                    ToastUtil.showToast(AliyunVideoCropActivity.this, R.string.alivc_crop_video_tip_crop_failed);
                    break;
                }
//                progressDialog.dismiss();
                setResult(Activity.RESULT_CANCELED, getIntent());
            }
        });
        isCropping = false;

    }

    @Override
    public void onComplete(long duration) {
        long time = System.currentTimeMillis();
        Log.d(TAG, "completed : " + (time - startCropTimestamp));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCropProgress.setVisibility(View.GONE);
                mCropProgressBg.setVisibility(View.GONE);
                seekBar.setSliceBlocked(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    //适配android Q
                    ThreadUtils.runOnSubThread(new Runnable() {
                        @Override
                        public void run() {
                            UriUtils.saveVideoToMediaStore(AliyunVideoCropActivity.this.getApplicationContext(), outputPath);
                        }
                    });
                } else {
                    scanFile();
                }
                long duration = mEndTime - mStartTime;
                AlivcCropOutputParam cropOutputParam = new AlivcCropOutputParam();
                cropOutputParam.setOutputPath(outputPath);
                cropOutputParam.setDuration(duration);
                onCropComplete(cropOutputParam);
            }
        });
        isCropping = false;
        VideoInfoUtils.printVideoInfo(outputPath);
    }

    @Override
    public void onCancelComplete() {
        //取消完成
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCropProgressBg.setVisibility(View.GONE);
                seekBar.setSliceBlocked(false);
            }
        });
        deleteFile();
        setResult(Activity.RESULT_CANCELED);
        finish();
        isCropping = false;
    }


    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
        case PAUSE_VIDEO:
            pauseVideo();
            break;
        case PLAY_VIDEO:
            if (mPlayer != null) {
                long currentPlayPos = mPlayer.getCurrentPosition() / 1000;
                Log.d(TAG, "currentPlayPos:" + currentPlayPos);
                if (currentPlayPos < mEndTime) {
                    seekBar.showFrameProgress(true);
                    seekBar.setFrameProgress(currentPlayPos / (float) duration);
                    playHandler.sendEmptyMessageDelayed(PLAY_VIDEO, 100);
                } else {
                    playVideo();
                }
            }
            break;
        default:
            break;
        }
        return false;
    }
    public static void startVideoCropForResult(Activity context, AlivcCropInputParam param, int requestCode) {
        if (param == null || TextUtils.isEmpty(param.getPath())) {
            return;
        }
        Intent intent = new Intent(context, AliyunVideoCropActivity.class);
        intent.putExtra(AlivcCropInputParam.INTENT_KEY_PATH, param.getPath());
        intent.putExtra(AlivcCropInputParam.INTENT_KEY_RESOLUTION_MODE, param.getResolutionMode());
        intent.putExtra(AlivcCropInputParam.INTENT_KEY_CROP_MODE, param.getCropMode());
        intent.putExtra(AlivcCropInputParam.INTENT_KEY_QUALITY, param.getQuality());
        intent.putExtra(AlivcCropInputParam.INTENT_KEY_CODECS, param.getVideoCodecs());
        int gop;
        if (requestCode == REQUEST_CODE_EDITOR_VIDEO_CROP) {
            //editor 裁剪预览转码时设置gop 5 ，进入编辑不需要二次裁剪
            gop = 5;
        } else {
            gop = param.getGop();
        }
        intent.putExtra(AlivcCropInputParam.INTENT_KEY_GOP, gop);
        intent.putExtra(AlivcCropInputParam.INTENT_KEY_RATIO_MODE, param.getRatioMode());
        intent.putExtra(AlivcCropInputParam.INTENT_KEY_FRAME_RATE, param.getFrameRate());
        intent.putExtra(AlivcCropInputParam.INTENT_KEY_ACTION, param.getAction());
        intent.putExtra(AlivcCropInputParam.INTENT_KEY_MIN_DURATION, param.getMinCropDuration());
        intent.putExtra(AlivcCropInputParam.INTENT_KEY_USE_GPU, param.isUseGPU());
        context.startActivityForResult(intent, requestCode);
    }

    public static void startCropForResult(Activity activity, int requestCode, AliyunSnapVideoParam param) {
        Intent intent = new Intent(activity, CropMediaActivity.class);
        intent.putExtra(AliyunSnapVideoParam.SORT_MODE, param.getSortMode());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_RESOLUTION, param.getResolutionMode());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_RATIO, param.getRatioMode());
        intent.putExtra(AliyunSnapVideoParam.NEED_RECORD, param.isNeedRecord());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_QUALITY, param.getVideoQuality());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_CODEC, param.getVideoCodec());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_GOP, param.getGop());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_FRAMERATE, param.getFrameRate());
        intent.putExtra(AliyunSnapVideoParam.CROP_MODE, param.getScaleMode());
        intent.putExtra(AliyunSnapVideoParam.MIN_CROP_DURATION, param.getMinCropDuration());
        intent.putExtra(AliyunSnapVideoParam.MIN_VIDEO_DURATION, param.getMinVideoDuration());
        intent.putExtra(AliyunSnapVideoParam.MAX_VIDEO_DURATION, param.getMaxVideoDuration());
        intent.putExtra(AliyunSnapVideoParam.RECORD_MODE, param.getRecordMode());
        intent.putExtra(AliyunSnapVideoParam.FILTER_LIST, param.getFilterList());
        intent.putExtra(AliyunSnapVideoParam.BEAUTY_LEVEL, param.getBeautyLevel());
        intent.putExtra(AliyunSnapVideoParam.BEAUTY_STATUS, param.getBeautyStatus());
        intent.putExtra(AliyunSnapVideoParam.CAMERA_TYPE, param.getCameraType());
        intent.putExtra(AliyunSnapVideoParam.FLASH_TYPE, param.getFlashType());
        intent.putExtra(AliyunSnapVideoParam.NEED_CLIP, param.isNeedClip());
        intent.putExtra(AliyunSnapVideoParam.MAX_DURATION, param.getMaxDuration());
        intent.putExtra(AliyunSnapVideoParam.MIN_DURATION, param.getMinDuration());
        intent.putExtra(AliyunSnapVideoParam.CROP_USE_GPU, param.isCropUseGPU());
        activity.startActivityForResult(intent, requestCode);
    }


    public int getVideoWidth() {
        int width = 0;
        switch (resolutionMode) {
        case AliyunSnapVideoParam.RESOLUTION_360P:
            width = 360;
            break;
        case AliyunSnapVideoParam.RESOLUTION_480P:
            width = 480;
            break;
        case AliyunSnapVideoParam.RESOLUTION_540P:
            width = 540;
            break;
        case AliyunSnapVideoParam.RESOLUTION_720P:
            width = 720;
            break;
        default:
            width = 540;
            break;
        }
        return width;
    }



    /**
     * 获取每个item取帧的时间值
     **/
    private void requestThumbItemTime() {
        int itemWidth = screenWidth / 10;

        mThumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher();
        mThumbnailFetcher.addVideoSource(mInputVideoPath, 0, Integer.MAX_VALUE, 0);
        mThumbnailFetcher.setParameters(itemWidth, itemWidth, AliyunIThumbnailFetcher.CropMode.Mediate, VideoDisplayMode.SCALE, 10);
        mThumbnailFetcher.setFastMode(true);
        long duration = mThumbnailFetcher.getTotalDuration();
        long itemTime = duration / 10;
        for (int i = 1; i <= 10; i++) {
            requestFetchThumbnail(itemTime, i, 10);
        }

    }

    /**
     * 获取缩略图
     *
     * @param interval 取帧平均间隔
     * @param position 第几张
     * @param count    总共的张数
     */
    private void requestFetchThumbnail(final long interval, final int position, final int count) {
        long[] times = {(position - 1) * interval + interval / 2};

        Log.d(TAG, "requestThumbnailImage() times :" + times[0] + " ,position = " + position);
        mThumbnailFetcher.requestThumbnailImage(times, new AliyunIThumbnailFetcher.OnThumbnailCompletion() {

            private int vecIndex = 1;

            @Override
            public void onThumbnailReady(Bitmap frameBitmap, long l, int index) {
                if (frameBitmap != null && !frameBitmap.isRecycled()) {
                    Log.i(TAG, "onThumbnailReady  put: " + position + " ,l = " + l / 1000);

                    SoftReference<Bitmap> bitmapSoftReference = new SoftReference<Bitmap>(frameBitmap);

                    adapter.add(bitmapSoftReference);
                } else {
                    if (position == 0) {
                        vecIndex = 1;
                    } else if (position == count + 1) {
                        vecIndex = -1;
                    }
                    int np = position + vecIndex;
                    Log.i(TAG, "requestThumbnailImage  failure: thisPosition = " + position + "newPosition = " + np);
                    requestFetchThumbnail(interval, np, count);
                }
            }

            @Override
            public void onError(int errorCode) {
                Log.w(TAG, "requestThumbnailImage error msg: " + errorCode);
            }
        });
    }

}
