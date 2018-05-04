/*
 * Copyright (C) 2010-2013 Alibaba Group Holding Limited
 */

package com.aliyun.demo.crop;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aliyun.common.global.Version;
import com.aliyun.common.utils.DensityUtil;
import com.aliyun.common.utils.FileUtils;
import com.aliyun.common.utils.ToastUtil;
import com.aliyun.crop.AliyunCropCreator;
import com.aliyun.crop.struct.CropParam;
import com.aliyun.crop.supply.AliyunICrop;
import com.aliyun.crop.supply.CropCallback;
import com.aliyun.demo.crop.media.FrameExtractor10;
import com.aliyun.demo.crop.media.VideoTrimAdapter;
import com.aliyun.querrorcode.AliyunErrorCode;
import com.aliyun.quview.FanProgressBar;
import com.aliyun.quview.HorizontalListView;
import com.aliyun.quview.SizeChangedNotifier;
import com.aliyun.quview.VideoSliceSeekBar;
import com.aliyun.quview.VideoTrimFrameLayout;
import com.aliyun.struct.common.CropKey;
import com.aliyun.struct.common.ScaleMode;
import com.aliyun.struct.common.VideoQuality;
import com.aliyun.struct.snap.AliyunSnapVideoParam;

import java.io.File;
import java.io.IOException;


/**
 * Copyright (C) 2010-2013 Alibaba Group Holding Limited
 * <p>
 * Created by Administrator on 2017/1/16.
 */

public class AliyunVideoCrop extends Activity implements TextureView.SurfaceTextureListener,
        VideoSliceSeekBar.SeekBarChangeListener, HorizontalListView.OnScrollCallBack, SizeChangedNotifier.Listener,
        MediaPlayer.OnVideoSizeChangedListener, VideoTrimFrameLayout.OnVideoScrollCallBack, View.OnClickListener, CropCallback, Handler.Callback {

    public static final ScaleMode SCALE_CROP = ScaleMode.PS;
    public static final ScaleMode SCALE_FILL = ScaleMode.LB;


    private static final int PLAY_VIDEO = 1000;
    private static final int PAUSE_VIDEO = 1001;
    private static final int END_VIDEO = 1003;

    private int playState = END_VIDEO;


    private static int OUT_STROKE_WIDTH;


    private AliyunICrop crop;

    private HorizontalListView listView;
    private VideoTrimFrameLayout frame;
    private TextureView textureview;
    private Surface mSurface;


    private MediaPlayer mPlayer;
    private ImageView cancelBtn, nextBtn, transFormBtn;
    private TextView dirationTxt;

    private VideoTrimAdapter adapter;

    private VideoSliceSeekBar seekBar;

    private FanProgressBar mCropProgress;
    private FrameLayout mCropProgressBg;

//    private ProgressDialog progressDialog;

    private long videoPos;
    private long lastVideoSeekTime;


    private String path;
    private String outputPath;
    private long duration;
    private int resolutionMode;
    private int ratioMode;
    private VideoQuality quality = VideoQuality.HD;
    private int frameRate;
    private int gop;
    private int mBitrate;

    private int screenWidth;
    private int screenHeight;
    private int frameWidth;
    private int frameHeight;
    private int mScrollX;
    private int mScrollY;
    private int videoWidth;
    private int videoHeight;
    private int cropDuration = 2000;


    private ScaleMode cropMode = ScaleMode.PS;

    private long mStartTime;
    private long mEndTime;

    private int maxDuration = Integer.MAX_VALUE;

    private FrameExtractor10 kFrame;

//    private MediaScannerConnection msc;

    private Handler playHandler = new Handler(this);

    private int currentPlayPos;

    private boolean isPause = false;
    private boolean isCropping = false;
    private boolean needPlayStart = false;
    private boolean isUseGPU = false;

    private int mAction = CropKey.ACTION_TRANSCODE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.aliyun_svideo_activity_crop);
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        crop = AliyunCropCreator.getCropInstance(this);
        crop.setCropCallback(this);
        getData();
        initView();
        initSurface();
