package com.aliyun.svideo.recorder.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.common.global.AliyunTag;
import com.aliyun.svideo.base.BaseChooser;
import com.aliyun.svideo.base.UIConfigManager;
import com.aliyun.svideo.base.beauty.api.BeautyFactory;
import com.aliyun.svideo.base.beauty.api.BeautyInterface;
import com.aliyun.svideo.base.beauty.api.IAliyunBeautyInitCallback;
import com.aliyun.svideo.base.beauty.api.OnDefaultBeautyLevelChangeListener;
import com.aliyun.svideo.base.beauty.api.constant.BeautyConstant;
import com.aliyun.svideo.base.beauty.api.constant.BeautySDKType;
import com.aliyun.svideo.base.http.MusicFileBean;
import com.aliyun.svideo.base.utils.FastClickUtil;
import com.aliyun.svideo.base.widget.ProgressDialog;
import com.aliyun.svideo.base.widget.RecordTimelineView;
import com.aliyun.svideo.common.utils.PermissionUtils;
import com.aliyun.svideo.common.utils.ThreadUtils;
import com.aliyun.svideo.common.utils.image.ImageLoaderImpl;
import com.aliyun.svideo.common.utils.image.ImageLoaderOptions;
import com.aliyun.svideo.downloader.zipprocessor.DownloadFileUtils;
import com.aliyun.svideo.pro.record.R;
import com.aliyun.svideo.recorder.bean.AlivcMixBorderParam;
import com.aliyun.svideo.recorder.bean.AlivcRecordInputParam;
import com.aliyun.svideo.recorder.util.ActivityUtil;
import com.aliyun.svideo.recorder.util.OrientationDetector;
import com.aliyun.svideo.recorder.util.RecordCommon;
import com.aliyun.svideo.recorder.view.borad.DrawingBoard;
import com.aliyun.svideo.recorder.view.borad.comm.PaintConstants;
import com.aliyun.svideo.recorder.view.borad.comm.PaintViewCallBack;
import com.aliyun.svideo.recorder.view.borad.widget.ColorPickerDialog;
import com.aliyun.svideo.recorder.view.control.FlashType;
import com.aliyun.svideo.recorder.view.dialog.FilterEffectChooser;
import com.aliyun.svideo.recorder.view.dialog.GIfEffectChooser;
import com.aliyun.svideo.recorder.view.effects.filter.EffectInfo;
import com.aliyun.svideo.recorder.view.effects.filter.OnFilterItemClickListener;
import com.aliyun.svideo.recorder.view.effects.paster.PasterSelectListener;
import com.aliyun.svideo.recorder.view.music.MusicChooser;
import com.aliyun.svideo.recorder.view.music.MusicSelectListener;
import com.aliyun.svideosdk.common.AliyunErrorCode;
import com.aliyun.svideosdk.common.callback.recorder.OnAudioCallBack;
import com.aliyun.svideosdk.common.callback.recorder.OnFrameCallback;
import com.aliyun.svideosdk.common.callback.recorder.OnTextureIdCallback;
import com.aliyun.svideosdk.common.struct.effect.EffectBean;
import com.aliyun.svideosdk.common.struct.project.Source;
import com.aliyun.svideosdk.common.struct.common.AliyunBorderParam;
import com.aliyun.svideosdk.common.struct.common.AliyunLayoutParam;
import com.aliyun.svideosdk.common.struct.common.VideoDisplayMode;
import com.aliyun.svideosdk.common.struct.common.VideoQuality;
import com.aliyun.svideosdk.common.struct.effect.EffectFilter;
import com.aliyun.svideosdk.common.struct.effect.EffectImage;
import com.aliyun.svideosdk.common.struct.effect.EffectPaster;
import com.aliyun.svideosdk.common.struct.encoder.VideoCodecs;
import com.aliyun.svideosdk.common.struct.form.PreviewPasterForm;
import com.aliyun.svideosdk.common.struct.recorder.CameraType;
import com.aliyun.svideosdk.multirecorder.AliyunICameraCapture;
import com.aliyun.svideosdk.multirecorder.AliyunIVideoRecorder;
import com.aliyun.svideosdk.multirecorder.AliyunIViewCapture;
import com.aliyun.svideosdk.multirecorder.OnPictureCallback;
import com.aliyun.svideosdk.multirecorder.OnVideoRecordListener;
import com.aliyun.svideosdk.multirecorder.config.AliyunVideoRecorderConfig;
import com.aliyun.svideosdk.multirecorder.impl.AliyunMultiRecorderCreator;
import com.aliyun.svideosdk.recorder.AliyunIClipManager;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class AlivcSvideoViewRecordActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ViewRecordDemo";

    private final int PEN = PaintConstants.PEN_SIZE.SIZE_2;
    private final int PENCIL = PaintConstants.PEN_SIZE.SIZE_1;
    private DrawingBoard mDrawingBoard;
    private float mBoardWidthRatio = 1.0f;

    private static final boolean SHOW_CAMERA = true;
    private RelativeLayout mRecordGroup;
    private FrameLayout mCameraGroup;

    //录制区域原始高
    private int mOriginRecordGroupHeight = 0;
    private boolean mIsRecordGroupChanged = false;

    private float sizeRatio = 4.0f / 8.0f;
    private SurfaceView mCameraView;

    private CameraType mCameraType = CameraType.FRONT;
    private FlashType mFlashType = FlashType.OFF;

    private View mCameraCtrlView;
    private View mBtnGifEffect;
    private View mBtnFilterEffect;
    private View mBtnBeauty;
    private View mBtnTakePhoto;
    private View mBtnSwitchCamera;
    private ImageView mBtnFlashType;
    private View mBtnChangeRatio;
    private ImageView mBtnMusic;
    // 在编辑界面, 如果录制添加了背景音乐, 则不能使用音效特效
    private boolean mIsUseMusic = false;

    private AliyunICameraCapture mCameraCapture;
    private AliyunIViewCapture mViewCapture;

    private Button mRecordBtn;
    private Button mFinishBtn;
    private Button mDeleteBtn;

    private boolean mIsRecording = false;
    private boolean mIsDestroy = false;

    private AliyunIVideoRecorder mVideoRecorder;
    private AliyunIClipManager mClipManager;

    private AlivcRecordInputParam mInputParam;

    private RecordTimelineView mRecordTimeView;

    private Switch mMuteSwitch;
    private Switch mWatermarkSwitch;
    private Switch mDenoiseSwitch;
    private Switch mMixAudioSwitch;
    private Switch mAecSwitch;
    private TextView mRateText;
    private SeekBar mRateSeekBar;

    /**
     * 视频边框
     */
    private AliyunBorderParam mBorderParam;
    private BeautyInterface mBeautyInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alivc_recorder_activity_view_record);
        getData();
        initViews();
        initRecorder();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        doRecord(false);
        stopPreview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsDestroy = true;
        destroyRecorder();
        if (copyAssetsTask != null) {
            copyAssetsTask.cancel(true);
            copyAssetsTask = null;
        }
    }

    private int getOutputWidth() {
        return mInputParam.getVideoWidth();
    }

    private int getOutputHeight() {
        if (mRecordGroup.getWidth() > 0) {
            float ratio = mRecordGroup.getHeight() * 1.0f / mRecordGroup.getWidth();
            return (int) (mInputParam.getVideoWidth() * ratio);
        }
        return mInputParam.getVideoHeight();
    }

    private void startPreview() {
        mVideoRecorder.startPreview();
        if (orientationDetector != null && orientationDetector.canDetectOrientation()) {
            orientationDetector.enable();
        }
    }

    private void stopPreview() {
        mVideoRecorder.stopPreview();
        if (orientationDetector != null) {
            orientationDetector.disable();
        }
    }

    /**
     * 销毁录制
     */
    public void destroyRecorder() {
        if (mVideoRecorder != null) {
            mVideoRecorder.destroy();
            Log.i(TAG, "recorder destroy");
        }
        if (orientationDetector != null) {
            orientationDetector.setOrientationChangedListener(null);
        }
    }

    private View getRecordView() {
        return mDrawingBoard;
    }

    private void initViews() {
        mRecordGroup = findViewById(R.id.record_group);
        initBackground();
        initBoardView();

        mRecordBtn = findViewById(R.id.record_btn);
        mFinishBtn = findViewById(R.id.finish_btn);
        mDeleteBtn = findViewById(R.id.delete_btn);

        mMuteSwitch = findViewById(R.id.switch_record_mute);
        mMuteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mVideoRecorder != null) {
                    mVideoRecorder.setMute(isChecked);
                }
            }
        });

        mWatermarkSwitch = findViewById(R.id.switch_record_watermark);
        mWatermarkSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                handleWatermark(isChecked);
            }
        });

        mDenoiseSwitch = findViewById(R.id.switch_record_denoise);
        mDenoiseSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mVideoRecorder != null) {
                    mVideoRecorder.setMicDenoiseWeight(isChecked ? 20 : 0);
                }
            }
        });

        mMixAudioSwitch = findViewById(R.id.switch_record_mixaudio);
        mMixAudioSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mVideoRecorder != null) {
                    mVideoRecorder.setOpenMixAudioMode(isChecked);
                    mVideoRecorder.setMixAudioWeight(50, 100);
                }
            }
        });

        mAecSwitch = findViewById(R.id.switch_record_aec);
        mAecSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mVideoRecorder != null) {
                    mVideoRecorder.setOpenMicAEC(isChecked);
                }
            }
        });

        mRateSeekBar = findViewById(R.id.record_rate_seekbar);
        mRateText = findViewById(R.id.record_rate);
        mRateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRateText.setText(String.valueOf(progress / 10.0f));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mRecordTimeView = findViewById(R.id.record_timeline);
        mRecordTimeView.setColor(R.color.alivc_record_bg_timeview_duraton,
                                 R.color.alivc_record_bg_timeview_selected,
                                 R.color.alivc_common_white,
                                 R.color.alivc_record_bg_timeview);
        mRecordTimeView.setMaxDuration(mInputParam.getMaxDuration());
        mRecordTimeView.setMinDuration(mInputParam.getMinDuration());

        initCameraView();
    }

    private void initBackground() {
        int backgroundColor = mInputParam.getMixBackgroundColor();
        String backgroundImage = mInputParam.getMixBackgroundImagePath();
        if (!TextUtils.isEmpty(backgroundImage)) {
            int displayMode = mInputParam.getMixBackgroundImageMode();
            ImageView backgroundImageView = new ImageView(getContext());
            switch (displayMode) {
            case 0:
                backgroundImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                break;
            case 1:
                backgroundImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                break;
            case 2:
                backgroundImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                break;
            }
            backgroundImageView.setBackgroundColor(backgroundColor);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            mRecordGroup.addView(backgroundImageView, 0, layoutParams);
            new ImageLoaderImpl().loadImage(
                getContext(),
                "file://" + backgroundImage,
                new ImageLoaderOptions.Builder()
                .skipDiskCacheCache()
                .skipMemoryCache()
                .build())
            .into(backgroundImageView);
        }
    }

    private void initBoardView() {
        mDrawingBoard = findViewById(R.id.paint_view);
        mDrawingBoard.getLayoutParams().width = (int) (getResources().getDisplayMetrics().widthPixels * mBoardWidthRatio);
        mDrawingBoard.post(new Runnable() {
            @Override
            public void run() {
                mOriginRecordGroupHeight = mRecordGroup.getHeight();
                mVideoRecorder.updateVideoSize(getOutputWidth(), getOutputHeight());
                if (SHOW_CAMERA) {
                    float heightRatio = getOutputWidth() * sizeRatio / getOutputHeight();
                    mCameraCapture.updateLayout(
                        AliyunLayoutParam.builder()
                        .layoutLevel(2)
                        .centerX(1.0f - sizeRatio / 2)
                        .centerY(heightRatio / 2)
                        .widthRatio(sizeRatio)
                        .heightRatio(heightRatio)
                        .displayMode(VideoDisplayMode.SCALE)
                        .build());
                    mViewCapture.updateLayout(AliyunLayoutParam.builder()
                            .layoutLevel(1)
                            .centerX(mBoardWidthRatio / 2)
                            .centerY(0.5f)
                            .widthRatio(mBoardWidthRatio)
                            .heightRatio(1.0f)
                            .displayMode(VideoDisplayMode.FILL)
                            .build());
                }
            }
        });
        mDrawingBoard.setCallBack(new PaintViewCallBack() {
            @Override
            public void onHasDraw() {
            }

            @Override
            public void onTouchDown() {
            }
        });
        findViewById(R.id.ib_pencil).setOnClickListener(this);
        findViewById(R.id.ib_pen).setOnClickListener(this);
        findViewById(R.id.ib_rudder).setOnClickListener(this);
        findViewById(R.id.ib_color).setOnClickListener(this);
        findViewById(R.id.ib_left).setOnClickListener(this);
        findViewById(R.id.ib_right).setOnClickListener(this);
        findViewById(R.id.ib_clear).setOnClickListener(this);
    }

    private void initCameraView() {
        if (!SHOW_CAMERA) {
            return;
        }
        initCameraControlView();

        mCameraGroup = mRecordGroup.findViewById(R.id.camera_group);
        int size = (int) (getResources().getDisplayMetrics().widthPixels * sizeRatio);
        mCameraView = new SurfaceView(this);
        // 设置背景透明
        mCameraView.setZOrderOnTop(true);
        mCameraView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        mCameraGroup.addView(mCameraView, size, size);

        initOrientationDetector();
        // 摄像头特效素材数据加载
        setFaceTrackModePath();
        loadData();
    }

    private void initCameraControlView() {
        mCameraCtrlView = findViewById(R.id.camera_ctrl_panel);
        mCameraCtrlView.setVisibility(View.VISIBLE);
        //背景音乐
        mBtnMusic = mCameraCtrlView.findViewById(R.id.ctrl_icon_music);
        mBtnMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastClickUtil.isFastClick()) {
                    Log.w(TAG, "show gifEffectChooser fail: click frequently");
                    return;
                }
                showMusicSelector();
            }
        });
        //动图
        mBtnGifEffect = mCameraCtrlView.findViewById(R.id.ctrl_icon_gif);
        mBtnGifEffect.setVisibility(View.VISIBLE);
        mBtnGifEffect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastClickUtil.isFastClick()) {
                    Log.w(TAG, "show gifEffectChooser fail: click frequently");
                    return;
                }
                showGifEffectChooser();
            }
        });
        //滤镜
        mBtnFilterEffect = mCameraCtrlView.findViewById(R.id.ctrl_icon_filter);
        mBtnFilterEffect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastClickUtil.isFastClick()) {
                    Log.w(TAG, "show filterEffectChooser fail: click frequently");
                    return;
                }
                showFilterEffectChooser();
            }
        });
        //美颜
        mBtnBeauty = mCameraCtrlView.findViewById(R.id.ctrl_icon_beauty);
        mBtnBeauty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastClickUtil.isFastClick()) {
                    Log.w(TAG, "show filterEffectChooser fail: click frequently");
                    return;
                }
                if (mBeautyInterface != null) {
                    mBeautyInterface.showControllerView(getSupportFragmentManager(), null);
                }
            }
        });
        //拍照
        mBtnTakePhoto = mCameraCtrlView.findViewById(R.id.ctrl_icon_photo);
        mBtnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastClickUtil.isFastClick()) {
                    Log.w(TAG, "take photo fail: click frequently");
                    return;
                }
                mCameraCapture.snapshot(true, new OnPictureCallback() {
                    @Override
                    public void onPicture(Bitmap bitmap) {
                        saveBitmap(bitmap);
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AlivcSvideoViewRecordActivity.this, "拍照完成", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onPicture(byte[] data) {
                        // ignore
                    }
                });
            }
        });
        //切换摄像头
        mBtnSwitchCamera = mCameraCtrlView.findViewById(R.id.ctrl_icon_switch);
        mBtnSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastClickUtil.isFastClick()) {
                    Log.w(TAG, "take photo fail: click frequently");
                    return;
                }
                if (mCameraType == CameraType.FRONT) {
                    mCameraType = CameraType.BACK;
                    mFlashType = FlashType.OFF;
                } else {
                    mCameraType = CameraType.FRONT;
                }
                mCameraCapture.switchCamera();
                updateFlashTypeBtn();
            }
        });
        //闪光灯
        mBtnFlashType = mCameraCtrlView.findViewById(R.id.ctrl_icon_flash);
        mBtnFlashType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastClickUtil.isFastClick()) {
                    Log.w(TAG, "take photo fail: click frequently");
                    return;
                }
                if (mFlashType == FlashType.OFF) {
                    mFlashType = FlashType.TORCH;
                } else {
                    mFlashType = FlashType.OFF;
                }
                mCameraCapture.setLight(mFlashType == FlashType.TORCH
                                        ? com.aliyun.svideosdk.common.struct.recorder.FlashType.TORCH
                                        : com.aliyun.svideosdk.common.struct.recorder.FlashType.OFF);
                updateFlashTypeBtn();
            }
        });
        //录制区域大小变更
        mBtnChangeRatio = mCameraCtrlView.findViewById(R.id.ctrl_icon_ratio);
        mBtnChangeRatio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastClickUtil.isFastClick()) {
                    Log.w(TAG, "take photo fail: click frequently");
                    return;
                }
                mIsRecordGroupChanged = !mIsRecordGroupChanged;
                resizeCameraRatio();
            }
        });
        updateFlashTypeBtn();
    }

    private void updateFlashTypeBtn() {
        if (mCameraType == CameraType.FRONT) {
            mBtnFlashType.setClickable(false);
            // 前置摄像头状态, 闪光灯图标变灰
            Drawable drawable = ContextCompat.getDrawable(this, R.mipmap.aliyun_svideo_icon_magic_light_off);
            mBtnFlashType.setImageDrawable(drawable);
            mBtnFlashType.setColorFilter(ContextCompat.getColor(this, R.color.alivc_record_color_filter), PorterDuff.Mode.MULTIPLY);
        } else if (mCameraType == CameraType.BACK) {
            mBtnFlashType.setClickable(true);
            // 后置摄像头状态, 清除过滤器
            mBtnFlashType.clearColorFilter();
            switch (mFlashType) {
            case TORCH:
                mBtnFlashType.setSelected(true);
                mBtnFlashType.setActivated(false);
                UIConfigManager.setImageResourceConfig(mBtnFlashType, R.attr.lightImageOpen, R.mipmap.aliyun_svideo_icon_magic_light);
                break;
            case OFF:
                mBtnFlashType.setSelected(true);
                mBtnFlashType.setActivated(true);
                Drawable drawable = ContextCompat.getDrawable(this, R.mipmap.aliyun_svideo_icon_magic_light_off);
                mBtnFlashType.setImageDrawable(drawable);
                break;
            default:
                break;
            }
        }
    }

    private void resizeCameraRatio() {
        ViewGroup.LayoutParams layoutParams = mRecordGroup.getLayoutParams();
        layoutParams.height = mIsRecordGroupChanged ? mRecordGroup.getWidth() : mOriginRecordGroupHeight;
        mRecordGroup.setLayoutParams(layoutParams);
        mRecordGroup.post(new Runnable() {
            @Override
            public void run() {
                float heightRatio = getOutputWidth() * sizeRatio / getOutputHeight();
                mVideoRecorder.updateVideoSize(getOutputWidth(), getOutputHeight());
                //更新视频宽高后需要刷新布局
                mCameraCapture.updateLayout(
                    AliyunLayoutParam.builder()
                    .layoutLevel(2)
                    .centerX(1.0f - sizeRatio / 2)
                    .centerY(heightRatio / 2)
                    .widthRatio(sizeRatio)
                    .heightRatio(heightRatio)
                    .displayMode(VideoDisplayMode.SCALE)
                    .build());
                mViewCapture.updateLayout(AliyunLayoutParam.builder()
                        .layoutLevel(1)
                        .centerX(mBoardWidthRatio / 2)
                        .centerY(0.5f)
                        .widthRatio(mBoardWidthRatio)
                        .heightRatio(1.0f)
                        .displayMode(VideoDisplayMode.FILL)
                        .build());
            }
        });
    }

    private AsyncTask<Void, Void, Void> copyAssetsTask;

    private void loadData() {
        if (!PermissionUtils.checkPermissionsGroup(this, PermissionUtils.PERMISSION_STORAGE)) {
            //有存储权限的时候才去copy资源
            return;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mIsDestroy) {
                    return;
                }
                copyAssetsTask = new CopyAssetsTask(AlivcSvideoViewRecordActivity.this)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }, 700);
    }

    public class CopyAssetsTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<Activity> weakReference;
        ProgressDialog progressBar;

        CopyAssetsTask(Activity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Activity activity = weakReference.get();
            if (activity != null && !activity.isFinishing()) {
                progressBar = new ProgressDialog(activity);
                progressBar.setMessage(activity.getString(R.string.alivc_progress_content_text));
                progressBar.setCanceledOnTouchOutside(false);
                progressBar.setCancelable(false);
                progressBar.setProgressStyle(android.app.ProgressDialog.STYLE_SPINNER);
                progressBar.show();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Activity activity = weakReference.get();
            if (activity != null) {
                RecordCommon.copyAll(activity);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Activity activity = weakReference.get();
            if (activity != null && !activity.isFinishing()) {
                progressBar.dismiss();
                //资源复制完成之后设置一下人脸追踪，防止第一次人脸动图应用失败的问题
                setFaceTrackModePath();
            }

        }
    }

    private void setFaceTrackModePath() {
        ThreadUtils.runOnSubThread(new Runnable() {
            @Override
            public void run() {
                String modelPath = getExternalFilesDir("") + File.separator + RecordCommon.QU_NAME + File.separator + "model";
                Log.d(TAG, "setFaceTrackModePath " + modelPath);
                if (mCameraCapture != null) {
                    mCameraCapture.needFaceTrackInternal(true);
                    if (!new File(modelPath).exists()) {
                        Log.e(TAG, "setFaceTrackModePath fail, model path invalid");
                    }
                    mCameraCapture.setFaceTrackInternalModelPath(modelPath);
                    handleWatermark(mWatermarkSwitch.isChecked());
                }
            }
        });

    }

    private boolean checkFragmentAdded(Fragment fragment, String tag) {
        if (fragment.isAdded()) {
            return true;
        }
        return getSupportFragmentManager().findFragmentByTag(tag) != null;
    }

    private Context getContext() {
        return this;
    }

    /********** 背景音乐 start *************/
    private static final String TAG_MUSIC_CHOOSER = "music";
    private MusicChooser musicChooseView;
    private EffectBean effectMusic;

    private int getMaxRecordTime() {
        return mInputParam.getMaxDuration();
    }

    public void setMusicIconId(@DrawableRes int id) {
        mBtnMusic.setImageResource(id);
    }

    public void setMusicIcon(String icon) {
        new ImageLoaderImpl()
                .loadImage(getContext(), icon, new ImageLoaderOptions.Builder()
                        .circle()
                        .error(com.aliyun.svideo.record.R.mipmap.aliyun_svideo_music)
                        .crossFade()
                        .build())
                .into(mBtnMusic);
    }

    private void showMusicSelector() {
        if (musicChooseView == null) {
            musicChooseView = new MusicChooser();
            musicChooseView.setRecordTime(getMaxRecordTime());
            musicChooseView.setMusicSelectListener(new MusicSelectListener() {

                @Override
                public void onMusicSelect(MusicFileBean musicFileBean, long startTime) {
                    if (musicFileBean != null) {
                        effectMusic = new EffectBean();
                        effectMusic.setPath(musicFileBean.getPath());
                        Source source = new Source(musicFileBean.getPath());
                        source.setId(musicFileBean.getMusicId());
                        effectMusic.setSource(source);
                        effectMusic.setStartTime(startTime);
                        int during = getMaxRecordTime();
                        if (during > musicFileBean.getDuration()) {
                            during = musicFileBean.getDuration() - 100;
                        }
                        effectMusic.setDuration(during);
                        //如果音乐地址或者图片地址为空则使用默认图标
                        if (TextUtils.isEmpty(musicFileBean.getImage()) || TextUtils.isEmpty(musicFileBean.getPath())) {
                            setMusicIconId(com.aliyun.svideo.record.R.mipmap.aliyun_svideo_music);
                        } else {
                            setMusicIcon(musicFileBean.getImage());
                        }
                    } else {
                        setMusicIconId(com.aliyun.svideo.record.R.mipmap.aliyun_svideo_music);
                    }
                }
            });

            musicChooseView.setDismissListener(new BaseChooser.DialogVisibleListener() {
                @Override
                public void onDialogDismiss() {
                    if (effectMusic != null) {
                        String path = effectMusic.getSource() != null ? effectMusic.getSource().getPath() : null;
                        if (TextUtils.isEmpty(path) || !new File(path).exists()) {
                            mIsUseMusic = false;
                            mVideoRecorder.removeMusic();
                        } else {
                            mIsUseMusic = true;
                            mVideoRecorder.setMusic(path, effectMusic.getStartTime(), effectMusic.getDuration());
                        }
                        Log.i(TAG, "set background music path :" + path);
                    }
                }

                @Override
                public void onDialogShow() {
                }
            });
        }
        musicChooseView.show(getSupportFragmentManager(), TAG_MUSIC_CHOOSER);
    }
    /********** 背景音乐 end **********/

    /********** 动图 start **********/
    private static final String FRAGMENT_TAG_GIF_EFFECT = "gif";
    private GIfEffectChooser gifEffectChooser;
    private EffectPaster effectPaster;
    private OrientationDetector orientationDetector;

    private void initOrientationDetector() {
        orientationDetector = new OrientationDetector(getApplicationContext());
        orientationDetector.setOrientationChangedListener(new OrientationDetector.OrientationChangedListener() {
            @Override
            public void onOrientationChanged() {
                int mRotation = getCameraRotation();
                mCameraCapture.setRotation(mRotation);
                if (mBeautyInterface != null) {
                    mBeautyInterface.setDeviceOrientation(0, ActivityUtil.getDegrees(AlivcSvideoViewRecordActivity.this));
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

        if (Camera.getNumberOfCameras() > mCameraType.getType()) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraType.getType(), cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                if (rotation != 0) {
                    rotation = 360 - rotation;
                }
            }
        }
        return rotation;
    }

    private void showGifEffectChooser() {
        if (gifEffectChooser == null) {
            gifEffectChooser = new GIfEffectChooser();
            gifEffectChooser.setPasterSelectListener(new PasterSelectListener() {
                @Override
                public void onPasterSelected(PreviewPasterForm imvForm) {
                    String path;
                    if (imvForm.getId() == 150) {
                        //id=150的动图为自带动图
                        path = imvForm.getPath();
                    } else {
                        path = DownloadFileUtils.getAssetPackageDir(
                                   AlivcSvideoViewRecordActivity.this,
                                   imvForm.getName(), imvForm.getId()).getAbsolutePath();
                    }
                    addGifEffect(new Source(String.valueOf(imvForm.getId()), path));
                }

                @Override
                public void onSelectPasterDownloadFinish(String path) {
                }
            });
        }
        if (checkFragmentAdded(gifEffectChooser, FRAGMENT_TAG_GIF_EFFECT)) {
            Log.w(TAG, "show gifEffectChooser fail: already added");
            return;
        }
        gifEffectChooser.show(getSupportFragmentManager(), FRAGMENT_TAG_GIF_EFFECT);
    }

    private void addGifEffect(Source source) {
        if (effectPaster != null) {
            //删除旧的动图
            mCameraCapture.removePaster(effectPaster);
        }
        effectPaster = new EffectPaster(source);
        //添加新的动图
        mCameraCapture.addPaster(effectPaster);
    }

    /********** 动图 end **********/

    /********** 滤镜 start **********/
    private static final String FRAGMENT_TAG_FILTER_EFFECT = "filter";
    private FilterEffectChooser filterEffectChooser;
    private int currentFilterPosition;

    private void showFilterEffectChooser() {
        if (filterEffectChooser == null) {
            filterEffectChooser = new FilterEffectChooser();
        }
        if (filterEffectChooser.isAdded()) {
            return;
        }
        filterEffectChooser.setOnFilterItemClickListener(new OnFilterItemClickListener() {
            @Override
            public void onItemClick(EffectInfo effectInfo, int index) {
                if (effectInfo != null) {
                    if (index == 0) {
                        removeFilterEffect();
                    } else {
                        addFilterEffect(effectInfo.getSource());
                    }
                }
                currentFilterPosition = index;
            }
        });
        if (checkFragmentAdded(filterEffectChooser, FRAGMENT_TAG_FILTER_EFFECT)) {
            Log.w(TAG, "show filterEffectChooser fail: already added");
            return;
        }
        filterEffectChooser.setFilterPosition(currentFilterPosition);
        filterEffectChooser.show(getSupportFragmentManager(), FRAGMENT_TAG_FILTER_EFFECT);
    }

    private void addFilterEffect(Source source) {
        mCameraCapture.applyFilter(new EffectFilter.Builder().source(source).build());
    }

    private void removeFilterEffect() {
        mCameraCapture.removeFilter();
    }

    /********** 滤镜 end **********/

    /********** 水印 start **********/
    private EffectImage effectWatermark;

    private void handleWatermark(boolean toAdd) {
        if (effectWatermark == null) {
            String logo = getExternalFilesDir("") + "/AliyunEditorDemo/tail/logo.png";
            effectWatermark = new EffectImage(logo);
            effectWatermark.setWidthRatio(0.1f);
            effectWatermark.setHeightRatio(0.1f);
            effectWatermark.setXRadio(1.0f - effectWatermark.getWidthRatio() / 2);
            effectWatermark.setYRatio(1.0f - effectWatermark.getHeightRatio() / 2);
        }
        if (mCameraCapture != null) {
            mVideoRecorder.removeWaterMark(effectWatermark);
        }
        if (toAdd) {
            mVideoRecorder.addWaterMark(effectWatermark);
        }
    }

    /********** 水印 end **********/


    /**
     * 渲染方式
     */
    private BeautySDKType mRenderingMode = BeautySDKType.QUEEN;


    private void initCameraRecorder() {
        if (!SHOW_CAMERA) {
            return;
        }
        mCameraCapture = mVideoRecorder.getVideoCapture().addCameraCapture(
                             AliyunLayoutParam.builder()
                             .layoutLevel(2)
                             .centerX(1.0f - sizeRatio / 2)
                             .centerY(sizeRatio / 2)
                             .widthRatio(sizeRatio)
                             .heightRatio(sizeRatio)
                             .displayMode(VideoDisplayMode.SCALE)
                             .build()
                         );
        mCameraCapture.setBeautyStatus(false);
        mCameraCapture.setDisplayView(mCameraView);
        mCameraCapture.setCamera(mCameraType);
        mCameraCapture.setOutputFlip(mInputParam.isUseFlip());
        if (mBorderParam != null) {
            mCameraCapture.setBorderParam(mBorderParam);
        }
        mCameraCapture.setOnFrameCallback(new OnFrameCallback() {
            @Override
            public void onFrameBack(byte[] bytes, int width, int height, Camera.CameraInfo info) {
                //原始数据回调 NV21,这里获取原始数据主要是为了faceUnity高级美颜使用
                if (mBeautyInterface != null) {
                    mBeautyInterface.onFrameBack(bytes, width, height, info);
                }
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
        mCameraCapture.setOnTextureIdCallback(new OnTextureIdCallback() {
            @Override
            public int onTextureIdBack(int textureId, int textureWidth, int textureHeight, float[] matrix) {
                //******************************** start ******************************************
                //以下为faceunity高级美颜接入代码，如果未集成faceunity，可以把此回调方法注释掉。以避免产生额外的license校验请求，影响您的产品性能。
                //这块代码会影响到标准版的faceUnity功能 改动的时候要关联app gradle 一起改动
                if (mBeautyInterface != null) {
                    return mBeautyInterface.onTextureIdBack(textureId, textureWidth, textureHeight, matrix, mCameraType.getType());
                }
                //******************************** end ********************************************
                return textureId;
            }

            @Override
            public int onScaledIdBack(int scaledId, int textureWidth, int textureHeight, float[] matrix) {
                return scaledId;
            }

            @Override
            public void onTextureDestroyed() {
                // sdk3.7.8改动, 自定义渲染（第三方渲染）销毁gl资源，以前GLSurfaceView时可以通过GLSurfaceView.queueEvent来做，
                // 现在增加了一个gl资源销毁的回调，需要统一在这里面做。
                if (mBeautyInterface != null) {
                    mBeautyInterface.release();
                    mBeautyInterface = null;
                }
            }
        });
        mCameraCapture.setFaceTrackInternalMaxFaceCount(2);
        //初始化美颜
        final BeautyInterface beautyInterface = BeautyFactory.createBeauty(mRenderingMode, getContext());
        if (beautyInterface != null) {
            beautyInterface.init(getContext(), new IAliyunBeautyInitCallback() {
                @Override
                public void onInit(int code) {
                    if (code == BeautyConstant.BEAUTY_INIT_SUCCEED) {
                        if (mRenderingMode == BeautySDKType.DEFAULT && mCameraCapture != null) {
                            mCameraCapture.setBeautyStatus(true);
                            beautyInterface.addDefaultBeautyLevelChangeListener(new OnDefaultBeautyLevelChangeListener() {
                                @Override
                                public void onDefaultBeautyLevelChange(int level) {
                                    if (mCameraCapture != null) {
                                        mCameraCapture.setBeautyLevel(level);
                                    }
                                }
                            });
                        }
                        beautyInterface.initParams();
                        mBeautyInterface = beautyInterface;

                    }
                }
            });
        }

    }


    private void saveBitmap(Bitmap bitmap) {
        FileOutputStream fos;
        try {
            String rootPath = getSaveDir();
            File file = new File(new File(rootPath), "take_photo.png");
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initRecorder() {
        AliyunVideoRecorderConfig config = AliyunVideoRecorderConfig.builder()
                                           //*** 必需 start ***
                                           .videoWidth(getOutputWidth())
                                           .videoHeight(getOutputHeight())
                                           .outputPath(getSaveDir() + File.separator + System.currentTimeMillis() + ".mp4")
                                           //*** 必需 end ***

                                           //编码质量因子，6~51，默认23
                                           .crf(23)
                                           //编码帧率，1~120，默认 30
                                           .encoderFps(30)
                                           //录制采集帧率，1~120，默认 30
                                           .fps(30)
                                           //编码器类型，H264_HARDWARE、H264_SOFT_OPENH264、H264_SOFT_FFMPEG，默认 H264_HARDWARE
                                           .videoCodecs(mInputParam.getVideoCodec())
                                           // 录制视频质量，枚举值 VideoQuality，默认 HD
                                           .videoQuality(mInputParam.getVideoQuality())
                                           // 录制视频码率
//                .videoBitrate()
                                           // 设置Gop大小
                                           .gop(mInputParam.getGop())
                                           .build();

        mVideoRecorder = AliyunMultiRecorderCreator.getVideoRecorderInstance(this, config);

        // 局部录制
        mViewCapture = mVideoRecorder.getVideoCapture().addViewCapture(
            AliyunLayoutParam.builder()
            .layoutLevel(1)
            .centerX(mBoardWidthRatio / 2)
            .centerY(0.5f)
            .widthRatio(mBoardWidthRatio)
            .heightRatio(1.0f)
            .displayMode(VideoDisplayMode.FILL)
            .build(),
            getRecordView()
        );

        //摄像头录制
        initCameraRecorder();

        //prepare
        mVideoRecorder.prepare();

        mVideoRecorder.setMute(mMuteSwitch.isChecked());
        mVideoRecorder.setIsAutoClearClipVideos(mInputParam.isAutoClearTemp());

        mClipManager = mVideoRecorder.getClipManager();
        // 设置最小录制时长（总录制时长，不是单个片段的时长），单位ms
        mClipManager.setMinDuration(mInputParam.getMinDuration());
        // 设置最大录制时长（总录制时长，不是单个片段的时长），单位ms
        mClipManager.setMaxDuration(mInputParam.getMaxDuration());

        // 设置背景
        if (!TextUtils.isEmpty(mInputParam.getMixBackgroundImagePath())) {
            mVideoRecorder.setBackgroundColor(mInputParam.getMixBackgroundColor());
            mVideoRecorder.setBackgroundDisplayMode(mInputParam.getMixBackgroundImageMode());
            mVideoRecorder.setBackgroundImage(mInputParam.getMixBackgroundImagePath());
        }

        mVideoRecorder.setOnRecordListener(new OnVideoRecordListener() {
            @Override
            public void onClipComplete(final boolean validClip, final long clipDuration) {
                Log.d(TAG, "onRecoderCallBack -> onComplete : validClip = " + validClip + " , clipDuration = " + clipDuration);
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (validClip) {
                            mRecordTimeView.setDuration((int) clipDuration);
                            mRecordTimeView.clipComplete();
                        } else {
                            mRecordTimeView.setDuration(0);
                        }
                        if (isRecording()) {
                            // 非主动停止录制
                            doRecord(false);
                        }
                        updateBtnState();
                        if (validClip && mClipManager.getDuration() >= mClipManager.getMaxDuration()) {
                            // 录制达到最大限时而停止录制，此时跳转编辑页面
                            finishRecord();
                        }
                    }
                });
            }

            @Override
            public void onFinish(final String outputPath) {
                Log.d(TAG, "onRecoderCallBack -> onFinish : outputPath = " + outputPath);
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final int duration = mClipManager.getDuration();
                        jumpEditPage(outputPath, duration);
                    }
                });
            }

            @Override
            public void onProgress(final long duration) {
                Log.d(TAG, "onRecoderCallBack -> onProgress : progress = " + duration);
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //设置录制进度
                        mRecordTimeView.setDuration((int) duration);
                    }
                });
            }

            @Override
            public void onMaxDuration() {
                Log.d(TAG, "onRecoderCallBack -> onMaxDuration");
            }

            @Override
            public void onError(int i) {
                Log.d(TAG, "onRecoderCallBack -> onError : errorCode = " + i);
            }

            @Override
            public void onInitReady() {

            }
        });

        mVideoRecorder.setOnAudioCallback(new OnAudioCallBack() {
            @Override
            public void onAudioDataBack(byte[] bytes, int i) {
                Log.d(TAG, "audioDataBack -> onAudioDataBack");
            }

            @Override
            public void onAudioDataBackInPreview(byte[] data, int length) {
//                Logger.logD("audioDataBack -> onAudioDataBackInPreview");
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ib_pencil) {// 设置为铅笔
            selectPenType(PENCIL);
        } else if (id == R.id.ib_pen) {//设置为钢笔
            selectPenType(PEN);
        } else if (id == R.id.ib_rudder) {//选择橡皮后进行擦除
            selectRudder();
        } else if (id == R.id.ib_color) {
            selectColors();
        } else if (id == R.id.ib_left) {
            revokeLeft();
        } else if (id == R.id.ib_right) {
            revokeRight();
        } else if (id == R.id.ib_clear) {
            clear();
        }
    }

    private void selectPenType(int penType) {
        mDrawingBoard.setPenSize(penType);
        mDrawingBoard.setPenType(PaintConstants.PEN_TYPE.PLAIN_PEN);
    }

    private void selectRudder() {
        mDrawingBoard.setPenType(PaintConstants.PEN_TYPE.ERASER);
    }

    private void selectColors() {
        new ColorPickerDialog(this, new ColorPickerDialog.OnColorChangedListener() {
            @Override
            public void colorChanged(int color) {
                mDrawingBoard.setPenColor(color);
            }
        }, Color.BLACK).show();
    }

    private void revokeLeft() {
        mDrawingBoard.undo();
    }

    private void revokeRight() {
        mDrawingBoard.redo();
    }

    private void clear() {
        if (mDrawingBoard.canRedo() || mDrawingBoard.canUndo()) {
            mDrawingBoard.clearAll(true);
            mDrawingBoard.onHasDraw();
        }
    }

    public void btnClickRecord(View view) {
        doRecord(!isRecording());
    }

    public void btnFinishRecord(View view) {
        finishRecord();
    }

    public void btnDeleteRecord(View view) {
        mRecordTimeView.deleteLast();
        mClipManager.deletePart();
        updateBtnState();
    }

    private void finishRecord() {
        if (mInputParam.isAutoClearTemp()) {
            new FinishRecodingTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            // 不合成分段视频，直接跳转编辑模块
            jumpEditPage(mVideoRecorder.finishRecordingForEdit());
        }
    }

    /**
     * 录制结束的AsyncTask
     */
    public class FinishRecodingTask extends AsyncTask<Void, Integer, Integer> {

        private WeakReference<Activity> weakReference;
        ProgressDialog progressBar;

        FinishRecodingTask(Activity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Activity activity = weakReference.get();
            if (activity != null && !activity.isFinishing()) {
                progressBar = new ProgressDialog(getContext());
                progressBar.setMessage(getResources().getString(com.aliyun.svideo.record.R.string.alivc_recorder_record_create_video));
                progressBar.setCanceledOnTouchOutside(false);
                progressBar.setCancelable(false);
                progressBar.setProgressStyle(android.app.ProgressDialog.STYLE_SPINNER);
                progressBar.show();
            }
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            if (mVideoRecorder == null) {
                return -1;
            }
            return mVideoRecorder.finishRecording();
        }

        @Override
        protected void onPostExecute(Integer code) {
            super.onPostExecute(code);
            Activity activity = weakReference.get();
            if (activity != null && !activity.isFinishing()) {
                progressBar.dismiss();
                progressBar = null;
            }
        }
    }

    private boolean isRecording() {
        return mIsRecording;
    }

    private void doRecord(boolean start) {
        if (start) {
            if (mClipManager.getDuration() >= mClipManager.getMaxDuration()) {
                Toast.makeText(this, "录制已达最大时长", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        mIsRecording = start;
        if (isRecording()) {
            mVideoRecorder.setRate(mRateSeekBar.getProgress() / 10.f);
            mVideoRecorder.setMute(mMuteSwitch.isChecked());
            if (mVideoRecorder.startRecording() == AliyunErrorCode.ALIVC_COMMON_RETURN_SUCCESS) {
                mFinishBtn.setVisibility(View.GONE);
                mDeleteBtn.setVisibility(View.GONE);
                if (SHOW_CAMERA) {
                    mCameraCtrlView.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(this, "启动录制失败", Toast.LENGTH_SHORT).show();
                mIsRecording = false;
            }
        } else {
            mVideoRecorder.stopRecording();
            if (SHOW_CAMERA) {
                mCameraCtrlView.setVisibility(View.VISIBLE);
            }
        }
        mRecordBtn.setText(isRecording() ? "停止录制" : "开启录制");
    }

    private void updateBtnState() {
        mDeleteBtn.setVisibility(mClipManager.getDuration() > 0 ? View.VISIBLE : View.GONE);
        if (SHOW_CAMERA) {
            if (mClipManager.getDuration() > 0) {
                // 已开始录制，有些控件不可用，如调整大小、修改背景音乐
                mBtnChangeRatio.setVisibility(View.GONE);
                mBtnMusic.setClickable(false);
                mBtnMusic.setColorFilter(ContextCompat.getColor(getContext(),
                        com.aliyun.svideo.record.R.color.alivc_record_color_filter), PorterDuff.Mode.MULTIPLY);
            } else {
                mBtnChangeRatio.setVisibility(View.VISIBLE);
                mBtnMusic.setClickable(true);
                mBtnMusic.clearColorFilter();
            }
        }
        mFinishBtn.setVisibility(mClipManager.getDuration() >= mClipManager.getMinDuration() ? View.VISIBLE : View.GONE);
    }

    private String getSaveDir() {
        String saveDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ViewRecord";
        File dirFile = new File(saveDir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        return saveDir;
    }

    /**
     * 获取上个页面的传参
     */
    private void getData() {
        Intent intent = getIntent();
        //获取录制输入参数
        AlivcRecordInputParam.Builder builder = new AlivcRecordInputParam.Builder()
        .setResolutionMode(intent.getIntExtra(AlivcRecordInputParam.INTENT_KEY_RESOLUTION_MODE, AlivcRecordInputParam.RESOLUTION_720P))
        .setRatioMode(intent.getIntExtra(AlivcRecordInputParam.INTENT_KEY_RATION_MODE, AlivcRecordInputParam.RATIO_MODE_9_16))
        .setMaxDuration(intent.getIntExtra(AlivcRecordInputParam.INTENT_KEY_MAX_DURATION, AlivcRecordInputParam.DEFAULT_VALUE_MAX_DURATION))
        .setMinDuration(intent.getIntExtra(AlivcRecordInputParam.INTENT_KEY_MIN_DURATION, AlivcRecordInputParam.DEFAULT_VALUE_MIN_DURATION))
        .setGop(intent.getIntExtra(AlivcRecordInputParam.INTENT_KEY_GOP, AlivcRecordInputParam.DEFAULT_VALUE_GOP))
        .setFrame(intent.getIntExtra(AlivcRecordInputParam.INTENT_KEY_FRAME, AlivcRecordInputParam.DEFAULT_VALUE_FRAME))
        .setVideoOutputPath(intent.getStringExtra(AlivcRecordInputParam.INTENT_KEY_VIDEO_OUTPUT_PATH))
        .setIsUseFlip(intent.getBooleanExtra(AlivcRecordInputParam.INTENT_KEY_RECORD_FLIP, false))
        .setSvideoRace(intent.getBooleanExtra(AlivcRecordInputParam.INTENT_KEY_IS_SVIDEO_QUEEN, false))
        .setIsAutoClearTemp(intent.getBooleanExtra(AlivcRecordInputParam.INTENT_KEY_IS_AUTO_CLEAR, false));

        VideoQuality videoQuality = (VideoQuality) intent.getSerializableExtra(AlivcRecordInputParam.INTENT_KEY_QUALITY);
        if (videoQuality == null) {
            videoQuality = VideoQuality.HD;
        }
        builder.setVideoQuality(videoQuality);

        VideoCodecs videoCodec = (VideoCodecs) intent.getSerializableExtra(AlivcRecordInputParam.INTENT_KEY_CODEC);
        if (videoCodec == null) {
            videoCodec = VideoCodecs.H264_HARDWARE;
        }
        builder.setVideoCodec(videoCodec);

        BeautySDKType renderingMode = (BeautySDKType) intent.getSerializableExtra(AlivcRecordInputParam.INTENT_KEY_VIDEO_RENDERING_MODE);
        if (renderingMode == null) {
            renderingMode = BeautySDKType.FACEUNITY;
        }
        builder.setVideoRenderingMode(renderingMode);

        boolean watermark = intent.getBooleanExtra(AlivcRecordInputParam.INTENT_KEY_WATER_MARK, false);
        builder.setWaterMark(watermark);

        // 设置背景
        if (intent.hasExtra(AlivcRecordInputParam.INTENT_KEY_MIX_BACKGROUND_COLOR)) {
            builder.setMixBackgroundColor(intent.getIntExtra(AlivcRecordInputParam.INTENT_KEY_MIX_BACKGROUND_COLOR, Color.BLACK));
        }
        if (intent.hasExtra(AlivcRecordInputParam.INTENT_KEY_MIX_BACKGROUND_IMAGE_PATH)) {
            builder.setMixBackgroundImagePath(intent.getStringExtra(AlivcRecordInputParam.INTENT_KEY_MIX_BACKGROUND_IMAGE_PATH));
        }
        if (intent.hasExtra(AlivcRecordInputParam.INTENT_KEY_MIX_BACKGROUND_IMAGE_MODE)) {
            builder.setMixBackgroundImageMode(intent.getIntExtra(AlivcRecordInputParam.INTENT_KEY_MIX_BACKGROUND_IMAGE_MODE, 0));
        }
        mInputParam = builder.build();
        AlivcMixBorderParam borderParam = (AlivcMixBorderParam) getIntent().getSerializableExtra(AlivcRecordInputParam.INTENT_KEY_MIX_BORDER_PARAM_RECORD);
        if (borderParam != null) {
            mBorderParam =
                    new AliyunBorderParam.Builder()
                            .borderColor(borderParam.getBorderColor())
                            .borderWidth(borderParam.getBorderWidth())
                            // 兼容旧的合拍
                            .corner(borderParam.getCornerRadius())
                            .build();
        }
        mRenderingMode = mInputParam.getmRenderingMode();
        if (!TextUtils.isEmpty(mInputParam.getMixBackgroundImagePath())) {
            // 设置背景时，局部录屏大小故意设置小点，否则看不到背景图，不方便测试
            mBoardWidthRatio = 0.6f;
        }
    }

    private void jumpEditPage(final String path, int duration) {
        Intent intent = new Intent();
        com.aliyun.svideo.media.MediaInfo mediaInfo = new com.aliyun.svideo.media.MediaInfo();
        mediaInfo.filePath = path;
        mediaInfo.startTime = 0;
        mediaInfo.mimeType = "video";
        mediaInfo.duration = duration;
        ArrayList<com.aliyun.svideo.media.MediaInfo> infoList = new ArrayList<>();
        infoList.add(mediaInfo);
        intent.putParcelableArrayListExtra("mediaInfos", infoList);
        jumpEditPage(intent);
    }

    private void jumpEditPage(Uri projectUri) {
        Intent intent = new Intent();
        intent.putExtra("OutputWidth", getOutputWidth());
        intent.putExtra("OutputHeight", getOutputHeight());
        intent.putExtra("draftPath", projectUri.getPath());
        jumpEditPage(intent);
    }

    /**
     * 录制完成，跳转编辑页面
     */
    private void jumpEditPage(Intent intent) {
        intent.setClassName(AlivcSvideoViewRecordActivity.this, "com.aliyun.svideo.editor.editor.EditorActivity");
        intent.putExtra("mFrame", mInputParam.getFrame());
        // AlivcEditInputParam.RATIO_MODE_ORIGINAL
        intent.putExtra("mRatioMode", 3);
        intent.putExtra("mGop", mInputParam.getGop());
        intent.putExtra("mVideoQuality", mInputParam.getVideoQuality());
        intent.putExtra("mResolutionMode", mInputParam.getResolutionMode());
        intent.putExtra("mVideoCodec", mInputParam.getVideoCodec());
        intent.putExtra("hasWaterMark", mInputParam.hasWaterMark());
        intent.putExtra("canReplaceMusic", mIsUseMusic);
        AlivcSvideoViewRecordActivity.this.startActivity(intent);
    }

    /**
     * 开启录制
     *
     * @param context          上下文
     * @param recordInputParam 录制输入参数
     */
    public static void startRecord(Context context, AlivcRecordInputParam recordInputParam) {
        Intent intent = new Intent(context, AlivcSvideoViewRecordActivity.class);
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_RESOLUTION_MODE, recordInputParam.getResolutionMode());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_MAX_DURATION, recordInputParam.getMaxDuration());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_MIN_DURATION, recordInputParam.getMinDuration());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_RATION_MODE, recordInputParam.getRatioMode());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_GOP, recordInputParam.getGop());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_FRAME, recordInputParam.getFrame());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_QUALITY, recordInputParam.getVideoQuality());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_CODEC, recordInputParam.getVideoCodec());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_VIDEO_OUTPUT_PATH, recordInputParam.getVideoOutputPath());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_VIDEO_RENDERING_MODE, recordInputParam.getmRenderingMode());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_RECORD_FLIP, recordInputParam.isUseFlip());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_IS_SVIDEO_QUEEN, recordInputParam.isSvideoRace());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_IS_AUTO_CLEAR, recordInputParam.isAutoClearTemp());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_MIX_BACKGROUND_COLOR, recordInputParam.getMixBackgroundColor());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_MIX_BACKGROUND_IMAGE_PATH, recordInputParam.getMixBackgroundImagePath());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_MIX_BACKGROUND_IMAGE_MODE, recordInputParam.getMixBackgroundImageMode());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_MIX_BORDER_PARAM_RECORD, recordInputParam.getMixBorderParam());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_VIDEO_RENDERING_MODE, recordInputParam.getmRenderingMode());
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_WATER_MARK, recordInputParam.hasWaterMark());
        context.startActivity(intent);
    }
}
