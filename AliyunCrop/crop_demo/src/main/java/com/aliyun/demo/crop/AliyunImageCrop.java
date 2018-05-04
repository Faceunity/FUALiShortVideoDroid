/*
 * Copyright (C) 2010-2013 Alibaba Group Holding Limited
 */

package com.aliyun.demo.crop;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.aliyun.common.global.Version;
import com.aliyun.common.utils.DensityUtil;
import com.aliyun.common.utils.FileUtils;
import com.aliyun.common.utils.ToastUtil;
import com.aliyun.crop.AliyunCropCreator;
import com.aliyun.crop.struct.CropParam;
import com.aliyun.crop.supply.AliyunICrop;
import com.aliyun.crop.supply.CropCallback;
import com.aliyun.querrorcode.AliyunErrorCode;
import com.aliyun.quview.FanProgressBar;
import com.aliyun.quview.HorizontalListView;
import com.aliyun.quview.SizeChangedNotifier;
import com.aliyun.quview.VideoTrimFrameLayout;
import com.aliyun.struct.common.CropKey;
import com.aliyun.struct.common.ScaleMode;
import com.aliyun.struct.common.VideoQuality;
import com.aliyun.struct.snap.AliyunSnapVideoParam;
import com.aliyun.svideo.sdk.external.struct.MediaType;
import com.bumptech.glide.Glide;

import java.io.File;

import static com.aliyun.struct.snap.AliyunSnapVideoParam.CROP_MODE;


/**
 * Copyright (C) 2010-2013 Alibaba Group Holding Limited
 * <p>
 * Created by Administrator on 2017/1/16.
 */

public class AliyunImageCrop extends Activity implements HorizontalListView.OnScrollCallBack, SizeChangedNotifier.Listener,
        VideoTrimFrameLayout.OnVideoScrollCallBack, View.OnClickListener, CropCallback {

    public static final String VIDEO_PATH = "video_path";

    public static final ScaleMode SCALE_CROP = ScaleMode.PS;
    public static final ScaleMode SCALE_FILL = ScaleMode.LB;

    public static final String RESULT_KEY_CROP_PATH = "crop_path";
    public static final String RESULT_KEY_DURATION = "duration";


    private static final int PLAY_VIDEO = 1000;
    private static final int PAUSE_VIDEO = 1001;
    private static final int END_VIDEO = 1003;

    private int playState = END_VIDEO;


    private static int OUT_STROKE_WIDTH;


    private AliyunICrop crop;

    private VideoTrimFrameLayout frame;
    private ImageView mImageView;


    private ImageView cancelBtn, nextBtn, transFormBtn;

    private FanProgressBar mCropProgress;
    private FrameLayout mCropProgressBg;


    private String path;
    private String outputPath;
    private long duration;
    private int resolutionMode;
    private int ratioMode;
    private VideoQuality quality = VideoQuality.HD;
    private int frameRate;
    private int gop;

    private int screenWidth;
    private int screenHeight;
    private int frameWidth;
    private int frameHeight;
    private int mScrollX;
    private int mScrollY;
    private int mImageWidth;
    private int mImageHeight;


    private ScaleMode cropMode = ScaleMode.PS;

//    private MediaScannerConnection msc;

    private boolean isCropping = false;
    private String mSuffix;
    private String mMimeType;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.aliyun_svideo_activity_image_crop);
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        crop = AliyunCropCreator.getCropInstance(this);
        crop.setCropCallback(this);
        getData();
        initView();
        initSurface();
