package com.aliyun.svideo.editor.view;

import static android.view.KeyEvent.KEYCODE_VOLUME_DOWN;
import static android.view.KeyEvent.KEYCODE_VOLUME_UP;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.common.utils.BitmapUtil;
import com.aliyun.common.utils.StringUtils;
import com.aliyun.svideo.base.Constants;
import com.aliyun.svideo.base.UIConfigManager;
import com.aliyun.svideo.common.utils.DensityUtils;
import com.aliyun.svideo.common.utils.FastClickUtil;
import com.aliyun.svideo.common.utils.FileUtils;
import com.aliyun.svideo.common.utils.PermissionUtils;
import com.aliyun.svideo.common.widget.AlivcCircleLoadingDialog;
import com.aliyun.svideo.editor.EditorMediaActivity;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.bean.AlivcEditInputParam;
import com.aliyun.svideo.editor.bean.AlivcEditOutputParam;
import com.aliyun.svideo.editor.bean.AlivcTransBean;
import com.aliyun.svideo.editor.bean.PasterRestoreBean;
import com.aliyun.svideo.editor.contant.CaptionConfig;
import com.aliyun.svideo.editor.contant.EditorConstants;
import com.aliyun.svideo.editor.editor.AbstractPasterUISimpleImpl;
import com.aliyun.svideo.editor.editor.AliyunBasePasterController;
import com.aliyun.svideo.editor.editor.EditorActivity;
import com.aliyun.svideo.editor.editor.PasterUICaptionImpl;
import com.aliyun.svideo.editor.editor.PasterUICompoundCaptionImpl;
import com.aliyun.svideo.editor.editor.PasterUIGifImpl;
import com.aliyun.svideo.editor.editor.PasterUITextImpl;
import com.aliyun.svideo.editor.editor.thumblinebar.OverlayThumbLineBar;
import com.aliyun.svideo.editor.editor.thumblinebar.ThumbLineBar;
import com.aliyun.svideo.editor.editor.thumblinebar.ThumbLineConfig;
import com.aliyun.svideo.editor.editor.thumblinebar.ThumbLineOverlay;
import com.aliyun.svideo.editor.effects.caption.component.CaptionChooserPanelView;
import com.aliyun.svideo.editor.effects.caption.listener.OnVideoUpdateDurationListener;
import com.aliyun.svideo.editor.effects.caption.manager.AlivcEditorViewFactory;
import com.aliyun.svideo.editor.effects.caption.manager.CaptionManager;
import com.aliyun.svideo.editor.effects.control.BaseChooser;
import com.aliyun.svideo.editor.effects.control.EditorService;
import com.aliyun.svideo.editor.effects.control.EffectInfo;
import com.aliyun.svideo.editor.effects.control.OnEffectActionLister;
import com.aliyun.svideo.editor.effects.control.OnEffectChangeListener;
import com.aliyun.svideo.editor.effects.control.OnTabChangeListener;
import com.aliyun.svideo.editor.effects.control.TabGroup;
import com.aliyun.svideo.editor.effects.control.TabViewStackBinding;
import com.aliyun.svideo.editor.effects.control.UIEditorPage;
import com.aliyun.svideo.editor.effects.control.ViewStack;
import com.aliyun.svideo.editor.effects.filter.AnimationFilterController;
import com.aliyun.svideo.editor.effects.pip.msg.PipAddMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipAlphaMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipAngleMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipBorderMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipBrighnessMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipContrastMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipDeleteMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipDenoiseMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipEffectMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipFrameAnimationMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipMoveMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipRadiusMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipSaturationMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipScaleMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipSharpnessMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipVignetteMsg;
import com.aliyun.svideo.editor.effects.pip.msg.PipVolumeMsg;
import com.aliyun.svideo.editor.effects.sound.SoundEffectInfo;
import com.aliyun.svideo.editor.effects.transition.TransitionChooserView;
import com.aliyun.svideo.editor.effects.transition.TransitionEffectCache;
import com.aliyun.svideo.editor.msg.Dispatcher;
import com.aliyun.svideo.editor.msg.body.BrightnessProgressMsg;
import com.aliyun.svideo.editor.msg.body.CheckDeleteFilter;
import com.aliyun.svideo.editor.msg.body.ContrastProgressMsg;
import com.aliyun.svideo.editor.msg.body.FilterTabClick;
import com.aliyun.svideo.editor.msg.body.LongClickAnimationFilter;
import com.aliyun.svideo.editor.msg.body.LongClickUpAnimationFilter;
import com.aliyun.svideo.editor.msg.body.SaturationProgressMsg;
import com.aliyun.svideo.editor.msg.body.SelectColorFilter;
import com.aliyun.svideo.editor.msg.body.SharpProgressMsg;
import com.aliyun.svideo.editor.msg.body.VideoEqResetAllMsg;
import com.aliyun.svideo.editor.msg.body.VideoEqResetMsg;
import com.aliyun.svideo.editor.msg.body.VignetteMsg;
import com.aliyun.svideo.editor.publish.PublishActivity;
import com.aliyun.svideo.editor.util.AlivcResUtil;
import com.aliyun.svideo.editor.util.AlivcSnapshot;
import com.aliyun.svideo.editor.util.EditorCommon;
import com.aliyun.svideo.editor.util.FixedToastUtils;
import com.aliyun.svideo.editor.util.ThreadUtil;
import com.aliyun.svideo.editor.viewoperate.ViewOperator;
import com.aliyun.svideo.editor.widget.AliyunPasterWithImageView;
import com.aliyun.svideo.editor.widget.AliyunPasterWithTextView;
import com.aliyun.svideo.media.MediaInfo;
import com.aliyun.svideosdk.common.AliyunErrorCode;
import com.aliyun.svideosdk.common.AliyunIThumbnailFetcher;
import com.aliyun.svideosdk.common.impl.AliyunThumbnailFetcherFactory;
import com.aliyun.svideosdk.common.struct.effect.TrackEffectMV;
import com.aliyun.svideosdk.common.struct.project.AliyunEditorProject;
import com.aliyun.svideosdk.common.struct.effect.TrackAudioStream;
import com.aliyun.svideosdk.common.struct.effect.TrackEffectFilter;
import com.aliyun.svideosdk.common.struct.project.AudioEffect;
import com.aliyun.svideosdk.common.struct.project.Effect;
import com.aliyun.svideosdk.common.struct.project.Filter;
import com.aliyun.svideosdk.common.struct.project.Source;
import com.aliyun.svideosdk.common.struct.project.TimeFilter;
import com.aliyun.svideosdk.common.struct.project.VideoTrack;
import com.aliyun.svideosdk.common.struct.project.VideoTrackClip;
import com.aliyun.svideosdk.common.internal.videoaugment.VideoAugmentationType;
import com.aliyun.svideosdk.common.struct.common.AliyunClip;
import com.aliyun.svideosdk.common.struct.common.AliyunVideoParam;
import com.aliyun.svideosdk.common.struct.common.VideoDisplayMode;
import com.aliyun.svideosdk.common.struct.common.VideoQuality;
import com.aliyun.svideosdk.common.struct.effect.ActionBase;
import com.aliyun.svideosdk.common.struct.effect.ActionTranslate;
import com.aliyun.svideosdk.common.struct.effect.EffectBase;
import com.aliyun.svideosdk.common.struct.effect.EffectBean;
import com.aliyun.svideosdk.common.struct.effect.EffectCaption;
import com.aliyun.svideosdk.common.struct.effect.EffectConfig;
import com.aliyun.svideosdk.common.struct.effect.EffectPaster;
import com.aliyun.svideosdk.common.struct.effect.EffectText;
import com.aliyun.svideosdk.common.struct.effect.LUTEffectBean;
import com.aliyun.svideosdk.common.struct.effect.TransitionBase;
import com.aliyun.svideosdk.common.struct.effect.TransitionCircle;
import com.aliyun.svideosdk.common.struct.effect.TransitionFade;
import com.aliyun.svideosdk.common.struct.effect.TransitionFiveStar;
import com.aliyun.svideosdk.common.struct.effect.TransitionShutter;
import com.aliyun.svideosdk.common.struct.effect.TransitionTranslate;
import com.aliyun.svideosdk.common.struct.effect.ValueTypeEnum;
import com.aliyun.svideosdk.common.struct.encoder.VideoCodecs;
import com.aliyun.svideosdk.common.struct.project.AliyunEditorProject;
import com.aliyun.svideosdk.common.struct.project.AudioEffect;
import com.aliyun.svideosdk.common.struct.project.Effect;
import com.aliyun.svideosdk.common.struct.project.Filter;
import com.aliyun.svideosdk.common.struct.project.Source;
import com.aliyun.svideosdk.common.struct.project.TimeFilter;
import com.aliyun.svideosdk.common.struct.project.VideoTrack;
import com.aliyun.svideosdk.common.struct.project.VideoTrackClip;
import com.aliyun.svideosdk.crop.AliyunICrop;
import com.aliyun.svideosdk.crop.CropCallback;
import com.aliyun.svideosdk.crop.CropParam;
import com.aliyun.svideosdk.crop.impl.AliyunCropCreator;
import com.aliyun.svideosdk.editor.AliyunIAugmentationController;
import com.aliyun.svideosdk.editor.AliyunICanvasController;
import com.aliyun.svideosdk.editor.AliyunIEditor;
import com.aliyun.svideosdk.editor.AliyunIPasterController;
import com.aliyun.svideosdk.editor.AliyunIPipController;
import com.aliyun.svideosdk.editor.AliyunIPipManager;
import com.aliyun.svideosdk.editor.AliyunISourcePartManager;
import com.aliyun.svideosdk.editor.AliyunPasterController;
import com.aliyun.svideosdk.editor.AliyunPasterManager;
import com.aliyun.svideosdk.editor.AliyunRollCaptionComposer;
import com.aliyun.svideosdk.editor.AudioEffectType;
import com.aliyun.svideosdk.editor.EditorCallBack;
import com.aliyun.svideosdk.editor.EffectType;
import com.aliyun.svideosdk.editor.OnAnimationFilterRestoredListener;
import com.aliyun.svideosdk.editor.OnPasterRestored;
import com.aliyun.svideosdk.editor.TimeEffectType;
import com.aliyun.svideosdk.editor.impl.AliyunEditorFactory;
import com.aliyun.svideosdk.editor.impl.AliyunPasterAbstractController;
import com.aliyun.svideosdk.editor.impl.AliyunPasterControllerCompoundCaption;
import com.aliyun.svideosdk.transcode.NativeParser;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zsy_18 data:2018/8/24
 */
