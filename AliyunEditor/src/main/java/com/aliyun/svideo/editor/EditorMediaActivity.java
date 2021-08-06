/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.aliyun.common.utils.ToastUtil;
import com.aliyun.svideo.base.Constants;
import com.aliyun.svideo.base.widget.ProgressDialog;
import com.aliyun.svideo.common.utils.FastClickUtil;
import com.aliyun.svideo.common.utils.MD5Utils;
import com.aliyun.svideo.common.utils.ToastUtils;
import com.aliyun.svideo.common.utils.UriUtils;
import com.aliyun.svideo.crop.AliyunImageCropActivity;
import com.aliyun.svideo.crop.AliyunVideoCropActivity;
import com.aliyun.svideo.crop.bean.AlivcCropInputParam;
import com.aliyun.svideo.crop.bean.AlivcCropOutputParam;
import com.aliyun.svideo.editor.bean.AlivcEditInputParam;
import com.aliyun.svideo.editor.editor.EditorActivity;
import com.aliyun.svideo.media.MediaInfo;
import com.aliyun.svideo.media.MutiMediaView;
import com.aliyun.svideosdk.common.AliyunErrorCode;
import com.aliyun.svideosdk.common.struct.common.CropKey;
import com.aliyun.svideosdk.common.struct.common.VideoDisplayMode;
import com.aliyun.svideosdk.common.struct.common.VideoQuality;
import com.aliyun.svideosdk.common.struct.encoder.VideoCodecs;
import com.duanqu.transcode.NativeParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 编辑模块的media选择Activity
 */
public class EditorMediaActivity extends Activity {

    private static final int IMAGE_DURATION = 3000;//图片代表的时长

    private final static String TAG = EditorMediaActivity.class.getSimpleName();

    private ProgressDialog progressDialog;

    private Transcoder mTransCoder;
    private MediaInfo mCurrMediaInfo;
    private int mCropPosition;

    private AlivcEditInputParam mInputParam;

    /**
     * 页面恢复时保存mBundleSaveMedias对象的key
     * 保存时 {@link #onSaveInstanceState(Bundle)}
     * 恢复时 {@link #onRestoreInstanceState(Bundle)}
     */
    private static final String BUNDLE_KEY_SAVE_MEDIAS = "bundle_key_save_transcoder";

