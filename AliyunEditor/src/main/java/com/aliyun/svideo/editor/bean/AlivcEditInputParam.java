package com.aliyun.svideo.editor.bean;

import android.graphics.BitmapFactory;
import android.util.Log;

import com.aliyun.common.global.AliyunTag;
import com.aliyun.svideo.media.MediaInfo;
import com.aliyun.svideo.sdk.external.struct.common.AliyunVideoParam;
import com.aliyun.svideo.sdk.external.struct.common.VideoDisplayMode;
import com.aliyun.svideo.sdk.external.struct.common.VideoQuality;
import com.aliyun.svideo.sdk.external.struct.encoder.VideoCodecs;
import com.duanqu.transcode.NativeParser;

import java.util.ArrayList;
import java.util.List;

/**
 * data:2019/5/20
 * 编辑输入参数
 */
public class AlivcEditInputParam {
    /**
     * 传参时作为intent extra param 的key值
     */
    public static final String INTENT_KEY_FRAME = "mFrame";
    public static final String INTENT_KEY_GOP = "mGop";
    public static final String INTENT_KEY_RATION_MODE = "mRatioMode";
    public static final String INTENT_KEY_QUALITY = "mVideoQuality";
    public static final String INTENT_KEY_RESOLUTION_MODE = "mResolutionMode";
    public static final String INTENT_KEY_CODEC = "mVideoCodec";
    public static final String INTETN_KEY_CRF = "mCrf";
    public static final String INTETN_KEY_SCANLE_RATE = "mScaleRate";
    public static final String INTETN_KEY_SCANLE_MODE = "mScaleMode";
    public static final String INTENT_KEY_TAIL_ANIMATION = "mHasTailAnimation";
    public static final String INTENT_KEY_REPLACE_MUSIC = "canReplaceMusic";
    public static final String INTENT_KEY_WATER_MARK = "hasWaterMark";
    public static final String INTENT_KEY_MEDIA_INFO = "mediaInfos";
    /**
     * 是否是合拍
     */
    public static final String INTENT_KEY_IS_MIX = "isMixRecord";

    /**
     * 视频比例
     */
    public static final int RATIO_MODE_3_4 = 0;
    public static final int RATIO_MODE_1_1 = 1;
    public static final int RATIO_MODE_9_16 = 2;
    //原比例
    public static final int RATIO_MODE_ORIGINAL = 3;
    /**
     * 视频分辨率
     */
    public static final int RESOLUTION_360P = 0;
    public static final int RESOLUTION_480P = 1;
    public static final int RESOLUTION_540P = 2;
    public static final int RESOLUTION_720P = 3;

    /**
     * 视频帧率
     */
    private int mFrameRate;
    /**
     * 视频GOP
     */
    private int mGop;
    /**
     * 视频比例
     */
    private int mRatio;
    /**
     * 视频质量
     */
    private VideoQuality mVideoQuality;
    /**
     * 视频分辨率
     */
    private int mResolutionMode;

    /**
     * 编解码方式
     */
    private VideoCodecs mVideoCodec;

    private int mCrf;
    /**
     * 压缩比例
     */
    private float mScaleRate;

    private VideoDisplayMode mScaleMode;

    /**
     * 是否使用片尾水印
     */
    private boolean mHasTailAnimation;
    /**
     * 是否使用音乐特效替换视频的原音
     */
    private boolean canReplaceMusic;
    /**
     * 是否是合拍
     */
    private boolean isMixRecorder;

    private ArrayList<MediaInfo> mediaInfos;
    private boolean hasWaterMark = true;//默认显示视频水印

    private AlivcEditInputParam() {
        this.mediaInfos = new ArrayList<>();
        this.mCrf = 23;
        this.mScaleRate = 1.0F;
        this.mResolutionMode = RESOLUTION_720P;
        this.mRatio = RATIO_MODE_9_16;
        this.mGop = 250;
        this.mFrameRate = 30;
        this.mVideoQuality = VideoQuality.HD;
        this.mVideoCodec = VideoCodecs.H264_HARDWARE;
    }

