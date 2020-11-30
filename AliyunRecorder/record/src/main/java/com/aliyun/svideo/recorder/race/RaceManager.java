package com.aliyun.svideo.recorder.race;
import android.content.Context;
import android.util.Log;
import com.aliyun.race.AliyunBeautifyNative;
import com.aliyun.race.AliyunCommon;
import com.aliyun.svideo.base.widget.beauty.enums.BeautyMode;
import com.aliyun.svideo.base.widget.beauty.sharp.BeautyShapeParams;
import java.io.File;
import static com.aliyun.race.AliyunBeautifyNative.*;


/**
 * RACE 管理类，支持高级美颜(美白，磨皮，红润调整)，美型(脸型，大眼，瘦脸)
 *
 * @author Mulberry
 * create on 2018/7/11.
 */

public class RaceManager {
    private static final String TAG = "RaceManager";
    static RaceManager mRaceManager = null;
    private AliyunBeautifyNative mBeautifyNative;
    private boolean mInited = false;
    private BeautyMode beautyMode = BeautyMode.Advanced;
    private int mShapeType = 0;
    private Context mContext;
    private boolean mRaceDebug = false;
    private int mInitResult = -1;
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
    /**
     * 瘦脸
     */
    private float mFaceBeautyCheekThin = 1.0f;
    /**
     * 大眼
     */
    private float mFaceBeautyEnlargeEye = 0.5f;



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


    private RaceManager() {
    }

    public static RaceManager getInstance() {
        if (mRaceManager != null) {
            return mRaceManager;
        }

        synchronized (RaceManager.class) {
            mRaceManager = new RaceManager();
            return mRaceManager;
        }
    }

    /**
     *  SDK初始化,
     *
     * @param context
     */
    public boolean setUp(Context context) {
        this.mContext = context;
        mBeautifyNative = new AliyunBeautifyNative(context);
        return true;
    }



    /**
     * @param cameraNV21Byte    cameraNV21原始数据
     * @param fuImgNV21Bytes    为用来人脸识别的图像内存数据
     * @param cameraTextureId   为用来绘制的texture id
     * @param cameraWidth       摄像头采集数据的宽
     * @param cameraHeight      摄像头采集数据的高
     * @param frameId           为当前帧数序号，重新初始化后需要从0开始
     * @param currentCameraType    相机类型
     */
    public int draw(byte[] cameraNV21Byte, byte[] fuImgNV21Bytes, int cameraTextureId, int cameraWidth, int cameraHeight, int frameId, int currentCameraType, int rotation) {

        if (
            mFaceBeautyALLBlurLevel == 0 && mFaceBeautySharpLevel == 0 &&
            mFaceBeautyColorLevel == 0 &&
            mFaceBeautyCutFace == 0 &&
            mFaceBeautyThinFace == 0 &&
            mFaceBeautyLongFace == 0 &&
            mFaceBeautyLowerJaw == 0 &&
            mFaceBeautyBigEye == 0 &&
            mFaceBeautyThinNose == 0 &&
            mFaceBeautyMouthWidth == 0 &&
            mFaceBeautyThinMandible == 0 &&
            mFaceBeautyCutCheek == 0) {
            return cameraTextureId;
        }
        if (mBeautifyNative == null) {
            mBeautifyNative = new AliyunBeautifyNative(mContext);
        }

        if (!mInited) {
            mInitResult = mBeautifyNative.initialize();
            AliyunCommon.setLogLevel(AliyunCommon.ALR_LOG_LEVEL_WARN);
            mBeautifyNative.setFaceSwitch(true);
            mInited = true;
        }
        if (mInitResult != 0) {
            return cameraTextureId;
        }
        if ("Nexus 6".equals(android.os.Build.MODEL)) {
            switch (rotation) {
            case 0:
                rotation = 180;
                break;
            case 90:
                rotation = 270;
                break;
            case 180:
                rotation = 0;
                break;
            case 270:
                rotation = 90;
                break;
            case 360:
                rotation = 180;
                break;
            default:
                rotation = 90;
                break;
            }
        }

        mBeautifyNative.setFaceDebug(mRaceDebug);

        mBeautifyNative.setSkinBuffing(mFaceBeautyALLBlurLevel);
        mBeautifyNative.setSharpen(mFaceBeautySharpLevel);
        //Log.d(TAG, " skinBuffing level " + mFaceBeautyALLBlurLevel + " sharpen level " + mFaceBeautySharpLevel+ " SkinWhitening level " + mFaceBeautyColorLevel);
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
//        有效果，画面正常显示
        return mBeautifyNative.process(cameraTextureId, cameraNV21Byte, cameraWidth, cameraHeight,
                                       cameraWidth, rotation, ALR_FLAG_TEXTURE_OES_EXTERNAL);
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
     * 瘦脸
     *
     * @param mFaceBeautyCheekThin 瘦脸
     */
    public RaceManager setFaceBeautySlimFace(float mFaceBeautyCheekThin) {
        this.mFaceBeautyCheekThin = mFaceBeautyCheekThin * 0.75f;
        Log.d(TAG, "setFaceBeautySlimFace: " + this.mFaceBeautyCheekThin );

        return this;
    }

    /**
     * 大眼
     *
     * @param mFaceBeautyEnlargeEye 大眼
     */
    public RaceManager setFaceBeautyBigEye(float mFaceBeautyEnlargeEye) {
        this.mFaceBeautyEnlargeEye = mFaceBeautyEnlargeEye * 0.75f;
        Log.d(TAG, "setFaceBeautyBigEye: " + this.mFaceBeautyEnlargeEye );

        return this;
    }


    /**
     * 美型参数
     *
     * @param shapeParam    美型参数
     */
    public void setShapeParam(BeautyShapeParams shapeParam) {
        Log.d(TAG, "setShapeParam: " + shapeParam.toString() );
        this.mFaceBeautyCutFace = shapeParam.beautyCutFace / 100 * 3;
        this.mFaceBeautyThinFace = shapeParam.beautyThinFace / 100 * 1.5f * 3;
        this.mFaceBeautyLongFace = shapeParam.beautyLongFace / 100 * -1f * 3;
        this.mFaceBeautyLowerJaw = shapeParam.beautyLowerJaw / 100 * -1f * 3;
        this.mFaceBeautyBigEye = shapeParam.beautyBigEye / 100 * 3;
        this.mFaceBeautyThinNose = shapeParam.beautyThinNose / 100 * 3;
        this.mFaceBeautyMouthWidth = shapeParam.beautyMouthWidth / 100 * -1f * 3;
        this.mFaceBeautyThinMandible = shapeParam.beautyThinMandible / 100 * 3;
        this.mFaceBeautyCutCheek = shapeParam.beautyCutCheek / 100 * 3;
        Log.d(TAG, "mFaceBeautyThinFace: " + mFaceBeautyThinFace );
    }


    /**
     * 设置当前选中的tab，美颜、美肌、美型
     * @param mode  tab
     */
    public void setCurrentBeautyMode(BeautyMode mode) {
        this.beautyMode = mode;
    }

    public void setmRaceDebug(boolean mRaceDebug) {
        this.mRaceDebug = mRaceDebug;
    }



    public void release() {
        Log.d(TAG, "release: mBeautifyNative.destroy()" );
        mInited = false;
        mContext = null;
        mRaceManager = null;
        mBeautifyNative.destroy();
    }
}
