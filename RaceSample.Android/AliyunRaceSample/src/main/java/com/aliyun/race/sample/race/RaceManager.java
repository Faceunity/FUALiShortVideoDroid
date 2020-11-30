package com.aliyun.race.sample.race;

import android.content.Context;
import android.util.Log;

import com.aliyun.race.AliyunBeautifyNative;
import com.aliyun.race.AliyunCommon;
import com.aliyun.race.AliyunFace;
import com.aliyun.race.AliyunFaceDetect;
import com.aliyun.race.sample.R;
import com.aliyun.race.sample.bean.BeautyShapeParams;
import com.aliyun.race.sample.bean.RaceMode;
import com.aliyun.race.sample.utils.ScreenUtils;

import java.io.File;

import static com.aliyun.race.AliyunBeautifyNative.ALR_FACE_SHAPE_TYPE_BIG_EYE;
import static com.aliyun.race.AliyunBeautifyNative.ALR_FACE_SHAPE_TYPE_CUT_CHEEK;
import static com.aliyun.race.AliyunBeautifyNative.ALR_FACE_SHAPE_TYPE_CUT_FACE;
import static com.aliyun.race.AliyunBeautifyNative.ALR_FACE_SHAPE_TYPE_LONG_FACE;
import static com.aliyun.race.AliyunBeautifyNative.ALR_FACE_SHAPE_TYPE_LOWER_JAW;
import static com.aliyun.race.AliyunBeautifyNative.ALR_FACE_SHAPE_TYPE_MOUTH_WIDTH;
import static com.aliyun.race.AliyunBeautifyNative.ALR_FACE_SHAPE_TYPE_THIN_FACE;
import static com.aliyun.race.AliyunBeautifyNative.ALR_FACE_SHAPE_TYPE_THIN_NOSE;
import static com.aliyun.race.AliyunBeautifyNative.ALR_FACE_TYPE_THIN_MANDIBLE;
import static com.aliyun.race.AliyunBeautifyNative.ALR_FLAG_OUTPUT_FLIP_Y;
import static com.aliyun.race.AliyunBeautifyNative.ALR_FLAG_TEXTURE_OES_EXTERNAL;
import static com.aliyun.race.AliyunCommon.ALR_IMAGE_FORMAT_NV21;
import static com.aliyun.race.AliyunFaceDetect.ALR_FACE_PARAM_DETECT_INTERVAL;
import static com.aliyun.race.AliyunFaceDetect.ALR_FACE_PARAM_SMOOTH_THRESHOLD;

public class RaceManager {
    private static final String TAG = "RaceManager";
    private AliyunBeautifyNative mBeautifyNative;
    private boolean mInited = false;
    private Context mContext;
    private boolean mRaceDebug = true;
    private int mInitResult = -1;
    private RaceInitCallback mRaceInitCallback;
    /**
     * 美白
     */
    private float mFaceBeautyColorLevel = 0.2f;

    /**
     * 磨皮
     */
    private float mFaceBeautyALLBlurLevel = 0.0f;
    /**
     * 锐化
     */
    private float mFaceBeautySharpLevel = 0.5f;

//    美型相关参数
    /**
     * 窄脸 cut_face
     */
    private float mFaceBeautyCutFace = 0f;
    /**
     * 瘦脸 thin_face
     */
    private float mFaceBeautyThinFace = 0f;
    /**
     * 脸长 long_face
     */
    private float mFaceBeautyLongFace = 0f;
    /**
     * 下巴缩短 lower_jaw
     */
    private float mFaceBeautyLowerJaw = 0f;
    /**
     * 大眼   big_eye
     */
    private float mFaceBeautyBigEye = 0f;
    /**
     * 瘦鼻 thin_nose
     */
    private float mFaceBeautyThinNose = 0f;
    /**
     * 唇宽 mouth_width
     */
    private float mFaceBeautyMouthWidth = 0f;
    /**
     * 下颌 thin_mandible
     */
    private float mFaceBeautyThinMandible = 0f;
    /**
     * 颧骨 cut_cheek
     */
    private float mFaceBeautyCutCheek = 0f;

    private byte[] mYuv;

    private int mScreenHeight;
    private int mScreenWidth;

    private RaceMode mRaceMode = RaceMode.MIX;

    private boolean mIsRaceOpen = true;

    public RaceManager() {
    }

    public void setRaceOpen(boolean raceOpen){
        mIsRaceOpen = raceOpen;
    }

    public void setRaceInitCallback(RaceInitCallback callback){
        mRaceInitCallback = callback;
    }

    /**
     *  SDK初始化,
     *
     * @param context
     */
    public boolean setUp(Context context) {
        this.mContext = context;
        mBeautifyNative = new AliyunBeautifyNative(context);
        mScreenHeight = ScreenUtils.getRealHeight(context);
        mScreenWidth = ScreenUtils.getRealWidth(context);
        return true;
    }

