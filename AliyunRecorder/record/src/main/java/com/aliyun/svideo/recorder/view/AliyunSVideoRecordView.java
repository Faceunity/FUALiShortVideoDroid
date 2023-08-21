package com.aliyun.svideo.recorder.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.aliyun.common.global.AliyunTag;
import com.aliyun.common.utils.BitmapUtil;
import com.aliyun.common.utils.CommonUtil;
import com.aliyun.common.utils.DensityUtil;
import com.aliyun.svideo.base.BaseChooser;
import com.aliyun.svideo.base.Constants;
import com.aliyun.svideo.base.beauty.api.OnBeautyLayoutChangeListener;
import com.aliyun.svideo.base.beauty.api.constant.BeautySDKType;
import com.aliyun.svideo.base.http.MusicFileBean;
import com.aliyun.svideo.base.utils.VideoInfoUtils;
import com.aliyun.svideo.base.widget.ProgressDialog;
import com.aliyun.svideo.base.widget.RecordTimelineView;
import com.aliyun.svideo.common.utils.DensityUtils;
import com.aliyun.svideo.common.utils.LanguageUtils;
import com.aliyun.svideo.common.utils.PermissionUtils;
import com.aliyun.svideo.common.utils.ScreenUtils;
import com.aliyun.svideo.common.utils.ThreadUtils;
import com.aliyun.svideo.common.utils.ToastUtils;
import com.aliyun.svideo.common.utils.UriUtils;
import com.aliyun.svideo.common.utils.image.ImageLoaderImpl;
import com.aliyun.svideo.common.utils.image.ImageLoaderOptions;
import com.aliyun.svideo.downloader.zipprocessor.DownloadFileUtils;
import com.aliyun.svideo.record.R;
import com.aliyun.svideo.recorder.activity.AlivcSvideoRecordActivity;
import com.aliyun.svideo.recorder.bean.AlivcMixBorderParam;
import com.aliyun.svideo.recorder.bean.VideoDisplayParam;
import com.aliyun.svideo.recorder.mixrecorder.AlivcIMixRecorderInterface;
import com.aliyun.svideo.recorder.profile.CSVUtils;
import com.aliyun.svideo.recorder.profile.Constant;
import com.aliyun.svideo.recorder.util.FixedToastUtils;
import com.aliyun.svideo.recorder.util.OrientationDetector;
import com.aliyun.svideo.recorder.util.PreferenceUtil;
import com.aliyun.svideo.recorder.util.RecordCommon;
import com.aliyun.svideo.recorder.util.TimeFormatterUtils;
import com.aliyun.svideo.recorder.view.control.CameraType;
import com.aliyun.svideo.recorder.view.control.ControlView;
import com.aliyun.svideo.recorder.view.control.ControlViewListener;
import com.aliyun.svideo.recorder.view.control.FlashType;
import com.aliyun.svideo.recorder.view.control.RecordState;
import com.aliyun.svideo.recorder.view.countdown.AlivcCountDownView;
import com.aliyun.svideo.recorder.view.dialog.AnimFilterEffectChooser;
import com.aliyun.svideo.recorder.view.dialog.FilterEffectChooser;
import com.aliyun.svideo.recorder.view.dialog.GIfEffectChooser;
import com.aliyun.svideo.recorder.view.effects.filter.EffectInfo;
import com.aliyun.svideo.recorder.view.effects.filter.OnFilterItemClickListener;
import com.aliyun.svideo.recorder.view.effects.filter.animfilter.AnimFilterLoadingView;
import com.aliyun.svideo.recorder.view.effects.filter.animfilter.OnAnimFilterItemClickListener;
import com.aliyun.svideo.recorder.view.effects.paster.PasterSelectListener;
import com.aliyun.svideo.recorder.view.focus.FocusView;
import com.aliyun.svideo.recorder.view.music.MusicChooser;
import com.aliyun.svideo.recorder.view.music.MusicSelectListener;
import com.aliyun.svideosdk.common.AliyunErrorCode;
import com.aliyun.svideosdk.common.callback.recorder.OnFrameCallback;
import com.aliyun.svideosdk.common.callback.recorder.OnPictureCallback;
import com.aliyun.svideosdk.common.callback.recorder.OnRecordCallback;
import com.aliyun.svideosdk.common.callback.recorder.OnTextureIdCallback;
import com.aliyun.svideosdk.common.struct.effect.EffectStream;
import com.aliyun.svideosdk.common.struct.project.Source;
import com.aliyun.svideosdk.common.struct.common.AliyunSnapVideoParam;
import com.aliyun.svideosdk.common.struct.effect.EffectFilter;
import com.aliyun.svideosdk.common.struct.form.PreviewPasterForm;
import com.aliyun.svideosdk.common.struct.recorder.CameraParam;
import com.aliyun.svideosdk.recorder.AliyunIClipManager;
import com.aliyun.svideosdk.recorder.impl.AliyunRecordPasterController;
import com.aliyun.svideosdk.recorder.impl.AliyunRecordWaterMarkController;
import com.faceunity.core.enumeration.CameraFacingEnum;
import com.faceunity.core.enumeration.FUAIProcessorEnum;
import com.faceunity.core.enumeration.FUTransformMatrixEnum;
import com.faceunity.core.faceunity.FUAIKit;
import com.faceunity.core.faceunity.FURenderKit;
import com.faceunity.core.model.facebeauty.FaceBeautyBlurTypeEnum;
import com.faceunity.core.utils.CameraUtils;
import com.faceunity.nama.FUConfig;
import com.faceunity.nama.FURenderer;
import com.faceunity.nama.data.FaceUnityDataFactory;
import com.faceunity.nama.listener.FURendererListener;
import com.faceunity.nama.ui.FaceUnityView;
import com.faceunity.nama.utils.FuDeviceUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 新版本(> 3.6.5之后)录制模块的具体业务实现类
 */
