/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.editor;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aliyun.common.media.ShareableBitmap;
import com.aliyun.common.utils.DensityUtil;
import com.aliyun.common.utils.StorageUtils;
import com.aliyun.common.utils.ToastUtil;
import com.aliyun.demo.editor.timeline.TimelineBar;
import com.aliyun.demo.effects.control.BottomAnimation;
import com.aliyun.demo.effects.control.EditorService;
import com.aliyun.demo.effects.control.EffectInfo;
import com.aliyun.demo.effects.control.OnDialogButtonClickListener;
import com.aliyun.demo.effects.control.OnEffectChangeListener;
import com.aliyun.demo.effects.control.OnTabChangeListener;
import com.aliyun.demo.effects.control.TabGroup;
import com.aliyun.demo.effects.control.TabViewStackBinding;
import com.aliyun.demo.effects.control.UIEditorPage;
import com.aliyun.demo.effects.control.ViewStack;
import com.aliyun.demo.effects.filter.AnimationFilterController;
import com.aliyun.demo.effects.paint.PaintMenuView;
import com.aliyun.demo.effects.paint.PaintMenuView.OnPaintOpera;
import com.aliyun.demo.msg.Dispatcher;
import com.aliyun.demo.msg.body.FilterTabClick;
import com.aliyun.demo.msg.body.LongClickAnimationFilter;
import com.aliyun.demo.msg.body.LongClickUpAnimationFilter;
import com.aliyun.demo.msg.body.SelectColorFilter;
import com.aliyun.demo.publish.PublishActivity;
import com.aliyun.demo.util.Common;
import com.aliyun.demo.widget.AliyunPasterWithImageView;
import com.aliyun.demo.widget.AliyunPasterWithTextView;
import com.aliyun.querrorcode.AliyunErrorCode;
import com.aliyun.qupai.editor.AliyunICanvasController;
import com.aliyun.qupai.editor.AliyunIEditor;
import com.aliyun.qupai.editor.AliyunIPlayer;
import com.aliyun.qupai.editor.AliyunIThumbnailFetcher;
import com.aliyun.qupai.editor.AliyunPasterController;
import com.aliyun.qupai.editor.AliyunPasterManager;
import com.aliyun.qupai.editor.AliyunThumbnailFetcherFactory;
import com.aliyun.qupai.editor.OnAnimationFilterRestored;
import com.aliyun.qupai.editor.OnPasterRestored;
import com.aliyun.qupai.editor.OnPlayCallback;
import com.aliyun.qupai.editor.OnPreparedListener;
import com.aliyun.qupai.editor.impl.AliyunEditorFactory;
import com.aliyun.qupai.import_core.AliyunIImport;
import com.aliyun.qupai.import_core.AliyunImportCreator;
import com.aliyun.struct.common.AliyunDisplayMode;
import com.aliyun.struct.common.AliyunVideoParam;
import com.aliyun.struct.common.ScaleMode;
import com.aliyun.struct.common.VideoDisplayMode;
import com.aliyun.struct.effect.EffectBean;
import com.aliyun.struct.effect.EffectFilter;
import com.aliyun.struct.effect.EffectPaster;
import com.aliyun.struct.effect.EffectPicture;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class EditorActivity extends AppCompatActivity implements
        OnTabChangeListener, OnEffectChangeListener, BottomAnimation, View.OnClickListener, OnAnimationFilterRestored {
    private static final String TAG = "EditorActivity";
    public static final String KEY_VIDEO_PARAM = "video_param";
    public static final String KEY_PROJECT_JSON_PATH = "project_json_path";
    public static final String KEY_TEMP_FILE_LIST = "temp_file_list";

    private LinearLayout mBottomLinear;
    private SurfaceView mSurfaceView;
    private TabGroup mTabGroup;
    private ViewStack mViewStack;
    private EditorService mEditorService;

    private AliyunIEditor mAliyunIEditor;
    private AliyunIPlayer mAliyunIPlayer;
    private AliyunPasterManager mPasterManager;
    private RecyclerView mThumbnailView;
    private TimelineBar mTimelineBar;
    private RelativeLayout mActionBar;
    private FrameLayout resCopy;

    private FrameLayout mPasterContainer;
    private FrameLayout mGlSurfaceContainer;
    private Uri mUri;
    private EffectPicture mPicture;
    private int mScreenWidth;
    private ImageView mIvLeft;
    private ImageView mIvRight;
    private TextView mTvCenter;
    private LinearLayout mBarLinear;
    private ImageView mPlayImage;
    private TextView mTvCurrTime;
    private AliyunVideoParam mVideoParam;
    private boolean mIsComposing = false; //当前是否正在合成视频
    private boolean isFullScreen = false; //导入视频是否全屏显示
    private ProgressDialog dialog;
    private MediaScannerConnection mMediaScanner;
    private RelativeLayout mEditor;
    private AliyunICanvasController mCanvasController;
    private ArrayList<String> mTempFilePaths = null;
    private AliyunIThumbnailFetcher mThumbnailFetcher;
    private Bitmap mWatermarkBitmap;
    private File mWatermarkFile;
    private AnimationFilterController mAnimationFilterController;
//    private AudioTimePicker mAudioTimePicker;
//    private View mPicker;
//    private EffectBean mAudioEffect;
//    private EditText mVideoFadeIndex, mFadeDurationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dispatcher.getInstance().register(this);
        mWatermarkFile = new File(StorageUtils.getCacheDirectory(EditorActivity.this) + "/AliyunEditorDemo/tail/logo.png");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        mScreenWidth = point.x;
        setContentView(R.layout.aliyun_svideo_activity_editor);
        Intent intent = getIntent();
        if (intent.getStringExtra(KEY_PROJECT_JSON_PATH) != null) {
            mUri = Uri.fromFile(new File(intent.getStringExtra(KEY_PROJECT_JSON_PATH)));
        }
        if (intent.getSerializableExtra(KEY_VIDEO_PARAM) != null) {
            mVideoParam = (AliyunVideoParam) intent.getSerializableExtra(KEY_VIDEO_PARAM);
        }

        mTempFilePaths = intent.getStringArrayListExtra(KEY_TEMP_FILE_LIST);
        initView();
        initListView();
        add2Control();
        initEditor();
        mMediaScanner = new MediaScannerConnection(this, null);
        mMediaScanner.connect();
        copyAssets();
    }

    private void initView() {
        mEditor = (RelativeLayout) findViewById(R.id.activity_editor);
        resCopy = (FrameLayout) findViewById(R.id.copy_res_tip);
        mBarLinear = (LinearLayout) findViewById(R.id.bar_linear);
        mBarLinear.bringToFront();
        mActionBar = (RelativeLayout) findViewById(R.id.action_bar);
        mIvLeft = (ImageView) findViewById(R.id.iv_left);
        mTvCenter = (TextView) findViewById(R.id.tv_center);
        mIvRight = (ImageView) findViewById(R.id.iv_right);
        mIvLeft.setImageResource(R.mipmap.aliyun_svideo_icon_back);
        mTvCenter.setText(getString(R.string.edit_nav_edit));
        mIvRight.setImageResource(R.mipmap.aliyun_svideo_icon_next);
        mIvLeft.setVisibility(View.VISIBLE);
        mIvRight.setVisibility(View.VISIBLE);
        mTvCenter.setVisibility(View.VISIBLE);
        mIvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        mTvCurrTime = (TextView) findViewById(R.id.tv_curr_duration);

        mGlSurfaceContainer = (FrameLayout) findViewById(R.id.glsurface_view);
        mSurfaceView = (SurfaceView) findViewById(R.id.play_view);
        mBottomLinear = (LinearLayout) findViewById(R.id.edit_bottom_tab);

        mPasterContainer = (FrameLayout) findViewById(R.id.pasterView);

        mPlayImage = (ImageView) findViewById(R.id.play_button);
        mPlayImage.setOnClickListener(this);

        final GestureDetector mGesture = new GestureDetector(this,
                new MyOnGestureListener());
        View.OnTouchListener pasterTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGesture.onTouchEvent(event);
                return true;
            }
        };

        mPasterContainer.setOnTouchListener(pasterTouchListener);

