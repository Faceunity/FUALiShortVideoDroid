package com.aliyun.svideo.editor.bean;

import com.aliyun.svideo.sdk.external.struct.common.AliyunVideoParam;

/**
 * data:2019/5/20
 * 编辑输出参数
 */
public class AlivcEditOutputParam {
    /**
     * 封面图片路径
     */
    private String mThumbnailPath;
    /**
     * 编辑生产的json配置信息路径
     */
    private String mConfigPath;
    /**
     * 生产视频的宽度
     */
    private int mOutputVideoWidth;
    /**
     * 生产视频的高度
     */
    private int mOutputVideoHeight;
    /**
     * 生产视频的角度
     */
    private float mVideoRatio;
    /**
     * 编辑配置信息
     */
    private AliyunVideoParam mVideoParam;

    public AliyunVideoParam getVideoParam() {
        return mVideoParam;
    }

    public void setVideoParam(AliyunVideoParam mVideoParam) {
        this.mVideoParam = mVideoParam;
    }

    public String getThumbnailPath() {
        return mThumbnailPath;
    }

    public void setThumbnailPath(String mThumbnailPath) {
        this.mThumbnailPath = mThumbnailPath;
    }

    public String getConfigPath() {
        return mConfigPath;
    }

    public void setConfigPath(String mConfigPath) {
        this.mConfigPath = mConfigPath;
    }

    public int getOutputVideoWidth() {
        return mOutputVideoWidth;
    }

    public void setOutputVideoWidth(int mOutputVideoWidth) {
        this.mOutputVideoWidth = mOutputVideoWidth;
    }

    public int getOutputVideoHeight() {
        return mOutputVideoHeight;
    }

    public void setOutputVideoHeight(int mOutputVideoHeight) {
        this.mOutputVideoHeight = mOutputVideoHeight;
    }

    public float getVideoRatio() {
        return mVideoRatio;
    }

    public void setVideoRatio(float mVideoRatio) {
        this.mVideoRatio = mVideoRatio;
    }
}
