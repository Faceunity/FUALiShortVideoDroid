/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.importer;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.aliyun.common.media.AliyunMediaExtractor;
import com.aliyun.crop.AliyunCropCreator;
import com.aliyun.crop.struct.CropParam;
import com.aliyun.crop.supply.CropCallback;
import com.aliyun.crop.supply.AliyunICrop;
import com.aliyun.struct.common.ScaleMode;
import com.aliyun.struct.common.VideoQuality;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.aliyun.demo.importer.media.MediaInfo;
import com.aliyun.svideo.sdk.external.struct.MediaType;
import com.duanqu.qupai.adaptive.NativeAdaptiveUtil;

/**
 * 对于大于540P的视频要先走转码
 */

public class Transcoder {

    private static final String TAG = "Transcoder";
    private List<MediaInfo> mOriginalVideos = new ArrayList<>();
    private List<CropParam> mTranscodeVideos = new ArrayList<>();
    private static final String TRANSCODE_SUFFIX = "_transcode";
    private AliyunICrop mQuCrop;
    private TransCallback mTransCallback;
    private int mTranscodeIndex = 0;
    private int mTranscodeTotal = 0;
    private boolean isTranscode;
    private AsyncTask<Void, Long, List<MediaInfo>> mTranscodeTask;
    private int width = 1920, height = 1920;
    private AliyunMediaExtractor mExtractor = new AliyunMediaExtractor();

    public void addMedia(MediaInfo mediaInfo) {
        mOriginalVideos.add(mediaInfo);
    }

    public boolean isTranscode(){
        return isTranscode;
    }

    public void addMedia(int index, MediaInfo mediaInfo) {
        mOriginalVideos.add(index, mediaInfo);
    }

    public int removeMedia(MediaInfo mediaInfo) {
        int index = mOriginalVideos.indexOf(mediaInfo);
        mOriginalVideos.remove(mediaInfo);
        return index;
    }

    public void setTransResolution(int width, int height) {
        if (width > 0) {
            this.width = width;
        }
        if (height > 0) {
            this.height = height;
        }
    }

    public void swap(int pos1, int pos2) {
        if (pos1 != pos2 && pos1 < mOriginalVideos.size() && pos2 < mOriginalVideos.size()) {
            Collections.swap(mOriginalVideos, pos1, pos2);
        }
    }

    public int getVideoCount() {
        return mOriginalVideos.size();
    }

    public void init(Context context) {
        mQuCrop = AliyunCropCreator.getCropInstance(context);
    }

    public void setTransCallback(TransCallback callback) {
        this.mTransCallback = callback;
    }

