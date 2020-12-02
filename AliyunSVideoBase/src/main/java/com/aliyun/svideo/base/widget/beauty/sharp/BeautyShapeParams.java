package com.aliyun.svideo.base.widget.beauty.sharp;

/**
 * Created by Akira on 2018/6/20.
 */
public class BeautyShapeParams implements Cloneable {


    /**
     * 窄脸
     */
    public float beautyCutFace = 0;
    /**
     * 瘦脸
     */
    public float beautyThinFace = 0;
    /**
     * 脸长
     */
    public float beautyLongFace = 0;
    /**
     * 下巴缩短
     */
    public float beautyLowerJaw = 0;
    /**
     * 大眼
     */
    public float beautyBigEye = 0;
    /**
     * 瘦鼻
     */
    public float beautyThinNose = 0;
    /**
     * 唇宽
     */
    public float beautyMouthWidth = 0;
    /**
     * 下颌
     */
    public float beautyThinMandible = 0;
    /**
     * 颧骨
     */
    public float beautyCutCheek = 0;

    public BeautyShapeParams() {
    }

    @Override
    public String toString() {
        return "BeautyShapeParams{" +
               "beautyCutFace=" + beautyCutFace +
               ", beautyThinFace=" + beautyThinFace +
               ", beautyLongFace=" + beautyLongFace +
               ", beautyLowerJaw=" + beautyLowerJaw +
               ", beautyBigEye=" + beautyBigEye +
               ", beautyThinNose=" + beautyThinNose +
               ", beautyMouthWidth=" + beautyMouthWidth +
               ", beautyThinMandible=" + beautyThinMandible +
               ", beautyCutCheek=" + beautyCutCheek +
               '}';
    }

    @Override
    public BeautyShapeParams clone() {
        BeautyShapeParams beautyParams = null;
        try {
            beautyParams = (BeautyShapeParams)super.clone();
            beautyParams.beautyCutFace = this.beautyCutFace;
            beautyParams.beautyThinFace = this.beautyThinFace;
            beautyParams.beautyLongFace = this.beautyLongFace;
            beautyParams.beautyLowerJaw = this.beautyLowerJaw;
            beautyParams.beautyBigEye = this.beautyBigEye;
            beautyParams.beautyThinNose = this.beautyThinNose;
            beautyParams.beautyMouthWidth = this.beautyMouthWidth;
            beautyParams.beautyThinMandible = this.beautyThinMandible;
            beautyParams.beautyCutCheek = this.beautyCutCheek;
            return beautyParams;

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
