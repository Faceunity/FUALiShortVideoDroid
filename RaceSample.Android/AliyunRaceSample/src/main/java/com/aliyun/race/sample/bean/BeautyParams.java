package com.aliyun.race.sample.bean;

/**
 * Created by Akira on 2018/6/20.
 */
public class BeautyParams implements Cloneable {
    /**
     * 美白
     */
    public float mBeautyWhite = 60;
    /**
     * 磨皮
     */
    public float mBeautyBuffing = 60;
    /**
     * 红润
     */
    public float mBeautyRuddy = 60;
    /**
     * 瘦脸
     */
    public float mBeautySlimFace = 60;
    /**
     * 大眼
     */
    public float mBeautyBigEye = 60;

    public BeautyParams() {
    }

    @Override
    public String toString() {
        return "BeautyParams{" +
            "mBeautyWhite=" + mBeautyWhite +
            ", mBeautyBuffing=" + mBeautyBuffing +
            ", mBeautyRuddy=" + mBeautyRuddy +
            ", mBeautySlimFace=" + mBeautySlimFace +
            ", mBeautyBigEye=" + mBeautyBigEye +
            '}';
    }

    @Override
    public BeautyParams clone(){
        BeautyParams beautyParams = null;
        try {
            beautyParams = (BeautyParams)super.clone();
            beautyParams.mBeautyWhite = this.mBeautyWhite;
            beautyParams.mBeautyBuffing = this.mBeautyBuffing;
            beautyParams.mBeautyRuddy = this.mBeautyRuddy;
            beautyParams.mBeautySlimFace = this.mBeautySlimFace;
            beautyParams.mBeautyBigEye = this.mBeautyBigEye;
            return beautyParams;

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
       return null;
    }
}
