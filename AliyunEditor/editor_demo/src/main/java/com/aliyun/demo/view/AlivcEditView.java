package com.aliyun.demo.view;

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
import android.graphics.Point;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
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

import com.aliyun.common.utils.StorageUtils;
import com.aliyun.crop.AliyunCropCreator;
import com.aliyun.crop.struct.CropParam;
import com.aliyun.crop.supply.AliyunICrop;
import com.aliyun.crop.supply.CropCallback;
import com.aliyun.demo.editor.AbstractPasterUISimpleImpl;
import com.aliyun.demo.editor.PasterUICaptionImpl;
import com.aliyun.demo.editor.PasterUIGifImpl;
import com.aliyun.demo.editor.PasterUITextImpl;
import com.aliyun.demo.editor.R;
import com.aliyun.demo.editor.thumblinebar.OverlayThumbLineBar;
import com.aliyun.demo.editor.thumblinebar.ThumbLineBar;
import com.aliyun.demo.editor.thumblinebar.ThumbLineConfig;
import com.aliyun.demo.editor.thumblinebar.ThumbLineOverlay;
import com.aliyun.demo.effects.control.BaseChooser;
import com.aliyun.demo.effects.control.EditorService;
import com.aliyun.demo.effects.control.EffectInfo;
import com.aliyun.demo.effects.control.OnEffectActionLister;
import com.aliyun.demo.effects.control.OnEffectChangeListener;
import com.aliyun.demo.effects.control.OnTabChangeListener;
import com.aliyun.demo.effects.control.TabGroup;
import com.aliyun.demo.effects.control.TabViewStackBinding;
import com.aliyun.demo.effects.control.UIEditorPage;
import com.aliyun.demo.effects.control.ViewStack;
import com.aliyun.demo.effects.filter.AnimationFilterController;
import com.aliyun.demo.effects.transition.TransitionChooserView;
import com.aliyun.demo.msg.Dispatcher;
import com.aliyun.demo.msg.body.FilterTabClick;
import com.aliyun.demo.msg.body.LongClickAnimationFilter;
import com.aliyun.demo.msg.body.LongClickUpAnimationFilter;
import com.aliyun.demo.msg.body.SelectColorFilter;
import com.aliyun.demo.publish.PublishActivity;
import com.aliyun.demo.util.Common;
import com.aliyun.svideo.base.utils.DensityUtil;
import com.aliyun.demo.util.FixedToastUtils;
import com.aliyun.demo.util.ThreadUtil;
import com.aliyun.demo.viewoperate.ViewOperator;
import com.aliyun.demo.widget.AliyunPasterWithImageView;
import com.aliyun.demo.widget.AliyunPasterWithTextView;
import com.aliyun.demo.widget.CustomProgressDialog;
import com.aliyun.editor.EditorCallBack;
import com.aliyun.editor.EffectType;
import com.aliyun.editor.TimeEffectType;
import com.aliyun.querrorcode.AliyunEditorErrorCode;
import com.aliyun.querrorcode.AliyunErrorCode;
import com.aliyun.qupai.editor.AliyunICanvasController;
import com.aliyun.qupai.editor.AliyunIEditor;
import com.aliyun.qupai.editor.AliyunPasterController;
import com.aliyun.qupai.editor.AliyunPasterManager;
import com.aliyun.qupai.editor.OnAnimationFilterRestored;
import com.aliyun.qupai.editor.OnPasterRestored;
import com.aliyun.qupai.editor.impl.AliyunEditorFactory;
import com.aliyun.svideo.base.UIConfigManager;
import com.aliyun.svideo.sdk.external.struct.AliyunIClipConstructor;
import com.aliyun.svideo.sdk.external.struct.common.AliyunClip;
import com.aliyun.svideo.sdk.external.struct.common.AliyunVideoParam;
import com.aliyun.svideo.sdk.external.struct.common.VideoDisplayMode;
import com.aliyun.svideo.sdk.external.struct.effect.EffectBase;
import com.aliyun.svideo.sdk.external.struct.effect.EffectBean;
import com.aliyun.svideo.sdk.external.struct.effect.EffectCaption;
import com.aliyun.svideo.sdk.external.struct.effect.EffectFilter;
import com.aliyun.svideo.sdk.external.struct.effect.EffectPaster;
import com.aliyun.svideo.sdk.external.struct.effect.EffectText;
import com.aliyun.svideo.sdk.external.struct.effect.TransitionBase;
import com.aliyun.svideo.sdk.external.struct.effect.TransitionCircle;
import com.aliyun.svideo.sdk.external.struct.effect.TransitionFade;
import com.aliyun.svideo.sdk.external.struct.effect.TransitionFiveStar;
import com.aliyun.svideo.sdk.external.struct.effect.TransitionShutter;
import com.aliyun.svideo.sdk.external.struct.effect.TransitionTranslate;
import com.aliyun.svideo.sdk.external.struct.encoder.VideoCodecs;
import com.aliyun.svideo.sdk.external.thumbnail.AliyunIThumbnailFetcher;
import com.aliyun.svideo.sdk.external.thumbnail.AliyunThumbnailFetcherFactory;
import com.duanqu.transcode.NativeParser;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static android.view.KeyEvent.KEYCODE_VOLUME_DOWN;
import static android.view.KeyEvent.KEYCODE_VOLUME_UP;

/**
 * @author zsy_18 data:2018/8/24
 */
