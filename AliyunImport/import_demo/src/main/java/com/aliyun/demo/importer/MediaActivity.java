/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.importer;

import android.app.Activity;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.aliyun.common.utils.ToastUtil;
import com.aliyun.demo.crop.AliyunImageCrop;
import com.aliyun.demo.crop.AliyunVideoCrop;
import com.aliyun.demo.importer.media.GalleryMediaChooser;
import com.aliyun.demo.importer.media.MediaStorage;
import com.aliyun.jasonparse.JSONSupportImpl;
import com.aliyun.querrorcode.AliyunErrorCode;
import com.aliyun.qupai.import_core.AliyunIImport;
import com.aliyun.qupai.import_core.AliyunImportCreator;
import com.aliyun.quview.ProgressDialog;
import com.aliyun.struct.common.AliyunDisplayMode;
import com.aliyun.struct.common.AliyunVideoParam;
import com.aliyun.struct.common.CropKey;
import com.aliyun.struct.common.ScaleMode;
import com.aliyun.struct.common.VideoQuality;

import java.util.List;

import com.aliyun.demo.importer.media.GalleryDirChooser;
import com.aliyun.demo.importer.media.MediaDir;
import com.aliyun.demo.importer.media.MediaInfo;
import com.aliyun.demo.importer.media.SelectedMediaAdapter;
import com.aliyun.demo.importer.media.SelectedMediaViewHolder;
import com.aliyun.demo.importer.media.ThumbnailGenerator;


public class MediaActivity extends Activity implements View.OnClickListener {
    private static final int[][] resolutions = new int[][]{new int[]{540, 720}, new int[]{540, 540}, new int[]{540, 960}};

    private static final int REQUEST_CODE_VIDEO_CROP = 1;
    private static final int REQUEST_CODE_IMAGE_CROP = 2;
    private MediaStorage storage;
    private ProgressDialog progressDialog;
    private GalleryDirChooser galleryDirChooser;
    private ThumbnailGenerator thumbnailGenerator;
    private GalleryMediaChooser galleryMediaChooser;
    private RecyclerView galleryView;
    private RecyclerView mRvSelectedVideo;
    private TextView mTvTotalDuration;
    private ImageButton back;
    private TextView title;
    private int mRatio;
    private ScaleMode scaleMode = ScaleMode.LB;
    private int frameRate;
    private int gop;
    private int mBitrate;
    private VideoQuality quality = VideoQuality.SSD;
    private SelectedMediaAdapter mSelectedVideoAdapter;
    private AliyunIImport mImport;
    private Transcoder mTransCoder;
    private MediaInfo mCurrMediaInfo;
    private int mCropPosition;
    private boolean mIsReachedMaxDuration = false;
    private AliyunVideoParam mVideoParam;
    private int[] mOutputResolution = null;
    private Button mBtnNextStep;

