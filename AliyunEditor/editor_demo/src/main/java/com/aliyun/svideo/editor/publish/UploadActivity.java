package com.aliyun.svideo.editor.publish;

import android.app.Activity;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aliyun.common.global.AliyunTag;
import com.aliyun.common.utils.ToastUtil;
import com.aliyun.qupai.editor.AliyunIVodCompose;
import com.aliyun.qupai.editor.impl.AliyunVodCompose;
import com.aliyun.qupaiokhttp.HttpRequest;
import com.aliyun.qupaiokhttp.RequestParams;
import com.aliyun.qupaiokhttp.StringHttpRequestCallback;
import com.aliyun.svideo.editor.R;
import com.aliyun.video.common.utils.ThreadUtils;

import java.io.File;
import java.io.IOException;


/**
 * Created by macpro on 2017/11/8. 上传页面
 */

public class UploadActivity extends Activity {

    public static final String KEY_UPLOAD_VIDEO = "video_path";
    public static final String KEY_UPLOAD_THUMBNAIL = "video_thumbnail";
    public static final String KEY_UPLOAD_DESC = "video_desc";
    public static final String KEY_PARAM_VIDEO_RATIO = "key_param_video_ratio";
    /**
     * 上个界面通过intent的传递参数的key, 对应的value主要作用是 判断是编辑模块进入还是通过社区模块的编辑功能进入
     */
    public static final String KEY_PARAM_ENTRANCE = "entrance";

    private ImageView mIvLeft;
    private TextView mTitle;
    private TextureView mTextureView;
    private ProgressBar mProgress;
    private TextView mVideoDesc;
    private TextView mProgressText;
    private MediaPlayer mMediaPlayer;
    private String mVideoPath;
    private String mThumbnailPath;
    private String mDesc;
    private boolean mIsUpload;
    private AliyunVodCompose mComposeClient;
    /**
     * home页面的activity package name
     */
    private static final String ACTIVITIY_ACTION_MAIN = "com.aliyun.alivcsolution.MainActivity";

    /**
     * 社区模块, 视频列表界面activity package name
     */
    private static final String ACTIVITIY_ACTION_COMMUNITY_VIDEOLIST = "com.aliyun.apsara.alivclittlevideo.activity.VideoListActivity";

    /**
     * 判断是编辑模块进入还是通过社区模块的编辑功能进入 svideo: 短视频 community: 社区
     */
    private String entrance;

    /**
     * 表示是通过短视频模块进入到该界面
     */
    private static final String INTENT_PARAM_ENTRANCE_SVIDEO = "svideo";
    /**
     * 表示是通过社区模块进入到该界面
     */
    private static final String INTENT_PARAM_ENTRANCE_COMMUNITY = "community";
    private float mRatio;
    /**
     * 页面是否处于后台/为了解决部分手机锁屏会重置SurfaceTexture，在后台恢复播放的问题
     */
    private boolean isBackground = true;

