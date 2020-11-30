/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.aliyun.common.global.AliyunTag;
import com.aliyun.crop.AliyunCropCreator;
import com.aliyun.crop.struct.CropParam;
import com.aliyun.crop.supply.AliyunICrop;
import com.aliyun.crop.supply.CropCallback;
import com.aliyun.svideo.base.Constants;
import com.aliyun.svideo.common.utils.MD5Utils;
import com.aliyun.svideo.common.utils.UriUtils;
import com.aliyun.svideo.media.MediaInfo;
import com.aliyun.svideo.sdk.external.struct.MediaType;
import com.aliyun.svideo.sdk.external.struct.common.VideoDisplayMode;
import com.aliyun.svideo.sdk.external.struct.common.VideoQuality;
import com.aliyun.svideo.sdk.external.struct.encoder.VideoCodecs;
import com.duanqu.transcode.NativeParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 转码 满足以下任一条件
 * 1.分辨率大于1080P
 * 2.gop大于35
 * 3.视频存在B帧
 */

public class Transcoder {

    private static final String TAG = "Transcoder";
    private ArrayList<MediaInfo> mOriginalVideos = new ArrayList<>();
    private CopyOnWriteArrayList<CropParam> mTranscodeVideos = new CopyOnWriteArrayList<>();
    private AliyunICrop mAliyunCrop;
    private TransCallback mTransCallback;
    private int mTranscodeIndex = 0;
    private int mTranscodeTotal = 0;
    private AsyncTask<Void, Long, List<MediaInfo>> mTranscodeTask;
    /**
     * 大于1080P的视频需要进行转码
     */
    private final static int WIDTH = 1920, HEIGHT = 1080, GOP = 35;

    public void addMedia(MediaInfo mediaInfo) {
        mOriginalVideos.add(mediaInfo);
    }

    public void addMedia(int index, MediaInfo mediaInfo) {
        mOriginalVideos.add(index, mediaInfo);
    }

    public int removeMedia(MediaInfo mediaInfo) {
        int index = -1;
        //由于mediaInfo的equals方法被重写，导致添加多次同一份资源的时候，通过indexOf会获取错误的位置
        //所以用该方式获取index
        for (int i = 0; i < mOriginalVideos.size(); i++) {
            if (mOriginalVideos.get(i) == mediaInfo) {
                index = i;
            }
        }
        //如果上面方式获取不到，则用indexOf的方式
        if (index == -1) {
            index = mOriginalVideos.indexOf(mediaInfo);
        }
        mOriginalVideos.remove(index);
        return index;
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
        mAliyunCrop = AliyunCropCreator.createCropInstance(context);
    }

    public void setTransCallback(TransCallback callback) {
        this.mTransCallback = callback;
    }