public class AlivcEditView extends RelativeLayout
    implements View.OnClickListener, OnEffectChangeListener, OnTabChangeListener,
    OnAnimationFilterRestored {
    private static final String TAG = AlivcEditView.class.getName();
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
    private SurfaceView mSurfaceView;
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
    public  FrameLayout mPasterContainer;
    private FrameLayout mGlSurfaceContainer;
    private ImageView mIvLeft;
    private ImageView mIvRight;
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
     * 时间特效在缩略图上的浮层
     * 用于删除时间浮层
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

    private AbstractPasterUISimpleImpl mCurrentEditEffect;
    /**
     * 音量
     */
    private int mVolume = 50;
    /**
     * 控制UI变动
     */
    private ViewOperator mViewOperate;
    private Point mPasterContainerPoint;
    private EffectBean lastMusicBean;
    //用户滑动thumbLineBar时的监听器
    private ThumbLineBar.OnBarSeekListener mBarSeekListener;
    //播放时间、显示时间、缩略图位置同步接口
    private PlayerListener mPlayerListener;
    private EffectInfo mLastMVEffect;
    private ObjectAnimator animatorX;
    private Toast showToast;

    /**
     * 编辑模块Handler处理类
     */
    private AlivcEditHandler alivcEditHandler;

    /*
     *  判断是编辑模块进入还是通过社区模块的编辑功能进入
     *  svideo: 短视频
     *  community: 社区
     */
    private String entrance;

    /**
     * 线程池
     */
    private ExecutorService executorService;

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
        WindowManager windowManager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getSize(point);
        mScreenWidth = point.x;
        LayoutInflater.from(getContext()).inflate(R.layout.aliyun_svideo_activity_editor, this, true);
        initView();
        initListView();
        add2Control();
        initThreadHandler();
        copyAssets();


    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        resCopy = (FrameLayout)findViewById(R.id.copy_res_tip);
        mTransCodeTip = (FrameLayout)findViewById(R.id.transcode_tip);
        mTransCodeProgress = (ProgressBar)findViewById(R.id.transcode_progress);
        mBarLinear = (LinearLayout)findViewById(R.id.bar_linear);
        mBarLinear.bringToFront();
        mActionBar = (RelativeLayout)findViewById(R.id.action_bar);
        mActionBar.setBackgroundDrawable(null);
        mIvLeft = (ImageView)findViewById(R.id.iv_left);
        mIvRight = (ImageView)findViewById(R.id.iv_right);
        mIvLeft.setImageResource(R.mipmap.aliyun_svideo_back);
        //uiConfig中的属性
        UIConfigManager.setImageResourceConfig(mIvRight,R.attr.finishImage,R.mipmap.aliyun_svideo_complete_red);
        mIvLeft.setVisibility(View.VISIBLE);
        mIvRight.setVisibility(View.VISIBLE);
        mIvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Activity)getContext()).finish();
            }
        });
        mTvCurrTime = (TextView)findViewById(R.id.tv_curr_duration);

        mGlSurfaceContainer = (FrameLayout)findViewById(R.id.glsurface_view);
        mSurfaceView = (SurfaceView)findViewById(R.id.play_view);
        mBottomLinear = findViewById(R.id.edit_bottom_tab);
        setBottomTabResource();
        mPasterContainer = (FrameLayout)findViewById(R.id.pasterView);

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
            findViewById(R.id.tab_effect_mv),
            findViewById(R.id.tab_effect_filter),
            findViewById(R.id.tab_effect_time),
            findViewById(R.id.tab_effect_transition),
            findViewById(R.id.tab_paint)
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
            R.attr.mvImage,
            R.attr.effectImage,
            R.attr.timeImage,
            R.attr.translationImage,
            R.attr.paintImage
        };
        int[] defaultResourceIds = {
            R.mipmap.aliyun_svideo_filter,
            R.mipmap.aliyun_svideo_music,
            R.mipmap.aliyun_svideo_overlay,
            R.mipmap.aliyun_svideo_caption,
            R.mipmap.aliyun_svideo_mv,
            R.mipmap.alivc_svideo_effect,
            R.mipmap.aliyun_svideo_time,
            R.mipmap.aliyun_svideo_transition,
            R.mipmap.aliyun_svideo_paint
        };
        UIConfigManager.setImageResourceConfig(textViews,index,attrs,defaultResourceIds);
    }

    public OverlayThumbLineBar getThumbLineBar() {
        return mThumbLineBar;
    }

    private void initGlSurfaceView() {
        if (mVideoParam == null) {
            return;
        }
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)mGlSurfaceContainer.getLayoutParams();
        FrameLayout.LayoutParams surfaceLayout = (FrameLayout.LayoutParams)mSurfaceView.getLayoutParams();
        int rotation = mAliyunIEditor.getRotation();
        int outputWidth = mVideoParam.getOutputWidth();
        int outputHeight = mVideoParam.getOutputHeight();
        if ((rotation == 90 || rotation == 270)) {
            int temp = outputWidth;
            outputWidth = outputHeight;
            outputHeight = temp;
        }

        float percent;
        if (outputWidth >= outputHeight) {
            percent = (float)outputWidth / outputHeight;
        } else {
            percent = (float)outputHeight / outputWidth;
        }
        /*
          指定surfaceView的宽高比是有必要的，这样可以避免某些非标分辨率下造成显示比例不对的问题
         */
        surfaceLayout.width = mScreenWidth;
        surfaceLayout.height = Math.round((float)outputHeight * mScreenWidth / outputWidth);
        mPasterContainerPoint = new Point(surfaceLayout.width, surfaceLayout.height);
        ViewGroup.MarginLayoutParams marginParams = null;
        if (layoutParams instanceof MarginLayoutParams) {
            marginParams = (ViewGroup.MarginLayoutParams)surfaceLayout;
        } else {
            marginParams = new MarginLayoutParams(surfaceLayout);
        }
        if (percent < 1.5 || (rotation == 90 || rotation == 270)) {
            marginParams.setMargins(0,
                getContext().getResources().getDimensionPixelSize(R.dimen.alivc_svideo_title_height), 0, 0);
        } else {
            if (outputWidth > outputHeight) {
                marginParams.setMargins(0,
                    getContext().getResources().getDimensionPixelSize(R.dimen.alivc_svideo_title_height) * 2, 0, 0);
            }
        }
        mGlSurfaceContainer.setLayoutParams(layoutParams);
        mPasterContainer.setLayoutParams(marginParams);
        mSurfaceView.setLayoutParams(marginParams);
    }

    private void initListView() {
        mViewOperate = new ViewOperator(this, mActionBar, mSurfaceView, mBottomLinear, mPasterContainer, mPlayImage);
        mViewOperate.setAnimatorListener(new ViewOperator.AnimatorListener() {
            @Override
            public void onAnimationEnd() {
                UIEditorPage index = UIEditorPage.get(mTabGroup.getCheckedIndex());
                switch (index) {
                    case PAINT:
                        //2018/8/30 添加涂鸦画布
                        if (mCanvasController == null) {
                            int width = mPasterContainer.getLayoutParams().width;
                            int height = mPasterContainer.getLayoutParams().height;
                            mCanvasController = mAliyunIEditor.obtainCanvasController(getContext(),
                                width, height);
                        }
                        mCanvasController.removeCanvas();
                        addPaint(mCanvasController);
                        break;
                    default:
                        break;
                }
            }
        });
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
        mTabGroup.addView(findViewById(R.id.tab_effect_mv));
        mTabGroup.addView(findViewById(R.id.tab_effect_filter));
        mTabGroup.addView(findViewById(R.id.tab_effect_time));
        mTabGroup.addView(findViewById(R.id.tab_effect_transition));
        mTabGroup.addView(findViewById(R.id.tab_paint));

    }

    private void add2Control() {
        TabViewStackBinding tabViewStackBinding = new TabViewStackBinding();
        tabViewStackBinding.setViewStack(mViewStack);
        mTabGroup.setOnCheckedChangeListener(tabViewStackBinding);
        mTabGroup.setOnTabChangeListener(this);
    }

    private void initEditor() {
        mAliyunIEditor = AliyunEditorFactory.creatAliyunEditor(mUri, mEditorCallback);
        initGlSurfaceView();
        {//该代码块中的操作必须在AliyunIEditor.init之前调用，否则会出现动图、动效滤镜的UI恢复回调不执行，开发者将无法恢复动图、动效滤镜UI
            mPasterManager = mAliyunIEditor.createPasterManager();
            FrameLayout.LayoutParams surfaceLayout = (FrameLayout.LayoutParams)mSurfaceView.getLayoutParams();
            /*
              指定显示区域大小后必须调用mPasterManager.setDisplaySize，否则将无法添加和恢复一些需要提前获知区域大小的资源，如字幕，动图等
              如果开发者的布局使用了wrapContent或者matchParent之类的布局，务必获取到view的真实宽高之后在调用
             */
            try {
                mPasterManager.setDisplaySize(surfaceLayout.width, surfaceLayout.height);
            } catch (Exception e) {
                showToast = FixedToastUtils.show(getContext(), e.getMessage());
                ((Activity) getContext()).finish();
            }
            mPasterManager.setOnPasterRestoreListener(mOnPasterRestoreListener);
            mAnimationFilterController = new AnimationFilterController(getContext().getApplicationContext(),
                mAliyunIEditor);
            mAliyunIEditor.setAnimationRestoredListener(AlivcEditView.this);
        }

        mTranscoder = AliyunCropCreator.createCropInstance(getContext());
        VideoDisplayMode mode = mVideoParam.getScaleMode();
        int ret = mAliyunIEditor.init(mSurfaceView, getContext().getApplicationContext());
        mAliyunIEditor.setDisplayMode(mode);
        mAliyunIEditor.setVolume(mVolume);
        mAliyunIEditor.setFillBackgroundColor(Color.BLACK);
        if (ret != AliyunErrorCode.OK) {
            showToast = FixedToastUtils.show(getContext(),
                getResources().getString(R.string.aliyun_svideo_editor_init_failed));
            return;
        }
        mEditorService.addTabEffect(UIEditorPage.MV, mAliyunIEditor.getMVLastApplyId());
        mEditorService.addTabEffect(UIEditorPage.FILTER_EFFECT, mAliyunIEditor.getFilterLastApplyId());
        mEditorService.addTabEffect(UIEditorPage.AUDIO_MIX, mAliyunIEditor.getMusicLastApplyId());
        mEditorService.setPaint(mAliyunIEditor.getPaintLastApply());

        mIvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                v.setEnabled(false);

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
                //                if(ret != AliyunErrorCode.OK) {
                //                    Log.e(AliyunTag.TAG, "Compose error, error code "+ret);
                //                    v.setEnabled(true);//compose error
                //                }

                //后台合成如下：如果要像Demo默认的这样，在其他页面合成，请参考下面这种方式
                mAliyunIEditor.saveEffectToLocal();
                final AliyunIThumbnailFetcher fetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher();
                fetcher.fromConfigJson(mUri.getPath());
                fetcher.setParameters(mAliyunIEditor.getVideoWidth(), mAliyunIEditor.getVideoHeight(),
                    AliyunIThumbnailFetcher.CropMode.Mediate, VideoDisplayMode.FILL, 1);
                fetcher.requestThumbnailImage(new long[] {0}, new AliyunIThumbnailFetcher.OnThumbnailCompletion() {

                    @Override
                    public void onThumbnailReady(Bitmap bitmap, long l) {
                        String path = getContext().getExternalFilesDir(null) + "thumbnail.jpeg";
                        try {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(path));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Intent intent = new Intent(getContext(), PublishActivity.class);
                        intent.putExtra(PublishActivity.KEY_PARAM_THUMBNAIL, path);
                        intent.putExtra(PublishActivity.KEY_PARAM_CONFIG, mUri.getPath());
                        intent.putExtra(PublishActivity.KEY_PARAM_ENTRANCE, entrance);
                        intent.putExtra(PublishActivity.KEY_PARAM_VIDEO_RATIO,((float)mPasterContainerPoint.x)/mPasterContainerPoint.y);//传入视频比列
                        getContext().startActivity(intent);

                        fetcher.release();
                    }

                    @Override
                    public void onError(int errorCode) {
                        fetcher.release();
                    }
                });
            }
        });

        mPlayerListener = new PlayerListener() {

            @Override
            public long getCurrDuration() {
                return mAliyunIEditor.getCurrentStreamPosition();
            }

            @Override
            public long getDuration() {
                long streamDuration = mAliyunIEditor.getStreamDuration();
                Log.d(TAG, "getDuration: " + streamDuration);
                return streamDuration;
            }

            @Override
            public void updateDuration(long duration) {
                mTvCurrTime.setText(convertDuration2Text(duration));
            }
        };

        mViewStack.setPlayerListener(mPlayerListener);
        //配置缩略图滑动条
        initThumbLineBar();
        //非编辑态隐藏
        mThumbLineBar.hide();
        File mWatermarkFile = new File(StorageUtils.getCacheDirectory(getContext()) + "/AliyunEditorDemo/tail/logo.png");
        if (mWatermarkFile.exists()) {
            if (mWatermarkBitmap == null || mWatermarkBitmap.isRecycled()) {
                mWatermarkBitmap = BitmapFactory.decodeFile(
                    StorageUtils.getCacheDirectory(getContext()) + "/AliyunEditorDemo/tail/logo.png");
            }
            mSurfaceView.post(new Runnable() {
                @Override
                public void run() {
                    int outputWidth = mVideoParam.getOutputWidth();
                    int outputHeight = mVideoParam.getOutputHeight();
                    int mWatermarkBitmapWidth = DensityUtil.dip2px(getContext(), 30);
                    int mWatermarkBitmapHeight = DensityUtil.dip2px(getContext(), 30);
                    if (mWatermarkBitmap == null && !mWatermarkBitmap.isRecycled()) {
                        mWatermarkBitmapWidth = mWatermarkBitmap.getWidth();
                        mWatermarkBitmapHeight = mWatermarkBitmap.getHeight();
                    }
                    float posY = 0;
                    float percent = (float)outputHeight / outputWidth;
                    if (percent > 1.5) {
                        posY = 0f
                            + (float)(mWatermarkBitmapHeight / 2 + getContext().getResources().getDimensionPixelSize(
                            R.dimen.alivc_svideo_title_height)) / 1.5f / mSurfaceView.getHeight();
                    } else {
                        posY = 0f + (float)mWatermarkBitmapHeight / 1.5f / mSurfaceView.getHeight() / 2;
                    }
                    /**
                     * 水印例子 水印的大小为 ：水印图片的宽高和显示区域的宽高比，注意保持图片的比例，不然显示不完全
                     * 水印的位置为 ：以水印图片中心点为基准，显示区域宽高的比例为偏移量，0,0为左上角，1,1为右下角
                     *
                     */
                    mAliyunIEditor.applyWaterMark(
                        StorageUtils.getCacheDirectory(getContext()) + "/AliyunEditorDemo/tail/logo.png",
                        (float)mWatermarkBitmapWidth * 0.5f * 0.8f / mSurfaceView.getWidth(),
                        (float)mWatermarkBitmapHeight * 0.5f * 0.8f / mSurfaceView.getHeight(),
                        (float)mWatermarkBitmapWidth / 1.5f / mSurfaceView.getWidth() / 2,
                        posY);

                    //旋转水印
                    //ActionRotate actionRotate = new ActionRotate();
                    //actionRotate.setStartTime(0);
                    //actionRotate.setTargetId(id);
                    //actionRotate.setDuration(10 * 1000 * 1000);
                    //actionRotate.setRepeat(true);
                    //actionRotate.setDurationPerCircle(3 * 1000 * 1000);
                    //mAliyunIEditor.addFrameAnimation(actionRotate);

                    /* //图片水印
                    EffectPicture effectPicture = new EffectPicture("/sdcard/1.png");
                    effectPicture.x = 0.5f;
                    effectPicture.y = 0.5f;
                    effectPicture.width = 0.5f;
                    effectPicture.height = 0.5f;
                    effectPicture.start = 0;
                    effectPicture.end = 10 * 1000 * 1000;
                    mAliyunIEditor.addImage(effectPicture);

                    ActionRotate actionRotateImg = new ActionRotate();
                    actionRotateImg.setStartTime(0);
                    actionRotateImg.setTargetId(effectPicture.getViewId());
                    actionRotateImg.setDuration(10 * 1000 * 1000);
                    actionRotateImg.setRepeat(true);
                    actionRotateImg.setDurationPerCircle(3 * 1000 * 1000);
                    mAliyunIEditor.addFrameAnimation(actionRotateImg);*/
                    if (hasTailAnimation) {
                        //片尾水印
                        mAliyunIEditor.addTailWaterMark(
                            StorageUtils.getCacheDirectory(getContext()) + "/AliyunEditorDemo/tail/logo.png",
                            (float)mWatermarkBitmapWidth / mSurfaceView.getWidth(),
                            (float)mWatermarkBitmapHeight / mSurfaceView.getHeight(), 0.5f, 0.5f, 2000 * 1000);
                    }

                }
            });
        }

        mAliyunIEditor.play();



    }

    /**
     * 配置新的缩略条
     */
    private void initThumbLineBar() {
        //获取每张缩略图的尺寸
        int thumbnailSize = getResources().getDimensionPixelOffset(R.dimen.aliyun_svideo_square_thumbnail_size);
        Point thumbnailPoint = new Point(thumbnailSize, thumbnailSize);

        //缩略图获取
        if (mThumbnailFetcher == null){
            mThumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher();
            mThumbnailFetcher.fromConfigJson(mUri.getPath());
        }else if (mThumbnailFetcher.getTotalDuration() != mAliyunIEditor.getStreamDuration()/1000) {
            //时长改变的时候才去修改缩略图
            Log.i(TAG, "initThumbLineBar: reset thumbLine");
            mThumbnailFetcher.release();
            mThumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher();
            mThumbnailFetcher.fromConfigJson(mUri.getPath());
        }

        //设置缩略条配置文件
        ThumbLineConfig thumbLineConfig = new ThumbLineConfig.Builder()
            .thumbnailFetcher(mThumbnailFetcher)
            .screenWidth(mScreenWidth)
            .thumbPoint(thumbnailPoint)
            .thumbnailCount(10).build();

        if (mThumbLineBar == null) {
            mThumbLineBar = findViewById(R.id.simplethumblinebar);

            mBarSeekListener = new ThumbLineBar.OnBarSeekListener() {

                @Override
                public void onThumbLineBarSeek(long duration) {
                    mAliyunIEditor.seek(duration);
                    if (mThumbLineBar != null) {
                        mThumbLineBar.pause();
                    }
                    switchPlayStateUI(true);
                    if (mCurrentEditEffect != null && !mCurrentEditEffect.isEditCompleted()) {
                        if (!mCurrentEditEffect.isVisibleInTime(duration)) {
                            //隐藏
                            mCurrentEditEffect.mPasterView.setVisibility(View.GONE);
                        } else {
                            //显示
                            mCurrentEditEffect.mPasterView.setVisibility(View.VISIBLE);
                        }
                    }
                    if (mUseInvert){
                        //当seek到最后时，不允许添加特效
                        if (duration<=USE_ANIMATION_REMAIN_TIME) {
                            mCanAddAnimation = false;
                        }else {
                            mCanAddAnimation = true;
                        }
                    }else {
                        //当seek到最后时，不允许添加特效
                        if (mAliyunIEditor.getDuration() - duration <= USE_ANIMATION_REMAIN_TIME){
                            mCanAddAnimation = false;
                        }else {
                            mCanAddAnimation = true;
                        }
                    }

               
                }

                @Override
                public void onThumbLineBarSeekFinish(long duration) {
                    mAliyunIEditor.seek(duration);
                    if (mThumbLineBar != null) {
                        mThumbLineBar.pause();
                    }
                    switchPlayStateUI(true);
                    if (mUseInvert){
                        //当seek到最后时，不允许添加特效
                        if (duration<=USE_ANIMATION_REMAIN_TIME) {
                            mCanAddAnimation = false;
                        }else {
                            mCanAddAnimation = true;
                        }
                    }else {
                        //当seek到最后时，不允许添加特效
                        if (mAliyunIEditor.getDuration() - duration >= USE_ANIMATION_REMAIN_TIME){
                            mCanAddAnimation = true;
                        }else {
                            mCanAddAnimation = false;
                        }
                    }
                }
            };

            //Overlay相关View
            mThumbLineOverlayView = new ThumbLineOverlay.ThumbLineOverlayView() {
                View rootView = LayoutInflater.from(getContext()).inflate(
                    R.layout.aliyun_svideo_layout_timeline_overlay, null);
                View headView = rootView.findViewById(R.id.head_view);
                View tailView = rootView.findViewById(R.id.tail_view);
                View middleView = rootView.findViewById(R.id.middle_view);

                @Override
                public ViewGroup getContainer() {
                    return (ViewGroup)rootView;
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

    }

    /**
     * 更改播放状态的图标和文字 播放时,文字内容显示为: 暂停播放, 图标使暂停图标, mipmap/aliyun_svideo_pause 暂停时,文字内容显示为: 播放全篇, 图标使用播放图标,
     * mipmap/aliyun_svideo_play
     *
     * @param changeState, 需要显示的状态,  true: 播放全篇, false: 暂停播放
     */
    public void switchPlayStateUI(boolean changeState) {
        if (changeState) {
            mPlayImage.setText(getResources().getString(R.string.alivc_svideo_play_film));
            UIConfigManager.setImageResourceConfig(mPlayImage,0,R.attr.playImage,R.mipmap.aliyun_svideo_play);
        } else {
            mPlayImage.setText(getResources().getString(R.string.alivc_svideo_pause_film));
            UIConfigManager.setImageResourceConfig(mPlayImage,0,R.attr.pauseImage,R.mipmap.aliyun_svideo_pause);
        }
    }

    private final OnPasterRestored mOnPasterRestoreListener = new OnPasterRestored() {

        @Override
        public void onPasterRestored(final List<AliyunPasterController> controllers) {

            Log.d(TAG, "onPasterRestored: " + controllers.size());

            mPasterContainer.post(new Runnable() {//之所以要放在这里面，是因为下面的操作中有UI相关的，需要保证布局完成后执行，才能保证UI更新的正确性
                @Override
                public void run() {

                    if (mThumbLineBar != null && mThumbLineBar.getChildCount() != 0) {
                        //这里做合成（时间和转场特效会清空paster特效）恢复 针对缩略图的覆盖效果
                        mThumbLineBar.removeOverlayByPages(
                            UIEditorPage.CAPTION,
                            UIEditorPage.FONT,
                            UIEditorPage.OVERLAY
                        );
                    }

                    if (mPasterContainer != null) {
                        mPasterContainer.removeAllViews();
                    }
                    final List<AbstractPasterUISimpleImpl> aps = new ArrayList<>();
                    for (AliyunPasterController c : controllers) {
                        if (!c.isPasterExists()) {
                            continue;
                        }
                        if (c.getPasterStartTime() >= mAliyunIEditor.getStreamDuration()) {
                            //恢复时覆盖超出缩略图,丢弃
                            continue;
                        }
                        c.setOnlyApplyUI(true);
                        if (c.getPasterType() == EffectPaster.PASTER_TYPE_GIF) {
                            mCurrentEditEffect = addPaster(c);
                        } else if (c.getPasterType() == EffectPaster.PASTER_TYPE_TEXT) {
                            mCurrentEditEffect = addSubtitle(c, true);
                        } else if (c.getPasterType() == EffectPaster.PASTER_TYPE_CAPTION) {
                            mCurrentEditEffect = addCaption(c);
                        }

                        mCurrentEditEffect.showTimeEdit();
                        mCurrentEditEffect.getPasterView().setVisibility(View.INVISIBLE);
                        aps.add(mCurrentEditEffect);
                        mCurrentEditEffect.moveToCenter();
                        mCurrentEditEffect.hideOverlayView();

                    }

                    for (AbstractPasterUISimpleImpl pui : aps) {
                        pui.editTimeCompleted();
                        pui.getController().setOnlyApplyUI(false);
                    }

                    //要保证涂鸦永远在动图的上方，则需要每次添加动图时都把已经渲染的涂鸦remove掉，添加完动图后，再重新把涂鸦加上去
                    if (mCanvasController != null && mCanvasController.hasCanvasPath()) {
                        mCanvasController.removeCanvas();
                        mCanvasController.resetPaintCanvas();
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
        effect.setPath(effectInfo.getPath());
        UIEditorPage type = effectInfo.type;

        Log.d(TAG, "effect path " + effectInfo.getPath());
        switch (type) {
            case AUDIO_MIX:
                if (!effectInfo.isAudioMixBar) {
                    //重制mv和混音的音效
                    mAliyunIEditor.resetEffect(EffectType.EFFECT_TYPE_MIX);
                    mAliyunIEditor.resetEffect(EffectType.EFFECT_TYPE_MV_AUDIO);
                    if (lastMusicBean != null) {
                        mAliyunIEditor.removeMusic(lastMusicBean);
                    }
                    lastMusicBean = new EffectBean();
                    lastMusicBean.setId(effectInfo.id);
                    lastMusicBean.setPath(effectInfo.getPath());

                    if (lastMusicBean.getPath() != null) {
                        lastMusicBean.setStartTime(effectInfo.startTime * 1000);//单位是us所以要x1000
                        lastMusicBean.setDuration(effectInfo.endTime == 0 ? Integer.MAX_VALUE
                            : (effectInfo.endTime - effectInfo.startTime) * 1000);//单位是us所以要x1000
                        lastMusicBean.setStreamStartTime(effectInfo.streamStartTime * 1000);
                        lastMusicBean.setStreamDuration(
                            (effectInfo.streamEndTime - effectInfo.streamStartTime) * 1000);//单位是us所以要x1000
                        effectInfo.mixId = mAliyunIEditor.applyMusic(lastMusicBean);
                    } else {
                        //恢复mv声音
                        if (mLastMVEffect != null) {
                            applyMVEffect(mLastMVEffect);
                        }
                    }
                } else {
                    effectInfo.mixId = mAliyunIEditor.getMusicLastApplyId();
                }
                mAliyunIEditor.applyMusicMixWeight(effectInfo.mixId, effectInfo.musicWeight);
                // 确定重新开始播放
                playingResume();
                break;
            case FILTER_EFFECT:
                if (effect.getPath().contains("Vertigo")) {
                    EffectFilter filter = new EffectFilter(effect.getPath());
                    mAliyunIEditor.addAnimationFilter(filter);
                } else {
                    mAliyunIEditor.applyFilter(effect);
                }
                break;
            case MV:
                //保存最后一次应用的MV，用于音乐选择无的时候恢复MV的声音
                mLastMVEffect = effectInfo;
                applyMVEffect(effectInfo);

                break;
            case CAPTION:
                mAliyunPasterController = mPasterManager.addPaster(effectInfo.getPath());
                if (mAliyunPasterController != null) {
                    mAliyunPasterController.setPasterStartTime(mAliyunIEditor.getCurrentStreamPosition());
                    PasterUICaptionImpl cui = addCaption(mAliyunPasterController);
                    if (mCurrentEditEffect != null && !mCurrentEditEffect.isEditCompleted()) {
                        //如果有正在编辑的paster，之前的remove
                        mCurrentEditEffect.removePaster();
                    }
                    playingPause();
                    mCurrentEditEffect = cui;
                    mCurrentEditEffect.showTimeEdit();
                } else {
                    showToast = FixedToastUtils.show(getContext(), "添加字幕失败");
                }
                break;
            case OVERLAY:
                mAliyunPasterController = mPasterManager.addPaster(effectInfo.getPath());
                if (mAliyunPasterController != null) {
                    //add success
                    mAliyunPasterController.setPasterStartTime(mAliyunIEditor.getCurrentStreamPosition());
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
                    showToast = FixedToastUtils.show(getContext(), "添加动图失败");
                }

                break;
            case FONT:
                mAliyunPasterController = mPasterManager.addSubtitle(null, effectInfo.fontPath + "/font.ttf");
                if (mAliyunPasterController != null) {
                    mAliyunPasterController.setPasterStartTime(mAliyunIEditor.getCurrentStreamPosition());
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
                    showToast = FixedToastUtils.show(getContext(), "添加文字失败");
                }
                //                mCurrentEditEffect.setImageView((ImageView) findViewById(R.id.test_image));

                break;
            case TIME:
                if (effectInfo.startTime < 0) {
                    effectInfo.startTime = mAliyunIEditor.getCurrentStreamPosition();
                }
                if (mIsTranscoding){
                    showToast = FixedToastUtils.show(getContext(), getResources().getString(R.string.alivc_svideo_tip_transcode_no_operate));
                    return;
                }
                applyTimeEffect(effectInfo);
                break;
            case TRANSITION:
                setTransition(effectInfo);

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
        effect.setPath(effectInfo.getPath());
        UIEditorPage type = effectInfo.type;
        if (mCurrentEditEffect != null && !mCurrentEditEffect.isPasterRemoved()) {
            mCurrentEditEffect.editTimeCompleted();
        }

        String path = null;
        if (effectInfo.list != null) {
            path = Common.getMVPath(effectInfo.list, mVideoParam.getOutputWidth(),
                mVideoParam.getOutputHeight());
        }
        effect.setPath(path);
        if (path != null && new File(path).exists()) {
            mAliyunIEditor.resetEffect(EffectType.EFFECT_TYPE_MIX);
            Log.d(TAG, "editor resetEffect end");
            mAliyunIEditor.applyMV(effect);
        } else {
            mAliyunIEditor.resetEffect(EffectType.EFFECT_TYPE_MV);
            if (lastMusicBean!=null){
                mAliyunIEditor.applyMusic(lastMusicBean);
            }
        }
        //重新播放，倒播重播流时间轴需要设置到最后
        if (mUseInvert) {
            mAliyunIEditor.seek(mAliyunIEditor.getStreamDuration());
        } else {
            mAliyunIEditor.seek(0);
        }
        mAliyunIEditor.resume();
        if (mThumbLineBar != null) {
            mThumbLineBar.resume();
        }
        switchPlayStateUI(false);
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

                mTimeEffectOverlay = mThumbLineBar.addOverlay(effectInfo.startTime, 1000 * 1000, mThumbLineOverlayView,
                    0, false, UIEditorPage.TIME);
                //mAliyunIEditor.stop();
                //playingPause();
                mAliyunIEditor.stop();
                mAliyunIEditor.rate(effectInfo.timeParam, effectInfo.startTime / 1000, 1000, false);
                playingResume();
            } else {
                mTimeEffectOverlay = mThumbLineBar.addOverlay(0, 1000000000L, mThumbLineOverlayView, 0, false,
                    UIEditorPage.TIME);
                //playingPause();
                mAliyunIEditor.stop();
                mAliyunIEditor.rate(effectInfo.timeParam, 0, 1000000000L, false);
                playingResume();

            }
        } else if (effectInfo.timeEffectType.equals(TimeEffectType.TIME_EFFECT_TYPE_INVERT)) {

            mUseInvert = true;
            mTimeEffectOverlay = mThumbLineBar.addOverlay(0, 1000000000L, mThumbLineOverlayView, 0, false,
                UIEditorPage.TIME);
            //mAliyunIEditor.stop();
            //playingPause();
            checkAndTranscode(TimeEffectType.TIME_EFFECT_TYPE_INVERT, 0, 0, 0, false);
        } else if (effectInfo.timeEffectType.equals(TimeEffectType.TIME_EFFECT_TYPE_REPEAT)) {
            mTimeEffectOverlay = mThumbLineBar.addOverlay(effectInfo.startTime, 1000 * 1000, mThumbLineOverlayView, 0,
                false, UIEditorPage.TIME);
            //mAliyunIEditor.stop();
            //playingPause();
            checkAndTranscode(TimeEffectType.TIME_EFFECT_TYPE_REPEAT, 3, effectInfo.startTime / 1000, 1000, false);
        }
        if (mTimeEffectOverlay != null) {
            mTimeEffectOverlay.switchState(ThumbLineOverlay.STATE_FIX);
        }
    }

    private boolean mIsTransitioning = false;
    private CustomProgressDialog mTransitionAnimation;

    private void startTransitionAnimation(){
        mTransitionAnimation.show();
        mIsTransitioning = true;
    }

    private void stopTransitionAnimation(){
        mTransitionAnimation.dismiss();
        mIsTransitioning = false;
    }


    private void setTransition(final EffectInfo effectInfo) {

        if (mTransitionAnimation == null){
            //转场animation
            mTransitionAnimation = new CustomProgressDialog(getContext(),mPasterContainer.getHeight());
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
                    bundle.putSerializable("effectInfo",effectInfo);
                    Message message = new Message();
                    message.what = ADD_TRANSITION;
                    message.setData(bundle);
                    alivcEditHandler.sendMessage(message);
                    resetTimeLineLayout();
                }
            });
        } else if (effectInfo.mutiEffect.size() != 0){
            //撤销转场特效
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    mAliyunIEditor.saveEffectToLocal();
                    Map<Integer,TransitionBase> hashMap= new HashMap<>();
                    for (EffectInfo info : effectInfo.mutiEffect) {
                        TransitionBase transitionBase = getTransitionBase(info);
                        hashMap.put(info.clipIndex,transitionBase);
                    }
                    mAliyunIEditor.setTransition(hashMap);

                    alivcEditHandler.sendEmptyMessage(REVERT_TRANSITION);
                    resetTimeLineLayout();

                }
            });

        }else {
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

    private static class AlivcEditHandler extends Handler{

        private WeakReference<AlivcEditView> reference;

        public AlivcEditHandler(AlivcEditView editView) {
            reference = new WeakReference<>(editView); }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REVERT_TRANSITION:
                    reference.get().playingResume();
                    reference.get().stopTransitionAnimation();
                    reference.get().clickCancel();
                    break;
                case ADD_TRANSITION:
                    EffectInfo effectInfo = (EffectInfo) msg.getData().getSerializable("effectInfo");
                    reference.get().addTransitionSuccess(effectInfo);
                    break;
                    default:
                        break;
            }
        }
    }

    /**
     * 添加转场成功
     * @param effectInfo
     */
    private void addTransitionSuccess(EffectInfo effectInfo){
        long clipStartTime = mAliyunIEditor.getClipStartTime(effectInfo.clipIndex + 1);
        mAliyunIEditor.seek(clipStartTime);
                playingResume();
                mWaitForReady = true;
                stopTransitionAnimation();
        Log.d(TAG, "onTransitionPreview: index = " + effectInfo.clipIndex
                + " ,clipStartTime = " + clipStartTime
                + " ,duration = " + mAliyunIEditor.getDuration());
    }

    @Nullable
    private TransitionBase getTransitionBase(EffectInfo effectInfo) {
        TransitionBase transition = null;
        long overlapDuration = 1000 * 1000;//转场时长
        switch (effectInfo.transitionType) {
            case TransitionChooserView.EFFECT_NONE:
                break;
            case TransitionChooserView.EFFECT_RIGHT:
                transition = new TransitionTranslate();
                transition.setOverlapDuration(overlapDuration);
                ((TransitionTranslate)transition).setDirection(TransitionBase.DIRECTION_RIGHT);
                break;
            case TransitionChooserView.EFFECT_CIRCLE:
                transition = new TransitionCircle();
                transition.setOverlapDuration(overlapDuration);
                break;
            case TransitionChooserView.EFFECT_FADE:
                transition = new TransitionFade();
                transition.setOverlapDuration(overlapDuration);
                break;
            case TransitionChooserView.EFFECT_FIVE_STAR:
                transition = new TransitionFiveStar();
                transition.setOverlapDuration(overlapDuration);
                break;
            case TransitionChooserView.EFFECT_SHUTTER:
                transition = new TransitionShutter();
                transition.setOverlapDuration(overlapDuration);
                ((TransitionShutter)transition).setLineWidth(0.1f);
                ((TransitionShutter)transition).setOrientation(TransitionBase.ORIENTATION_HORIZONTAL);
                break;
            case TransitionChooserView.EFFECT_UP:
                transition = new TransitionTranslate();
                transition.setOverlapDuration(overlapDuration);
                ((TransitionTranslate)transition).setDirection(TransitionBase.DIRECTION_UP);
                break;
            case TransitionChooserView.EFFECT_DOWN:
                transition = new TransitionTranslate();
                transition.setOverlapDuration(overlapDuration);
                ((TransitionTranslate)transition).setDirection(TransitionBase.DIRECTION_DOWN);
                break;
            case TransitionChooserView.EFFECT_LEFT:
                transition = new TransitionTranslate();
                transition.setOverlapDuration(overlapDuration);
                ((TransitionTranslate)transition).setDirection(TransitionBase.DIRECTION_LEFT);
                break;
            default:
                break;
        }
        return transition;
    }

    /**
     * 对于Gop比较大的视频做时间特效时需要先检查是否满足实时，如果不满足实时，需要提前做转码，逻辑如下
     *
     * @param type
     * @param times
     * @param startTime
     * @param duration
     * @param needDuration
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
                    param.setGop(1);
                    param.setVideoBitrate(8000);//8mbps
                    param.setInputPath(clip.getSource());
                    param.setVideoCodec(VideoCodecs.H264_SOFT_OPENH264);
                    param.setOutputPath(clip.getSource() + "_invert_transcode");
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(clip.getSource());
                    int width = 0;
                    int height = 0;
                    int rotate = 0;
                    try {
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
                    }
                    mmr.release();
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
                            AliyunIClipConstructor clipConstructor = mAliyunIEditor.getSourcePartManager();
                            AliyunClip clip = clipConstructor.getMediaPart(0);
                            clip.setSource(clip.getSource() + "_invert_transcode");
                            clipConstructor.updateMediaClip(0, clip);
                            mAliyunIEditor.applySourceChange();
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
                    if (r != AliyunErrorCode.OK) {
                        return null;
                    }
                    post(new Runnable() {
                        @Override
                        public void run() {
                            mTransCodeTip.setVisibility(View.VISIBLE);
                            BaseChooser bottomView = mViewOperate.getBottomView();
                            if (bottomView!=null){
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
                    if (((AtomicInteger)o).get() == 0 || ((AtomicInteger)o).get() == 2) {
                        if (type == TimeEffectType.TIME_EFFECT_TYPE_INVERT) {
                            mAliyunIEditor.invert();
                        } else if (type == TimeEffectType.TIME_EFFECT_TYPE_REPEAT) {
                            mAliyunIEditor.repeat(times, startTime, duration, needDuration);
                        }

                    }
                }
                //如果转码完成时，本页面被stop，则不进行恢复播放
                //只是把isNeedResume改为true
                if (!mIsStop){
                    playingResume();
                }else {
                    isNeedResume = true;
                }

                //mAliyunIEditor.play();
                BaseChooser bottomView = mViewOperate.getBottomView();
                if (bottomView!=null){
                    bottomView.setClickable(true);
                }
            }
        }.execute(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onTabChange() {
        Log.d(TAG, "onTabChange: ");
        UIEditorPage page = UIEditorPage.get(mTabGroup.getCheckedIndex());
        switch (page) {
            case AUDIO_MIX:
                playingPause();
                break;
            case FONT:
            case CAPTION:
            case OVERLAY:
                //case穿透统一处理paster的保存，用于撤销
                mPasterEffecCachetList.clear();
                for (int i = 0; i < mPasterContainer.getChildCount(); i++) {
                    View childAt = mPasterContainer.getChildAt(i);
                    Object tag = childAt.getTag();
                    if (tag == null || !(tag instanceof AbstractPasterUISimpleImpl)){
                        //如果子pasterView的tag异常
                        continue;
                    }
                    AbstractPasterUISimpleImpl uiSimple = (AbstractPasterUISimpleImpl) tag;
                    if (!isPasterTypeHold(page, uiSimple.getEditorPage())) {
                        //如果paster类型与所打开的编辑页面不一致
                        continue;
                    }
                    EffectBase effect = uiSimple.getController().getEffect();
                    if (effect instanceof EffectCaption) {
                        EffectCaption src = (EffectCaption) effect;
                        EffectCaption copy = new EffectCaption("");
                        src.copy(copy);
                        mPasterEffecCachetList.add(copy);
                    } else if (effect instanceof EffectText) {
                        EffectText src = (EffectText) effect;
                        EffectText copy = new EffectText("");
                        src.copy(copy);
                        mPasterEffecCachetList.add(copy);
                    } else if (effect instanceof EffectPaster) {
                        EffectPaster src = (EffectPaster) effect;
                        EffectPaster copy = new EffectPaster("");
                        src.copy(copy);
                        mPasterEffecCachetList.add(copy);
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 贴纸是否相同的超强力判断
     * @param pageOne {@link UIEditorPage}
     * @param page2 {@link UIEditorPage}
     * @return boolean
     */
    private boolean isPasterTypeHold(UIEditorPage pageOne, UIEditorPage page2) {
        //当pageOne为动图时，page2也是动图返回true
        //当pageOne是字幕或者字体，page2也是字幕或者字体时返回true
        return pageOne == UIEditorPage.OVERLAY && page2 == UIEditorPage.OVERLAY
            || pageOne != UIEditorPage.OVERLAY && page2 != UIEditorPage.OVERLAY;
    }

    private List<EffectBase> mPasterEffecCachetList = new ArrayList<>();

    private void checkAndRemovePaster() {
        int count = mPasterContainer.getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            View pv = mPasterContainer.getChildAt(i);
            AbstractPasterUISimpleImpl uic = (AbstractPasterUISimpleImpl)pv.getTag();
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
        mThumbLineBar.post(new Runnable() {
            @Override
            public void run() {
            initThumbLineBar();
            }
        });
    }

    protected void playingPause() {
        if (mAliyunIEditor.isPlaying()) {
            mAliyunIEditor.pause();
            if (mThumbLineBar != null) {
                mThumbLineBar.pause();
            }
            switchPlayStateUI(true);
        }
    }

    private void playingResume() {
        if (!mAliyunIEditor.isPlaying()) {
            mAliyunIEditor.play();
            mAliyunIEditor.resume();
            if (mThumbLineBar != null) {
                mThumbLineBar.resume();
            }
            switchPlayStateUI(false);
        }
    }

    private PasterUIGifImpl addPaster(AliyunPasterController controller) {
        Log.d(TAG, "add GIF");
        AliyunPasterWithImageView pasterView = (AliyunPasterWithImageView)View.inflate(getContext(),
            R.layout.aliyun_svideo_qupai_paster_gif, null);

        mPasterContainer.addView(pasterView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        return new PasterUIGifImpl(pasterView, controller, mThumbLineBar);
    }

    /**
     * 添加字幕
     *
     * @param controller
     * @return
     */
    private PasterUICaptionImpl addCaption(AliyunPasterController controller) {
        AliyunPasterWithImageView captionView = (AliyunPasterWithImageView)View.inflate(getContext(),
            R.layout.aliyun_svideo_qupai_paster_caption, null);
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
        AliyunPasterWithTextView captionView = (AliyunPasterWithTextView)View.inflate(getContext(),
            R.layout.aliyun_svideo_qupai_paster_text, null);
        mPasterContainer.addView(captionView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        return new PasterUITextImpl(captionView, controller, mThumbLineBar, mAliyunIEditor, restore);
    }

    /**
     * 添加涂鸦
     *
     * @param canvasController
     * @return
     */
    private View addPaint(AliyunICanvasController canvasController) {
        View canvasView = canvasController.getCanvas();
        mPasterContainer.removeView(canvasView);
        mPasterContainer.addView(canvasView, mPasterContainer.getWidth(), mPasterContainer.getHeight());
        return canvasView;
    }

    @Override
    public void onClick(View view) {
        if (view == mPlayImage && mAliyunIEditor != null) {
            //当在添加特效的时候，关闭该按钮
            if (mUseAnimationFilter){
                return;
            }
            if (mAliyunIEditor.isPlaying()) {
                playingPause();
            } else {
                playingResume();
                if (mCurrentEditEffect != null && !mCurrentEditEffect.isPasterRemoved()) {
                    mCurrentEditEffect.editTimeCompleted();
                    //要保证涂鸦永远在动图的上方，则需要每次添加动图时都把已经渲染的涂鸦remove掉，添加完动图后，再重新把涂鸦加上去
                    if (mCanvasController != null && mCanvasController.hasCanvasPath()) {
                        mCanvasController.removeCanvas();
                        mCanvasController.resetPaintCanvas();
                    }
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
            case AUDIO_MIX:
                playingResume();
                break;
            case FONT:
            case OVERLAY:
            case CAPTION:
                //这里做paster的撤销恢复处理
                if (mCurrentEditEffect != null && !mCurrentEditEffect.isEditCompleted()) {
                    mCurrentEditEffect.removePaster();
                }

                //先remove所有指定类型的paster
                for (int i = 0; i < mPasterContainer.getChildCount(); i++) {
                    View childAt = mPasterContainer.getChildAt(i);
                    Object tag = childAt.getTag();
                    if (tag == null || !(tag instanceof AbstractPasterUISimpleImpl)){
                        continue;
                    }
                    AbstractPasterUISimpleImpl uiSimple = (AbstractPasterUISimpleImpl) tag;

                    if (isPasterTypeHold(uiSimple.getEditorPage(),page)){
                        // 1.Controller remove
                        // 2.pasterContainer remove
                        // 3.ThumbLBar remove
                        uiSimple.removePaster();
                        //涉及到集合遍历删除元素的问题（角标前移）
                        i --;
                    }

                }

                //恢复缓存的指定类型paster
                for (EffectBase effectBase : mPasterEffecCachetList) {
                    AliyunPasterController pasterController;

                    //获取对应的controller、（判断文件存在，避免用户删除了对应的资源后恢复时crash）
                    if (effectBase instanceof EffectCaption && new File(effectBase.getPath()).exists()) {
                        EffectCaption effect = (EffectCaption) effectBase;
                        pasterController = mPasterManager.addPasterWithStartTime(effect.getPath(),effect.start,effect.end - effect.start);
                    } else if (effectBase instanceof EffectText) {
                        EffectText effect = (EffectText) effectBase;
                        pasterController = mPasterManager.addSubtitleWithStartTime(effect.text,effect.font,effect.start,effect.end - effect.start);
                    } else if (effectBase instanceof EffectPaster && new File(effectBase.getPath()).exists()) {
                        EffectPaster effect = (EffectPaster) effectBase;
                        pasterController = mPasterManager.addPasterWithStartTime(effect.getPath(),effect.start,effect.end - effect.start);
                    }else {
                        continue;
                    }
                    pasterController.setEffect(effectBase);
                    //锁定参数（避免被设置effectBase参数被冲掉）
                    pasterController.setRevert(true);
                    if (pasterController.getPasterType() == EffectPaster.PASTER_TYPE_GIF) {
                        mCurrentEditEffect = addPaster(pasterController);
                    } else if (pasterController.getPasterType() == EffectPaster.PASTER_TYPE_TEXT) {
                        mCurrentEditEffect = addSubtitle(pasterController, true);
                    } else if (pasterController.getPasterType() == EffectPaster.PASTER_TYPE_CAPTION) {
                        mCurrentEditEffect = addCaption(pasterController);
                    }
                    mCurrentEditEffect.showTimeEdit();
                    mCurrentEditEffect.editTimeStart();
                    mCurrentEditEffect.moveToCenter();
                    mCurrentEditEffect.editTimeCompleted();
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

        mViewOperate.hideBottomEditorView(page);

    }

    /**
     * 恢复动效滤镜UI（这里主要是编辑页面顶部时间轴的覆盖
     *
     * @param animationFilters
     */
    @Override
    public void animationFilterRestored(final List<EffectFilter> animationFilters) {
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
        mPasterManager.setDisplaySize((int)(mPasterContainerPoint.x * scaleSize),
            (int)(mPasterContainerPoint.y * scaleSize));
    }

    /**
     * 通过前面的界面传递的入口类型的参数, 为了区分短视频和社区两个模块的不同进入该界面的路径
     *
     * @param moduleEntrance svideo: 短视频,  community: 社区
     */
    public void setModuleEntrance(String moduleEntrance) {
        this.entrance = moduleEntrance;
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
            Log.d(TAG, "onSingleTapConfirmed");

            if (!shouldDrag) {
                boolean outside = true;
                BaseChooser bottomView = null;
                if (mViewOperate != null) {
                    bottomView = mViewOperate.getBottomView();
                }
                if (bottomView != null) {

                    int count = mPasterContainer.getChildCount();
                    for (int i = count - 1; i >= 0; i--) {
                        View pv = mPasterContainer.getChildAt(i);
                        AbstractPasterUISimpleImpl uic = (AbstractPasterUISimpleImpl)pv.getTag();

                        if (uic != null && bottomView.isHostPaster(uic)) {
                            if (uic.isVisibleInTime(mAliyunIEditor.getCurrentStreamPosition())
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
                                    mAliyunIEditor.getCurrentStreamPosition())) {
                                    uic.editTimeCompleted();
                                    playingResume();
                                }
                            }
                        }
                    }
                }

                if (outside) {
                    if (mCurrentEditEffect != null && !mCurrentEditEffect.isEditCompleted()) {
                        mCurrentEditEffect.editTimeCompleted();
                        //要保证涂鸦永远在动图的上方，则需要每次添加动图时都把已经渲染的涂鸦remove掉，添加完动图后，再重新把涂鸦加上去
                        //mCanvasController = mAliyunIEditor.obtainCanvasController(getContext()
                        // .getApplicationContext(),
                        //    mGlSurfaceContainer.getWidth(), mGlSurfaceContainer.getHeight());
                        if (mCanvasController != null && mCanvasController.hasCanvasPath()) {
                            mCanvasController.removeCanvas();
                            mCanvasController.resetPaintCanvas();
                        }
                    }
                    hideBottomEditorView();
                }
            } else {
                playingPause();
                mCurrentEditEffect.showTextEdit(mUseInvert);
            }
            //            if (mAliyunPasterController != null) {
            //                //旋转动图，文字，字幕
            //                ActionRotate actionRotate = new ActionRotate();
            //                actionRotate.setStartTime(0);
            //                actionRotate.setTargetId(mAliyunPasterController.getEffect().getViewId());
            //                actionRotate.setDuration(10 * 1000 * 1000);
            //                actionRotate.setRepeat(true);
            //                actionRotate.setDurationPerCircle(3 * 1000 * 1000);
            //                mAliyunIEditor.addFrameAnimation(actionRotate);
            //                if(mAliyunPasterController.getEffect() instanceof EffectCaption){
            //                    actionRotate = new ActionRotate();
            //                    actionRotate.setStartTime(0);
            //                    actionRotate.setDuration(10 * 1000 * 1000);
            //                    actionRotate.setRepeat(true);
            //                    actionRotate.setDurationPerCircle(3 * 1000 * 1000);
            //                    actionRotate.setTargetId(((EffectCaption) mAliyunPasterController.getEffect())
            // .gifViewId);
            //                    mAliyunIEditor.addFrameAnimation(actionRotate);
            //                }
            //            }
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
                    && mCurrentEditEffect.isVisibleInTime(mAliyunIEditor.getCurrentStreamPosition()

                );
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
        float relSec = (float)duration / (1000 * 1000);// us -> s
        int min = (int)((relSec % 3600) / 60);
        int sec = 0;
        sec = (int)(relSec % 60);
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
                Common.copyAll(getContext(), resCopy);
            }
        });
    }

    public AliyunIEditor getEditor() {
        return this.mAliyunIEditor;
    }

    public void showMessage(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(id);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private boolean checkInvert(String filePath) {
        NativeParser parser = new NativeParser();
        parser.init(filePath);
        boolean ret = parser.getMaxGopSize() <= 5;
        parser.release();
        parser.dispose();
        return ret;
    }

    /**
     * 设置转场的预览监听
     */
    private TransitionChooserView.OnPreviewListener mOnTransitionPreviewListener = new TransitionChooserView
        .OnPreviewListener() {
        @Override
        public void onPreview(int clipIndex, long leadTime, boolean isStop) {
            long clipStartTime = mAliyunIEditor.getClipStartTime(clipIndex + 1);
            mAliyunIEditor.seek(clipStartTime);
            playingResume();
            Log.d(TAG, "onTransitionPreview: index = " + clipIndex
                + " ,clipStartTime = " + clipStartTime
                + " ,duration = " + mAliyunIEditor.getDuration());
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

    private void deleteTempFiles() {
        if (mTempFilePaths != null) {
            for (String path : mTempFilePaths) {
                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventColorFilterSelected(SelectColorFilter selectColorFilter) {
        EffectInfo effectInfo = selectColorFilter.getEffectInfo();
        EffectBean effect = new EffectBean();
        effect.setId(effectInfo.id);
        effect.setPath(effectInfo.getPath());
        mAliyunIEditor.applyFilter(effect);
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
        if (mCanAddAnimation){
            playingResume();
        }else {
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

                    if (!mUseAnimationFilter){
                        //当正在添加滤镜的时候，不允许重新播放
                        mAliyunIEditor.replay();
                        mThumbLineBar.restart();
                    }else {
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
                        case AliyunEditorErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_WRONG_STATE:
                        case AliyunEditorErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_PROCESS_FAILED:
                        case AliyunEditorErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_NO_FREE_DISK_SPACE:
                        case AliyunEditorErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_CREATE_DECODE_GOP_TASK_FAILED:
                        case AliyunEditorErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_AUDIO_STREAM_DECODER_INIT_FAILED:
                        case AliyunEditorErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_VIDEO_STREAM_DECODER_INIT_FAILED:

                        case AliyunEditorErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_QUEUE_FULL_WARNING:
                        case AliyunEditorErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_SPS_PPS_NULL:
                        case AliyunEditorErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_CREATE_H264_PARAM_SET_FAILED:
                        case AliyunEditorErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_CREATE_HEVC_PARAM_SET_FAILED:
                        case AliyunEditorErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_QUEUE_EMPTY_WARNING:
                        case AliyunEditorErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_CREATE_DECODER_FAILED:
                        case AliyunEditorErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_ERROR_STATE:
                        case AliyunEditorErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_ERROR_INPUT:
                        case AliyunEditorErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_ERROR_NO_BUFFER_AVAILABLE:

                        case AliyunEditorErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_ERROR_DECODE_SPS:
                        case AliyunEditorErrorCode.ALIVC_FRAMEWORK_AUDIO_DECODER_QUEUE_EMPTY_WARNING:
                        case AliyunEditorErrorCode.ALIVC_FRAMEWORK_AUDIO_DECODER_QUEUE_FULL_WARNING:
                        case AliyunEditorErrorCode.ALIVC_FRAMEWORK_AUDIO_DECODER_CREATE_DECODER_FAILED:
                        case AliyunEditorErrorCode.ALIVC_FRAMEWORK_AUDIO_DECODER_ERROR_STATE:
                        case AliyunEditorErrorCode.ALIVC_FRAMEWORK_AUDIO_DECODER_ERROR_INPUT:
                        case AliyunEditorErrorCode.ALIVC_FRAMEWORK_AUDIO_DECODER_ERROR_NO_BUFFER_AVAILABLE:
                            showToast = FixedToastUtils.show(getContext(), "错误码是" + errorCode);
                            ((Activity)getContext()).finish();
                            break;
                        case AliyunEditorErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_CACHE_DATA_SIZE_OVERFLOW:
                            showToast = FixedToastUtils.show(getContext(), "错误码是" + errorCode);
                            mThumbLineBar.restart();
                            mAliyunIEditor.play();
                            break;
                        case AliyunErrorCode.ERROR_MEDIA_NOT_SUPPORTED_AUDIO:
                            showToast = FixedToastUtils.show(getContext(),
                                getResources().getString(R.string.not_supported_audio));
                            ((Activity)getContext()).finish();
                            break;
                        case AliyunErrorCode.ERROR_MEDIA_NOT_SUPPORTED_VIDEO:
                            showToast = FixedToastUtils.show(getContext(),
                                getResources().getString(R.string.not_supported_video));
                            ((Activity)getContext()).finish();
                            break;
                        case AliyunEditorErrorCode.ALIVC_FRAMEWORK_MEDIA_POOL_STREAM_NOT_EXISTS:
                        case AliyunEditorErrorCode.ALIVC_FRAMEWORK_VIDEO_DECODER_ERROR_INTERRUPT:
                        case AliyunErrorCode.ERROR_MEDIA_NOT_SUPPORTED_PIXEL_FORMAT:
                            showToast = FixedToastUtils.show(getContext(),
                                getResources().getString(R.string.not_supported_pixel_format));
                            ((Activity)getContext()).finish();
                            break;
                        default:
                            showToast = FixedToastUtils.show(getContext(),
                                getResources().getString(R.string.play_video_error));
                            ((Activity)getContext()).finish();
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
            return 0;
        }

        @Override
        public void onPlayProgress(final long currentPlayTime, final long currentStreamPlayTime) {
            post(new Runnable() {
                @Override
                public void run() {
                    long currentPlayTime = mAliyunIEditor.getCurrentPlayPosition();
                    if (mUseAnimationFilter && mAliyunIEditor.getDuration() - currentPlayTime < USE_ANIMATION_REMAIN_TIME) {
                        mCanAddAnimation = false;
                    }else{
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
    public static final int USE_ANIMATION_REMAIN_TIME=300 * 1000;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KEYCODE_VOLUME_DOWN:
                mVolume -= 5;
                if (mVolume < 0) {
                    mVolume = 0;
                }
                Log.d("xxffdd", "volume down, current volume = " + mVolume);
                mAliyunIEditor.setVolume(mVolume);
                return true;
            case KEYCODE_VOLUME_UP:
                mVolume += 5;
                if (mVolume > 100) {
                    mVolume = 100;
                }
                Log.d("xxffdd", "volume up, current volume = " + mVolume);
                mAliyunIEditor.setVolume(mVolume);
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    private boolean isNeedResume = true;

    public void onStart() {
        mIsStop = false;
        mIvRight.setEnabled(true);
        if (mViewStack != null) {
            mViewStack.setVisibleStatus(true);
        }
    }

    public void onResume() {
        if (isNeedResume) {
            playingResume();
        }
        //当删除使用的MV的时候，会发生崩溃，所以在次判断一下mv是否被删除
        if (mLastMVEffect != null) {
            String path = Common.getMVPath(mLastMVEffect.list, mVideoParam.getOutputWidth(),
                mVideoParam.getOutputHeight());

            if (!TextUtils.isEmpty(path)&&!new File(path).exists()) {
                applyMVEffect(new EffectInfo());
            }
        }
        checkAndRemovePaster();
    }

    public void onPause() {
        isNeedResume = mAliyunIEditor.isPlaying();
        playingPause();
        mAliyunIEditor.saveEffectToLocal();
    }

    public void onStop() {
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
            }else {
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

        if (executorService != null){
            executorService.shutdownNow();
        }
        //退出编辑界面，将编辑生成的文件（编辑添加的文字图片会保存为文件存在project相应目录）及project config配置删除，如果后续还有合成该视频的需求则不应该删除
        //        String path = mUri.getPath();
        //        File f = new File(path);
        //        if(!f.exists()){
        //            return ;
        //        }
        //        FileUtils.deleteDirectory(f.getParentFile());
        //删除录制生成的临时文件
        //deleteTempFiles();由于返回依然可以接着录，因此现在不能删除
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mViewStack.onActivityResult(requestCode, resultCode, data);
    }

    public boolean onBackPressed() {
        if (mViewOperate != null) {
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

    public void setParam(AliyunVideoParam mVideoParam, Uri mUri, boolean hasTailAnimation) {
        this.hasTailAnimation = hasTailAnimation;
        this.mUri = mUri;
        this.mVideoParam = mVideoParam;
        initEditor();
    }

    private AliyunVideoParam mVideoParam;

    public void setTempFilePaths(ArrayList<String> mTempFilePaths) {
        this.mTempFilePaths = mTempFilePaths;
    }

    private ArrayList<String> mTempFilePaths = null;

    /**
     * 播放时间、显示时间同步接口
     */
    public interface PlayerListener {

        //获取当前的播放时间（-->缩略图条位置同步）
        long getCurrDuration();

        //获取视频总时间
        long getDuration();

        //更新时间（-->显示时间同步）
        void updateDuration(long duration);
    }
}