public class AlivcEditView extends RelativeLayout
    implements View.OnClickListener, OnEffectChangeListener, OnTabChangeListener, IAlivcEditView,
    OnAnimationFilterRestoredListener {
    private static final String TAG = AlivcEditView.class.getName();
    public static final int REQ_CODE_GET_MEDIA = 101;
    /**
     * 编辑核心接口类
     */
    private AliyunIEditor mAliyunIEditor;
    /**
     * 动图管理接口类
     */
    private AliyunPasterManager mPasterManager;
    /**
     * 涂鸦使用的Controller接口，可以对涂鸦做一系列的操作
     */
    public AliyunICanvasController mCanvasController;
    /**
     * 获取缩略图片接口
     */
    private AliyunIThumbnailFetcher mThumbnailFetcher;

    /**
     * 裁剪接口核心类，对于Gop比较大的视频做时间特效时需要先检查是否满足实时，如果不满足实时，需要提前做转码，逻辑如下
     */
    private AliyunICrop mTranscoder;

    /**
     * 动图使用的Controller接口,可以获取动图的系列属性并且可以操作动图
     */
    private AliyunPasterController mAliyunPasterController;

    private OverlayThumbLineBar mThumbLineBar;

    /**
     * 底部滑动item的横向ScrollView
     */
    private HorizontalScrollView mBottomLinear;
    /**
     * 编辑需要渲染显示的SurfaceView
     */
    private View mSurfaceView;
    /**
     * 底部菜单点击事件管理类
     */
    private TabGroup mTabGroup;
    /**
     * 处理底部菜单点击事件
     */
    private ViewStack mViewStack;
    /**
     * 主要用于记录各个功能view上次的状态，用于下次进入的时候进行恢复
     */
    private EditorService mEditorService;
    /**
     * 控件
     */
    private RelativeLayout mActionBar;
    private FrameLayout resCopy;
    private FrameLayout mTransCodeTip;
    private ProgressBar mTransCodeProgress;
    public FrameLayout mPasterContainer;
    private FrameLayout mGlSurfaceContainer;
    private ImageView mIvLeft;
    private TextView mTvRight;
    private LinearLayout mBarLinear;
    private TextView mPlayImage;
    private TextView mTvCurrTime;
    /**
     * 屏幕宽度
     */
    private int mScreenWidth;
    /**
     * 水印图片
     */
    private Bitmap mWatermarkBitmap;
    /**
     * 特效使用的控制类
     */
    private AnimationFilterController mAnimationFilterController;
    /**
     * 时间特效在缩略图上的浮层 用于删除时间浮层
     */
    private ThumbLineOverlay mTimeEffectOverlay;
    private ThumbLineOverlay.ThumbLineOverlayView mThumbLineOverlayView;
    /**
     * 状态，使用倒放时间特效
     */
    private boolean mUseInvert = false;
    /**
     * 状态，正在添加滤镜特效那个中
     */
    private boolean mUseAnimationFilter = false;
    /**
     * 状态，判断是否可以继续添加时间特效，true不可以继续添加特效
     */
    private boolean mCanAddAnimation = true;
    /**
     * 状态，是否正在转码中
     */
    private boolean mIsTranscoding = false;
    /**
     * 状态，界面是否被销毁
     */
    private boolean mIsDestroyed = false;
    /**
     * 状态，与生命周期onStop有关
     */
    private boolean mIsStop = false;
    private boolean mWaitForReady = false;

    private AliyunBasePasterController mCurrentEditEffect;
    /**
     * 音量
     */
    private int mVolume = 50;
    /**
     * 控制UI变动
     */
    private ViewOperator mViewOperate;
    private Point mPasterContainerPoint;
    private TrackAudioStream lastMusicBean;
    //用户滑动thumbLineBar时的监听器
    private ThumbLineBar.OnBarSeekListener mBarSeekListener;
    //播放时间、显示时间、缩略图位置同步接口
    private PlayerListener mPlayerListener;
    private EffectInfo mLastMVEffect;
    private EffectInfo mLastSoundEffect;
    private ObjectAnimator animatorX;
    private Toast showToast;

    /**
     * 编辑模块Handler处理类
     */
    private AlivcEditHandler alivcEditHandler;
    /**
     * 线程池
     */
    private ExecutorService executorService;
    /**
     * 封面保存路径
     */
    private final String PATH_THUMBNAIL = Constants.SDCardConstants.getDir(getContext()) + "thumbnail.jpg";
    /**
     * 是否可以截图
     */
    private boolean isTakeFrame = false;
    /**
     * 是否确认选择截图
     */
    private boolean isTakeFrameSelected = false;
    /**
     * 是否已经截取封面
     */
    private boolean hasCaptureCover = false;
    /**
     * 截图工具，用于获取surface的画面
     */
    private AlivcSnapshot mSnapshop;
    /**
     * 是否使用默认水印
     */
    private boolean hasWaterMark;
    /**
     * 是否水平镜像翻转
     * */
    private boolean mHorizontalFlip = false;
    /**
     * 判断是否有音乐
     */
    private boolean mHasRecordMusic;
    /**
     * 是否替换原视频中音乐
     */
    private boolean isReplaceMusic;
    /**
     * 是否合拍，合拍无法使用音乐
     */
    private boolean isMixRecord;

    private AlivcCircleLoadingDialog mLoadingDialog;

    /**
     * 记录可调节的转场特效
     */
    private LinkedHashMap<Integer, EffectInfo> mTransitionCache;
    /**
     * 记录可调节的转场特效的初始值
     */
    private LinkedHashMap<Integer, List<AlivcTransBean>> mTransitionParamsCache;

    /**
     * 是否开启降噪，默认无
     */
    private boolean mHasDeNoise = false;
    /**
     * 翻转字幕
     */
    private AliyunRollCaptionComposer mAliyunRollCaptionComposer;
    private PasterUICompoundCaptionImpl mPasterUICompoundCaptionImpl;
    private PointF tempPointF = new PointF();
    /**
     * 播放器进度监听，用于字幕预览
     */
    private Set<OnVideoUpdateDurationListener> playerListenerSet = new HashSet<>();

    public AlivcEditView(Context context) {
        this(context, null);
    }

    public AlivcEditView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlivcEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        Dispatcher.getInstance().register(this);

        Point point = new Point();
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getSize(point);
        mScreenWidth = point.x;
        LayoutInflater.from(getContext()).inflate(R.layout.alivc_editor_view_edit, this, true);
        initView();
        initListView();
        add2Control();
        initThreadHandler();
        if (PermissionUtils.checkPermissionsGroup(getContext(), PermissionUtils.PERMISSION_STORAGE)) {
            copyAssets();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        resCopy = (FrameLayout) findViewById(R.id.copy_res_tip);
        mTransCodeTip = (FrameLayout) findViewById(R.id.transcode_tip);
        mTransCodeProgress = (ProgressBar) findViewById(R.id.transcode_progress);
        mBarLinear = (LinearLayout) findViewById(R.id.bar_linear);
        mBarLinear.bringToFront();
        mActionBar = (RelativeLayout) findViewById(R.id.action_bar);
        mActionBar.setBackgroundDrawable(null);
        mIvLeft = (ImageView) findViewById(R.id.iv_left);
        mTvRight = findViewById(R.id.tv_right);
        mIvLeft.setImageResource(R.mipmap.aliyun_svideo_back);
        //uiConfig中的属性
        //UIConfigManager.setImageResourceConfig(mTvRight, R.attr.finishImage, R.mipmap.aliyun_svideo_complete_red);
        mIvLeft.setVisibility(View.VISIBLE);

        mIvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Activity) getContext()).finish();
            }
        });
        mTvCurrTime = (TextView) findViewById(R.id.tv_curr_duration);

        mGlSurfaceContainer = (FrameLayout) findViewById(R.id.glsurface_view);
        mSurfaceView = findViewById(R.id.play_view);
        mBottomLinear = findViewById(R.id.edit_bottom_tab);
        setBottomTabResource();
        mPasterContainer = (FrameLayout) findViewById(R.id.pasterView);

        mPlayImage = findViewById(R.id.play_button);
        mPlayImage.setOnClickListener(this);
        switchPlayStateUI(false);

        final GestureDetector mGesture = new GestureDetector(getContext(), new MyOnGestureListener());
        View.OnTouchListener pasterTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGesture.onTouchEvent(event);
            }
        };

        mPasterContainer.setOnTouchListener(pasterTouchListener);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideBottomEditorView();
            }
        });

    }

    /**
     * 设置底部效果按钮图标资源
     */
    private void setBottomTabResource() {
        TextView[] textViews = {

            findViewById(R.id.tab_filter),
            findViewById(R.id.tab_effect_audio_mix),
            findViewById(R.id.tab_effect_overlay),
            findViewById(R.id.tab_effect_caption),
            findViewById(R.id.tab_effect_caption_old),
            findViewById(R.id.tab_effect_mv),
            findViewById(R.id.tab_effect_sound),
            findViewById(R.id.tab_effect_filter),
            findViewById(R.id.tab_effect_time),
            findViewById(R.id.tab_effect_transition),
            findViewById(R.id.tab_paint),
            findViewById(R.id.tab_cover),
            findViewById(R.id.tab_videoeq),
            findViewById(R.id.tab_roll_caption),
            findViewById(R.id.tab_pip),
            findViewById(R.id.tab_lut),
        };
        int length = textViews.length;
        int[] index = new int[length];
        for (int i = 0; i < length; i++) {
            //所有的图片方向都是top
            index[i] = 1;
        }
        int[] attrs = {
            R.attr.filterImage,
            R.attr.musicImage,
            R.attr.pasterImage,
            R.attr.captionImage,
            R.attr.captionImage,
            R.attr.mvImage,
            R.attr.sound,//音效
            R.attr.effectImage,
            R.attr.timeImage,
            R.attr.translationImage,
            R.attr.paintImage,
            R.attr.coverImage,
            R.attr.videoEqImage,
            R.attr.rollCoverImage,
            R.attr.picInPic,
            R.attr.lutImage,
            R.attr.rollCoverImage,
            R.attr.captionImage,
        };
        int[] defaultResourceIds = {
            R.mipmap.aliyun_svideo_filter,
            R.mipmap.aliyun_svideo_music,
            R.mipmap.aliyun_svideo_overlay,
            R.mipmap.aliyun_svideo_caption,
            R.mipmap.aliyun_svideo_caption,
            R.mipmap.aliyun_svideo_mv,
            R.mipmap.aliyun_svideo_sound,//音效, 暂用mv icon
            R.mipmap.alivc_svideo_effect,
            R.mipmap.aliyun_svideo_time,
            R.mipmap.aliyun_svideo_transition,
            R.mipmap.aliyun_svideo_paint,
            R.mipmap.aliyun_svideo_cover,
            R.mipmap.aliyun_svideo_augmentation,
            R.mipmap.aliyun_svideo_caption,
            R.mipmap.aliyun_svideo_filter,
            R.mipmap.aliyun_svideo_caption,
            R.mipmap.aliyun_svideo_caption,
        };
        UIConfigManager.setImageResourceConfig(textViews, index, attrs, defaultResourceIds);
        //控制底部菜单显示内容
        int[] bottomItemMenuVisibleTags = getContext().getResources().getIntArray(R.array.bottomItemMenuVisibleTags);
        for (int i = 0; i < textViews.length; i++) {
            if (bottomItemMenuVisibleTags[i] == 0) {
                textViews[i].setVisibility(GONE);
            } else {
                textViews[i].setVisibility(VISIBLE);
            }

        }
    }

    public OverlayThumbLineBar getThumbLineBar() {
        return mThumbLineBar;
    }

    private void initGlSurfaceView() {
        if (mVideoParam == null) {
            return;
        }
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mGlSurfaceContainer.getLayoutParams();

        int outputWidth = mVideoParam.getOutputWidth();
        int outputHeight = mVideoParam.getOutputHeight();

        float percent;
        if (outputWidth >= outputHeight) {
            percent = (float) outputWidth / outputHeight;
        } else {
            percent = (float) outputHeight / outputWidth;
        }
        FrameLayout.LayoutParams surfaceLayout = (FrameLayout.LayoutParams) mSurfaceView.getLayoutParams();
        /*
          指定surfaceView的宽高比是有必要的，这样可以避免某些非标分辨率下造成显示比例不对的问题
         */
        surfaceLayout.width = mScreenWidth;
        surfaceLayout.height = Math.round((float) outputHeight * mScreenWidth / outputWidth);
        mPasterContainerPoint = new Point(surfaceLayout.width, surfaceLayout.height);
        ViewGroup.MarginLayoutParams marginParams = null;
        if (layoutParams instanceof MarginLayoutParams) {
            marginParams = (ViewGroup.MarginLayoutParams) surfaceLayout;
        } else {
            marginParams = new MarginLayoutParams(surfaceLayout);
        }
        if (percent < 1.5) {
            marginParams.setMargins(0,
                getContext().getResources().getDimensionPixelSize(R.dimen.alivc_svideo_title_height), 0, 0);
        } else {
            if (outputWidth > outputHeight) {
                marginParams.setMargins(0,
                    getContext().getResources().getDimensionPixelSize(R.dimen.alivc_svideo_title_height) * 2, 0, 0);
                //} else {
                //    int screenWidth = ScreenUtils.getRealWidth(getContext());
                //    int screenHeight = ScreenUtils.getRealHeight(getContext());
                //    float screenRatio = screenWidth / (float)screenHeight;
                //    if (screenRatio <= 9 / 16f) {
                //        //长手机，宽高比小于9/16
                //        marginParams.height = screenHeight;
                //        marginParams.width = screenHeight / 16 * 9;
                //    }
            }
        }
        mGlSurfaceContainer.setLayoutParams(layoutParams);
        mPasterContainer.setLayoutParams(marginParams);
        mSurfaceView.setLayoutParams(marginParams);
        //mCanvasController = mAliyunIEditor.obtainCanvasController(getContext(),
        //                    marginParams.width, marginParams.height);
    }

    public float dip2px(Context paramContext, float paramFloat) {
        return 0.5F + paramFloat * paramContext.getResources().getDisplayMetrics().density;
    }

    private void initListView() {
        mViewOperate = new ViewOperator(this, mActionBar, mSurfaceView, mBottomLinear, mPasterContainer, mPlayImage);
        mViewOperate.setAnimatorListener(new ViewOperator.AnimatorListener() {
            @Override
            public void onShowAnimationEnd() {
                UIEditorPage index = UIEditorPage.get(mTabGroup.getCheckedIndex());
                switch (index) {
                    case PAINT:
                        //2018/8/30 添加涂鸦画布
                        if (mCanvasController == null) {
                            int width = mPasterContainer.getLayoutParams().width;
                            int height = mPasterContainer.getLayoutParams().height;
                            mCanvasController = mAliyunIEditor.obtainCanvasController(getContext(),
                                width, height);
                            Paint paint = mAliyunIEditor.getPaintLastApply();
                            if (paint != null) {
                                mCanvasController.setCurrentColor(paint.getColor());
                                mCanvasController.setCurrentSize(paint.getStrokeWidth());
                            } else {
                                mCanvasController.setCurrentSize(dip2px(getContext(), 5));
                            }

                        }

                        mCanvasController.removeCanvas();
                        View canvasView = mCanvasController.getCanvas();
                        mPasterContainer.removeView(canvasView);
                        mPasterContainer.addView(canvasView, mPasterContainer.getWidth(), mPasterContainer.getHeight());
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onHideAnimationEnd() {
                if (isTakeFrameSelected) {
                    isTakeFrame = true;
                    //继续播放保证截图
                    playingResume();
                    //播放按钮变为可见状态
                    mPlayImage.setVisibility(VISIBLE);
                    isTakeFrameSelected = false;
                }
            }
        });
        mTvRight.setVisibility(View.VISIBLE);
        mEditorService = new EditorService();
        mTabGroup = new TabGroup();
        mViewStack = new ViewStack(getContext(), this, mViewOperate);
        mViewStack.setEditorService(mEditorService);
        mViewStack.setEffectChange(this);
        mViewStack.setOnEffectActionLister(mOnEffectActionLister);
        mViewStack.setOnTransitionPreviewListener(mOnTransitionPreviewListener);
        mTabGroup.addView(findViewById(R.id.tab_filter));
        mTabGroup.addView(findViewById(R.id.tab_effect_audio_mix));
        mTabGroup.addView(findViewById(R.id.tab_effect_overlay));
        mTabGroup.addView(findViewById(R.id.tab_effect_caption));
        mTabGroup.addView(findViewById(R.id.tab_effect_caption_old));
        mTabGroup.addView(findViewById(R.id.tab_effect_mv));
        mTabGroup.addView(findViewById(R.id.tab_effect_sound));
        mTabGroup.addView(findViewById(R.id.tab_effect_filter));
        mTabGroup.addView(findViewById(R.id.tab_effect_time));
        mTabGroup.addView(findViewById(R.id.tab_effect_transition));
        mTabGroup.addView(findViewById(R.id.tab_paint));
        mTabGroup.addView(findViewById(R.id.tab_cover));
        mTabGroup.addView(findViewById(R.id.tab_videoeq));
        mTabGroup.addView(findViewById(R.id.tab_roll_caption));
        mTabGroup.addView(findViewById(R.id.tab_pip));
        mTabGroup.addView(findViewById(R.id.tab_lut));

    }

    private void add2Control() {
        TabViewStackBinding tabViewStackBinding = new TabViewStackBinding();
        tabViewStackBinding.setViewStack(mViewStack);
        mTabGroup.setOnCheckedChangeListener(tabViewStackBinding);
        mTabGroup.setOnTabChangeListener(this);
    }

    private void initEditor() {
        //设置onTextureRender能够回调
        mEditorCallback.mNeedRenderCallback = EditorCallBack.RENDER_CALLBACK_TEXTURE;
        mAliyunIEditor = AliyunEditorFactory.creatAliyunEditor(mUri, mEditorCallback);
        mAliyunRollCaptionComposer = mAliyunIEditor.createRollCaptionComposer();
        if (mViewStack != null) {
            mViewStack.setAliyunRollCaptionComposer(mAliyunRollCaptionComposer);
        }
        initGlSurfaceView();
        final FrameLayout.LayoutParams surfaceLayout = (FrameLayout.LayoutParams) mSurfaceView.getLayoutParams();
        {
            //该代码块中的操作必须在AliyunIEditor.init之前调用，否则会出现动图、动效滤镜的UI恢复回调不执行，开发者将无法恢复动图、动效滤镜UI
            mPasterManager = mAliyunIEditor.getPasterManager();
            /*
              指定显示区域大小后必须调用mPasterManager.setDisplaySize，否则将无法添加和恢复一些需要提前获知区域大小的资源，如字幕，动图等
              如果开发者的布局使用了wrapContent或者matchParent之类的布局，务必获取到view的真实宽高之后在调用
             */
            try {
                mPasterManager.setDisplaySize(surfaceLayout.width, surfaceLayout.height);
            } catch (Exception e) {
                showToast = FixedToastUtils.show(getContext(), e.getMessage());
                ((Activity) getContext()).finish();
                return;
            }
            mPasterManager.setOnPasterRestoreListener(mOnPasterRestoreListener);
            mAnimationFilterController = new AnimationFilterController(getContext().getApplicationContext(),
                mAliyunIEditor);
            mAliyunIEditor.setOnAnimationRestoredListener(AlivcEditView.this);
        }

        mTranscoder = AliyunCropCreator.createCropInstance(getContext());
        VideoDisplayMode mode = mVideoParam.getScaleMode();
        int ret = mAliyunIEditor.init((SurfaceView) mSurfaceView, getContext().getApplicationContext());
        mAliyunIEditor.setDisplayMode(mode);
        mAliyunIEditor.setVolume(mVolume);
        mAliyunIEditor.setFillBackgroundColor(Color.BLACK);
        List<AliyunClip> clips = mAliyunIEditor.getSourcePartManager().getAllClips();
        mAliyunIEditor.denoise(clips.get(0).getId(), mHasDeNoise);
        if (ret != AliyunErrorCode.ALIVC_COMMON_RETURN_SUCCESS) {
            showToast = FixedToastUtils.show(getContext(),
                getResources().getString(R.string.alivc_editor_edit_tip_init_failed));
            ((Activity) getContext()).finish();
            return;
        }

        Filter colorFilter = mAliyunIEditor.getEditorProject().getColorEffect();
        if (colorFilter != null) {
            String path = colorFilter.getSource().getPath();
            List<String> list = EditorCommon.getColorFilterList(getContext());
            int index = list.indexOf(path);
            if (index > -1) {
                //还有一个原片，所以加一
                mEditorService.addTabEffect(UIEditorPage.FILTER, index + 1);
            }
        }
        //音效选中状态恢复
        VideoTrack videoTrack = mAliyunIEditor.getEditorProject().getTimeline().getPrimaryTrack();
        int effectType = -1;
        for (VideoTrackClip clip : videoTrack.getVideoTrackClips()) {
            if(mHorizontalFlip){
                mAliyunIEditor.setHorizontalFlip(clip.getClipId(), true);
            }else{
                mAliyunIEditor.setHorizontalFlip(clip.getClipId(), false);
            }
            for (Effect effect : clip.getEffects()) {
                if (effect instanceof AudioEffect) {
                    effectType = ((AudioEffect) effect).mEffectType.getEffectType();
                    break;
                }
            }
            if (effectType != -1) {
                break;
            }
        }

        if (effectType != -1) {
            mEditorService.addTabEffect(UIEditorPage.SOUND, effectType);
        }
        mEditorService.addTabEffect(UIEditorPage.MV, mAliyunIEditor.getMVLastApplyId());
        mEditorService.addTabEffect(UIEditorPage.FILTER_EFFECT, mAliyunIEditor.getFilterLastApplyId());
        mEditorService.addTabEffect(UIEditorPage.AUDIO_MIX, mAliyunIEditor.getMusicLastApplyId());
        mEditorService.setPaint(mAliyunIEditor.getPaintLastApply());

        mTvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (FastClickUtil.isFastClickActivity(EditorActivity.class.getSimpleName())) {
                    return;
                }
                mTvRight.setEnabled(false);
                //合成方式分为两种，当前页面合成（前台页面）和其他页面合成（后台合成，这里后台并不是真正的app退到后台）
                //前台合成如下：如果要直接合成（当前页面合成），请打开注释，参考注释代码这种方式
                //                int ret = mAliyunIEditor.compose(mVideoParam, "/sdcard/output_compose.mp4", new
                // AliyunIComposeCallBack() {
                //                    @Override
                //                    public void onComposeError(int errorCode) {
                //                        runOnUiThread(new Runnable() {
                //                            @Override
                //                            public void run() {
                //                                v.setEnabled(true);
                //                            }
                //                        });
                //
                //                        Log.d(AliyunTag.TAG, "Compose error, error code "+errorCode);
                //                    }
                //
                //                    @Override
                //                    public void onComposeProgress(int progress) {
                //                        Log.d(AliyunTag.TAG, "Compose progress "+progress+"%");
                //                    }
                //
                //                    @Override
                //                    public void onComposeCompleted() {
                //                        runOnUiThread(new Runnable() {
                //                            @Override
                //                            public void run() {
                //                                v.setEnabled(true);
                //                            }
                //                        });
                //                        Log.d(AliyunTag.TAG, "Compose complete");
                //                    }
                //                });
                //                if(ret != AliyunErrorCode.ALIVC_COMMON_RETURN_SUCCESS) {
                //                    Log.e(AliyunTag.TAG, "Compose error, error code "+ret);
                //                    v.setEnabled(true);//compose error
                //                }

                //后台合成如下：如果要像Demo默认的这样，在其他页面合成，请参考下面这种方式
                mAliyunIEditor.saveEffectToLocal();
                //已经选择封面，并且封面尚未生成的过程不允许跳转
                if (hasCaptureCover && mSnapshop.isSnapshotting()) {
                    alivcEditHandler.sendEmptyMessageDelayed(SAVE_COVER, 500);
                    if (mTransitionAnimation == null) {
                        //转场animation
                        mTransitionAnimation = new AlivcCircleLoadingDialog(getContext(), mPasterContainer.getHeight());
                    }
                    mTransitionAnimation.show();
                    return;
                }
                if (hasCaptureCover && !mSnapshop.isSnapshotting()) {
                    //如果已经选择了封面，则直接跳到下一个页面
                    jumpToNextActivity();
                } else {
                    //如果没有选择封面，则选择原视频的缩略图作为封面，并开启loading框

                    if (mLoadingDialog == null) {
                        mLoadingDialog = new AlivcCircleLoadingDialog(getContext(), 0);
                        mLoadingDialog.show();
                    } else {
                        return;
                    }
                    final AliyunIThumbnailFetcher fetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher();
                    fetcher.fromConfigJson(mUri.getPath());
                    fetcher.setFastMode(true);
                    fetcher.setParameters(mAliyunIEditor.getVideoWidth(), mAliyunIEditor.getVideoHeight(),
                        AliyunIThumbnailFetcher.CropMode.Mediate, VideoDisplayMode.SCALE, 1);
                    fetcher.requestThumbnailImage(new long[] {0}, new AliyunIThumbnailFetcher.OnThumbnailCompletion() {

                        @Override
                        public void onThumbnailReady(Bitmap bitmap, long l, int index) {
                            FileOutputStream fileOutputStream = null;
                            try {
                                fileOutputStream = new FileOutputStream(PATH_THUMBNAIL);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                if (fileOutputStream != null) {
                                    try {
                                        fileOutputStream.close();
                                        fileOutputStream = null;
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            jumpToNextActivity();
                            mLoadingDialog.dismiss();
                            mLoadingDialog = null;
                            fetcher.release();
                        }

                        @Override
                        public void onError(int errorCode) {
                            fetcher.release();
                            mLoadingDialog.dismiss();
                            mLoadingDialog = null;
                        }
                    });
                }

            }
        });

        mPlayerListener = new PlayerListener() {

            @Override
            public long getCurrDuration() {
                return mAliyunIEditor.getPlayerController().getCurrentStreamPosition();
            }

            @Override
            public long getDuration() {
                long streamDuration = mAliyunIEditor.getPlayerController().getStreamDuration();
                Log.d(TAG, "getDuration: " + streamDuration);
                return streamDuration;
            }

            @Override
            public void updateDuration(long duration) {
                mTvCurrTime.setText(convertDuration2Text(duration));
                Log.d(TAG, "updateDuration: " + duration);
                for (OnVideoUpdateDurationListener videoUpdateDurationListener : playerListenerSet) {
                    if (videoUpdateDurationListener != null) {
                        videoUpdateDurationListener.onUpdateDuration(duration);
                    }
                }
            }
        };

        mViewStack.setPlayerListener(mPlayerListener);
        //配置缩略图滑动条
        initThumbLineBar();
        //非编辑态隐藏
        mThumbLineBar.hide();
        //时间特效恢复
        List<TimeFilter> timeFilters = mAliyunIEditor.getEditorProject().getAllTimeFilters();
        if (timeFilters.size() > 0) {
            TimeFilter timeFilter = timeFilters.get(0);
            EffectInfo effectInfo = new EffectInfo();
            effectInfo.type = UIEditorPage.TIME;
            effectInfo.timeEffectType = TimeEffectType.getTypeByValue(timeFilter.getTimeFilterType());
            effectInfo.startTime = timeFilter.getStartTime() * 1000;
            effectInfo.timeParam = timeFilter.getParam();
            effectInfo.endTime = (timeFilter.getStartTime() + timeFilter.getDuration()) * 1000;
            effectInfo.isMoment = effectInfo.timeEffectType != TimeEffectType.TIME_EFFECT_TYPE_INVERT;
            onResumeTimeEffectThumbLineBar(effectInfo);
            mEditorService.setLastTimeEffectInfo(effectInfo);
        }
        File mWatermarkFile = new File(
            getContext().getExternalFilesDir("") + "/AliyunEditorDemo/tail/logo.png");
        if (mWatermarkFile.exists()) {
            if (mWatermarkBitmap == null || mWatermarkBitmap.isRecycled()) {
                mWatermarkBitmap = BitmapFactory.decodeFile(
                    getContext().getExternalFilesDir("") + "/AliyunEditorDemo/tail/logo.png");
            }
            mSurfaceView.post(new Runnable() {
                @Override
                public void run() {
                    int outputWidth = mVideoParam.getOutputWidth();
                    int outputHeight = mVideoParam.getOutputHeight();
                    int mWatermarkBitmapWidth = DensityUtils.dip2px(getContext(), 30);
                    int mWatermarkBitmapHeight = DensityUtils.dip2px(getContext(), 30);
                    if (mWatermarkBitmap != null && !mWatermarkBitmap.isRecycled()) {
                        mWatermarkBitmapWidth = mWatermarkBitmap.getWidth();
                        mWatermarkBitmapHeight = mWatermarkBitmap.getHeight();
                    }
                    float posY = 0;
                    float percent = (float) outputHeight / outputWidth;
                    if (percent > 1.5) {
                        posY = 0f
                            + (float) (mWatermarkBitmapHeight / 2 + getContext().getResources().getDimensionPixelSize(
                            R.dimen.alivc_svideo_title_height)) / 1.5f / mSurfaceView.getHeight();
                    } else {
                        posY = 0f + (float) mWatermarkBitmapHeight / 1.5f / mSurfaceView.getHeight() / 2;
                    }
                    /**
                     * 水印例子 水印的大小为 ：水印图片的宽高和显示区域的宽高比，注意保持图片的比例，不然显示不完全
                     * 水印的位置为 ：以水印图片中心点为基准，显示区域宽高的比例为偏移量，0,0为左上角，1,1为右下角
                     *
                     */
                    if (hasWaterMark) {
                        mAliyunIEditor.applyWaterMark(
                            getContext().getExternalFilesDir("") + "/AliyunEditorDemo/tail/logo.png",
                            (float) mWatermarkBitmapWidth * 0.5f * 0.8f / mSurfaceView.getWidth(),
                            (float) mWatermarkBitmapHeight * 0.5f * 0.8f / mSurfaceView.getHeight(),
                            (float) mWatermarkBitmapWidth / 1.5f / mSurfaceView.getWidth() / 2,
                            posY);
                    }
                    //旋转水印
                    //ActionRotate actionRotate = new ActionRotate();
                    //actionRotate.setStartTime(0);
                    //actionRotate.setTargetId(id);
                    //actionRotate.setDuration(10 * 1000 * 1000);
                    //actionRotate.setRepeat(true);
                    //actionRotate.setDurationPerCircle(3 * 1000 * 1000);
                    //mAliyunIEditor.addFrameAnimation(actionRotate);


                    //                    if (hasWaterMark) {
                    //                        //图片水印
                    //                        EffectPicture effectPicture = new EffectPicture(getContext().getExternalFilesDir("")+ File.separator + "/AliyunEditorDemo/tail/logo.png");
                    //                        effectPicture.x = 0.12f;
                    //                        effectPicture.y = 0.1f;
                    //                        effectPicture.width = (float) mWatermarkBitmapWidth * 0.5f * 0.8f / mSurfaceView.getWidth();
                    //                        effectPicture.height = (float) mWatermarkBitmapHeight * 0.5f * 0.8f / mSurfaceView.getHeight();
                    //                        effectPicture.start = 0;
                    //                        effectPicture.end = mAliyunIEditor.getDuration() + 1000;
                    //                        mAliyunIEditor.addImage(effectPicture);
                    //
                    //                        ActionBase mActionBase = new ActionTranslate();
                    //                        ((ActionTranslate) mActionBase).setToPointX(1f);
                    //                        mActionBase.setStartTime(0);
                    //                        mActionBase.setDuration(1000 * 1000);
                    //                        mActionBase.setTargetId(effectPicture.getViewId());
                    //                        setTranslateParams(mWatermarkBitmapWidth,mWatermarkBitmapHeight,mActionBase);
                    //                        mAliyunIEditor.addFrameAnimation(mActionBase);
                    //                    }


                    //                    ActionRotate actionRotateImg = new ActionRotate();
                    //                    actionRotateImg.setStartTime(0);
                    //                    actionRotateImg.setTargetId(effectPicture.getViewId());
                    //                    actionRotateImg.setDuration(2 * 1000 * 1000);
                    //                    actionRotateImg.setRepeat(true);
                    //                    actionRotateImg.setDurationPerCircle(3 * 1000 * 1000);

                    if (hasTailAnimation) {
                        //片尾水印
                        mAliyunIEditor.addTailWaterMark(
                            getContext().getExternalFilesDir("") + "/AliyunEditorDemo/tail/logo.png",
                            (float) mWatermarkBitmapWidth / mSurfaceView.getWidth(),
                            (float) mWatermarkBitmapHeight / mSurfaceView.getHeight(), 0.5f, 0.5f, 2000 * 1000);
                    }

                }
            });
        }

        mAliyunIEditor.play();


        //        List<AliyunClip> clips = mAliyunIEditor.getSourcePartManager().getAllClips();
        //
        ////0-3s模糊
        //        mAliyunIEditor.applyBlurBackground(clips.get(0).getId(), 0, 3000, 10f);
        //
        ////0-3s进行 0.5x -> 1.5x  的放大
        //        ActionScale scaleAction = new ActionScale();
        //        scaleAction.setTargetId(clips.get(0).getId());
        //        scaleAction.setStartTime(0);
        //        scaleAction.setDuration(3000 * 1000);
        //        scaleAction.setFromScale(0.5f);
        //        scaleAction.setToScale(1f);
        //        scaleAction.setIsStream(true);
        //        mAliyunIEditor.addFrameAnimation(scaleAction);
    }


    /**
     * @param actionBase ActionBase
     */
    private void setTranslateParams(int width, int height, ActionBase actionBase) {
        ActionTranslate actionTranslate = (ActionTranslate) actionBase;
        float x = -0.1f;
        float y = 1 - 0.12f;
        //入场1s结束
        actionTranslate.setToPointX(x);
        actionTranslate.setToPointY(y);
        //向右平移
        actionTranslate.setFromPointY(y);
        actionTranslate.setFromPointX(-1);
    }

    /**
     * 配置新的缩略条
     */
    private void initThumbLineBar() {
        //获取每张缩略图的尺寸
        int thumbnailSize = getResources().getDimensionPixelOffset(R.dimen.aliyun_editor_size_square_thumbnail);
        Point thumbnailPoint = new Point(thumbnailSize, thumbnailSize);

        //缩略图获取
        if (mThumbnailFetcher == null) {
            mThumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher();
            mThumbnailFetcher.fromConfigJson(mUri.getPath());
        } else if (mThumbnailFetcher.getTotalDuration() != mAliyunIEditor.getPlayerController().getStreamDuration()) {
            //时长改变的时候才去修改缩略图
            Log.i(TAG, "initThumbLineBar: reset thumbLine");
            mAliyunIEditor.saveEffectToLocal();
            mThumbnailFetcher.release();
            mThumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher();
            mThumbnailFetcher.fromConfigJson(mUri.getPath());
        }

        int thumbCount = 10;
        if (mThumbnailFetcher.getTotalDuration() > 10 * 1000) {
            thumbCount = (int) (mThumbnailFetcher.getTotalDuration() / 1000);
        }

        //设置缩略条配置文件
        ThumbLineConfig thumbLineConfig = new ThumbLineConfig.Builder()
            .thumbnailFetcher(mThumbnailFetcher)
            .screenWidth(mScreenWidth)
            .thumbPoint(thumbnailPoint)
            .thumbnailCount(thumbCount).build();

        if (mThumbLineBar == null) {
            mThumbLineBar = findViewById(R.id.simplethumblinebar);

            mBarSeekListener = new ThumbLineBar.OnBarSeekListener() {

                @Override
                public void onThumbLineBarSeek(long duration) {
                    mAliyunIEditor.seek(duration, TimeUnit.MILLISECONDS);
                    if (mThumbLineBar != null) {
                        mThumbLineBar.pause();
                    }
                    switchPlayStateUI(true);
                    if (mCurrentEditEffect != null && !mCurrentEditEffect.isEditCompleted()) {
                        if (!mCurrentEditEffect.isVisibleInTime(mAliyunIEditor.getPlayerController().getCurrentPlayPosition())) {
                            //隐藏
                            mCurrentEditEffect.setPasterViewVisibility(View.GONE);
                        } else {
                            //显示
                            mCurrentEditEffect.setPasterViewVisibility(View.VISIBLE);
                        }
                    }
                    if (mUseInvert) {
                        //当seek到最后时，不允许添加特效
                        if (duration <= USE_ANIMATION_REMAIN_TIME) {
                            mCanAddAnimation = false;
                        } else {
                            mCanAddAnimation = true;
                        }
                    } else {
                        //当seek到最后时，不允许添加特效
                        if (mAliyunIEditor.getPlayerController().getDuration() - duration <= USE_ANIMATION_REMAIN_TIME) {
                            mCanAddAnimation = false;
                        } else {
                            mCanAddAnimation = true;
                        }
                    }

                }

                @Override
                public void onThumbLineBarSeekFinish(long duration) {
                    mAliyunIEditor.seek(duration, TimeUnit.MILLISECONDS);
                    if (mThumbLineBar != null) {
                        mThumbLineBar.pause();
                    }
                    switchPlayStateUI(true);
                    if (mUseInvert) {
                        //当seek到最后时，不允许添加特效
                        if (duration <= USE_ANIMATION_REMAIN_TIME) {
                            mCanAddAnimation = false;
                        } else {
                            mCanAddAnimation = true;
                        }
                    } else {
                        //当seek到最后时，不允许添加特效
                        if (mAliyunIEditor.getPlayerController().getDuration() - duration >= USE_ANIMATION_REMAIN_TIME) {
                            mCanAddAnimation = true;
                        } else {
                            mCanAddAnimation = false;
                        }
                    }
                }
            };

            //Overlay相关View
            mThumbLineOverlayView = new ThumbLineOverlay.ThumbLineOverlayView() {
                View rootView = LayoutInflater.from(getContext()).inflate(
                    R.layout.alivc_editor_view_timeline_overlay, null);
                View headView = rootView.findViewById(R.id.head_view);
                View tailView = rootView.findViewById(R.id.tail_view);
                View middleView = rootView.findViewById(R.id.middle_view);

                @Override
                public ViewGroup getContainer() {
                    return (ViewGroup) rootView;
                }

                @Override
                public View getHeadView() {
                    return headView;
                }

                @Override
                public View getTailView() {
                    return tailView;
                }

                @Override
                public View getMiddleView() {
                    return middleView;
                }
            };

        }

        mThumbLineBar.setup(thumbLineConfig, mBarSeekListener, mPlayerListener);

        if (mAnimationFilterController != null) {
            mAnimationFilterController.setThumbLineBar(mThumbLineBar);
        }
    }

    /**
     * 更改播放状态的图标和文字 播放时,文字内容显示为: 暂停播放, 图标使暂停图标, mipmap/aliyun_svideo_pause 暂停时,文字内容显示为: 播放全篇, 图标使用播放图标,
     * mipmap/aliyun_svideo_play
     *
     * @param changeState, 需要显示的状态,  true: 播放全篇, false: 暂停播放
     */
    public void switchPlayStateUI(boolean changeState) {
        if (changeState) {
            mPlayImage.setText(getResources().getString(R.string.alivc_editor_edit_play_start));
            UIConfigManager.setImageResourceConfig(mPlayImage, 0, R.attr.playImage, R.mipmap.aliyun_svideo_play);
        } else {
            mPlayImage.setText(getResources().getString(R.string.alivc_editor_edit_play_pause));
            UIConfigManager.setImageResourceConfig(mPlayImage, 0, R.attr.pauseImage, R.mipmap.aliyun_svideo_pause);
        }
    }

    private final OnPasterRestored mOnPasterRestoreListener = new OnPasterRestored() {

        @Override
        public void onPasterRestored(final List<AliyunPasterAbstractController> controllers) {
            if (controllers == null || controllers.size() == 0) {
                return;
            }
            Log.d(TAG, "onPasterRestored: " + controllers.size());

            mPasterContainer.post(new Runnable() {//之所以要放在这里面，是因为下面的操作中有UI相关的，需要保证布局完成后执行，才能保证UI更新的正确性
                @Override
                public void run() {

                    if (mThumbLineBar != null && mThumbLineBar.getChildCount() != 0) {
                        //这里做合成（时间和转场特效会清空paster特效）恢复 针对缩略图的覆盖效果
                        mThumbLineBar.removeOverlayByPages(
                            UIEditorPage.OVERLAY,
                            UIEditorPage.COMPOUND_CAPTION
                        );
                    }

                    if (mPasterContainer != null) {
                        mPasterContainer.removeAllViews();
                    }
                    final List<AliyunBasePasterController> aps = new ArrayList<>();
                    for (AliyunPasterAbstractController controller : controllers) {
                        if (controller instanceof AliyunPasterController) {
                            AliyunPasterController c = (AliyunPasterController) controller;
                            if (!c.isPasterExists()) {
                                continue;
                            }
                            if (c.getPasterDuration(TimeUnit.MILLISECONDS) >= mAliyunIEditor.getPlayerController().getStreamDuration()) {
                                //恢复时覆盖超出缩略图,丢弃
                                continue;
                            }
                            c.setOnlyApplyUI(true);
                            if (c.getEffect() instanceof EffectPaster) {
                                EffectPaster paster = (EffectPaster) c.getEffect();
                                float scaleSize = mViewOperate.getPasterScaleSize(paster.getPasterType());
                                int displayWidth = (int) (mPasterContainerPoint.x * scaleSize);
                                int displayHeight = (int) (mPasterContainerPoint.y * scaleSize);
                                if (paster.displayWidth != displayWidth) {
                                    paster.x = (paster.x / paster.displayWidth) * displayWidth;
                                    paster.y = (paster.y / paster.displayHeight) * displayHeight;
                                    int oldS = Math.min(paster.displayWidth, paster.displayHeight);
                                    int newS = Math.min(displayWidth, displayHeight);
                                    float scale = newS / (float) oldS;
                                    float wScale = displayWidth / (float) paster.displayWidth;
                                    float hScale = displayHeight / (float) paster.displayHeight;
                                    float w = paster.width * wScale;
                                    float h = paster.height * hScale;
                                    paster.width = (int) Math.ceil(w);
                                    paster.height = (int) Math.ceil(h);
                                    if (paster instanceof EffectText) {
                                        EffectText text = (EffectText) paster;
                                        float tw = (float) text.textWidth * wScale;
                                        text.textWidth = (int) Math.ceil(tw);
                                        float th = (float) text.textHeight * hScale;
                                        text.textHeight = (int) Math.ceil(th);
                                        float tSize = (float) text.mTextSize * scale;
                                        text.mTextSize = (int) Math.ceil(tSize);
                                        float tx = (float) text.mTextPaddingX * wScale;
                                        text.mTextPaddingX = (int) tx;
                                        float ty = (float) text.mTextPaddingY * hScale;
                                        text.mTextPaddingY = (int) ty;
                                    }
                                    if (paster instanceof EffectCaption) {
                                        EffectCaption caption = (EffectCaption) paster;
                                        float tcx = (float) caption.textCenterX * wScale;
                                        caption.textCenterX = (int) Math.ceil(tcx);
                                        float tcy = (float) caption.textCenterY * hScale;
                                        caption.textCenterY = (int) Math.ceil(tcy);
                                    }

                                    paster.displayWidth = displayWidth;
                                    paster.displayHeight = displayHeight;
                                }
                            }
                            if (c.getPasterType() == EffectPaster.PASTER_TYPE_GIF) {
                                mCurrentEditEffect = addPaster(c);
                            } else if (c.getPasterType() == EffectPaster.PASTER_TYPE_TEXT) {
                                mCurrentEditEffect = addSubtitle(c, true);
                            } else if (c.getPasterType() == EffectPaster.PASTER_TYPE_CAPTION) {
                                mCurrentEditEffect = addCaption(c);
                            }
                            if (mCurrentEditEffect instanceof AbstractPasterUISimpleImpl && c.getEffect() instanceof EffectPaster
                                && ((EffectPaster) c.getEffect()).action != null) {
                                ((AbstractPasterUISimpleImpl) mCurrentEditEffect).setFrameAction(((EffectPaster) c.getEffect()).action);
                                ((AbstractPasterUISimpleImpl) mCurrentEditEffect).setTempFrameAction(((EffectPaster) c.getEffect()).action);
                            }
                            mCurrentEditEffect.showTimeEdit();
                            View pasterView = mCurrentEditEffect.getPasterView();
                            if (pasterView != null) {
                                pasterView.setVisibility(View.INVISIBLE);
                            }
                            aps.add(mCurrentEditEffect);
                            mCurrentEditEffect.moveToCenter();
                            mCurrentEditEffect.hideOverlayView();
                        } else if (controller instanceof AliyunPasterControllerCompoundCaption) {
                            if (controller.getStartTime(TimeUnit.MILLISECONDS) >= mAliyunIEditor.getPlayerController().getStreamDuration()) {
                                //恢复时覆盖超出缩略图,丢弃
                                continue;
                            }
                            AliyunPasterControllerCompoundCaption controllerCompoundCaption = (AliyunPasterControllerCompoundCaption) controller;
                            if (mPasterUICompoundCaptionImpl == null) {
                                mPasterUICompoundCaptionImpl = new PasterUICompoundCaptionImpl();
                            }

                            mCurrentEditEffect = mPasterUICompoundCaptionImpl;
                            mPasterUICompoundCaptionImpl.updateParams(AlivcEditView.this, controllerCompoundCaption, mPasterManager, mThumbLineBar);
                            mPasterUICompoundCaptionImpl.showTimeEdit();
                            aps.add(mCurrentEditEffect);
                        }
                    }

                    for (AliyunBasePasterController pui : aps) {
                        pui.editTimeCompleted();
                        if (pui instanceof AbstractPasterUISimpleImpl &&
                            ((AbstractPasterUISimpleImpl) pui).getEffect() instanceof EffectCaption) {
                            ((AbstractPasterUISimpleImpl) pui).getController().setEffect((EffectBase) pui.getTextView().getTag());
                        }
                        pui.setOnlyApplyUI(false);
                    }

                }
            });
        }

    };

    @Override
    public void onEffectChange(final EffectInfo effectInfo) {
        Log.e("editor", "====== onEffectChange ");
        //返回素材属性

        EffectBean effect = new EffectBean();
        effect.setId(effectInfo.id);
        if (effectInfo.getSource() != null) {
            effect.setSource(effectInfo.getSource());
            Log.d(TAG, "effect path " + effectInfo.getSource().getPath());
        }
        UIEditorPage type = effectInfo.type;
        switch (type) {
        case AUDIO_MIX:
            if (!effectInfo.isAudioMixBar) {
                //重制mv和混音的音效
                mAliyunIEditor.resetEffect(EffectType.EFFECT_TYPE_MIX);
                mAliyunIEditor.resetEffect(EffectType.EFFECT_TYPE_MV_AUDIO);
                if (lastMusicBean != null) {
                    mAliyunIEditor.removeMusic(lastMusicBean);
                }

                if (effectInfo.getSource() != null && !TextUtils.isEmpty(effectInfo.getSource().getPath())) {

                    TrackAudioStream.Builder lBuilder = new TrackAudioStream.Builder()
                    .source(effectInfo.getSource())
                    .startTime(effectInfo.startTime, TimeUnit.MILLISECONDS)
                    .streamStartTime(effectInfo.streamStartTime, TimeUnit.MILLISECONDS)
                    .streamDuration(effectInfo.streamEndTime - effectInfo.streamStartTime, TimeUnit.MILLISECONDS)
                    .audioWeight(effectInfo.musicWeight);

                    lastMusicBean = lBuilder.build();
                    effectInfo.mixId = mAliyunIEditor.applyMusic(lastMusicBean);
                } else {
                    lastMusicBean = null;
                    //恢复mv声音
                    if (mLastMVEffect != null && mLastMVEffect.getSource() != null && !TextUtils.isEmpty(mLastMVEffect.getSource().getPath())) {
                        applyMVEffect(mLastMVEffect);
                        }
                    }
                } else {
                    effectInfo.mixId = mAliyunIEditor.getMusicLastApplyId();
                }
                if (isReplaceMusic) {
                    mAliyunIEditor.applyMusicMixWeight(effectInfo.mixId, 100);
                } else {
                    mAliyunIEditor.applyMusicMixWeight(effectInfo.mixId, effectInfo.musicWeight);
                }
                mAliyunIEditor.seek(0);
                // 确定重新开始播放
                playingResume();
                break;
            case FILTER_EFFECT:
                if (effect.getSource() != null && effect.getSource().getPath().contains("Vertigo")) {
                    TrackEffectFilter lTrackEffectFilter = new TrackEffectFilter.Builder()
                        .source(effect.getSource())
                        .build();
                    mAliyunIEditor.addAnimationFilter(lTrackEffectFilter);
                } else {
                    mAliyunIEditor.applyFilter(effect.toTrackEffectFilter());
                }
                break;

            case SOUND:
                // 音效

                List<AliyunClip> allClips = mAliyunIEditor.getSourcePartManager().getAllClips();
                int size = allClips.size();
                //音效状态恢复
                if (mLastSoundEffect == null) {
                    List<Effect> effectList = mAliyunIEditor.getEditorProject().getTimeline().getPrimaryTrack().getVideoTrackClips().get(0).getEffects();
                    for (Effect item : effectList) {
                        if (item.getType() == Effect.Type.audio_effect) {
                            mLastSoundEffect = new SoundEffectInfo();
                            mLastSoundEffect.audioEffectType = ((AudioEffect)item).mEffectType;
                            mLastSoundEffect.soundWeight = ((AudioEffect)item).mEffectParam;
                            break;
                        }
                    }
                }
                for (int i = 0; i < size; i++) {
                    if (mLastSoundEffect != null) {
                        mAliyunIEditor.removeAudioEffect(allClips.get(i).getId(), mLastSoundEffect.audioEffectType);
                    }
                    mAliyunIEditor.applyAudioEffect(allClips.get(i).getId(), effectInfo.audioEffectType, effectInfo.soundWeight);
                }
                mLastSoundEffect = effectInfo;
                Log.i("log_editor_sound_type", String.valueOf(effectInfo.audioEffectType));
                mAliyunIEditor.seek(0);
                mAliyunIEditor.play();
                switchPlayStateUI(false);

                break;
            case MV:
                //保存最后一次应用的MV，用于音乐选择无的时候恢复MV的声音
                mLastMVEffect = effectInfo;
                applyMVEffect(effectInfo);

                break;
            case CAPTION:
                mAliyunPasterController = mPasterManager.addPaster(effectInfo.getSource());

                if (mAliyunPasterController != null) {
                    //获取字幕中的字体
                    EffectBase effectBase = mAliyunPasterController.getEffect();
                    if (effectBase instanceof EffectCaption) {
                        if (effectInfo.fontSource != null) {
                            effectInfo.fontSource.setPath(effectInfo.fontSource.getPath() + "/font.ttf");
                            ((EffectCaption) effectBase).fontSource = effectInfo.fontSource;
                        } else {
                            ((EffectCaption) effectBase).fontSource = null;
                        }
                        mAliyunPasterController.setEffect(effectBase);
                    }

                    mAliyunPasterController.setPasterStartTime(mAliyunIEditor.getPlayerController().getCurrentStreamPosition(), TimeUnit.MILLISECONDS);
                    PasterUICaptionImpl cui = addCaption(mAliyunPasterController);
                    if (mCurrentEditEffect != null && !mCurrentEditEffect.isEditCompleted()) {
                        //如果有正在编辑的paster，之前的remove
                        mCurrentEditEffect.removePaster();
                    }
                    playingPause();
                    mCurrentEditEffect = cui;
                    mCurrentEditEffect.showTimeEdit();
                    //              气泡字幕默认不弹出输入法
                    //                cui.showTextEdit(mUseInvert);
                } else {
                    showToast = FixedToastUtils.show(getContext(), getResources().getString(R.string.alivc_editor_edit_tip_captions_fail));
                }
                break;
            case OVERLAY:
                mAliyunPasterController = mPasterManager.addPaster(effectInfo.getSource());
                if (mAliyunPasterController != null) {
                    //add success
                    mAliyunPasterController.setPasterStartTime(mAliyunIEditor.getPlayerController().getCurrentStreamPosition(), TimeUnit.MILLISECONDS);
                    PasterUIGifImpl gifui = addPaster(mAliyunPasterController);
                    if (mCurrentEditEffect != null && !mCurrentEditEffect.isEditCompleted()) {
                        //如果有正在编辑的paster，之前的remove
                        mCurrentEditEffect.removePaster();
                    }
                    playingPause();
                    mCurrentEditEffect = gifui;
                    mCurrentEditEffect.showTimeEdit();
                } else {
                    //add failed
                    showToast = FixedToastUtils.show(getContext(), getResources().getString(R.string.alivc_editor_edit_tip_gif_fail));
                }

                break;
            case FONT:
                if (effectInfo.fontSource != null) {
                    effectInfo.fontSource.setPath(effectInfo.fontSource.getPath() + "/font.ttf");
                }
                mAliyunPasterController = mPasterManager.addSubtitle(null, effectInfo.fontSource);
                if (mAliyunPasterController != null) {
                    mAliyunPasterController.setPasterStartTime(mAliyunIEditor.getPlayerController().getCurrentStreamPosition(), TimeUnit.MILLISECONDS);
                    PasterUITextImpl textui = addSubtitle(mAliyunPasterController, false);
                    if (mCurrentEditEffect != null && !mCurrentEditEffect.isEditCompleted()) {
                        //如果有正在编辑的paster，之前的remove
                        mCurrentEditEffect.removePaster();
                    }
                    playingPause();
                    mCurrentEditEffect = textui;
                    mCurrentEditEffect.showTimeEdit();
                    textui.showTextEdit(mUseInvert);
                } else {
                    showToast = FixedToastUtils.show(getContext(), getResources().getString(R.string.alivc_editor_edit_tip_word_fail));
                }
                break;
            case COMPOUND_CAPTION:
                if (mCurrentEditEffect != null && !(mCurrentEditEffect instanceof PasterUICompoundCaptionImpl)
                    && !mCurrentEditEffect.isEditCompleted()) {
                    //如果有正在编辑的paster，之前的remove
                    mCurrentEditEffect.removePaster();
                }
                if (mCurrentEditEffect instanceof PasterUICompoundCaptionImpl) {
                    mCurrentEditEffect.editTimeCompleted();
                }
                playingPause();
                long captionDuration = CaptionManager.captionDurationBoundJudge(mAliyunIEditor, CaptionConfig.DEFAULT_DURATION);
                AliyunPasterControllerCompoundCaption aliyunPasterControllerCompoundCaption = CaptionManager.addCaptionWithStartTime(getContext(), mPasterManager,
                    null, null, mAliyunIEditor.getPlayerController().getCurrentPlayPosition(), captionDuration);
                if (aliyunPasterControllerCompoundCaption != null) {
                    if (mPasterUICompoundCaptionImpl == null) {
                        mPasterUICompoundCaptionImpl = new PasterUICompoundCaptionImpl();
                    }
                    mCurrentEditEffect = mPasterUICompoundCaptionImpl;
                    mPasterUICompoundCaptionImpl.updateParams(this, aliyunPasterControllerCompoundCaption, mPasterManager, mThumbLineBar);
                    mPasterUICompoundCaptionImpl.showCaptionBorderView(mPasterContainer, mSurfaceView);
                    mPasterUICompoundCaptionImpl.showTimeEdit();
                    mPasterUICompoundCaptionImpl.editTimeStart();
                    mViewOperate.setCaptionTextView(AlivcEditorViewFactory.findCaptionEditorPanelView(this));
                } else {
                    showToast = FixedToastUtils.show(getContext(), getResources().getString(R.string.alivc_editor_edit_tip_word_fail));
                }

                break;
            case TIME:
                if (effectInfo.startTime < 0) {
                    effectInfo.startTime = TimeUnit.MILLISECONDS.toMicros(mAliyunIEditor.getPlayerController().getCurrentStreamPosition());
                }
                if (mIsTranscoding) {
                    showToast = FixedToastUtils.show(getContext(),
                        getResources().getString(R.string.alivc_editor_edit_tip_transcode_no_operate));
                    return;
                }
                //            当前有动画效果时，执行倒播，增加提示信息
                if (effectInfo.timeEffectType.equals(TimeEffectType.TIME_EFFECT_TYPE_INVERT) && mCurrentEditEffect != null && mCurrentEditEffect.isAddedAnimation()) {
                    FixedToastUtils.show(getContext(), getContext().getString(R.string.alivc_editor_dialog_caption_tip_not_support));
                }
                applyTimeEffect(effectInfo);
                break;
            case TRANSITION:
                if (effectInfo.isUpdateTransition) {
                    //                保存转场效果，只保存第一次获取到的值
                    if (mTransitionCache == null) {
                        mTransitionCache = new LinkedHashMap<>();
                    }
                    if (mTransitionParamsCache == null) {
                        mTransitionParamsCache = new LinkedHashMap<>();
                    }
                    if (mTransitionCache != null && mTransitionCache.get(effectInfo.clipIndex) == null) {
                        mTransitionCache.put(effectInfo.clipIndex, effectInfo);
                        List<EffectConfig.NodeBean> nodeTree = effectInfo.transitionBase.getNodeTree();
                        List<AlivcTransBean> paramsList = new ArrayList<>();
                        if (nodeTree == null || nodeTree.size() == 0) {
                            return;
                        }
                        for (EffectConfig.NodeBean nodeBean : nodeTree) {
                            List<EffectConfig.NodeBean.Params> params = nodeBean.getParams();
                            if (params == null || params.size() == 0) {
                                continue;
                            }
                            for (EffectConfig.NodeBean.Params param : params) {
                                ValueTypeEnum valueTypeEnum = param.getType();
                                if (valueTypeEnum == ValueTypeEnum.INT) {
                                    AlivcTransBean alivcTransBean = new AlivcTransBean();
                                    alivcTransBean.setmType(valueTypeEnum);
                                    if (param.getValue().getValue() != null && param.getValue().getValue().length > 0) {
                                        alivcTransBean.setmIntergerValue((int) param.getValue().getValue()[0]);
                                    }
                                    paramsList.add(alivcTransBean);
                                } else if (valueTypeEnum == ValueTypeEnum.FLOAT) {
                                    AlivcTransBean alivcTransBean = new AlivcTransBean();
                                    alivcTransBean.setmType(valueTypeEnum);
                                    if (param.getValue().getValue() != null && param.getValue().getValue().length > 0) {
                                        alivcTransBean.setmFloatValue((float) param.getValue().getValue()[0]);
                                    }
                                    paramsList.add(alivcTransBean);
                                } else {
                                    AlivcTransBean alivcTransBean = new AlivcTransBean();
                                    alivcTransBean.setmType(valueTypeEnum);
                                    paramsList.add(alivcTransBean);
                                }
                            }
                        }
                        mTransitionParamsCache.put(effectInfo.clipIndex, paramsList);
                    }
                    //                更新转场
                    effectInfo.isUpdateTransition = false;
                    mAliyunIEditor.updateTransition(effectInfo.clipIndex, effectInfo.transitionBase);
                } else {
                    setTransition(effectInfo);
                }

                break;
            case ROLL_CAPTION:
                mAliyunIEditor.seek(0);
                mAliyunIEditor.play();
                switchPlayStateUI(false);
                break;
            default:
                break;
        }
    }

    /**
     * 应用MV特效
     *
     * @param effectInfo
     */
    private void applyMVEffect(EffectInfo effectInfo) {
        EffectBean effect = new EffectBean();
        effect.setId(effectInfo.id);
        if (mCurrentEditEffect != null && !mCurrentEditEffect.isPasterRemoved()) {
            mCurrentEditEffect.editTimeCompleted();
        }

        String path = null;
        if (effectInfo.list != null) {
            path = EditorCommon.getMVPath(effectInfo.list, mVideoParam.getOutputWidth(),
                mVideoParam.getOutputHeight());
        }
        effect.setPath(path);
        effectInfo.setPath(path);
        Source source = new Source(path);
        source.setId(String.valueOf(effectInfo.id));
        if (!StringUtils.isEmpty(path) && source.getPath().contains(File.separator)) {
            boolean isApp = path.contains("aliyun_svideo_mv/");
            String name = path.substring(path.lastIndexOf(File.separator) + 1);
            source.setURL(AlivcResUtil.getResUri(isApp ? "app" : "cloud", AlivcResUtil.TYPE_MV, String.valueOf(effectInfo.id), name));
        }

        effect.setSource(source);
        effectInfo.setSource(source);
        int id;
        if (path != null && new File(path).exists()) {
            mAliyunIEditor.resetEffect(EffectType.EFFECT_TYPE_MIX);
            //先执行applyMV之后才能拿到对应的getMvAudioId
            TrackEffectMV trackEffectMV = effect.toTrackMV();
            mAliyunIEditor.applyMV(trackEffectMV);
            // 注意：applyMV时内部会更新设置 TrackEffectMV 的流ID，因此调用完也需要更新设置 EffectBean#setMvAudioId()
            effect.setMvAudioId(trackEffectMV.getAudioStreamId());
            id = effect.getMvAudioId();
            Log.d(TAG, "editor resetEffect end:" + id);
            if (isReplaceMusic) {
                mAliyunIEditor.applyMusicMixWeight(id, 100);

            } else if (isMixRecord) {
                //如果是合拍，不需要mv音乐
                mAliyunIEditor.applyMusicWeight(id, 0);
            } else {
                mAliyunIEditor.applyMusicMixWeight(id, effect.getWeight());

            }
        } else {
            mAliyunIEditor.resetEffect(EffectType.EFFECT_TYPE_MV);
            if (lastMusicBean != null && lastMusicBean.getSource() != null && lastMusicBean.getSource().getPath() != null) {
                mAliyunIEditor.applyMusic(lastMusicBean);
                id = lastMusicBean.getStreamId();
                if (isReplaceMusic) {
                    mAliyunIEditor.applyMusicMixWeight(id, 100);
                } else if (isMixRecord) {
                    mAliyunIEditor.applyMusicWeight(id, 0);
                } else {
                    mAliyunIEditor.applyMusicMixWeight(id, lastMusicBean.getAudioWeight());
                }
            } else {
                if (isReplaceMusic) {
                    //恢复原音
                    mAliyunIEditor.applyMusicMixWeight(0, 0);
                }
            }
        }
        //重新播放，倒播重播流时间轴需要设置到最后
        if (mUseInvert) {
            mAliyunIEditor.seek(mAliyunIEditor.getPlayerController().getStreamDuration(), TimeUnit.MILLISECONDS);
        } else {
            mAliyunIEditor.seek(0);
        }
        mAliyunIEditor.resume();
        if (mThumbLineBar != null) {
            mThumbLineBar.resume();
        }
        switchPlayStateUI(false);
    }

    private void onResumeTimeEffectThumbLineBar(EffectInfo effectInfo) {
        mUseInvert = false;
        if (mTimeEffectOverlay != null) {
            mThumbLineBar.removeOverlay(mTimeEffectOverlay);
        }

        if (effectInfo.timeEffectType.equals(TimeEffectType.TIME_EFFECT_TYPE_RATE)) {
            if (effectInfo.isMoment) {
                mTimeEffectOverlay = mThumbLineBar.addOverlay(effectInfo.startTime / 1000, (effectInfo.endTime - effectInfo.startTime) / 1000, mThumbLineOverlayView,
                    0, false, UIEditorPage.TIME);
            } else {
                mTimeEffectOverlay = mThumbLineBar.addOverlay(0, 1000000L, mThumbLineOverlayView, 0, false,
                    UIEditorPage.TIME);
            }
        } else if (effectInfo.timeEffectType.equals(TimeEffectType.TIME_EFFECT_TYPE_INVERT)) {
            mUseInvert = true;
            mTimeEffectOverlay = mThumbLineBar.addOverlay(0, 1000000L, mThumbLineOverlayView, 0, false,
                UIEditorPage.TIME);
        } else if (effectInfo.timeEffectType.equals(TimeEffectType.TIME_EFFECT_TYPE_REPEAT)) {
            mTimeEffectOverlay = mThumbLineBar.addOverlay(effectInfo.startTime / 1000, (effectInfo.endTime - effectInfo.startTime) / 1000, mThumbLineOverlayView, 0,
                false, UIEditorPage.TIME);
        }
        if (mTimeEffectOverlay != null) {
            mTimeEffectOverlay.switchState(ThumbLineOverlay.STATE_FIX);
        }
    }

    private void applyTimeEffect(EffectInfo effectInfo) {

        mUseInvert = false;
        if (mTimeEffectOverlay != null) {
            mThumbLineBar.removeOverlay(mTimeEffectOverlay);
        }
        mAliyunIEditor.resetEffect(EffectType.EFFECT_TYPE_TIME);
        if (effectInfo.timeEffectType.equals(TimeEffectType.TIME_EFFECT_TYPE_NONE)) {
            playingResume();
        } else if (effectInfo.timeEffectType.equals(TimeEffectType.TIME_EFFECT_TYPE_RATE)) {
            if (effectInfo.isMoment) {

                mTimeEffectOverlay = mThumbLineBar.addOverlay(effectInfo.startTime / 1000, 1000, mThumbLineOverlayView,
                    0, false, UIEditorPage.TIME);
                //mAliyunIEditor.stop();
                //playingPause();
                mAliyunIEditor.stop();
                mAliyunIEditor.rate(effectInfo.timeParam, effectInfo.startTime, 1000, false);
                playingResume();
            } else {
                mTimeEffectOverlay = mThumbLineBar.addOverlay(0, 1000000L, mThumbLineOverlayView, 0, false,
                    UIEditorPage.TIME);
                //playingPause();
                mAliyunIEditor.stop();
                mAliyunIEditor.rate(effectInfo.timeParam, 0, 1000000L, false);
                playingResume();

            }
        } else if (effectInfo.timeEffectType.equals(TimeEffectType.TIME_EFFECT_TYPE_INVERT)) {

            mUseInvert = true;
            mTimeEffectOverlay = mThumbLineBar.addOverlay(0, 1000000L, mThumbLineOverlayView, 0, false,
                UIEditorPage.TIME);
            //mAliyunIEditor.stop();
            //playingPause();
            checkAndTranscode(TimeEffectType.TIME_EFFECT_TYPE_INVERT, 0, 0, 0, false);
        } else if (effectInfo.timeEffectType.equals(TimeEffectType.TIME_EFFECT_TYPE_REPEAT)) {
            mTimeEffectOverlay = mThumbLineBar.addOverlay(effectInfo.startTime / 1000, 1000, mThumbLineOverlayView, 0,
                false, UIEditorPage.TIME);
            //mAliyunIEditor.stop();
            //playingPause();
            checkAndTranscode(TimeEffectType.TIME_EFFECT_TYPE_REPEAT, 3, effectInfo.startTime, 1000, false);
        }
        if (mTimeEffectOverlay != null) {
            mTimeEffectOverlay.switchState(ThumbLineOverlay.STATE_FIX);
        }
    }

    private boolean mIsTransitioning = false;
    private AlivcCircleLoadingDialog mTransitionAnimation;

    private void startTransitionAnimation() {
        mTransitionAnimation.show();
        mIsTransitioning = true;
    }

    private void stopTransitionAnimation() {
        mTransitionAnimation.dismiss();
        mIsTransitioning = false;
    }

    private void setTransition(final EffectInfo effectInfo) {

        if (mTransitionAnimation == null) {
            //转场animation
            mTransitionAnimation = new AlivcCircleLoadingDialog(getContext(), mPasterContainer.getHeight());
        }
        if (mIsTransitioning) {
            return;
        }
        startTransitionAnimation();

        if (effectInfo.mutiEffect == null) {
            //添加转场特效
            final TransitionBase transition = getTransitionBase(effectInfo);
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    mAliyunIEditor.saveEffectToLocal();
                    mAliyunIEditor.setTransition(effectInfo.clipIndex, transition);

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("effectInfo", effectInfo);
                    Message message = new Message();
                    message.what = ADD_TRANSITION;
                    message.setData(bundle);
                    alivcEditHandler.sendMessage(message);
                    resetTimeLineLayout();
                }
            });
        } else if (effectInfo.mutiEffect.size() != 0) {
            //撤销转场特效
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    mAliyunIEditor.saveEffectToLocal();
                    Map<Integer, TransitionBase> hashMap = new HashMap<>();
                    for (EffectInfo info : effectInfo.mutiEffect) {
                        TransitionBase transitionBase = getTransitionBase(info);
                        hashMap.put(info.clipIndex, transitionBase);
                    }
                    mAliyunIEditor.setTransition(hashMap);

                    alivcEditHandler.sendEmptyMessage(REVERT_TRANSITION);
                    resetTimeLineLayout();

                }
            });

        } else {
            stopTransitionAnimation();
        }

    }

    /**
     * 初始化线程池和Handler
     */
    private void initThreadHandler() {
        executorService = ThreadUtil.newDynamicSingleThreadedExecutor(new AlivcEditThread());
        alivcEditHandler = new AlivcEditHandler(this);
    }

    /**
     * 是否存在录制有音乐
     *
     * @param isHashRecordMusic boolean
     */
    public void setHasRecordMusic(boolean isHashRecordMusic) {
        this.mHasRecordMusic = isHashRecordMusic;
    }

    /**
     * 获取存在录制有音乐
     *
     * @return boolean
     */
    public boolean isHasRecordMusic() {
        return mHasRecordMusic;
    }

    /**
     * 是否是合拍过来的视频
     */
    public void setIsMixRecord(boolean isMixRecord) {
        this.isMixRecord = isMixRecord;
    }

    /**
     * 获取是否是合拍
     */
    public boolean isMaxRecord() {
        return isMixRecord;
    }

    public void setHasDeNoise(boolean deNoise) {
        this.mHasDeNoise = deNoise;
    }

    @Override
    public View getSufaceView() {
        return mSurfaceView;
    }

    @Override
    public AlivcEditView getAlivcEditView() {
        return this;
    }

    @Override
    public AliyunIEditor getAliyunIEditor() {
        return mAliyunIEditor;
    }

    @Override
    public void addVideoUpdateListener(OnVideoUpdateDurationListener videoUpdateDurationListener) {
        playerListenerSet.add(videoUpdateDurationListener);
    }

    @Override
    public void removeVideoUpdateListener(OnVideoUpdateDurationListener videoUpdateDurationListener) {
        playerListenerSet.remove(videoUpdateDurationListener);
    }


    public static class AlivcEditThread implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("AlivcEdit Thread");
            return thread;
        }
    }

    private static final int ADD_TRANSITION = 1;
    private static final int REVERT_TRANSITION = 2;
    private static final int SAVE_COVER = 3;

    private static class AlivcEditHandler extends Handler {

        private WeakReference<AlivcEditView> reference;

        public AlivcEditHandler(AlivcEditView editView) {
            reference = new WeakReference<>(editView);
        }

        @Override
        public void handleMessage(Message msg) {
            AlivcEditView alivcEditView = reference.get();
            if (alivcEditView == null) {
                return;
            }
            switch (msg.what) {
                case REVERT_TRANSITION:
                    alivcEditView.playingResume();
                    alivcEditView.stopTransitionAnimation();
                    if (sIsDeleteTransitionSource) {
                        sIsDeleteTransitionSource = false;
                        Log.i(TAG, "delete transition source");
                    } else {
                        alivcEditView.clickCancel();
                    }
                    break;
                case ADD_TRANSITION:
                    EffectInfo effectInfo = (EffectInfo) msg.getData().getSerializable("effectInfo");
                    alivcEditView.addTransitionSuccess(effectInfo);
                    break;

                case SAVE_COVER:
                    //循环查询截取封面工作是否结束，结束跳转到下个页面
                    if (alivcEditView.mSnapshop.isSnapshotting()) {
                        sendEmptyMessageDelayed(SAVE_COVER, 500);
                    } else {
                        removeMessages(SAVE_COVER);
                        alivcEditView.mTransitionAnimation.dismiss();
                        alivcEditView.jumpToNextActivity();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 添加转场成功
     *
     * @param effectInfo
     */
    private void addTransitionSuccess(EffectInfo effectInfo) {

        //提前一秒
        long advanceTime = 1000;
        long clipStartTime = TimeUnit.MICROSECONDS.toMillis(mAliyunIEditor.getClipStartTime(effectInfo.clipIndex + 1));

        advanceTime = clipStartTime - advanceTime >= 0 ? clipStartTime - advanceTime : 0;
        mAliyunIEditor.seek(advanceTime, TimeUnit.MILLISECONDS);
        playingResume();
        mWaitForReady = true;
        stopTransitionAnimation();
        Log.d(TAG, "onTransitionPreview: index = " + effectInfo.clipIndex
            + " ,clipStartTime = " + clipStartTime
            + " ,duration = " + mAliyunIEditor.getPlayerController().getDuration()
            + " ,advanceTime = " + advanceTime
        );
    }

    @Nullable
    private TransitionBase getTransitionBase(EffectInfo effectInfo) {
        TransitionBase transition = null;
        long overlapDuration = 1000;//转场时长
        switch (effectInfo.transitionType) {
            case TransitionChooserView.EFFECT_NONE:
                break;
            case TransitionChooserView.EFFECT_RIGHT:
                transition = new TransitionTranslate();
                transition.setOverlapDuration(overlapDuration, TimeUnit.MILLISECONDS);
                ((TransitionTranslate) transition).setDirection(TransitionBase.DIRECTION_RIGHT);
                break;
            case TransitionChooserView.EFFECT_CIRCLE:
                transition = new TransitionCircle();
                transition.setOverlapDuration(overlapDuration, TimeUnit.MILLISECONDS);
                break;
            case TransitionChooserView.EFFECT_FADE:
                transition = new TransitionFade();
                transition.setOverlapDuration(overlapDuration, TimeUnit.MILLISECONDS);
                break;
            case TransitionChooserView.EFFECT_FIVE_STAR:
                transition = new TransitionFiveStar();
                transition.setOverlapDuration(overlapDuration, TimeUnit.MILLISECONDS);
                break;
            case TransitionChooserView.EFFECT_SHUTTER:
                transition = new TransitionShutter();
                transition.setOverlapDuration(overlapDuration, TimeUnit.MILLISECONDS);
                ((TransitionShutter) transition).setLineWidth(0.1f);
                ((TransitionShutter) transition).setOrientation(TransitionBase.ORIENTATION_HORIZONTAL);
                break;
            case TransitionChooserView.EFFECT_UP:
                transition = new TransitionTranslate();
                transition.setOverlapDuration(overlapDuration, TimeUnit.MILLISECONDS);
                ((TransitionTranslate) transition).setDirection(TransitionBase.DIRECTION_UP);
                break;
            case TransitionChooserView.EFFECT_DOWN:
                transition = new TransitionTranslate();
                transition.setOverlapDuration(overlapDuration, TimeUnit.MILLISECONDS);
                ((TransitionTranslate) transition).setDirection(TransitionBase.DIRECTION_DOWN);
                break;
            case TransitionChooserView.EFFECT_LEFT:
                transition = new TransitionTranslate();
                transition.setOverlapDuration(overlapDuration, TimeUnit.MILLISECONDS);
                ((TransitionTranslate) transition).setDirection(TransitionBase.DIRECTION_LEFT);
                break;
            case TransitionChooserView.EFFECT_CUSTOM:
                transition = effectInfo.transitionBase;
                transition.setOverlapDuration(overlapDuration, TimeUnit.MILLISECONDS);
                break;
            default:
                break;
        }
        return transition;
    }

    /**
     * 对于Gop比较大的视频做时间特效时需要先检查是否满足实时，如果不满足实时，需要提前做转码，逻辑如下
     * 转码倒播参数要求：小于1080, 1920，gop小于5，fps小于30
     * 转码生成临时文件的默认参数为：分辨率：小于1080, 1920，gop：5，fps：30，type： ffmpeg。
     * <p>
     * 高清分辨率处理在导入时处理
     *
     * @param type         操作的类型（倒放，反复等）
     * @param times        这里指的是反复的次数
     * @param startTime    反复开始的时间
     * @param duration     反复的时长
     * @param needDuration 是否需要保持原视频长度
     */
    private void checkAndTranscode(final TimeEffectType type, final int times, final long startTime,
        final long duration, final boolean needDuration) {

        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                AliyunClip clip = mAliyunIEditor.getSourcePartManager().getAllClips().get(0);
                final AtomicInteger flag = new AtomicInteger(0);
                if (clip == null) {
                    return null;
                }
                boolean ret = checkInvert(clip.getSource());
                if (!ret) {
                    mAliyunIEditor.saveEffectToLocal();
                    final CountDownLatch countDownLatch = new CountDownLatch(1);

                    CropParam param = new CropParam();
                    param.setGop(5);
                    param.setFrameRate(30);
                    param.setQuality(VideoQuality.SSD);
                    param.setInputPath(clip.getSource());
                    param.setVideoCodec(VideoCodecs.H264_SOFT_OPENH264);
                    param.setCrf(19);
                    param.setOutputPath(clip.getSource() + "_invert_transcode.mp4");
                    Log.i(TAG, "log_editor_edit_transcode : " + param.getOutputPath());
                    int width = 0;
                    int height = 0;
                    int rotate = 0;
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    try {
                        mmr.setDataSource(clip.getSource());
                        rotate = Integer.parseInt(
                            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
                        if (rotate == 90 || rotate == 270) {
                            height = Integer.parseInt(
                                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                            width = Integer.parseInt(
                                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                        } else {
                            width = Integer.parseInt(
                                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                            height = Integer.parseInt(
                                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                        }
                    } catch (Exception e) {
                        width = mVideoParam.getOutputWidth();
                        height = mVideoParam.getOutputHeight();
                    } finally {
                        mmr.release();
                    }
                    param.setOutputWidth(width);
                    param.setOutputHeight(height);
                    mTranscoder.setCropParam(param);
                    mTranscoder.setCropCallback(new CropCallback() {
                        @Override
                        public void onProgress(final int percent) {
                            Log.d(TAG, "percent" + percent);
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    mTransCodeProgress.setProgress(percent);
                                }
                            });
                        }

                        @Override
                        public void onError(int code) {
                            Log.d(TAG, "onError" + code);
                            flag.set(1);
                            countDownLatch.countDown();
                            mIsTranscoding = false;
                        }

                        @Override
                        public void onComplete(long duration) {
                            AliyunISourcePartManager lSourcePartManager = mAliyunIEditor.getSourcePartManager();
                            AliyunClip clip = lSourcePartManager.getMediaPart(0);
                            clip.setSource(clip.getSource() + "_invert_transcode.mp4");
                            lSourcePartManager.updateMediaClip(0, clip);
                            lSourcePartManager.applySourceChange();
                            flag.set(2);
                            countDownLatch.countDown();
                            mIsTranscoding = false;
                        }

                        @Override
                        public void onCancelComplete() {
                            flag.set(3);
                            if (mIsDestroyed) {
                                mTranscoder.dispose();
                            }
                            countDownLatch.countDown();
                            mIsTranscoding = false;
                        }
                    });
                    mIsTranscoding = true;
                    int r = mTranscoder.startCrop();
                    if (r != AliyunErrorCode.ALIVC_COMMON_RETURN_SUCCESS) {
                        return null;
                    }
                    post(new Runnable() {
                        @Override
                        public void run() {
                            mTransCodeTip.setVisibility(View.VISIBLE);
                            BaseChooser bottomView = mViewOperate.getBottomView();
                            if (bottomView != null) {
                                bottomView.setClickable(false);
                            }
                        }
                    });
                    try {
                        countDownLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                return flag;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if (mIsDestroyed) {
                    return;
                }
                mTransCodeTip.setVisibility(View.GONE);
                mAliyunIEditor.stop();
                if (o instanceof AtomicInteger) {
                    if (((AtomicInteger) o).get() == 0 || ((AtomicInteger) o).get() == 2) {
                        if (type == TimeEffectType.TIME_EFFECT_TYPE_INVERT) {
                            mAliyunIEditor.invert();
                        } else if (type == TimeEffectType.TIME_EFFECT_TYPE_REPEAT) {
                            mAliyunIEditor.repeat(times, startTime, duration, needDuration);
                        }

                    }
                }
                //如果转码完成时，本页面被stop，则不进行恢复播放
                //只是把isNeedResume改为true
                if (!mIsStop) {
                    playingResume();
                } else {
                    isNeedResume = true;
                }

                //mAliyunIEditor.play();
                BaseChooser bottomView = mViewOperate.getBottomView();
                if (bottomView != null) {
                    bottomView.setClickable(true);
                }
            }
        } .execute(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onTabChange() {
        Log.d(TAG, "onTabChange: ");
        UIEditorPage page = UIEditorPage.get(mTabGroup.getCheckedIndex());
        Log.d(TAG, "onTabChange: page" + page.name());
        switch (page) {
            case AUDIO_MIX:
                playingPause();
                break;
            case SOUND:
                //音效。
                break;
            case FONT:
            case CAPTION:
            case OVERLAY:
                //case穿透统一处理paster的保存，用于撤销
                mPasterEffectCachetList.clear();
                for (int i = 0; i < mPasterContainer.getChildCount(); i++) {
                    View childAt = mPasterContainer.getChildAt(i);
                    Object tag = childAt.getTag();
                    if (tag == null || !(tag instanceof AbstractPasterUISimpleImpl)) {
                        //如果子pasterView的tag异常
                        continue;
                    }
                    AbstractPasterUISimpleImpl uiSimple = (AbstractPasterUISimpleImpl) tag;
                    if (!isPasterTypeHold(page, uiSimple.getEditorPage())) {
                        //如果paster类型与所打开的编辑页面不一致
                        continue;
                    }
                    PasterRestoreBean restoreBean = new PasterRestoreBean();
                    restoreBean.setFrameAction(uiSimple.getFrameAction());
                    restoreBean.setTempFrameAction(uiSimple.getTempFrameAction());
                    restoreBean.setFrameSelectedPosition(uiSimple.getFrameSelectPosition());
                    EffectBase effect = uiSimple.getEffect();
                    if (effect instanceof EffectCaption) {
                        EffectCaption src = (EffectCaption) effect;
                        EffectCaption copy = new EffectCaption("");
                        src.copy(copy);
                        restoreBean.setEffectBase(copy);
                    } else if (effect instanceof EffectText) {
                        EffectText src = (EffectText) effect;
                        EffectText copy = new EffectText(new Source());
                        src.copy(copy);
                        restoreBean.setEffectBase(copy);
                    } else if (effect instanceof EffectPaster) {
                        EffectPaster src = (EffectPaster) effect;
                        EffectPaster copy = new EffectPaster(new Source());
                        src.copy(copy);
                        restoreBean.setEffectBase(copy);
                    }
                    mPasterEffectCachetList.add(restoreBean);
                }
                break;
            case COVER:
                //暂停播放并隐藏播放按钮。
                playingPause();
                break;
            case PIP:
                break;
            default:
                break;
        }
    }

    /**
     * 贴纸是否相同的超强力判断
     *
     * @param pageOne {@link UIEditorPage}
     * @param page2   {@link UIEditorPage}
     * @return boolean
     */
    private boolean isPasterTypeHold(UIEditorPage pageOne, UIEditorPage page2) {
        //当pageOne为动图时，page2也是动图返回true
        //当pageOne是字幕或者字体，page2也是字幕或者字体时返回true
        return pageOne == UIEditorPage.OVERLAY && page2 == UIEditorPage.OVERLAY
            || pageOne != UIEditorPage.OVERLAY && page2 != UIEditorPage.OVERLAY;
    }

    private List<PasterRestoreBean> mPasterEffectCachetList = new ArrayList<>();

    private void checkAndRemoveEffects() {
        //删除资源时，如果有使用对应的特效也删除
        checkAndRemovePaster();
        checkAndRemoveTransition();
        checkAndRemoveAnimationFilter();
    }

    private void checkAndRemoveAnimationFilter() {
        if (mAliyunIEditor != null) {
            Dispatcher.getInstance().postMsg(new CheckDeleteFilter());
        }
    }

    /**
     * 转场资源被删除，应用的动画结束后不关闭窗口 {@link AlivcEditHandler#handleMessage(Message)}
     */
    private static boolean sIsDeleteTransitionSource = false;

    private void checkAndRemoveTransition() {
        if (mAliyunIEditor != null) {
            TransitionEffectCache transitionEffectCache = mEditorService.getTransitionEffectCache(mAliyunIEditor.getSourcePartManager());

            List<EffectInfo> deleteList = transitionEffectCache.checkTransitionCacheIsDelete();
            if (deleteList.size() == 0) {
                return;
            }
            sIsDeleteTransitionSource = true;
            EffectInfo effectInfo = new EffectInfo();
            effectInfo.type = UIEditorPage.TRANSITION;
            effectInfo.transitionType = TransitionChooserView.EFFECT_CUSTOM;
            effectInfo.mutiEffect = deleteList;
            onEffectChange(effectInfo);
        }
    }

    private void checkAndRemovePaster() {
        int count = mPasterContainer.getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            View pv = mPasterContainer.getChildAt(i);
            AliyunBasePasterController uic = (AliyunBasePasterController) pv.getTag();
            if (uic != null && !uic.isPasterExists()) {
                Log.e(TAG, "removePaster");
                uic.removePaster();
            }
        }
    }

    /**
     * 调用resetThumbLine的时机
     */
    private void resetTimeLineLayout() {
        Log.i(TAG, "resetTimeLineLayout");
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                initThumbLineBar();
            }
        });
    }

    public void playingPause() {
        if (mAliyunIEditor.isPlaying()) {
            mAliyunIEditor.pause();
            if (mThumbLineBar != null) {
                mThumbLineBar.pause();
            }
            switchPlayStateUI(true);
        }
    }

    public void playingResume() {
        if (!mAliyunIEditor.isPlaying()) {
            if (mAliyunIEditor.isPaused()) {
                mAliyunIEditor.resume();
            } else {
                mAliyunIEditor.play();
            }
            if (mThumbLineBar != null) {
                mThumbLineBar.resume();
            }
            switchPlayStateUI(false);
        }
    }

    private PasterUIGifImpl addPaster(AliyunPasterController controller) {
        Log.d(TAG, "add GIF");
        AliyunPasterWithImageView pasterView = (AliyunPasterWithImageView) View.inflate(getContext(),
            R.layout.alivc_editor_view_paster_gif, null);
        final PasterUIGifImpl pasterUIGif = new PasterUIGifImpl(pasterView, controller, mThumbLineBar, mAliyunIEditor);

        ImageView imageView = pasterView.findViewById(R.id.qupai_btn_edit_overlay_animation);
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                pasterUIGif.showAnimationDialog(mUseInvert);
            }
        });
        mPasterContainer.addView(pasterView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return pasterUIGif;
    }

    /**
     * 添加字幕
     *
     * @param controller
     * @return
     */
    private PasterUICaptionImpl addCaption(AliyunPasterController controller) {
        AliyunPasterWithImageView captionView = (AliyunPasterWithImageView) View.inflate(getContext(),
            R.layout.alivc_editor_view_paster_caption, null);
        mPasterContainer.addView(captionView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        Log.d(TAG, "add 字幕");
        return new PasterUICaptionImpl(captionView, controller, mThumbLineBar, mAliyunIEditor);
    }

    /**
     * 添加文字
     *
     * @param controller
     * @param restore
     * @return
     */
    private PasterUITextImpl addSubtitle(AliyunPasterController controller, boolean restore) {
        Log.d(TAG, "add 文字");
        AliyunPasterWithTextView captionView = (AliyunPasterWithTextView) View.inflate(getContext(),
            R.layout.alivc_editor_view_paster_text, null);
        mPasterContainer.addView(captionView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        return new PasterUITextImpl(captionView, controller, mThumbLineBar, mAliyunIEditor, restore);
    }

    @Override
    public void onClick(View view) {
        if (view == mPlayImage && mAliyunIEditor != null) {
            //当在添加特效的时候，关闭该按钮
            if (mUseAnimationFilter) {
                return;
            }
            if (mAliyunIEditor.isPlaying()) {
                playingPause();
            } else {
                playingResume();
                if (mCurrentEditEffect != null && !mCurrentEditEffect.isPasterRemoved()) {
                    mCurrentEditEffect.editTimeCompleted();
                }
            }
        }
    }

    private void clickConfirm() {

        // 确认后变化，各个模块自行实现
        int checkIndex = mTabGroup.getCheckedIndex();
        UIEditorPage page = UIEditorPage.get(checkIndex);
        if (mCurrentEditEffect != null && !mCurrentEditEffect.isPasterRemoved()) {
            mCurrentEditEffect.editTimeCompleted();
        }
        switch (page) {
            case COVER:
                //改变标示，具体截图代码在mEditorCallback.onTextureRender中实现(旧实现方式)
                //                isTakeFrameSelected = true;
                //                hasCaptureCover = true;
                //新实现方式
                Executors.newSingleThreadExecutor().submit(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = getEditor().getCurrentFrame();
                        String path = getEditor().getEditorProject().getProjectDir().getAbsolutePath() + File.separator + "cover.jpeg";
                        BitmapUtil.writeBitmap(path, bitmap, Bitmap.CompressFormat.JPEG, 80);
                        bitmap.recycle();
                        getEditor().updateCover(new Source(path));
                        getEditor().saveEffectToLocal();
                    }
                });
                break;
            case PAINT:
                if (mCanvasController != null) {
                    mCanvasController.confirm();
                    mCanvasController.applyPaintCanvas();
                    mPasterContainer.removeView(mCanvasController.getCanvas());
                }
                break;
            default:
                break;
        }
        //        将转场效果的缓存清空
        if (mTransitionCache != null) {
            mTransitionCache.clear();
        }
        if (mTransitionParamsCache != null) {
            mTransitionParamsCache.clear();
        }
        mViewOperate.hideBottomView();
    }

    /**
     * 编辑态视图点击返回
     */
    private void clickCancel() {
        // 取消后变化，各个模块自行实现
        int checkIndex = mTabGroup.getCheckedIndex();
        UIEditorPage page = UIEditorPage.get(checkIndex);
        if (mCurrentEditEffect != null && !mCurrentEditEffect.isEditCompleted()) {
            mCurrentEditEffect.removePaster();
        }
        switch (page) {
            case COVER:
                isTakeFrameSelected = false;
                mPlayImage.setVisibility(VISIBLE);
                break;
            case AUDIO_MIX:
                playingResume();
                break;
            case PAINT:
                if (mCanvasController == null) {
                    break;
                }
                //清除当前操作
                mCanvasController.cancel();
                mCanvasController.applyPaintCanvas();
                mPasterContainer.removeView(mCanvasController.getCanvas());
                break;
            case SOUND:

                break;
            case TRANSITION:
                //            点击取消时，重新调用updateTransition，使用设置参数之前的值
                if (mTransitionCache != null && mTransitionParamsCache != null && !mTransitionCache.isEmpty() && !mTransitionParamsCache.isEmpty() && mTransitionCache.size() == mTransitionParamsCache.size()) {
                    for (Integer i : mTransitionCache.keySet()) {
                        for (Integer key : mTransitionCache.keySet()) {
                            EffectInfo effectInfo = mTransitionCache.get(key);
                            List<AlivcTransBean> paramList = mTransitionParamsCache.get(key);
                            List<EffectConfig.NodeBean> nodeTree = effectInfo.transitionBase.getNodeTree();
                            if (nodeTree == null || nodeTree.size() == 0) {
                                break;
                            }
                            for (EffectConfig.NodeBean nodeBean : nodeTree) {
                                List<EffectConfig.NodeBean.Params> params = nodeBean.getParams();
                                if (params == null || params.size() == 0) {
                                    continue;
                                }
                                if (params.size() != paramList.size()) {
                                    break;
                                }
                                for (int j = 0; j < params.size(); j++) {
                                    EffectConfig.NodeBean.Params param = params.get(j);
                                    AlivcTransBean alivcTransBean = paramList.get(j);
                                    ValueTypeEnum type = param.getType();
                                    if (type == ValueTypeEnum.INT) {
                                        //                                    重设之前的int值
                                        param.getValue().updateINT(alivcTransBean.getmIntergerValue());
                                    } else if (type == ValueTypeEnum.FLOAT) {
                                        //                                    重设之前的float值
                                        param.getValue().updateFLOAT(alivcTransBean.getmFloatValue());
                                    }
                                }
                            }
                        }
                        //                    调用updateTransition方法，重新设置动画效果
                        mAliyunIEditor.updateTransition(mTransitionCache.get(i).clipIndex, mTransitionCache.get(i).transitionBase);
                    }
                    //                设置完成后清空
                    mTransitionCache.clear();
                    mTransitionParamsCache.clear();
                }
                break;
            case COMPOUND_CAPTION:
                if (mCurrentEditEffect != null && !mCurrentEditEffect.isEditCompleted()) {
                    mCurrentEditEffect.removePaster();
                }
                break;
            case OVERLAY:
                //这里做paster的撤销恢复处理
                if (mCurrentEditEffect != null && !mCurrentEditEffect.isEditCompleted()) {
                    mCurrentEditEffect.removePaster();
                }

                //先remove所有指定类型的paster
                for (int i = 0; i < mPasterContainer.getChildCount(); i++) {
                    View childAt = mPasterContainer.getChildAt(i);
                    Object tag = childAt.getTag();
                    if (tag == null || !(tag instanceof AliyunBasePasterController)) {
                        continue;
                    }
                    AliyunBasePasterController uiSimple = (AliyunBasePasterController) tag;

                    if (isPasterTypeHold(uiSimple.getEditorPage(), page)) {
                        // 1.Controller remove
                        // 2.pasterContainer remove
                        // 3.ThumbLBar remove
                        uiSimple.removePaster();
                        //涉及到集合遍历删除元素的问题（角标前移）
                        i--;
                    }

                }

                //恢复缓存的指定类型paster
                for (PasterRestoreBean restoreBean : mPasterEffectCachetList) {
                    final AliyunPasterController pasterController;


                    EffectBase effectBase = restoreBean.getEffectBase();
                    //获取对应的controller、（判断文件存在，避免用户删除了对应的资源后恢复时crash）
                    if (effectBase instanceof EffectCaption && new File(effectBase.getSource().getPath()).exists()) {
                        EffectCaption effect = (EffectCaption) effectBase;
                        pasterController = mPasterManager.addPasterWithStartTime(effect.getSource(), effect.start,
                            effect.end - effect.start);
                    } else if (effectBase instanceof EffectText) {
                        EffectText effect = (EffectText) effectBase;
                        pasterController = mPasterManager.addSubtitleWithStartTime(effect.text, effect.fontSource,
                            effect.start, effect.end - effect.start);
                    } else if (effectBase instanceof EffectPaster && new File(effectBase.getSource().getPath()).exists()) {
                        EffectPaster effect = (EffectPaster) effectBase;
                        pasterController = mPasterManager.addPasterWithStartTime(effect.getSource(), effect.start,
                            effect.end - effect.start);
                    } else {
                        continue;
                    }
                    pasterController.setEffect(effectBase);
                    //锁定参数（避免被设置effectBase参数被冲掉）
                    pasterController.setRevert(true);
                    if (pasterController.getPasterType() == EffectPaster.PASTER_TYPE_GIF) {
                        mCurrentEditEffect = addPaster(pasterController);
                    }
                    mCurrentEditEffect.showTimeEdit();
                    mCurrentEditEffect.editTimeStart();
                    mCurrentEditEffect.moveToCenter();
                    mCurrentEditEffect.editTimeCompleted();

                    ActionBase frameAction = restoreBean.getFrameAction();
                    ActionBase tempFrameAction = restoreBean.getTempFrameAction();
                    if (frameAction != null && mCurrentEditEffect instanceof AbstractPasterUISimpleImpl) {
                        //恢复贴纸的帧动画
                        frameAction.setTargetId(pasterController.getEffect().getViewId());
                        ((AbstractPasterUISimpleImpl)mCurrentEditEffect).setFrameAction(frameAction);
                        ((AbstractPasterUISimpleImpl)mCurrentEditEffect).setTempFrameAction(tempFrameAction);
                        ((AbstractPasterUISimpleImpl)mCurrentEditEffect).setFrameSelectedPosition(restoreBean.getFrameSelectedPosition());
                        mAliyunIEditor.removeFrameAnimation(frameAction);
                        mAliyunIEditor.addFrameAnimation(frameAction);
                    }

                    pasterController.setRevert(false);
                }

                break;
            default:
                break;
        }

        mViewOperate.hideBottomView();
    }

    /**
     * 点击空白出弹窗消失
     */
    private void hideBottomEditorView() {

        int checkIndex = mTabGroup.getCheckedIndex();
        if (checkIndex == -1) {
            return;
        }
        UIEditorPage page = UIEditorPage.get(checkIndex);

        if (mViewOperate != null) {
            mViewOperate.hideBottomEditorView(page);
        }

    }

    /**
     * 恢复动效滤镜UI（这里主要是编辑页面顶部时间轴的覆盖
     *
     * @param animationFilters
     */
    @Override
    public void onAnimationFilterRestored(final List<TrackEffectFilter> animationFilters) {
        mPasterContainer.post(new Runnable() {
            @Override
            public void run() {
                mAnimationFilterController.setThumbLineBar(mThumbLineBar);
                if (mAnimationFilterController != null) {
                    mAnimationFilterController.restoreAnimationFilters(animationFilters);
                }
            }
        });
    }

    /**
     * 页面缩小时 对应的paster也要缩小
     *
     * @param scaleSize 缩小比率
     */
    public void setPasterDisplayScale(float scaleSize) {
        mPasterManager.setDisplaySize((int) (mPasterContainerPoint.x * scaleSize),
            (int) (mPasterContainerPoint.y * scaleSize));
    }

    private class MyOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        float mPosX;
        float mPosY;
        boolean shouldDrag = true;

        boolean shouldDrag() {
            return shouldDrag;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            Log.d(TAG, "onDoubleTapEvent");
            return super.onDoubleTapEvent(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.d(TAG, "onSingleTapConfirmed" + shouldDrag);
            boolean hasFindController = false;
            if (!shouldDrag) {
                boolean outside = true;
                BaseChooser bottomView = null;
                if (mViewOperate != null) {
                    bottomView = mViewOperate.getBottomView();
                }
                if (bottomView != null) {
                    //编辑界面打开状态
                    if (mPasterUICompoundCaptionImpl != null && bottomView instanceof CaptionChooserPanelView) {
                        long currentPlayPositionInMills = mAliyunIEditor.getPlayerController().getCurrentPlayPosition();
                        //这里返回所有类型的贴图，包含动图和字幕
                        AliyunIPasterController controllerAtPoiont = CaptionManager.findControllerAtPoint(mPasterManager, e, tempPointF, currentPlayPositionInMills);
                        if (controllerAtPoiont instanceof AliyunPasterControllerCompoundCaption) {
                            outside = false;
                            AliyunPasterControllerCompoundCaption currentController = mPasterUICompoundCaptionImpl.getController();
                            playingPause();
                            if (controllerAtPoiont == currentController) {
                                //已是编辑状态，再次点击打开字幕编辑面板
                                if (!mCurrentEditEffect.isEditCompleted()) {
                                    mPasterUICompoundCaptionImpl.updateParams(getIAlivcEditView(), currentController, mPasterManager, mThumbLineBar);
                                    mPasterUICompoundCaptionImpl.showTextEdit(mUseInvert);
                                } else {
                                    mPasterUICompoundCaptionImpl.showCaptionBorderView(mPasterContainer, mSurfaceView);
                                    mPasterUICompoundCaptionImpl.showTimeEdit();
                                }

                            } else {
                                //选中字幕与当先编辑字幕不同，切换字幕边框、进度条
                                if (mCurrentEditEffect != null && !mCurrentEditEffect.isEditCompleted()) {
                                    mCurrentEditEffect.editTimeCompleted();
                                }
                                mPasterUICompoundCaptionImpl.updateParams(getIAlivcEditView(), (AliyunPasterControllerCompoundCaption) controllerAtPoiont, mPasterManager, mThumbLineBar);
                                mPasterUICompoundCaptionImpl.showCaptionBorderView(mPasterContainer, mSurfaceView);
                                mPasterUICompoundCaptionImpl.showTimeEdit();
                            }
                            mCurrentEditEffect = mPasterUICompoundCaptionImpl;
                            mViewOperate.setCaptionTextView(AlivcEditorViewFactory.findCaptionEditorPanelView(getAlivcEditView()));
                            mCurrentEditEffect.editTimeStart();
                            hasFindController = true;

                        }

                    }
                    if (!hasFindController) {
                        //v3.22版本后，只有动图使用这种方式
                        int count = mPasterContainer.getChildCount();
                        for (int i = count - 1; i >= 0; i--) {
                            View pv = mPasterContainer.getChildAt(i);
                            Object tag = pv.getTag();
                            if (tag instanceof AbstractPasterUISimpleImpl) {
                                AbstractPasterUISimpleImpl uic = (AbstractPasterUISimpleImpl) pv.getTag();

                                if (uic != null && bottomView.isHostPaster(uic)) {
                                    if (uic.isVisibleInTime(mAliyunIEditor.getPlayerController().getCurrentStreamPosition())
                                        && uic.contentContains(e.getX(), e.getY())) {
                                        outside = false;
                                        if (mCurrentEditEffect != null && mCurrentEditEffect != uic && !mCurrentEditEffect
                                            .isEditCompleted()) {
                                            mCurrentEditEffect.editTimeCompleted();
                                        }
                                        mCurrentEditEffect = uic;
                                        if (uic.isEditCompleted()) {
                                            playingPause();
                                            uic.editTimeStart();
                                        }
                                        break;
                                    } else {
                                        if (mCurrentEditEffect != uic && uic.isVisibleInTime(
                                            mAliyunIEditor.getPlayerController().getCurrentStreamPosition())) {
                                            uic.editTimeCompleted();
                                        }
                                    }
                                }
                            }


                        }

                    }
                }

                if (outside) {
                    if (mCurrentEditEffect != null && !mCurrentEditEffect.isEditCompleted()) {
                        mCurrentEditEffect.editTimeCompleted();
                    }
                    hideBottomEditorView();
                }
            } else {
                //可拖动状态下，判定该操作在字幕区域内
                playingPause();
                if (mCurrentEditEffect instanceof PasterUICompoundCaptionImpl) {
                    mPasterUICompoundCaptionImpl.updateParams(getIAlivcEditView(), (AliyunPasterControllerCompoundCaption) ((PasterUICompoundCaptionImpl) mCurrentEditEffect).getController(),
                        mPasterManager, mThumbLineBar);
                    mCurrentEditEffect.showTextEdit(mUseInvert);

                } else {
                    mCurrentEditEffect.showTextEdit(mUseInvert);

                }
            }
            return shouldDrag;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return super.onSingleTapUp(e);
        }

        @Override
        public void onShowPress(MotionEvent e) {
            Log.d(TAG, "onShowPress");
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (shouldDrag()) {
                if (mPosX == 0 || mPosY == 0) {
                    mPosX = e1.getX();
                    mPosY = e1.getY();
                }
                float x = e2.getX();
                float y = e2.getY();

                mCurrentEditEffect.moveContent(x - mPosX, y - mPosY);

                mPosX = x;
                mPosY = y;

            } else {

            }

            return shouldDrag;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.d(TAG, "onLongPress");
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return shouldDrag;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            Log.d(TAG, "onDown");
            if (mCurrentEditEffect != null && mCurrentEditEffect.isPasterRemoved()) {
                mCurrentEditEffect = null;
            }

            if (mCurrentEditEffect != null) {
                Log.d(TAG, "mCurrentEditEffect != null");
                shouldDrag = !mCurrentEditEffect.isEditCompleted()
                    && mCurrentEditEffect.contentContains(e.getX(), e.getY())
                    && mCurrentEditEffect.isVisibleInTime(mAliyunIEditor.getPlayerController().getCurrentStreamPosition())
                    && mCurrentEditEffect.canDrag();

            } else {
                shouldDrag = false;
            }

            mPosX = 0;
            mPosY = 0;
            return true;

        }
    }

    StringBuilder mDurationText = new StringBuilder(5);

    private String convertDuration2Text(long duration) {
        mDurationText.delete(0, mDurationText.length());
        float relSec = (float) duration / 1000;// ms -> s
        int min = (int) ((relSec % 3600) / 60);
        int sec = 0;
        sec = (int) (relSec % 60);
        if (min >= 10) {
            mDurationText.append(min);
        } else {
            mDurationText.append("0").append(min);
        }
        mDurationText.append(":");
        if (sec >= 10) {
            mDurationText.append(sec);
        } else {
            mDurationText.append("0").append(sec);
        }
        return mDurationText.toString();
    }

    private void copyAssets() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                EditorCommon.copyAll(getContext(), resCopy);
            }
        });
    }

    public AliyunIEditor getEditor() {
        return this.mAliyunIEditor;
    }

    public void showMessage(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(id);
        builder.setNegativeButton(R.string.alivc_common_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * 转码倒播参数要求：gop小于5，fps小于35 （1080P分辨率要求在进入编辑前处理）
     *
     * @param filePath 视频地址
     * @return true 视频满足倒播要求，不需要转码 otherwise 需要转码
     */
    private boolean checkInvert(String filePath) {
        NativeParser parser = new NativeParser();
        if (parser.checkIfSupportedImage(filePath)) {
            parser.release();
            parser.dispose();
            return true;
        }
        parser.init(filePath);
        boolean gop = parser.getMaxGopSize() <= 5;
        boolean fps = false;
        try {
            fps = Float.parseFloat(parser.getValue(NativeParser.VIDEO_FPS)) <= 35;
        } catch (NumberFormatException e) {
            Log.e(TAG, e.getMessage());
        }
        parser.release();
        parser.dispose();
        return gop && fps;
    }

    /**
     * 设置转场的预览监听
     */
    private TransitionChooserView.OnPreviewListener mOnTransitionPreviewListener = new TransitionChooserView
        .OnPreviewListener() {
        @Override
        public void onPreview(int clipIndex, long leadTime, boolean isStop) {
            //提前一秒
            long advanceTimeInMIlls = 1000;
            long clipStartTimeInMills = TimeUnit.MICROSECONDS.toMillis(mAliyunIEditor.getClipStartTime(clipIndex + 1));
            advanceTimeInMIlls = clipStartTimeInMills - advanceTimeInMIlls >= 0 ? clipStartTimeInMills - advanceTimeInMIlls : 0;
            mAliyunIEditor.seek(advanceTimeInMIlls, TimeUnit.MILLISECONDS);
            playingResume();
            Log.d(TAG, "onTransitionPreview: index = " + clipIndex
                + " ,clipStartTime = " + clipStartTimeInMills
                + " ,duration = " + mAliyunIEditor.getDuration()
                + " ,advanceTime = " + advanceTimeInMIlls
            );
        }
    };

    private OnEffectActionLister mOnEffectActionLister = new OnEffectActionLister() {
        @Override
        public void onCancel() {
            clickCancel();
        }

        @Override
        public void onComplete() {
            clickConfirm();
        }
    };

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventColorFilterSelected(SelectColorFilter selectColorFilter) {
        EffectInfo effectInfo = selectColorFilter.getEffectInfo();
        if (TextUtils.isEmpty(effectInfo.getSource().getPath())) {
            mAliyunIEditor.removeFilter();
        } else {
            TrackEffectFilter lTrackEffectFilter = new TrackEffectFilter.Builder()
                .source(effectInfo.getSource())
                .startTime(0, TimeUnit.MILLISECONDS)
                .duration(0, TimeUnit.MILLISECONDS)
                .resId(effectInfo.id)
                .build();
            mAliyunIEditor.applyFilter(lTrackEffectFilter);
        }

    }
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventLutSelected(LUTEffectBean lutEffectBean) {
        if (lutEffectBean != null){
            if(EditorConstants.EFFECT_FILTER_LOCAL_LUT_CLEAR.equals(lutEffectBean.getSource().getPath())){
                mAliyunIEditor.applyLutFilter(null);
            }else if (EditorConstants.EFFECT_FILTER_LOCAL_LUT_ADD.equals(lutEffectBean.getSource().getPath())){
                Intent intent =new  Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//无类型限制
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                ((Activity) getContext()).startActivityForResult(intent,EditorConstants.EFFECT_FILTER_LOCAL_LUT_REQUEST_CODE );

            }else {
                mAliyunIEditor.applyLutFilter(lutEffectBean);
            }
        }



    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onBrightnessProgressChange(BrightnessProgressMsg msg) {
        List<AliyunClip> allClips = mAliyunIEditor.getSourcePartManager().getAllClips();
        for (AliyunClip clip : allClips) {
            mAliyunIEditor.setVideoAugmentation(clip.getId(), VideoAugmentationType.BRIGHTNESS, msg.getProgress());
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onContrastProgressChange(ContrastProgressMsg msg) {
        List<AliyunClip> allClips = mAliyunIEditor.getSourcePartManager().getAllClips();
        for (AliyunClip clip : allClips) {
            mAliyunIEditor.setVideoAugmentation(clip.getId(), VideoAugmentationType.CONTRAST, msg.getProgress());
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onSaturationProgressChange(SaturationProgressMsg msg) {
        List<AliyunClip> allClips = mAliyunIEditor.getSourcePartManager().getAllClips();
        for (AliyunClip clip : allClips) {
            mAliyunIEditor.setVideoAugmentation(clip.getId(), VideoAugmentationType.SATURATION, msg.getProgress());
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onPipScaleProgressChange(PipScaleMsg msg) {
        long current = mAliyunIEditor.getCurrentPlayPosition();
        AliyunIPipManager pipManager = mAliyunIEditor.getPipManager();
        List<AliyunIPipController> controllers = pipManager.getAllPip();
        if (controllers == null || controllers.size() == 0) {
            return;
        }
        AliyunIPipController controller = controllers.get(controllers.size() - 1);
        controller.getLayoutController().setScale(msg.progress).apply();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onPipVolumeProgressChange(PipVolumeMsg msg) {
        long current = mAliyunIEditor.getCurrentPlayPosition();
        AliyunIPipManager pipManager = mAliyunIEditor.getPipManager();
        List<AliyunIPipController> controllers = pipManager.getAllPip();
        if (controllers == null || controllers.size() == 0) {
            return;
        }
        AliyunIPipController controller = controllers.get(controllers.size() - 1);
        controller.getAudioController().setVolume(msg.progress).apply();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onPipDenoiseProgressChange(PipDenoiseMsg msg) {
        long current = mAliyunIEditor.getCurrentPlayPosition();
        AliyunIPipManager pipManager = mAliyunIEditor.getPipManager();
        List<AliyunIPipController> controllers = pipManager.getAllPip();
        if (controllers == null || controllers.size() == 0) {
            return;
        }
        AliyunIPipController controller = controllers.get(controllers.size() - 1);
        controller.getAudioController().denoise(msg.progress).apply();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onPipAlphaProgressChange(PipAlphaMsg msg) {
        long current = mAliyunIEditor.getCurrentPlayPosition();
        AliyunIPipManager pipManager = mAliyunIEditor.getPipManager();
        List<AliyunIPipController> controllers = pipManager.getAllPip();
        if (controllers == null || controllers.size() == 0) {
            return;
        }
        AliyunIPipController controller = controllers.get(controllers.size() - 1);
        controller.getLayoutController().setAlpha(msg.progress).apply();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onPipBrightnessProgressChange(PipBrighnessMsg msg) {
        long current = mAliyunIEditor.getCurrentPlayPosition();
        AliyunIPipManager pipManager = mAliyunIEditor.getPipManager();
        List<AliyunIPipController> controllers = pipManager.getAllPip();
        if (controllers == null || controllers.size() == 0) {
            return;
        }
        AliyunIPipController controller = controllers.get(controllers.size() - 1);
        AliyunIAugmentationController augmentationController = controller.getAugmentationController();
        augmentationController.setBrightness(msg.progress).apply();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onPipContrastProgressChange(PipContrastMsg msg) {
        long current = mAliyunIEditor.getCurrentPlayPosition();
        AliyunIPipManager pipManager = mAliyunIEditor.getPipManager();
        List<AliyunIPipController> controllers = pipManager.getAllPip();
        if (controllers == null || controllers.size() == 0) {
            return;
        }
        AliyunIPipController controller = controllers.get(controllers.size() - 1);
        AliyunIAugmentationController augmentationController = controller.getAugmentationController();
        augmentationController.setContrast(msg.progress).apply();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onPipSaturationProgressChange(PipSaturationMsg msg) {
        long current = mAliyunIEditor.getCurrentPlayPosition();
        AliyunIPipManager pipManager = mAliyunIEditor.getPipManager();
        List<AliyunIPipController> controllers = pipManager.getAllPip();
        if (controllers == null || controllers.size() == 0) {
            return;
        }
        AliyunIPipController controller = controllers.get(controllers.size() - 1);
        AliyunIAugmentationController augmentationController = controller.getAugmentationController();
        augmentationController.setSaturation(msg.progress).apply();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onPipSharpnessProgressChange(PipSharpnessMsg msg) {
        long current = mAliyunIEditor.getCurrentPlayPosition();
        AliyunIPipManager pipManager = mAliyunIEditor.getPipManager();
        List<AliyunIPipController> controllers = pipManager.getAllPip();
        if (controllers == null || controllers.size() == 0) {
            return;
        }
        AliyunIPipController controller = controllers.get(controllers.size() - 1);
        AliyunIAugmentationController augmentationController = controller.getAugmentationController();
        augmentationController.setSharpness(msg.progress).apply();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onPipVignetteProgressChange(PipVignetteMsg msg) {
        long current = mAliyunIEditor.getCurrentPlayPosition();
        AliyunIPipManager pipManager = mAliyunIEditor.getPipManager();
        List<AliyunIPipController> controllers = pipManager.getAllPip();
        if (controllers == null || controllers.size() == 0) {
            return;
        }
        AliyunIPipController controller = controllers.get(controllers.size() - 1);
        AliyunIAugmentationController augmentationController = controller.getAugmentationController();
        augmentationController.setVignette(msg.progress).apply();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onPipAngleProgressChange(PipAngleMsg msg) {
        long current = mAliyunIEditor.getCurrentPlayPosition();
        AliyunIPipManager pipManager = mAliyunIEditor.getPipManager();
        List<AliyunIPipController> controllers = pipManager.getAllPip();
        if (controllers == null || controllers.size() == 0) {
            return;
        }
        AliyunIPipController controller = controllers.get(controllers.size() - 1);
        controller.getLayoutController().setRotation(msg.progress).apply();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onPipMoveProgressChange(PipMoveMsg msg) {
        long current = mAliyunIEditor.getCurrentPlayPosition();
        AliyunIPipManager pipManager = mAliyunIEditor.getPipManager();
        List<AliyunIPipController> controllers = pipManager.getAllPip();
        if (controllers == null || controllers.size() == 0) {
            return;
        }
        AliyunIPipController controller = controllers.get(controllers.size() - 1);
        controller.getLayoutController().setPosition(msg.progress, msg.progress).apply();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onPipBorderProgressChange(PipBorderMsg msg) {
        long current = mAliyunIEditor.getCurrentPlayPosition();
        AliyunIPipManager pipManager = mAliyunIEditor.getPipManager();
        List<AliyunIPipController> controllers = pipManager.getAllPip();
        if (controllers == null || controllers.size() == 0) {
            return;
        }
        AliyunIPipController controller = controllers.get(controllers.size() - 1);
        controller.setBorderWidth(msg.progress).apply();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onPipCornerProgressChange(PipRadiusMsg msg) {
        long current = mAliyunIEditor.getCurrentPlayPosition();
        AliyunIPipManager pipManager = mAliyunIEditor.getPipManager();
        List<AliyunIPipController> controllers = pipManager.getAllPip();
        if (controllers == null || controllers.size() == 0) {
            return;
        }
        AliyunIPipController controller = controllers.get(controllers.size() - 1);
        controller.setBorderCornerRadius(msg.progress).apply();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onPipEffectChanged(PipEffectMsg msg) {
        AliyunIPipManager pipManager = mAliyunIEditor.getPipManager();
        List<AliyunIPipController> controllers = pipManager.getAllPip();
        if (controllers == null || controllers.size() == 0) {
            return;
        }
        AliyunIPipController controller = controllers.get(controllers.size() - 1);
        controller.getAudioController().setAudioEffect(AudioEffectType.values()[new Random().nextInt(7)], 1.f)
            .apply();
    }

    private ActionTranslate actionTranslate = null;

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onPipFrameAnimationChanged(PipFrameAnimationMsg msg) {
        AliyunIPipManager pipManager = mAliyunIEditor.getPipManager();
        List<AliyunIPipController> controllers = pipManager.getAllPip();
        if (controllers == null || controllers.size() == 0) {
            return;
        }
        AliyunIPipController controller = controllers.get(controllers.size() - 1);
        if (actionTranslate == null) {
            actionTranslate = new ActionTranslate();
            actionTranslate.setFromPointX(-1);
            actionTranslate.setFromPointY(-1);
            actionTranslate.setToPointX(1);
            actionTranslate.setToPointY(1);
            actionTranslate.setStartTime(controller.getTimeLineStartTimeInMillis() * 1000);
            actionTranslate.setDuration(controller.getClipDurationInMillis() * 1000);
            controller.getAnimationController().addFrameAnimation(actionTranslate);
        } else {
            controller.getAnimationController().removeFrameAnimation(actionTranslate);
            actionTranslate = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onAddPip(PipAddMsg msg) {
        playingPause();
        EditorMediaActivity.selectMediaOnResult((Activity) getContext(), REQ_CODE_GET_MEDIA);
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onDeletePip(PipDeleteMsg msg) {
        playingPause();
        AliyunIPipManager pipManager = mAliyunIEditor.getPipManager();
        List<AliyunIPipController> controllers = pipManager.getAllPip();
        if (controllers == null || controllers.size() == 0) {
            return;
        }
        AliyunIPipController controller = controllers.get(controllers.size() - 1);
        pipManager.removePip(controller);
        playingResume();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onSharpProgressChange(SharpProgressMsg msg) {
        List<AliyunClip> allClips = mAliyunIEditor.getSourcePartManager().getAllClips();
        for (AliyunClip clip : allClips) {
            mAliyunIEditor.setVideoAugmentation(clip.getId(), VideoAugmentationType.SHARPNESS, msg.getProgress());
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onVignetteCornerChange(VignetteMsg msg) {
        List<AliyunClip> allClips = mAliyunIEditor.getSourcePartManager().getAllClips();
        for (AliyunClip clip : allClips) {
            mAliyunIEditor.setVideoAugmentation(clip.getId(), VideoAugmentationType.VIGNETTE, msg.getProgress());
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onVideoEqResetMsg(VideoEqResetMsg msg) {
        List<AliyunClip> allClips = mAliyunIEditor.getSourcePartManager().getAllClips();
        for (AliyunClip clip : allClips) {
            mAliyunIEditor.resetVideoAugmentation(clip.getId(), msg.getType());
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onVideoEqResetAllMsg(VideoEqResetAllMsg msg) {
        List<AliyunClip> allClips = mAliyunIEditor.getSourcePartManager().getAllClips();
        for (AliyunClip clip : allClips) {
            mAliyunIEditor.resetVideoAugmentation(clip.getId(), VideoAugmentationType.BRIGHTNESS);
            mAliyunIEditor.resetVideoAugmentation(clip.getId(), VideoAugmentationType.CONTRAST);
            mAliyunIEditor.resetVideoAugmentation(clip.getId(), VideoAugmentationType.SATURATION);
            mAliyunIEditor.resetVideoAugmentation(clip.getId(), VideoAugmentationType.SHARPNESS);
            mAliyunIEditor.resetVideoAugmentation(clip.getId(), VideoAugmentationType.VIGNETTE);
        }
    }

    /**
     * 长按时需要恢复播放
     *
     * @param filter
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventAnimationFilterLongClick(LongClickAnimationFilter filter) {
        if (!mUseAnimationFilter) {
            mUseAnimationFilter = true;
        }
        if (mCanAddAnimation) {
            playingResume();
        } else {
            playingPause();
        }

    }

    /**
     * 长按抬起手指需要暂停播放
     *
     * @param filter
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventAnimationFilterClickUp(LongClickUpAnimationFilter filter) {
        if (mUseAnimationFilter) {
            mUseAnimationFilter = false;
        }
        if (mAliyunIEditor.isPlaying()) {
            playingPause();

        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventFilterTabClick(FilterTabClick ft) {
        //切换到特效的tab需要暂停播放，切换到滤镜的tab需要恢复播放
        if (mAliyunIEditor != null) {
            switch (ft.getPosition()) {
                case FilterTabClick.POSITION_ANIMATION_FILTER:
                    if (mAliyunIEditor.isPlaying()) {
                        playingPause();
                    }
                    break;
                case FilterTabClick.POSITION_COLOR_FILTER:
                    if (!mAliyunIEditor.isPlaying()) {
                        playingResume();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private EditorCallBack mEditorCallback = new EditorCallBack() {
        @Override
        public void onEnd(int state) {

            post(new Runnable() {
                @Override
                public void run() {

                    if (!mUseAnimationFilter) {
                        //当正在添加滤镜的时候，不允许重新播放
                        mAliyunIEditor.replay();
                        mThumbLineBar.restart();
                    } else {
                        mCanAddAnimation = false;
                        switchPlayStateUI(true);

                    }

                }
            });
        }

        @Override
        public void onError(final int errorCode) {
            Log.e(TAG, "play error " + errorCode);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    switch (errorCode) {
                        case AliyunErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_WRONG_STATE:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_PROCESS_FAILED:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_NO_FREE_DISK_SPACE:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_CREATE_DECODE_GOP_TASK_FAILED:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_AUDIO_STREAM_DECODER_INIT_FAILED:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_VIDEO_STREAM_DECODER_INIT_FAILED:

                        case AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_SPS_PPS_NULL:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_CREATE_H264_PARAM_SET_FAILED:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_CREATE_HEVC_PARAM_SET_FAILED:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_CREATE_DECODER_FAILED:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_ERROR_STATE:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_ERROR_INPUT:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_ERROR_NO_BUFFER_AVAILABLE:

                        case AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_ERROR_DECODE_SPS:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_AUDIO_DECODER_CREATE_DECODER_FAILED:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_AUDIO_DECODER_ERROR_STATE:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_AUDIO_DECODER_ERROR_INPUT:
                        case AliyunErrorCode.ALIVC_FRAMEWORK_AUDIO_DECODER_ERROR_NO_BUFFER_AVAILABLE:
                            showToast = FixedToastUtils.show(getContext(), errorCode + "");
                            ((Activity) getContext()).finish();
                            break;
                        case AliyunErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_CACHE_DATA_SIZE_OVERFLOW:
                            showToast = FixedToastUtils.show(getContext(), errorCode + "");
                            mThumbLineBar.restart();
                            mAliyunIEditor.play();
                            break;
                        case AliyunErrorCode.ALIVC_SVIDEO_ERROR_MEDIA_NOT_SUPPORTED_AUDIO:
                            showToast = FixedToastUtils.show(getContext(),
                                getResources().getString(R.string.alivc_editor_error_tip_not_supported_audio));
                            ((Activity) getContext()).finish();
                            break;
                        case AliyunErrorCode.ALIVC_SVIDEO_ERROR_MEDIA_NOT_SUPPORTED_VIDEO:
                            showToast = FixedToastUtils.show(getContext(),
                                getResources().getString(R.string.alivc_editor_error_tip_not_supported_video));
                            ((Activity) getContext()).finish();
                            break;
                        case AliyunErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_STREAM_NOT_EXISTS:
                        case AliyunErrorCode.ALIVC_SVIDEO_ERROR_MEDIA_NOT_SUPPORTED_PIXEL_FORMAT:
                            showToast = FixedToastUtils.show(getContext(),
                                getResources().getString(R.string.alivc_editor_error_tip_not_supported_pixel_format));
                            ((Activity) getContext()).finish();
                            break;
                        case AliyunErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_ERROR_INTERRUPT:
                            showToast = FixedToastUtils.show(getContext(),
                                getResources().getString(R.string.alivc_editor_edit_tip_decoder_error_interrupt));
                            ((Activity) getContext()).finish();
                            break;
                        default:
                            showToast = FixedToastUtils.show(getContext(),
                                getResources().getString(R.string.alivc_editor_error_tip_play_video_error));
                            ((Activity) getContext()).finish();
                            break;
                    }
                }
            });

        }

        @Override
        public int onCustomRender(int srcTextureID, int width, int height) {
            return srcTextureID;
        }

        @Override
        public int onTextureRender(int srcTextureID, int width, int height) {
            if (isTakeFrame) {
                if (mSnapshop == null) {
                    mSnapshop = new AlivcSnapshot();
                }
                mSnapshop.useTextureIDGetFrame(srcTextureID, width, height, new File(PATH_THUMBNAIL));
                isTakeFrame = false;
            }
            return 0;
        }

        @Override
        public void onPlayProgress(final long currentPlayTime, final long currentStreamPlayTime) {
            post(new Runnable() {
                @Override
                public void run() {
                    long currentPlayTime = mAliyunIEditor.getPlayerController().getCurrentPlayPosition();
                    if (mUseAnimationFilter
                        && mAliyunIEditor.getPlayerController().getDuration() - currentPlayTime < USE_ANIMATION_REMAIN_TIME / 1000) {
                        mCanAddAnimation = false;
                    } else {
                        mCanAddAnimation = true;
                    }
                }
            });

        }

        private int c = 0;

        @Override
        public void onDataReady() {
            post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onDataReady received");
                    if (mWaitForReady && c > 0) {
                        Log.d(TAG, "onDataReady resume");
                        mWaitForReady = false;
                        mAliyunIEditor.resume();
                    }
                    c++;
                }
            });

        }
    };
    public static final int USE_ANIMATION_REMAIN_TIME = 300;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KEYCODE_VOLUME_DOWN:
                mVolume -= 5;
                if (mVolume < 0) {
                    mVolume = 0;
                }
                mAliyunIEditor.setVolume(mVolume);
                return true;
            case KEYCODE_VOLUME_UP:
                mVolume += 5;
                if (mVolume > 100) {
                    mVolume = 100;
                }
                mAliyunIEditor.setVolume(mVolume);
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    private boolean isNeedResume = true;

    public void onStart() {
        mIsStop = false;
        if (mViewStack != null) {
            mViewStack.setVisibleStatus(true);
        }
    }

    public void onResume() {
        mTvRight.setEnabled(true);
        if (isNeedResume) {
            playingResume();
        }
        //当删除使用的MV的时候，会发生崩溃，所以在次判断一下mv是否被删除
        if (mLastMVEffect != null) {
            String path = EditorCommon.getMVPath(mLastMVEffect.list, mVideoParam.getOutputWidth(),
                mVideoParam.getOutputHeight());

            if (!TextUtils.isEmpty(path) && !new File(path).exists()) {
                applyMVEffect(new EffectInfo());
            }
        }
        checkAndRemoveEffects();
    }

    public void onPause() {
        isNeedResume = mAliyunIEditor.isPlaying();
        playingPause();
        mAliyunIEditor.saveEffectToLocal();
    }

    public void onStop() {

        if (mTvRight != null) {
            mTvRight.setEnabled(true);
        }
        mIsStop = true;
        if (mViewStack != null) {
            mViewStack.setVisibleStatus(false);
        }
        if (showToast != null) {
            showToast.cancel();
            showToast = null;
        }
    }

    public void onDestroy() {
        mIsDestroyed = true;
        Dispatcher.getInstance().unRegister(this);
        if (mAliyunIEditor != null) {
            mAliyunIEditor.onDestroy();
        }

        if (mAnimationFilterController != null) {
            mAnimationFilterController.destroyController();
        }

        if (mThumbLineBar != null) {
            mThumbLineBar.stop();
        }

        if (mThumbnailFetcher != null) {
            mThumbnailFetcher.release();
        }

        if (mCanvasController != null) {
            mCanvasController.release();
        }

        if (mTranscoder != null) {
            if (mIsTranscoding) {
                mTranscoder.cancel();
            } else {
                mTranscoder.dispose();
            }
        }

        if (mViewOperate != null) {
            mViewOperate.setAnimatorListener(null);
            mViewOperate = null;
        }

        if (animatorX != null) {
            animatorX.cancel();
            animatorX.addUpdateListener(null);
            animatorX.addListener(null);
            animatorX = null;
        }

        if (mWatermarkBitmap != null && !mWatermarkBitmap.isRecycled()) {
            mWatermarkBitmap.recycle();
            mWatermarkBitmap = null;
        }

        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    private void addPip(MediaInfo mediaInfo) {
        long current = mAliyunIEditor.getCurrentPlayPosition();
        Random random = new Random();
        int r = random.nextInt(255);
        int g = random.nextInt(255);
        int b = random.nextInt(255);
        int color = Color.rgb(r, g, b);
        AliyunIPipManager pipManager = mAliyunIEditor.getPipManager();
        Log.e("AliyunPip", "newPicInPic source = " + mediaInfo.filePath);
        AliyunIPipController pipController = pipManager.createNewPip(mediaInfo.filePath);
        pipController.setTimelineStartTime(current, TimeUnit.MICROSECONDS)
            .setClipStartTime(0, TimeUnit.MICROSECONDS)
            .setBorderColor(color)
            .setHorizontalFlip(mHorizontalFlip)
            .apply();
        pipController.getLayoutController()
            .setPosition(0.5f, 0.5f)
            .setScale(30 / 100.f)
            .apply();
        pipController.getAudioController().setVolume(100).apply();
        playingResume();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        mViewStack.onActivityResult(requestCode, resultCode, data);
        if (PublishActivity.REQUEST_CODE == requestCode && resultCode == Activity.RESULT_OK) {
            String cover = data.getStringExtra(PublishActivity.KEY_RESULT_COVER);
            if (!StringUtils.isEmpty(cover)) {
                getEditor().updateCover(new Source(cover));
            }
        }
        if (requestCode == REQ_CODE_GET_MEDIA) {
            ArrayList<MediaInfo> mediaInfos = data.getParcelableArrayListExtra(AlivcEditInputParam.INTENT_KEY_MEDIA_INFO);
            if (mediaInfos == null || mediaInfos.isEmpty()) {
                return;
            }
            addPip(mediaInfos.get(0));
        } else if (requestCode == EditorConstants.EFFECT_FILTER_LOCAL_LUT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String path = FileUtils.getFilePathByUri(getContext(), uri);
            LUTEffectBean bean = new LUTEffectBean();
            Source source = new Source(path);
            bean.setSource(source);
            bean.setIntensity(1.f);
            onEventLutSelected(bean);
        }
    }

    public boolean onBackPressed() {
        if (mIsTranscoding) {
            //转码过程中无法操作
            showToast = FixedToastUtils.show(getContext(),
                getResources().getString(R.string.alivc_editor_edit_tip_transcode_no_operate));
            return true;
        }

        if (mViewOperate != null) {
            if (mViewOperate.isCaptionEditPanelShow()) {
                return true;
            }
            boolean isShow = mViewOperate.isBottomViewShow();
            // 直接隐藏
            if (isShow) {
                if (mViewOperate != null) {
                    mViewOperate.getBottomView().onBackPressed();
                    hideBottomEditorView();
                }
            }
            return isShow;
        } else {
            return false;
        }
    }

    private Uri mUri;
    private boolean hasTailAnimation = false;

    public void setParam(AliyunVideoParam mVideoParam, Uri mUri, boolean hasTailAnimation, boolean hasWaterMark, boolean horizontalFlip) {
        this.hasTailAnimation = hasTailAnimation;
        this.mUri = mUri;
        this.mVideoParam = mVideoParam;
        this.hasWaterMark = hasWaterMark;
        this.mHorizontalFlip = horizontalFlip;
        initEditor();

    }

    public void setReplaceMusic(boolean replaceMusic) {
        isReplaceMusic = replaceMusic;
    }

    private AliyunVideoParam mVideoParam;

    /**
     * 播放时间、显示时间同步接口
     */
    public interface PlayerListener {
        /**
         * 获取当前的播放时间（-->缩略图条位置同步）
         *
         * @return 前的播放时间
         */
        long getCurrDuration();

        /**
         * 获取视频总时间
         *
         * @return 视频总时间
         */
        long getDuration();

        /**
         * 更新时间（-->显示时间同步）
         *
         * @param duration 更新时间
         */
        void updateDuration(long duration);
    }

    /**
     * 根据配置跳转到下一个activity
     */
    private void jumpToNextActivity() {
        if (mOnFinishListener != null) {
            AlivcEditOutputParam outputParam = new AlivcEditOutputParam();
            outputParam.setConfigPath(mUri.getPath());
            outputParam.setOutputVideoHeight(mAliyunIEditor.getVideoHeight());
            outputParam.setOutputVideoWidth(mAliyunIEditor.getVideoWidth());
            outputParam.setVideoRatio(((float) mPasterContainerPoint.x) / mPasterContainerPoint.y);
            outputParam.setVideoParam(mVideoParam);
            AliyunEditorProject project = getEditor().getEditorProject();
            if (project != null && project.getCover() != null && !StringUtils.isEmpty(project.getCover().getPath())) {
                String path = project.getCover().getPath();
                outputParam.setThumbnailPath(path);
            } else {
                outputParam.setThumbnailPath(PATH_THUMBNAIL);
            }
            mOnFinishListener.onComplete(outputParam);
        }
    }

    /**
     * 编辑完成事件监听
     */
    public interface OnFinishListener {
        void onComplete(AlivcEditOutputParam outputParam);
    }

    private OnFinishListener mOnFinishListener;

    public OnFinishListener getOnFinishListener() {
        return mOnFinishListener;
    }

    public void setmOnFinishListener(OnFinishListener finishListener) {
        this.mOnFinishListener = finishListener;
    }

    private IAlivcEditView getIAlivcEditView() {
        return this;
    }
}

