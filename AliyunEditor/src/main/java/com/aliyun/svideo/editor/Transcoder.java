/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.aliyun.common.global.AliyunTag;
import com.aliyun.common.utils.FileUtils;
import com.aliyun.svideo.base.Constants;
import com.aliyun.svideo.media.MediaInfo;
import com.aliyun.svideosdk.common.struct.common.MediaType;
import com.aliyun.svideosdk.common.struct.common.VideoDisplayMode;
import com.aliyun.svideosdk.common.struct.common.VideoQuality;
import com.aliyun.svideosdk.common.struct.encoder.VideoCodecs;
import com.aliyun.svideosdk.common.utils.AliyunSVideoUtils;
import com.aliyun.svideosdk.crop.AliyunICrop;
import com.aliyun.svideosdk.crop.CropCallback;
import com.aliyun.svideosdk.crop.CropParam;
import com.aliyun.svideosdk.crop.impl.AliyunCropCreator;
import com.aliyun.svideosdk.transcode.NativeParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * 转码 满足以下任一条件
 * 1.分辨率大于1080P
 * 2.gop大于35（已屏蔽）
 * 3.视频存在B帧（已屏蔽）
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
    private List<String> mConvertFilePaths = new CopyOnWriteArrayList<>();
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
                          final VideoDisplayMode scaleMode, final boolean transcode) {
        mTranscodeTotal = 0;
        mTranscodeIndex = 0;
        mTranscodeVideos.clear();
        if (mAliyunCrop == null) {
            return;
        }
        mTranscodeTask = new AsyncTask<Void, Long, List<MediaInfo>>() {

            @Override
            protected List<MediaInfo> doInBackground(Void... params) {
                checkHEIFImages(mOriginalVideos);
                if (transcode) {
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

    private void checkHEIFImages(List<MediaInfo> resultVideos) {
        for (MediaInfo info : resultVideos) {
            if (AliyunSVideoUtils.isHEIFImage(info.filePath) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Log.e(TAG, "HEIF file " + info.filePath);
                String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "DCIM" + File.separator + "Camera" + File.separator + "editor_temp_" + System.currentTimeMillis() + ".png";
                final boolean rs = AliyunSVideoUtils.convertHEIFImage(info.filePath, path, Bitmap.CompressFormat.PNG, 100);
                if (!rs) {
                    Log.e(TAG, "convert HEIF image failed! " + info.filePath);
                    continue;
                }
                Log.d(TAG, "convert HEIF image success! " + path);
                info.filePath = path;
                mConvertFilePaths.add(path);
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
            duration = info.duration;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (frameWidth * frameHeight > WIDTH * HEIGHT /**|| gop > GOP || isHasBFrame **/ ) {
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
            cropParam.setStartTime(info.startTime, TimeUnit.MILLISECONDS);
            cropParam.setScaleMode(scaleMode);
            cropParam.setQuality(VideoQuality.SSD);
            cropParam.setGop(5);
            cropParam.setFrameRate(30);
            cropParam.setCrf(19);
            cropParam.setVideoCodec(VideoCodecs.H264_SOFT_OPENH264);
            cropParam.setEndTime(duration + cropParam.getStartTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
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
                        mediaInfo.duration = (int)(cropParam.getEndTime(TimeUnit.MILLISECONDS) - cropParam.getStartTime(TimeUnit.MILLISECONDS));
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
        try {
            if (mConvertFilePaths.size() > 0) {
                for (String path : mConvertFilePaths) {
                    boolean rs = FileUtils.deleteFile(path);
                    Log.e(TAG, "delete temp file " + path + " | " + rs);
                }
            }
        } catch (Throwable e) {}
    }
}