//        if(msc == null) {
//            msc = new MediaScannerConnection(getApplicationContext(), null);
//            msc.connect();
//        }
    }

    private void getData() {
        mAction = getIntent().getIntExtra(CropKey.ACTION, CropKey.ACTION_TRANSCODE);
        path = getIntent().getStringExtra(CropKey.VIDEO_PATH);
        try {
            duration = crop.getVideoDuration(path) / 1000;
        } catch (Exception e) {
            ToastUtil.showToast(this, R.string.aliyun_video_crop_error);
        }//获取精确的视频时间
        resolutionMode = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_RESOLUTION, AliyunSnapVideoParam.RESOLUTION_540P);
        cropMode = (ScaleMode) getIntent().getSerializableExtra(AliyunSnapVideoParam.CROP_MODE);
        if (cropMode == null) {
            cropMode = ScaleMode.PS;
        }
        quality = (VideoQuality) getIntent().getSerializableExtra(AliyunSnapVideoParam.VIDEO_QUALITY);
        if (quality == null) {
            quality = VideoQuality.HD;
        }
        gop = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_GOP, 5);
        mBitrate = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_BITRATE, 0);
        frameRate = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_FRAMERATE, 25);
        ratioMode = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_RATIO, AliyunSnapVideoParam.RATIO_MODE_3_4);
        cropDuration = getIntent().getIntExtra(AliyunSnapVideoParam.MIN_CROP_DURATION, 2000);
        isUseGPU = getIntent().getBooleanExtra(AliyunSnapVideoParam.CROP_USE_GPU, false);
    }

    public static void startCropForResult(Activity activity, int requestCode, AliyunSnapVideoParam param) {
        Intent intent = new Intent(activity, MediaActivity.class);
        intent.putExtra(AliyunSnapVideoParam.SORT_MODE, param.getSortMode());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_RESOLUTION, param.getResolutionMode());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_RATIO, param.getRatioMode());
        intent.putExtra(AliyunSnapVideoParam.NEED_RECORD, param.isNeedRecord());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_QUALITY, param.getVideoQuality());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_GOP, param.getGop());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_BITRATE, param.getVideoBitrate());
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

    public static void startCrop(Context context, AliyunSnapVideoParam param) {
        Intent intent = new Intent(context, MediaActivity.class);
        intent.putExtra(AliyunSnapVideoParam.SORT_MODE, param.getSortMode());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_RESOLUTION, param.getResolutionMode());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_RATIO, param.getRatioMode());
        intent.putExtra(AliyunSnapVideoParam.NEED_RECORD, param.isNeedRecord());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_QUALITY, param.getVideoQuality());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_GOP, param.getGop());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_BITRATE, param.getVideoBitrate());
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
        context.startActivity(intent);
    }

    public static final String getVersion() {
        return Version.VERSION;
    }

    private void initView() {
        OUT_STROKE_WIDTH = DensityUtil.dip2px(this, 5);
        kFrame = new FrameExtractor10();
        kFrame.setDataSource(path);
        seekBar = (VideoSliceSeekBar) findViewById(R.id.aliyun_seek_bar);
        seekBar.setSeekBarChangeListener(this);
        int minDiff = (int) (cropDuration / (float) duration * 100) + 1;
        seekBar.setProgressMinDiff(minDiff > 100 ? 100 : minDiff);
        listView = (HorizontalListView) findViewById(R.id.aliyun_video_tailor_image_list);
        listView.setOnScrollCallBack(this);
        adapter = new VideoTrimAdapter(this, duration, maxDuration, kFrame, seekBar);
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
        mCropProgress.setOutRadius(DensityUtil.dip2px(this, 40) / 2 - OUT_STROKE_WIDTH / 2);
        mCropProgress.setOffset(OUT_STROKE_WIDTH / 2, OUT_STROKE_WIDTH / 2);
        mCropProgress.setOutStrokeWidth(OUT_STROKE_WIDTH);
        setListViewHeight();
    }

    private void setListViewHeight() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) listView.getLayoutParams();
        layoutParams.height = screenWidth / 8;
        listView.setLayoutParams(layoutParams);
        seekBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, screenWidth / 8));
    }

    public void initSurface() {
        frame = (VideoTrimFrameLayout) findViewById(R.id.aliyun_video_surfaceLayout);
        frame.setOnSizeChangedListener(this);
        frame.setOnScrollCallBack(this);
        resizeFrame();
        textureview = (TextureView) findViewById(R.id.aliyun_video_textureview);
        textureview.setSurfaceTextureListener(this);
    }

    private void resizeFrame() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) frame.getLayoutParams();
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
        }
        frame.setLayoutParams(layoutParams);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mPlayer == null) {
            mSurface = new Surface(surface);
            mPlayer = new MediaPlayer();
            mPlayer.setSurface(mSurface);
            try {
                mPlayer.setDataSource(path);
                mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        if (!isPause) {
                            playVideo();
                            playState = PLAY_VIDEO;
                        } else {
                            isPause = false;
                            mPlayer.start();
                            mPlayer.seekTo(currentPlayPos);
                            playHandler.sendEmptyMessageDelayed(PAUSE_VIDEO, 100);
                        }
                    }
                });
                mPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mPlayer.setOnVideoSizeChangedListener(this);

        }
    }


    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
            mSurface = null;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void SeekBarValueChanged(float leftThumb, float rightThumb, int whitchSide) {
        long seekPos = 0;
        if (whitchSide == 0) {
            seekPos = (long) (duration * leftThumb / 100);
            mStartTime = seekPos;
        } else if (whitchSide == 1) {
            seekPos = (long) (duration * rightThumb / 100);
            mEndTime = seekPos;
        }
        dirationTxt.setText((float) (mEndTime - mStartTime) / 1000 + "");
        if(mPlayer != null){
            mPlayer.seekTo((int) seekPos);
        }
    }

    @Override
    public void onSeekStart() {
        pauseVideo();
    }

    @Override
    public void onSeekEnd() {
        needPlayStart = true;
        if (playState == PLAY_VIDEO) {
            playVideo();
        }
    }

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
    }

    @Override
    protected void onPause() {
        if (playState == PLAY_VIDEO) {
            pauseVideo();
            playState = PAUSE_VIDEO;
        }
        isPause = true;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        msc.disconnect();
        AliyunCropCreator.destroyCropInstance();
    }

    private void scaleFill(int videoWidth, int videoHeight) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) textureview.getLayoutParams();
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
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) textureview.getLayoutParams();
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
        }
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
        layoutParams.setMargins(0, 0, 0, 0);
        textureview.setLayoutParams(layoutParams);
        cropMode = SCALE_CROP;
        transFormBtn.setActivated(true);
        resetScroll();
    }


    private void scanFile() {
        MediaScannerConnection.scanFile(getApplicationContext(),
                new String[]{outputPath}, new String[]{"video/mp4"}, null);
//        if(msc != null && msc.isConnected()) {
//            msc.scanFile(outputPath, "video/mp4");
//        }
    }

    private void playVideo() {
        if (mPlayer == null) {
            return;
        }
        mPlayer.seekTo((int) mStartTime);
        mPlayer.start();
        videoPos = mStartTime;
        lastVideoSeekTime = System.currentTimeMillis();
        playHandler.sendEmptyMessage(PLAY_VIDEO);
    }

    private void pauseVideo() {
        if (mPlayer == null) {
            return;
        }
        mPlayer.pause();
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
        mPlayer.start();
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
                mEndTime = (long) (crop.getVideoDuration(path) * 1.0f / 1000);
            } catch (Exception e) {
                ToastUtil.showToast(this, R.string.aliyun_video_crop_error);
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
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) textureview.getLayoutParams();
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
        if (playState == END_VIDEO) {
            playVideo();
            playState = PLAY_VIDEO;
        } else if (playState == PLAY_VIDEO) {
            pauseVideo();
            playState = PAUSE_VIDEO;
        } else if (playState == PAUSE_VIDEO) {
            resumeVideo();
            playState = PLAY_VIDEO;
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
            switch (mAction) {
                case CropKey.ACTION_TRANSCODE:
                    startCrop();
                    break;
                case CropKey.ACTION_SELECT_TIME:
                    Intent intent = getIntent();
                    intent.putExtra(CropKey.RESULT_KEY_CROP_PATH, path);
                    intent.putExtra(CropKey.RESULT_KEY_DURATION, mEndTime - mStartTime);
                    intent.putExtra(CropKey.RESULT_KEY_START_TIME, mStartTime);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                    break;
            }
        } else if (v == cancelBtn) {
            onBackPressed();
        }
    }

    private void startCrop() {
        if (frameWidth == 0 || frameHeight == 0) {
            ToastUtil.showToast(this, R.string.aliyun_video_crop_error);
            isCropping = false;
            return;
        }
        if (isCropping) {
            return;
        }
        final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) textureview.getLayoutParams();
        int posX;
        int posY;
        int outputWidth = 0;
        int outputHeight = 0;
        int cropWidth;
        int cropHeight;
        outputPath = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DCIM + File.separator + "crop_" + System.currentTimeMillis() + ".mp4";
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
            }
        } else {
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
            }
            cropWidth = 0;
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
            }
        }
        CropParam cropParam = new CropParam();
        cropParam.setOutputPath(outputPath);
        cropParam.setInputPath(path);
        cropParam.setOutputWidth(outputWidth);
        cropParam.setOutputHeight(outputHeight);
        Rect cropRect = new Rect(posX, posY, posX + cropWidth, posY + cropHeight);
        cropParam.setCropRect(cropRect);
        cropParam.setStartTime(mStartTime * 1000);
        cropParam.setEndTime(mEndTime * 1000);
        cropParam.setScaleMode(cropMode);
        cropParam.setFrameRate(frameRate);
        cropParam.setGop(gop);
        cropParam.setVideoBitrate(mBitrate);
        cropParam.setQuality(quality);
        cropParam.setFillColor(Color.BLACK);
