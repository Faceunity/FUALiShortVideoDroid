package com.aliyun.svideo.base.widget.beauty;

/**
 * Created by Akira on 2018/6/20.
 */
public class BeautyParams implements Cloneable {
    /**
     * 美白
     */
    public float beautyWhite = 60;
    /**
     * 磨皮
     */
    public float beautyBuffing = 60;
    /**
     * 红润
     */
    public float beautyRuddy = 60;
    /**
     * 瘦脸
     */
    public float beautySlimFace = 60;
    /**
     * 大眼
     */
    public float beautyBigEye = 60;

    public BeautyParams() {
    }

    @Override
    public String toString() {
        return "BeautyParams{" +
            "beautyWhite=" + beautyWhite +
            ", beautyBuffing=" + beautyBuffing +
            ", beautyRuddy=" + beautyRuddy +
            ", beautySlimFace=" + beautySlimFace +
            ", beautyBigEye=" + beautyBigEye +
            '}';
    }

    @Override
    public BeautyParams clone(){
        BeautyParams beautyParams = null;
        try {
            beautyParams = (BeautyParams)super.clone();
            beautyParams.beautyWhite = this.beautyWhite;
            beautyParams.beautyBuffing = this.beautyBuffing;
            beautyParams.beautyRuddy = this.beautyRuddy;
            beautyParams.beautySlimFace = this.beautySlimFace;
            beautyParams.beautyBigEye = this.beautyBigEye;
            return beautyParams;

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
       return null;
    }
}