    private int requestWidth;
    private int requestHeight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aliyun_svideo_import_activity_media);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getData();
        init();
    }

    private void getData() {
        mRatio = getIntent().getIntExtra(CropKey.VIDEO_RATIO, CropKey.RATIO_MODE_3_4);
        scaleMode = (ScaleMode) getIntent().getSerializableExtra(CropKey.VIDEO_SCALE);
        if (scaleMode == null) {
            scaleMode = ScaleMode.LB;
        }
        frameRate = getIntent().getIntExtra(CropKey.VIDEO_FRAMERATE, 25);
        gop = getIntent().getIntExtra(CropKey.VIDEO_GOP, 125);
        mBitrate = getIntent().getIntExtra(CropKey.VIDEO_BITRATE, 0);
        quality = (VideoQuality) getIntent().getSerializableExtra(CropKey.VIDEO_QUALITY);
        if (quality == null) {
            quality = VideoQuality.SSD;
        }
        mOutputResolution = resolutions[mRatio];
        mVideoParam = new AliyunVideoParam.Builder()
                .frameRate(frameRate)
                .gop(gop)
                .bitrate(mBitrate)
                .videoQuality(quality)
                .scaleMode(scaleMode)
                .outputWidth(mOutputResolution[0])
                .outputHeight(mOutputResolution[1])
                .build();
        try {
            requestWidth = Integer.parseInt(getIntent().getStringExtra("width"));
        } catch (Exception e) {
            requestWidth = 0;
        }
        try {
            requestHeight = Integer.parseInt(getIntent().getStringExtra("height"));
        } catch (Exception e) {
            requestHeight = 0;
        }

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
                mImport = AliyunImportCreator.getImportInstance(MediaActivity.this);
                mImport.setVideoParam(mVideoParam);
                for (int i = 0; i < resultVideos.size(); i++) {
                    MediaInfo mediaInfo = resultVideos.get(i);
                    if (i == 0) {
                        if (mediaInfo.mimeType.startsWith("video")) {
                            mImport.addVideo(mediaInfo.filePath, mediaInfo.startTime, mediaInfo.startTime + mediaInfo.duration, 0, AliyunDisplayMode.DEFAULT);
                        } else if (mediaInfo.mimeType.startsWith("image")) {
                            mImport.addImage(mediaInfo.filePath, 0, 5000, AliyunDisplayMode.DEFAULT);
                        }
                    } else {
                        if (mediaInfo.mimeType.startsWith("video")) {
                            mImport.addVideo(mediaInfo.filePath, mediaInfo.startTime, mediaInfo.startTime + mediaInfo.duration, 600, AliyunDisplayMode.DEFAULT);
                        } else if (mediaInfo.mimeType.startsWith("image")) {
                            mImport.addImage(mediaInfo.filePath, 600, 5000, AliyunDisplayMode.DEFAULT);
                        }
                    }

                }
                String projectJsonPath = mImport.generateProjectConfigure();
                if (projectJsonPath != null) {
                    Intent intent = new Intent("com.duanqu.qupai.action.editor");
                    intent.putExtra("video_param", mVideoParam);
                    intent.putExtra("project_json_path", projectJsonPath);
                    startActivity(intent);
                }
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
        mBtnNextStep = (Button) findViewById(R.id.btn_next_step);
        galleryView = (RecyclerView) findViewById(R.id.gallery_media);
        title = (TextView) findViewById(R.id.gallery_title);
        title.setText(R.string.gallery_all_media);
        back = (ImageButton) findViewById(R.id.gallery_closeBtn);
        back.setOnClickListener(this);
        storage = new MediaStorage(this, new JSONSupportImpl());
        thumbnailGenerator = new ThumbnailGenerator(this);
        galleryDirChooser = new GalleryDirChooser(this, findViewById(R.id.topPanel), thumbnailGenerator, storage);
        galleryMediaChooser = new GalleryMediaChooser(galleryView, galleryDirChooser, storage, thumbnailGenerator);
        storage.setSortMode(MediaStorage.SORT_MODE_MERGE);
        storage.startFetchmedias();
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
                if (info.mimeType.startsWith("image")) {
                    infoCopy.duration = 5000;//图片的时长设置为3s
                } else {
                    infoCopy.duration = info.duration;
                }
                infoCopy.filePath = info.filePath;
                infoCopy.id = info.id;
                infoCopy.isSquare = info.isSquare;
                infoCopy.mimeType = info.mimeType;
                infoCopy.thumbnailPath = info.thumbnailPath;
                infoCopy.title = info.title;
                infoCopy.type = info.type;
                mSelectedVideoAdapter.addMedia(infoCopy);
//                mImport.addVideo(infoCopy.filePath, 3000, AliyunDisplayMode.DEFAULT);    //导入器中添加视频
                mTransCoder.addMedia(infoCopy);
            }
        });
        mRvSelectedVideo = (RecyclerView) findViewById(R.id.rv_selected_video);
        mTvTotalDuration = (TextView) findViewById(R.id.tv_duration_value);
        mSelectedVideoAdapter = new SelectedMediaAdapter(new MediaImageLoader(this), 5 * 60 * 1000);//最大时长5分钟
        mRvSelectedVideo.setAdapter(mSelectedVideoAdapter);
        mRvSelectedVideo.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
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
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                //滑动事件
                mSelectedVideoAdapter.swap((SelectedMediaViewHolder) viewHolder, (SelectedMediaViewHolder) target);
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
        itemTouchHelper.attachToRecyclerView(mRvSelectedVideo);
        mSelectedVideoAdapter.setItemViewCallback(new SelectedMediaAdapter.OnItemViewCallback() {
            @Override
            public void onItemPhotoClick(MediaInfo info, int position) {
                mCurrMediaInfo = info;
                mCropPosition = position;
                if (info.mimeType.startsWith("video")) {
                    Intent intent = new Intent(MediaActivity.this, AliyunVideoCrop.class);
                    intent.putExtra(CropKey.VIDEO_PATH, info.filePath);
                    intent.putExtra(CropKey.VIDEO_DURATION, info.duration);
                    intent.putExtra(CropKey.VIDEO_RATIO, mRatio);
                    intent.putExtra(CropKey.VIDEO_SCALE, scaleMode);
                    intent.putExtra(CropKey.VIDEO_QUALITY, quality);
                    intent.putExtra(CropKey.VIDEO_GOP, gop);
                    intent.putExtra(CropKey.VIDEO_BITRATE, mBitrate);
                    intent.putExtra(CropKey.VIDEO_FRAMERATE, frameRate);
                    intent.putExtra(CropKey.ACTION, CropKey.ACTION_SELECT_TIME);//是否真裁剪
                    startActivityForResult(intent, REQUEST_CODE_VIDEO_CROP);
                } else if (info.mimeType.startsWith("image")) {
                    Intent intent = new Intent(MediaActivity.this, AliyunImageCrop.class);
                    intent.putExtra(CropKey.VIDEO_PATH, info.filePath);
                    intent.putExtra(CropKey.VIDEO_DURATION, info.duration);
                    intent.putExtra(CropKey.VIDEO_RATIO, mRatio);
                    intent.putExtra(CropKey.VIDEO_SCALE, scaleMode);
                    intent.putExtra(CropKey.VIDEO_QUALITY, quality);
                    intent.putExtra(CropKey.VIDEO_GOP, gop);
                    intent.putExtra(CropKey.VIDEO_BITRATE, mBitrate);
                    intent.putExtra(CropKey.VIDEO_FRAMERATE, frameRate);
                    startActivityForResult(intent, REQUEST_CODE_IMAGE_CROP);
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
                        mCurrMediaInfo.duration = (int) duration;
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
            }

        }
    }

    private String convertDuration2Text(long duration) {
        int sec = Math.round(((float) duration) / 1000);
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
                progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mBtnNextStep.setEnabled(false);//为了防止未取消成功的情况下就开始下一次转码，这里在取消转码成功前会禁用下一步按钮
                        mTransCoder.cancel();
                    }
                });
                mTransCoder.init(this);
                mTransCoder.setTransResolution(requestWidth, requestHeight);
                mTransCoder.transcode(mOutputResolution, quality, scaleMode);
            } else {
                ToastUtil.showToast(this, R.string.please_select_video);
            }
        }
    }
}
