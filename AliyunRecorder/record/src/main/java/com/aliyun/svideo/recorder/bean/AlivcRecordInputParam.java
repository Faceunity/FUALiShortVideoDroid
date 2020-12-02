package com.aliyun.svideo.recorder.bean;

import com.aliyun.svideo.sdk.external.struct.common.VideoQuality;
import com.aliyun.svideo.sdk.external.struct.encoder.VideoCodecs;
import com.aliyun.svideo.sdk.external.struct.snap.AliyunSnapVideoParam;


/**
 * @author zsy_18 data:2019/5/16
 * 录制参数配置
 */
public class AlivcRecordInputParam {
    /**
     * 传参时作为intent extra param 的key值
     */
    public static final String INTENT_KEY_RESOLUTION_MODE = "mResolutionMode";
    public static final String INTENT_KEY_MAX_DURATION = "mMaxDuration";
    public static final String INTENT_KEY_MIN_DURATION = "mMinDuration";
    public static final String INTENT_KEY_RATION_MODE = "mRatioMode";
    public static final String INTENT_KEY_GOP = "mGop";
    public static final String INTENT_KEY_FRAME = "mFrame";
    public static final String INTENT_KEY_QUALITY = "mVideoQuality";
    public static final String INTENT_KEY_CODEC = "mVideoCodec";
    public static final String INTENT_KEY_VIDEO_OUTPUT_PATH = "videoOutputPath";
    public static final String INTENT_KEY_VIDEO_RENDERING_MODE = "mRenderingMode";
    public static final String INTENT_KEY_RECORD_FLIP = "mRecordFlip";
    public static final String INTENT_KEY_IS_SVIDEO_RACE = "mIsSvideoRace";

    public static final int RESOLUTION_360P = 0;
    public static final int RESOLUTION_480P = 1;
    public static final int RESOLUTION_540P = 2;
    public static final int RESOLUTION_720P = 3;

    public static final int RATIO_MODE_3_4 = 0;
    public static final int RATIO_MODE_1_1 = 1;
    public static final int RATIO_MODE_9_16 = 2;
    /**
     * 默认配置参数
     */
    public static final int DEFAULT_VALUE_MAX_DURATION = 15 * 1000;
    public static final int DEFAULT_VALUE_MIN_DURATION = 2 * 1000;
    public static final int DEFAULT_VALUE_GOP = 250;
    public static final int DEFAULT_VALUE_FRAME = 30;

    /**
     * 视频分辨率
     */
    private int mResolutionMode;
    /**
     * 视频录制最大时长
     */
    private int mMaxDuration;
    /**
     * 视频录制最小时长
     */
    private int mMinDuration;
    /**
     * 录制视频比例
     */
    private int mRatioMode;
    /**
     * 录制视频的关键帧间隔
     */
    private int mGop;
    /**
     * 录制视频的帧率
     */
    private int mFrame;
    /**
     * 录制视频的视频质量
     */
    private VideoQuality mVideoQuality;
    /**
     * 录制视频的编码方式
     */
    private VideoCodecs mVideoCodec;
    /**
     * 输出视频路径
     */
    private String mVideoOutputPath;
    /**
     * 渲染方式
     */
    private RenderingMode mRenderingMode;
    /**
     * 录制是否输出镜像
     */
    private boolean mIsUseFlip;
    /**
     * 是否是单独的race版本包
     */
    private boolean mIsSvideoRace = false;

    public AlivcRecordInputParam() {
        this.mResolutionMode = RESOLUTION_720P;
        this.mMaxDuration = DEFAULT_VALUE_MAX_DURATION;
        this.mMinDuration = DEFAULT_VALUE_MIN_DURATION;
        this.mRatioMode = RATIO_MODE_9_16;
        this.mGop = DEFAULT_VALUE_GOP;
        this.mFrame = DEFAULT_VALUE_FRAME;
        this.mVideoQuality = VideoQuality.HD;
        this.mVideoCodec = VideoCodecs.H264_HARDWARE;
        this.mRenderingMode = RenderingMode.FaceUnity;
        this.mIsUseFlip = false;
    }
    /**
     * 获取拍摄视频宽度
     * @return
     */
    public int getVideoWidth() {
        int width = 0;
        switch (mResolutionMode) {
        case AliyunSnapVideoParam.RESOLUTION_360P:
            width = 360;
            break;
        case AliyunSnapVideoParam.RESOLUTION_480P:
            width = 480;
            break;
        case AliyunSnapVideoParam.RESOLUTION_540P:
            width = 540;
            break;
        case AliyunSnapVideoParam.RESOLUTION_720P:
            width = 720;
            break;
        default:
            width = 540;
            break;
        }

        return width;
    }
    public int getVideoHeight() {
        int width = getVideoWidth();
        int height = 0;
        switch (mRatioMode) {
        case AliyunSnapVideoParam.RATIO_MODE_1_1:
            height = width;
            break;
        case AliyunSnapVideoParam.RATIO_MODE_3_4:
            height = width * 4 / 3;
            break;
        case AliyunSnapVideoParam.RATIO_MODE_9_16:
            height = width * 16 / 9;
            break;
        default:
            height = width;
            break;
        }
        return height;
    }
    public void setResolutionMode(int mResolutionMode) {
        this.mResolutionMode = mResolutionMode;
    }