    public void transcode(final int[] resolution, final VideoQuality videoQuality,
                          final ScaleMode scaleMode) {
        mTranscodeTotal = 0;
        mTranscodeIndex = 0;
        mTranscodeVideos.clear();
        if (mQuCrop == null) {
            return;
        }
        mTranscodeTask = new AsyncTask<Void, Long, List<MediaInfo>>() {

            @Override
            protected List<MediaInfo> doInBackground(Void... params) {
                CropParam cropParam = null;
                for (MediaInfo info : mOriginalVideos) {
                    if (info.mimeType.startsWith("video")) {
                        if(!NativeAdaptiveUtil.isDeviceDecoderEnable()){
                            cropParam = loadVideoCropInfo(info, scaleMode, videoQuality);
                        }
                    } else if (info.mimeType.startsWith("image")) {
                        cropParam = loadImageCropInfo(info, scaleMode, videoQuality);
                    }
                    if (cropParam != null) {
                        mTranscodeVideos.add(cropParam);
                        mTranscodeTotal++;
                    }
                }

                if (isCancelled()) {
                    return null;
                }
                if (mTranscodeVideos.size() > 0) {
                    transcodeVideo(0);
                } else {
                    if (mTransCallback != null) {
                        mTransCallback.onComplete(mOriginalVideos);
                    }
                }
                return null;
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return;
    }

    public void cancel() {
        mTranscodeTask.cancel(true);
        mQuCrop.cancel();
        if (mTransCallback != null) {
            mTransCallback.onCancelComplete();
        }
    }

    private void transcodeVideo(int index) {
        mTranscodeIndex++;
        CropParam cropParam = mTranscodeVideos.get(index);
        mQuCrop.setCropParam(cropParam);
        mQuCrop.setCropCallback(mTranscodeCallback);
        mQuCrop.startCrop();
        Log.d(TAG, "startCrop...path..." + cropParam.getInputPath());
    }


    public interface TransCallback {
        void onError(Throwable e, int errorCode);

        void onProgress(int progress);

        void onComplete(List<MediaInfo> resultVideos);

        void onCancelComplete();
    }

    private CropCallback mTranscodeCallback = new CropCallback() {
        @Override
        public void onProgress(int percent) {
            int progress = (int) ((mTranscodeIndex - 1) / (float) mTranscodeTotal * 100 + percent / (float) mTranscodeTotal);
            Log.d(TAG, "progress..." + progress);
            if (mTransCallback != null) {
                mTransCallback.onProgress(progress);
            }
        }

        @Override
        public void onError(int code) {
            if (mTransCallback != null) {
                mTransCallback.onError(new Throwable("transcode error, error code = " + code), code);
            }
        }

        @Override
        public void onComplete(long duration) {
            if (mTranscodeIndex < mTranscodeVideos.size()) {
                transcodeVideo(mTranscodeIndex);
            } else {
                if (mTransCallback != null) {
                    replaceOutputPath();
                    isTranscode = true;
                    mTransCallback.onComplete(mOriginalVideos);
                }
            }
        }

        @Override
        public void onCancelComplete() {

        }
    };

    private CropParam loadVideoCropInfo(MediaInfo info, ScaleMode scaleMode, VideoQuality videoQuality) {
        int frameWidth = 0;
        int frameHeight = 0;
        long duration = 0;
        int frameRate = 0;
        int rotation = 0;
        try {
            mExtractor.setDataSource(info.filePath);
            frameWidth = mExtractor.getVideoWidth();
            frameHeight = mExtractor.getVideoHeight();
            frameRate = mExtractor.getFrameRate();
            rotation = mExtractor.getRotation();
            duration = mQuCrop.getVideoDuration(info.filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (frameWidth * frameHeight > width * height || frameRate > 30) {
            Log.d(TAG, "need transcode...path..." + info.filePath);
            CropParam cropParam = new CropParam();
            cropParam.setInputPath(info.filePath);
            StringBuilder sb = new StringBuilder(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).getAbsolutePath())
                    .append(File.separator).append(System.currentTimeMillis()).append(".mp4_transcode");
            cropParam.setOutputPath(sb.toString());//info.filePath + TRANSCODE_SUFFIX);
            int outputWidth = 0;
            int outputHeight = 0;
            if (frameWidth * frameHeight > width * height) {
                if (frameWidth > frameHeight) {
                    outputWidth = Math.min(width, height);
                    outputHeight = (int) ((float) frameHeight / frameWidth * outputWidth);
                } else {
                    outputHeight = Math.min(width, height);
                    outputWidth = (int) ((float) frameWidth / frameHeight * outputHeight);
                }
            }
            if (frameRate > 30) {
                outputWidth = frameWidth;
                outputHeight = frameHeight;
            }

            int temp;
            if (rotation == 90 || rotation == 270) {
                temp = outputWidth;
                outputWidth = outputHeight;
                outputHeight = temp;
            }
            cropParam.setOutputHeight(outputHeight);
            cropParam.setOutputWidth(outputWidth);
            if (rotation == 90 || rotation == 270) {
                cropParam.setCropRect(new Rect(0, 0, frameHeight, frameWidth));
            } else {
                cropParam.setCropRect(new Rect(0, 0, frameWidth, frameHeight));
            }
            cropParam.setScaleMode(scaleMode);
            cropParam.setQuality(videoQuality);
            cropParam.setFrameRate(30);
            cropParam.setStartTime(0);
            cropParam.setEndTime(duration);
            cropParam.setMediaType(MediaType.ANY_VIDEO_TYPE);
            return cropParam;
        }
        return null;
    }

    private CropParam loadImageCropInfo(MediaInfo info, ScaleMode scaleMode, VideoQuality quality) {
        int frameWidth = 0;
        int frameHeight = 0;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(info.filePath, options);
        frameWidth = options.outWidth;
        frameHeight = options.outHeight;

        CropParam cropParam = new CropParam();
        cropParam.setInputPath(info.filePath);
        StringBuilder sb = new StringBuilder(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM).getAbsolutePath())
                .append(File.separator).append(System.currentTimeMillis())
                .append(options.outMimeType.replace("image/", "."));
        cropParam.setOutputPath(sb.toString());//info.filePath + TRANSCODE_SUFFIX);

        int outputWidth = 0;
        int outputHeight = 0;
        if (frameWidth * frameHeight > width * height) {
            if (frameWidth > frameHeight) {
                outputWidth = Math.min(width, height);
                outputHeight = (int) ((float) frameHeight / frameWidth * outputWidth);
            } else {
                outputHeight = Math.min(width, height);
                outputWidth = (int) ((float) frameWidth / frameHeight * outputHeight);
            }
            cropParam.setOutputHeight(outputHeight);
            cropParam.setOutputWidth(outputWidth);
            cropParam.setCropRect(new Rect(0, 0, frameWidth, frameHeight));
            cropParam.setScaleMode(scaleMode);
            cropParam.setQuality(quality);
            cropParam.setFrameRate(30);
            cropParam.setStartTime(0);
            cropParam.setEndTime(info.duration);
            cropParam.setMediaType(MediaType.ANY_IMAGE_TYPE);
            return cropParam;
        }
        return null;
    }

    private void replaceOutputPath() {
        for (CropParam cropParam : mTranscodeVideos) {
            for (MediaInfo mediaInfo : mOriginalVideos) {
                if (cropParam.getInputPath().equals(mediaInfo.filePath)) {
                    mediaInfo.filePath = cropParam.getOutputPath();
                }
            }
        }
    }

    public void release() {
        AliyunCropCreator.destroyCropInstance();
    }
}
