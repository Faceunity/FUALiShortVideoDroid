package com.aliyun.svideo.editor.publish;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aliyun.common.global.AliyunTag;
import com.aliyun.querrorcode.AliyunErrorCode;
import com.aliyun.qupai.editor.AliyunIComposeCallBack;
import com.aliyun.qupai.editor.AliyunIVodCompose;
import com.aliyun.svideo.base.ActionInfo;
import com.aliyun.svideo.base.AliyunSvideoActionConfig;
import com.aliyun.svideo.base.Constants;
import com.aliyun.svideo.base.utils.VideoInfoUtils;
import com.aliyun.svideo.common.utils.DateTimeUtils;
import com.aliyun.svideo.common.utils.ThreadUtils;
import com.aliyun.svideo.common.utils.ToastUtils;
import com.aliyun.svideo.common.utils.UriUtils;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.sdk.external.struct.common.VideoDisplayMode;
import com.aliyun.svideo.sdk.external.thumbnail.AliyunIThumbnailFetcher;
import com.aliyun.svideo.sdk.external.thumbnail.AliyunThumbnailFetcherFactory;
import com.duanqu.transcode.NativeParser;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;

/**
 * Created by macpro on 2017/11/6.
 * 视频合成页面
 */

public class PublishActivity extends Activity implements View.OnClickListener {
    private static final String TAG = PublishActivity.class.getName();

    public static final String KEY_PARAM_CONFIG = "project_json_path";
    public static final String KEY_PARAM_THUMBNAIL = "svideo_thumbnail";
    public static final String KEY_PARAM_VIDEO_RATIO = "key_param_video_ratio";
    public static final String KEY_PARAM_VIDEO_WIDTH = "key_param_video_width";
    public static final String KEY_PARAM_VIDEO_HEIGHT = "key_param_video_height";

    private View mActionBar;
    private ImageView mIvLeft;
    private ProgressBar mProgress;
    private ImageView mCoverImage, mCoverBlur;
    private EditText mVideoDesc;
    private View mCoverSelect;
    private View mComposeProgressView;
    private TextView mComposeProgress;
    private View mComposeIndiate;
    private TextView mComposeStatusText, mComposeStatusTip;
    private TextView mPublish;

    private String mOutputPath = "";

    private String mThumbnailPath;
    private AliyunIVodCompose mCompose;
    private boolean mComposeCompleted;
    private AsyncTask<String, Void, Bitmap> mAsyncTaskOnCreate;
    private AsyncTask<String, Void, Bitmap> mAsyncTaskResult;

    private int videoWidth;
    private int videoHeight;

