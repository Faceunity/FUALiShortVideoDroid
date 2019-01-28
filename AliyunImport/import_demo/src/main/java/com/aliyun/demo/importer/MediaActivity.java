/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.importer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.svideo.base.AlivcEditorRoute;
import com.aliyun.svideo.base.AlivcSvideoEditParam;
import com.aliyun.svideo.base.MediaInfo;
import com.aliyun.common.global.AliyunTag;
import com.aliyun.common.utils.ToastUtil;
import com.aliyun.demo.crop.AliyunImageCropActivity;
import com.aliyun.demo.crop.AliyunVideoCropActivity;
import com.aliyun.demo.importer.media.FixedToastUtils;
import com.aliyun.demo.importer.media.GalleryDirChooser;
import com.aliyun.demo.importer.media.GalleryMediaChooser;
import com.aliyun.demo.importer.media.MediaDir;
import com.aliyun.demo.importer.media.MediaStorage;
import com.aliyun.demo.importer.media.SelectedMediaAdapter;
import com.aliyun.demo.importer.media.SelectedMediaViewHolder;
import com.aliyun.demo.importer.media.ThumbnailGenerator;
import com.aliyun.jasonparse.JSONSupportImpl;
import com.aliyun.querrorcode.AliyunErrorCode;
import com.aliyun.svideo.base.widget.ProgressDialog;
import com.aliyun.svideo.sdk.external.struct.common.CropKey;
import com.aliyun.svideo.sdk.external.struct.common.VideoDisplayMode;
import com.aliyun.svideo.sdk.external.struct.common.VideoQuality;
import com.duanqu.transcode.NativeParser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;



public class MediaActivity extends Activity implements View.OnClickListener {

    private static final int REQUEST_CODE_VIDEO_CROP = 1;
    private static final int REQUEST_CODE_IMAGE_CROP = 2;
    private static final int IMAGE_DURATION = 3000;//图片代表的时长


    private MediaStorage storage;
    private ProgressDialog progressDialog;
    private ThumbnailGenerator thumbnailGenerator;
    private GalleryMediaChooser galleryMediaChooser;
    private TextView mTvTotalDuration;
    private ImageButton back;
    private TextView title;

    private SelectedMediaAdapter mSelectedVideoAdapter;
    private Transcoder mTransCoder;
    private MediaInfo mCurrMediaInfo;
    private int mCropPosition;
    private boolean mIsReachedMaxDuration = false;

    private Button mBtnNextStep;

    private int requestWidth;
    private int requestHeight;