//        if(msc == null) {
//            msc = new MediaScannerConnection(this, null);
//            msc.connect();
//        }
        Glide.with(getApplicationContext()).load("file://" + path).into(mImageView);
        frame.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                frameWidth = frame.getWidth();
                frameHeight = frame.getHeight();
                if (cropMode == SCALE_CROP) {
                    scaleCrop(mImageWidth, mImageHeight);

                } else if (cropMode == SCALE_FILL) {
                    scaleFill(mImageWidth, mImageHeight);
                }
                frame.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    private void getData() {
        path = getIntent().getStringExtra(VIDEO_PATH);
        mSuffix = path.substring(path.lastIndexOf("."), path.length());
        resolutionMode = getIntent().getIntExtra(CropKey.VIDEO_RESOLUTION, CropKey.RESOLUTION_540P);
        cropMode = (ScaleMode) getIntent().getSerializableExtra(CROP_MODE);
        if (cropMode == null) {
            cropMode = ScaleMode.PS;
        }
        quality = (VideoQuality) getIntent().getSerializableExtra(CropKey.VIDEO_QUALITY);
        if (quality == null) {
            quality = VideoQuality.HD;
        }
        gop = getIntent().getIntExtra(CropKey.VIDEO_GOP, 5);
        frameRate = getIntent().getIntExtra(CropKey.VIDEO_FRAMERATE, 25);
        ratioMode = getIntent().getIntExtra(CropKey.VIDEO_RATIO, CropKey.RATIO_MODE_3_4);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        mMimeType = options.outMimeType;
        mImageWidth = options.outWidth;
        mImageHeight = options.outHeight;
    }

    public static void startCropForResult(Activity activity, int requestCode, AliyunSnapVideoParam param) {
        Intent intent = new Intent(activity, MediaActivity.class);
        intent.putExtra(AliyunSnapVideoParam.VIDEO_RESOLUTION, param.getResolutionMode());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_RATIO, param.getRatioMode());
        intent.putExtra(AliyunSnapVideoParam.NEED_RECORD, param.isNeedRecord());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_QUALITY, param.getVideoQuality());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_GOP, param.getGop());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_FRAMERATE, param.getFrameRate());
        intent.putExtra(CROP_MODE, param.getScaleMode());
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
        activity.startActivityForResult(intent, requestCode);
    }

    public static void startCrop(Context context, AliyunSnapVideoParam param) {
        Intent intent = new Intent(context, MediaActivity.class);
        intent.putExtra(AliyunSnapVideoParam.VIDEO_RESOLUTION, param.getResolutionMode());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_RATIO, param.getRatioMode());
        intent.putExtra(AliyunSnapVideoParam.NEED_RECORD, param.isNeedRecord());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_QUALITY, param.getVideoQuality());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_GOP, param.getGop());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_FRAMERATE, param.getFrameRate());
        intent.putExtra(CROP_MODE, param.getScaleMode());
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
        context.startActivity(intent);
    }

    public static final String getVersion() {
        return Version.VERSION;
    }

    private void initView() {
        OUT_STROKE_WIDTH = DensityUtil.dip2px(this, 5);
        transFormBtn = (ImageView) findViewById(R.id.aliyun_transform);
        transFormBtn.setOnClickListener(this);
        nextBtn = (ImageView) findViewById(R.id.aliyun_next);
        nextBtn.setOnClickListener(this);
        cancelBtn = (ImageView) findViewById(R.id.aliyun_back);
        cancelBtn.setOnClickListener(this);
        mCropProgressBg = (FrameLayout) findViewById(R.id.aliyun_crop_progress_bg);
        mCropProgressBg.setVisibility(View.GONE);
        mCropProgress = (FanProgressBar) findViewById(R.id.aliyun_crop_progress);
        mCropProgress.setOutRadius(DensityUtil.dip2px(this, 40) / 2 - OUT_STROKE_WIDTH / 2);
        mCropProgress.setOffset(OUT_STROKE_WIDTH / 2, OUT_STROKE_WIDTH / 2);
        mCropProgress.setOutStrokeWidth(OUT_STROKE_WIDTH);
    }


    public void initSurface() {
        frame = (VideoTrimFrameLayout) findViewById(R.id.aliyun_video_surfaceLayout);
        frame.setOnSizeChangedListener(this);
        frame.setOnScrollCallBack(this);
        resizeFrame();
        mImageView = (ImageView) findViewById(R.id.aliyun_image_view);
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
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if(msc != null) {
//            msc.disconnect();
//            msc = null;
//        }
        AliyunCropCreator.destroyCropInstance();
    }

    private void scaleCrop(int imageWidth, int imageHeight) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mImageView.getLayoutParams();
        int s = Math.min(imageWidth, imageHeight);
        int b = Math.max(imageWidth, imageHeight);
        float imageRatio = (float) b / s;
        float ratio = 1f;
        switch (ratioMode) {
            case CropKey.RATIO_MODE_1_1:
                ratio = 1f;
                break;
            case CropKey.RATIO_MODE_3_4:
                ratio = (float) 4 / 3;
                break;
            case CropKey.RATIO_MODE_9_16:
                ratio = (float) 16 / 9;
                break;
        }
        if (imageWidth > imageHeight) {
            layoutParams.width = frameWidth;
            layoutParams.height = frameWidth * imageHeight / imageWidth;
        } else {
            if (imageRatio <= ratio) {
                layoutParams.height = frameHeight;
                layoutParams.width = frameHeight * imageWidth / imageHeight;
            } else {
                layoutParams.width = frameWidth;
                layoutParams.height = frameWidth * imageHeight / imageWidth;
            }
        }
        layoutParams.setMargins(0, 0, 0, 0);
        mImageView.setLayoutParams(layoutParams);
        cropMode = SCALE_CROP;
        transFormBtn.setActivated(false);
        resetScroll();
    }

    private void scaleFill(int imageWidth, int imageHeight) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mImageView.getLayoutParams();
        int s = Math.min(imageWidth, imageHeight);
        int b = Math.max(imageWidth, imageHeight);
        float imageRatio = (float) b / s;
        float ratio = 1f;
        switch (ratioMode) {
            case CropKey.RATIO_MODE_1_1:
                ratio = 1f;
                break;
            case CropKey.RATIO_MODE_3_4:
                ratio = (float) 4 / 3;
                break;
            case CropKey.RATIO_MODE_9_16:
                ratio = (float) 16 / 9;
                break;
        }
        if (imageWidth > imageHeight) {
            layoutParams.height = frameHeight;
            layoutParams.width = frameHeight * imageWidth / imageHeight;
        } else {
            if (imageRatio <= ratio) {
                layoutParams.width = frameWidth;
                layoutParams.height = frameWidth * imageHeight / imageWidth;
            } else {
                layoutParams.height = frameHeight;
                layoutParams.width = frameHeight * imageWidth / imageHeight;

            }
        }
        layoutParams.setMargins(0, 0, 0, 0);
        mImageView.setLayoutParams(layoutParams);
        cropMode = SCALE_FILL;
        transFormBtn.setActivated(true);
        resetScroll();
    }


    private void scanFile() {
        MediaScannerConnection.scanFile(getApplicationContext(),
                new String[]{outputPath},
                new String[]{mMimeType}, null);
//        if(msc != null && msc.isConnected()) {
//            msc.scanFile(outputPath, mMimeType);
//        }
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
    public void onSizeChanged(View view, int w, int h, int oldw, int oldh) {

    }

    @Override
    public void onVideoScroll(float distanceX, float distanceY) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mImageView.getLayoutParams();
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

        mImageView.setLayoutParams(lp);
    }

    @Override
    public void onVideoSingleTapUp() {
    }

    @Override
    public void onClick(View v) {
        if (v == transFormBtn) {
            if (isCropping) {
                return;
            }
            if (cropMode == SCALE_FILL) {
                scaleCrop(mImageWidth, mImageHeight);
            } else if (cropMode == SCALE_CROP) {
                scaleFill(mImageWidth, mImageHeight);

            }
        } else if (v == nextBtn) {
            startCrop();
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
        int posX;
        int posY;
        int outputWidth = 0;
        int outputHeight = 0;
        int cropWidth;
        int cropHeight;
        outputPath = Environment.getExternalStorageDirectory()
                + File.separator
                + Environment.DIRECTORY_DCIM
                + File.separator + "crop_"
                + System.currentTimeMillis() + mSuffix;
        float videoRatio = (float) mImageHeight / mImageWidth;
        float outputRatio = 1f;
        switch (ratioMode) {
            case CropKey.RATIO_MODE_1_1:
                outputRatio = 1f;
                break;
            case CropKey.RATIO_MODE_3_4:
                outputRatio = (float) 4 / 3;
                break;
            case CropKey.RATIO_MODE_9_16:
                outputRatio = (float) 16 / 9;
                break;
        }
        if (videoRatio > outputRatio) {
            posX = 0;
            posY = ((mImageView.getMeasuredHeight() - frameHeight) / 2 + mScrollY) * mImageWidth / frameWidth;
            switch (resolutionMode) {
                case CropKey.RESOLUTION_360P:
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
            cropWidth = mImageWidth;
            cropHeight = 0;
            switch (ratioMode) {
                case AliyunSnapVideoParam.RATIO_MODE_1_1:
                    cropHeight = mImageWidth;
                    outputHeight = outputWidth;
                    break;
                case AliyunSnapVideoParam.RATIO_MODE_3_4:
                    cropHeight = mImageWidth * 4 / 3;
                    outputHeight = outputWidth * 4 / 3;
                    break;
                case AliyunSnapVideoParam.RATIO_MODE_9_16:
                    cropHeight = mImageWidth * 16 / 9;
                    outputHeight = outputWidth * 16 / 9;
                    break;
            }
        } else {
            posX = ((mImageView.getMeasuredWidth() - frameWidth) / 2 + mScrollX) * mImageHeight / frameHeight;
            posY = 0;
            switch (resolutionMode) {
                case CropKey.RESOLUTION_360P:
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
            cropHeight = mImageHeight;
            switch (ratioMode) {
                case AliyunSnapVideoParam.RATIO_MODE_1_1:
                    cropWidth = mImageHeight;
                    outputHeight = outputWidth;
                    break;
                case AliyunSnapVideoParam.RATIO_MODE_3_4:
                    cropWidth = mImageHeight * 3 / 4;
                    outputHeight = outputWidth * 4 / 3;
                    break;
                case AliyunSnapVideoParam.RATIO_MODE_9_16:
                    cropWidth = mImageHeight * 9 / 16;
                    outputHeight = outputWidth * 16 / 9;
                    break;
            }
        }
        CropParam cropParam = new CropParam();
        cropParam.setOutputPath(outputPath);
        cropParam.setInputPath(path);
        cropParam.setOutputWidth(outputWidth);
        cropParam.setOutputHeight(outputHeight);
        cropParam.setMediaType(MediaType.ANY_IMAGE_TYPE);
        Rect cropRect = new Rect(posX, posY, posX + cropWidth, posY + cropHeight);
        cropParam.setCropRect(cropRect);
        cropParam.setScaleMode(cropMode);
        cropParam.setFrameRate(frameRate);
        cropParam.setGop(gop);
        cropParam.setQuality(quality);
        cropParam.setFillColor(Color.BLACK);

        mCropProgressBg.setVisibility(View.VISIBLE);
        crop.setCropParam(cropParam);
        isCropping = true;
        crop.startCrop();
        mCropProgressBg.setVisibility(View.GONE);
        isCropping = false;


        scanFile();
        Intent intent = getIntent();
        intent.putExtra(RESULT_KEY_CROP_PATH, outputPath);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCropProgressBg.setVisibility(View.GONE);
                switch (code) {
                    case AliyunErrorCode.ERROR_MEDIA_NOT_SUPPORTED_VIDEO:
                        ToastUtil.showToast(AliyunImageCrop.this, R.string.aliyun_video_crop_error);
                        break;
                    case AliyunErrorCode.ERROR_MEDIA_NOT_SUPPORTED_AUDIO:
                        ToastUtil.showToast(AliyunImageCrop.this, R.string.aliyun_not_supported_audio);
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCropProgress.setVisibility(View.GONE);
                mCropProgressBg.setVisibility(View.GONE);
                scanFile();
                Intent intent = getIntent();
                intent.putExtra(RESULT_KEY_CROP_PATH, outputPath);
//                intent.putExtra(RESULT_KEY_DURATION, (endTime - startTime) / 1000);
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
            }
        });
        deleteFile();
        setResult(Activity.RESULT_CANCELED);
        finish();
        isCropping = false;
    }

}