    /**
     * 页面恢复时保存选择Medias对象
     */
    private ArrayList<MediaInfo> mBundleSaveMedias;
    private MutiMediaView mMutiMediaView;
    private int mRatio;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alivc_editor_media);
        initData();
        init();
    }

    private void initData() {
        Intent intent = getIntent();
        int mFrameRate = intent.getIntExtra(AlivcEditInputParam.INTENT_KEY_FRAME, 30);
        int mGop = intent.getIntExtra(AlivcEditInputParam.INTENT_KEY_GOP, 250 );
        mRatio = intent.getIntExtra(AlivcEditInputParam.INTENT_KEY_RATION_MODE, AlivcEditInputParam.RATIO_MODE_9_16);
        VideoQuality mVideoQuality = (VideoQuality)intent.getSerializableExtra(AlivcEditInputParam.INTENT_KEY_QUALITY);
        if (mVideoQuality == null) {
            mVideoQuality = VideoQuality.HD;
        }
        int mResolutionMode = intent.getIntExtra(AlivcEditInputParam.INTENT_KEY_RESOLUTION_MODE, AlivcEditInputParam.RESOLUTION_720P );
        VideoCodecs mVideoCodec = (VideoCodecs)intent.getSerializableExtra(AlivcEditInputParam.INTENT_KEY_CODEC);
        if (mVideoCodec == null) {
            mVideoCodec = VideoCodecs.H264_HARDWARE;
        }
        int mCrf = intent.getIntExtra(AlivcEditInputParam.INTETN_KEY_CRF, 23 );
        float mScaleRate = intent.getFloatExtra(AlivcEditInputParam.INTETN_KEY_SCANLE_RATE, 1.0f );
        VideoDisplayMode mScaleMode = (VideoDisplayMode)intent.getSerializableExtra(AlivcEditInputParam.INTETN_KEY_SCANLE_MODE);
        if (mScaleMode == null) {
            mScaleMode = VideoDisplayMode.FILL;
        }
        boolean mHasTailAnimation = intent.getBooleanExtra(AlivcEditInputParam.INTENT_KEY_TAIL_ANIMATION, false );
        boolean canReplaceMusic = intent.getBooleanExtra(AlivcEditInputParam.INTENT_KEY_REPLACE_MUSIC, true );
        ArrayList<MediaInfo> mediaInfos = intent.getParcelableArrayListExtra(AlivcEditInputParam.INTENT_KEY_MEDIA_INFO);
        boolean hasWaterMark = intent.getBooleanExtra(AlivcEditInputParam.INTENT_KEY_WATER_MARK, false );
        mInputParam = new AlivcEditInputParam.Builder()
        .setFrameRate(mFrameRate)
        .setGop(mGop)
        .setRatio(mRatio)
        .setVideoQuality(mVideoQuality)
        .setResolutionMode(mResolutionMode)
        .setVideoCodec(mVideoCodec)
        .setCrf(mCrf)
        .setScaleRate(mScaleRate)
        .setScaleMode(mScaleMode)
        .setHasTailAnimation(mHasTailAnimation)
        .setCanReplaceMusic(canReplaceMusic)
        .addMediaInfos(mediaInfos)
        .setHasWaterMark(hasWaterMark)
        .build();
    }

    private void init() {
        mMutiMediaView = findViewById(R.id.media_view);
        //最大时长5分钟
        mMutiMediaView.enableSelectView(5 * 60 * 1000);
        mMutiMediaView.enableSwap();
        mTransCoder = new Transcoder();
        mTransCoder.init(this);
        mTransCoder.setTransCallback(new Transcoder.TransCallback() {
            @Override
            public void onError(Throwable e, final int errorCode) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                        switch (errorCode) {
                        case AliyunErrorCode.ALIVC_SVIDEO_ERROR_MEDIA_NOT_SUPPORTED_AUDIO:
                            ToastUtil.showToast(EditorMediaActivity.this, R.string.alivc_crop_video_tip_not_supported_audio);
                            break;
                        case AliyunErrorCode.ALIVC_SVIDEO_ERROR_MEDIA_NOT_SUPPORTED_VIDEO:
                            ToastUtil.showToast(EditorMediaActivity.this, R.string.alivc_crop_video_tip_crop_failed);
                            break;
                        case AliyunErrorCode.ALIVC_COMMON_UNKNOWN_ERROR_CODE:
                        default:
                            ToastUtil.showToast(EditorMediaActivity.this, R.string.alivc_crop_video_tip_crop_failed);
                        }
                    }
                });

            }
            @Override
            public void onProgress(int progress) {
                if (progressDialog != null) {
                    progressDialog.setProgress(progress);
                }
            }
            @Override
            public void onComplete(List<MediaInfo> resultVideos) {
                Log.d("TRANCODE", "ONCOMPLETED, dialog : " + (progressDialog == null));
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                mInputParam.setMediaInfos((ArrayList<MediaInfo>)resultVideos);
                EditorActivity.startEdit(EditorMediaActivity.this, mInputParam  );
            }

            @Override
            public void onCancelComplete() {
                //取消完成
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMutiMediaView.setNextEnable(true);
                    }
                });
            }
        });

        mMutiMediaView.setOnActionListener(new MutiMediaView.OnActionListener() {
            @Override
            public void onNext(boolean isReachedMaxDuration) {
                if (FastClickUtil.isFastClick()) {
                    return;
                }
                if (isReachedMaxDuration) {
                    ToastUtil.showToast(EditorMediaActivity.this, R.string.alivc_media_message_max_duration_import);
                    return;
                }
                //对于大于720P的视频需要走转码流程

                int videoCount = mTransCoder.getVideoCount();
                if (videoCount > 0 && (progressDialog == null || !progressDialog.isShowing())) {
                    progressDialog = ProgressDialog.show(EditorMediaActivity.this, null, getResources().getString(R.string.alivc_media_wait));
                    progressDialog.setCancelable(true);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setOnCancelListener(new OnCancelListener(EditorMediaActivity.this));
                    mTransCoder.transcode(EditorMediaActivity.this.getApplicationContext(), mInputParam.getScaleMode());
                } else {
                    ToastUtil.showToast(EditorMediaActivity.this, R.string.alivc_media_please_select_video);
                }
            }

            @Override
            public void onBack() {
                finish();
            }
        });

        mMutiMediaView.setOnMediaClickListener(new MutiMediaView.OnMediaClickListener() {
            @Override
            public void onClick(MediaInfo info) {
                Log.i(TAG, "log_editor_video_path : " + info.filePath);
                MediaInfo infoCopy = new MediaInfo();
                infoCopy.addTime = info.addTime;
                infoCopy.mimeType = info.mimeType;
                if (info.mimeType.startsWith("image")) {
                    if (info.filePath.endsWith("gif") || info.filePath.endsWith("GIF")) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !TextUtils.isEmpty(info.fileUri)) {
                            //适配 android Q, copy 媒体文件到应用沙盒下
                            info.filePath = cacheMediaFile(EditorMediaActivity.this, info.fileUri, info.filePath);
                        }
                        NativeParser parser = new NativeParser();
                        parser.init(info.filePath);
                        int frameCount;

                        try {
                            frameCount = Integer.parseInt(parser.getValue(NativeParser.VIDEO_FRAME_COUNT));
                        } catch (Exception e) {
                            ToastUtils.show(EditorMediaActivity.this, R.string.alivc_editor_error_tip_play_video_error);
                            parser.release();
                            parser.dispose();
                            return;
                        }
                        //当gif动图为一帧的时候当作图片处理，否则当作视频处理
                        if (frameCount > 1) {
                            int duration;
                            try {
                                duration = Integer.parseInt(parser.getValue(NativeParser.VIDEO_DURATION)) / 1000;
                            } catch (Exception e) {
                                ToastUtils.show(EditorMediaActivity.this, R.string.alivc_editor_error_tip_play_video_error);
                                parser.release();
                                parser.dispose();
                                return;
                            }
                            infoCopy.mimeType = "video";
                            infoCopy.duration = duration;
                        } else {
                            infoCopy.duration = IMAGE_DURATION;
                        }
                        parser.release();
                        parser.dispose();

                    } else {
                        if (mRatio == AlivcEditInputParam.RATIO_MODE_ORIGINAL) {
                            //原比例下android解码器对图片大小有要求，目前支持为单边不大于3840
                            try {
                                ParcelFileDescriptor pfd = EditorMediaActivity.this.getContentResolver().openFileDescriptor(Uri.parse(info.fileUri), "r");
                                if (pfd != null) {
                                    Bitmap bitmap = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
                                    if (bitmap != null && (bitmap.getHeight() > 3840 || bitmap.getWidth() > 3840)) {
                                        ToastUtils.show(EditorMediaActivity.this, "原尺寸输出时，图片宽高不能超过3840");
                                        return;
                                    }
                                }
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                        infoCopy.duration = IMAGE_DURATION;
                    }

                } else {
                    infoCopy.duration = info.duration;
                }
                infoCopy.filePath = info.filePath;
                infoCopy.fileUri = info.fileUri;
                infoCopy.id = info.id;
                infoCopy.isSquare = info.isSquare;
                infoCopy.thumbnailPath = info.thumbnailPath;
                infoCopy.thumbnailUri = info.thumbnailUri;
                infoCopy.title = info.title;
                infoCopy.type = info.type;

                mMutiMediaView.addSelectMedia(infoCopy);
                mMutiMediaView.setNextEnable(true);
                mTransCoder.addMedia(infoCopy);
            }
        });

        mMutiMediaView.setOnSelectMediaChangeListener(new MutiMediaView.OnSelectMediaChangeListener() {
            @Override
            public void onRemove(MediaInfo info) {
                mTransCoder.removeMedia(info);
            }

            @Override
            public void onClick(MediaInfo info, int position) {

                if (FastClickUtil.isFastClickActivity(EditorMediaActivity.class.getSimpleName())) {
                    return;
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !TextUtils.isEmpty(info.fileUri)) {
                    //适配 android Q, copy 媒体文件到应用沙盒下
                    info.filePath = cacheMediaFile(EditorMediaActivity.this, info.fileUri, info.filePath);
                }
                mCurrMediaInfo = info;
                mCropPosition = position;

                if (info.filePath.endsWith("gif") || info.filePath.endsWith("GIF")) {
                    Toast.makeText(EditorMediaActivity.this, R.string.alivc_crop_media_gif_not_support, Toast.LENGTH_SHORT).show();
                    return;
                }

                AlivcCropInputParam cropInputParam = new AlivcCropInputParam.Builder()
                .setRatioMode(mInputParam.getRatio())
                .setResolutionMode(mInputParam.getResolutionMode())
                .setCropMode(mInputParam.getScaleMode())
                .setFrameRate(mInputParam.getFrameRate())
                .setGop(mInputParam.getGop())
                .setQuality(mInputParam.getVideoQuality())
                .setVideoCodecs(mInputParam.getVideoCodec())
                .setAction(CropKey.ACTION_SELECT_TIME)
                .build();
                if (info.mimeType.startsWith("video")) {
                    cropInputParam.setPath(info.filePath);
                    AliyunVideoCropActivity.startVideoCropForResult(EditorMediaActivity.this, cropInputParam, AliyunVideoCropActivity.REQUEST_CODE_EDITOR_VIDEO_CROP );
                } else if (info.mimeType.startsWith("image")) {
                    cropInputParam.setPath(info.filePath);
                    AliyunImageCropActivity.startImageCropForResult(EditorMediaActivity.this, cropInputParam, AliyunImageCropActivity.REQUEST_CODE_EDITOR_IMAGE_CROP);
                }
            }

            @Override
            public void onSwap(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                               RecyclerView.ViewHolder target) {
                mTransCoder.swap(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            }
        });
        mMutiMediaView.loadMedia();

    }

    /**
     * Android Q 缓存媒体文件到文件沙盒
     * @param context 上下文
     * @param originalFilePath 文件path
     * @param fileUri 文件Uri
     * @return filePath 文件缓存地址
     */
    private String cacheMediaFile(Context context, String fileUri, String originalFilePath) {
        if (originalFilePath.contains(context.getPackageName())) {
            return originalFilePath;
        }
        String filePath = null;
        if (!TextUtils.isEmpty(fileUri)) {
            int index = originalFilePath.lastIndexOf(".");
            String suffix = index == -1 ? "" : originalFilePath.substring(index);
            filePath = Constants.SDCardConstants.getCacheDir(context) + File.separator + MD5Utils
                       .getMD5(fileUri) + suffix;
            if (!new File(filePath).exists()) {
                UriUtils.copyFileToDir(context, fileUri, filePath);
            }
        }
        return filePath;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            AlivcCropOutputParam outputParam = (AlivcCropOutputParam)data.getSerializableExtra(AlivcCropOutputParam.RESULT_KEY_OUTPUT_PARAM);
            if (outputParam == null) {
                return;
            }
            String path = outputParam.getOutputPath();
            switch (requestCode) {
            case AliyunVideoCropActivity.REQUEST_CODE_EDITOR_VIDEO_CROP:

                long duration = outputParam.getDuration();
                long startTime = outputParam.getStartTime();
                if (!TextUtils.isEmpty(path) && duration > 0 && mCurrMediaInfo != null) {
                    mMutiMediaView.changeDurationPosition(mCropPosition, duration);
                    int index = mTransCoder.removeMedia(mCurrMediaInfo);
                    mCurrMediaInfo.filePath = path;
                    mCurrMediaInfo.startTime = startTime;
                    mCurrMediaInfo.duration = (int)duration;
                    mTransCoder.addMedia(index, mCurrMediaInfo);
                }
                break;
            case AliyunImageCropActivity.REQUEST_CODE_EDITOR_IMAGE_CROP:
                if (!TextUtils.isEmpty(path) && mCurrMediaInfo != null) {
                    int index = mTransCoder.removeMedia(mCurrMediaInfo);
                    mCurrMediaInfo.filePath = path;
                    mTransCoder.addMedia(index, mCurrMediaInfo);
                }
                break;
            default:
                break;
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMutiMediaView.onDestroy();
        mTransCoder.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //恢复选择的medias
        if (mBundleSaveMedias != null) {
            for (MediaInfo mediaInfo : mBundleSaveMedias) {
                mMutiMediaView.addSelectMedia(mediaInfo);
                mTransCoder.addMedia(mediaInfo);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //每次退到后台清空save的值，避免正常时也会恢复
        mBundleSaveMedias = null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(BUNDLE_KEY_SAVE_MEDIAS, mTransCoder.getOriginalVideos());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList<MediaInfo> data = savedInstanceState.getParcelableArrayList(BUNDLE_KEY_SAVE_MEDIAS);
        if (data != null && data.size() != 0) {
            mBundleSaveMedias = data;
        }
    }

    /**
     * progressDialog cancel listener
     */
    private static class OnCancelListener implements DialogInterface.OnCancelListener {

        private WeakReference<EditorMediaActivity> weakReference;

        private OnCancelListener(EditorMediaActivity mediaActivity) {
            weakReference = new WeakReference<>(mediaActivity);
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            EditorMediaActivity mediaActivity = weakReference.get();
            if (mediaActivity != null) {
                mediaActivity.mMutiMediaView.setNextEnable(false);//为了防止未取消成功的情况下就开始下一次转码，这里在取消转码成功前会禁用下一步按钮
                mediaActivity.mTransCoder.cancel();
            }
        }
    }

    public static void startImport(Context context, AlivcEditInputParam param) {
        if (param == null) {
            return;
        }
        Intent intent = new Intent(context, EditorMediaActivity.class);
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_FRAME, param.getFrameRate());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_GOP, param.getGop());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_RATION_MODE, param.getRatio());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_QUALITY, param.getVideoQuality());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_RESOLUTION_MODE, param.getResolutionMode());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_CODEC, param.getVideoCodec());
        intent.putExtra(AlivcEditInputParam.INTETN_KEY_CRF, param.getCrf());
        intent.putExtra(AlivcEditInputParam.INTETN_KEY_SCANLE_RATE, param.getScaleRate());
        intent.putExtra(AlivcEditInputParam.INTETN_KEY_SCANLE_MODE, param.getScaleMode());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_TAIL_ANIMATION, param.isHasTailAnimation());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_REPLACE_MUSIC, param.isCanReplaceMusic());
        intent.putExtra(AlivcEditInputParam.INTENT_KEY_WATER_MARK, param.isHasWaterMark());
        intent.putParcelableArrayListExtra(AlivcEditInputParam.INTENT_KEY_MEDIA_INFO, param.getMediaInfos());
        context.startActivity(intent);
    }

}
