package com.aliyun.svideo.recorder.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.aliyun.common.utils.MySystemParams;
import com.aliyun.common.utils.StorageUtils;
import com.aliyun.svideo.base.beauty.api.constant.BeautySDKType;
import com.aliyun.svideo.base.http.MusicFileBean;
import com.aliyun.svideo.base.widget.ProgressDialog;
import com.aliyun.svideo.common.utils.PermissionUtils;
import com.aliyun.svideo.common.utils.ThreadUtils;
import com.aliyun.svideo.common.utils.ToastUtils;
import com.aliyun.svideo.common.utils.UriUtils;
import com.aliyun.svideo.media.MediaInfo;
import com.aliyun.svideo.record.R;
import com.aliyun.svideo.recorder.bean.AlivcRecordInputParam;
import com.aliyun.svideo.recorder.bean.VideoDisplayParam;
import com.aliyun.svideo.recorder.mixrecorder.AlivcRecorder;
import com.aliyun.svideo.recorder.mixrecorder.AlivcRecorderFactory;
import com.aliyun.svideo.recorder.util.FixedToastUtils;
import com.aliyun.svideo.recorder.util.NotchScreenUtil;
import com.aliyun.svideo.recorder.util.RecordCommon;
import com.aliyun.svideo.recorder.util.voice.PhoneStateManger;
import com.aliyun.svideo.recorder.view.AliyunSVideoRecordView;
import com.aliyun.svideo.recorder.view.music.MusicSelectListener;
import com.aliyun.svideosdk.common.struct.common.VideoQuality;
import com.aliyun.svideosdk.common.struct.encoder.VideoCodecs;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * 新版本(> 3.6.5之后)录制模块的实现类, 主要是为了承载 AliyunSvideoRecordView
 */
public class AlivcSvideoRecordActivity extends AppCompatActivity {