    public void setMaxDuration(int mMaxDuration) {
        this.mMaxDuration = mMaxDuration;
    }

    public void setMinDuration(int mMinDuration) {
        this.mMinDuration = mMinDuration;
    }

    public void setRatioMode(int mRatioMode) {
        this.mRatioMode = mRatioMode;
    }

    public void setGop(int mGop) {
        this.mGop = mGop;
    }

    public void setFrame(int mFrame) {
        this.mFrame = mFrame;
    }

    public void setVideoQuality(VideoQuality mVideoQuality) {
        this.mVideoQuality = mVideoQuality;
    }

    public void setVideoCodec(VideoCodecs mVideoCodec) {
        this.mVideoCodec = mVideoCodec;
    }

    public void setVideoOutputPath(String videoOutputPath) {
        this.mVideoOutputPath = videoOutputPath;
    }

    public int getResolutionMode() {
        return mResolutionMode;
    }

    public int getMaxDuration() {
        return mMaxDuration;
    }

    public int getMinDuration() {
        return mMinDuration;
    }

    public int getRatioMode() {
        return mRatioMode;
    }

    public int getGop() {
        return mGop;
    }

    public int getFrame() {
        return mFrame;
    }

    public boolean isUseFlip() {
        return mIsUseFlip;
    }

    public VideoQuality getVideoQuality() {
        return mVideoQuality;
    }

    public VideoCodecs getVideoCodec() {
        return mVideoCodec;
    }

    public String getVideoOutputPath() {
        return mVideoOutputPath;
    }

    public RenderingMode getmRenderingMode() {
        return mRenderingMode;
    }

    public boolean isSvideoRace() {
        return mIsSvideoRace;
    }

    public void setmRenderingMode(RenderingMode mRenderingMode) {
        this.mRenderingMode = mRenderingMode;
    }

    public static class Builder {
        private AlivcRecordInputParam mParam = new AlivcRecordInputParam();
        public Builder setResolutionMode(int mResolutionMode) {
            this.mParam.mResolutionMode = mResolutionMode;
            return this;
        }

        public Builder setMaxDuration(int mMaxDuration) {
            this.mParam.mMaxDuration = mMaxDuration;
            return this;
        }

        public Builder setMinDuration(int mMinDuration) {
            this.mParam.mMinDuration = mMinDuration;
            return this;
        }

        public Builder setIsUseFlip(boolean isUseFlip) {
            this.mParam.mIsUseFlip = isUseFlip;
            return this;
        }

        public Builder setRatioMode(int mRatioMode) {
            this.mParam.mRatioMode = mRatioMode;
            return this;
        }

        public Builder setGop(int mGop) {
            this.mParam.mGop = mGop;
            return this;
        }

        public Builder setFrame(int mFrame) {
            this.mParam.mFrame = mFrame;
            return this;
        }

        public Builder setVideoQuality(VideoQuality mVideoQuality) {
            this.mParam.mVideoQuality = mVideoQuality;
            return this;
        }

        public Builder setVideoCodec(VideoCodecs mVideoCodec) {
            this.mParam.mVideoCodec = mVideoCodec;
            return this;
        }
        public Builder setVideoRenderingMode(RenderingMode mRenderingMode) {
            this.mParam.mRenderingMode = mRenderingMode;
            return this;
        }

        public Builder setVideoOutputPath(String videoOutputPath) {
            this.mParam.mVideoOutputPath = videoOutputPath;
            return this;
        }
        public Builder setSvideoRace(boolean mIsSvideoRace) {
            this.mParam.mIsSvideoRace = mIsSvideoRace;
            return this;
        }
        public AlivcRecordInputParam build() {
            return this.mParam;
        }
    }
}
