package com.aliyun.race.sample.race;

import android.content.Context;
import android.util.Log;

import com.aliyun.race.AliyunFace;
import com.aliyun.race.AliyunFaceDetect;

import static com.aliyun.race.AliyunCommon.*;
import static com.aliyun.race.AliyunFaceDetect.*;

public class FaceManager {
    private static final String TAG = "FaceManager";
    private AliyunFaceDetect mFaceDetect;
    private byte[] mYuv;
    /**
     *  SDK初始化,
     *
     * @param context context
     */
    public boolean setUp(Context context) {
        mFaceDetect = new AliyunFaceDetect();
        mFaceDetect.initialize(context,AliyunFaceDetect.ALR_FACE_DETECT_MODE_VIDEO);
        mFaceDetect.setParam(ALR_FACE_PARAM_DETECT_INTERVAL,5);
        mFaceDetect.setParam(ALR_FACE_PARAM_SMOOTH_THRESHOLD,1);
        return true;
    }
    /**
     * @param cameraNV21Byte    cameraNV21原始数据
     * @param cameraWidth       摄像头采集数据的宽
     * @param cameraHeight      摄像头采集数据的高
     */
    public void drawPoint(final byte[] cameraNV21Byte, int cameraWidth, int cameraHeight, int rotation, AliyunFace[] face106s) {
        mFaceDetect.detect(cameraNV21Byte, ALR_IMAGE_FORMAT_NV21, cameraWidth, cameraHeight, cameraWidth, rotation, face106s);
    }

    public void release() {
        Log.d(TAG, "release: mBeautifyNative.destroy()" );
        mFaceDetect.destroy();
    }

    /**
     * 根据相机前置摄像头rotation变换出人脸检查所需要的rotation
     * @param rotation 当前相机的rotation
     * @return
     *
     * //       返回 右旋转 -- 90  原始rotation 180
     * //        返回 正面 --- 0   rotation 270
     * //        返回 左旋转--270   rotation 0
     * //       返回 倒置 -- 180  rotation90
     */
    private int transFrontRotation(int rotation){
        int detectRotation;
        switch (rotation){
            case 0:
                detectRotation = 270;
            break;
            case 90:
                detectRotation =  180;
                break;
            case 180:
                detectRotation =  90;
                break;
            default:
                detectRotation =  0;
                break;
        }
        return detectRotation;
    }
    /**
     * 根据相机后置摄像头rotation变换出人脸检查所需要的rotation
     * @param rotation 当前相机的rotation
     * @return
     *
     * //       返回 右旋转 -- 90  原始rotation 180
     * //        返回 正面 --- 0   rotation 90
     * //        返回 左旋转--270   rotation 0
     * //       返回 倒置 -- 180  rotation270
     */
    private int transBackRotation(int rotation){
        int detectRotation;
        switch (rotation){
            case 0:
                detectRotation = 270;
            break;
            case 180:
                detectRotation =  90;
                break;
            case 270:
                detectRotation =  180;
                break;
            default:
                detectRotation =  0;
                break;
        }
        return detectRotation;
    }


    private byte[] rotateYUVDegree270AndMirror(byte[] data, int imageWidth, int imageHeight) {
        if(mYuv == null || mYuv.length < imageWidth * imageHeight * 3 / 2){
            mYuv = new byte[imageWidth * imageHeight * 3 / 2];
        }
// Rotate and mirror the Y luma
        int i = 0;
        int maxY;
        for (int x = imageWidth - 1; x >= 0; x--) {
            maxY = imageWidth * (imageHeight - 1) + x * 2;
            for (int y = 0; y < imageHeight; y++) {
                mYuv[i] = data[maxY - (y * imageWidth + x)];
                i++;
            }
        }
// Rotate and mirror the U and V color components
        int uvSize = imageWidth * imageHeight;
        i = uvSize;
        int maxUV;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            maxUV = imageWidth * (imageHeight / 2 - 1) + x * 2 + uvSize;
            for (int y = 0; y < imageHeight / 2; y++) {
                mYuv[i] = data[maxUV - 2 - (y * imageWidth + x - 1)];
                i++;
                mYuv[i] = data[maxUV - (y * imageWidth + x)];
                i++;
            }
        }
        return mYuv;
    }

    private byte[] rotateYUVDegree90(byte[] data, int imageWidth, int imageHeight) {
        if(mYuv == null || mYuv.length < imageWidth * imageHeight * 3 / 2){
            mYuv = new byte[imageWidth * imageHeight * 3 / 2];
        }
// Rotate the Y luma
        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                mYuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
// Rotate the U and V color components
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                mYuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                mYuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
                i--;
            }
        }
        return mYuv;
    }
}