    private AliyunSVideoRecordView mVideoRecordView;
    private AlivcRecordInputParam mInputParam;
    private static final int REQUEST_CODE_PLAY = 2002;
    /**
     * 录制过程中是否使用了音乐
     */
    private boolean isUseMusic;
    /**
     * 判断是否电话状态
     * true: 响铃, 通话
     * false: 挂断
     */
    private boolean isCalling = false;
    /**
     * 权限申请
     */
    String[] permission = {
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private Toast phoningToast;
    private PhoneStateManger phoneStateManger;

    /**
     * 判断是编辑模块进入还是通过社区模块的编辑功能进入
     */
    private static final String INTENT_PARAM_KEY_ENTRANCE = "entrance";

    /**
     * 判断是否有音乐, 如果有音乐, 编辑界面不能使用音效
     */
    private static final String INTENT_PARAM_KEY_HAS_MUSIC = "hasRecordMusic";
    /**
     * 判断是编辑模块进入还是通过社区模块的编辑功能进入
     * svideo: 短视频
     * community: 社区
     */
    private String mRecordEntrance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //乐视x820手机在AndroidManifest中设置横竖屏无效，并且只在该activity无效其他activity有效
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        MySystemParams.getInstance().init(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Window window = getWindow();
        // 检测是否是全面屏手机, 如果不是, 设置FullScreen
        if (!NotchScreenUtil.checkNotchScreen(this)) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initAssetPath();
        setContentView(R.layout.alivc_recorder_activity_record);
        mVideoRecordView = findViewById(R.id.alivc_recordView);
        getData();
        boolean checkResult = PermissionUtils.checkPermissionsGroup(this, permission);
        if (!checkResult) {
            PermissionUtils.requestPermissions(this, permission, PERMISSION_REQUEST_CODE);
        } else {
            initRecord();
        }

    }

    private void initRecord() {
        mVideoRecordView.setActivity(this);
        if (mInputParam != null) {
            mVideoRecordView.isUseFlip(mInputParam.isUseFlip());
            mVideoRecordView.setMaxRecordTime(mInputParam.getMaxDuration());
            mVideoRecordView.setMinRecordTime(mInputParam.getMinDuration());
            mVideoRecordView.setRatioMode(mInputParam.getRatioMode());
            mVideoRecordView.setResolutionMode(mInputParam.getResolutionMode());
            mVideoRecordView.setRenderingMode(mInputParam.getmRenderingMode());
            mVideoRecordView.setSvideoRace(mInputParam.isSvideoRace());
            AlivcRecorder alivcRecorder = (AlivcRecorder)AlivcRecorderFactory.createAlivcRecorderFactory(AlivcRecorderFactory.RecorderType.GENERAL, this);
            com.aliyun.svideosdk.common.struct.recorder.MediaInfo outputInfo = new com.aliyun.svideosdk.common.struct.recorder.MediaInfo();
            outputInfo.setFps(35);
            outputInfo.setGop(mInputParam.getGop());
            outputInfo.setVideoCodec(mInputParam.getVideoCodec());
            outputInfo.setVideoQuality(mInputParam.getVideoQuality());
            outputInfo.setVideoWidth(mInputParam.getVideoWidth());
            outputInfo.setVideoHeight(mInputParam.getVideoHeight());
            outputInfo.setVideoCodec(mInputParam.getVideoCodec());            //配置录制recorder
            alivcRecorder.setMediaInfo(outputInfo);
            alivcRecorder.setIsAutoClearClipVideos(mInputParam.isAutoClearTemp());
            mVideoRecordView.setRecorder(alivcRecorder);
        }
        if (PermissionUtils.checkPermissionsGroup(this, PermissionUtils.PERMISSION_STORAGE)) {
            //有存储权限的时候才去copy资源
            copyAssets();
        }
    }


    private String[] mEffDirs;
    private AsyncTask<Void, Void, Void> copyAssetsTask;
    private AsyncTask<Void, Void, Void> initAssetPath;

    private void initAssetPath() {
        initAssetPath = new AssetPathInitTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static class AssetPathInitTask extends AsyncTask<Void, Void, Void> {

        private final WeakReference<AlivcSvideoRecordActivity> weakReference;

        AssetPathInitTask(AlivcSvideoRecordActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            AlivcSvideoRecordActivity activity = weakReference.get();
            if (activity != null) {
                activity.setAssetPath();
            }
            return null;
        }
    }

    private void setAssetPath() {
        String path = StorageUtils.getCacheDirectory(this).getAbsolutePath() + File.separator + RecordCommon.QU_NAME
                      + File.separator;
        File filter = new File(new File(path), "filter");
        String[] list = filter.list();
        if (list == null || list.length == 0) {
            return;
        }
        mEffDirs = new String[list.length + 1];
        mEffDirs[0] = null;
        int length = list.length;
        for (int i = 0; i < length; i++) {
            mEffDirs[i + 1] = filter.getPath() + File.separator + list[i];
        }
    }

    private void copyAssets() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                copyAssetsTask = new CopyAssetsTask(AlivcSvideoRecordActivity.this).executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }, 700);

    }

    public static class CopyAssetsTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<AlivcSvideoRecordActivity> weakReference;
        ProgressDialog progressBar;

        CopyAssetsTask(AlivcSvideoRecordActivity activity) {

            weakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AlivcSvideoRecordActivity activity = weakReference.get();
            if (activity != null && !activity.isFinishing()) {
                progressBar = new ProgressDialog(activity);
//                progressBar.setMessage(activity.getResources().getString(R.string.aliyun_res_copy));
                progressBar.setCanceledOnTouchOutside(false);
                progressBar.setCancelable(false);
                progressBar.setProgressStyle(android.app.ProgressDialog.STYLE_SPINNER);
                progressBar.show();
            }

        }

        @Override
        protected Void doInBackground(Void... voids) {
            AlivcSvideoRecordActivity activity = weakReference.get();
            if (activity != null) {
                RecordCommon.copyAll(activity);
                RecordCommon.copyQueen(activity);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            AlivcSvideoRecordActivity activity = weakReference.get();
            if (activity != null && !activity.isFinishing()) {
                progressBar.dismiss();
                //资源复制完成之后设置一下人脸追踪，防止第一次人脸动图应用失败的问题
                activity.mVideoRecordView.setFaceTrackModePath();
            }

        }
    }


    /**
     * 获取上个页面的传参
     */
    private void getData() {
        Intent intent = getIntent();
        int resolutionMode = intent.getIntExtra(AlivcRecordInputParam.INTENT_KEY_RESOLUTION_MODE, AlivcRecordInputParam.RESOLUTION_720P);
        int maxDuration = intent.getIntExtra(AlivcRecordInputParam.INTENT_KEY_MAX_DURATION, AlivcRecordInputParam.DEFAULT_VALUE_MAX_DURATION);
        int minDuration = intent.getIntExtra(AlivcRecordInputParam.INTENT_KEY_MIN_DURATION, AlivcRecordInputParam.DEFAULT_VALUE_MIN_DURATION);
        int ratioMode = intent.getIntExtra(AlivcRecordInputParam.INTENT_KEY_RATION_MODE, AlivcRecordInputParam.RATIO_MODE_9_16);
        boolean watermark = intent.getBooleanExtra(AlivcRecordInputParam.INTENT_KEY_WATER_MARK, true);
        int gop = intent.getIntExtra(AlivcRecordInputParam.INTENT_KEY_GOP, AlivcRecordInputParam.DEFAULT_VALUE_GOP);
        int frame = intent.getIntExtra(AlivcRecordInputParam.INTENT_KEY_FRAME, AlivcRecordInputParam.DEFAULT_VALUE_FRAME);
        VideoQuality videoQuality = (VideoQuality) intent.getSerializableExtra(AlivcRecordInputParam.INTENT_KEY_QUALITY);
        if (videoQuality == null) {
            videoQuality = VideoQuality.HD;
        }
        VideoCodecs videoCodec = (VideoCodecs) intent.getSerializableExtra(AlivcRecordInputParam.INTENT_KEY_CODEC);
        if (videoCodec == null) {
            videoCodec = VideoCodecs.H264_HARDWARE;
        }
        BeautySDKType renderingMode = (BeautySDKType) intent.getSerializableExtra(AlivcRecordInputParam.INTENT_KEY_VIDEO_RENDERING_MODE);
        if (renderingMode == null) {
            renderingMode = BeautySDKType.FACEUNITY;
        }
        String videoOutputPath = intent.getStringExtra(AlivcRecordInputParam.INTENT_KEY_VIDEO_OUTPUT_PATH);
        boolean isUseFlip = intent.getBooleanExtra(AlivcRecordInputParam.INTENT_KEY_RECORD_FLIP, false);
        boolean isSvideoQueen = intent.getBooleanExtra(AlivcRecordInputParam.INTENT_KEY_IS_SVIDEO_QUEEN, false);
        boolean isAutoClear = intent.getBooleanExtra(AlivcRecordInputParam.INTENT_KEY_IS_AUTO_CLEAR, false);
        //获取录制输入参数
        mInputParam = new AlivcRecordInputParam.Builder()
        .setResolutionMode(resolutionMode)
        .setRatioMode(ratioMode)
        .setMaxDuration(maxDuration)
        .setMinDuration(minDuration)
        .setGop(gop)
        .setWaterMark(watermark)
        .setFrame(frame)
        .setVideoQuality(videoQuality)
        .setVideoCodec(videoCodec)
        .setVideoOutputPath(videoOutputPath)
        .setVideoRenderingMode(renderingMode)
        .setIsUseFlip(isUseFlip)
        .setSvideoRace(isSvideoQueen)
        .setIsAutoClearTemp(isAutoClear)
        .setPlayDisplayParam(new VideoDisplayParam.Builder().build())
        .build();
    }


    @Override
    protected void onStart() {
        super.onStart();
        initPhoneStateManger();
    }

    private void initPhoneStateManger() {
        if (phoneStateManger == null) {
            phoneStateManger = new PhoneStateManger(this);
            phoneStateManger.registPhoneStateListener();
            phoneStateManger.setOnPhoneStateChangeListener(new PhoneStateManger.OnPhoneStateChangeListener() {

                @Override
                public void stateIdel() {
                    // 挂断
                    mVideoRecordView.setRecordMute(false);
                    isCalling = false;
                }

                @Override
                public void stateOff() {
                    // 接听
                    mVideoRecordView.setRecordMute(true);
                    isCalling = true;
                }

                @Override
                public void stateRinging() {
                    // 响铃
                    mVideoRecordView.setRecordMute(true);
                    isCalling = true;
                }
            });
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoRecordView.onResume();
        mVideoRecordView.startPreview();
        mVideoRecordView.setBackClickListener(new AliyunSVideoRecordView.OnBackClickListener() {
            @Override
            public void onClick() {
                finish();
            }
        });
        mVideoRecordView.setOnMusicSelectListener(new MusicSelectListener() {
            @Override
            public void onMusicSelect(MusicFileBean musicFileBean, long startTime) {
                String path = musicFileBean.getPath();
                if (musicFileBean != null && !TextUtils.isEmpty(path) && new File(path).exists()) {
                    isUseMusic = true;
                } else {
                    isUseMusic = false;
                }

            }
        });

        mVideoRecordView.setCompleteListener(new AliyunSVideoRecordView.OnFinishListener() {
            @Override
            public void onComplete(final String path, int duration, int ratio) {
                // 如果是RACE单独包，直接finish
                if (mInputParam.isSvideoRace()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        //适配android Q
                        ThreadUtils.runOnSubThread(new Runnable() {
                            @Override
                            public void run() {
                                UriUtils.saveVideoToMediaStore(AlivcSvideoRecordActivity.this, path);
                                ThreadUtils.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ToastUtils.show(AlivcSvideoRecordActivity.this, "已保存到相册");
                                        AlivcSvideoRecordActivity.this.finish();
                                    }
                                });
                            }
                        });

                    } else {
                        MediaScannerConnection.scanFile(AlivcSvideoRecordActivity.this.getApplicationContext(),
                                                        new String[] {path},
                                                        new String[] {"video/mp4"}, null);
                        ToastUtils.show(AlivcSvideoRecordActivity.this, "已保存到相册");
                        AlivcSvideoRecordActivity.this.finish();
                    }

                    return;
                }
                // 跳转到下一个页面
                //切换画幅重新设置视频分辨率宽高
                MediaInfo mediaInfo = new MediaInfo();
                mediaInfo.filePath = path;
                mediaInfo.startTime = 0;
                mediaInfo.mimeType = "video";
                mediaInfo.duration = duration;
                ArrayList<MediaInfo> infoList = new ArrayList<>();
                infoList.add(mediaInfo);

                Intent intent = new Intent();
                intent.setClassName(AlivcSvideoRecordActivity.this, "com.aliyun.svideo.editor.editor.EditorActivity");
                intent.putExtra("mFrame", mInputParam.getFrame());
                intent.putExtra("mRatioMode", ratio);
                intent.putExtra("mGop", mInputParam.getGop());
                intent.putExtra("mVideoQuality", mInputParam.getVideoQuality());
                intent.putExtra("mResolutionMode", mInputParam.getResolutionMode());
                intent.putExtra("mVideoCodec", mInputParam.getVideoCodec());
                intent.putExtra("canReplaceMusic", isUseMusic);
                intent.putExtra("hasWaterMark", mInputParam.hasWaterMark());
                intent.putParcelableArrayListExtra("mediaInfos", infoList);
                AlivcSvideoRecordActivity.this.startActivity(intent);

            }
        });
    }

    @Override
    protected void onPause() {
        mVideoRecordView.onPause();
        mVideoRecordView.stopPreview();
        super.onPause();
        if (phoningToast != null) {
            phoningToast.cancel();
            phoningToast = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (phoneStateManger != null) {
            phoneStateManger.setOnPhoneStateChangeListener(null);
            phoneStateManger.unRegistPhoneStateListener();
            phoneStateManger = null;
        }

        if (mVideoRecordView != null) {
            mVideoRecordView.onStop();
        }
    }


    @Override
    protected void onDestroy() {
        mVideoRecordView.destroyRecorder();
        super.onDestroy();
        if (copyAssetsTask != null) {
            copyAssetsTask.cancel(true);
            copyAssetsTask = null;
        }

        if (initAssetPath != null) {
            initAssetPath.cancel(true);
            initAssetPath = null;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PLAY) {
            if (resultCode == Activity.RESULT_OK) {
                mVideoRecordView.deleteAllPart();
                finish();
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (isCalling) {
            phoningToast = FixedToastUtils.show(this, getResources().getString(R.string.alivc_recorder_record_tip_phone_state_calling));
        }
    }

    /**
     * 开启录制
     *
     * @param context          上下文
     * @param recordInputParam 录制输入参数
     */
    public static void startRecord(Context context, AlivcRecordInputParam recordInputParam) {
        Intent intent = new Intent(context, AlivcSvideoRecordActivity.class);
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
        intent.putExtra(AlivcRecordInputParam.INTENT_KEY_WATER_MARK, recordInputParam.hasWaterMark());
        context.startActivity(intent);
    }

    public static final int PERMISSION_REQUEST_CODE = 1000;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean isAllGranted = true;

            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                // 如果所有的权限都授予了
                initRecord();
            } else {
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                showPermissionDialog();
            }
        }
    }

    //系统授权设置的弹框
    AlertDialog openAppDetDialog = null;

    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.ugc_app_name) + getResources().getString(R.string.alivc_recorder_record_dialog_permission_remind));
        builder.setPositiveButton(getResources().getString(R.string.alivc_record_request_permission_positive_btn_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });
        builder.setCancelable(false);
        builder.setNegativeButton(getResources().getString(R.string.alivc_recorder_record_dialog_not_setting), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //finish();
            }
        });
        if (null == openAppDetDialog) {
            openAppDetDialog = builder.create();
        }
        if (null != openAppDetDialog && !openAppDetDialog.isShowing()) {
            openAppDetDialog.show();
        }
    }
}
