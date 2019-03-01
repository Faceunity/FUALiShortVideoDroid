/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.recorder.activity;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.common.global.AppInfo;
import com.aliyun.common.logger.Logger;
import com.aliyun.common.utils.CommonUtil;
import com.aliyun.common.utils.DensityUtil;
import com.aliyun.common.utils.MySystemParams;
import com.aliyun.common.utils.StorageUtils;
import com.aliyun.common.utils.ToastUtil;
import com.aliyun.demo.R;
import com.aliyun.demo.recorder.MVChooser;
import com.aliyun.demo.recorder.MusicActivity;
import com.aliyun.demo.recorder.MvForm;
import com.aliyun.demo.recorder.PasterAdapter;
import com.aliyun.demo.recorder.VideoPlayActivity;
import com.aliyun.demo.recorder.util.Common;
import com.aliyun.demo.recorder.util.MVResourceUtil;
import com.aliyun.demo.recorder.util.OrientationDetector;
import com.aliyun.downloader.DownloaderManager;
import com.aliyun.downloader.FileDownloaderCallback;
import com.aliyun.downloader.FileDownloaderModel;
import com.aliyun.downloader.zipprocessor.DownloadFileUtils;
import com.aliyun.jasonparse.JSONSupportImpl;
import com.aliyun.querrorcode.AliyunErrorCode;
import com.aliyun.qupaiokhttp.HttpRequest;
import com.aliyun.qupaiokhttp.StringHttpRequestCallback;
import com.aliyun.svideo.base.widget.CenterLayoutManager;
import com.aliyun.svideo.base.widget.CircleProgressBar;
import com.aliyun.svideo.base.widget.RecordTimelineView;
import com.aliyun.recorder.AliyunRecorderCreator;
import com.aliyun.recorder.supply.AliyunIClipManager;
import com.aliyun.recorder.supply.AliyunIRecorder;
import com.aliyun.recorder.supply.EncoderInfoCallback;
import com.aliyun.recorder.supply.RecordCallback;
import com.aliyun.svideo.sdk.external.struct.common.AliyunVideoParam;
import com.aliyun.svideo.sdk.external.struct.common.VideoDisplayMode;
import com.aliyun.svideo.sdk.external.struct.common.VideoQuality;
import com.aliyun.svideo.sdk.external.struct.effect.EffectBean;
import com.aliyun.svideo.sdk.external.struct.effect.EffectFilter;
import com.aliyun.svideo.sdk.external.struct.effect.EffectPaster;
import com.aliyun.svideo.sdk.external.struct.encoder.EncoderInfo;
import com.aliyun.svideo.sdk.external.struct.encoder.VideoCodecs;
import com.aliyun.svideo.sdk.external.struct.form.IMVForm;
import com.aliyun.svideo.sdk.external.struct.form.PreviewPasterForm;
import com.aliyun.svideo.sdk.external.struct.form.PreviewResourceForm;
import com.aliyun.svideo.sdk.external.struct.recorder.CameraType;
import com.aliyun.svideo.sdk.external.struct.recorder.FlashType;
import com.aliyun.svideo.sdk.external.struct.recorder.MediaInfo;
import com.aliyun.svideo.sdk.external.struct.snap.AliyunSnapVideoParam;
import com.google.gson.reflect.TypeToken;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.qu.preview.callback.OnFrameCallBack;
import com.qu.preview.callback.OnTextureIdCallBack;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 视频录制界面
 *      原魔法相机CameraDemo
 */