    /**
     * 生产视频合成需要的配置参数
     *
     * @return
     */
    public AliyunVideoParam generateVideoParam() {
        AliyunVideoParam param = new AliyunVideoParam.Builder()
        .frameRate(mFrameRate)
        .gop(mGop)
        .crf(mCrf)
        .videoQuality(mVideoQuality)
        .scaleMode(mScaleMode)
        .scaleRate(mScaleRate)
        .outputWidth(getOutputVideoWidth())
        .outputHeight(geOutputtVideoHeight())
        .videoCodec(mVideoCodec)
        .build();
        return param;
    }

    /**
     * 生产合拍视频合成需要的配置参数
     *
     * @return
     */
    public AliyunVideoParam generateMixVideoParam() {
        AliyunVideoParam param = new AliyunVideoParam.Builder()
        .frameRate(mFrameRate)
        .gop(mGop)
        .crf(mCrf)
        .videoQuality(mVideoQuality)
        .scaleMode(mScaleMode)
        .scaleRate(mScaleRate)
        .outputWidth(720)
        .outputHeight(640)
        .videoCodec(mVideoCodec)
        .build();
        return param;
    }

    /**
     * 获取编辑输出视频高度
     *
     * @return
     */
    public int geOutputtVideoHeight() {
        int height = 0;
        int width = getOutputVideoWidth();
        switch (mRatio) {
        case RATIO_MODE_1_1:
            height = width;
            break;
        case RATIO_MODE_3_4:
            height = width * 4 / 3;
            break;
        case RATIO_MODE_9_16:
            height = width * 16 / 9;
            break;
        case RATIO_MODE_ORIGINAL:
            if (mediaInfos != null && mediaInfos.size() > 0) {
                height = (int) (width / getMediaRatio(mediaInfos.get(0)));
            } else {
                height = width * 16 / 9;
            }
            break;
        default:
            height = width * 16 / 9;
            break;
        }
        return height;
    }

    /**
     * 获取编辑输出视频宽度
     *
     * @return
     */
    public int getOutputVideoWidth() {
        int width = 0;
        switch (mResolutionMode) {
        case RESOLUTION_360P:
            width = 360;
            break;
        case RESOLUTION_480P:
            width = 480;
            break;
        case RESOLUTION_540P:
            width = 540;
            break;
        case RESOLUTION_720P:
            width = 720;
            break;
        default:
            width = 540;
            break;
        }
        return width;
    }

    /**
     * 获取视频角度
     *
     * @param mediaInfo
     * @return
     */
    private float getMediaRatio(MediaInfo mediaInfo) {
        float videoWidth = 9;
        float videoHeight = 16;
        int videoRotation = 0;
        if (mediaInfo.mimeType.startsWith("video") || mediaInfo.filePath.endsWith("gif") || mediaInfo.filePath.endsWith("GIF")) {
            NativeParser parser = new NativeParser();
            parser.init(mediaInfo.filePath);
            try {
                videoWidth = Float.parseFloat(parser.getValue(NativeParser.VIDEO_WIDTH));
                videoHeight = Integer.parseInt(parser.getValue(NativeParser.VIDEO_HEIGHT));
                videoRotation = Integer.parseInt(parser.getValue(NativeParser.VIDEO_ROTATION));
            } catch (Exception e) {
                Log.e(AliyunTag.TAG, "parse rotation failed");
            }

        } else {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mediaInfo.filePath, options);
            videoWidth = options.outWidth;
            videoHeight = options.outHeight;
        }

        float ratio = videoWidth / videoHeight;

