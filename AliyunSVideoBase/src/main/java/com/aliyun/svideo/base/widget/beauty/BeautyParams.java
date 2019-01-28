package com.aliyun.svideo.base.widget.beauty;

/**
 * Created by Akira on 2018/6/20.
 */
public class BeautyParams {
    /**
     * 美白
     */
    public int beautyWhite = 60;
    /**
     * 磨皮
     */
    public int beautyBuffing = 60;
    /**
     * 红润
     */
    public int beautyRuddy = 60;
    /**
     * 瘦脸
     */
    public int beautySlimFace = 60;
    /**
     * 大眼
     */
    public int beautyBigEye = 60;

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
}
