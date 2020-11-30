/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.util;

import android.graphics.Matrix;

public class MatrixUtil {

    public float translateX;
    public float translateY;

    public float scaleX;
    public float scaleY;

    public float rotation;

    public void setRotationDeg(float value) {
        rotation = (float) (value * Math.PI / 180);
    }

    public float getRotationDeg() {
        return (float) (rotation / Math.PI * 180);
    }

    public float getRotation() {
        return rotation;
    }

    private final float[] _Data = new float[9];

    public
    void decomposeTSR(Matrix m) {
        m.getValues(_Data);

        translateX = _Data[Matrix.MTRANS_X];
        translateY = _Data[Matrix.MTRANS_Y];

        float sx = _Data[Matrix.MSCALE_X];
        float sy = _Data[Matrix.MSCALE_Y];
        float skewx = _Data[Matrix.MSKEW_X];
        float skewy = _Data[Matrix.MSKEW_Y];

        scaleX = (float) Math.sqrt(sx * sx + skewx * skewx);
        scaleY = (float) Math.sqrt(sy * sy + skewy * skewy) * Math.signum(sx * sy - skewx * skewy);

        rotation = (float) Math.atan2(-skewx, sx);
    }

    public
    void composeTSR(Matrix m) {
        m.setRotate(getRotationDeg());
        m.postScale(scaleX, scaleY);
        m.postTranslate(translateX, translateY);
    }

    @Override
    public String toString() {
        return "MatrixUtil{" +
               "translateX=" + translateX +
               ", translateY=" + translateY +
               ", scaleX=" + scaleX +
               ", scaleY=" + scaleY +
               ", rotation=" + rotation +
               '}';
    }

}