@Deprecated
public class AlivcRecorderActivity extends Activity implements View.OnClickListener, GestureDetector.OnGestureListener,
    View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener {
    private static final String TAG = "AlivcRecorderActivity";
    private static final String LOCAL_SETTING_NAME = "sdk_record_download_paster";
    public static final String NEED_GALLERY = "need_gallery";


    public static final int TYPE_FILTER = 1;
    public static final int TYPE_MV = 2;
    public static final int TYPE_MUSIC = 3;

    private GLSurfaceView glSurfaceView;
    private ImageView switchCameraBtn, switchLightBtn, backBtn, musicBtn, compeleteBtn;
    private TextView recordDurationTxt, filterTxt, rateVerySlowTxt, rateSlowTxt,
            rateStandardTxt, rateFastTxt, rateVeryFastTxt, tipTxt;
    private View magicMusic, recordLayoutBottom;
    private TextView deleteBtn;
    private AliyunIRecorder recorder;
    private FlashType flashType = FlashType.OFF;
    private CameraType cameraType = CameraType.FRONT;
    private RecyclerView pasterView;
    //    private FanProgressBar fanProgressBar;
    private FrameLayout recordBg, waitingLayout;
    private LinearLayout rateBar;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private PreviewPasterForm currentPasterForm;
    private RecordTimelineView recordTimelineView;
    private LinearSnapHelper linearSnapHelper;
    private LinearLayoutManager linearLayoutManager;
    private static int TEST_VIDEO_WIDTH = 540;
    private static int TEST_VIDEO_HEIGHT = 960;
    private static final int MAX_ITEM_COUNT = 5;
    private static final int MIN_RECORD_TIME = 500;
    private static final int MAX_RECORD_TIME = 15 * 1000;
    private static final int MAX_SWITCH_VELOCITY = 2000;

    private static final float FADE_IN_START_ALPHA = 0.3f;
    private static final int FILTER_ANIMATION_DURATION = 1000;

    private static final int REQUEST_CODE_MUSIC = 2001;
    private static final int REQUEST_CODE_PLAY = 2002;

    public static final String MUSIC_PATH = "mucenter_verticalsic_path";
    public static final String MUSIC_START_TIME = "music_start_time";
    public static final String MUSIC_MAX_RECORD_TIME = "music_max_record_time";

    private static int OUT_STROKE_WIDTH;

    private CircleProgressBar progressBar;
    private int itemWidth;
    private EffectPaster effect;
    private int filterIndex = 0;
    private String videoPath;
    private int recordTime;
    private int beautyLevel = 80;
    private boolean recordStopped = true;
    private float lastScaleFactor;
    private float scaleFactor;

    private boolean isOpenFailed;

    private boolean isFaceDetectOpen;

    private boolean isRecording;

    long downTime = 0;

    private AliyunIClipManager clipManager;

    private OrientationDetector orientationDetector;
    private int rotation;
    private boolean isMaxDuration;

    private int mSelectedMvPos;
    private List<IMVForm> imvForms;
    private int mGlSurfaceWidth, mGlSurfaceHeight;
    private List<PreviewPasterForm> resources = new ArrayList<>();
    String[] mEffDirs;
    //    private EffectFilter mCurrEffectFilter;
    private LinkedHashMap<Object, Integer> mConflictEffects = new LinkedHashMap<>();
    private EffectBean mCurrMv;
    private EffectFilter mCurrFilter;

    /** 上个页面传参 */
    private int mResolutionMode;
    private int mMinDuration;
    private int mMaxDuration;
    private int mGop;
    private int mBitrate;
    private VideoQuality mVideoQuality = VideoQuality.HD;
    private VideoCodecs mVideoCodec = VideoCodecs.H264_HARDWARE;
    private int mRatioMode = AliyunSnapVideoParam.RATIO_MODE_3_4;
    private int mSortMode = AliyunSnapVideoParam.SORT_MODE_MERGE;
    private boolean isNeedClip;
    private boolean isNeedGallery;
    private AliyunVideoParam mVideoParam;
    private int mFrame = 25;
    private VideoDisplayMode mCropMode = VideoDisplayMode.SCALE;
    private int mMinCropDuration = 2000;
    private int mMaxVideoDuration = 10000;
    private int mMinVideoDuration = 2000;
    private int mGalleryVisibility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MySystemParams.getInstance().init(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.aliyun_svideo_activity_camera_demo);
        getData();
        initOritationDetector();
        initView();
        initSDK();
        initAssetPath();
        copyAssets();
        final FrameLayout parent = (FrameLayout) findViewById(R.id.parent);

    }

    private void initAssetPath() {
        String path = StorageUtils.getCacheDirectory(this).getAbsolutePath() + File.separator + Common.QU_NAME + File.separator;
        File filter = new File(new File(path), "filter");

        String[] list = filter.list();
        if (list == null || list.length == 0) {
            return;
        }
        mEffDirs = new String[list.length + 1];
        mEffDirs[0] = null;
        for (int i = 0; i < list.length; i++) {
            mEffDirs[i + 1] = filter.getPath() + "/" + list[i];
        }
    }

    private void initOritationDetector() {
        orientationDetector = new OrientationDetector(getApplicationContext());
        orientationDetector.setOrientationChangedListener(new OrientationDetector.OrientationChangedListener() {
            @Override
            public void onOrientationChanged() {
                rotation = getPictureRotation();

                recorder.setRotation(rotation);
            }
        });
    }

    /**
     * 获取上个页面的传参
     */
    private void getData() {

        mResolutionMode = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_RESOLUTION, AliyunSnapVideoParam.RESOLUTION_540P);
        mMinDuration = getIntent().getIntExtra(AliyunSnapVideoParam.MIN_DURATION, 2000);
        mMaxDuration = getIntent().getIntExtra(AliyunSnapVideoParam.MAX_DURATION, 30000);
        mRatioMode = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_RATIO, AliyunSnapVideoParam.RATIO_MODE_3_4);
        mGop = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_GOP, 5);
        mBitrate = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_BITRATE, 0);
        mVideoQuality = (VideoQuality) getIntent().getSerializableExtra(AliyunSnapVideoParam.VIDEO_QUALITY);
        if (mVideoQuality == null) {
            mVideoQuality = VideoQuality.HD;
        }
        mVideoCodec = (VideoCodecs) getIntent().getSerializableExtra(AliyunSnapVideoParam.VIDEO_CODEC);
        if (mVideoCodec == null) {
            mVideoCodec = VideoCodecs.H264_HARDWARE;
        }
        isNeedClip = getIntent().getBooleanExtra(AliyunSnapVideoParam.NEED_CLIP, true);
        isNeedGallery = getIntent().getBooleanExtra(NEED_GALLERY, true) && mGalleryVisibility == 0;
        mVideoParam = new AliyunVideoParam.Builder()
        .gop(mGop)
        .bitrate(mBitrate)
        .frameRate(25)
        .videoQuality(mVideoQuality)
        .videoCodec(mVideoCodec)
        .build();

        /**
         * 裁剪参数
         */
        mFrame = getIntent().getIntExtra(AliyunSnapVideoParam.VIDEO_FRAMERATE, 25);
        mCropMode = (VideoDisplayMode) getIntent().getSerializableExtra(AliyunSnapVideoParam.CROP_MODE);
        if (mCropMode == null) {
            mCropMode = VideoDisplayMode.SCALE;
        }
        mMinCropDuration = getIntent().getIntExtra(AliyunSnapVideoParam.MIN_CROP_DURATION, 2000);
        mMinVideoDuration = getIntent().getIntExtra(AliyunSnapVideoParam.MIN_VIDEO_DURATION, 2000);
        mMaxVideoDuration = getIntent().getIntExtra(AliyunSnapVideoParam.MAX_VIDEO_DURATION, 10000);
        mSortMode = getIntent().getIntExtra(AliyunSnapVideoParam.SORT_MODE, AliyunSnapVideoParam.SORT_MODE_MERGE);

    }


    private void initSDK() {
        recorder = AliyunRecorderCreator.getRecorderInstance(this);
        recorder.setDisplayView(glSurfaceView);
        clipManager = recorder.getClipManager();
        clipManager.setMaxDuration(MAX_RECORD_TIME);
        clipManager.setMinDuration(MIN_RECORD_TIME);
        MediaInfo mediaInfo = new MediaInfo();
        mediaInfo.setVideoWidth(TEST_VIDEO_WIDTH);
        mediaInfo.setVideoHeight(TEST_VIDEO_HEIGHT);
        //mediaInfo.setHWAutoSize(true);//硬编时自适应宽高为16的倍数
        recorder.setMediaInfo(mediaInfo);
        cameraType = recorder.getCameraCount() == 1 ? CameraType.BACK : cameraType;
        recorder.setCamera(cameraType);
        recorder.setBeautyLevel(beautyLevel);
        recorder.needFaceTrackInternal(true);
        recorder.setOnFrameCallback(new OnFrameCallBack() {
            @Override
            public void onFrameBack(byte[] bytes, int width, int height, Camera.CameraInfo info) {

                if (!isFaceDetectOpen) {
                    return;
                }

                int orient;
                if (Camera.CameraInfo.CAMERA_FACING_FRONT == info.facing) {
                    orient = (info.orientation + (rotation - 270) + 360) % 360;
                } else {
                    orient = (info.orientation + (rotation - 90) + 360) % 360;
                }

                float[][] point = new float[0][6];
            }

            @Override
            public Camera.Size onChoosePreviewSize(List<Camera.Size> supportedPreviewSizes,
                                                   Camera.Size preferredPreviewSizeForVideo) {
                return null;
            }

            @Override
            public void openFailed() {
                isOpenFailed = true;
            }
        });

        recorder.setRecordCallback(new RecordCallback() {

            @Override
            public void onComplete(boolean validClip, long clipDuration) {
                Log.d("EncoderInputManager", "call onComplete isValid " + validClip);
                handleStopCallback(validClip, clipDuration);

                if (isMaxDuration) {
                    isMaxDuration = false;
                    finishRecording();
                }
            }

            @Override
            public void onFinish(String outputPath) {
                Intent intent = new Intent(AlivcRecorderActivity.this, VideoPlayActivity.class);
                intent.putExtra(VideoPlayActivity.VIDEO_PATH, videoPath);
                startActivityForResult(intent, REQUEST_CODE_PLAY);
            }

            @Override
            public void onProgress(final long duration) {
                Log.d("AlivcRecorderActivity", "duration..." + duration);
                recordTime = (int) duration + clipManager.getDuration();
                recordTimelineView.setDuration((int) duration);
                if (recordTime >= clipManager.getMinDuration()) {
                    if (!compeleteBtn.isSelected()) {
                        compeleteBtn.setSelected(true);
                    }
                }
                if (recordStopped) {
                    return;
                }
                String result = String.format("%.2f", recordTime / 1000f);
                recordDurationTxt.setText(result);
            }

            @Override
            public void onMaxDuration() {
                isMaxDuration = true;
                compeleteBtn.setEnabled(false);
                handleRecordStop();
            }

            @Override
            public void onError(int errorCode) {
                if (errorCode == AliyunErrorCode.ERROR_LICENSE_FAILED) {
                    // 2017/2/17
                }
                Log.e(TAG, "errorCode..." + errorCode);
                recordTime = 0;
                handleStopCallback(false, 0);
            }

            @Override
            public void onInitReady() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        restoreConflictEffect();
                        if (effect != null) {
                            addEffectToRecord(effect.getPath());     //因为底层在onpause的时候会做资源回收，所以初始化完成的时候要做资源的恢复
                        }
                        String path = Common.QU_DIR + "maohuzi";
                        final EffectPaster paster = new EffectPaster(path);
                        paster.isTrack = false;
                        if (mEffDirs != null) {
                            EffectFilter effectFilter = new EffectFilter(mEffDirs[filterIndex]);
                            recorder.applyFilter(effectFilter);
                        }
                    }
                });
            }

            @Override
            public void onDrawReady() {

            }

            @Override
            public void onPictureBack(final Bitmap bitmap) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) findViewById(R.id.aliyun_test)).setImageBitmap(bitmap);
                    }
                });

            }

            @Override
            public void onPictureDataBack(final byte[] data) {
                File file = new File("/sdcard/test.jpeg");
                try {
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
                    outputStream.write(data);
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) findViewById(R.id.aliyun_test)).setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
                    }
                });
            }

        });

        recorder.setOnTextureIdCallback(new OnTextureIdCallBack() {
            @Override
            public int onTextureIdBack(int textureId, int textureWidth, int textureHeight, float[] matrix) {
                return textureId;

            }

            @Override
            public int onScaledIdBack(int scaledId, int textureWidth, int textureHeight, float[] matrix) {
                return scaledId;
            }

            @Override
            public void onTextureDestroyed() {

            }
        });
        recorder.setEncoderInfoCallback(new EncoderInfoCallback() {
            @Override
            public void onEncoderInfoBack(EncoderInfo info) {
                Log.d(TAG, info.toString());
            }
        });
        recorder.setFaceTrackInternalMaxFaceCount(2);
        switchLightBtnState();
        rateStandardTxt.performClick();
        recordTimelineView.setMaxDuration(clipManager.getMaxDuration());
        recordTimelineView.setMinDuration(clipManager.getMinDuration());
    }

    /**
     * 恢复冲突的特效，这些特效都是会彼此冲突的，比如滤镜和MV，因为MV中也有滤镜效果，所以MV和滤镜的添加顺序
     * 会影响最终产生视频的效果，在恢复时必须严格按照用户的操作顺序来恢复，
     * 这样就需要维护一个添加过的特效类的列表，然后按照列表顺序
     * 去恢复
     */
    private void restoreConflictEffect() {
        if (!mConflictEffects.isEmpty()) {
            for (Map.Entry<Object, Integer> entry : mConflictEffects.entrySet()) {
                switch (entry.getValue()) {
                case TYPE_FILTER:
                    recorder.applyFilter((EffectFilter) entry.getKey());
                    break;
                case TYPE_MV:
                    recorder.applyMv((EffectBean) entry.getKey());
                    break;
                case TYPE_MUSIC:
                    EffectBean music = (EffectBean) entry.getKey();
                    recorder.setMusic(music.getPath(), music.getStartTime(), music.getDuration());
                    break;
                default:
                    break;
                }
            }
        }
    }

    boolean isUseNative = true;


    private int getPictureRotation() {
        int orientation = orientationDetector.getOrientation();
        int rotation = 90;
        if ((orientation >= 45) && (orientation < 135)) {
            rotation = 180;
        }
        if ((orientation >= 135) && (orientation < 225)) {
            rotation = 270;
        }
        if ((orientation >= 225) && (orientation < 315)) {
            rotation = 0;
        }
        if (cameraType == CameraType.FRONT) {
            if (rotation != 0) {
                rotation = 360 - rotation;
            }
        }
        Log.d("MyOrientationDetector", "generated rotation ..." + rotation);
        return rotation;
    }

    private void initPasterResource() {
        if (CommonUtil.hasNetwork(this)) {
            initPasterResourceOnLine();
        } else {
            initPasterResourceLocal();
        }
    }

    private void addConstantPaster() {
        String path = Common.QU_DIR + "maohuzi";
        File file = new File(path);
        File iconFile = new File(path + "/icon.png");
        if (file.exists() && iconFile.exists()) {
            PreviewPasterForm form = new PreviewPasterForm();
            form.setUrl(file.getAbsolutePath());
            form.setIcon(file.getAbsolutePath() + File.separator + "icon.png");
            form.setLocalRes(true);
            resources.add(0, form);
        }

    }

    private void initPasterResourceLocal() {
        File aseetFile = StorageUtils.getFilesDirectory(this);
        File[] files = null;
        if (aseetFile.isDirectory()) {
            files = aseetFile.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.isDirectory() && pathname.getName().contains("-")) {
                        return true;
                    }
                    return false;
                }
            });
        }
        if (files == null) {
            return;
        }
        for (File file : files) {
            PreviewPasterForm form = new PreviewPasterForm();
            form.setIcon(file.getAbsolutePath() + File.separator + "icon.png");
            String fileName = file.getName();
            String[] strs = fileName.split("-");
            if (strs.length == 2) {
                String name = strs[0];
                String id = strs[1];
                form.setName(name);
                form.setUrl(getLocalResUrl(id));
                try {
                    form.setId(Integer.parseInt(id));
                    resources.add(form);
                } catch (Exception e) {
                    continue;
                }
            } else {
                continue;
            }
        }
        addConstantPaster();
        initPasterView();
    }
    /**
     * 素材分发服务为官方demo演示使用，无法达到商业化使用程度。请自行搭建相关的服务
     */
    private void initPasterResourceOnLine() {
        StringBuilder resUrl = new StringBuilder();
        resUrl.append(Common.BASE_URL).append("/api/res/prepose")
        .append("?packageName=")
        .append(getApplicationInfo().packageName)
        .append("&signature=")
        .append(AppInfo.getInstance().obtainAppSignature(getApplicationContext()));
        Logger.getDefaultLogger().d("pasterUrl url = " + resUrl.toString());
        HttpRequest.get(resUrl.toString(),
        new StringHttpRequestCallback() {
            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                JSONSupportImpl jsonSupport = new JSONSupportImpl();
                try {
                    List<PreviewResourceForm> resourceList = jsonSupport.readListValue(s,
                    new TypeToken<List<PreviewResourceForm>>() {
                    } .getType());
                    if (resourceList != null && resourceList.size() > 0) {
                        for (int i = 0; i < resourceList.size(); i++) {
                            resources.addAll(resourceList.get(i).getPasterList());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    initPasterResourceLocal();
                }
                addConstantPaster();
                initPasterView();
            }

            @Override
            public void onFailure(int errorCode, String msg) {
                super.onFailure(errorCode, msg);
                initPasterResourceLocal();
            }
        });
    }

    private void initPasterView() {
        if (resources != null) {
            fillItemBlank();
            PasterAdapter adapter = new PasterAdapter(AlivcRecorderActivity.this, resources, itemWidth);
            pasterView.setAdapter(adapter);
            adapter.setOnItemClickListener(new PasterAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    pasterView.smoothScrollToPosition(position);
                }
            });
            linearLayoutManager = new CenterLayoutManager(AlivcRecorderActivity.this, LinearLayoutManager.HORIZONTAL, false);
            pasterView.setLayoutManager(linearLayoutManager);
        }
    }

    private void copyAssets() {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                Common.copyAll(AlivcRecorderActivity.this);
                String path = StorageUtils.getCacheDirectory(AlivcRecorderActivity.this).getAbsolutePath() + File.separator + Common.QU_NAME + File.separator;
                recorder.setFaceTrackInternalModelPath(path + "/model");
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                waitingLayout.setVisibility(View.GONE);
                initPasterResource();
                initAssetPath();
            }
        } .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void fillItemBlank() {
        for (int i = 0; i < MAX_ITEM_COUNT / 2; i++) {
            resources.add(0, new PreviewPasterForm());
            resources.add(new PreviewPasterForm());
        }
        resources.add(0, new PreviewPasterForm());
    }

    private void initView() {
        OUT_STROKE_WIDTH = DensityUtil.dip2px(10);
        glSurfaceView = (GLSurfaceView) findViewById(R.id.aliyun_preview);
        glSurfaceView.setOnTouchListener(this);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) glSurfaceView.getLayoutParams();
        Rect rect = new Rect();
        getWindowManager().getDefaultDisplay().getRectSize(rect);
        layoutParams.width = rect.width();
        layoutParams.height = rect.height();
        glSurfaceView.setLayoutParams(layoutParams);
        switchCameraBtn = (ImageView) findViewById(R.id.aliyun_switch_camera);
        switchCameraBtn.setOnClickListener(this);
        switchLightBtn = (ImageView) findViewById(R.id.aliyun_switch_light);
        switchLightBtn.setOnClickListener(this);
        musicBtn = (ImageView) findViewById(R.id.aliyun_music);
        musicBtn.setOnClickListener(this);
        pasterView = (RecyclerView) findViewById(R.id.aliyun_pasterView);
        pasterView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                pasterView.setVisibility(View.INVISIBLE);
                pasterView.setVisibility(View.VISIBLE);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (effect != null) {
                        recorder.removePaster(effect);
                        effect = null;
                    }
                    View centerView = linearSnapHelper.findSnapView(linearLayoutManager);
                    if (centerView.getTag() != null && centerView.getTag() instanceof PreviewPasterForm) {
                        currentPasterForm = (PreviewPasterForm) centerView.getTag();
                        if (currentPasterForm != null && !currentPasterForm.getUrl().isEmpty()) {
                            addPasterResource(currentPasterForm);
                        }
                    } else {

                    }
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    currentPasterForm = null;
                    progressBar.setVisibility(View.GONE);
                }
            }

        });
