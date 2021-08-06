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
import android.opengl.EGL14;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
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
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.common.global.AliyunTag;
import com.aliyun.common.utils.BitmapUtil;
import com.aliyun.common.utils.CommonUtil;
import com.aliyun.common.utils.DensityUtil;
import com.aliyun.svideo.common.utils.image.ImageLoaderImpl;
import com.aliyun.svideo.common.utils.image.ImageLoaderOptions;
import com.aliyun.svideo.recorder.bean.AlivcMixBorderParam;
import com.aliyun.svideo.recorder.bean.VideoDisplayParam;
import com.aliyun.svideosdk.common.struct.common.AliyunSnapVideoParam;
import com.aliyun.svideosdk.mixrecorder.AliyunMixMediaInfoParam;
import com.aliyun.svideosdk.common.AliyunErrorCode;
import com.aliyun.svideosdk.recorder.AliyunIClipManager;
import com.aliyun.svideosdk.recorder.RecordCallback;
import com.aliyun.svideo.base.Constants;
import com.aliyun.svideo.base.http.MusicFileBean;
import com.aliyun.svideo.base.utils.VideoInfoUtils;
import com.aliyun.svideo.base.widget.ProgressDialog;
import com.aliyun.svideo.base.widget.RecordTimelineView;
import com.aliyun.svideo.base.widget.beauty.BeautyConstants;
import com.aliyun.svideo.base.widget.beauty.BeautyDetailSettingView;
import com.aliyun.svideo.base.widget.beauty.BeautyParams;
import com.aliyun.svideo.base.widget.beauty.BeautyRaceConstants;
import com.aliyun.svideo.base.widget.beauty.BeautyShapeConstants;
import com.aliyun.svideo.base.widget.beauty.enums.BeautyLevel;
import com.aliyun.svideo.base.widget.beauty.enums.BeautyMode;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautyDetailClickListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautyFaceItemSeletedListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautyModeChangeListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautyParamsChangeListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautyShapeItemSeletedListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautyShapeParamsChangeListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautySkinItemSeletedListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnBeautyTableItemSeletedListener;
import com.aliyun.svideo.base.widget.beauty.listener.OnViewClickListener;
import com.aliyun.svideo.base.widget.beauty.sharp.BeautyShapeParams;
import com.aliyun.svideo.common.utils.DensityUtils;
import com.aliyun.svideo.common.utils.LanguageUtils;
import com.aliyun.svideo.common.utils.PermissionUtils;
import com.aliyun.svideo.common.utils.ScreenUtils;
import com.aliyun.svideo.common.utils.ThreadUtils;
import com.aliyun.svideo.common.utils.ToastUtils;
import com.aliyun.svideo.common.utils.UriUtils;
import com.aliyun.svideo.downloader.zipprocessor.DownloadFileUtils;
import com.aliyun.svideo.record.R;
import com.aliyun.svideo.recorder.activity.AlivcSvideoRecordActivity;
import com.aliyun.svideo.recorder.bean.RememberBeautyBean;
import com.aliyun.svideo.recorder.bean.RememberBeautyShapeBean;
import com.aliyun.svideo.recorder.bean.RenderingMode;
import com.aliyun.svideo.recorder.mixrecorder.AlivcIMixRecorderInterface;
import com.aliyun.svideo.recorder.profile.CSVUtils;
import com.aliyun.svideo.recorder.profile.Constant;
import com.aliyun.svideo.recorder.race.RaceManager;
import com.aliyun.svideo.recorder.util.FixedToastUtils;
import com.aliyun.svideo.recorder.util.OrientationDetector;
import com.aliyun.svideo.recorder.util.PreferenceUtil;
import com.aliyun.svideo.recorder.util.RecordCommon;
import com.aliyun.svideo.recorder.util.SharedPreferenceUtils;
import com.aliyun.svideo.recorder.util.TimeFormatterUtils;
import com.aliyun.svideo.recorder.view.control.CameraType;
import com.aliyun.svideo.recorder.view.control.ControlView;
import com.aliyun.svideo.recorder.view.control.ControlViewListener;
import com.aliyun.svideo.recorder.view.control.FlashType;
import com.aliyun.svideo.recorder.view.control.RecordState;
import com.aliyun.svideo.recorder.view.countdown.AlivcCountDownView;
import com.aliyun.svideo.recorder.view.dialog.AnimFilterEffectChooser;
import com.aliyun.svideo.recorder.view.dialog.BeautyEffectChooser;
import com.aliyun.svideo.recorder.view.dialog.DialogVisibleListener;
import com.aliyun.svideo.recorder.view.dialog.FilterEffectChooser;
import com.aliyun.svideo.recorder.view.dialog.GIfEffectChooser;
import com.aliyun.svideo.recorder.view.effects.face.BeautyFaceDetailChooser;
import com.aliyun.svideo.recorder.view.effects.face.BeautyService;
import com.aliyun.svideo.recorder.view.effects.filter.EffectInfo;
import com.aliyun.svideo.recorder.view.effects.filter.OnFilterItemClickListener;
import com.aliyun.svideo.recorder.view.effects.filter.animfilter.AnimFilterLoadingView;
import com.aliyun.svideo.recorder.view.effects.filter.animfilter.OnAnimFilterItemClickListener;
import com.aliyun.svideo.recorder.view.effects.paster.PasterSelectListener;
import com.aliyun.svideo.recorder.view.effects.shape.BeautyShapeDetailChooser;
import com.aliyun.svideo.recorder.view.effects.skin.BeautySkinDetailChooser;
import com.aliyun.svideo.recorder.view.focus.FocusView;
import com.aliyun.svideo.recorder.view.music.MusicChooser;
import com.aliyun.svideo.recorder.view.music.MusicSelectListener;
import com.aliyun.svideosdk.common.struct.common.VideoQuality;
import com.aliyun.svideosdk.common.struct.effect.EffectBean;
import com.aliyun.svideosdk.common.struct.effect.EffectFilter;
import com.aliyun.svideosdk.common.struct.effect.EffectImage;
import com.aliyun.svideosdk.common.struct.effect.EffectPaster;
import com.aliyun.svideosdk.common.struct.encoder.VideoCodecs;
import com.aliyun.svideosdk.common.struct.form.PreviewPasterForm;
import com.aliyun.svideosdk.common.struct.recorder.CameraParam;
import com.aliyun.svideosdk.common.struct.recorder.MediaInfo;
import com.aliyun.svideosdk.mixrecorder.AliyunMixMediaInfoParam;
import com.faceunity.core.enumeration.CameraFacingEnum;
import com.faceunity.core.enumeration.FUAIProcessorEnum;
import com.faceunity.core.enumeration.FUInputTextureEnum;
import com.faceunity.core.enumeration.FUTransformMatrixEnum;
import com.faceunity.core.utils.CameraUtils;
import com.faceunity.nama.FURenderer;
import com.faceunity.nama.data.FaceUnityDataFactory;
import com.faceunity.nama.listener.FURendererListener;
import com.faceunity.nama.ui.FaceUnityView;
import com.google.gson.Gson;
import com.aliyun.svideosdk.common.callback.recorder.OnFrameCallBack;
import com.aliyun.svideosdk.common.callback.recorder.OnTextureIdCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 新版本(> 3.6.5之后)录制模块的具体业务实现类
 */
