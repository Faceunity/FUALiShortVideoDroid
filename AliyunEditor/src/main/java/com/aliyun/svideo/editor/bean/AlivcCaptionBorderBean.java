package com.aliyun.svideo.editor.bean;

import android.graphics.RectF;

public class AlivcCaptionBorderBean {
    public RectF rectF;
    public float scale;
    public float roation;

    public AlivcCaptionBorderBean(RectF rectF, float scale, float roation) {
        this.rectF = rectF;
        this.scale = scale;
        this.roation = roation;
    }

    @Override
    public String toString() {
        return "AlivcCaptionBorderBean{" +
                "rectF=" + rectF +
                ", scale=" + scale +
                ", roation=" + roation +
                '}';
    }
}