    public int processBufferToBuffer(byte[] cameraNV21Byte,  int cameraWidth, int cameraHeight, int rotation) {
        if (!mInited) {
            mInitResult = mBeautifyNative.initialize(mContext.getExternalFilesDir("") + File.separator);
            AliyunCommon.setLogLevel(AliyunCommon.ALR_LOG_LEVEL_WARN);
            mBeautifyNative.setFaceSwitch(true);
            mInited = true;
        }
        if (mInitResult != 0) {
            return 0;
        }
        byte[] bytes = new byte[cameraNV21Byte.length];
        int result =  mBeautifyNative.processBufferToBuffer(cameraNV21Byte, cameraWidth, cameraHeight, rotation, AliyunCommon.ALR_IMAGE_FORMAT_I420,cameraWidth*cameraHeight * 3 / 2,bytes);
        Log.d(TAG,"bytes----"+bytes.length);
//            yuvToBitmap(bytes,cameraWidth,cameraHeight);
        return result;
    }


    /**
     * @param cameraNV21Byte    cameraNV21原始数据
     * @param cameraTextureId   为用来绘制的texture id
     * @param cameraWidth       摄像头采集数据的宽
     * @param cameraHeight      摄像头采集数据的高
     */
    public int draw(final byte[] cameraNV21Byte, int cameraTextureId, int cameraWidth, int cameraHeight, int rotation) {
        if (mBeautifyNative == null) {
            mBeautifyNative = new AliyunBeautifyNative(mContext);
        }
        if (!mInited) {
            mInitResult = mBeautifyNative.initialize();
            AliyunCommon.setLogLevel(AliyunCommon.ALR_LOG_LEVEL_WARN);
            mBeautifyNative.setFaceSwitch(true);
            mInited = true;
//           mInitResult小于0时， race sdk 初始化失败，可能是由于license过期导致,请检查license
            if(mInitResult < 0){
                Log.e(TAG, mContext.getResources().getString(R.string.alivc_base_beauty_race_error));
            }
        }
        if(mRaceInitCallback != null){
            mRaceInitCallback.onRaceInitResult(mInitResult == 0);
        }
        if (mInitResult != 0) {
            return cameraTextureId;
        }
        mBeautifyNative.setSkinBuffing(mFaceBeautyALLBlurLevel);
        mBeautifyNative.setSharpen(mFaceBeautySharpLevel);
        mBeautifyNative.setSkinWhitening(mFaceBeautyColorLevel);

//            1 窄脸 cut_face
        mBeautifyNative.setFaceShape(ALR_FACE_SHAPE_TYPE_CUT_FACE, mFaceBeautyCutFace);
        //       2 瘦脸 thin_face
        mBeautifyNative.setFaceShape(ALR_FACE_SHAPE_TYPE_THIN_FACE, mFaceBeautyThinFace);
        //        3 脸长 small_face 0-100
        mBeautifyNative.setFaceShape(ALR_FACE_SHAPE_TYPE_LONG_FACE, mFaceBeautyLongFace);
        //        4 下巴缩短 lower_jaw
        mBeautifyNative.setFaceShape(ALR_FACE_SHAPE_TYPE_LOWER_JAW, mFaceBeautyLowerJaw);
        //       8 大眼 big_eye
        mBeautifyNative.setFaceShape(ALR_FACE_SHAPE_TYPE_BIG_EYE, mFaceBeautyBigEye);
        //      14 瘦鼻 thin_nose
        mBeautifyNative.setFaceShape(ALR_FACE_SHAPE_TYPE_THIN_NOSE, mFaceBeautyThinNose);
        //      18 唇宽 mouth_shape
        mBeautifyNative.setFaceShape(ALR_FACE_SHAPE_TYPE_MOUTH_WIDTH, mFaceBeautyMouthWidth);
        //         下颌 thin_mandible
        mBeautifyNative.setFaceShape(ALR_FACE_TYPE_THIN_MANDIBLE, mFaceBeautyThinMandible);
        //        颧骨 cut_cheek
        mBeautifyNative.setFaceShape(ALR_FACE_SHAPE_TYPE_CUT_CHEEK, mFaceBeautyCutCheek);
        int textureId;
        if(mRaceMode == RaceMode.MIX){
            textureId = mBeautifyNative.process(cameraTextureId, cameraNV21Byte, cameraWidth, cameraHeight,
                    cameraWidth, rotation, ALR_FLAG_TEXTURE_OES_EXTERNAL);
        } else if(mRaceMode == RaceMode.BUFFER){
            textureId = mBeautifyNative.processBuffer(cameraNV21Byte,ALR_IMAGE_FORMAT_NV21, cameraWidth, cameraHeight,
                    cameraWidth, rotation, ALR_FLAG_OUTPUT_FLIP_Y);
        } else {
//            解决横屏时美颜不生效问题
            if(rotation == 0){
                rotation = 180;
            } else if(rotation == 180){
                rotation = 0;
            }
            textureId = mBeautifyNative.processTexture(cameraTextureId, cameraWidth, cameraHeight, rotation, AliyunBeautifyNative.ALR_FLAG_TEXTURE_OES_EXTERNAL);
        }
        return textureId;
    }