public class AliyunSVideoRecordView extends FrameLayout
    implements BaseChooser.DialogVisibleListener, ScaleGestureDetector.OnScaleGestureListener {
    private static final String TAG = AliyunSVideoRecordView.class.getSimpleName();
    private static final String TAG_GIF_CHOOSER = "gif";
    private static final String TAG_BEAUTY_CHOOSER = "beauty";
    private static final String TAG_MUSIC_CHOOSER = "music";
    private static final String TAG_FILTER_CHOOSER = "filter";
    private static final String TAG_ANIM_FILTER_CHOOSER = "anim_filter";

    //最小录制时长
    private static final int MIN_RECORD_TIME = 0;
    //最大录制时长
    private static final int MAX_RECORD_TIME = Integer.MAX_VALUE;

    /**
     * v3.7.8改动, GlsurfaceView --> SurfaceView
     * AliyunIRecorder.setDisplayView(GLSurfaceView surfaceView) =====>> AliyunIRecorder.setDisplayView(SurfaceView surfaceView)
     */
    private SurfaceView mRecorderSurfaceView;
    private SurfaceView mPlayerSurfaceView;
    private FrameLayout mVideoContainer;
    private ControlView mControlView;
    private RecordTimelineView mRecordTimeView;
    private AlivcCountDownView mCountDownView;

    private AlivcIMixRecorderInterface recorder;

    private AliyunIClipManager clipManager;
    private com.aliyun.svideosdk.common.struct.recorder.CameraType cameraType
        = com.aliyun.svideosdk.common.struct.recorder.CameraType.FRONT;
    private FragmentActivity mActivity;
    //录制视频是否达到最大值
    private boolean isMaxDuration = false;
    //录制时长
    private int recordTime = 0;

    //最小录制时长
    private int minRecordTime = 2000;
    //最大录制时长
    private int maxRecordTime = 15 * 1000;
    //关键帧间隔
    private int mGop = 5;
    //视频比例
    private int mRatioMode = AliyunSnapVideoParam.RATIO_MODE_3_4;
    //渲染方式
    private BeautySDKType mRenderingMode;
    //是否是race录制包
    private boolean isSvideoRace = false;

    private AlivcMixBorderParam mMixBorderParam;


    //用来合拍的视频路径
    private String mMixVideoPath;

    private GIfEffectChooser gifEffectChooser;
    /**
     * 滤镜选择弹窗
     */
    private FilterEffectChooser filterEffectChooser;
    /**
     * 特效滤镜选择弹窗
     */
    private AnimFilterEffectChooser mAnimFilterEffectChooser;
    //视频分辨率
    private int mResolutionMode = AliyunSnapVideoParam.RESOLUTION_540P;

    //选中的贴图效果
    private AliyunRecordPasterController mPasterController;
    private OrientationDetector orientationDetector;
    private MusicChooser musicChooseView;


    public static final int TYPE_FILTER = 1;
    public static final int TYPE_MUSIC = 3;
    private LinkedHashMap<Integer, Object> mConflictEffects = new LinkedHashMap<>();
    private AsyncTask<Void, Integer, Integer> finishRecodingTask;
    private AsyncTask<Void, Void, Void> faceTrackPathTask;

    /**
     * 记录filter选中的item索引
     */
    private int currentFilterPosition;

    /**
     * 记录动效filter选中的item索引
     */
    private int mCurrentAnimFilterPosition;

    /**
     * 控制mv的添加, 开始录制后,不允许切换mv
     */
    private boolean isAllowChangeMv = true;
    private ProgressDialog progressBar;

    /**
     * 是否处于后台
     */
    private boolean mIsBackground;

    private boolean isHasMusic = false;
    private FocusView mFocusView;
    private EffectFilter mCurrentAnimFilterEffect;
    private boolean mIsUseFlip = false;

    public boolean isHasMusic() {
        return isHasMusic;
    }

    /**
     * 音乐选择监听，通知到外部
     */
    private MusicSelectListener mOutMusicSelectListener;

    /**
     * race单独录制的水印
     */
    private AliyunRecordWaterMarkController mWaterMarkController;
    private boolean mIsVoiceOff = false;


    private FaceUnityDataFactory mFaceUnityDataFactory;
    private FURenderer mFURenderer;
    private TextView tvFps;
    private int mSkippedFrames = 5;
    private CSVUtils mCSVUtils;


    /**
     * 恢复冲突的特效，这些特效都是会彼此冲突的，比如滤镜和MV，因为MV中也有滤镜效果，所以MV和滤镜的添加顺序 会影响最终产生视频的效果，在恢复时必须严格按照用户的操作顺序来恢复，
     * 这样就需要维护一个添加过的特效类的列表，然后按照列表顺序 去恢复
     */
    private void restoreConflictEffect() {
        if (!mConflictEffects.isEmpty() && recorder != null) {
            for (Map.Entry<Integer, Object> entry : mConflictEffects.entrySet()) {
                switch (entry.getKey()) {
                case TYPE_FILTER:
                    recorder.applyFilter((EffectFilter) entry.getValue());
                    break;
                case TYPE_MUSIC:
                    EffectStream music = (EffectStream) entry.getValue();

                    if (music != null) {
                        // 根据音乐路径判断是否添加了背景音乐,
                        // 在编辑界面, 如果录制添加了背景音乐, 则不能使用音效特效
                        isHasMusic = !TextUtils.isEmpty(music.getSource().getPath());
                        if (isHasMusic) {
                            recorder.applyBackgroundMusic(music);
                        }

                    }
                    break;
                default:
                    break;
                }
            }
        }
    }

    /**
     * 用于防止sdk的oncomplete回调之前, 再次调用startRecord
     */
    private boolean tempIsComplete = true;

    public AliyunSVideoRecordView(Context context) {
        super(context);
    }

    public AliyunSVideoRecordView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AliyunSVideoRecordView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initVideoView() {

        initControlView();
        initCountDownView();
        initRecordTimeView();
        initFocusView();
        initBeauty();
        setFaceTrackModePath();

    }


    /**
     * 焦点focus
     */
    private void initFocusView() {
        mFocusView = new FocusView(getContext());
        mFocusView.setPadding(10, 10, 10, 10);
        addSubView(mFocusView);
    }


    public void onPause() {
        mIsBackground = true;
    }

    public void onResume() {
        mIsBackground = false;
    }

    public void onStop() {
        if (mFocusView != null) {
            mFocusView.activityStop();
        }
    }

    /**
     * FURenderer状态回调
     */
    private FURendererListener mFURendererListener = new FURendererListener() {
        @Override
        public void onPrepare() {
            mFaceUnityDataFactory.bindCurrentRenderer();
        }

        @Override
        public void onTrackStatusChanged(FUAIProcessorEnum type, int status) {

        }

        @Override
        public void onFpsChanged(double fps, double callTime) {
            final String FPS = String.format(Locale.getDefault(), "%.2f", fps);
            Log.e(TAG, "onFpsChanged: FPS " + FPS + " callTime " + String.format(Locale.getDefault(), "%.2f", callTime));
            post(new Runnable() {
                @Override
                public void run() {
                    tvFps.setText("FPS: " + FPS);
                }
            });
        }

        @Override
        public void onRelease() {

        }
    };

    /**
     * 初始化美颜SDK
     */
    private void initBeauty() {
        Context context = getContext();
        boolean mIsFuBeautyOpen = TextUtils.equals(PreferenceUtil.VALUE_ON, PreferenceUtil.getString(context, PreferenceUtil.KEY_FACEUNITY_ISON));
        if (mIsFuBeautyOpen) {
            FURenderer.getInstance().setup(context);
            tvFps = new TextView(context);
            tvFps.setTextColor(Color.BLACK);
            FrameLayout.LayoutParams fpsParams = new FrameLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            fpsParams.gravity = Gravity.START | Gravity.TOP;
            fpsParams.topMargin = DensityUtil.dip2px(context, 80);
            fpsParams.leftMargin = DensityUtil.dip2px(context, 10);
            addView(tvFps, fpsParams);

            mFURenderer = FURenderer.getInstance();
            mFURenderer.setMarkFPSEnable(true);
            mFURenderer.setInputBufferMatrix(FUTransformMatrixEnum.CCROT0_FLIPVERTICAL);
            mFURenderer.setInputTextureMatrix(FUTransformMatrixEnum.CCROT0_FLIPVERTICAL);
            mFURenderer.setOutputMatrix(FUTransformMatrixEnum.CCROT0);
            FaceUnityView beautyControlView = new FaceUnityView(context);
            mFaceUnityDataFactory = new FaceUnityDataFactory(-1);
            beautyControlView.bindDataFactory(mFaceUnityDataFactory);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
            params.bottomMargin = DensityUtil.dip2px(context, 200);
            addView(beautyControlView, params);
        }
    }


    public void isUseFlip(boolean isUseFlip) {
        this.mIsUseFlip = isUseFlip;
    }


    /**
     * 初始化倒计时view
     */
    private void initCountDownView() {
        if (mCountDownView == null) {
            mCountDownView = new AlivcCountDownView(getContext());
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.CENTER;
            addView(mCountDownView, params);
        }
    }

    private void initRecordTimeView() {
        mRecordTimeView = new RecordTimelineView(getContext());
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, DensityUtils.dip2px(getContext(), 6));

        params.setMargins(DensityUtils.dip2px(getContext(), 12),
                          DensityUtils.dip2px(getContext(), 6),
                          DensityUtils.dip2px(getContext(), 12),
                          0);
        mRecordTimeView.setColor(R.color.alivc_record_bg_timeview_duraton, R.color.alivc_record_bg_timeview_selected, R.color.alivc_common_white,
                                 R.color.alivc_record_bg_timeview);
        mRecordTimeView.setMaxDuration(clipManager.getMaxDuration());
        mRecordTimeView.setMinDuration(clipManager.getMinDuration());
        addView(mRecordTimeView, params);
        mRecordTimeView.setMaxDuration(getMaxRecordTime());
        mRecordTimeView.setMinDuration(minRecordTime);
    }

    private void initVideoContainer() {
        mVideoContainer = new FrameLayout(getContext());
        int videoOutputWidth = recorder.getVideoWidth();
        int videoOutputHeight = recorder.getVideoHeight();
        int width = ScreenUtils.getRealWidth(getContext());
        int height = width * videoOutputHeight / videoOutputWidth;
        LayoutParams params = new LayoutParams(width, height);
        params.gravity = Gravity.CENTER;
        addView(mVideoContainer, params);
        if (recorder.isMixRecorder()) {
            int backgroundColor = recorder.getBackgroundColor();
            mVideoContainer.setBackgroundColor(backgroundColor);
            String backgroundImage = recorder.getBackgroundImage();
            if (!TextUtils.isEmpty(backgroundImage)) {
                int displayMode = recorder.getBackgroundImageDisplayMode();
                ImageView lBackgroundImageView = new ImageView(getContext());
                switch (displayMode) {
                case 0:
                    lBackgroundImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    break;
                case 1:
                    lBackgroundImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    break;
                case 2:
                    lBackgroundImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    break;
                }
                lBackgroundImageView.setBackgroundColor(backgroundColor);
                LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                mVideoContainer.addView(lBackgroundImageView, layoutParams);
                new ImageLoaderImpl().loadImage(getContext(), "file://" + backgroundImage, new ImageLoaderOptions.Builder().skipDiskCacheCache().skipMemoryCache().build()).into(lBackgroundImageView);
            }
            //调整布局级别
            if (recorder.getPlayDisplayParams().getLayoutLevel() > recorder.getRecordDisplayParam().getLayoutLevel()) {
                initRecorderSurfaceView();
                initPlayerSurfaceView();
            } else {
                initPlayerSurfaceView();
                initRecorderSurfaceView();
            }

        } else {
            initRecorderSurfaceView();
        }
        //添加录制surFaceView
        recorder.setDisplayView(mRecorderSurfaceView, mPlayerSurfaceView);
    }

    /**
     * 初始化RecordersurfaceView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initRecorderSurfaceView() {
        mRecorderSurfaceView = new SurfaceView(getContext());
        final ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(getContext(), this);
        final GestureDetector gestureDetector = new GestureDetector(getContext(),
        new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (recorder == null) {
                    return true;
                }
                float x = e.getX() / mRecorderSurfaceView.getWidth();
                float y = e.getY() / mRecorderSurfaceView.getHeight();
                recorder.setFocus(x, y);

                mFocusView.showView();
                mFocusView.setLocation(e.getRawX(), e.getRawY());
                return true;
            }
        });
        mRecorderSurfaceView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getPointerCount() >= 2) {
                    scaleGestureDetector.onTouchEvent(event);
                } else if (event.getPointerCount() == 1) {
                    gestureDetector.onTouchEvent(event);
                }
                return true;
            }
        });

        int border = 0;
        int color = Color.TRANSPARENT;
        float tempRadius = 0;

        if (mMixBorderParam != null) {
            border = mMixBorderParam.getBorderWidth();
            color = mMixBorderParam.getBorderColor();
            tempRadius = mMixBorderParam.getCornerRadius();
        }

        final float radius = tempRadius;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mRecorderSurfaceView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    Rect rect = new Rect();
                    view.getGlobalVisibleRect(rect);
                    int leftMargin = 0;
                    int topMargin = 0;
                    Rect selfRect = new Rect(leftMargin, topMargin,
                                             rect.right - rect.left - leftMargin,
                                             rect.bottom - rect.top - topMargin);
                    outline.setRoundRect(selfRect, radius);
                }
            });
            mRecorderSurfaceView.setClipToOutline(true);
        }

        VideoDisplayParam displayParam = recorder.getRecordDisplayParam();
        int videoOutputWidth = recorder.getVideoWidth();
        int videoOutputHeight = recorder.getVideoHeight();
        int parentWidth = ScreenUtils.getRealWidth(getContext());
        int parentHeight = parentWidth * videoOutputHeight / videoOutputWidth;
        int width = (int) (parentWidth * displayParam.getWidthRatio());
        int height = (int) (parentHeight * displayParam.getHeightRatio());
        int marginLeft = (int) ((displayParam.getCenterX() - displayParam.getWidthRatio() / 2) * parentWidth);
        int marginTop = (int) ((displayParam.getCenterY() - displayParam.getHeightRatio() / 2) * parentHeight);

        FrameLayout container = new FrameLayout(getContext());
        container.setBackgroundColor(color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            container.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    Rect rect = new Rect();
                    view.getGlobalVisibleRect(rect);
                    int leftMargin = 0;
                    int topMargin = 0;
                    Rect selfRect = new Rect(leftMargin, topMargin,
                                             rect.right - rect.left - leftMargin,
                                             rect.bottom - rect.top - topMargin);
                    outline.setRoundRect(selfRect, radius);
                }
            });
            container.setClipToOutline(true);
        }

        LayoutParams slp = new LayoutParams(width - border * 2, height - border * 2);
        slp.gravity = Gravity.CENTER;
        container.addView(mRecorderSurfaceView, slp);

        LayoutParams layoutParams = new LayoutParams(width, height);
        layoutParams.leftMargin = marginLeft;
        layoutParams.topMargin = marginTop;
        mVideoContainer.addView(container, layoutParams);
    }

    /**
     * 初始化PlayerSurfaceView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initPlayerSurfaceView() {
        mPlayerSurfaceView = new SurfaceView(getContext());
        VideoDisplayParam displayParam = recorder.getPlayDisplayParams();
        int videoOutputWidth = recorder.getVideoWidth();
        int videoOutputHeight = recorder.getVideoHeight();
        int parentWidth = ScreenUtils.getRealWidth(getContext());
        int parentHeight = parentWidth * videoOutputHeight / videoOutputWidth;
        int width = (int) (parentWidth * displayParam.getWidthRatio());
        int height = (int) (parentHeight * displayParam.getHeightRatio());
        int marginLeft = (int) ((displayParam.getCenterX() - displayParam.getWidthRatio() / 2) * parentWidth);
        int marginTop = (int) ((displayParam.getCenterY() - displayParam.getHeightRatio() / 2) * parentHeight);
        LayoutParams layoutParams = new LayoutParams(width, height);
        layoutParams.leftMargin = marginLeft;
        layoutParams.topMargin = marginTop;
        mVideoContainer.addView(mPlayerSurfaceView, layoutParams);
    }


    /**
     * 初始化控制栏view
     */
    private void initControlView() {
        mControlView = new ControlView(getContext());
        if (isSvideoRace) {
            mControlView.setAliyunCompleteText(R.string.alivc_base_svideo_save);
        } else {
            mControlView.setAliyunCompleteText(R.string.alivc_base_svideo_next);
        }
        mControlView.setControlViewListener(new ControlViewListener() {
            @Override
            public void onBackClick() {
                if (mBackClickListener != null) {
                    mBackClickListener.onClick();
                }
            }

            @Override
            public void onNextClick() {
                // 完成录制
                if (!isStopToCompleteDuration) {
                    finishRecording();
                }
            }

            @Override
            public void onBeautyFaceClick() {
            }

            @Override
            public void onMusicClick() {
                showMusicSelView();
            }

            @Override
            public void onCameraSwitch() {
                if (recorder != null) {
                    int cameraId = recorder.switchCamera();
                    for (com.aliyun.svideosdk.common.struct.recorder.CameraType type : com.aliyun.svideosdk.common.struct.recorder.CameraType.values()) {
                        if (type.getType() == cameraId) {
                            cameraType = type;
                        }
                    }
                    mSkippedFrames = 3;
                    if (mFURenderer != null) {
                        int type = cameraType.getType();
                        CameraFacingEnum cameraType = type == Camera.CameraInfo.CAMERA_FACING_FRONT ? CameraFacingEnum.CAMERA_FRONT : CameraFacingEnum.CAMERA_BACK;
                        mFURenderer.setCameraFacing(cameraType);
                    }
                    if (mControlView != null) {
                        for (CameraType type : CameraType.values()) {
                            if (type.getType() == cameraId) {
                                mControlView.setCameraType(type);
                            }
                        }

                        if (mControlView.getFlashType() == FlashType.ON
                                && mControlView.getCameraType() == CameraType.BACK) {
                            recorder.setLight(com.aliyun.svideosdk.common.struct.recorder.FlashType.TORCH);
                        }
                    }
                }
            }

            @Override
            public void onVoiceSwitch() {
                if (recorder != null) {
                    mIsVoiceOff = !mIsVoiceOff;
                    recorder.setMute(mIsVoiceOff);
                }
            }

            @Override
            public void onLightSwitch(FlashType flashType) {
                if (recorder != null) {
                    for (com.aliyun.svideosdk.common.struct.recorder.FlashType type : com.aliyun.svideosdk.common.struct.recorder.FlashType.values()) {
                        if (flashType.toString().equals(type.toString())) {
                            recorder.setLight(type);
                        }
                    }

                }
                if (mControlView.getFlashType() == FlashType.ON
                        && mControlView.getCameraType() == CameraType.BACK) {
                    recorder.setLight(com.aliyun.svideosdk.common.struct.recorder.FlashType.TORCH);
                }
            }

            @Override
            public void onRateSelect(float rate) {
                if (recorder != null) {
                    recorder.setRate(rate);
                }
            }

            @Override
            public void onGifEffectClick() {
                showGifEffectView();
            }

            @Override
            public void onReadyRecordClick(boolean isCancel) {
                if (isStopToCompleteDuration) {
                    /*TODO  这里是因为如果 SDK 还没有回调onComplete,倒计时录制，会crash */
                    return;
                }
                if (isCancel) {
                    cancelReadyRecord();
                } else {
                    showReadyRecordView();
                }

            }

            @Override
            public void onStartRecordClick() {
                if (!tempIsComplete) {
                    // 连续点击开始录制，停止录制，要保证在onComplete回调回来再允许点击开始录制,
                    // 否则SDK会出现ANR问题, v3.7.8修复会影响较大, 工具包暂时处理
                    return;
                }
                startRecord();
            }

            @Override
            public void onStopRecordClick() {
                if (!tempIsComplete) {
                    // 连续点击开始录制，停止录制，要保证在onComplete回调回来再允许点击开始录制,
                    // 否则SDK会出现ANR问题, v3.7.8修复会影响较大, 工具包暂时处理
                    return;
                }
                stopRecord();
            }

            @Override
            public void onDeleteClick() {
                if (isStopToCompleteDuration) {
                    // 这里是因为如果 SDK 还没有回调onComplete,点击回删会出现删除的不是最后一段的问题
                    return;
                }
                mRecordTimeView.deleteLast();
                // clipManager.deletePart();
                recorder.deleteLastPart();
                isMaxDuration = false;
                if (mControlView != null) {
                    if (clipManager.getDuration() < clipManager.getMinDuration()) {
                        mControlView.setCompleteEnable(false);
                    }

                    mControlView.updateCutDownView(true);
                }

                if (clipManager.getDuration() == 0) {
                    //音乐可以选择
                    mControlView.setHasRecordPiece(false);
                    isAllowChangeMv = true;
                }
                mControlView.setRecordTime(TimeFormatterUtils.formatTime(clipManager.getDuration()));
            }

            @Override
            public void onFilterEffectClick() {
                // 滤镜选择弹窗弹出
                showFilterEffectView();
            }

            @Override
            public void onChangeAspectRatioClick(int ratio) {
                //重新绘制界面
                setReSizeRatioMode(ratio);
            }

            @Override
            public void onAnimFilterClick() {

                showAnimFilterEffectView();
            }

            @Override
            public void onTakePhotoClick() {
                //拍照
                recorder.takeSnapshot(true, new OnPictureCallback() {
                    @Override
                    public void onPicture(final Bitmap bitmap, byte[] data) {
                        ThreadUtils.runOnSubThread(new Runnable() {
                            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                            @Override
                            public void run() {
                                final String imgPath = Constants.SDCardConstants.getDir(getContext().getApplicationContext()) + File.separator + System.currentTimeMillis() + "-photo.jpg";
                                try {
                                    BitmapUtil.generateFileFromBitmap(bitmap, imgPath, "jpg");

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        //适配android Q
                                        ThreadUtils.runOnSubThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                UriUtils.saveImgToMediaStore(getContext().getApplicationContext(), imgPath);
                                            }
                                        });
                                    } else {
                                        MediaScannerConnection.scanFile(getContext().getApplicationContext(),
                                                                        new String[] {imgPath}, new String[] {"image/jpeg"}, null);
                                    }

                                    ThreadUtils.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ToastUtils.show(getContext(), "图片已保存到相册");
                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
            }

            @Override
            public void onRaceDebug(boolean debug) {

            }
        });
        mControlView.setRecordType(recorder.isMixRecorder());
        addSubView(mControlView);
        mControlView.setAspectRatio(mRatioMode);
    }

    /**
     * 权限申请
     */
    String[] permission = {
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * 开始录制
     */
    private void startRecord() {
        boolean checkResult = PermissionUtils.checkPermissionsGroup(getContext(), permission);
        if (!checkResult && mActivity != null) {
            PermissionUtils.requestPermissions(mActivity, permission,
                                               AlivcSvideoRecordActivity.PERMISSION_REQUEST_CODE);
            return;
        }

        if (CommonUtil.SDFreeSize() < 50 * 1000 * 1000) {
            FixedToastUtils.show(getContext(), getResources().getString(R.string.alivc_music_no_free_memory));
            return;
        }
        if (isMaxDuration) {
            mControlView.setRecordState(RecordState.STOP);
            return;
        }

        if (recorder != null && !mIsBackground) {
            // 快速显示回删字样, 将音乐按钮置灰,假设此时已经有片段, 在录制失败时, 需要改回false
            mControlView.setHasRecordPiece(true);
            mControlView.setRecordState(RecordState.RECORDING);
            mControlView.setRecording(true);
            String videoPath = Constants.SDCardConstants.getDir(getContext().getApplicationContext()) + File.separator + System.currentTimeMillis() + "-record.mp4";
            recorder.setOutputPath(videoPath);
            recorder.startRecording();

            Log.d(TAG, "startRecording    isStopToCompleteDuration:" + isStopToCompleteDuration);
        }

    }

    /**
     * 视频是是否正正在已经调用stopRecord到onComplete回调过程中这段时间，这段时间不可再次调用stopRecord
     * true: 正在调用stop~onComplete, false反之
     */
    private boolean isStopToCompleteDuration;

    /**
     * 停止录制
     */
    private void stopRecord() {
        Log.d(TAG, "stopRecord    isStopToCompleteDuration:" + isStopToCompleteDuration);
        if (recorder != null && !isStopToCompleteDuration && mControlView.isRecording()) {//
            isStopToCompleteDuration = true;

            //此处添加判断，progressBar弹出，也即当视频片段合成的时候，不调用stopRecording,
            //否则在finishRecording的时候调用stopRecording，会导致finishRecording阻塞
            //暂时规避，等待sdk解决该问题，取消该判断
            if ((progressBar == null || !progressBar.isShowing())) {
                recorder.stopRecording();

            }

        }

    }

    /**
     * 取消拍摄倒计时
     */
    private void cancelReadyRecord() {
        if (mCountDownView != null) {
            mCountDownView.cancle();
        }
    }

    /**
     * 显示准备拍摄倒计时view
     */
    private void showReadyRecordView() {
        if (mCountDownView != null) {
            mCountDownView.start();
        }
    }

    /**
     * 显示音乐选择的控件
     */
    private void showMusicSelView() {
        if (musicChooseView == null) {
            musicChooseView = new MusicChooser();

            musicChooseView.setRecordTime(getMaxRecordTime());

            musicChooseView.setMusicSelectListener(new MusicSelectListener() {

                @Override
                public void onMusicSelect(MusicFileBean musicFileBean, long startTime) {
                    if (musicFileBean != null) {


                        if (mOutMusicSelectListener != null) {
                            mOutMusicSelectListener.onMusicSelect(musicFileBean, startTime);
                        }
                        //如果音乐地址或者图片地址为空则使用默认图标
                        if (TextUtils.isEmpty(musicFileBean.getImage()) || TextUtils.isEmpty(musicFileBean.getPath())) {
                            mControlView.setMusicIconId(R.mipmap.aliyun_svideo_music);
                        } else {
                            mControlView.setMusicIcon(musicFileBean.getImage());
                        }
                        if (TextUtils.isEmpty(musicFileBean.getPath())) {
                            recorder.removeBackgroundMusic();
                        } else {
                            int during = getMaxRecordTime();
                            if (during > musicFileBean.getDuration()) {
                                during = musicFileBean.getDuration() - 100;
                            }
                            EffectStream effectMusic = new EffectStream.Builder()
                                    .source(new Source(musicFileBean.getMusicId(), musicFileBean.getPath()))
                                    .streamStartTime(startTime, TimeUnit.MILLISECONDS)
                                    .streamDuration(during, TimeUnit.MILLISECONDS)
                                    .build();
                            recorder.applyBackgroundMusic(effectMusic);
                            mConflictEffects.put(TYPE_MUSIC, effectMusic);
                        }
                    } else {
                        mControlView.setMusicIconId(R.mipmap.aliyun_svideo_music);
                    }
                }
            });

            musicChooseView.setDismissListener(new BaseChooser.DialogVisibleListener() {
                @Override
                public void onDialogDismiss() {
                    mControlView.setMusicSelViewShow(false);
                }

                @Override
                public void onDialogShow() {
                    mControlView.setMusicSelViewShow(true);
                }
            });
        }
        musicChooseView.show(getFragmentManager(), TAG_MUSIC_CHOOSER);
    }

    /**
     * 当前所选中的动图路径
     */
    private Source currPaster;

    /**
     * 显示动图效果调节控件
     */
    private void showGifEffectView() {
        if (gifEffectChooser == null) {
            gifEffectChooser = new GIfEffectChooser();
            gifEffectChooser.setDismissListener(this);
            gifEffectChooser.setPasterSelectListener(new PasterSelectListener() {
                int id;

                @Override
                public void onPasterSelected(PreviewPasterForm imvForm) {
                    String path;
                    id = imvForm.getId();
                    if (imvForm.getId() == 150) {
                        //id=150的动图为自带动图
                        path = imvForm.getPath();
                    } else {
                        path = DownloadFileUtils.getAssetPackageDir(getContext(),
                                imvForm.getName(), imvForm.getId()).getAbsolutePath();
                    }
                    currPaster = new Source(String.valueOf(imvForm.getId()), path);
                    addEffectToRecord(currPaster);
                }

                @Override
                public void onSelectPasterDownloadFinish(String path) {
                    // 所选的paster下载完成后, 记录该paster 的path
                    currPaster = new Source(String.valueOf(id), path);
                }
            });

            gifEffectChooser.setDismissListener(new BaseChooser.DialogVisibleListener() {
                @Override
                public void onDialogDismiss() {
                    mControlView.setEffectSelViewShow(false);
                }

                @Override
                public void onDialogShow() {
                    mControlView.setEffectSelViewShow(true);
                    if (currPaster != null && !TextUtils.isEmpty(currPaster.getPath())) {
                        // dialog显示后,如果记录的paster不为空, 使用该paster
                        addEffectToRecord(currPaster);
                    }
                }
            });
        }
        gifEffectChooser.show(getFragmentManager(), TAG_GIF_CHOOSER);
    }

    private void addEffectToRecord(Source source) {

        if (mPasterController != null) {
            recorder.getPasterManager().remove(mPasterController);
        }

        mPasterController = recorder.getPasterManager().addFacePaster(source);
        mPasterController.endEdit();

    }

    /**
     * 设置race单独包的水印
     */
    private void setRaceEffectView() {
        String logo = getContext().getExternalFilesDir("") + "/AliyunDemo/tail/logo.png";
        if (mWaterMarkController != null) {
            recorder.getPasterManager().remove(mWaterMarkController);
        }


        mWaterMarkController = recorder.getPasterManager().addImage(new Source(logo));

        mWaterMarkController.setXRadio(0.13f);
        mWaterMarkController.setYRatio(0.1f);

        float width = 1.0f;
        float height = 1.0f;
        switch (mRatioMode) {
        case AliyunSnapVideoParam.RATIO_MODE_3_4:
            width = (float) 100.0 / ScreenUtils.getRealWidth(mActivity);
            height = (float) 80.0 / ScreenUtils.getRealHeight(mActivity) / 3 * 4;
            break;
        case AliyunSnapVideoParam.RATIO_MODE_1_1:
            width = (float) 100.0 / ScreenUtils.getRealWidth(mActivity);
            height = (float) 80.0 / ScreenUtils.getRealHeight(mActivity) / 9 * 16;
            break;
        default:
            width = (float) 100.0 / ScreenUtils.getRealWidth(mActivity);
            height = (float) 80.0 / ScreenUtils.getRealHeight(mActivity);
            break;
        }

        mWaterMarkController.setWidthRatio(width);
        mWaterMarkController.setHeightRatio(height);
        mWaterMarkController.endEdit();

    }


    private FragmentManager getFragmentManager() {
        FragmentManager fm = null;
        if (mActivity != null) {
            fm = mActivity.getSupportFragmentManager();
        } else {
            Context mContext = getContext();
            if (mContext instanceof FragmentActivity) {
                fm = ((FragmentActivity) mContext).getSupportFragmentManager();
            }
        }
        return fm;
    }

    /**
     * 显示滤镜选择的控件
     */
    private void showFilterEffectView() {
        if (filterEffectChooser == null) {
            filterEffectChooser = new FilterEffectChooser();
        }
        if (filterEffectChooser.isAdded()) {
            return;
        }
        // 滤镜改变listener
        filterEffectChooser.setOnFilterItemClickListener(new OnFilterItemClickListener() {
            @Override
            public void onItemClick(EffectInfo effectInfo, int index) {
                if (effectInfo != null) {

                    if (index == 0) {
                        recorder.removeFilter();
                        mConflictEffects.remove(TYPE_FILTER);
                    } else {
                        EffectFilter filterEffect = new EffectFilter(effectInfo.getSource());
                        recorder.applyFilter(filterEffect);
                        mConflictEffects.put(TYPE_FILTER, filterEffect);
                    }


                }
                currentFilterPosition = index;
            }
        });
        filterEffectChooser.setFilterPosition(currentFilterPosition);
        filterEffectChooser.setDismissListener(new BaseChooser.DialogVisibleListener() {
            @Override
            public void onDialogDismiss() {
                mControlView.setEffectSelViewShow(false);
            }

            @Override
            public void onDialogShow() {
                mControlView.setEffectSelViewShow(true);
            }
        });
        filterEffectChooser.show(getFragmentManager(), TAG_FILTER_CHOOSER);
    }

    /**
     * 显示特效滤镜选择的控件
     */
    private void showAnimFilterEffectView() {
        if (mAnimFilterEffectChooser == null) {
            mAnimFilterEffectChooser = new AnimFilterEffectChooser();
        }
        if (mAnimFilterEffectChooser.isAdded()) {
            return;
        }
        // 滤镜改变listener
        mAnimFilterEffectChooser.setOnAnimFilterItemClickListener(new OnAnimFilterItemClickListener() {
            @Override
            public void onItemClick(EffectFilter effectInfo, int index) {

                String path = null;
                if (effectInfo.getSource() != null) {
                    path = effectInfo.getSource().getPath();
                }
                if (path == null || index == 0) {
                    recorder.removeAnimationFilter(mCurrentAnimFilterEffect);
                } else {
                    mCurrentAnimFilterEffect = effectInfo;
                    recorder.applyAnimationFilter(mCurrentAnimFilterEffect);
                }

                mCurrentAnimFilterPosition = index;
            }

            @Override
            public void onItemUpdate(EffectFilter effectInfo) {
                recorder.updateAnimationFilter(effectInfo);
            }

        });
        mAnimFilterEffectChooser.setFilterPosition(mCurrentAnimFilterPosition);
        mAnimFilterEffectChooser.setDismissListener(new BaseChooser.DialogVisibleListener() {
            @Override
            public void onDialogDismiss() {
                mControlView.setEffectSelViewShow(false);
            }

            @Override
            public void onDialogShow() {
                mControlView.setEffectSelViewShow(true);
            }
        });
        mAnimFilterEffectChooser.show(getFragmentManager(), TAG_ANIM_FILTER_CHOOSER);
    }

    /**
     * 设置录制类型
     */
    public void setRecorder(AlivcIMixRecorderInterface recorder) {
        this.recorder = recorder;

        initRecorder();
        initVideoView();

    }

    private void initRecorder() {
        recorder.setGop(mGop);
        recorder.setRatioMode(mRatioMode);
        recorder.useFlip(mIsUseFlip);
        recorder.setResolutionMode(mResolutionMode);
        clipManager = recorder.getClipManager();
        clipManager.setMaxDuration(getMaxRecordTime());
        clipManager.setMinDuration(minRecordTime);
        recorder.setFocusMode(CameraParam.FOCUS_MODE_CONTINUE);
        //mediaInfo.setHWAutoSize(true);//硬编时自适应宽高为16的倍数
        cameraType = recorder.getCameraCount() == 1 ? com.aliyun.svideosdk.common.struct.recorder.CameraType.BACK : cameraType;
        recorder.setCamera(cameraType);
        recorder.setBeautyLevel(0);

        initOrientationDetector();

        //获取需要的surFaceView数量
        initVideoContainer();

        recorder.setOnFrameCallback(new OnFrameCallback() {
            @Override
            public void onFrameBack(byte[] bytes, int width, int height, Camera.CameraInfo info) {
                //原始数据回调 NV21,这里获取原始数据主要是为了faceUnity高级美颜使用

            }

            @Override
            public Camera.Size onChoosePreviewSize(List<Camera.Size> supportedPreviewSizes,
                                                   Camera.Size preferredPreviewSizeForVideo) {
                return null;
            }

            @Override
            public void openFailed() {
                Log.e(AliyunTag.TAG, "openFailed----------");
            }
        });
        recorder.setOnRecordCallback(new OnRecordCallback() {
            @Override
            public void onClipComplete(final boolean validClip, final long clipDuration) {
                Log.i(TAG, "onComplete   duration : " + clipDuration +
                      ", clipManager.getDuration() = " + clipManager.getDuration());
                tempIsComplete = true;
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "onComplete    isStopToCompleteDuration:" + isStopToCompleteDuration);
                        if (recorder == null) {
                            return;
                        }
                        if (recorder.isMixRecorder() && !isMaxDuration) {
                            ToastUtils.show(mActivity, getResources().getString(R.string.alivc_mix_record_continue), Gravity.CENTER, Toast.LENGTH_SHORT);
                        }

                        isStopToCompleteDuration = false;
                        handleStopCallback(validClip, clipDuration);
                        if (isMaxDuration && validClip) {
                            finishRecording();
                        }

                    }
                });

            }

            /**
             * 合成完毕的回调
             * @param outputPath
             */
            @Override
            public void onFinish(final String outputPath) {
                Log.i(TAG, "onFinish:" + outputPath);

                if (progressBar != null && progressBar.isShowing()) {
                    progressBar.dismiss();
                }

                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCompleteListener != null) {
                            final int duration = clipManager.getDuration();
                            //deleteAllPart();
                            // 选择音乐后, 在录制完合成过程中退后台
                            // 保持在后台情况下, sdk合成完毕后, 会仍然执行跳转代码, 此时会弹起跳转后的页面
                            if (activityStoped) {
                                pendingCompseFinishRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!isStopToCompleteDuration) {
                                            mCompleteListener.onComplete(outputPath, duration, mRatioMode);
                                        }
                                    }
                                };
                            } else {
                                if (!isStopToCompleteDuration) {
                                    mCompleteListener.onComplete(outputPath, duration, mRatioMode);
                                }
                            }
                        }
                        VideoInfoUtils.printVideoInfo(outputPath);
                    }
                });

            }

            @Override
            public void onProgress(final long duration) {
                final int currentDuration = clipManager.getDuration();
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isAllowChangeMv = false;
                        recordTime = 0;
                        //设置录制进度
                        if (mRecordTimeView != null) {
                            mRecordTimeView.setDuration((int) duration);
                        }

                        recordTime = (int) (currentDuration + duration);

                        //Log.d(TAG, "onProgress: " + recordTime + "——————" + currentDuration + "————" + duration);

                        if (recordTime <= clipManager.getMaxDuration() && recordTime >= clipManager.getMinDuration()) {
                            // 2018/7/11 让下一步按钮可点击
                            mControlView.setCompleteEnable(true);
                        } else {
                            mControlView.setCompleteEnable(false);
                        }
                        if (mControlView != null && mControlView.getRecordState().equals(RecordState.STOP)) {
                            return;
                        }
                        if (mControlView != null) {
                            mControlView.setRecordTime(TimeFormatterUtils.formatTime(recordTime));
                        }

                    }
                });

            }

            @Override
            public void onMaxDuration() {
                Log.i(TAG, "onMaxDuration:");
                isMaxDuration = true;
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mControlView != null) {
                            mControlView.setCompleteEnable(false);
                            mControlView.setRecordState(RecordState.STOP);
                            mControlView.updateCutDownView(false);
                        }
                    }
                });

            }

            @Override
            public void onError(int errorCode) {
                Log.e(TAG, "onError:" + errorCode);
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tempIsComplete = true;
                        if (progressBar != null && progressBar.isShowing()) {
                            progressBar.dismiss();
                            mControlView.setCompleteEnable(true);
                        } else {
                            handleStopCallback(false, 0);
                        }
                    }
                });
            }

            @Override
            public void onInitReady() {
                Log.i(TAG, "onInitReady");
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        restoreConflictEffect();
                        if (mPasterController != null) {
                            addEffectToRecord(mPasterController.getSource());
                        }
                    }
                });
            }



        });
        recorder.setOnTextureIdCallback(new OnTextureIdCallback() {
            private boolean firstInit = true;

            private void cheekFaceNum() {
                //根据有无人脸 + 设备性能 判断开启的磨皮类型
                float faceProcessorGetConfidenceScore = FUAIKit.getInstance().getFaceProcessorGetConfidenceScore(0);
                if (faceProcessorGetConfidenceScore >= 0.95) {
                    //高端手机并且检测到人脸开启均匀磨皮，人脸点位质
                    if (FURenderKit.getInstance().getFaceBeauty() != null && FURenderKit.getInstance().getFaceBeauty().getBlurType() != FaceBeautyBlurTypeEnum.EquallySkin) {
                        FURenderKit.getInstance().getFaceBeauty().setBlurType(FaceBeautyBlurTypeEnum.EquallySkin);
                        FURenderKit.getInstance().getFaceBeauty().setEnableBlurUseMask(true);
                    }
                } else {
                    if (FURenderKit.getInstance().getFaceBeauty() != null && FURenderKit.getInstance().getFaceBeauty().getBlurType() != FaceBeautyBlurTypeEnum.FineSkin) {
                        FURenderKit.getInstance().getFaceBeauty().setBlurType(FaceBeautyBlurTypeEnum.FineSkin);
                        FURenderKit.getInstance().getFaceBeauty().setEnableBlurUseMask(false);
                    }
                }
            }

            @Override
            public int onTextureIdBack(int textureId, int textureWidth, int textureHeight, float[] matrix) {
                if (mFURenderer == null) {
                    return textureId;
                }
                if (firstInit) {
                    firstInit = false;
                    initCsvUtil(getContext());
                    mFURenderer.prepareRenderer(mFURendererListener);
                }

                if (FUConfig.DEVICE_LEVEL > FuDeviceUtils.DEVICE_LEVEL_MID) {
                    cheekFaceNum();
                }

                long start = System.nanoTime();
                int texId = mFURenderer.onDrawFrameDualInput(null, textureId, textureWidth, textureHeight);
                long time = System.nanoTime() - start;
                if (mCSVUtils != null) {
                    mCSVUtils.writeCsv(null, time);
                }
                if (mSkippedFrames > 0) {
                    mSkippedFrames--;
                    texId = textureId;
                }
                return texId;
            }

            @Override
            public int onScaledIdBack(int scaledId, int textureWidth, int textureHeight, float[] matrix) {

                return scaledId;
            }

            @Override
            public void onTextureDestroyed() {
                // sdk3.7.8改动, 自定义渲染（第三方渲染）销毁gl资源，以前GLSurfaceView时可以通过GLSurfaceView.queueEvent来做，
                // 现在增加了一个gl资源销毁的回调，需要统一在这里面做。
                if (mFURenderer != null) {
                    mFURenderer.release();
                }
                if (mCSVUtils != null) {
                    mCSVUtils.close();
                }
            }
        });

        recorder.setFaceTrackInternalMaxFaceCount(2);
        recorder.setMixBorderParam(mMixBorderParam);
    }


    public void setFaceTrackModePath() {
        ThreadUtils.runOnSubThread(new Runnable() {
            @Override
            public void run() {
                String path = getContext().getExternalFilesDir("") + File.separator + RecordCommon.QU_NAME + File.separator;
                if (recorder != null) {
                    recorder.needFaceTrackInternal(true);
                    recorder.setFaceTrackInternalModelPath(path + "/model");
//                    防止第一次添加水印时，资源未加载成功，导致添加失败
                    if (isSvideoRace) {
                        setRaceEffectView();
                    }
                }
            }
        });

    }

    public void setRecordMute(boolean recordMute) {
        if (recorder != null) {
            recorder.setMute(recordMute);
        }
    }

    private void initOrientationDetector() {
        orientationDetector = new OrientationDetector(getContext().getApplicationContext());
        orientationDetector.setOrientationChangedListener(new OrientationDetector.OrientationChangedListener() {
            @Override
            public void onOrientationChanged() {
                int rotation = getCameraRotation();
                recorder.setRotation(rotation);
                if (mFURenderer != null) {
                    mFURenderer.setDeviceOrientation(rotation);
                }
            }
        });
    }

    private int getCameraRotation() {
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

        if (Camera.getNumberOfCameras() > cameraType.getType()) {

            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraType.getType(), cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                if (rotation != 0) {
                    rotation = 360 - rotation;
                }
            }
        }
        return rotation;
    }


    private boolean activityStoped;
    private Runnable pendingCompseFinishRunnable;

    /**
     * 开始预览
     */
    public void startPreview() {

        if (recorder == null) {
            return;
        }

        activityStoped = false;
        if (pendingCompseFinishRunnable != null) {
            pendingCompseFinishRunnable.run();
        }
        pendingCompseFinishRunnable = null;

        recorder.startPreview();
        if (isAllowChangeMv) {
            restoreConflictEffect();
        }
        //            recorder.setZoom(scaleFactor);
        if (clipManager.getDuration() >= clipManager.getMinDuration() || isMaxDuration) {
            // 2018/7/11 让下一步按钮可点击
            mControlView.setCompleteEnable(true);
        } else {
            mControlView.setCompleteEnable(false);
        }
        if (orientationDetector != null && orientationDetector.canDetectOrientation()) {
            orientationDetector.enable();
        }

        mCountDownView.setOnCountDownFinishListener(new AlivcCountDownView.OnCountDownFinishListener() {
            @Override
            public void onFinish() {
                FixedToastUtils.show(getContext(), getResources().getString(R.string.alivc_recorder_record_start_recorder));
                startRecord();
            }
        });

    }

    /**
     * 结束预览
     */
    public void stopPreview() {
        if (recorder == null) {
            return;
        }
        activityStoped = true;
        if (mControlView != null && mCountDownView != null && mControlView.getRecordState().equals(RecordState.READY)) {
            mCountDownView.cancle();
            mControlView.setRecordState(RecordState.STOP);
            mControlView.setRecording(false);
        }

        if (mControlView != null && mControlView.getRecordState().equals(RecordState.RECORDING)) {
            recorder.stopRecording();
        }
        recorder.stopPreview();

        if (orientationDetector != null) {
            orientationDetector.disable();
        }

        if (mControlView != null && mControlView.getFlashType() == FlashType.ON
                && mControlView.getCameraType() == CameraType.BACK) {
            mControlView.setFlashType(FlashType.OFF);
        }
    }

    /**
     * 销毁录制，在activity或者fragment被销毁时调用此方法
     */
    public void destroyRecorder() {

        //destroy时删除多段录制的片段文件
//        deleteSliceFile();//通过AlivcIMixRecorderInterface#setIsAutoClearClipVideos(true)处理
        if (finishRecodingTask != null) {
            finishRecodingTask.cancel(true);
            finishRecodingTask = null;
        }

        if (faceTrackPathTask != null) {
            faceTrackPathTask.cancel(true);
            faceTrackPathTask = null;
        }

        if (recorder != null) {
            recorder.release();
            recorder = null;
            Log.i(TAG, "recorder destroy");
        }

        if (orientationDetector != null) {
            orientationDetector.setOrientationChangedListener(null);
        }

        AnimFilterLoadingView.clearCacheEffectFilter();

    }

    /**
     * 删除多段录制临时文件
     */
    private void deleteSliceFile() {
        if (recorder != null) {
            recorder.getClipManager().deleteAllPart();
        }
    }

    /**
     * 结束录制，并且将录制片段视频拼接成一个视频 跳转editorActivity在合成完成的回调的方法中，{@link AlivcSvideoRecordActivity#()}
     */
    private void finishRecording() {
        //弹窗提示
        if (progressBar == null) {
            progressBar = new ProgressDialog(getContext());
            progressBar.setMessage(getResources().getString(R.string.alivc_recorder_record_create_video));
            progressBar.setCanceledOnTouchOutside(false);
            progressBar.setCancelable(false);
            progressBar.setProgressStyle(android.app.ProgressDialog.STYLE_SPINNER);
        }
        progressBar.show();
        mControlView.setCompleteEnable(false);
        finishRecodingTask = new FinishRecodingTask(this).executeOnExecutor(
            AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * 录制结束的AsyncTask
     */
    public static class FinishRecodingTask extends AsyncTask<Void, Integer, Integer> {
        WeakReference<AliyunSVideoRecordView> weakReference;

        FinishRecodingTask(AliyunSVideoRecordView recordView) {
            weakReference = new WeakReference<>(recordView);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            if (weakReference == null) {
                return -1;
            }

            AliyunSVideoRecordView recordView = weakReference.get();
            if (recordView != null) {
                Log.i(TAG, "finishRecording");
                return recordView.recorder.finishRecording();

            }
            return -1;
        }

        @Override
        protected void onPostExecute(Integer code) {
            if (weakReference == null) {
                return;
            }
            if (code != AliyunErrorCode.ALIVC_COMMON_RETURN_SUCCESS) {
                Log.e(TAG, "合成失败 错误码 : " + code);
                AliyunSVideoRecordView recordView = weakReference.get();
                if (recordView != null) {
                    ToastUtils.show(recordView.getContext(), R.string.alivc_record_mix_compose_failure);
                    if (recordView.progressBar != null && recordView.progressBar.isShowing()) {
                        recordView.progressBar.dismiss();
                        recordView.mControlView.setCompleteEnable(true);
                    }
                }
            }
        }
    }

    /**
     * 片段录制完成的回调处理
     *
     * @param isValid
     * @param duration
     */
    private void handleStopCallback(final boolean isValid, final long duration) {


        mControlView.setRecordState(RecordState.STOP);
        if (mControlView != null) {
            mControlView.setRecording(false);
        }

        if (!isValid) {
            if (mRecordTimeView != null) {
                mRecordTimeView.setDuration(0);

                if (mRecordTimeView.getTimelineDuration() == 0) {
                    mControlView.setHasRecordPiece(false);
                }
            }
            return;
        }

        if (mRecordTimeView != null) {
            mRecordTimeView.setDuration((int) duration);
            mRecordTimeView.clipComplete();
            mControlView.setHasRecordPiece(true);
            isAllowChangeMv = false;
        }
    }

    /**
     * addSubView 添加子view到布局中
     *
     * @param view 子view
     */
    private void addSubView(View view) {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(view, params);//添加到布局中
    }

    /**
     * 录制界面返回按钮click listener
     */
    private OnBackClickListener mBackClickListener;

    public void setBackClickListener(OnBackClickListener listener) {
        this.mBackClickListener = listener;
    }

    private OnFinishListener mCompleteListener;


    @Override
    public void onDialogDismiss() {
        if (mControlView != null) {
            mControlView.setEffectSelViewShow(false);
        }
    }

    @Override
    public void onDialogShow() {
        if (mControlView != null) {
            mControlView.setEffectSelViewShow(true);
        }
    }

    /**
     * 删除所有录制文件
     */
    public void deleteAllPart() {
        if (clipManager != null) {
            clipManager.deleteAllPart();
            if (clipManager.getDuration() < clipManager.getMinDuration() && mControlView != null) {
                mControlView.setCompleteEnable(false);
            }
            if (clipManager.getDuration() == 0) {
                // 音乐可以选择
                //                    musicBtn.setVisibility(View.VISIBLE);
                //                    magicMusic.setVisibility(View.VISIBLE);
                //recorder.restartMv();
                mRecordTimeView.clear();
                mControlView.setHasRecordPiece(false);
                isAllowChangeMv = true;
            }
        }
    }

    private float lastScaleFactor;
    private float scaleFactor;

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
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

    /**
     * 返回按钮事件监听
     */
    public interface OnBackClickListener {
        void onClick();
    }

    /**
     * 录制完成事件监听
     */
    public interface OnFinishListener {
        void onComplete(String path, int duration, int ratioMode);
    }

    public void setCompleteListener(OnFinishListener mCompleteListener) {
        this.mCompleteListener = mCompleteListener;
    }

    public void setActivity(FragmentActivity mActivity) {
        this.mActivity = mActivity;
    }

    /**
     * 获取最大录制时长
     *
     * @return
     */
    public int getMaxRecordTime() {
        if (maxRecordTime < MIN_RECORD_TIME) {
            return MIN_RECORD_TIME;
        } else if (maxRecordTime > MAX_RECORD_TIME) {
            return MAX_RECORD_TIME;
        } else {

            return maxRecordTime;
        }

    }

    /**
     * 设置录制时长
     *
     * @param maxRecordTime
     */
    public void setMaxRecordTime(int maxRecordTime) {
        this.maxRecordTime = maxRecordTime;
    }

    /**
     * 设置最小录制时长
     *
     * @param minRecordTime
     */
    public void setMinRecordTime(int minRecordTime) {
        this.minRecordTime = minRecordTime;
    }

    /**
     * 设置Gop
     *
     * @param mGop
     */
    public void setGop(int mGop) {
        this.mGop = mGop;

    }

    /**
     * 重新绘制SurFaceView的比例
     */
    public void setReSizeRatioMode(int ratioMode) {
        this.mRatioMode = ratioMode;
        recorder.setRatioMode(ratioMode);
        mRecorderSurfaceView.setLayoutParams(recorder.getLayoutParams());
//        切换画幅比例后重新添加水印
        if (isSvideoRace) {
            setRaceEffectView();
        }
    }

    /**
     * 设置视频比例
     *
     * @param ratioMode
     */
    public void setRatioMode(int ratioMode) {
        this.mRatioMode = ratioMode;
    }

    /**
     * 设置播放视频的路径
     */
    public void setVideoPath(String path) {
        this.mMixVideoPath = path;
    }


    /**
     * 设置渲染方式
     *
     * @param mRenderingMode
     */
    public void setRenderingMode(BeautySDKType mRenderingMode) {
        this.mRenderingMode = mRenderingMode;
    }

    /**
     * 设置是否是race录制包
     *
     * @param isSvideoRace 是否是race录制包
     */
    public void setSvideoRace(boolean isSvideoRace) {
        this.isSvideoRace = isSvideoRace;
    }

    public void setMixBorderParam(AlivcMixBorderParam param) {
        mMixBorderParam = param;
    }

    /**
     * 设置视频码率
     *
     * @param mResolutionMode
     */
    public void setResolutionMode(int mResolutionMode) {
        this.mResolutionMode = mResolutionMode;
    }


    public void setOnMusicSelectListener(MusicSelectListener musicSelectListener) {
        this.mOutMusicSelectListener = musicSelectListener;
    }

    /**
     * 获取滤镜名称 适配系统语言/中文或其他
     *
     * @param path 滤镜文件目录
     * @return name
     */
    private String getFilterName(String path) {
        if (path == null) {
            return null;
        }
        if (LanguageUtils.isCHEN(getContext())) {
            path = path + "/config.json";
        } else {
            path = path + "/configEn.json";
        }
        String name = "";
        StringBuffer var2 = new StringBuffer();
        File var3 = new File(path);

        try {
            FileReader var4 = new FileReader(var3);

            int var7;
            while ((var7 = var4.read()) != -1) {
                var2.append((char) var7);
            }

            var4.close();
        } catch (IOException var6) {
            var6.printStackTrace();
        }

        try {
            JSONObject var4 = new JSONObject(var2.toString());
            name = var4.optString("name");
        } catch (JSONException var5) {
            var5.printStackTrace();
        }

        return name;

    }

    private void initCsvUtil(Context context) {
        mCSVUtils = new CSVUtils(context);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
        String dateStrDir = format.format(new Date(System.currentTimeMillis()));
        dateStrDir = dateStrDir.replaceAll("-", "").replaceAll("_", "");
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault());
        String dateStrFile = df.format(new Date());
        String filePath = Constant.filePath + dateStrDir + File.separator + "excel-" + dateStrFile + ".csv";
        Log.d(TAG, "initLog: CSV file path:" + filePath);
        StringBuilder headerInfo = new StringBuilder();
        headerInfo.append("version：").append(FURenderer.getInstance().getVersion()).append(CSVUtils.COMMA)
                .append("机型：").append(android.os.Build.MANUFACTURER).append(android.os.Build.MODEL)
                .append("处理方式：Texture").append(CSVUtils.COMMA);
        mCSVUtils.initHeader(filePath, headerInfo);
    }
}