/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.crop;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.demo.crop.media.GalleryDirChooser;
import com.aliyun.demo.crop.media.GalleryMediaChooser;
import com.aliyun.demo.crop.media.MediaDir;
import com.aliyun.demo.crop.media.MediaInfo;
import com.aliyun.demo.crop.media.MediaStorage;
import com.aliyun.demo.crop.media.ThumbnailGenerator;
import com.aliyun.jasonparse.JSONSupportImpl;
import com.aliyun.svideo.sdk.external.struct.common.CropKey;
import com.aliyun.svideo.sdk.external.struct.common.VideoDisplayMode;
import com.aliyun.svideo.sdk.external.struct.common.VideoQuality;
import com.aliyun.svideo.sdk.external.struct.encoder.VideoCodecs;
import com.aliyun.svideo.sdk.external.struct.recorder.CameraType;
import com.aliyun.svideo.sdk.external.struct.recorder.FlashType;
import com.aliyun.svideo.sdk.external.struct.snap.AliyunSnapVideoParam;
import com.aliyun.video.common.utils.FastClickUtil;

/**
 * 裁剪模块的media选择Activity
 */
public class MediaActivity extends Activity implements View.OnClickListener {
    private MediaStorage storage;
    private GalleryDirChooser galleryDirChooser;
    private ThumbnailGenerator thumbnailGenerator;
    private GalleryMediaChooser galleryMediaChooser;
    private RecyclerView galleryView;
    private EditText mEtVideoPath;
    private ImageButton back;
    private TextView title;
    private int resolutionMode;
    private int ratioMode;
    private VideoDisplayMode cropMode = VideoDisplayMode.FILL;
    private int frameRate;
    private int gop;
    private int mBitrate;
    private boolean needRecord;
    private int minVideoDuration;
    private int maxVideoDuration;
    private int minCropDuration;
    private VideoQuality quality = VideoQuality.SSD;
    private VideoCodecs mVideoCodec = VideoCodecs.H264_HARDWARE;
    private static final int CROP_CODE = 3001;
    private static final int RECORD_CODE = 3002;

    public static final String RESULT_TYPE = "result_type";
    public static final int RESULT_TYPE_CROP = 4001;
    public static final int RESULT_TYPE_RECORD = 4002;