        return videoRotation == 90 || videoRotation == 270 ? 1 / ratio : ratio;

    }

    public void setFrameRate(int mFrameRate) {
        this.mFrameRate = mFrameRate;
    }

    public void setGop(int mGop) {
        this.mGop = mGop;
    }


    public void setRatio(int mRatio) {
        this.mRatio = mRatio;
    }

    public void setVideoQuality(VideoQuality mVideoQuality) {
        this.mVideoQuality = mVideoQuality;
    }

    public void setResolutionMode(int mResolutionMode) {
        this.mResolutionMode = mResolutionMode;
    }

    public void setVideoCodec(VideoCodecs mVideoCodec) {
        this.mVideoCodec = mVideoCodec;
    }

    public void setCrf(int mCrf) {
        this.mCrf = mCrf;
    }

    public void setScaleRate(float mScaleRate) {
        this.mScaleRate = mScaleRate;
    }

    public void setmScaleMode(VideoDisplayMode mScaleMode) {
        this.mScaleMode = mScaleMode;
    }

    public void setHasTailAnimation(boolean mHasTailAnimation) {
        this.mHasTailAnimation = mHasTailAnimation;
    }

    public void setCanReplaceMusic(boolean canReplaceMusic) {
        this.canReplaceMusic = canReplaceMusic;
    }

    public void setMediaInfos(ArrayList<MediaInfo> mediaInfos) {
        this.mediaInfos = mediaInfos;
    }

    public void setHasWaterMark(boolean hasWaterMark) {
        this.hasWaterMark = hasWaterMark;
    }

    public int getFrameRate() {
        return mFrameRate;
    }

    public int getGop() {
        return mGop;
    }

    public int getRatio() {
        return mRatio;
    }

    public VideoQuality getVideoQuality() {
        return mVideoQuality;
    }

    public int getResolutionMode() {
        return mResolutionMode;
    }

    public VideoCodecs getVideoCodec() {
        return mVideoCodec;
    }

    public int getCrf() {
        return mCrf;
    }

    public float getScaleRate() {
        return mScaleRate;
    }

    public VideoDisplayMode getScaleMode() {
        return mScaleMode;
    }

    public boolean isHasTailAnimation() {
        return mHasTailAnimation;
    }

    public boolean isCanReplaceMusic() {
        return canReplaceMusic;
    }

    public ArrayList<MediaInfo> getMediaInfos() {
        return mediaInfos;
    }

    public boolean isHasWaterMark() {
        return hasWaterMark;
    }

    public boolean isMixRecorder() {
        return isMixRecorder;
    }

    public void setsMixRecorder(boolean mixRecorder) {
        isMixRecorder = mixRecorder;
    }

    public static class Builder {
        private AlivcEditInputParam mParam = new AlivcEditInputParam();


        public Builder setFrameRate(int mFrameRate) {
            mParam.mFrameRate = mFrameRate;
            return this;
        }

        public Builder setGop(int mGop) {
            mParam.mGop = mGop;
            return this;
        }

        public Builder setCrf(int mCrf) {
            mParam.mCrf = mCrf;
            return this;
        }

        public Builder setScaleRate(float mScaleRate) {
            mParam.mScaleRate = mScaleRate;
            return this;
        }

        public Builder setVideoQuality(VideoQuality mVideoQuality) {
            mParam.mVideoQuality = mVideoQuality;
            return this;
        }

        public Builder setScaleMode(VideoDisplayMode mScaleMode) {
            mParam.mScaleMode = mScaleMode;
            return this;
        }

        public Builder setVideoCodec(VideoCodecs mVideoCodec) {
            mParam.mVideoCodec = mVideoCodec;
            return this;
        }

        public Builder setHasTailAnimation(boolean mHasTailAnimation) {
            mParam.mHasTailAnimation = mHasTailAnimation;
            return this;
        }

        public Builder setCanReplaceMusic(boolean canReplaceMusic) {
            mParam.canReplaceMusic = canReplaceMusic;
            return this;
        }

        public Builder addMediaInfos(List<MediaInfo> mediaInfos) {
            mParam.mediaInfos.addAll(mediaInfos);
            return this;
        }

        public Builder addMediaInfo(MediaInfo mediaInfo) {
            mParam.mediaInfos.add(mediaInfo);
            return this;
        }

        public Builder setRatio(int mRatio) {
            mParam.mRatio = mRatio;
            return this;
        }


        public Builder setResolutionMode(int mResolutionMode) {
            mParam.mResolutionMode = mResolutionMode;
            return this;
        }


        public Builder setHasWaterMark(boolean hasWaterMark) {
            mParam.hasWaterMark = hasWaterMark;
            return this;
        }

        public Builder setIsMixRecorder(boolean isMixRecord) {
            mParam.isMixRecorder = isMixRecord;
            return this;
        }

        public AlivcEditInputParam build() {
            return mParam;
        }

    }
}