//        mPicker = findViewById(R.id.time_picker);
//        mPicker.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mAudioEffect.setStartTime(mAudioTimePicker.getStart());
//                mAudioEffect.setDuration(mAudioTimePicker.getEnd() - mAudioTimePicker.getStart());
//                mAliyunIEditor.applyMusic(mAudioEffect);
//                mTimelineBar.resume();
//                mPlayImage.setSelected(false);
//                mAudioTimePicker.hideAudioTimePicker();
//            }
//        });
//
//        mVideoFadeIndex = (EditText) findViewById(R.id.fade_start);
//        mFadeDurationView = (EditText) findViewById(R.id.fade_end);
//
//        View fadeCompleted = findViewById(R.id.fade_completed);
//        fadeCompleted.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String indexStr = mVideoFadeIndex.getText().toString();
//                String fadeDuStr = mFadeDurationView.getText().toString();
//
//                if(TextUtils.isEmpty(indexStr)
//                        || !TextUtils.isDigitsOnly(indexStr)
//                        || TextUtils.isEmpty(fadeDuStr)
//                        || !TextUtils.isDigitsOnly(fadeDuStr)){
//                    return ;
//                }
//                int index = Integer.parseInt(indexStr);
//                int fadeDu = Integer.parseInt(fadeDuStr);
//
//                mAliyunIEditor.setClipFadeDurationAndAnimation(index, fadeDu, 0, 0);
//            }
//        });
    }

    private void initGlSurfaceView() {
        if (mAliyunIPlayer != null) {
            if (mVideoParam == null) {
                return;
            }
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mGlSurfaceContainer.getLayoutParams();
            int rotation = mAliyunIPlayer.getRotation();
            int outputWidth = mVideoParam.getOutputWidth();
            int outputHeight = mVideoParam.getOutputHeight();
            if ((rotation == 90 || rotation == 270)) {
                int temp = outputWidth;
                outputWidth = outputHeight;
                outputHeight = temp;
            }

            float percent;
            if (outputWidth >= outputHeight) {
                percent = (float) outputWidth / outputHeight;
            } else {
                percent = (float) outputHeight / outputWidth;
            }
            if (percent < 1.5 || (rotation == 90 || rotation == 270)) {
                layoutParams.height = Math.round((float) outputHeight * mScreenWidth / outputWidth);
                layoutParams.addRule(RelativeLayout.BELOW, R.id.bar_linear);
            } else {
                layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
                isFullScreen = true;
                mBottomLinear.setBackgroundColor(getResources().getColor(R.color.tab_bg_color_50pct));
                mActionBar.setBackgroundColor(getResources().getColor(R.color.action_bar_bg_50pct));
            }
            mGlSurfaceContainer.setLayoutParams(layoutParams);
        }
    }

    private void initListView() {
        mEditorService = new EditorService();
        mTabGroup = new TabGroup();
        mViewStack = new ViewStack(this);
        mViewStack.setEditorService(mEditorService);
        mViewStack.setEffectChange(this);
        mViewStack.setBottomAnimation(this);
        mViewStack.setDialogButtonClickListener(mDialogButtonClickListener);

        mTabGroup.addView(findViewById(R.id.tab_effect_filter));
        mTabGroup.addView(findViewById(R.id.tab_effect_overlay));
        mTabGroup.addView(findViewById(R.id.tab_effect_caption));
        mTabGroup.addView(findViewById(R.id.tab_effect_mv));
        mTabGroup.addView(findViewById(R.id.tab_effect_audio_mix));
        mTabGroup.addView(findViewById(R.id.tab_paint));
    }

    private void add2Control() {
        TabViewStackBinding tabViewStackBinding = new TabViewStackBinding();
        tabViewStackBinding.setViewStack(mViewStack);
        mTabGroup.setOnCheckedChangeListener(tabViewStackBinding);
        mTabGroup.setOnTabChangeListener(this);
    }

    private void initEditor() {
//        String[] url = new String[]{
//                "/storage/emulated/0/Android/data/com.fengzhongkeji/files/2or3m/1516353293665-1193959466.mp4",
//                "/storage/emulated/0/Android/data/com.fengzhongkeji/files/2or3m/1516353304243-1193959466.mp4",
//                "/storage/emulated/0/Android/data/com.fengzhongkeji/files/2or3m/1516353314905-1193959466.mp4",
//                "/storage/emulated/0/Android/data/com.fengzhongkeji/files/2or3m/1516353328222-1193959466.mp4",
//                "/storage/emulated/0/Android/data/com.fengzhongkeji/files/2or3m/1516353342029-1193959466.mp4",
//                "/storage/emulated/0/Android/data/com.fengzhongkeji/files/2or3m/1516353358921-1193959466.mp4",
//                "/storage/emulated/0/Android/data/com.fengzhongkeji/files/2or3m/1516353370427-1193959466.mp4",
//                "/storage/emulated/0/Android/data/com.fengzhongkeji/files/2or3m/1516353392072-1193959466.mp4",
//                "/storage/emulated/0/Android/data/com.fengzhongkeji/files/2or3m/1516353399461-1193959466.mp4",
//                "/storage/emulated/0/Android/data/com.fengzhongkeji/files/2or3m/1516353410446-1193959466.mp4",
//                "/storage/emulated/0/Android/data/com.fengzhongkeji/files/2or3m/1516353421613-1193959466.mp4",
//                "/storage/emulated/0/Android/data/com.fengzhongkeji/files/2or3m/1516353432714-1193959466.mp4",
//                "/storage/emulated/0/Android/data/com.fengzhongkeji/files/2or3m/1516353443815-1193959466.mp4",
//                "/storage/emulated/0/Android/data/com.fengzhongkeji/files/2or3m/1516353456628-1193959466.mp4",
//                "/storage/emulated/0/Android/data/com.fengzhongkeji/files/2or3m/1516353468134-1193959466.mp4",
//                "/storage/emulated/0/Android/data/com.fengzhongkeji/files/2or3m/1516353479188-1193959466.mp4",
//                "/storage/emulated/0/Android/data/com.fengzhongkeji/files/2or3m/1516353491112-1193959466.mp4",
//                "/storage/emulated/0/Android/data/com.fengzhongkeji/files/2or3m/ESDzcDn2BF.mp4"
//        };
//        AliyunIImport aliyunIImport = AliyunImportCreator.getImportInstance(getApplicationContext());
//        AliyunVideoParam param = new AliyunVideoParam();
//        param.setFrameRate(25);
//        param.setHWAutoSize(true);
//        param.setOutputWidth(540);
//        param.setOutputHeight(960);
//        aliyunIImport.setVideoParam(param);
//        MediaMetadataRetriever mr = new MediaMetadataRetriever();
//        for(String u:url) {
//            aliyunIImport.addVideo(u, 0, 0, 0, AliyunDisplayMode.DEFAULT);
//        }
//        mAliyunIEditor = AliyunEditorFactory.creatAliyunEditor(Uri.fromFile(new File(aliyunIImport.generateProjectConfigure())));

        mAliyunIEditor = AliyunEditorFactory.creatAliyunEditor(mUri);
        mAliyunIEditor.init(mSurfaceView);
        mAliyunIPlayer = mAliyunIEditor.createAliyunPlayer();
        if (mAliyunIPlayer == null) {
            ToastUtil.showToast(this, "Create AliyunPlayer failed");
            finish();
            return;
        }
        initGlSurfaceView();
        mThumbnailView = (RecyclerView) findViewById(R.id.rv_thumbnail);
        mThumbnailView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mThumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher();
        mThumbnailFetcher.fromConfigJson(mUri.getPath());
        mThumbnailView.setAdapter(new ThumbnailAdapter(10, mThumbnailFetcher, mScreenWidth));
        mEditorService.setFullScreen(isFullScreen);
        mEditorService.addTabEffect(UIEditorPage.MV, mAliyunIEditor.getMVLastApplyId());
        mEditorService.addTabEffect(UIEditorPage.FILTER_EFFECT, mAliyunIEditor.getFilterLastApplyId());
        mEditorService.addTabEffect(UIEditorPage.AUDIO_MIX, mAliyunIEditor.getMusicLastApplyId());
        mEditorService.setPaint(mAliyunIEditor.getPaintLastApply());
        mPasterManager = mAliyunIEditor.createPasterManager();
        mAliyunIPlayer.setOnPreparedListener(new OnPreparedListener() {

            @Override
            public void onPrepared() {
                mAliyunIPlayer.start();
                mAliyunIEditor.setAnimationRestoredListener(EditorActivity.this);
                ScaleMode mode = mVideoParam.getScaleMode();
                if (mode != null) {
                    switch (mode) {
                        case LB:
                            mAliyunIPlayer.setDisplayMode(VideoDisplayMode.SCALE);
                            break;
                        case PS:
                            mAliyunIPlayer.setDisplayMode(VideoDisplayMode.FILL);
                            break;
                        default:
                            break;
                    }
                }
                mAliyunIPlayer.setFillBackgroundColor(Color.BLACK);
                if (mTimelineBar == null) {
                    mTimelineBar = new TimelineBar(
                            mAliyunIPlayer.getDuration(),
                            DensityUtil.dip2px(EditorActivity.this, 50),
                            new TimelineBar.TimelinePlayer() {
                                @Override
                                public long getCurrDuration() {
                                    return mAliyunIPlayer.getCurrentPosition();
                                }
                            });
                    mTimelineBar.setThumbnailView(new TimelineBar.ThumbnailView() {
                        @Override
                        public RecyclerView getThumbnailView() {
                            return mThumbnailView;
                        }

                        @Override
                        public ViewGroup getThumbnailParentView() {
                            return (ViewGroup) mThumbnailView.getParent();
                        }

                        @Override
                        public void updateDuration(long duration) {
                            mTvCurrTime.setText(convertDuration2Text(duration));
                        }
                    });
                    ViewGroup.MarginLayoutParams layoutParams =
                            (ViewGroup.MarginLayoutParams) mThumbnailView.getLayoutParams();
                    layoutParams.width = mScreenWidth;
                    mTimelineBar.setTimelineBarDisplayWidth(mScreenWidth);
                    mTimelineBar.setBarSeekListener(new TimelineBar.TimelineBarSeekListener() {
                        @Override
                        public void onTimelineBarSeek(long duration) {
                            mAliyunIPlayer.seek(duration);
                            mTimelineBar.pause();
                            mPlayImage.setSelected(true);
                            mPlayImage.setEnabled(false);
                            Log.d(TimelineBar.TAG, "OnTimelineSeek duration = " + duration);
                            if (mCurrentEditEffect != null
                                    && !mCurrentEditEffect.isEditCompleted()) {
                                if (!mCurrentEditEffect.isVisibleInTime(duration)) {
                                    //隐藏
                                    mCurrentEditEffect.mPasterView.setVisibility(View.GONE);
                                } else {
                                    //显示
                                    mCurrentEditEffect.mPasterView.setVisibility(View.VISIBLE);
                                }
                            }
                        }

                        @Override
                        public void onTimelineBarSeekFinish(long duration) {
//                            Log.e(TAG, "onTimelineBarSeekFinish duration..." + duration);
                            mAliyunIPlayer.seek(duration);
                            mTimelineBar.pause();
                            mPlayImage.setSelected(true);
                        }
                    });
                }

//                mAudioTimePicker = new AudioTimePicker(getApplicationContext(),
//                        mPicker, mTimelineBar, mAliyunIPlayer.getDuration());
                mTimelineBar.start();
                mPasterManager.setDisplaySize(mPasterContainer.getWidth(),
                        mPasterContainer.getHeight());
                mPasterManager.setOnPasterRestoreListener(mOnPasterRestoreListener);
                mAnimationFilterController = new AnimationFilterController(getApplicationContext(), mTimelineBar,
                        mAliyunIEditor, mAliyunIPlayer);
            }
        });
        mAliyunIPlayer.setOnPlayCallbackListener(new OnPlayCallback() {

            @Override
            public void onPlayStarted() {
                Log.d("xxx", "AliyunIPlayer onPlayStarted");
                if (mTimelineBar != null) {
                    if (mTimelineBar.isPausing() && !mIsComposing) {
                        mTimelineBar.resume();
                    }
                }
                if (mWatermarkFile.exists()) {
                    if (mWatermarkBitmap == null) {
                        mWatermarkBitmap = BitmapFactory.decodeFile(StorageUtils.getCacheDirectory(EditorActivity.this) + "/AliyunEditorDemo/tail/logo.png");
                    }
                    /**
                     * 水印例子 水印的大小为 ：水印图片的宽高和显示区域的宽高比，注意保持图片的比例，不然显示不完全  水印的位置为 ：以水印图片中心点为基准，显示区域宽高的比例为偏移量，0,0为左上角，1,1为右下角
                     */
                    mAliyunIEditor.applyWaterMark(StorageUtils.getCacheDirectory(EditorActivity.this) + "/AliyunEditorDemo/tail/logo.png",
                            (float) mWatermarkBitmap.getWidth() * 0.5f * 0.8f / mSurfaceView.getWidth(),
                            (float) mWatermarkBitmap.getHeight() * 0.5f * 0.8f / mSurfaceView.getHeight(),
                            1f - (float) mWatermarkBitmap.getWidth() / 1.5f / mSurfaceView.getWidth() / 2,
                            0f + (float) mWatermarkBitmap.getHeight() / 1.5f / mSurfaceView.getHeight() / 2);
                }
            }

            @Override
            public void onError(int errorCode) {
                switch (errorCode) {
                    case AliyunErrorCode.ERROR_MEDIA_NOT_SUPPORTED_AUDIO:
                        ToastUtil.showToast(EditorActivity.this, R.string.not_supported_audio);
                        finish();
                        break;
                    case AliyunErrorCode.ERROR_MEDIA_NOT_SUPPORTED_VIDEO:
                        ToastUtil.showToast(EditorActivity.this, R.string.not_supported_video);
                        finish();
                        break;
                    case AliyunErrorCode.ERROR_MEDIA_NOT_SUPPORTED_PIXEL_FORMAT:
                        ToastUtil.showToast(EditorActivity.this, R.string.not_supported_pixel_format);
                        finish();
                        break;
                    default:
                        ToastUtil.showToast(EditorActivity.this, R.string.play_video_error);
                        break;
                }
//                mPlayImage.setEnabled(true);
                mAliyunIPlayer.stop();
                mTimelineBar.stop();
//                finish();
                mPlayImage.setEnabled(true);
            }

            @Override
            public void onSeekDone() {
                mPlayImage.setEnabled(true);
            }

            @Override
            public void onPlayCompleted() {
                //重播时必须先掉stop，再调用start
//                mAliyunIPlayer.stop();
                mAliyunIPlayer.start();
                mTimelineBar.restart();
//                Log.d(TimelineBar.TAG, "TailView aliyun_svideo_play restart");
            }

            @Override
            public int onTextureIDCallback(int txtID, int txtWidth, int txtHeight) {
//                Log.d(TAG, "onTextureIDCallback: txtID "+txtID+", txtWidth "+txtWidth+", txtHeight "+txtHeight);
                return 0;
            }
        });

        mIvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                v.setEnabled(false);
                final AliyunIThumbnailFetcher fetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher();
                fetcher.fromConfigJson(mUri.getPath());
                fetcher.setParameters(mAliyunIPlayer.getVideoWidth(), mAliyunIPlayer.getVideoHeight(), AliyunIThumbnailFetcher.CropMode.Mediate, ScaleMode.LB, 1);
                fetcher.requestThumbnailImage(new long[]{0},
                        new AliyunIThumbnailFetcher.OnThumbnailCompletion() {
                            @Override
                            public void onThumbnailReady(ShareableBitmap frameBitmap, long time) {
                                String path = getExternalFilesDir(null) + "thumbnail.jpeg";
                                try {
                                    frameBitmap.getData().compress(Bitmap.CompressFormat.JPEG, 100,
                                            new FileOutputStream(path));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                Intent intent = new Intent(EditorActivity.this, PublishActivity.class);
                                intent.putExtra(PublishActivity.KEY_PARAM_THUMBNAIL, path);
                                intent.putExtra(PublishActivity.KEY_PARAM_CONFIG, mUri.getPath());
                                startActivity(intent);

                                fetcher.release();
                            }

                            @Override
                            public void onError(int errorCode) {
                                fetcher.release();
                            }
                        });
//                if (mIsComposing) {
//                    return;
//                }
//                dialog = new ProgressDialog(EditorActivity.this);
//                dialog.setTitle("合成中");
//                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//                dialog.setMax(100);
//                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                    @Override
//                    public void onCancel(DialogInterface dialog) {
//                        mAliyunIEditor.getExporter().cancelCompose();
//                    }
//                });
//                if (mCurrentEditEffect != null) {
//                    mCurrentEditEffect.editTimeCompleted();
//                }
//                dialog.show();
//                mTimelineBar.aliyun_svideo_pause();
//                AliyunIExporter exporter = mAliyunIEditor.getExporter();
//                File tailImg = new File(StorageUtils.getCacheDirectory(EditorActivity.this) + "/AliyunEditorDemo/tail/logo.png");
//                if (tailImg.exists()) {
//                    exporter.setTailWatermark(StorageUtils.getCacheDirectory(EditorActivity.this) + "/AliyunEditorDemo/tail/logo.png",
//                            280.0f / mSurfaceView.getMeasuredWidth(),
//                            200.f / mSurfaceView.getMeasuredHeight(), 0, 0);
//                }
//                long time = System.currentTimeMillis();
//                final String path = Environment.getExternalStorageDirectory() + File.separator + "outputVideo" + time + ".mp4";
//                exporter.startCompose(path, new OnComposeCallback() {
//
//                    @Override
//                    public void onError() {
//                        mIsComposing = false;
//                        Log.e("COMPOSE", "compose error");
//                        dialog.aliyun_svideo_dismiss();
//                        Toast.makeText(getApplicationContext(), "合成失败", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        Log.e("COMPOSE", "compose finished");
//                        dialog.aliyun_svideo_dismiss();
//                        if (mMediaScanner != null) {
//                            mMediaScanner.scanFile(path, "video/mp4");
//                        }
//                        Toast.makeText(getApplicationContext(), "合成完成", Toast.LENGTH_SHORT).show();
//                        mAliyunIPlayer.start();
//                        mTimelineBar.resume();
//                        mIsComposing = false;
//                    }
//
//                    @Override
//                    public void onProgress(int progress) {
//                        Log.d(TAG, "compose progress " + progress);
//                        dialog.setProgress(progress);
//                    }
//
//                    @Override
//                    public void onCancel() {
//                        FileUtils.deleteFile(path);
//                        mAliyunIPlayer.start();
//                        mTimelineBar.resume();
//                        mIsComposing = false;
//                    }
//                });
//                mIsComposing = true;
            }
        });
    }

    private final OnPasterRestored mOnPasterRestoreListener = new OnPasterRestored() {

        @Override
        public void onPasterRestored(List<AliyunPasterController> controllers) {
            final List<PasterUISimpleImpl> aps = new ArrayList<>();
            for (AliyunPasterController c : controllers) {
                if (!c.isPasterExists()) {
                    continue;
                }
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
//                mCurrentEditEffect.editTimeCompleted();
                mCurrentEditEffect.moveToCenter();
            }

            mPasterContainer.post(new Runnable() {
                @Override
                public void run() {
                    for (PasterUISimpleImpl pui : aps) {
                        pui.editTimeCompleted();
                    }
                }
            });
            //要保证涂鸦永远在动图的上方，则需要每次添加动图时都把已经渲染的涂鸦remove掉，添加完动图后，再重新把涂鸦加上去
            mCanvasController = mAliyunIEditor.obtainCanvasController(EditorActivity.this, mGlSurfaceContainer.getWidth(), mGlSurfaceContainer.getHeight());
            if (mCanvasController.hasCanvasPath()) {
                mCanvasController.removeCanvas();
                mCanvasController.resetPaintCanvas();
            }
        }

    };

    @Override
    protected void onResume() {
        super.onResume();
        mAliyunIPlayer.resume();
        mPlayImage.setSelected(false);
        if (mTimelineBar != null) {
            mTimelineBar.resume();
        }
        mAliyunIEditor.onResume();
        checkAndRemovePaster();
        Log.d("xxx", "EditorActivity onResume");
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mCurrentEditEffect != null && !mCurrentEditEffect.isEditCompleted()) {
            mCurrentEditEffect.editTimeCompleted();
        }
        mAliyunIEditor.onPause();
        mAliyunIPlayer.pause();
        if (mTimelineBar != null) {
            mTimelineBar.pause();
        }

        mPlayImage.setSelected(true);
        if (dialog != null && dialog.isShowing()) {
            mIsComposing = false;
            dialog.cancel();
        }
        Log.d("xxx", "EditorActivity onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAnimationFilterController != null) {
            mAnimationFilterController.destroyController();
        }

        if (mAliyunIEditor != null) {
            mAliyunIEditor.onDestroy();
        }
        if (mTimelineBar != null) {
            mTimelineBar.stop();
        }
        if (mMediaScanner != null) {
            mMediaScanner.disconnect();
        }

        if (mThumbnailFetcher != null) {
            mThumbnailFetcher.release();
        }

        if (mCanvasController != null) {
            mCanvasController.release();
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

    @Override
    public void onTabChange() {
        //暂停播放
//        if (mAliyunIPlayer.isPlaying()) {
//            playingPause();
//        }

        //tab切换时通知
        hideBottomView();
        UIEditorPage index = UIEditorPage.get(mTabGroup.getCheckedIndex());
        int ix = mEditorService.getEffectIndex(index);
        switch (index) {
            case FILTER_EFFECT:
                break;
            case OVERLAY:
                break;
            default:
                break;
        }
        Log.e("editor", "====== onTabChange " + ix + " " + index);
    }

    @Override
    public void onEffectChange(EffectInfo effectInfo) {
        Log.e("editor", "====== onEffectChange ");
        //返回素材属性

        EffectBean effect = new EffectBean();
        effect.setId(effectInfo.id);
        effect.setPath(effectInfo.getPath());
        UIEditorPage type = effectInfo.type;
        AliyunPasterController c;
        Log.d(TAG, "effect path " + effectInfo.getPath());
        switch (type) {
            case AUDIO_MIX:
                mAliyunIEditor.applyMusicMixWeight(effectInfo.musicWeight);
                if (!effectInfo.isAudioMixBar) {
                    mAliyunIEditor.applyMusic(effect);
                    mTimelineBar.resume();
                    mPlayImage.setSelected(false);
                }
//                mAudioEffect = effect;
//                if (!effectInfo.isAudioMixBar) {
//                    if(TextUtils.isEmpty(effectInfo.getPath())){
//                        mAudioTimePicker.removeAudioTimePicker();
//                        mPicker.performClick();
//                    }else{
//                        mAudioTimePicker.showAudioTimePicker();
//                        playingPause();
//                    }
//
//                }
                break;
            case FILTER_EFFECT:
                if (effect.getPath().contains("Vertigo")) {
                    EffectFilter filter = new EffectFilter(effect.getPath());
                    filter.setStartTime(0);
                    filter.setDuration(5000);
                    mAliyunIEditor.addAnimationFilter(filter);
                } else {
                    mAliyunIEditor.applyFilter(effect);
                }
                break;
            case MV:
                if (mCurrentEditEffect != null && !mCurrentEditEffect.isPasterRemoved()) {
                    mCurrentEditEffect.editTimeCompleted();
                }

                String path = null;
                if (effectInfo.list != null) {
                    path = Common.getMVPath(effectInfo.list, mAliyunIPlayer.getVideoWidth(), mAliyunIPlayer.getVideoHeight());
                }
                effect.setPath(path);
                mAliyunIEditor.applyMV(effect);
                mTimelineBar.resume();
                mPlayImage.setSelected(false);
                break;
            case CAPTION:
                c = mPasterManager.addPaster(effectInfo.getPath());
                c.setPasterStartTime(mAliyunIPlayer.getCurrentPosition());
                PasterUICaptionImpl cui = addCaption(c);
                if (mCurrentEditEffect != null && !mCurrentEditEffect.isPasterRemoved()) {
                    mCurrentEditEffect.editTimeCompleted();
                }
                playingPause();
                mCurrentEditEffect = cui;
                mCurrentEditEffect.showTimeEdit();
                break;
            case OVERLAY:
                c = mPasterManager.addPaster(effectInfo.getPath());
                c.setPasterStartTime(mAliyunIPlayer.getCurrentPosition());
                PasterUIGifImpl gifui = addPaster(c);
                if (mCurrentEditEffect != null && !mCurrentEditEffect.isPasterRemoved()) {
                    mCurrentEditEffect.editTimeCompleted();
                }
                playingPause();
                mCurrentEditEffect = gifui;
                mCurrentEditEffect.showTimeEdit();

                break;
            case FONT:
                c = mPasterManager.addSubtitle(null, effectInfo.fontPath + "/font.ttf");
                c.setPasterStartTime(mAliyunIPlayer.getCurrentPosition());
                PasterUITextImpl textui = addSubtitle(c, false);
                if (mCurrentEditEffect != null && !mCurrentEditEffect.isPasterRemoved()) {
                    mCurrentEditEffect.editTimeCompleted();
                }
                playingPause();
                mCurrentEditEffect = textui;
                mCurrentEditEffect.showTimeEdit();
                textui.showTextEdit();
//                mCurrentEditEffect.setImageView((ImageView) findViewById(R.id.test_image));

                break;
            case PAINT:
                if (mCurrentEditEffect != null && !mCurrentEditEffect.isEditCompleted()) {
                    mCurrentEditEffect.editTimeCompleted();
                }
                mCanvasController = mAliyunIEditor.obtainCanvasController(EditorActivity.this, mGlSurfaceContainer.getWidth(), mGlSurfaceContainer.getHeight());
                mCanvasController.removeCanvas();
                addPaint(mCanvasController);
                break;
            default:
                break;
        }
    }

    private void checkAndRemovePaster() {
        int count = mPasterContainer.getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            View pv = mPasterContainer.getChildAt(i);
            PasterUISimpleImpl uic = (PasterUISimpleImpl) pv.getTag();
            if (uic != null && !uic.isPasterExists()) {
                Log.e(TAG, "removePaster");
                uic.removePaster();
            }
        }
    }

    protected void playingPause() {
        if (mAliyunIPlayer.isPlaying()) {
            mAliyunIPlayer.pause();
            mTimelineBar.pause();
            mPlayImage.setSelected(true);
        }
    }

    protected void playingResume() {
        if (!mAliyunIPlayer.isPlaying()) {
            mAliyunIPlayer.resume();
            mTimelineBar.resume();
            mPlayImage.setSelected(false);
        }
    }

    private PasterUIGifImpl addPaster(AliyunPasterController controller) {
        AliyunPasterWithImageView pasterView = (AliyunPasterWithImageView) View.inflate(this,
                R.layout.aliyun_svideo_qupai_paster_gif, null);

        mPasterContainer.addView(pasterView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        return new PasterUIGifImpl(pasterView, controller, mTimelineBar);
    }

    /**
     * 添加字幕
     *
     * @param controller
     * @return
     */
    private PasterUICaptionImpl addCaption(AliyunPasterController controller) {
        AliyunPasterWithImageView captionView = (AliyunPasterWithImageView) View.inflate(this,
                R.layout.aliyun_svideo_qupai_paster_caption, null);
//        ImageView content = (ImageView) captionView.findViewById(R.id.qupai_overlay_content_animation);
//        Glide.with(getApplicationContext())
//                .load("file://" + controller.getPasterIconPath())
//                .into(content);
        mPasterContainer.addView(captionView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        return new PasterUICaptionImpl(captionView, controller, mTimelineBar);
    }

    /**
     * 添加文字
     *
     * @param controller
     * @param restore
     * @return
     */
    private PasterUITextImpl addSubtitle(AliyunPasterController controller, boolean restore) {
        AliyunPasterWithTextView captionView = (AliyunPasterWithTextView) View.inflate(this,
                R.layout.aliyun_svideo_qupai_paster_text, null);
        mPasterContainer.addView(captionView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        return new PasterUITextImpl(captionView, controller, mTimelineBar, restore);
    }

    /**
     * 添加涂鸦
     *
     * @param canvasController
     * @return
     */
    private View addPaint(AliyunICanvasController canvasController) {
        hideBottomView();
        View canvasView = canvasController.getCanvas();
        mPasterContainer.removeView(canvasView);
        mPasterContainer.addView(canvasView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addPaintMenu(canvasController);
        return canvasView;
    }

    private void addPaintMenu(AliyunICanvasController canvasController) {
        PaintMenuView menuView = new PaintMenuView(canvasController);
        menuView.setOnPaintOpera(onPaintOpera);
        menuView.setEditorService(mEditorService);
        View view = menuView.getPaintMenu(this);
        if (isFullScreen) {
            view.findViewById(R.id.paint_menu).setBackgroundColor(getResources().getColor(R.color.tab_bg_color_50pct));
        }
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        view.setLayoutParams(layoutParams);
        mEditor.addView(view);
    }

    private OnPaintOpera onPaintOpera = new OnPaintOpera() {
        @Override
        public void removeView(View view) {
            mEditor.removeView(view);
            mPasterContainer.removeView(mCanvasController.getCanvas());
            showBottomView();
        }

        @Override
        public void completeView() {
            mCanvasController.applyPaintCanvas();
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mViewStack.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mIvRight.setEnabled(true);
    }

    @Override
    public void showBottomView() {
//        ViewCompat.animate(mBottomLinear)
//                .translationYBy(-mBottomLinear.getMeasuredHeight())
//                .alpha(1f)
//                .setDuration(300).start();

        mBottomLinear.setVisibility(View.VISIBLE);
        mActionBar.setVisibility(View.VISIBLE);
        mPlayImage.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideBottomView() {
//        ViewCompat.animate(mBottomLinear)
//                .translationYBy(mBottomLinear.getMeasuredHeight())
//                .alpha(0f)
//                .setDuration(300).start();

        mBottomLinear.setVisibility(View.GONE);
        mActionBar.setVisibility(View.GONE);
        mPlayImage.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        if (view == mPlayImage && mAliyunIPlayer != null) {
            if (mAliyunIPlayer.isPlaying()) {
                playingPause();
            } else {
                playingResume();
                if (mCurrentEditEffect != null && !mCurrentEditEffect.isPasterRemoved()) {
                    mCurrentEditEffect.editTimeCompleted();
                    //要保证涂鸦永远在动图的上方，则需要每次添加动图时都把已经渲染的涂鸦remove掉，添加完动图后，再重新把涂鸦加上去
                    mCanvasController = mAliyunIEditor.obtainCanvasController(EditorActivity.this, mGlSurfaceContainer.getWidth(), mGlSurfaceContainer.getHeight());
                    if (mCanvasController.hasCanvasPath()) {
                        mCanvasController.removeCanvas();
                        mCanvasController.resetPaintCanvas();
                    }
                }
            }
        }

//        if(view == mReset){
//            mGlSurfaceContainer.removeView(mSurfaceView);
//            mGlSurfaceContainer.post(new Runnable() {
//                @Override
//                public void run() {
//                    mAliyunIEditor.onDestroy();
//                    mAliyunIEditor = AliyunEditorFactory.creatAliyunEditor(mUri);
//                    mGlSurfaceContainer.addView(mSurfaceView, 0);
//                    mAliyunIEditor.init(mSurfaceView);
//                    mAliyunIPlayer = mAliyunIEditor.createAliyunPlayer();
//                    mPasterManager = mAliyunIEditor.createPasterManager();
//                }
//            });
//
//
//
//        }
    }

    private PasterUISimpleImpl mCurrentEditEffect;

    @Override
    public void animationFilterRestored(List<EffectFilter> animationFilters) {
        mAnimationFilterController.restoreAnimationFilters(animationFilters);
    }

    private class MyOnGestureListener extends
            GestureDetector.SimpleOnGestureListener {
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
            Log.d("MOVE", "onDoubleTapEvent");
            return super.onDoubleTapEvent(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.d("MOVE", "onSingleTapConfirmed");

            if (!shouldDrag) {
                boolean outside = true;
                int count = mPasterContainer.getChildCount();
                for (int i = count - 1; i >= 0; i--) {
                    View pv = mPasterContainer.getChildAt(i);
                    PasterUISimpleImpl uic = (PasterUISimpleImpl) pv.getTag();
                    if (uic != null) {
                        if (uic.isVisibleInTime(mAliyunIPlayer.getCurrentPosition())
                                && uic.contentContains(e.getX(), e.getY())) {
                            outside = false;
                            if (mCurrentEditEffect != null && mCurrentEditEffect != uic
                                    && !mCurrentEditEffect.isEditCompleted()) {
                                mCurrentEditEffect.editTimeCompleted();
                            }
                            mCurrentEditEffect = uic;
                            if (uic.isEditCompleted()) {
                                playingPause();
                                uic.editTimeStart();
                            }
                            break;
                        } else {
                            if (mCurrentEditEffect != uic && uic.isVisibleInTime(mAliyunIPlayer.getCurrentPosition())) {
                                uic.editTimeCompleted();
                                playingResume();
                            }
                        }
                    }
                }

                if (outside) {
                    if (mCurrentEditEffect != null && !mCurrentEditEffect.isEditCompleted()) {
//                        Log.d("LLLL", "CurrPosition = " + mAliyunIPlayer.getCurrentPosition());
                        mCurrentEditEffect.editTimeCompleted();
                        //要保证涂鸦永远在动图的上方，则需要每次添加动图时都把已经渲染的涂鸦remove掉，添加完动图后，再重新把涂鸦加上去
                        mCanvasController = mAliyunIEditor.obtainCanvasController(EditorActivity.this.getApplicationContext(), mGlSurfaceContainer.getWidth(), mGlSurfaceContainer.getHeight());
                        if (mCanvasController.hasCanvasPath()) {
                            mCanvasController.removeCanvas();
                            mCanvasController.resetPaintCanvas();
                        }
                    }
                }
            } else {
                playingPause();
                mCurrentEditEffect.showTextEdit();
            }

            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return super.onSingleTapUp(e);
        }

        @Override
        public void onShowPress(MotionEvent e) {
            Log.d("MOVE", "onShowPress");
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
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
            }

//            Log.d("MOVE", "onScroll" + " shouldDrag : " + shouldDrag
//                    + " x : " + mPosX + " y : " + mPosY + " dx : "
//                    + distanceX + " dy : " + distanceY);

            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.d("MOVE", "onLongPress");
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {
            Log.d("MOVE", "onFling");
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
//            Log.d("MOVE", "onDown" + " (" + e.getX() + " : " + e.getY()
//                    + ")");
            if (mCurrentEditEffect != null && mCurrentEditEffect.isPasterRemoved()) {
                mCurrentEditEffect = null;
            }

            if (mCurrentEditEffect != null) {
                shouldDrag = !mCurrentEditEffect.isEditCompleted()
                        && mCurrentEditEffect.contentContains(e.getX(), e.getY())
                        && mCurrentEditEffect.isVisibleInTime(mAliyunIPlayer.getCurrentPosition());
            } else {
                shouldDrag = false;
            }

            mPosX = 0;
            mPosY = 0;
            return false;
        }
    }

    StringBuilder mDurationText = new StringBuilder(5);

    private String convertDuration2Text(long duration) {
        mDurationText.delete(0, mDurationText.length());
        int sec = Math.round(((float) duration) / (1000 * 1000));// us -> s
        int min = (sec % 3600) / 60;
        sec = (sec % 60);
        //TODO:优化内存,不使用String.format
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
        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
                Common.copyAll(EditorActivity.this, resCopy);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
//                resCopy.setVisibility(View.GONE);
            }
        }.execute();
    }

    public AliyunIPlayer getPlayer() {
        return this.mAliyunIPlayer;
    }

    public void showMessage(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(id);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private OnDialogButtonClickListener mDialogButtonClickListener = new OnDialogButtonClickListener() {
        @Override
        public void onPositiveClickListener(int index) {

        }

        @Override
        public void onNegativeClickListener(int index) {
            UIEditorPage in = UIEditorPage.get(index);
            int count = mPasterContainer.getChildCount();
            switch (in) {
                case OVERLAY://清除所有动图
                    for (int i = count - 1; i >= 0; i--) {
                        View pv = mPasterContainer.getChildAt(i);
                        PasterUISimpleImpl uic = (PasterUISimpleImpl) pv.getTag();
                        if (uic != null && uic.mController.getPasterType() == EffectPaster.PASTER_TYPE_GIF) {
                            uic.removePaster();
                        }
                    }
                    break;
                case CAPTION:
                    for (int i = count - 1; i >= 0; i--) {
                        View pv = mPasterContainer.getChildAt(i);
                        PasterUISimpleImpl uic = (PasterUISimpleImpl) pv.getTag();
                        if (uic == null) {
                            return;
                        }
                        if (uic.mController.getPasterType() == EffectPaster.PASTER_TYPE_CAPTION
                                || uic.mController.getPasterType() == EffectPaster.PASTER_TYPE_TEXT) {
                            uic.removePaster();
                        }
                    }
                    break;
                default:
                    break;
            }
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
        if (!mAliyunIPlayer.isPlaying()) {
            playingResume();
        }
    }

    /**
     * 长按抬起手指需要暂停播放
     *
     * @param filter
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventAnimationFilterClickUp(LongClickUpAnimationFilter filter) {
        if (mAliyunIPlayer.isPlaying()) {
            playingPause();
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventFilterTabClick(FilterTabClick ft) {
        //切换到特效的tab需要暂停播放，切换到滤镜的tab需要恢复播放
        if (mAliyunIPlayer != null) {
            switch (ft.getPosition()) {
                case FilterTabClick.POSITION_ANIMATION_FILTER:
                    if (mAliyunIPlayer.isPlaying()) {
                        playingPause();
                    }
                    break;
                case FilterTabClick.POSITION_COLOR_FILTER:
                    if (!mAliyunIPlayer.isPlaying()) {
                        playingResume();
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