//        fanProgressBar = (FanProgressBar) findViewById(R.id.record_progress);
        progressBar = (CircleProgressBar) findViewById(R.id.aliyun_download_progress);
        recordTimelineView = (RecordTimelineView) findViewById(R.id.aliyun_record_timeline);
        recordTimelineView.setColor(R.color.aliyun_color_record_duraton, R.color.aliyun_music_colorPrimary, R.color.qupai_black_opacity_70pct, R.color.aliyun_transparent);
        recordBg = (FrameLayout) findViewById(R.id.aliyun_record_bg);
        recordBg.setOnTouchListener(this);
        waitingLayout = (FrameLayout) findViewById(R.id.aliyun_copy_res_tip);
        tipTxt = (TextView) findViewById(R.id.aliyun_tip_text);
        linearSnapHelper = new LinearSnapHelper();
        linearSnapHelper.attachToRecyclerView(pasterView);
        calculateItemWidth();
//        fanProgressBar.setOutRadius(itemWidth / 2 - OUT_STROKE_WIDTH / 2);
//        fanProgressBar.setOffset(OUT_STROKE_WIDTH / 2, OUT_STROKE_WIDTH / 2);
//        fanProgressBar.setOutStrokeWidth(OUT_STROKE_WIDTH);
        final FrameLayout.LayoutParams recordBgLp = (FrameLayout.LayoutParams) recordBg.getLayoutParams();
        recordBgLp.width = itemWidth;
        recordBgLp.height = itemWidth;
        recordBg.setLayoutParams(recordBgLp);
        FrameLayout.LayoutParams downloadLp = (FrameLayout.LayoutParams) progressBar.getLayoutParams();
        downloadLp.width = itemWidth;
        downloadLp.height = itemWidth;
        progressBar.setLayoutParams(downloadLp);
        progressBar.setBackgroundWidth(itemWidth, itemWidth);
        progressBar.setProgressWidth(itemWidth - DensityUtil.dip2px(this, 20));
        progressBar.isFilled(true);
        backBtn = (ImageView) findViewById(R.id.aliyun_back);
        backBtn.setOnClickListener(this);
        recordDurationTxt = (TextView) findViewById(R.id.aliyun_record_duration);
        recordDurationTxt.setVisibility(View.GONE);
        filterTxt = (TextView) findViewById(R.id.aliyun_filter_txt);
        filterTxt.setVisibility(View.GONE);
        rateVerySlowTxt = (TextView) findViewById(R.id.aliyun_rate_quarter);
        rateVerySlowTxt.setOnClickListener(this);
        rateSlowTxt = (TextView) findViewById(R.id.aliyun_rate_half);
        rateSlowTxt.setOnClickListener(this);
        rateStandardTxt = (TextView) findViewById(R.id.aliyun_rate_origin);
        rateStandardTxt.setOnClickListener(this);
        rateFastTxt = (TextView) findViewById(R.id.aliyun_rate_double);
        rateFastTxt.setOnClickListener(this);
        rateVeryFastTxt = (TextView) findViewById(R.id.aliyun_rate_double_power2);
        rateVeryFastTxt.setOnClickListener(this);
        deleteBtn = (TextView) findViewById(R.id.aliyun_delete);
        deleteBtn.setOnClickListener(this);
        compeleteBtn = (ImageView) findViewById(R.id.aliyun_complete);
        compeleteBtn.setOnClickListener(this);
        rateBar = (LinearLayout) findViewById(R.id.aliyun_rate_bar);
        gestureDetector = new GestureDetector(this, this);
        scaleGestureDetector = new ScaleGestureDetector(this, this);
        recordLayoutBottom = findViewById(R.id.aliyun_record_layout_bottom);
        imvForms = MVResourceUtil.fetchMvLocalResource();

        magicMusic = findViewById(R.id.aliyun_mv);
        magicMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mGlSurfaceWidth == 0 || mGlSurfaceHeight == 0) {
                    mGlSurfaceWidth = glSurfaceView.getWidth();
                    mGlSurfaceHeight = glSurfaceView.getHeight();
                }
                recordLayoutBottom.setVisibility(View.GONE);
                final MVChooser mvChooser = MVChooser.newInstance();
                mvChooser.setSelectedEffectIndex(mSelectedMvPos);
                mvChooser.setChooseData(imvForms);
                mvChooser.setCancelable(true);
                mvChooser.setChooseListener(new MVChooser.OnEffectChangeListener() {
                    @Override
                    public void onEffectChanged(MvForm effect) {
                        if (effect == null) {
                            if (mCurrMv != null) {
                                mConflictEffects.remove(mCurrMv);
                                mCurrMv = null;
                            }
                        } else {
                            EffectBean mv = new EffectBean();
                            String path = MVResourceUtil.getMVPath(effect.list, mGlSurfaceWidth, mGlSurfaceHeight);
                            mv.setPath(path);
                            recorder.applyMv(mv);
                            if (path == null) {
                                mConflictEffects.remove(mCurrMv);
                                mCurrMv = null;
                            } else {
                                mConflictEffects.put(mv, TYPE_MV);
                                mCurrMv = mv;
                            }

                        }
                    }
                });
                mvChooser.setOnDismissListener(new MVChooser.OnChooseDismissListener() {
                    @Override
                    public void onChooseDismiss() {
                        recordLayoutBottom.setVisibility(View.VISIBLE);
                        mSelectedMvPos = mvChooser.getSelectedEffectIndex();
                    }
                });
                mvChooser.show(getFragmentManager(), "mv_chooser");
            }
        });

    }

    private void calculateItemWidth() {
        itemWidth = getResources().getDisplayMetrics().widthPixels / MAX_ITEM_COUNT;
    }

    private void addPasterResToLocal(String url, String id) {
        SharedPreferences sharedPreferences = getSharedPreferences(LOCAL_SETTING_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit().putString(id, url);
        editor.commit();
    }

    private String getLocalResUrl(String id) {
        SharedPreferences sharedPreferences = getSharedPreferences(LOCAL_SETTING_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(id, "");
    }

    private void addPasterResource(final PreviewPasterForm pasterForm) {
        if (pasterForm != null && !pasterForm.getIcon().isEmpty()) {
            if ((DownloadFileUtils.isPasterExist(this, pasterForm.getName(), pasterForm.getId()) && !getLocalResUrl(String.valueOf(pasterForm.getId())).isEmpty()) || pasterForm.isLocalRes()) {
                String path;
                if (pasterForm.isLocalRes()) {
                    path = pasterForm.getUrl();
                } else {
                    path = DownloadFileUtils.getAssetPackageDir(this,
                            pasterForm.getName(), pasterForm.getId()).getAbsolutePath();
                }
                Logger.getDefaultLogger().d("faces add downloaded res ..." + path);
                addEffectToRecord(path);
            } else {

                FileDownloaderModel fileDownloaderModel = new FileDownloaderModel();
                fileDownloaderModel.setUrl(pasterForm.getUrl());
                fileDownloaderModel.setPath(DownloadFileUtils.getAssetPackageDir(this,
                                            pasterForm.getName(), pasterForm.getId()).getAbsolutePath());
                fileDownloaderModel.setId(pasterForm.getId());
                fileDownloaderModel.setIsunzip(1);

                final FileDownloaderModel model = DownloaderManager.getInstance().addTask(fileDownloaderModel, fileDownloaderModel.getUrl());
                if (DownloaderManager.getInstance().isDownloading(model.getTaskId(), model.getPath())) {
                    return;
                }
                DownloaderManager.getInstance().startTask(model.getTaskId(), new FileDownloaderCallback() {
                    @Override
                    public void onProgress(int downloadId, long soFarBytes, long totalBytes, long speed, int progress) {
                        if (pasterForm == currentPasterForm) {
                            progressBar.setVisibility(View.VISIBLE);
                            progressBar.setProgress(progress);
                        } else {
                            progressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFinish(int downloadId, String path) {
                        Log.e("faces", "onItemDownloadCompleted ...");
                        progressBar.setVisibility(View.GONE);
                        File file = new File(path);
                        if (!file.exists() || !file.isDirectory()) {
                            return;
                        }
                        addPasterResToLocal(pasterForm.getUrl(), String.valueOf(pasterForm.getId()));
                        if (pasterForm == currentPasterForm) {
                            addEffectToRecord(path);
                        }
                    }

                    @Override
                    public void onError(BaseDownloadTask task, Throwable e) {
                        ToastUtil.showToast(AlivcRecorderActivity.this, R.string.aliyun_network_not_connect);
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        }
    }

    private void addEffectToRecord(String path) {
        if (new File(path).exists()) {
            if (effect != null) {
                recorder.removePaster(effect);
            }
            effect = new EffectPaster(path);
            recorder.addPaster(effect);
        }
//        if(((EffectPaster)effect).isPasterReady()){
//            recorder.addPaster(effect);
//            recorder.setViewSize(((EffectPaster) effect).getWidth() / (float) glSurfaceView.getWidth(),
//                    ((EffectPaster) effect).getHeight() / (float) glSurfaceView.getHeight(), effect);
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        recorder.startPreview();
        recorder.setZoom(scaleFactor);
        if (orientationDetector != null && orientationDetector.canDetectOrientation()) {
            orientationDetector.enable();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isRecording) {
            recorder.cancelRecording();
        }
        recorder.stopPreview();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (orientationDetector != null) {
            orientationDetector.disable();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        compeleteBtn.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recorder.destroy();
//        try {
//            dos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        if (orientationDetector != null) {
            orientationDetector.setOrientationChangedListener(null);
        }
    }

    private EffectBean music;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_MUSIC) {
            if (resultCode == Activity.RESULT_OK) {
                String path = data.getStringExtra(MUSIC_PATH);
                int startTime = data.getIntExtra(MUSIC_START_TIME, 0);
                if (music != null) {
                    mConflictEffects.remove(music);
                }
                music = new EffectBean();
                music.setPath(path);
                music.setStartTime(startTime);
                music.setDuration(clipManager.getMaxDuration());
                mConflictEffects.put(music, TYPE_MUSIC);
//                glSurfaceView.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        recorder.setMusic(music.getPath(), music.getStartTime(), music.getDuration());
//                    }
//                }, 3000);
//
//                glSurfaceView.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        recorder.setMusic(music.getPath(), music.getStartTime(), music.getDuration());
//                    }
//                }, 4000);
//                recorder.setMusic(path, startTime, clipManager.getMaxDuration());
            } else if (resultCode == Activity.RESULT_CANCELED) {
                if (music != null) {
                    mConflictEffects.remove(music);
                }
                recorder.setMusic(null, 0, 0);
            }
        } else if (requestCode == REQUEST_CODE_PLAY) {
            if (resultCode == Activity.RESULT_OK) {
                clipManager.deleteAllPart();
                finish();
            }
        }
    }

    private void switchLightBtnState() {
        if (cameraType == CameraType.FRONT) {
            switchLightBtn.setVisibility(View.GONE);
        } else if (cameraType == CameraType.BACK) {
            switchLightBtn.setVisibility(View.VISIBLE);
        }
    }

    private void switchBeauty() {
        if (cameraType == CameraType.FRONT) {
            recorder.setBeautyStatus(true);
        } else if (cameraType == CameraType.BACK) {
            recorder.setBeautyStatus(false);
        }
    }

    private void txtFadeIn() {
        filterTxt.animate().alpha(1).setDuration(FILTER_ANIMATION_DURATION / 2).setListener(
        new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                txtFadeOut();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();
    }

    private void txtFadeOut() {
        filterTxt.animate().alpha(0).setDuration(FILTER_ANIMATION_DURATION / 2).start();
        filterTxt.animate().setListener(null);
    }

    private void recordBtnScale(float scaleRate) {
        FrameLayout.LayoutParams recordBgLp = (FrameLayout.LayoutParams) recordBg.getLayoutParams();
        recordBgLp.width = (int) (itemWidth * scaleRate);
        recordBgLp.height = (int) (itemWidth * scaleRate);
        recordBg.setLayoutParams(recordBgLp);
//        FrameLayout.LayoutParams fanProgressBarBgLp = (FrameLayout.LayoutParams) fanProgressBar.getLayoutParams();
//        fanProgressBarBgLp.width = (int) (itemWidth * scaleRate);
//        fanProgressBarBgLp.height = (int) (itemWidth * scaleRate);
//        fanProgressBar.setLayoutParams(fanProgressBarBgLp);
//        fanProgressBar.setOffset((int) ((OUT_STROKE_WIDTH + itemWidth * (scaleRate - 1)) / 2), (int) ((OUT_STROKE_WIDTH + itemWidth * (scaleRate - 1)) / 2));
//        fanProgressBar.setOutRadius((int) (itemWidth * scaleRate - OUT_STROKE_WIDTH) / 2);
    }

    private void setSelectRateItem(View view) {
        rateVerySlowTxt.setSelected(false);
        rateSlowTxt.setSelected(false);
        rateStandardTxt.setSelected(false);
        rateFastTxt.setSelected(false);
        rateVeryFastTxt.setSelected(false);
        view.setSelected(true);
    }

    private void showFilter(String name) {
        if (name == null || name.isEmpty()) {
            name = getString(R.string.aliyun_filter_null);
        }
        filterTxt.animate().cancel();
        filterTxt.setText(name);
        filterTxt.setVisibility(View.VISIBLE);
        filterTxt.setAlpha(FADE_IN_START_ALPHA);
        txtFadeIn();
    }

    @Override
    public void onClick(View v) {
        if (v == switchCameraBtn) {
            int type = recorder.switchCamera();
            if (type == CameraType.BACK.getType()) {
                cameraType = CameraType.BACK;
            } else if (type == CameraType.FRONT.getType()) {
                cameraType = CameraType.FRONT;
            }
            switchLightBtnState();
        } else if (v == switchLightBtn) {
            if (flashType == FlashType.OFF) {
                flashType = FlashType.AUTO;
            } else if (flashType == FlashType.AUTO) {
                flashType = FlashType.ON;
            } else if (flashType == FlashType.ON) {
                flashType = FlashType.OFF;
            }
            switch (flashType) {
            case AUTO:
                v.setSelected(false);
                v.setActivated(true);
                break;
            case ON:
                v.setSelected(true);
                v.setActivated(false);
                break;
            case OFF:
                v.setSelected(true);
                v.setActivated(true);
                break;
            default:
                break;
            }
            recorder.setLight(flashType);
        } else if (v == backBtn) {
            finish();
        } else if (v == musicBtn) {
            if (waitingLayout.getVisibility() != View.VISIBLE) {
                Intent intent = new Intent(this, MusicActivity.class);
                intent.putExtra(MUSIC_MAX_RECORD_TIME, clipManager.getMaxDuration());
                startActivityForResult(intent, REQUEST_CODE_MUSIC);
            }
        } else if (v == rateVerySlowTxt) {
            recorder.setRate(0.5f);
            setSelectRateItem(v);
        } else if (v == rateSlowTxt) {
            recorder.setRate(0.75f);
            setSelectRateItem(v);
        } else if (v == rateStandardTxt) {
            recorder.setRate(1f);
            setSelectRateItem(v);
        } else if (v == rateFastTxt) {
            recorder.setRate(1.5f);
            setSelectRateItem(v);
        } else if (v == rateVeryFastTxt) {
            recorder.setRate(2f);
            setSelectRateItem(v);
        } else if (v == deleteBtn) {
            recordTimelineView.deleteLast();
            clipManager.deletePart();
            if (clipManager.getDuration() < clipManager.getMinDuration()) {
                compeleteBtn.setSelected(false);
            }
            if (clipManager.getDuration() == 0) {
                musicBtn.setVisibility(View.VISIBLE);
                magicMusic.setVisibility(View.VISIBLE);
                recorder.restartMv();
            }
        } else if (v == compeleteBtn) {
            if (v.isSelected() && waitingLayout.getVisibility() != View.VISIBLE) {
                finishRecording();
            }
        }
    }

    private void finishRecording() {
        waitingLayout.setVisibility(View.VISIBLE);
        tipTxt.setText(R.string.aliyun_compose);
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                recorder.finishRecording();
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                waitingLayout.setVisibility(View.GONE);
            }
        } .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public boolean onDown(MotionEvent e) {

        //if(isUseNative){false;
        //    isUseNative =
        //}else{
        //    isUseNative = true;
        //}
        return false;
    }


    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        float x = e.getX() / glSurfaceView.getWidth();
        float y = e.getY() / glSurfaceView.getHeight();
        recorder.setFocus(x, y);
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (waitingLayout.getVisibility() == View.VISIBLE) {
            return true;
        }
        if (e1.getPointerCount() > 1 || e2.getPointerCount() > 1) {
            return true;
        }
        if (velocityX > MAX_SWITCH_VELOCITY) {
            filterIndex++;
            if (filterIndex >= mEffDirs.length) {
                filterIndex = 0;
            }
        } else if (velocityX < -MAX_SWITCH_VELOCITY) {
            filterIndex--;
            if (filterIndex < 0) {
                filterIndex = mEffDirs.length - 1;
            }
        } else {
            return true;
        }
        mCurrFilter = new EffectFilter(mEffDirs[filterIndex]);
        recorder.applyFilter(mCurrFilter);
        mConflictEffects.put(mCurrFilter, TYPE_FILTER);
        showFilter(mCurrFilter.getName());
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (waitingLayout.getVisibility() == View.VISIBLE) {
            return true;
        }
        if (v == glSurfaceView) {
            gestureDetector.onTouchEvent(event);
            scaleGestureDetector.onTouchEvent(event);
        } else if (v == recordBg) {
            if (isOpenFailed) {
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                downTime = System.currentTimeMillis();
                if (v.isActivated()) {
                    return false;
                } else {
                    if (CommonUtil.SDFreeSize() < 50 * 1000 * 1000) {
                        Toast.makeText(this, R.string.aliyun_no_free_memory, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    videoPath = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DCIM + File.separator + System.currentTimeMillis() + ".mp4";
                    File file = new File(Environment.getExternalStorageDirectory()
                                         + File.separator
                                         + Environment.DIRECTORY_DCIM);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    recorder.setOutputPath(videoPath);

                    handleRecordStart();
//                    recorder.setMute(true);
                    recorder.startRecording();
                    if (clipManager.getPartCount() == 0) {
                        recorder.restartMv();
                    } else {
                        recorder.resumeMv();
                    }

                    if (flashType == FlashType.ON && cameraType == CameraType.BACK) {
                        recorder.setLight(FlashType.TORCH);
                    }
                }
            } else if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
                if (System.currentTimeMillis() - downTime < 500) {
//                    recorder.takePhoto(true);
//                    recorder.takePicture(true);
                }
                recorder.stopRecording();
                recorder.pauseMv();
                handleRecordStop();
            }
        }
        return true;
    }

    private void handleStopCallback(final boolean isValid, final long duration) {
//        recorder.finishRecording();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isValid) {
                    recordTimelineView.setDuration((int) duration);
                    recordTimelineView.clipComplete();
                } else {
                    recordTimelineView.setDuration(0);
                }
                recordBg.setActivated(false);
                isRecording = false;
            }
        });
    }

    private void handleRecordStop() {
        recordStopped = true;
        recordBtnScale(1f);
//        fanProgressBar.setProgress(0);
        pasterView.setVisibility(View.VISIBLE);
        switchCameraBtn.setVisibility(View.VISIBLE);
        if (cameraType == CameraType.BACK) {
            switchLightBtn.setVisibility(View.VISIBLE);
        }
        backBtn.setVisibility(View.VISIBLE);
        compeleteBtn.setVisibility(View.VISIBLE);
        deleteBtn.setVisibility(View.VISIBLE);
        rateBar.setVisibility(View.VISIBLE);
        recordDurationTxt.setVisibility(View.GONE);
        if (flashType == FlashType.ON && cameraType == CameraType.BACK) {
            recorder.setLight(FlashType.OFF);
        }

    }

    private void handleRecordStart() {
        recordTime = 0;
        recordDurationTxt.setText("");
        recordBtnScale(1.2f);
        recordBg.setActivated(true);
        isRecording = true;
        recordDurationTxt.setVisibility(View.VISIBLE);
        recordStopped = false;
        musicBtn.setVisibility(View.GONE);
        magicMusic.setVisibility(View.GONE);
        pasterView.setVisibility(View.INVISIBLE);
        switchCameraBtn.setVisibility(View.INVISIBLE);
        if (cameraType == CameraType.BACK) {
            switchLightBtn.setVisibility(View.INVISIBLE);
        }
        backBtn.setVisibility(View.INVISIBLE);
        compeleteBtn.setVisibility(View.INVISIBLE);
        deleteBtn.setVisibility(View.INVISIBLE);
        rateBar.setVisibility(View.INVISIBLE);

    }

    private void deleteVideoFile() {
        if (videoPath == null || videoPath.isEmpty()) {
            return;
        }
        File file = new File(videoPath);
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        Log.e(TAG, "factor..." + detector.getScaleFactor());
        float factorOffset = detector.getScaleFactor() - lastScaleFactor;
        scaleFactor += factorOffset;
        lastScaleFactor = detector.getScaleFactor();
        if (scaleFactor < 0) {
            scaleFactor = 0;
        }
        if (scaleFactor > 1) {
            scaleFactor = 1;
        }
        recorder.setZoom(scaleFactor);
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        lastScaleFactor = detector.getScaleFactor();
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    public int getVirtualBarHeigh() {
        int vh = 0;
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        try {
            @SuppressWarnings("rawtypes")
            Class c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            vh = dm.heightPixels - windowManager.getDefaultDisplay().getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vh;
    }

    public static void startRecord(Context context, AliyunSnapVideoParam param) {
        Intent intent = new Intent(context, AlivcRecorderActivity.class);
        intent.putExtra(AliyunSnapVideoParam.VIDEO_RESOLUTION, param.getResolutionMode());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_RATIO, param.getRatioMode());
        intent.putExtra(AliyunSnapVideoParam.RECORD_MODE, param.getRecordMode());
        intent.putExtra(AliyunSnapVideoParam.FILTER_LIST, param.getFilterList());
        intent.putExtra(AliyunSnapVideoParam.BEAUTY_LEVEL, param.getBeautyLevel());
        intent.putExtra(AliyunSnapVideoParam.BEAUTY_STATUS, param.getBeautyStatus());
        intent.putExtra(AliyunSnapVideoParam.CAMERA_TYPE, param.getCameraType());
        intent.putExtra(AliyunSnapVideoParam.FLASH_TYPE, param.getFlashType());
        intent.putExtra(AliyunSnapVideoParam.NEED_CLIP, param.isNeedClip());
        intent.putExtra(AliyunSnapVideoParam.MAX_DURATION, param.getMaxDuration());
        intent.putExtra(AliyunSnapVideoParam.MIN_DURATION, param.getMinDuration());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_QUALITY, param.getVideoQuality());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_GOP, param.getGop());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_BITRATE, param.getVideoBitrate());
        intent.putExtra(AliyunSnapVideoParam.SORT_MODE, param.getSortMode());
        intent.putExtra(AliyunSnapVideoParam.VIDEO_CODEC, param.getVideoCodec());


        intent.putExtra(AliyunSnapVideoParam.VIDEO_FRAMERATE, param.getFrameRate());
        intent.putExtra(AliyunSnapVideoParam.CROP_MODE, param.getScaleMode());
        intent.putExtra(AliyunSnapVideoParam.MIN_CROP_DURATION, param.getMinCropDuration());
        intent.putExtra(AliyunSnapVideoParam.MIN_VIDEO_DURATION, param.getMinVideoDuration());
        intent.putExtra(AliyunSnapVideoParam.MAX_VIDEO_DURATION, param.getMaxVideoDuration());
        intent.putExtra(AliyunSnapVideoParam.SORT_MODE, param.getSortMode());

        context.startActivity(intent);
    }
}