    /**
     * 视频缩略图截取，不同于MediaMetadataRetriever，可精准获取视频非关键帧图片
     */
    private AliyunIThumbnailFetcher aliyunIThumbnailFetcher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alivc_editor_activity_publish);
        initView();
        String config = getIntent().getStringExtra(KEY_PARAM_CONFIG);
        mThumbnailPath = getIntent().getStringExtra(KEY_PARAM_THUMBNAIL);
        videoWidth = getIntent().getIntExtra(KEY_PARAM_VIDEO_WIDTH, 0);
        videoHeight = getIntent().getIntExtra(KEY_PARAM_VIDEO_HEIGHT, 0);

        aliyunIThumbnailFetcher = AliyunThumbnailFetcherFactory.createThumbnailFetcher();

        mCompose = ComposeFactory.INSTANCE.getAliyunVodCompose();
        mCompose.init(this.getApplicationContext());

        //开始合成

        String time = DateTimeUtils.getDateTimeFromMillisecond(System.currentTimeMillis());
        mOutputPath = Constants.SDCardConstants.getDir(this) + time + Constants.SDCardConstants.COMPOSE_SUFFIX;
        int ret = mCompose.compose(config, mOutputPath, mCallback);
        if (ret != AliyunErrorCode.ALIVC_COMMON_RETURN_SUCCESS) {
            return;
        }
        View root = (View) mActionBar.getParent();
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputManager = (InputMethodManager) getApplication()
                                                  .getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputManager.isActive()) {
                    inputManager
                    .hideSoftInputFromWindow(mVideoDesc.getWindowToken(), 0);
                }
            }
        });
        mAsyncTaskOnCreate = new MyAsyncTask(this).execute(mThumbnailPath);
    }

    private void initThumbnail(Bitmap thumbnail) {
        mCoverBlur.setImageBitmap(thumbnail);

        ViewParent parent = mCoverBlur.getParent();
        int width = 0;
        int height = 0;

        //封面的宽 = 背景容器的2/5  高 = 3/5
        if (parent instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) parent;
            width = group.getWidth() * 2 / 5;
            height = group.getHeight() * 3 / 5;
        } else {
            width = mCoverBlur.getWidth() * 2 / 5;
            height = mCoverBlur.getHeight() * 3 / 5;
        }
        FrameLayout.LayoutParams para;
        para = (FrameLayout.LayoutParams) mCoverImage.getLayoutParams();
        para.width = width;
        para.height = height;

        mCoverImage.setLayoutParams(para);
        mCoverImage.setImageBitmap(thumbnail);

    }

    static class MyAsyncTask extends AsyncTask<String, Void, Bitmap> {

        private WeakReference<PublishActivity> ref;
        private float maxWidth;

        MyAsyncTask(PublishActivity activity) {
            ref = new WeakReference<>(activity);
            maxWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                       240, activity.getResources().getDisplayMetrics());
        }

        @Override
        protected Bitmap doInBackground(String... thumbnailPaths) {
            Bitmap bmp = null;
            if (ref != null) {
                PublishActivity publishActivity = ref.get();
                if (publishActivity != null) {
                    String path = thumbnailPaths[0];
                    if (TextUtils.isEmpty(path)) {
                        return null;
                    }
                    File thumbnail = new File(path);
                    if (!thumbnail.exists()) {
                        return null;
                    }
                    BitmapFactory.Options opt = new BitmapFactory.Options();
                    opt.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(path, opt);
                    float bw = opt.outWidth;
                    float bh = opt.outHeight;
                    float scale;
                    if (bw > bh) {
                        scale = bw / maxWidth;
                    } else {
                        scale = bh / maxWidth;
                    }
                    boolean needScaleAfterDecode = scale != 1;
                    opt.inJustDecodeBounds = false;
                    bmp = BitmapFactory.decodeFile(path, opt);
                    if (bmp != null && needScaleAfterDecode) {
                        bmp = publishActivity.scaleBitmap(bmp, scale);
                    }
                }
            }

            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null && ref != null && ref.get() != null) {
                ref.get().initThumbnail(bitmap);
            }
        }
    }

    private Bitmap scaleBitmap(Bitmap bmp, float scale) {
        Matrix mi = new Matrix();
        mi.setScale(1 / scale, 1 / scale);
        Bitmap temp = bmp;
        bmp = Bitmap.createBitmap(temp, 0, 0, temp.getWidth(), temp.getHeight(), mi, false);
        temp.recycle();
        return bmp;
    }

    private void initView() {
        mActionBar = findViewById(R.id.action_bar);
        mActionBar.setBackgroundColor(
            getResources().getColor(R.color.alivc_common_theme_primary_alpha_50));
        mPublish = (TextView) findViewById(R.id.tv_right);
        mIvLeft = (ImageView) findViewById(R.id.iv_left);
        mIvLeft.setOnClickListener(this);
        mIvLeft.setImageResource(R.mipmap.aliyun_svideo_icon_back);
        mPublish.setText(R.string.alivc_editor_publish_tittle);
        mIvLeft.setVisibility(View.VISIBLE);
        mPublish.setVisibility(View.VISIBLE);
        mProgress = (ProgressBar) findViewById(R.id.publish_progress);
        mComposeProgressView = findViewById(R.id.compose_progress_view);
        mCoverBlur = (ImageView) findViewById(R.id.publish_cover_blur);
        mCoverImage = (ImageView) findViewById(R.id.publish_cover_image);
        mVideoDesc = (EditText) findViewById(R.id.publish_desc);
        mComposeIndiate = findViewById(R.id.image_compose_indicator);
        mPublish.setEnabled(mComposeCompleted);
        mPublish.setOnClickListener(this);
        mCoverSelect = findViewById(R.id.publish_cover_select);
        mCoverSelect.setEnabled(mComposeCompleted);
        mCoverSelect.setOnClickListener(this);
        mComposeProgress = (TextView) findViewById(R.id.compose_progress_text);
        mComposeStatusText = (TextView) findViewById(R.id.compose_status_text);
        mComposeStatusTip = (TextView) findViewById(R.id.compose_status_tip);
        mVideoDesc.addTextChangedListener(new TextWatcher() {

            private int start;
            private int end;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                start = mVideoDesc.getSelectionStart();
                end = mVideoDesc.getSelectionEnd();

                int count = count(s.toString());
                // 限定EditText只能输入10个数字
                if (count > 20 && start > 0) {
                    Log.d(AliyunTag.TAG, "超过10个以后的数字");

                    s.delete(start - 1, end);
                    mVideoDesc.setText(s);
                    mVideoDesc.setSelection(s.length());
                }
            }
        });
    }

    private int count(String text) {
        int len = text.length();
        int skip;
        int letter = 0;
        int chinese = 0;
        for (int i = 0; i < len; i += skip) {
            int code = text.codePointAt(i);
            skip = Character.charCount(code);
            if (code == 10) {
                continue;
            }
            String s = text.substring(i, i + skip);
            if (isChinese(s)) {
                chinese++;
            } else {
                letter++;
            }

        }
        letter = letter % 2 == 0 ? letter / 2 : (letter / 2 + 1);
        int result = chinese + letter;
        return result;
    }

    // 完整的判断中文汉字和符号
    private boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }

    private boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if (v == mPublish) {
            mPublish.setEnabled(false);
            String tagClassName = AliyunSvideoActionConfig.getInstance().getAction().getTagClassName(ActionInfo.SVideoAction.EDITOR_TARGET_CLASSNAME);
            Intent intent = new Intent();
            intent.setClassName(this, tagClassName);

            intent.putExtra(UploadActivity.KEY_UPLOAD_THUMBNAIL, mThumbnailPath);
            intent.putExtra(UploadActivity.KEY_PARAM_VIDEO_RATIO, getIntent().getFloatExtra(KEY_PARAM_VIDEO_RATIO, 0f));
            if (!TextUtils.isEmpty(mVideoDesc.getText())) {
                intent.putExtra(UploadActivity.KEY_UPLOAD_DESC, mVideoDesc.getText().toString());
            }
            intent.putExtra(UploadActivity.KEY_UPLOAD_VIDEO, mOutputPath);
            startActivity(intent);
        } else if (v == mCoverSelect) {
            Intent intent = new Intent(this, CoverEditActivity.class);
            intent.putExtra(CoverEditActivity.KEY_PARAM_VIDEO, mOutputPath);
            startActivityForResult(intent, 0);
        } else if (v == mIvLeft) {
            onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        if (mComposeCompleted) {
            super.onBackPressed();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final AlertDialog dialog = builder.setTitle(R.string.alivc_editor_publish_dialog_cancel_content_tip)
            .setNegativeButton(R.string.alivc_editor_publish_goback, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mComposeCompleted) {
                        finish();
                    } else {
                        if (mCompose != null) {
                            mCompose.cancelCompose();
                        }
                        finish();
                    }
                }
            })
            .setPositiveButton(R.string.alivc_editor_publish_continue, null).create();
            dialog.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            mThumbnailPath = data.getStringExtra(CoverEditActivity.KEY_PARAM_RESULT);
            mAsyncTaskResult = new MyAsyncTask(this).execute(mThumbnailPath);
        }
    }

    private final AliyunIComposeCallBack mCallback = new AliyunIComposeCallBack() {
        @Override

        public void onComposeError(int errorCode) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mComposeProgress.setVisibility(View.GONE);
                    mComposeIndiate.setVisibility(View.VISIBLE);
                    mComposeIndiate.setActivated(false);
                    mComposeStatusTip.setText(R.string.alivc_editor_publish_tip_retry);
                    mComposeStatusText.setText(R.string.alivc_editor_publish_compose_failed);
                }
            });
        }

        @Override
        public void onComposeProgress(final int progress) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mComposeProgress.setText(progress + "%");
                    mProgress.setProgress(progress);
                }
            });
        }

        @Override
        public void onComposeCompleted() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //适配android Q
                ThreadUtils.runOnSubThread(new Runnable() {
                    @Override
                    public void run() {
                        UriUtils.saveVideoToMediaStore(PublishActivity.this, mOutputPath);
                    }
                });

            } else {
                MediaScannerConnection.scanFile(getApplicationContext(),
                                                new String[] {mOutputPath}, new String[] {"video/mp4"}, null);
            }
            mComposeCompleted = true;
            aliyunIThumbnailFetcher.addVideoSource(mOutputPath, 0, Integer.MAX_VALUE, 0);
            aliyunIThumbnailFetcher.setParameters(videoWidth, videoHeight,
                                                  AliyunIThumbnailFetcher.CropMode.Mediate, VideoDisplayMode.SCALE, 8);
            requestThumbnailImage(0);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //请务必在非回调线程调用，避免内存泄露
                    if (mCompose != null) {
                        mCompose.release();
                        mCompose = null;
                    }
                }
            });

            VideoInfoUtils.printVideoInfo(mOutputPath);
        }
    };

    private void requestThumbnailImage(int index) {
        Log.e("frameBitmap", "requestThumbnailImage" + index);
        aliyunIThumbnailFetcher.requestThumbnailImage(new long[] {index}, mThumbnailCallback);
    }

    private final AliyunIThumbnailFetcher.OnThumbnailCompletion mThumbnailCallback = new AliyunIThumbnailFetcher.OnThumbnailCompletion() {
        private int vecIndex = 1;
        private int mInterval = 100;

        @Override
        public void onThumbnailReady(Bitmap frameBitmap, long time) {
            if (frameBitmap != null && !frameBitmap.isRecycled()) {
                Log.e("frameBitmap", "isRecycled");
                mCoverImage.setVisibility(View.VISIBLE);
                initThumbnail(frameBitmap);
                mPublish.setEnabled(mComposeCompleted);
                mProgress.setVisibility(View.GONE);
                mComposeProgress.setVisibility(View.GONE);

                mComposeIndiate.setVisibility(View.VISIBLE);
                mComposeIndiate.setActivated(true);
                mComposeStatusTip.setVisibility(View.GONE);
                mComposeStatusText.setText(R.string.alivc_editor_publish_compose_success);
                mComposeProgressView.postDelayed(composeProgressRunnable, 2000);
            } else {
                vecIndex = vecIndex + mInterval;
                requestThumbnailImage(vecIndex);
            }


        }

        @Override
        public void onError(int errorCode) {
            Log.d(TAG, "fetcher onError " + errorCode);
            ToastUtils.show(PublishActivity.this, R.string.alivc_editor_cover_fetch_cover_error);
        }
    };

    private Runnable composeProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mComposeProgressView != null) {
                mComposeProgressView.setVisibility(View.GONE);
            }
            if (mCoverSelect != null) {
                mCoverSelect.setVisibility(View.VISIBLE);
                mCoverSelect.setEnabled(mComposeCompleted);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (mCompose != null) {
            mCompose.resumeCompose();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCompose != null) {
            mCompose.pauseCompose();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        aliyunIThumbnailFetcher.release();

        if (mCompose != null) {
            mCompose.release();
            mCompose = null;
        }
        if (mComposeProgressView != null) {
            mComposeProgressView.removeCallbacks(composeProgressRunnable);
        }

        if (mAsyncTaskOnCreate != null) {
            mAsyncTaskOnCreate.cancel(true);
            mAsyncTaskOnCreate = null;
        }

        if (mAsyncTaskResult != null) {
            mAsyncTaskResult.cancel(true);
            mAsyncTaskResult = null;
        }
    }

}