    /**
     * 临时上传的视频id,主要用于刷新视频上传凭证使用
     */
    private String videoId;
    /**
     * 视频文件大小
     */
    private long videoSize;
    /**
     * 图片文件大小
     */
    private long imageSize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aliyun_svideo_activity_upload);
        isBackground = false;
        initView();
        mVideoPath = getIntent().getStringExtra(KEY_UPLOAD_VIDEO);
        mThumbnailPath = getIntent().getStringExtra(KEY_UPLOAD_THUMBNAIL);
        mRatio = getIntent().getFloatExtra(KEY_PARAM_VIDEO_RATIO, 0f);
        entrance = getIntent().getStringExtra(KEY_PARAM_ENTRANCE);

        mDesc = getIntent().getStringExtra(KEY_UPLOAD_DESC);
        mVideoDesc.setText(mDesc);

        mTextureView.setSurfaceTextureListener(mlistener);

        if (mRatio != 0) {
            setTextureViewParams(mRatio);
        }
        mComposeClient = ComposeFactory.INSTANCE.getAliyunVodCompose();
        mComposeClient.init(this);
        videoSize = new File(mVideoPath).length();
        imageSize = new File(mThumbnailPath).length();
        startImageUpload();
    }

    private void initView() {
        mTitle = (TextView)findViewById(R.id.tv_center);
        mTitle.setVisibility(View.VISIBLE);
        mTitle.setText(R.string.my_video);
        mVideoDesc = (TextView)findViewById(R.id.video_desc);
        mTextureView = (TextureView)findViewById(R.id.texture);
        mIvLeft = (ImageView)findViewById(R.id.iv_left);
        mIvLeft.setVisibility(View.VISIBLE);
        mIvLeft.setImageResource(R.drawable.aliyun_svideo_crop_icon_cancel);
        mProgress = (ProgressBar)findViewById(R.id.upload_progress);
        mIvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mProgressText = (TextView)findViewById(R.id.progress_text);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = null;
        if (INTENT_PARAM_ENTRANCE_SVIDEO.equals(entrance)) {
            intent = new Intent();
            intent.setClassName(this, ACTIVITIY_ACTION_MAIN);
        } else if (INTENT_PARAM_ENTRANCE_COMMUNITY.equals(entrance)) {
            intent = new Intent();
            intent.setClassName(this, ACTIVITIY_ACTION_COMMUNITY_VIDEOLIST);
        }

        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mIsUpload) {
            mComposeClient.resumeUpload();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mIsUpload) {
            mComposeClient.pauseUpload();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        isBackground = false;
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isBackground = true;
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mComposeClient != null) {
            mComposeClient.release();
            mComposeClient = null;
        }
    }

    /**
     * 上传图片
     */
    private void startImageUpload() {
        RequestParams params = new RequestParams();
        params.addFormDataPart("imageType", "default");
        HttpRequest.get("https://alivc-demo.aliyuncs.com/demo/getImageUploadAuth",
        params, new StringHttpRequestCallback() {
            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                Log.d(AliyunTag.TAG, s);
                VodImageUploadAuth tokenInfo = VodImageUploadAuth.getImageTokenInfo(s);
                if (tokenInfo != null && mComposeClient != null) {
                    int rv = mComposeClient.uploadImageWithVod(mThumbnailPath, tokenInfo.getUploadAddress(), tokenInfo.getUploadAuth(), mUploadCallback);
                    if (rv < 0) {
                        Log.d(AliyunTag.TAG, "上传参数错误 video path : " + mVideoPath + " thumbnailk : " + mThumbnailPath);
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                ToastUtil.showToast(UploadActivity.this, "上传参数错误");
                            }
                        });
                    } else {
                        mIsUpload = true;
                    }

                } else {
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(UploadActivity.this, "Get image upload auth info failed");
                        }
                    });
                    Log.e(AliyunTag.TAG, "Get image upload auth info failed");
                }
            }

            @Override
            public void onFailure(int errorCode, String msg) {
                super.onFailure(errorCode, msg);
                Log.e(AliyunTag.TAG, "Get image upload auth info failed, errorCode:" + errorCode + ", msg:" + msg);
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showToast(UploadActivity.this, "Get image upload auth info failed");
                    }
                });
            }
        });
    }

    /**
     * 上传视频
     */
    private void startVideoUpload() {
        RequestParams params = new RequestParams();
        params.addFormDataPart("title", TextUtils.isEmpty(mVideoDesc.getText().toString()) ? "test video" : mVideoDesc.getText().toString());
        params.addFormDataPart("fileName", mVideoPath.toString());
        HttpRequest.get("https://alivc-demo.aliyuncs.com/demo/getVideoUploadAuth?", params, new StringHttpRequestCallback() {
            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                VodVideoUploadAuth tokenInfo = VodVideoUploadAuth.getVideoTokenInfo(s);
                if (tokenInfo != null && mComposeClient != null) {
                    videoId = tokenInfo.getVideoId().toString();
                    int rv = mComposeClient.uploadVideoWithVod(mVideoPath, tokenInfo.getUploadAddress(), tokenInfo.getUploadAuth(), mUploadCallback);
                    if (rv < 0) {
                        Log.d(AliyunTag.TAG, "上传参数错误 video path : " + mVideoPath + " thumbnailk : " + mThumbnailPath);
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast(UploadActivity.this, "上传参数错误");
                            }
                        });
                    } else {
                        mIsUpload = true;
                    }

                } else {
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(UploadActivity.this, "Get video upload auth failed");
                        }
                    });
                    Log.e(AliyunTag.TAG, "Get video upload auth info failed");
                }
            }

            @Override
            public void onFailure(int errorCode, String msg) {
                super.onFailure(errorCode, msg);
                Log.e(AliyunTag.TAG, "Get video upload auth failed, errorCode:" + errorCode + ", msg:" + msg);
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showToast(UploadActivity.this, "Get video upload auth failed");
                    }
                });
            }
        });
    }

    private final AliyunVodCompose.AliyunIVodUploadCallBack mUploadCallback = new AliyunVodCompose.AliyunIVodUploadCallBack() {

        @Override
        public void onUploadSucceed() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mComposeClient.getState() == AliyunIVodCompose.AliyunComposeState.STATE_IMAGE_UPLOADING) {
                        //如果是图片上传回调，继续视频上传
                        startVideoUpload();
                        return;
                    }
                    mProgress.setVisibility(View.GONE);
                    mProgressText.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.aliyun_svideo_icon_composite_success,
                            0, 0, 0);
                    mProgressText.setText(R.string.upload_success);
                    mProgressText.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mProgressText.setVisibility(View.GONE);
                        }
                    }, 2000);
                }
            });

        }

        @Override
        public void onUploadFailed(String code, String message) {
            Log.e(AliyunTag.TAG, "onUploadFailed, errorCode:" + code + ", msg:" + message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgress.setProgress(0);
                    mProgressText.setText(R.string.upload_failed);
                    mProgressText.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mProgressText.setVisibility(View.GONE);
                        }
                    }, 2000);
                }
            });

        }

        @Override
        public void onUploadProgress(final long uploadedSize, final long totalSize) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int progress = 0;
                    if (mComposeClient.getState() == AliyunVodCompose.AliyunComposeState.STATE_IMAGE_UPLOADING) {
                        progress = (int)((uploadedSize * 100) / (totalSize  + videoSize));
                    } else if (mComposeClient.getState() == AliyunVodCompose.AliyunComposeState.STATE_VIDEO_UPLOADING) {
                        progress = (int)(((uploadedSize + imageSize) * 100) / (totalSize + imageSize));
                    }

                    mProgress.setProgress(progress);
                    mProgressText.setText("正在上传 " + progress + "%");
                }
            });

        }

        @Override
        public void onUploadRetry(String code, String message) {

        }

        @Override
        public void onUploadRetryResume() {

        }

        @Override
        public void onUploadTokenExpired() {
            if (mComposeClient.getState() == AliyunIVodCompose.AliyunComposeState.STATE_IMAGE_UPLOADING) {
                startImageUpload();
            } else if (mComposeClient.getState() == AliyunIVodCompose.AliyunComposeState.STATE_VIDEO_UPLOADING) {
                refreshVideoUpload(videoId);
            }
        }
    };

    /**
     * 刷新视频凭证
     * @param videoId
     */
    private void refreshVideoUpload(String videoId) {
        RequestParams params = new RequestParams();
        params.addFormDataPart("videoId", videoId);
        HttpRequest.get("https://alivc-demo.aliyuncs.com/demo/refreshVideoUploadAuth?", params, new StringHttpRequestCallback() {
            @Override
            protected void onSuccess(String s) {
                super.onSuccess(s);
                RefreshVodVideoUploadAuth tokenInfo = RefreshVodVideoUploadAuth.getReVideoTokenInfo(s);
                if (tokenInfo != null && mComposeClient != null) {
                    int rv = mComposeClient.refreshWithUploadAuth(tokenInfo.getUploadAuth());
                    if (rv < 0) {
                        Log.d(AliyunTag.TAG, "上传参数错误 video path : " + mVideoPath + " thumbnailk : " + mThumbnailPath);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast(UploadActivity.this, "上传参数错误");
                            }
                        });
                    } else {
                        mIsUpload = true;
                    }

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(UploadActivity.this, "Get video upload auth failed");
                        }
                    });
                    Log.e(AliyunTag.TAG, "Get video upload auth info failed");
                }

            }

            @Override
            public void onFailure(int errorCode, String msg) {
                super.onFailure(errorCode, msg);
            }
        });
    }

    private final TextureView.SurfaceTextureListener mlistener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            initVideoPlayer();
            try {
                mMediaPlayer.setDataSource(mVideoPath);
                mMediaPlayer.setSurface(new Surface(surface));
                mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private void initVideoPlayer() {
        if (mMediaPlayer != null) {
            return;
        }
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setScreenOnWhilePlaying(true);

        mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //为了解决部分手机锁屏会重置SurfaceTexture，在后台恢复播放的问题
                if (!isBackground) {
                    mMediaPlayer.start();
                }
            }
        });

    }

    private final MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();
            int vw = mp.getVideoWidth();
            int vh = mp.getVideoHeight();

            if (mRatio == 0) {
                setTextureViewParams(((float) vw) / vh);
            }
        }
    };

    /**
     * 设置textureView导入的视频比例
     * @param ratio x/y
     */
    private void setTextureViewParams(float ratio) {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        mTextureView.getLayoutParams().width = screenWidth;
        mTextureView.getLayoutParams().height = (int)(screenWidth / ratio);
    }

}