    /**
     * 录制参数
     */
    private int recordMode = AliyunSnapVideoParam.RECORD_MODE_AUTO;
    private String[] filterList;
    private int beautyLevel = 80;
    private boolean beautyStatus = true;
    private CameraType cameraType = CameraType.FRONT;
    private FlashType flashType = FlashType.ON;
    private int maxDuration = 30000;
    private int minDuration = 2000;
    private boolean needClip = true;
    private int sortMode = MediaStorage.SORT_MODE_MERGE;
    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.aliyun_svideo_activity_media);
        getData();
        init();
    }

    private void getData() {
        resolutionMode = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_RESOLUTION, CropKey.RESOLUTION_720P);
        cropMode = (VideoDisplayMode) getIntent().getSerializableExtra(AliyunSnapVideoParam.CROP_MODE);
        frameRate = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_FRAMERATE, 30);
        gop = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_GOP, 250);
        mBitrate = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_BITRATE, 0);
        quality = (VideoQuality) getIntent().getSerializableExtra(AliyunSnapVideoParam.VIDEO_QUALITY);
        mVideoCodec = (VideoCodecs) getIntent().getSerializableExtra(AliyunSnapVideoParam.VIDEO_CODEC);
        ratioMode = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_RATIO, CropKey.RATIO_MODE_9_16);
        needRecord = getIntent().getBooleanExtra(AliyunSnapVideoParam.NEED_RECORD, true);
        minVideoDuration = getIntent().getIntExtra(AliyunSnapVideoParam.MIN_VIDEO_DURATION, 2000);
        maxVideoDuration = getIntent().getIntExtra(AliyunSnapVideoParam.MAX_VIDEO_DURATION, 10 * 60 * 1000);
        minCropDuration = getIntent().getIntExtra(AliyunSnapVideoParam.MIN_CROP_DURATION, 2000);
        recordMode = getIntent().getIntExtra(AliyunSnapVideoParam.RECORD_MODE, AliyunSnapVideoParam.RECORD_MODE_AUTO);
        filterList = getIntent().getStringArrayExtra(AliyunSnapVideoParam.FILTER_LIST);
        beautyLevel = getIntent().getIntExtra(AliyunSnapVideoParam.BEAUTY_LEVEL, 80);
        beautyStatus = getIntent().getBooleanExtra(AliyunSnapVideoParam.BEAUTY_STATUS, true);
        cameraType = (CameraType) getIntent().getSerializableExtra(AliyunSnapVideoParam.CAMERA_TYPE);
        if (cameraType == null) {
            cameraType = CameraType.FRONT;
        }
        flashType = (FlashType) getIntent().getSerializableExtra(AliyunSnapVideoParam.FLASH_TYPE);
        if (flashType == null) {
            flashType = FlashType.ON;
        }
        minDuration = getIntent().getIntExtra(AliyunSnapVideoParam.MIN_DURATION, 2000);
        maxDuration = getIntent().getIntExtra(AliyunSnapVideoParam.MAX_DURATION, 30000);
        needClip = getIntent().getBooleanExtra(AliyunSnapVideoParam.NEED_CLIP, true);
        sortMode = getIntent().getIntExtra(AliyunSnapVideoParam.SORT_MODE, MediaStorage.SORT_MODE_MERGE);

    }
    private void init() {
        galleryView = (RecyclerView) findViewById(R.id.aliyun_gallery_media);
        title = (TextView) findViewById(R.id.aliyun_gallery_title);
        title.setText(R.string.aliyun_gallery_all_media);
        back = (ImageButton) findViewById(R.id.aliyun_gallery_closeBtn);
        mEtVideoPath = (EditText) findViewById(R.id.et_video_path);
        back.setOnClickListener(this);
        storage = new MediaStorage(this, new JSONSupportImpl());
        thumbnailGenerator = new ThumbnailGenerator(this);
        galleryDirChooser = new GalleryDirChooser(this, findViewById(R.id.aliyun_topPanel), thumbnailGenerator, storage);
        galleryMediaChooser = new GalleryMediaChooser(galleryView, galleryDirChooser, storage, thumbnailGenerator, needRecord);
        storage.setSortMode(sortMode);
        storage.setVideoDurationRange(minVideoDuration, maxVideoDuration);
        storage.startFetchmedias();
        storage.setOnMediaDirChangeListener(new MediaStorage.OnMediaDirChange() {
            @Override
            public void onMediaDirChanged() {
                MediaDir dir = storage.getCurrentDir();
                if (dir == null) {
                    return;
                }
                if (dir.id == -1) {
                    title.setText(getString(R.string.aliyun_gallery_all_media));
                } else {
                    title.setText(dir.dirName);
                }
                galleryMediaChooser.changeMediaDir(dir);
            }
        });
        storage.setOnCurrentMediaInfoChangeListener(new MediaStorage.OnCurrentMediaInfoChange() {
            @Override
            public void onCurrentMediaInfoChanged(MediaInfo info) {
                if (FastClickUtil.isFastClickActivity(MediaActivity.class.getSimpleName())) {
                    return;
                }
                if (info == null) {

                    Class recorder = null;
                    try {
                        recorder = Class.forName("com.aliyun.demo.recorder.AliyunVideoRecorder");
                    } catch (ClassNotFoundException e) {
                        Log.e(TAG, "ClassNotFoundException: com.aliyun.demo.recorder.AliyunVideoRecorder");
                    }
                    if (recorder == null) {
                        return;
                    }
                    Intent intent = new Intent(MediaActivity.this, recorder);
                    intent.putExtra(AliyunSnapVideoParam.VIDEO_RESOLUTION, resolutionMode);
                    intent.putExtra(AliyunSnapVideoParam.VIDEO_RATIO, ratioMode);
                    intent.putExtra(AliyunSnapVideoParam.RECORD_MODE, recordMode);
                    intent.putExtra(AliyunSnapVideoParam.FILTER_LIST, filterList);
                    intent.putExtra(AliyunSnapVideoParam.BEAUTY_LEVEL, beautyLevel);
                    intent.putExtra(AliyunSnapVideoParam.BEAUTY_STATUS, beautyStatus);
                    intent.putExtra(AliyunSnapVideoParam.CAMERA_TYPE, cameraType);
                    intent.putExtra(AliyunSnapVideoParam.FLASH_TYPE, flashType);
                    intent.putExtra(AliyunSnapVideoParam.NEED_CLIP, needClip);
                    intent.putExtra(AliyunSnapVideoParam.MAX_DURATION, maxDuration);
                    intent.putExtra(AliyunSnapVideoParam.MIN_DURATION, minDuration);
                    intent.putExtra(AliyunSnapVideoParam.VIDEO_QUALITY, quality);
                    intent.putExtra(AliyunSnapVideoParam.VIDEO_GOP, gop);
                    intent.putExtra(AliyunSnapVideoParam.VIDEO_BITRATE, mBitrate);
                    intent.putExtra(AliyunSnapVideoParam.VIDEO_CODEC, mVideoCodec);
                    intent.putExtra(AliyunSnapVideoParam.CROP_USE_GPU, getIntent().getBooleanExtra(AliyunSnapVideoParam.CROP_USE_GPU, false));
                    intent.putExtra("need_gallery", false);
                    startActivityForResult(intent, RECORD_CODE);
                } else {
                    String mediaPath = null;
                    if (mEtVideoPath.getVisibility() == View.VISIBLE) {
                        //for test
                        mediaPath = mEtVideoPath.getText().toString();
                        if (TextUtils.isEmpty(mediaPath)) {
                            mediaPath = info.filePath;
                        }
                    } else {
                        mediaPath = info.filePath;
                    }
                    if (info.filePath.endsWith("gif") || info.filePath.endsWith("gif")) {
                        Toast.makeText(MediaActivity.this, R.string.aliyun_crop_gif_not_support, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (info.mimeType.startsWith("image")) {
                        Intent intent = new Intent(MediaActivity.this, AliyunImageCropActivity.class);
                        intent.putExtra(CropKey.VIDEO_PATH, mediaPath);
                        intent.putExtra(AliyunSnapVideoParam.VIDEO_RESOLUTION, resolutionMode);
                        intent.putExtra(AliyunSnapVideoParam.CROP_MODE, cropMode);
                        intent.putExtra(AliyunSnapVideoParam.VIDEO_QUALITY, quality);
                        intent.putExtra(AliyunSnapVideoParam.VIDEO_GOP, gop);
                        intent.putExtra(AliyunSnapVideoParam.VIDEO_BITRATE, mBitrate);
                        intent.putExtra(AliyunSnapVideoParam.VIDEO_FRAMERATE, frameRate);
                        intent.putExtra(AliyunSnapVideoParam.VIDEO_RATIO, ratioMode);
                        intent.putExtra(AliyunSnapVideoParam.MIN_CROP_DURATION, minCropDuration);
                        intent.putExtra(AliyunSnapVideoParam.VIDEO_CODEC, mVideoCodec);
                        startActivityForResult(intent, CROP_CODE);
                    } else {
                        Intent intent = new Intent(MediaActivity.this, AliyunVideoCropActivity.class);
                        intent.putExtra(CropKey.VIDEO_PATH, mediaPath);
                        intent.putExtra(AliyunSnapVideoParam.VIDEO_RESOLUTION, resolutionMode);
                        intent.putExtra(AliyunSnapVideoParam.CROP_MODE, cropMode);
                        intent.putExtra(AliyunSnapVideoParam.VIDEO_QUALITY, quality);
                        intent.putExtra(AliyunSnapVideoParam.VIDEO_GOP, gop);
                        intent.putExtra(AliyunSnapVideoParam.VIDEO_BITRATE, mBitrate);
                        intent.putExtra(AliyunSnapVideoParam.VIDEO_FRAMERATE, frameRate);
                        intent.putExtra(AliyunSnapVideoParam.VIDEO_RATIO, ratioMode);
                        intent.putExtra(AliyunSnapVideoParam.MIN_CROP_DURATION, minCropDuration);
                        intent.putExtra(CropKey.ACTION, CropKey.ACTION_TRANSCODE);
                        intent.putExtra(AliyunSnapVideoParam.VIDEO_CODEC, mVideoCodec);
                        intent.putExtra(AliyunSnapVideoParam.CROP_USE_GPU, getIntent().getBooleanExtra(AliyunSnapVideoParam.CROP_USE_GPU, false));
                        startActivityForResult(intent, CROP_CODE);
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode ==  CROP_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    data.putExtra(RESULT_TYPE, RESULT_TYPE_CROP);
                }
                setResult(Activity.RESULT_OK, data);
                finish();
            } else if (resultCode ==  Activity.RESULT_CANCELED) {
                setResult(Activity.RESULT_CANCELED);
            }
        } else if (requestCode == RECORD_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    data.putExtra(RESULT_TYPE, RESULT_TYPE_RECORD);
                }
                setResult(Activity.RESULT_OK, data);
                finish();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                setResult(Activity.RESULT_CANCELED);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        storage.saveCurrentDirToCache();
        storage.cancelTask();
        thumbnailGenerator.cancelAllTask();
    }

    @Override
    public void onClick(View v) {
        if (v ==  back) {
            finish();
        }
    }
}