public class AliyunSVideoRecordView extends FrameLayout
    implements DialogVisibleListener, ScaleGestureDetector.OnScaleGestureListener {
    private static final String TAG = AliyunSVideoRecordView.class.getSimpleName();
    private static final String TAG_GIF_CHOOSER = "gif";
    private static final String TAG_BEAUTY_CHOOSER = "beauty";
    private static final String TAG_MUSIC_CHOOSER = "music";
    private static final String TAG_FILTER_CHOOSER = "filter";
    private static final String TAG_ANIM_FILTER_CHOOSER = "anim_filter";
    private static final String TAG_BEAUTY_DETAIL_FACE_CHOOSER = "beautyFace";
    private static final String TAG_BEAUTY_DETAIL_SKIN_CHOOSER = "beautySkin";
    private static final String TAG_BEAUTY_DETAIL_SHAPE_CHOOSER = "beautyShape";
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
    private ImageView mBackgroundImageView;
    private ControlView mControlView;
    private RecordTimelineView mRecordTimeView;
    private AlivcCountDownView mCountDownView;

    private AlivcIMixRecorderInterface recorder;

    private AliyunIClipManager clipManager;
    private com.aliyun.svideosdk.common.struct.recorder.CameraType cameraType
            = com.aliyun.svideosdk.common.struct.recorder.CameraType.FRONT;
    private FragmentActivity mActivity;
    private boolean isOpenFailed = false;
    //正在准备录制视频,readyview显示期间为true，其他为false
    private boolean isLoadingReady = false;
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
    //视频质量
    private VideoQuality mVideoQuality = VideoQuality.HD;
    //视频比例
    private int mRatioMode = AliyunSnapVideoParam.RATIO_MODE_3_4;
    //编码方式
    private VideoCodecs mVideoCodec = VideoCodecs.H264_HARDWARE;
    //渲染方式
//    private RenderingMode mRenderingMode = RenderingMode.Race;
    //是否是race录制包
    private boolean isSvideoRace = false;

    private AlivcMixBorderParam mMixBorderParam;

    //视频输出参数
    private MediaInfo mOutputInfo;

    //合拍输入视频参数
    private AliyunMixMediaInfoParam mMixInputInfo;

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

    private BeautyEffectChooser beautyEffectChooser;
    //选中的贴图效果
    private EffectPaster effectPaster;
    private OrientationDetector orientationDetector;
    private int rotation;
    private MusicChooser musicChooseView;
    /**
     * 第三方高级美颜支持库faceUnity管理类
     */
//    private FaceUnityManager faceUnityManager;
    private RaceManager raceManager;
    /**
     * 相机的原始NV21数据
     */
    private byte[] frameBytes;
    private byte[] mFuImgNV21Bytes;
    /**
     * 原始数据宽
     */
    private int frameWidth;
    /**
     * 原始数据高
     */
    private int frameHeight;
    /**
     * faceUnity相关
     */
    private int mFrameId = 0;
    private BeautyParams beautyParams;
    private BeautyShapeParams beautyShapeParams;
    private BeautyFaceDetailChooser beautyFaceDetailChooser;
    private BeautySkinDetailChooser beautySkinDetailChooser;
    private BeautyShapeDetailChooser beautyShapeDetailChooser;
    /**
     * 美肌美颜微调dialog是否正在显示
     */
    private boolean isbeautyDetailShowing;

    /**
     * 美颜默认档位
     */
    private BeautyLevel defaultBeautyLevel = BeautyLevel.BEAUTY_LEVEL_THREE;

    /**
     * 当前美颜模式
     */
    private BeautyMode currentBeautyFaceMode = BeautyMode.Advanced;
    public static final int TYPE_FILTER = 1;
    public static final int TYPE_MUSIC = 3;
    private LinkedHashMap<Integer, Object> mConflictEffects = new LinkedHashMap<>();
    private EffectBean effectMusic;
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
     * 记录美颜选中的索引, 默认为3
     */
    private int currentBeautyFacePosition = 3;

    /**
     * 记录美颜选中的索引, 默认为3
     */
    private int currentBeautyFaceNormalPosition = 3;

    /**
     * 当前美肌选择的item下标, 默认为3
     */
    private int currentBeautySkinPosition = 3;
    /**
     * 当前美型选择的item下标, 默认为0
     */
    private int currentBeautyShapePosition = 0;
    /**
     * 控制mv的添加, 开始录制后,不允许切换mv
     */
    private boolean isAllowChangeMv = true;
    private AsyncTask<Void, Void, Void> mFaceUnityTask;
    private List<BeautyParams> rememberParamList;
    private RememberBeautyBean rememberBeautyBean;
    private List<BeautyParams> rememberRaceParamList;
    private RememberBeautyBean rememberRaceBeautyBean;
    private List<BeautyShapeParams> rememberShapeParamList;
    private RememberBeautyShapeBean rememberBeautyShapeBean;
    private ProgressDialog progressBar;

    /**
     * 用于判断当前音乐界面是否可见, 如果可见, 从后台回到前台时, restoreConflictEffect()不恢复mv的播放, 否则会和音乐重复播放
     * <p>
     * true: 可见 false: 不可见
     */
    private boolean isMusicViewShowing;

    /**
     * faceUnity的初始化结果 true: 初始化成功 false: 初始化失败
     */
    private static boolean faceInitResult;
    /**
     * 是否处于后台
     */
    private boolean mIsBackground;
    private BeautyService beautyService;

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

    private boolean isRaceDrawed = false;

    /**
     * race单独录制的水印
     */
    private EffectImage effectImage;

    /**
     * faceunity 美颜贴纸
     */
    private FaceUnityDataFactory mFaceUnityDataFactory;
    private FURenderer mFURenderer;
    private boolean mIsFuBeautyOpen;
    private int mSkippedFrames = 5;
    private TextView tvFps;

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
                    EffectBean music = (EffectBean) entry.getValue();

                    if (music != null) {
                        recorder.setMusic(music.getPath(), music.getStartTime(), music.getDuration());
                        Log.i(TAG, "path :" + music.getPath());
                        // 根据音乐路径判断是否添加了背景音乐,
                        // 在编辑界面, 如果录制添加了背景音乐, 则不能使用音效特效
                        isHasMusic = !TextUtils.isEmpty(music.getPath());
                    }
                    break;
                default:
                    break;
                }
            }
        }
    }

    /**
     * 底层在onPause时会回收资源, 此处选择的滤镜的资源路径, 用于恢复状态
     */
    private String filterSourcePath;

    /**
     * 美颜美肌点击了back按钮
     */
    private boolean isbeautyDetailBack;

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
        initBeautyParam();
        initRecordTimeView();
        initFocusView();
        setFaceTrackModePath();

        initFuBeautyView();
    }

    /**
     * 焦点focus
     */
    private void initFocusView() {
        mFocusView = new FocusView(getContext());
        mFocusView.setPadding(10, 10, 10, 10);
        addSubView(mFocusView);
    }

    private void initBeautyParam() {
        beautyParamCopy();
        //进来前应该获取上一次记住的美颜模式
        currentBeautyFaceMode = SharedPreferenceUtils.getBeautyMode(getContext());

        currentBeautySkinPosition = SharedPreferenceUtils.getBeautySkinLevel(getContext());
        currentBeautyFacePosition = SharedPreferenceUtils.getBeautyFaceLevel(getContext());
        currentBeautyFaceNormalPosition = SharedPreferenceUtils.getBeautyNormalFaceLevel(getContext());
        currentBeautyShapePosition = SharedPreferenceUtils.getBeautyShapeLevel(getContext());
    }

    private void beautyParamCopy() {

//        faceunity美颜
        rememberBeautyBean = new RememberBeautyBean();
        rememberParamList = new ArrayList<>();
        int size = BeautyConstants.BEAUTY_MAP.size();
        // 需求要记录之前修改的美颜参数, 所以每次先读取json中的数据,如果json无数据, 就从常量中拿
        String jsonBeautyParam = SharedPreferenceUtils.getBeautyParams(getContext());
        if (TextUtils.isEmpty(jsonBeautyParam)) {
            for (int i = 0; i < size; i++) {
                BeautyParams beautyParams = BeautyConstants.BEAUTY_MAP.get(i);
                BeautyParams rememberParam = beautyParams.clone();
                rememberParamList.add(rememberParam);
            }
        } else {
            for (int i = 0; i < size; i++) {
                BeautyParams beautyParams = getBeautyParams(i);
                if (beautyParams == null) {
                    BeautyParams defaultBeautyParams = BeautyConstants.BEAUTY_MAP.get(i);
                    beautyParams = defaultBeautyParams.clone();
                }
                rememberParamList.add(beautyParams);
            }
        }
        rememberBeautyBean.setBeautyList(rememberParamList);


//        race美颜
        rememberRaceBeautyBean = new RememberBeautyBean();
        rememberRaceParamList = new ArrayList<>();
        int raceSize = BeautyRaceConstants.BEAUTY_MAP.size();
        // 需求要记录之前修改的美颜参数, 所以每次先读取json中的数据,如果json无数据, 就从常量中拿
        String jsonRaceBeautyParam = SharedPreferenceUtils.getRaceBeautyParams(getContext());
        if (TextUtils.isEmpty(jsonRaceBeautyParam)) {
            for (int i = 0; i < raceSize; i++) {
                BeautyParams beautyParams = BeautyRaceConstants.BEAUTY_MAP.get(i);
                BeautyParams rememberParam = beautyParams.clone();
                rememberRaceParamList.add(rememberParam);
            }
        } else {
            for (int i = 0; i < raceSize; i++) {
                BeautyParams beautyParams = getBeautyParams(i);
                rememberRaceParamList.add(beautyParams);
            }
        }
        rememberRaceBeautyBean.setBeautyList(rememberRaceParamList);

//      美型
        rememberBeautyShapeBean = new RememberBeautyShapeBean();

        rememberShapeParamList = new ArrayList<>();

        int shapeSize = BeautyShapeConstants.BEAUTY_MAP.size();
        // 需求要记录之前修改的美颜参数, 所以每次先读取json中的数据,如果json无数据, 就从常量中拿
        String jsonBeautyShapeParam = SharedPreferenceUtils.getBeautyShapeParams(getContext());
        if (TextUtils.isEmpty(jsonBeautyShapeParam)) {
            for (int i = 0; i < shapeSize; i++) {
                BeautyShapeParams beautyParams = BeautyShapeConstants.BEAUTY_MAP.get(i);
                BeautyShapeParams rememberParam = beautyParams.clone();
                rememberShapeParamList.add(rememberParam);
            }
        } else {
            for (int i = 0; i < shapeSize; i++) {
                BeautyShapeParams beautyParams = getBeautyShapeParams(i);
                if (beautyParams == null) {
                    beautyParams = BeautyShapeConstants.BEAUTY_MAP.get(i);
                }
                rememberShapeParamList.add(beautyParams);
            }
        }
        rememberBeautyShapeBean.setBeautyList(rememberShapeParamList);
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
            post(() -> tvFps.setText("FPS: " + FPS));
        }

        @Override
        public void onRelease() {

        }
    };

    private void initFuBeautyView() {
        Context context = getContext();
        mIsFuBeautyOpen = TextUtils.equals(PreferenceUtil.VALUE_ON, PreferenceUtil.getString(context, PreferenceUtil.KEY_FACEUNITY_IS_ON));
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
            mFaceUnityDataFactory = new FaceUnityDataFactory(0);
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

    private void initVideoContainer(){
        mVideoContainer = new FrameLayout(getContext());
        int videoOutputWidth = recorder.getVideoWidth();
        int videoOutputHeight = recorder.getVideoHeight();
        int width = ScreenUtils.getRealWidth(getContext());
        int height = width*videoOutputHeight/videoOutputWidth;
        LayoutParams params = new LayoutParams(width, height);
        params.gravity = Gravity.CENTER;
        addView(mVideoContainer,params);
        if (recorder.isMixRecorder()) {
            int backgroundColor = recorder.getBackgroundColor();
            mVideoContainer.setBackgroundColor(backgroundColor);
            String backgroundImage = recorder.getBackgroundImage();
            if (!TextUtils.isEmpty(backgroundImage)){
                int displayMode = recorder.getBackgroundImageDisplayMode();
                mBackgroundImageView = new ImageView(getContext());
                switch (displayMode){
                    case 0:
                        mBackgroundImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        break;
                    case 1:
                        mBackgroundImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        break;
                    case 2:
                        mBackgroundImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        break;
                }
                mBackgroundImageView.setBackgroundColor(backgroundColor);
                LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
                mVideoContainer.addView(mBackgroundImageView,layoutParams);
                new ImageLoaderImpl().loadImage(getContext(), "file://" + backgroundImage, new ImageLoaderOptions.Builder().skipDiskCacheCache().skipMemoryCache().build()).into(mBackgroundImageView);
            }
            //调整布局级别
            if (recorder.getPlayDisplayParams().getLayoutLevel()>recorder.getRecordDisplayParam().getLayoutLevel()){
                initRecorderSurfaceView();
                initPlayerSurfaceView();
            }else {
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

        if(mMixBorderParam != null){
            border = mMixBorderParam.getBorderWidth();
            color = mMixBorderParam.getBorderColor();
            tempRadius = mMixBorderParam.getCornerRadius();
        }

        final float radius = tempRadius;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
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
        int parentHeight = parentWidth*videoOutputHeight/videoOutputWidth;
        int width = (int)(parentWidth*displayParam.getWidthRatio());
        int height = (int)(parentHeight*displayParam.getHeightRatio());
        int marginLeft = (int)((displayParam.getCenterX()-displayParam.getWidthRatio()/2)*parentWidth);
        int marginTop = (int)((displayParam.getCenterY()-displayParam.getHeightRatio()/2)*parentHeight);

        FrameLayout container = new FrameLayout(getContext());
        container.setBackgroundColor(color);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
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

        LayoutParams layoutParams = new LayoutParams(width,height);
        layoutParams.leftMargin = marginLeft;
        layoutParams.topMargin = marginTop;
        mVideoContainer.addView(container,layoutParams);
    }

    /**
     * 初始化PlayerSurfaceView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initPlayerSurfaceView() {
        mPlayerSurfaceView = new SurfaceView(getContext());
        addSubView(mPlayerSurfaceView);
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
                showBeautyFaceView();
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
                        mFURenderer.setInputOrientation(CameraUtils.INSTANCE.getCameraOrientation(type));
                        if (cameraType == CameraFacingEnum.CAMERA_FRONT) {
                            mFURenderer.setInputBufferMatrix(FUTransformMatrixEnum.CCROT0_FLIPVERTICAL);
                            mFURenderer.setInputTextureMatrix(FUTransformMatrixEnum.CCROT0_FLIPVERTICAL);
                            mFURenderer.setOutputMatrix(FUTransformMatrixEnum.CCROT0);
                        }else {
                            mFURenderer.setInputBufferMatrix(FUTransformMatrixEnum.CCROT0);
                            mFURenderer.setInputTextureMatrix(FUTransformMatrixEnum.CCROT0);
                            mFURenderer.setOutputMatrix(FUTransformMatrixEnum.CCROT0_FLIPVERTICAL);
                        }
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
                    recorder.restartMv();
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
                recorder.takePhoto(true);
            }

            @Override
            public void onRaceDebug(boolean debug) {
//                if (raceManager != null && mRenderingMode == RenderingMode.Race) {
//                    raceManager.setmRaceDebug(debug);
//                }
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
                        if (effectMusic != null) {
                            mConflictEffects.remove(TYPE_MUSIC);
                        }
                        effectMusic = new EffectBean();
                        effectMusic.setPath(musicFileBean.getPath());
                        effectMusic.setStartTime(startTime);
                        effectMusic.setDuration(getMaxRecordTime());

                        mConflictEffects.put(TYPE_MUSIC, effectMusic);
                        if (mOutMusicSelectListener != null) {
                            mOutMusicSelectListener.onMusicSelect(musicFileBean, startTime);
                        }
                        //如果音乐地址或者图片地址为空则使用默认图标
                        if (TextUtils.isEmpty(musicFileBean.getImage()) || TextUtils.isEmpty(musicFileBean.getPath())) {
                            mControlView.setMusicIconId(R.mipmap.aliyun_svideo_music);
                        } else {
                            mControlView.setMusicIcon(musicFileBean.getImage());
                        }
                    } else {
                        mControlView.setMusicIconId(R.mipmap.aliyun_svideo_music);
                    }
                }
            });

            musicChooseView.setDismissListener(new DialogVisibleListener() {
                @Override
                public void onDialogDismiss() {
                    mControlView.setMusicSelViewShow(false);
                    isMusicViewShowing = false;
                    restoreConflictEffect();
                }

                @Override
                public void onDialogShow() {
                    mControlView.setMusicSelViewShow(true);
                    recorder.applyMv(new EffectBean());
                    isMusicViewShowing = true;
                }
            });
        }
        musicChooseView.show(getFragmentManager(), TAG_MUSIC_CHOOSER);
    }

    /**
     * 当前所选中的动图路径
     */
    private String currPasterPath;

    /**
     * 显示动图效果调节控件
     */
    private void showGifEffectView() {
        if (gifEffectChooser == null) {
            gifEffectChooser = new GIfEffectChooser();
            gifEffectChooser.setDismissListener(this);
            gifEffectChooser.setPasterSelectListener(new PasterSelectListener() {
                @Override
                public void onPasterSelected(PreviewPasterForm imvForm) {
                    String path;
                    if (imvForm.getId() == 150) {
                        //id=150的动图为自带动图
                        path = imvForm.getPath();
                    } else {
                        path = DownloadFileUtils.getAssetPackageDir(getContext(),
                                imvForm.getName(), imvForm.getId()).getAbsolutePath();
                    }
                    currPasterPath = path;
                    addEffectToRecord(path);
                }

                @Override
                public void onSelectPasterDownloadFinish(String path) {
                    // 所选的paster下载完成后, 记录该paster 的path
                    currPasterPath = path;
                }
            });

            gifEffectChooser.setDismissListener(new DialogVisibleListener() {
                @Override
                public void onDialogDismiss() {
                    mControlView.setEffectSelViewShow(false);
                }

                @Override
                public void onDialogShow() {
                    mControlView.setEffectSelViewShow(true);
                    if (!TextUtils.isEmpty(currPasterPath)) {
                        // dialog显示后,如果记录的paster不为空, 使用该paster
                        addEffectToRecord(currPasterPath);
                    }
                }
            });
        }
        gifEffectChooser.show(getFragmentManager(), TAG_GIF_CHOOSER);
    }

    private void addEffectToRecord(String path) {

        if (effectPaster != null) {
            recorder.removePaster(effectPaster);
        }

        effectPaster = new EffectPaster(path);
        recorder.addPaster(effectPaster);

    }

    /**
     * 设置race单独包的水印
     */
    private void setRaceEffectView() {
        String logo = getContext().getExternalFilesDir("") + "/AliyunDemo/tail/logo.png";
        if (effectImage == null) {
            effectImage = new EffectImage(logo);
            effectImage.x = 0.13f;
            effectImage.y = 0.1f;
        }
        switch (mRatioMode) {
        case AliyunSnapVideoParam.RATIO_MODE_3_4:
            effectImage.width = (float) 100.0 / ScreenUtils.getRealWidth(mActivity);
            effectImage.height = (float) 80.0 / ScreenUtils.getRealHeight(mActivity) / 3 * 4;
            break;
        case AliyunSnapVideoParam.RATIO_MODE_1_1:
            effectImage.width = (float) 100.0 / ScreenUtils.getRealWidth(mActivity);
            effectImage.height = (float) 80.0 / ScreenUtils.getRealHeight(mActivity) / 9 * 16;
            break;
        case AliyunSnapVideoParam.RATIO_MODE_9_16:
            effectImage.width = (float) 100.0 / ScreenUtils.getRealWidth(mActivity);
            effectImage.height = (float) 80.0 / ScreenUtils.getRealHeight(mActivity);
            break;
        default:
            effectImage.width = (float) 100.0 / ScreenUtils.getRealWidth(mActivity);
            effectImage.height = (float) 80.0 / ScreenUtils.getRealHeight(mActivity);
            break;
        }
        recorder.removeImage(effectImage);
        recorder.addImage(effectImage);
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
                    filterSourcePath = effectInfo.getPath();
                    if (index == 0) {
                        filterSourcePath = null;
                    }
                    EffectFilter filterEffect = new EffectFilter(filterSourcePath);
                    recorder.applyFilter(filterEffect);
                    mConflictEffects.put(TYPE_FILTER, filterEffect);
                }
                currentFilterPosition = index;
            }
        });
        filterEffectChooser.setFilterPosition(currentFilterPosition);
        filterEffectChooser.setDismissListener(new DialogVisibleListener() {
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

                String path = effectInfo.getPath();

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
        mAnimFilterEffectChooser.setDismissListener(new DialogVisibleListener() {
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
     * 显示美颜调节的控件
     */
    private void showBeautyFaceView() {
        if (beautyEffectChooser == null) {
            beautyEffectChooser = new BeautyEffectChooser();
        }
        currentBeautySkinPosition = SharedPreferenceUtils.getBeautySkinLevel(getContext());



        // 美颜item选中listener
        beautyEffectChooser.setOnBeautyFaceItemSeletedListener(new OnBeautyFaceItemSeletedListener() {
            @Override
            public void onNormalSelected(int postion, BeautyLevel beautyLevel) {
                defaultBeautyLevel = beautyLevel;
                currentBeautyFaceNormalPosition = postion;
                // 普通美颜
                recorder.setBeautyLevel(beautyLevel.getValue());

                if (beautyService != null) {
                    beautyService.saveSelectParam(getContext(), currentBeautyFaceNormalPosition, currentBeautyFacePosition, currentBeautySkinPosition, currentBeautyShapePosition);
                }
            }

            @Override
            public void onAdvancedSelected(int position, BeautyLevel beautyLevel) {
                currentBeautyFacePosition = position;
                // 高级美颜
                BeautyParams beautyParams;
//                if (mRenderingMode == RenderingMode.FaceUnity) {
//                    beautyParams = rememberParamList.get(position);
//                } else {
//                    beautyParams = rememberRaceParamList.get(position);
//                }
                //// 美白和红润faceUnity的值范围 0~1.0f
//                if (beautyService != null) {
//                    beautyService.setBeautyParam(beautyParams, BeautyService.BEAUTY_FACE);
//                    beautyService.saveSelectParam(getContext(), currentBeautyFaceNormalPosition, currentBeautyFacePosition, currentBeautySkinPosition, currentBeautyShapePosition);
//                }
            }
        });

        // 美肌item选中
        beautyEffectChooser.setOnBeautySkinSelectedListener(new OnBeautySkinItemSeletedListener() {
            @Override
            public void onItemSelected(int postion) {
                currentBeautySkinPosition = postion;
                BeautyParams beautyParams;
//                if (mRenderingMode == RenderingMode.FaceUnity) {
//                    beautyParams = rememberParamList.get(postion);
//                } else {
//                    beautyParams = rememberRaceParamList.get(postion);
//                }
//                if (beautyService != null) {
//                    beautyService.setBeautyParam(beautyParams, BeautyService.BEAUTY_SKIN);
//                    beautyService.saveSelectParam(getContext(), currentBeautyFaceNormalPosition, currentBeautyFacePosition, currentBeautySkinPosition, currentBeautyShapePosition);
//                }
            }
        });

        // 美型item选中
        beautyEffectChooser.setOnBeautyShapeItemSeletedListener(new OnBeautyShapeItemSeletedListener() {
            @Override
            public void onItemSelected(int postion) {
                currentBeautyShapePosition = postion;
                if (beautyService != null) {
                    beautyService.saveSelectParam(getContext(), currentBeautyFaceNormalPosition, currentBeautyFacePosition, currentBeautySkinPosition, currentBeautyShapePosition);
                }
//                if (raceManager != null && mRenderingMode == RenderingMode.Race) {
//                    beautyShapeParams = getBeautyShapeParams(currentBeautyShapePosition);
//                    if (beautyShapeParams == null) {
//                        beautyShapeParams = rememberShapeParamList.get(currentBeautyShapePosition);
//                    }
//                    raceManager.setShapeParam(beautyShapeParams);
//                    raceManager.setCurrentShapeType(currentBeautyShapePosition);

//                }
            }
        });
        // tab选中监听  美颜（高级）、美肌、美型
        beautyEffectChooser.setOnTableSeletedListener(new OnBeautyTableItemSeletedListener() {
            @Override
            public void onNormalSelected(BeautyMode beautyMode) {
//                if (raceManager != null && mRenderingMode == RenderingMode.Race) {
//                    raceManager.setCurrentBeautyMode(beautyMode);
//                }
            }
        });

        // 美颜微调dialog
        beautyEffectChooser.setOnBeautyFaceDetailClickListener(new OnBeautyDetailClickListener() {
            @Override
            public void onDetailClick() {
                beautyEffectChooser.dismiss();
                mControlView.setEffectSelViewShow(true);
                showBeautyFaceDetailDialog();

            }
        });

        // 美肌微调dialog
        beautyEffectChooser.setOnBeautySkinDetailClickListener(new OnBeautyDetailClickListener() {
            @Override
            public void onDetailClick() {
                beautyEffectChooser.dismiss();
                mControlView.setEffectSelViewShow(true);
                showBeautySkinDetailDialog();
            }
        });
        // 美型微调dialog
        beautyEffectChooser.setOnBeautyShapeDetailClickListener(new OnBeautyDetailClickListener() {
            @Override
            public void onDetailClick() {
                beautyEffectChooser.dismiss();
                mControlView.setEffectSelViewShow(true);
                showBeautyShapeDetailDialog();
            }
        });

        // 美颜普通和高级模式切换
        beautyEffectChooser.setOnBeautyModeChangeListener(new OnBeautyModeChangeListener() {
            @Override
            public void onModeChange(RadioGroup group, int checkedId) {

                BeautyParams beautyParams = null;
                if (checkedId == R.id.rb_level_advanced) {
                    currentBeautyFaceMode = BeautyMode.Advanced;
                    recorder.setBeautyStatus(false);
//                    if (mRenderingMode == RenderingMode.FaceUnity) {
//                        beautyParams = rememberParamList.get(currentBeautyFacePosition);
//                    } else {
//                        beautyParams = rememberRaceParamList.get(currentBeautyFacePosition);
//                    }
                    if (beautyService != null) {
                        beautyService.setBeautyParam(beautyParams, BeautyService.BEAUTY_FACE);
                    }
                } else if (checkedId == R.id.rb_level_normal) {
                    currentBeautyFaceMode = BeautyMode.Normal;
                    recorder.setBeautyStatus(true);
                    recorder.setBeautyLevel(defaultBeautyLevel.getValue());
                }

                if (beautyService != null) {
                    beautyService.saveBeautyMode(getContext(), currentBeautyFaceMode);
                }
            }
        });

        beautyEffectChooser.setDismissListener(new DialogVisibleListener() {
            @Override
            public void onDialogDismiss() {
                // 如果微调的页面不在显示状态,
                if (!isbeautyDetailShowing) {
                    mControlView.setEffectSelViewShow(false);
                } else {
                    mControlView.setEffectSelViewShow(true);
                }
                isbeautyDetailBack = false;

                if (beautyService != null) {
                    beautyService.saveBeautyMode(getContext(), currentBeautyFaceMode);
                    beautyService.saveSelectParam(getContext(), currentBeautyFaceNormalPosition, currentBeautyFacePosition, currentBeautySkinPosition, currentBeautyShapePosition);
                }
            }

            @Override
            public void onDialogShow() {
                mControlView.setEffectSelViewShow(true);
                beautyEffectChooser.setBeautyParams(AliyunSVideoRecordView.this.beautyParams);

            }
        });


        beautyEffectChooser.show(getFragmentManager(), TAG_BEAUTY_CHOOSER);
    }

    /**
     * 显示美颜微调dialog
     */
    private void showBeautyFaceDetailDialog() {
        beautyParams = getBeautyParams(currentBeautyFacePosition);
        if (beautyParams == null) {
//            if (mRenderingMode == RenderingMode.FaceUnity) {
//                beautyParams = rememberParamList.get(currentBeautyFacePosition);
//            } else {
//                beautyParams = rememberRaceParamList.get(currentBeautyFacePosition);
//            }
        }
        beautyFaceDetailChooser = new BeautyFaceDetailChooser();
        beautyFaceDetailChooser.setBeautyLevel(currentBeautyFacePosition);
        beautyFaceDetailChooser.setOnBeautyParamsChangeListener(
        new OnBeautyParamsChangeListener() {
            @Override
            public void onBeautyChange(BeautyParams param) {
                if (beautyParams != null && param != null) {
                    beautyParams.beautyWhite = param.beautyWhite;
                    beautyParams.beautyBuffing = param.beautyBuffing;
                    beautyParams.beautyRuddy = param.beautyRuddy;
//                    if (faceUnityManager != null && mRenderingMode == RenderingMode.FaceUnity) {
//                        // 美白
//                        faceUnityManager.setFaceBeautyWhite(param.beautyWhite / 100);
//                        // 红润
//                        faceUnityManager.setFaceBeautyRuddy(param.beautyRuddy / 100);
//                        // 磨皮
//                        //将【0-100】转化【0-6】，比率为0。06,
//                        faceUnityManager.setFaceBeautyBuffing((float) (param.beautyBuffing * 0.06));
//                    } else if (raceManager != null && mRenderingMode == RenderingMode.Race) {
//                        // 美白
//                        raceManager.setFaceBeautyWhite(param.beautyWhite / 100);
//                        // 红润
//                        raceManager.setFaceBeautySharpLevel(param.beautyRuddy / 100);
//                        // 磨皮
//                        raceManager.setFaceBeautyBuffing(param.beautyBuffing / 100);
//                    }
                }
            }
        });
        // 点击back按钮
        beautyFaceDetailChooser.setOnBackClickListener(new OnViewClickListener() {
            @Override
            public void onClick() {
                isbeautyDetailShowing = false;
                isbeautyDetailBack = true;
                beautyFaceDetailChooser.dismiss();
                beautyEffectChooser.show(getFragmentManager(), TAG_BEAUTY_CHOOSER);
            }
        });
        // 空白区域点击
        beautyFaceDetailChooser.setOnBlankClickListener(new BeautyDetailSettingView.OnBlanckViewClickListener() {
            @Override
            public void onBlankClick() {
                isbeautyDetailShowing = false;
            }
        });
        beautyFaceDetailChooser.setDismissListener(new DialogVisibleListener() {
            @Override
            public void onDialogDismiss() {
                // 如果是点击微调界面中的back按钮, controlview的底部view仍要保持隐藏状态
                if (isbeautyDetailBack) {
                    mControlView.setEffectSelViewShow(true);
                } else {
                    mControlView.setEffectSelViewShow(false);
                }
                saveBeautyParams(currentBeautyFacePosition, beautyParams);
                isbeautyDetailBack = false;
            }

            @Override
            public void onDialogShow() {
            }
        });
        beautyFaceDetailChooser.setBeautyParams(beautyParams);
        beautyEffectChooser.dismiss();
        isbeautyDetailShowing = true;
        beautyFaceDetailChooser.show(getFragmentManager(), TAG_BEAUTY_DETAIL_FACE_CHOOSER);
    }


    /**
     * 显示美肌微调dialog
     */
    private void showBeautySkinDetailDialog() {
        beautyParams = getBeautyParams(currentBeautySkinPosition);
        if (beautyParams == null) {
//            if (mRenderingMode == RenderingMode.FaceUnity) {
//                beautyParams = rememberParamList.get(currentBeautyFacePosition);
//            } else {
//                beautyParams = rememberRaceParamList.get(currentBeautyFacePosition);
//            }
        }

        beautySkinDetailChooser = new BeautySkinDetailChooser();
        beautySkinDetailChooser.setBeautyLevel(currentBeautySkinPosition);
        beautySkinDetailChooser.setOnBeautyParamsChangeListener(
        new OnBeautyParamsChangeListener() {
            @Override
            public void onBeautyChange(BeautyParams param) {
                if (beautyParams != null && param != null) {

                    beautyParams.beautyBigEye = param.beautyBigEye;
                    beautyParams.beautySlimFace = param.beautySlimFace;
//                    if (faceUnityManager != null && mRenderingMode == RenderingMode.FaceUnity) {
//                        //大眼
//                        faceUnityManager.setFaceBeautyBigEye(param.beautyBigEye / 100);
//                        //瘦脸
//                        faceUnityManager.setFaceBeautySlimFace(param.beautySlimFace / 100 * 1.5f);
//                    } else if (raceManager != null && mRenderingMode == RenderingMode.Race) {
//                        //大眼
//                        raceManager.setFaceBeautyBigEye(param.beautyBigEye / 50.F);
//                        //瘦脸
//                        raceManager.setFaceBeautySlimFace(param.beautySlimFace / 50.F);
//                    }
                    saveBeautyParams(currentBeautySkinPosition, beautyParams);

                }
            }
        });

        // 点击back按钮
        beautySkinDetailChooser.setOnBackClickListener(new OnViewClickListener() {
            @Override
            public void onClick() {
                isbeautyDetailShowing = false;
                beautySkinDetailChooser.dismiss();
                isbeautyDetailBack = true;
                beautyEffectChooser.show(getFragmentManager(), TAG_BEAUTY_CHOOSER);
            }
        });

        // 空白区域点击
        beautySkinDetailChooser.setOnBlankClickListener(new BeautyDetailSettingView.OnBlanckViewClickListener() {
            @Override
            public void onBlankClick() {
                isbeautyDetailShowing = false;
            }
        });

        beautySkinDetailChooser.setDismissListener(new DialogVisibleListener() {
            @Override
            public void onDialogDismiss() {
                // 如果是点击微调界面中的back按钮, controlview的底部view仍要保持隐藏状态
                if (isbeautyDetailBack) {
                    mControlView.setEffectSelViewShow(true);
                } else {
                    mControlView.setEffectSelViewShow(false);
                }
                saveBeautyParams(currentBeautySkinPosition, AliyunSVideoRecordView.this.beautyParams);
                isbeautyDetailBack = false;
            }

            @Override
            public void onDialogShow() {

            }
        });
        beautySkinDetailChooser.setBeautyParams(beautyParams);
        beautyEffectChooser.dismiss();
        isbeautyDetailShowing = true;
        beautySkinDetailChooser.show(getFragmentManager(), TAG_BEAUTY_DETAIL_SKIN_CHOOSER);
    }


    /**
     * 显示美型微调dialog
     */
    private void showBeautyShapeDetailDialog() {
        beautyShapeParams = getBeautyShapeParams(currentBeautyShapePosition);
        if (beautyShapeParams == null) {
            beautyShapeParams = rememberShapeParamList.get(currentBeautyShapePosition);
        }

        beautyShapeDetailChooser = new BeautyShapeDetailChooser();
        beautyShapeDetailChooser.setBeautyLevel(currentBeautyShapePosition);
        beautyShapeDetailChooser.setOnBeautyParamsChangeListener(new OnBeautyShapeParamsChangeListener() {
            @Override
            public void onBeautyChange(BeautyShapeParams param) {
                if (beautyShapeParams != null && param != null) {

                    beautyShapeParams.beautyCutFace = param.beautyCutFace;
                    beautyShapeParams.beautyThinFace = param.beautyThinFace;
                    beautyShapeParams.beautyLongFace = param.beautyLongFace;
                    beautyShapeParams.beautyLowerJaw = param.beautyLowerJaw;
                    beautyShapeParams.beautyBigEye = param.beautyBigEye;
                    beautyShapeParams.beautyThinNose = param.beautyThinNose;
                    beautyShapeParams.beautyMouthWidth = param.beautyMouthWidth;
                    beautyShapeParams.beautyThinMandible = param.beautyThinMandible;
                    beautyShapeParams.beautyCutCheek = param.beautyCutCheek;
//                    if (raceManager != null && mRenderingMode == RenderingMode.Race) {
//                        raceManager.setShapeParam(beautyShapeParams);
//                    }
                    saveBeautyShapeParams(currentBeautyShapePosition, beautyShapeParams);

                }
            }
        });

        // 点击back按钮
        beautyShapeDetailChooser.setOnBackClickListener(new OnViewClickListener() {
            @Override
            public void onClick() {
                isbeautyDetailShowing = false;
                beautyShapeDetailChooser.dismiss();
                isbeautyDetailBack = true;
                beautyEffectChooser.show(getFragmentManager(), TAG_BEAUTY_CHOOSER);
            }
        });

        // 空白区域点击
        beautyShapeDetailChooser.setOnBlankClickListener(new BeautyDetailSettingView.OnBlanckViewClickListener() {
            @Override
            public void onBlankClick() {
                isbeautyDetailShowing = false;
            }
        });

        beautyShapeDetailChooser.setDismissListener(new DialogVisibleListener() {
            @Override
            public void onDialogDismiss() {
                // 如果是点击微调界面中的back按钮, controlview的底部view仍要保持隐藏状态
                if (isbeautyDetailBack) {
                    mControlView.setEffectSelViewShow(true);
                } else {
                    mControlView.setEffectSelViewShow(false);
                }
                saveBeautyShapeParams(currentBeautyShapePosition, AliyunSVideoRecordView.this.beautyShapeParams);
                isbeautyDetailBack = false;
            }

            @Override
            public void onDialogShow() {

            }
        });
        beautyShapeDetailChooser.setBeautyShapeParams(beautyShapeParams);
        beautyEffectChooser.dismiss();
        isbeautyDetailShowing = true;
        beautyShapeDetailChooser.show(getFragmentManager(), TAG_BEAUTY_DETAIL_SHAPE_CHOOSER);
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
        recorder.setVideoQuality(mVideoQuality);
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
        recorder.setBeautyStatus(false);
        recorder.applyFilter(new EffectFilter(null));
        initOrientationDetector();


        //获取需要的surFaceView数量
        initVideoContainer();

        recorder.setOnFrameCallback(new OnFrameCallBack() {
            @Override
            public void onFrameBack(byte[] bytes, int width, int height, Camera.CameraInfo info) {
                //原始数据回调 NV21,这里获取原始数据主要是为了faceUnity高级美颜使用
                frameBytes = bytes;
                frameWidth = width;
                frameHeight = height;
            }

            @Override
            public Camera.Size onChoosePreviewSize(List<Camera.Size> supportedPreviewSizes,
                                                   Camera.Size preferredPreviewSizeForVideo) {
                return null;
            }

            @Override
            public void openFailed() {
                Log.e(AliyunTag.TAG, "openFailed----------");
                isOpenFailed = true;
            }
        });

        recorder.setRecordCallback(new RecordCallback() {
            @Override
            public void onComplete(final boolean validClip, final long clipDuration) {
                Log.i(TAG, "onComplete   duration : " + clipDuration +
                      ", clipManager.getDuration() = " + clipManager.getDuration());
                tempIsComplete = true;
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "onComplete    isStopToCompleteDuration:" + isStopToCompleteDuration);

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
                        if (effectPaster != null) {
                            addEffectToRecord(effectPaster.getPath());
                        }
                    }
                });
            }

            @Override
            public void onDrawReady() {

            }

            @Override
            public void onPictureBack(final Bitmap bitmap) {

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

            @Override
            public void onPictureDataBack(final byte[] data) {

            }

        });
        recorder.setOnTextureIdCallback(new OnTextureIdCallBack() {
            private boolean mIsFirstFrame = true;

            @Override
            public int onTextureIdBack(int textureId, int textureWidth, int textureHeight, float[] matrix) {
                if (!mIsFuBeautyOpen) {
                    return textureId;
                }
                if (mIsFirstFrame) {
                    mIsFirstFrame = false;
                    Log.d(TAG, "onTextureDestroyed thread:" + Thread.currentThread().getName()
                            + ", texId:" + textureId + ", width:" + textureWidth + ", height:" + textureHeight + " gl context " + EGL14.eglGetCurrentContext());
                    mFURenderer.prepareRenderer(mFURendererListener);
                    initCsvUtil(getContext());
                }
                int texId = 0;
                if (currentBeautyFaceMode == BeautyMode.Advanced) {
                    long start = System.nanoTime();
//                    texId = mFURenderer.onDrawFrameDualInput(frameBytes, textureId, textureWidth, textureHeight);
                    texId = mFURenderer.onDrawFrameDualInput(null, textureId, textureWidth, textureHeight);
                    long time = System.nanoTime() - start;
                    if (mCSVUtils != null) {
                        mCSVUtils.writeCsv(null, time);
                    }
                    if (mSkippedFrames > 0) {
                        mSkippedFrames--;
                        texId = textureId;
                    }
                }
                if (texId <= 0) {
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
    }

    /**
     * 美颜默认选中高级, 3档 美白: 0.6 红润: 0.6 磨皮: 6 大眼: 0.6 瘦脸: 0.6 * 1.5 (总范围0~1.5)
     * 使用默认参数前判断是那种美颜
     */
    private void faceunityDefaultParam() {
        beautyService = new BeautyService();
        if (BeautyMode.Advanced == currentBeautyFaceMode) {
            recorder.setBeautyStatus(false);
//            beautyService.bindFaceUnity(getContext(), faceUnityManager);
            initRememberParams();
        } else if (BeautyMode.Normal == currentBeautyFaceMode) {
            //普通美颜等级
            int beautyNormalFaceLevel = SharedPreferenceUtils.getBeautyNormalFaceLevel(getContext());
            recorder.setBeautyStatus(true);
//            beautyService.bindNormalFaceUnity(faceUnityManager);
            recorder.setBeautyLevel(getNormalBeautyLevel(beautyNormalFaceLevel));
        } else {
//            beautyService.bindFaceUnity(getContext(), faceUnityManager);
            initRememberParams();
        }

    }
    /**
     * race
     * 美颜默认选中高级, 3档 美白: 0.6 红润: 0.6 磨皮: 6 大眼: 0.6 瘦脸: 0.6 * 1.5 (总范围0~1.5)
     * 使用默认参数前判断是那种美颜
     */
    private void raceDefaultParam() {
        beautyService = new BeautyService();
        if (BeautyMode.Advanced == currentBeautyFaceMode) {
            recorder.setBeautyStatus(false);
            beautyService.bindRace(getContext(), raceManager);
            initRememberParams();
        } else if (BeautyMode.Normal == currentBeautyFaceMode) {
            //普通美颜等级
            int beautyNormalFaceLevel = SharedPreferenceUtils.getBeautyNormalFaceLevel(getContext());
            recorder.setBeautyStatus(true);
            beautyService.bindNormalRace(raceManager);
            recorder.setBeautyLevel(getNormalBeautyLevel(beautyNormalFaceLevel));
        } else {
            beautyService.bindRace(getContext(), raceManager);
            initRememberParams();
        }
    }

    private void initRememberParams() {
        //高级美颜等级
        int beautyFaceLevel = SharedPreferenceUtils.getBeautyFaceLevel(mActivity);
        //美肌等级
        int beautySkinLevel = SharedPreferenceUtils.getBeautySkinLevel(mActivity);
        BeautyParams beautyFaceParams;
//        if (mRenderingMode == RenderingMode.FaceUnity) {
//            beautyFaceParams = rememberParamList.get(beautyFaceLevel);
//        } else {
//            beautyFaceParams = rememberRaceParamList.get(beautyFaceLevel);
//        }
//        beautyService.setBeautyParam(beautyFaceParams, BeautyService.BEAUTY_FACE);
        BeautyParams beautyShinParams = rememberParamList.get(beautySkinLevel);
        beautyService.setBeautyParam(beautyShinParams, BeautyService.BEAUTY_SKIN);
        if (raceManager != null && !rememberShapeParamList.isEmpty()) {
            raceManager.setShapeParam(rememberShapeParamList.get(currentBeautyShapePosition));
        }
    }

    /**
     * 获取当前普通美颜的级别对应的数值【0-5】-【0-100】
     */
    private int getNormalBeautyLevel(int beautyNormalFaceLevel) {

        switch (beautyNormalFaceLevel) {
        case 0:
            return BeautyLevel.BEAUTY_LEVEL_ZERO.getValue();
        case 1:
            return BeautyLevel.BEAUTY_LEVEL_ONE.getValue();
        case 2:
            return BeautyLevel.BEAUTY_LEVEL_TWO.getValue();
        case 3:
            return BeautyLevel.BEAUTY_LEVEL_THREE.getValue();
        case 4:
            return BeautyLevel.BEAUTY_LEVEL_FOUR.getValue();
        case 5:
            return BeautyLevel.BEAUTY_LEVEL_FIVE.getValue();
        default:
            return BeautyLevel.BEAUTY_LEVEL_THREE.getValue();

        }

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
                rotation = getCameraRotation();
                if (mFURenderer != null) {
                    mFURenderer.setDeviceOrientation(rotation);
                }
                recorder.setRotation(rotation);
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

        if (beautyEffectChooser != null) {
            beautyEffectChooser.dismiss();
        }

        if (mFaceUnityTask != null) {
            mFaceUnityTask.cancel(true);
            mFaceUnityTask = null;
        }
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
        deleteSliceFile();
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
     * 设置视频质量
     *
     * @param mVideoQuality
     */
    public void setVideoQuality(VideoQuality mVideoQuality) {
        this.mVideoQuality = mVideoQuality;
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
     * 设置视频编码方式
     *
     * @param mVideoCodec
     */
    public void setVideoCodec(VideoCodecs mVideoCodec) {
        this.mVideoCodec = mVideoCodec;
    }

    /**
     * 设置渲染方式
     * @param mRenderingMode
     */
    public void setRenderingMode(RenderingMode mRenderingMode) {
//        this.mRenderingMode = mRenderingMode;
    }

    /**
     * 设置是否是race录制包
     * @param isSvideoRace 是否是race录制包
     */
    public void setSvideoRace(boolean isSvideoRace) {
        this.isSvideoRace = isSvideoRace;
    }

    public void setMixBorderParam(AlivcMixBorderParam param){
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


    private void saveBeautyParams(int position, BeautyParams beautyParams) {
        if (beautyParams != null) {
//            if (mRenderingMode == RenderingMode.FaceUnity) {
//                Gson gson = new Gson();
//                rememberParamList.set(position, beautyParams);
//                rememberBeautyBean.setBeautyList(rememberParamList);
//                String jsonString = gson.toJson(rememberBeautyBean);
//                if (!TextUtils.isEmpty(jsonString)) {
//                    SharedPreferenceUtils.setBeautyParams(getContext(), jsonString);
//                }
//            } else {
//                Gson gson = new Gson();
//                rememberRaceParamList.set(position, beautyParams);
//                rememberRaceBeautyBean.setBeautyList(rememberRaceParamList);
//                String jsonString = gson.toJson(rememberRaceBeautyBean);
//                if (!TextUtils.isEmpty(jsonString)) {
//                    SharedPreferenceUtils.setRaceBeautyParams(getContext(), jsonString);
//                }
//            }

        }
    }
    private void saveBeautyShapeParams(int position, BeautyShapeParams beautyParams) {
        if (beautyParams != null) {
            Gson gson = new Gson();

            rememberShapeParamList.set(position, beautyParams);
            rememberBeautyShapeBean.setBeautyList(rememberShapeParamList);
            String jsonString = gson.toJson(rememberBeautyShapeBean);

            if (!TextUtils.isEmpty(jsonString)) {
                SharedPreferenceUtils.setBeautyShapeParams(getContext(), jsonString);
            }
        }
    }

    /**
     * 获取美颜美肌参数
     */
    private BeautyParams getBeautyParams(int position) {
        String jsonString;
//        if (mRenderingMode == RenderingMode.FaceUnity) {
//            jsonString = SharedPreferenceUtils.getBeautyParams(getContext());
//        } else {
//            jsonString = SharedPreferenceUtils.getRaceBeautyParams(getContext());
//        }
//        if (TextUtils.isEmpty(jsonString)) {
//            return null;
//        }
//        Gson gson = new Gson();
//        RememberBeautyBean rememberBeautyBean = gson.fromJson(jsonString, RememberBeautyBean.class);
        List<BeautyParams> beautyList = rememberBeautyBean.getBeautyList();
        if (beautyList == null) {
            return null;
        }
        return beautyList.get(position);

    }
    /**
     * 获取美型参数
     */
    private BeautyShapeParams getBeautyShapeParams(int position) {
        String jsonString = SharedPreferenceUtils.getBeautyShapeParams(getContext());
        if (TextUtils.isEmpty(jsonString)) {
            return null;
        }
        Gson gson = new Gson();
        RememberBeautyShapeBean rememberBeautyBean = gson.fromJson(jsonString, RememberBeautyShapeBean.class);
        List<BeautyShapeParams> beautyList = rememberBeautyBean.getBeautyList();
        if (beautyList == null) {
            return null;
        }
        if (position >= beautyList.size()) {
            return null;
        }
        return beautyList.get(position);
    }


    public void setOnMusicSelectListener(MusicSelectListener musicSelectListener) {
        this.mOutMusicSelectListener = musicSelectListener;
    }

    /**
     * 获取滤镜名称 适配系统语言/中文或其他
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
                var2.append((char)var7);
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