    public void transcode(final Context context,
                          final VideoDisplayMode scaleMode) {
        mTranscodeTotal = 0;
        mTranscodeIndex = 0;
        mTranscodeVideos.clear();
        if (mAliyunCrop == null) {
            return;
        }
        mTranscodeTask = new AsyncTask<Void, Long, List<MediaInfo>>() {

            @Override
            protected List<MediaInfo> doInBackground(Void... params) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    //适配 android Q, copy 媒体文件到应用沙盒下
                    cacheMediaFile(context);
                }
                CropParam cropParam = null;
                for (MediaInfo info : mOriginalVideos) {

                    if (info.filePath.endsWith("gif") || info.filePath.endsWith("GIF")) {
                        Log.d(TAG, "addTransCode excluded: --.gif");
                        continue;
                    }
                    if (info.mimeType.startsWith("video")) {
                        cropParam = loadVideoCropInfo(context, info, scaleMode);
                    } else if (info.mimeType.startsWith("image")) {
                        cropParam = loadImageCropInfo(context, info, scaleMode);
                    }
                    if (cropParam != null) {
                        mTranscodeVideos.add(cropParam);
                        mTranscodeTotal++;
                    }
                    if (isCancelled()) {
                        return null;
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

        } .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    /**
     * 缓存媒体文件到沙盒cache目录
     * @param context Context
     */
    private void cacheMediaFile(Context context) {
        for (MediaInfo originalVideo : mOriginalVideos) {
            if (originalVideo.filePath.contains(context.getPackageName())) {
                continue;
            }
            if (!TextUtils.isEmpty(originalVideo.fileUri)) {
                int index = originalVideo.filePath.lastIndexOf(".");
                String suffix = index == -1 ? "" : originalVideo.filePath.substring(index);

                //适配Android Q，文件copy到沙盒内部加载
                String filePath = Constants.SDCardConstants.getCacheDir(context) + File.separator + MD5Utils
                                  .getMD5(originalVideo.fileUri) + suffix;
                if (!new File(filePath).exists()) {
                    UriUtils.copyFileToDir(context, originalVideo.fileUri, filePath);
                }
                originalVideo.filePath = filePath;
            }
        }
    }

    public void cancel() {
        mTranscodeTask.cancel(true);
        mAliyunCrop.cancel();
        if (mTransCallback != null) {
            mTransCallback.onCancelComplete();
        }
    }

    private void transcodeVideo(int index) {
        mTranscodeIndex++;
        CropParam cropParam = mTranscodeVideos.get(index);
        mAliyunCrop.setCropParam(cropParam);
        mAliyunCrop.setCropCallback(mTranscodeCallback);
        mAliyunCrop.startCrop();
        Log.i(TAG, "log_editor_media_transcode :" + cropParam.getInputPath());
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
                    mTransCallback.onComplete(mOriginalVideos);
                }
            }
        }