    private void clearParams(){
        mBeautifyNative.setSkinBuffing(0);
        mBeautifyNative.setSharpen(0);
        mBeautifyNative.setSkinWhitening(0);
        //            1 窄脸 cut_face
        mBeautifyNative.setFaceShape(ALR_FACE_SHAPE_TYPE_CUT_FACE, 0);
        //       2 瘦脸 thin_face
        mBeautifyNative.setFaceShape(ALR_FACE_SHAPE_TYPE_THIN_FACE, 0);
        //        3 脸长 small_face 0-100
        mBeautifyNative.setFaceShape(ALR_FACE_SHAPE_TYPE_LONG_FACE, 0);
        //        4 下巴缩短 lower_jaw
        mBeautifyNative.setFaceShape(ALR_FACE_SHAPE_TYPE_LOWER_JAW, 0);
        //       8 大眼 big_eye
        mBeautifyNative.setFaceShape(ALR_FACE_SHAPE_TYPE_BIG_EYE, 0);
        //      14 瘦鼻 thin_nose
        mBeautifyNative.setFaceShape(ALR_FACE_SHAPE_TYPE_THIN_NOSE, 0);
        //      18 唇宽 mouth_shape
        mBeautifyNative.setFaceShape(ALR_FACE_SHAPE_TYPE_MOUTH_WIDTH, 0);
        //         下颌 thin_mandible
        mBeautifyNative.setFaceShape(ALR_FACE_TYPE_THIN_MANDIBLE, 0);
        //        颧骨 cut_cheek
        mBeautifyNative.setFaceShape(ALR_FACE_SHAPE_TYPE_CUT_CHEEK, 0);

    }

    /**
     * 美白
     *
     * @param mFaceBeautyColorLevel 美白
     */
    public RaceManager setFaceBeautyWhite(float mFaceBeautyColorLevel) {
        this.mFaceBeautyColorLevel = mFaceBeautyColorLevel;
        Log.d(TAG, "setFaceBeautyWhite: " + this.mFaceBeautyColorLevel );

        return this;
    }

    /**
     * 磨皮
     *
     * @param mFaceBeautyBlurLevel  磨皮
     */
    public RaceManager setFaceBeautyBuffing(float mFaceBeautyBlurLevel) {
        this.mFaceBeautyALLBlurLevel = mFaceBeautyBlurLevel;
        Log.d(TAG, "mFaceBeautyALLBlurLevel: " + this.mFaceBeautyALLBlurLevel );
        return this;
    }

    /**
     * 锐化
     *
     * @param mFaceBeautySharpLevel 锐化
     */
    public RaceManager setFaceBeautySharpLevel(float mFaceBeautySharpLevel) {
        this.mFaceBeautySharpLevel = mFaceBeautySharpLevel;
        Log.d(TAG, "mFaceBeautySharpLevel: " + this.mFaceBeautySharpLevel);

        return this;
    }





    /**
     * 美型参数
     *
     * @param shapeParam    美型参数
     */
    public void setShapeParam(BeautyShapeParams shapeParam) {
        Log.d(TAG, "setShapeParam: " + shapeParam.toString() );
        this.mFaceBeautyCutFace = shapeParam.mBeautyCutFace / 100 * 3;
        this.mFaceBeautyThinFace = shapeParam.mBeautyThinFace / 100 * 1.5f * 3;
        this.mFaceBeautyLongFace = shapeParam.mBeautyLongFace / 100 * -1f * 3;
        this.mFaceBeautyLowerJaw = shapeParam.mBeautyLowerJaw / 100 * -1f * 3;
        this.mFaceBeautyBigEye = shapeParam.mBeautyBigEye / 100 * 3;
        this.mFaceBeautyThinNose = shapeParam.mBeautyThinNose / 100 * 3;
        this.mFaceBeautyMouthWidth = shapeParam.mBeautyMouthWidth / 100 * -1f * 3;
        this.mFaceBeautyThinMandible = shapeParam.mBeautyThinMandible / 100 * 3;
        this.mFaceBeautyCutCheek = shapeParam.mBeautyCutCheek / 100 * 3;
        Log.d(TAG, "mFaceBeautyThinFace: " + mFaceBeautyThinFace );
    }



    public void setRaceDebug(boolean mRaceDebug) {
        this.mRaceDebug = mRaceDebug;
    }



    public void setRaceMode(RaceMode mode){
        this.mRaceMode = mode;
    }

    public void release() {
        Log.d(TAG, "release: mBeautifyNative.destroy()" );
        mInited = false;
        mContext = null;
        mBeautifyNative.destroy();
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
        int detectRotation = 0;
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
            case 270:
                detectRotation =  0;
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
        int detectRotation = 0;
        switch (rotation){
            case 0:
                detectRotation = 270;
            break;
            case 90:
                detectRotation =  0;
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


    public byte[] rotateYUVDegree270AndMirror(byte[] data, int imageWidth, int imageHeight) {
        if(mYuv == null || mYuv.length < imageWidth * imageHeight * 3 / 2){
            mYuv = new byte[imageWidth * imageHeight * 3 / 2];
        }
// Rotate and mirror the Y luma
        int i = 0;
        int maxY = 0;
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
        int maxUV = 0;
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

    public byte[] rotateYUVDegree90(byte[] data, int imageWidth, int imageHeight) {
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
