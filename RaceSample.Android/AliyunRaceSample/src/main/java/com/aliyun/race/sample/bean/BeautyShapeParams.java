package com.aliyun.race.sample.bean;

/**
 * Created by Akira on 2018/6/20.
 */
public class BeautyShapeParams implements Cloneable {


    /**
     * 窄脸
     */
    public float mBeautyCutFace = 0;
    /**
     * 瘦脸
     */
    public float mBeautyThinFace = 0;
    /**
     * 脸长
     */
    public float mBeautyLongFace = 0;
    /**
     * 下巴缩短
     */
    public float mBeautyLowerJaw = 0;
    /**
     * 大眼
     */
    public float mBeautyBigEye = 0;
    /**
     * 瘦鼻
     */
    public float mBeautyThinNose = 0;
    /**
     * 唇宽
     */
    public float mBeautyMouthWidth = 0;
    /**
     * 下颌
     */
    public float mBeautyThinMandible = 0;
    /**
     * 颧骨
     */
    public float mBeautyCutCheek = 0;

    public BeautyShapeParams() {
    }

    @Override
    public String toString() {
        return "BeautyShapeParams{" +
                "mBeautyCutFace=" + mBeautyCutFace +
                ", mBeautyThinFace=" + mBeautyThinFace +
                ", mBeautyLongFace=" + mBeautyLongFace +
                ", mBeautyLowerJaw=" + mBeautyLowerJaw +
                ", mBeautyBigEye=" + mBeautyBigEye +
                ", mBeautyThinNose=" + mBeautyThinNose +
                ", mBeautyMouthWidth=" + mBeautyMouthWidth +
                ", mBeautyThinMandible=" + mBeautyThinMandible +
                ", mBeautyCutCheek=" + mBeautyCutCheek +
                '}';
    }

    @Override
    public BeautyShapeParams clone(){
        BeautyShapeParams beautyParams = null;
        try {
            beautyParams = (BeautyShapeParams)super.clone();
            beautyParams.mBeautyCutFace = this.mBeautyCutFace;
            beautyParams.mBeautyThinFace = this.mBeautyThinFace;
            beautyParams.mBeautyLongFace = this.mBeautyLongFace;
            beautyParams.mBeautyLowerJaw = this.mBeautyLowerJaw;
            beautyParams.mBeautyBigEye = this.mBeautyBigEye;
            beautyParams.mBeautyThinNose = this.mBeautyThinNose;
            beautyParams.mBeautyMouthWidth = this.mBeautyMouthWidth;
            beautyParams.mBeautyThinMandible = this.mBeautyThinMandible;
            beautyParams.mBeautyCutCheek = this.mBeautyCutCheek;
            return beautyParams;

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
       return null;
    }
}