    private AlivcSvideoEditParam mSvideoParam;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aliyun_svideo_import_activity_media);
        getData();
        init();
    }

    private void getData() {
        int mRatio = getIntent().getIntExtra(AlivcSvideoEditParam.VIDEO_RATIO, AlivcSvideoEditParam.RATIO_MODE_3_4);
        int mResolutionMode = getIntent().getIntExtra(AlivcSvideoEditParam.VIDEO_RESOLUTION, AlivcSvideoEditParam.RESOLUTION_540P);
        boolean hasTailAnimation = getIntent().getBooleanExtra(AlivcSvideoEditParam.TAIL_ANIMATION, false);
        String entrance = getIntent().getStringExtra(AlivcSvideoEditParam.INTENT_PARAM_KEY_ENTRANCE);
        VideoDisplayMode scaleMode = (VideoDisplayMode)getIntent().getSerializableExtra(AlivcSvideoEditParam.VIDEO_CROP_MODE);
        if (scaleMode == null) {
            scaleMode = VideoDisplayMode.FILL;
        }
        int frameRate = getIntent().getIntExtra(AlivcSvideoEditParam.VIDEO_FRAMERATE, 25);
        int gop = getIntent().getIntExtra(AlivcSvideoEditParam.VIDEO_GOP, 125);
        int mBitrate = getIntent().getIntExtra(AlivcSvideoEditParam.VIDEO_BITRATE, 0);
        VideoQuality quality = (VideoQuality)getIntent().getSerializableExtra(AlivcSvideoEditParam.VIDEO_QUALITY);
        if (quality == null) {
            quality = VideoQuality.SSD;
        }
        mSvideoParam = new AlivcSvideoEditParam.Build()
            .setRatio(mRatio)
            .setResolutionMode(mResolutionMode)
            .setHasTailAnimation(hasTailAnimation)
            .setEntrance(entrance)
            .setCropMode(scaleMode)
            .setFrameRate(frameRate)
            .setGop(gop)
            .setBitrate(mBitrate)
            .setVideoQuality(quality)
            .build();
    }

    private void init() {
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
                            case AliyunErrorCode.ERROR_MEDIA_NOT_SUPPORTED_AUDIO:
                                ToastUtil.showToast(MediaActivity.this, R.string.aliyun_not_supported_audio);
                                break;
                            case AliyunErrorCode.ERROR_MEDIA_NOT_SUPPORTED_VIDEO:
                                ToastUtil.showToast(MediaActivity.this, R.string.aliyun_video_crop_error);
                                break;
                            case AliyunErrorCode.ERROR_UNKNOWN:
                            default:
                                ToastUtil.showToast(MediaActivity.this, R.string.aliyun_video_error);
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
                //跳转EditorActivity
                AlivcEditorRoute.startEditorActivity(MediaActivity.this, mSvideoParam, (ArrayList<MediaInfo>) resultVideos);
            }

            @Override
            public void onCancelComplete() {
                //取消完成
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBtnNextStep.setEnabled(true);
                    }
                });
            }
        });
        mBtnNextStep = (Button)findViewById(R.id.btn_next_step);
        RecyclerView galleryView = (RecyclerView) findViewById(R.id.gallery_media);
        title = (TextView)findViewById(R.id.gallery_title);
        title.setText(R.string.gallery_all_media);
        back = (ImageButton)findViewById(R.id.gallery_closeBtn);
        back.setOnClickListener(this);
        storage = new MediaStorage(this, new JSONSupportImpl());
        thumbnailGenerator = new ThumbnailGenerator(this);
        GalleryDirChooser galleryDirChooser = new GalleryDirChooser(this, findViewById(R.id.topPanel),
            thumbnailGenerator, storage);
        galleryMediaChooser = new GalleryMediaChooser(galleryView, galleryDirChooser, storage, thumbnailGenerator);
        storage.setSortMode(MediaStorage.SORT_MODE_MERGE);
        try {
            storage.startFetchmedias();
        } catch (SecurityException e) {
            FixedToastUtils.show(MediaActivity.this, "没有权限");
        }
        storage.setOnMediaDirChangeListener(new MediaStorage.OnMediaDirChange() {
            @Override
            public void onMediaDirChanged() {
                MediaDir dir = storage.getCurrentDir();
                if (dir.id == -1) {
                    title.setText(getString(R.string.gallery_all_media));
                } else {
                    title.setText(dir.dirName);
                }
                galleryMediaChooser.changeMediaDir(dir);
            }
        });
        storage.setOnCurrentMediaInfoChangeListener(new MediaStorage.OnCurrentMediaInfoChange() {
            @Override
            public void onCurrentMediaInfoChanged(MediaInfo info) {
                MediaInfo infoCopy = new MediaInfo();
                infoCopy.addTime = info.addTime;
                infoCopy.mimeType = info.mimeType;
                String outputPath = null;
                if (info.mimeType.startsWith("image")) {
                    if (info.filePath.endsWith("gif")||info.filePath.endsWith("GIF")) {
                        NativeParser parser = new NativeParser();
                        parser.init(info.filePath);
                        int frameCount = Integer.parseInt(parser.getValue(NativeParser.VIDEO_FRAME_COUNT));
                        //当gif动图为一帧的时候当作图片处理，否则当作视频处理
                        if (frameCount>1){
                            infoCopy.mimeType="video";
                            infoCopy.duration = Integer.parseInt(parser.getValue(NativeParser
                                .VIDEO_DURATION)) / 1000;
                        }else {
                            infoCopy.duration = IMAGE_DURATION;
                        }

                    } else {
                        infoCopy.duration = IMAGE_DURATION;
                    }

                } else {
                    infoCopy.duration = info.duration;
                }
                if (outputPath != null) {
                    infoCopy.filePath = outputPath;//info.filePath;
                } else {
                    infoCopy.filePath = info.filePath;
                }
                infoCopy.id = info.id;
                infoCopy.isSquare = info.isSquare;
                infoCopy.thumbnailPath = info.thumbnailPath;
                infoCopy.title = info.title;
                infoCopy.type = info.type;
                //第一次添加并且分辨率为原比例的时候，计算视频真实宽高
                if (mSelectedVideoAdapter.getItemCount() == 0 ) {
                    requestWidth = mSvideoParam.getVideoWidth();
                    requestHeight = mSvideoParam.getVideoHeight(info);
                }
                mSelectedVideoAdapter.addMedia(infoCopy);
                //                mImport.addVideo(infoCopy.filePath, 3000, AliyunDisplayMode.DEFAULT);    //导入器中添加视频
                mTransCoder.addMedia(infoCopy);
            }
        });
        RecyclerView rvSelectedVideo = (RecyclerView) findViewById(R.id.rv_selected_video);
        mTvTotalDuration = (TextView)findViewById(R.id.tv_duration_value);
        mSelectedVideoAdapter = new SelectedMediaAdapter(new MediaImageLoader(this), 5 * 60 * 1000);//最大时长5分钟
        rvSelectedVideo.setAdapter(mSelectedVideoAdapter);
        rvSelectedVideo.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mTvTotalDuration.setText(convertDuration2Text(0));
        mTvTotalDuration.setActivated(false);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                //首先回调的方法 返回int表示是否监听该方向
                int dragFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;//拖拽
                int swipeFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;//侧滑删除
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                //滑动事件
                mSelectedVideoAdapter.swap((SelectedMediaViewHolder)viewHolder, (SelectedMediaViewHolder)target);
                //mImport.swap(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                mTransCoder.swap(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return false;
            }

            @Override
            public boolean isLongPressDragEnabled() {
                //是否可拖拽
                return true;
            }
        });

        itemTouchHelper.attachToRecyclerView(rvSelectedVideo);
        mSelectedVideoAdapter.setItemViewCallback(new SelectedMediaAdapter.OnItemViewCallback() {
            @Override
            public void onItemPhotoClick(MediaInfo info, int position) {
                mCurrMediaInfo = info;
                mCropPosition = position;
                if (info.filePath.endsWith("gif")||info.filePath.endsWith("GIF")){
                    Toast.makeText(MediaActivity.this, R.string.alivc_tip_crop_gif, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (info.mimeType.startsWith("video")) {
                    mSvideoParam.setMediaInfo(info);
                    AliyunVideoCropActivity.startCropForResult(MediaActivity.this, mSvideoParam,REQUEST_CODE_VIDEO_CROP );
                } else if (info.mimeType.startsWith("image")) {
                    mSvideoParam.setMediaInfo(info);
                    AliyunImageCropActivity.startImageCropForResult(MediaActivity.this, mSvideoParam, REQUEST_CODE_IMAGE_CROP);
                }
            }

            @Override
            public void onItemDeleteClick(MediaInfo info) {
                //                mImport.removeVideo(info.filePath); //从导入器中移除视频
                mTransCoder.removeMedia(info);
            }

            @Override
            public void onDurationChange(long currDuration, boolean isReachedMaxDuration) {
                mTvTotalDuration.setText(convertDuration2Text(currDuration));
                mTvTotalDuration.setActivated(isReachedMaxDuration);
                mIsReachedMaxDuration = isReachedMaxDuration;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            String path = data.getStringExtra(CropKey.RESULT_KEY_CROP_PATH);
            switch (requestCode) {
                case REQUEST_CODE_VIDEO_CROP:
                    long duration = data.getLongExtra(CropKey.RESULT_KEY_DURATION, 0);
                    long startTime = data.getLongExtra(CropKey.RESULT_KEY_START_TIME, 0);
                    if (!TextUtils.isEmpty(path) && duration > 0 && mCurrMediaInfo != null) {
                        mSelectedVideoAdapter.changeDurationPosition(mCropPosition, duration);
                        int index = mTransCoder.removeMedia(mCurrMediaInfo);
                        mCurrMediaInfo.filePath = path;
                        mCurrMediaInfo.startTime = startTime;
                        mCurrMediaInfo.duration = (int)duration;
                        mTransCoder.addMedia(index, mCurrMediaInfo);
                    }
                    break;
                case REQUEST_CODE_IMAGE_CROP:
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
    private String convertDuration2Text(long duration) {
        int sec = Math.round(((float)duration) / 1000);
        int hour = sec / 3600;
        int min = (sec % 3600) / 60;
        sec = (sec % 60);
        return String.format(getString(R.string.video_duration),
            hour,
            min,
            sec);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        storage.saveCurrentDirToCache();
        storage.cancelTask();
        mTransCoder.release();
        thumbnailGenerator.cancelAllTask();
    }

    @Override
    public void onClick(View v) {
        if (v == back) {
            finish();
        } else if (v.getId() == R.id.btn_next_step) {//点击下一步

            if (mIsReachedMaxDuration) {
                ToastUtil.showToast(MediaActivity.this, R.string.message_max_duration_import);
                return;
            }
            //对于大于720P的视频需要走转码流程

            int videoCount = mTransCoder.getVideoCount();
            if (videoCount > 0) {
                progressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.wait));
                progressDialog.setCancelable(true);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mBtnNextStep.setEnabled(false);//为了防止未取消成功的情况下就开始下一次转码，这里在取消转码成功前会禁用下一步按钮
                        mTransCoder.cancel();
                    }
                });
                mTransCoder.init(this);
                mTransCoder.setTransResolution(requestWidth, requestHeight);
                mTransCoder.transcode(mSvideoParam.getVideoQuality(), mSvideoParam.getCropMode());
            } else {
                ToastUtil.showToast(this, R.string.please_select_video);
            }
        }
    }
    public static void startImport(Context context,AlivcSvideoEditParam param){
        Intent intent = new Intent(context,MediaActivity.class);
        intent.putExtra(AlivcSvideoEditParam.VIDEO_BITRATE, param.getBitrate());
        intent.putExtra(AlivcSvideoEditParam.VIDEO_FRAMERATE, param.getFrameRate());
        intent.putExtra(AlivcSvideoEditParam.VIDEO_GOP, param.getGop());
        intent.putExtra(AlivcSvideoEditParam.VIDEO_RATIO, param.getRatio());
        intent.putExtra(AlivcSvideoEditParam.VIDEO_QUALITY, param.getVideoQuality());
        intent.putExtra(AlivcSvideoEditParam.VIDEO_RESOLUTION, param.getResolutionMode());
        intent.putExtra(AlivcSvideoEditParam.VIDEO_CROP_MODE, param.getCropMode());
        intent.putExtra(AlivcSvideoEditParam.TAIL_ANIMATION, param.isHasTailAnimation());
        intent.putExtra(AlivcSvideoEditParam.INTENT_PARAM_KEY_ENTRANCE,"svideo" );
        context.startActivity(intent);
    }

}
