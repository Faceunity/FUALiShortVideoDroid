package com.aliyun.svideo.editor.publish;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.common.widget.AlivcCircleLoadingDialog;
import com.aliyun.svideo.player.AliyunISVideoPlayer;
import com.aliyun.svideo.player.AliyunSVideoPlayerCreator;
import com.aliyun.svideo.player.PlayerCallback;
import com.aliyun.svideo.sdk.external.struct.common.VideoDisplayMode;
import com.aliyun.svideo.sdk.external.thumbnail.AliyunIThumbnailFetcher;
import com.aliyun.svideo.sdk.external.thumbnail.AliyunThumbnailFetcherFactory;
import com.aliyun.svideo.common.utils.ToastUtils;
import com.duanqu.transcode.NativeParser;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by macpro on 2017/11/7.
 */

public class CoverEditActivity extends Activity {

    public static final String KEY_PARAM_VIDEO = "vidseo_path";
    public static final String KEY_PARAM_RESULT = "thumbnail";
    private static final String TAG = CoverEditActivity.class.getSimpleName();
    private ImageView mIvLeft, mIvRight;
    private View mSlider;
    private LinearLayout mThumbnailList;

    private String mVideoPath;
    private AliyunIThumbnailFetcher mThumbnailFetcher;

    /**
     * sdk提供的播放器，支持非关键帧的实时预览
     */
    private AliyunISVideoPlayer mPlayer;
    private TextureView mTextureView;
    /**
     * 最后seek的时间，选择时取这个时间点的封面
     */
    private long mEndTime;
    private AliyunIThumbnailFetcher mCoverThumbnailFetcher;
    private AlivcCircleLoadingDialog mLoadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alivc_editor_activity_cover_edit);
        initView();
        mVideoPath = getIntent().getStringExtra(KEY_PARAM_VIDEO);
        mThumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher();
        mThumbnailFetcher.addVideoSource(mVideoPath, 0, Integer.MAX_VALUE, 0);
        mThumbnailList.post(mInitThumbnails);
    }

    private void initView() {
        mIvLeft = (ImageView) findViewById(R.id.iv_left);
        mIvRight = (ImageView) findViewById(R.id.iv_right);
        TextView title = (TextView)findViewById(R.id.tv_center);

        mIvLeft.setVisibility(View.VISIBLE);
        mIvRight.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);

        title.setText(R.string.alivc_editor_cover_tittle);
        mIvLeft.setImageResource(R.drawable.aliyun_svideo_crop_icon_cancel);
        mIvRight.setImageResource(R.mipmap.aliyun_svideo_icon_confirm);
        mIvLeft.setOnClickListener(mOnClickListener);
        mIvRight.setOnClickListener(mOnClickListener);

        mSlider = findViewById(R.id.indiator);
        mSlider.setOnTouchListener(mSliderListener);
        mThumbnailList = (LinearLayout) findViewById(R.id.cover_thumbnail_list);
        mThumbnailList.setOnTouchListener(mOnTouchListener);
        mTextureView = findViewById(R.id.ttv_editor_cover);
        mTextureView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {

                mTextureView.setSurfaceTextureListener(mTextureViewListener);
                resizeLayout();
                ViewTreeObserver viewTreeObserver = mTextureView.getViewTreeObserver();
                if (viewTreeObserver.isAlive()) {
                    viewTreeObserver.removeOnPreDrawListener(this);
                }

                return true;
            }
        });
    }

    /**
     * 重置textureView的宽高，需要在AliyunISVideoPlayer初始化之前执行
     */
    private void resizeLayout() {
        NativeParser nativeParser = new NativeParser();
        nativeParser.init(mVideoPath);
        int dataWidth = Integer.parseInt(nativeParser.getValue(NativeParser.VIDEO_WIDTH));
        int dataHeight = Integer.parseInt(nativeParser.getValue(NativeParser.VIDEO_HEIGHT));
        nativeParser.release();
        nativeParser.dispose();
        ViewGroup.LayoutParams layoutParams = mTextureView.getLayoutParams();
        float videoRatio = dataWidth / (float)dataHeight;
        float frameRatio = mTextureView.getWidth() / (float)mTextureView.getHeight();

        if (videoRatio >= frameRatio) {
            //视频宽高比大于TextureView的宽高比
            layoutParams.width = mTextureView.getWidth();
            layoutParams.height = (int)(mTextureView.getWidth() / videoRatio);
        } else {
            layoutParams.height = mTextureView.getHeight();
            layoutParams.width = (int)(mTextureView.getHeight() * videoRatio);
        }
        mTextureView.setLayoutParams(layoutParams);

    }

    private final View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int offset = mSlider.getLeft() - mSlider.getPaddingLeft();
            int vw = mSlider.getWidth() - mSlider.getPaddingRight() - mSlider.getPaddingLeft();
            int endOffset = mSlider.getLeft() + mThumbnailList.getWidth() - vw - mSlider.getPaddingLeft();
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                float x = motionEvent.getX();
                float px = x + mSlider.getLeft() - mSlider.getPaddingLeft();
                if (px >= endOffset) {
                    px = endOffset;
                }
                if (px <= offset) {
                    px = offset;
                }
                long time = (long) (mThumbnailFetcher.getTotalDuration() * px / mThumbnailList.getWidth());
                seek(time);

                mSlider.setX(px);
            }
            return true;
        }
    };

    private final View.OnTouchListener mSliderListener = new View.OnTouchListener() {

        private float lastX;
        private float dx;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            int offset = v.getLeft() - v.getPaddingLeft();
            int vw = v.getWidth() - v.getPaddingRight() - v.getPaddingLeft();
            int endOffset = v.getLeft() + mThumbnailList.getWidth() - vw - v.getPaddingLeft();
            long time = 0;
            switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getRawX();
                dx = lastX - v.getX();
                break;
            case MotionEvent.ACTION_MOVE:

                lastX = event.getRawX();
                float nx = lastX - dx;
                if (nx >= endOffset) {
                    nx = endOffset;
                }
                if (nx <= offset) {
                    nx = offset;
                }

                v.setX(nx);
                time = (long) (mThumbnailFetcher.getTotalDuration() * (nx - offset) / mThumbnailList.getWidth());
                seek(time);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                float x = v.getX() - offset;
                time = (long) (mThumbnailFetcher.getTotalDuration() * x / mThumbnailList.getWidth());
                seek(time);
                break;
            default:
                break;
            }
            return true;
        }
    };


    private void seek(long time) {
        mPlayer.seek(time);
        mEndTime = time;
    }

    private final Runnable mInitThumbnails = new Runnable() {
        @Override
        public void run() {
            mSlider.setX(mSlider.getX() - mSlider.getPaddingLeft());
            initThumbnails();
        }
    };

    private void initThumbnails() {
        int width = mThumbnailList.getWidth();
        int itemWidth = width / 8;
        mThumbnailFetcher.setParameters(itemWidth, itemWidth,
                                        AliyunIThumbnailFetcher.CropMode.Mediate, VideoDisplayMode.SCALE, 8);
        long duration = mThumbnailFetcher.getTotalDuration();
        long itemTime = duration / 8;
        for (int i = 1; i <= 8; i++) {
            requestFetchThumbnail(itemTime, i, 8);
        }
    }

    /**
     * 获取缩略图
     * @param interval 取帧平均间隔
     * @param position 第几张
     * @param count 总共的张数
     */
    private void requestFetchThumbnail(final long interval, final int position, final int count) {
        long[] times = {(position - 1) * interval + interval / 2};

        Log.d(TAG, "requestThumbnailImage() times :" + times[0] + " ,position = " + position);
        mThumbnailFetcher.requestThumbnailImage(times, new AliyunIThumbnailFetcher.OnThumbnailCompletion() {

            private int vecIndex = 1;

            @Override
            public void onThumbnailReady(Bitmap frameBitmap, long l) {
                if (frameBitmap != null && !frameBitmap.isRecycled()) {
                    Log.i(TAG, "onThumbnailReady  put: " + position + " ,l = " + l / 1000);
                    ImageView image = new ImageView(CoverEditActivity.this);
                    image.setScaleType(ImageView.ScaleType.FIT_XY);
                    image.setImageBitmap(frameBitmap);
                    mThumbnailList.addView(image);
                } else {
                    if (position == 0) {
                        vecIndex = 1;
                    } else if (position == count + 1) {
                        vecIndex = -1;
                    }
                    int np = position + vecIndex;
                    Log.i(TAG, "requestThumbnailImage  failure: thisPosition = " + position + "newPosition = " + np);
                    requestFetchThumbnail(interval, np, count);
                }
            }

            @Override
            public void onError(int errorCode) {
                Log.w(TAG, "requestThumbnailImage error msg: " + errorCode);
            }
        });
    }

    private Surface mSurface;
    private TextureView.SurfaceTextureListener mTextureViewListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            if (mPlayer == null) {
                mSurface = new Surface(surface);
                mPlayer = AliyunSVideoPlayerCreator.createPlayer();
                mPlayer.init(CoverEditActivity.this);

                mPlayer.setPlayerCallback(new PlayerCallback() {
                    @Override
                    public void onPlayComplete() {

                    }

                    @Override
                    public void onDataSize(int dataWidth, int dataHeight) {

                        ViewGroup.LayoutParams layoutParams = mTextureView.getLayoutParams();
                        float videoRatio = dataWidth / (float)dataHeight;
                        float frameRatio = mTextureView.getWidth() / (float)mTextureView.getHeight();

                        if (videoRatio >= frameRatio) {
                            //视频宽高比大于TextureView的宽高比
                            layoutParams.width = mTextureView.getWidth();
                            layoutParams.height = (int)(mTextureView.getWidth() / videoRatio);
                        } else {
                            layoutParams.height = mTextureView.getHeight();
                            layoutParams.width = (int)(mTextureView.getHeight() * videoRatio);
                        }
                        mTextureView.setLayoutParams(layoutParams);
                        mPlayer.setDisplaySize(layoutParams.width, layoutParams.height);
                    }

                    @Override
                    public void onError(int i) {
                        Log.e(TAG, "错误码 : " + i);
                    }
                });
                mPlayer.setDisplay(mSurface);
                mPlayer.setSource(mVideoPath);

            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            if (mPlayer != null) {
                mPlayer.stop();
                mPlayer.release();
                mPlayer = null;
            }
            if (mSurface != null) {
                mSurface.release();
                mSurface = null;
            }
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mThumbnailFetcher != null) {
            mThumbnailFetcher.release();
        }
        if (mCoverThumbnailFetcher != null) {
            mCoverThumbnailFetcher.release();
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mIvLeft) {
                onBackPressed();
            } else if (v == mIvRight) {
                if (mLoadingDialog == null) {
                    mLoadingDialog = new AlivcCircleLoadingDialog(CoverEditActivity.this, 0);
                }
                mLoadingDialog.show();
                requestThumbnailCover();
            }
        }
    };

    private void requestThumbnailCover() {
        mCoverThumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher();
        mCoverThumbnailFetcher.addVideoSource(mVideoPath, 0, Integer.MAX_VALUE, 0);
        mCoverThumbnailFetcher.setParameters(mTextureView.getWidth(), mTextureView.getHeight(), AliyunIThumbnailFetcher.CropMode.Mediate, VideoDisplayMode.SCALE, 2);
        mCoverThumbnailFetcher.requestThumbnailImage(new long[] {mEndTime},
        new AliyunIThumbnailFetcher.OnThumbnailCompletion() {
            @Override
            public void onThumbnailReady(Bitmap bitmap, long l) {
                if (bitmap != null && !bitmap.isRecycled()) {
                    String path = getExternalFilesDir(null) + "thumbnail.jpeg";
                    FileOutputStream fileOutputStream = null;
                    try {
                        fileOutputStream = new FileOutputStream(path);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        ToastUtils.show(CoverEditActivity.this, R.string.alivc_editor_cover_fetch_cover_error);
                        mLoadingDialog.dismiss();
                        return;
                    } finally {
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    Intent data = new Intent();
                    data.putExtra(KEY_PARAM_RESULT, path);
                    setResult(RESULT_OK, data);
                    finish();
                    mLoadingDialog.dismiss();
                } else {
                    requestThumbnailCover();
                }
            }

            @Override
            public void onError(int i) {
                ToastUtils.show(CoverEditActivity.this, R.string.alivc_editor_cover_fetch_cover_error);
                mLoadingDialog.dismiss();
            }
        });
    }

}