        @Override
        public void onCancelComplete() {

        }
    };

    private CropParam loadVideoCropInfo(Context context, MediaInfo info,
                                        VideoDisplayMode scaleMode) {
        int frameWidth = 0;
        int frameHeight = 0;
        long duration = 0;
        int rotation = 0;
        int gop = 0;
        boolean isHasBFrame = false;
        try {
            NativeParser nativeParser = new NativeParser();
            nativeParser.init(info.filePath);
            try {
                rotation = Integer.parseInt(nativeParser.getValue(NativeParser.VIDEO_ROTATION));
                frameWidth = Integer.parseInt(nativeParser.getValue(NativeParser.VIDEO_WIDTH));
                frameHeight = Integer.parseInt(nativeParser.getValue(NativeParser.VIDEO_HEIGHT));
                gop = nativeParser.getMaxGopSize();
                isHasBFrame = nativeParser.checkBFrame();
            } catch (Exception e) {
                Log.e(AliyunTag.TAG, "parse rotation failed");
            }
            nativeParser.release();
            nativeParser.dispose();
            duration = info.duration * 1000;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (frameWidth * frameHeight > WIDTH * HEIGHT || gop > GOP || isHasBFrame ) {
            Log.d(TAG, "need transcode...path..." + info.filePath);
            CropParam cropParam = new CropParam();
            cropParam.setInputPath(info.filePath);
            String outputPath = Constants.SDCardConstants.getCacheDir(context) + File.separator + System.currentTimeMillis() + Constants.SDCardConstants.TRANSCODE_SUFFIX;
            cropParam.setOutputPath(outputPath);
            int outputWidth;
            int outputHeight;

            if (frameWidth * frameHeight > WIDTH * HEIGHT) {
                //尺寸过大裁剪时重置宽高
                if (frameWidth > frameHeight) {
                    outputWidth = Math.max(WIDTH, HEIGHT);
                    outputHeight = (int) ((float) frameHeight / frameWidth * outputWidth);
                } else {
                    outputHeight = Math.max(WIDTH, HEIGHT);
                    outputWidth = (int) ((float) frameWidth / frameHeight * outputHeight);
                }
            } else {
                //gop或者B帧问题视频宽高不变
                outputHeight = frameHeight;
                outputWidth = frameWidth;
            }

            //保证宽高为偶数
            if (outputWidth % 2 == 1) {
                outputWidth += 1;
            }
            if (outputHeight % 2 == 1) {
                outputHeight += 1;
            }
            int temp;
            if (rotation == 90 || rotation == 270) {
                temp = outputWidth;
                outputWidth = outputHeight;
                outputHeight = temp;
            }
            //输出高度设置为16的倍数，解决部分手机部分视频编码时重影
            int i = outputHeight % 16;
            outputHeight = outputHeight - i;
            cropParam.setOutputHeight(outputHeight);
            cropParam.setOutputWidth(outputWidth);
            if (rotation == 90 || rotation == 270) {
                cropParam.setCropRect(new Rect(0, 0, frameHeight, frameWidth));
            } else {
                cropParam.setCropRect(new Rect(0, 0, frameWidth, frameHeight));
            }
            cropParam.setStartTime(info.startTime * 1000);
            cropParam.setScaleMode(scaleMode);
            cropParam.setQuality(VideoQuality.SSD);
            cropParam.setGop(5);
            cropParam.setFrameRate(30);
            cropParam.setCrf(19);
            cropParam.setVideoCodec(VideoCodecs.H264_SOFT_FFMPEG);
            cropParam.setEndTime(duration + cropParam.getStartTime());
            cropParam.setMediaType(MediaType.ANY_VIDEO_TYPE);
            return cropParam;
        }
        return null;
    }
    private CropParam loadImageCropInfo(Context context, MediaInfo info,
                                        VideoDisplayMode scaleMode) {
        int frameWidth = 0;
        int frameHeight = 0;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(info.filePath, options);
        frameWidth = options.outWidth;
        frameHeight = options.outHeight;

        CropParam cropParam = new CropParam();
        cropParam.setInputPath(info.filePath);
        if (options.outMimeType == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder(Constants.SDCardConstants.getCacheDir(context))
        .append(File.separator).append(System.currentTimeMillis())
        //两次System.currentTimeMillis()获取的时间可能相同，追加一个index防止图片名字相同
        .append(mTranscodeTotal)
        .append(options.outMimeType.replace("image/", "."));
        cropParam.setOutputPath(sb.toString());

        int outputWidth = 0;
        int outputHeight = 0;
        if (frameWidth * frameHeight > WIDTH * HEIGHT * 2) {
            if (frameWidth > frameHeight) {
                outputWidth = Math.min(WIDTH, HEIGHT);
                outputHeight = (int) ((float) frameHeight / frameWidth * outputWidth);
            } else {
                outputHeight = Math.min(WIDTH, HEIGHT);
                outputWidth = (int) ((float) frameWidth / frameHeight * outputHeight);
            }
            cropParam.setOutputHeight(outputHeight);
            cropParam.setOutputWidth(outputWidth);
            cropParam.setCropRect(new Rect(0, 0, frameWidth, frameHeight));
            cropParam.setScaleMode(scaleMode);
            cropParam.setQuality(VideoQuality.SSD);
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
                    if (mediaInfo.mimeType.contains("video")) {
                        //转码后该视频片段为真裁剪，startTime为0。
                        mediaInfo.startTime = 0;
                        //视频转码后会以视频的时长为准，多余的音频会被裁掉
                        mediaInfo.duration = (int)(cropParam.getEndTime() - cropParam.getStartTime()) / 1000;
                    }
                }
            }
        }
    }

    /**
     * 获取所有媒体，activity恢复的时候使用
     * @return List<MediaInfo>
     */
    public ArrayList<MediaInfo> getOriginalVideos() {
        return mOriginalVideos;
    }

    public void release() {
        if (mAliyunCrop != null) {
            mAliyunCrop.dispose();
            mAliyunCrop = null;
        }
        if (mTranscodeCallback != null) {
            mTranscodeCallback = null;
        }
        if (mTransCallback != null) {
            mTransCallback = null;
        }
    }
}