//        if ((mEndTime - mStartTime) /  1000 / 60 >= 5) {
//            ToastUtil.showToast(this, R.string.aliyun_video_duration_5min_tip);
//            isCropping = false;
//            return;
//        }
        mCropProgressBg.setVisibility(View.VISIBLE);
        cropParam.setUseGPU(isUseGPU);
        crop.setCropParam(cropParam);
//        progressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.wait));
//        progressDialog.setCancelable(true);
//        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface dialog) {
//                crop.cancelCompose();
//                deleteFile();
//                setResult(Activity.RESULT_CANCELED, getIntent());
//            }
//        });


        int ret = crop.startCrop();
        if (ret < 0) {
            ToastUtil.showToast(this, getString(R.string.aliyun_video_crop_error) + "错误码 ：" + ret);
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
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        Log.d("CROP_COST", "crop failed : " + code);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCropProgressBg.setVisibility(View.GONE);
                seekBar.setSliceBlocked(false);
                switch (code) {
                    case AliyunErrorCode.ERROR_MEDIA_NOT_SUPPORTED_VIDEO:
                        ToastUtil.showToast(AliyunVideoCrop.this, R.string.aliyun_video_crop_error);
                        break;
                    case AliyunErrorCode.ERROR_MEDIA_NOT_SUPPORTED_AUDIO:
                        ToastUtil.showToast(AliyunVideoCrop.this, R.string.aliyun_not_supported_audio);
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
        Log.d("CROP_COST", "completed : " + (time - startCropTimestamp));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCropProgress.setVisibility(View.GONE);
                mCropProgressBg.setVisibility(View.GONE);
                seekBar.setSliceBlocked(false);
                scanFile();
                Intent intent = getIntent();
                intent.putExtra(CropKey.RESULT_KEY_CROP_PATH, outputPath);
                intent.putExtra(CropKey.RESULT_KEY_DURATION, mEndTime - mStartTime);
                intent.putExtra(CropKey.RESULT_KEY_FILE_PATH, path);
                setResult(Activity.RESULT_OK, intent);
                finish();
//                progressDialog.dismiss();
            }
        });
        isCropping = false;
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
                playState = PAUSE_VIDEO;
                break;
            case PLAY_VIDEO:
                if (mPlayer != null) {
                    currentPlayPos = (int) (videoPos + System.currentTimeMillis() - lastVideoSeekTime);
                    if (currentPlayPos < mEndTime) {
                        seekBar.showFrameProgress(true);
                        seekBar.setFrameProgress(currentPlayPos / (float) duration);
                        playHandler.sendEmptyMessageDelayed(PLAY_VIDEO, 100);
                    } else {
                        playVideo();
                    }
                }
                break;
        }
        return false;
    }
}
