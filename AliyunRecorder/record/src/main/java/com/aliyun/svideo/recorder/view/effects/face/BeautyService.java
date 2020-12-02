package com.aliyun.svideo.recorder.view.effects.face;

import android.content.Context;
import android.support.annotation.IntDef;

import com.aliyun.svideo.base.widget.beauty.BeautyParams;
import com.aliyun.svideo.base.widget.beauty.enums.BeautyLevel;
import com.aliyun.svideo.base.widget.beauty.enums.BeautyMode;
import com.aliyun.svideo.recorder.bean.RenderingMode;
import com.aliyun.svideo.recorder.race.RaceManager;
import com.aliyun.svideo.recorder.util.SharedPreferenceUtils;

public class BeautyService {

    public static final int BEAUTY_FACE = 0;
    public static final int BEAUTY_SKIN = 1;

    @IntDef({BEAUTY_FACE, BEAUTY_SKIN})
    public @interface BeautyType {
    }

    //    private FaceUnityManager faceUnityManager;
    private RaceManager raceManager;

    private BeautyParams beautyParams;
    private RenderingMode renderingMode = RenderingMode.Race;

    /**
     * FaceUnity
     * normal跟advanced分开，当是normal的时候不需要初始化BeautyParams
     */
//    public BeautyParams bindFaceUnity(Context context, FaceUnityManager faceUnityManager) {
//        renderingMode = RenderingMode.FaceUnity;
//        this.faceUnityManager = faceUnityManager;
//        return initBeautyParam(context);
//    }
//
//    public void bindNormalFaceUnity( FaceUnityManager faceUnityManager) {
//        renderingMode = RenderingMode.FaceUnity;
//        this.faceUnityManager = faceUnityManager;
//    }
    /**
     * Race
     * normal跟advanced分开，当是normal的时候不需要初始化BeautyParams
     */
    public BeautyParams bindRace(Context context, RaceManager raceManager) {
        renderingMode = RenderingMode.Race;
        this.raceManager = raceManager;
        return initBeautyParam(context);
    }

    public void bindNormalRace( RaceManager raceManager) {
        renderingMode = RenderingMode.Race;
        this.raceManager = raceManager;

    }

    private BeautyParams initBeautyParam(Context context) {

        //高级美颜等级
        int beautyFaceLevel = SharedPreferenceUtils.getBeautyFaceLevel(context);
        //美肌等级
        int beautySkinLevel = SharedPreferenceUtils.getBeautySkinLevel(context);

        float beautyfaceValue = checkBeautyParam(beautyFaceLevel) / 100;
        float beautySkinValue = checkBeautyParam(beautySkinLevel) / 100;

        beautyParams = new BeautyParams();
        beautyParams.beautyBuffing = (int) (beautyfaceValue * 10);
        if (renderingMode == RenderingMode.FaceUnity) {
//            if (faceUnityManager != null) {
//                faceUnityManager
//                .setFaceBeautyWhite(beautyfaceValue)
//                .setFaceBeautyRuddy(beautyfaceValue)
//                .setFaceBeautyBuffing(beautyfaceValue * 10 * 0.6f)
//                .setFaceBeautyBigEye(beautySkinValue * 1.5f)
//                .setFaceBeautySlimFace(beautySkinValue);
//            }
        } else if (renderingMode == RenderingMode.Race) {
            if (raceManager != null) {
                raceManager
                .setFaceBeautyWhite(beautyfaceValue)
                .setFaceBeautySharpLevel(beautyfaceValue)
                .setFaceBeautyBuffing(beautyfaceValue)
                .setFaceBeautyBigEye(beautySkinValue * 2)
                .setFaceBeautySlimFace(beautySkinValue * 2);
            }
        }
        return beautyParams;
    }

    private float checkBeautyParam(int level) {
        BeautyLevel beautyLevel;
        switch (level) {
        case 0:
            beautyLevel = BeautyLevel.BEAUTY_LEVEL_ZERO;
            break;
        case 1:
            beautyLevel = BeautyLevel.BEAUTY_LEVEL_ONE;
            break;
        case 2:
            beautyLevel = BeautyLevel.BEAUTY_LEVEL_TWO;
            break;
        case 3:
            beautyLevel = BeautyLevel.BEAUTY_LEVEL_THREE;
            break;
        case 4:
            beautyLevel = BeautyLevel.BEAUTY_LEVEL_FOUR;
            break;
        case 5:
            beautyLevel = BeautyLevel.BEAUTY_LEVEL_FIVE;
            break;
        default:
            beautyLevel = BeautyLevel.BEAUTY_LEVEL_THREE;
            break;
        }
        return beautyLevel.getValue();
    }

    public void updateAll(BeautyParams beautyParams) {
        if (beautyParams == null) {
            throw new IllegalArgumentException("beautyParams is null");
        }
        if (renderingMode == RenderingMode.FaceUnity) {
//            if (faceUnityManager != null) {
//                faceUnityManager
//                .setFaceBeautyWhite(beautyParams.beautyWhite / 100)
//                .setFaceBeautyRuddy(beautyParams.beautyRuddy / 100)
//                .setFaceBeautyBuffing(beautyParams.beautyBuffing / 10 * 0.6f)
//                .setFaceBeautySlimFace(beautyParams.beautySlimFace / 100)
//                .setFaceBeautyBigEye(beautyParams.beautyBigEye / 100 * 1.5f);
//            }
        } else if (renderingMode == RenderingMode.Race) {
            if (raceManager != null) {
                raceManager
                .setFaceBeautyWhite(beautyParams.beautyWhite / 100)
                .setFaceBeautySharpLevel(beautyParams.beautyRuddy / 100)
                .setFaceBeautyBuffing(beautyParams.beautyBuffing / 100)
                .setFaceBeautySlimFace(beautyParams.beautySlimFace / 100)
                .setFaceBeautyBigEye(beautyParams.beautyBigEye / 100);
            }
        }
    }

    public void setBeautyParam(BeautyParams beautyParams, @BeautyType int beautyType) {
        if (beautyParams == null) {
            throw new IllegalArgumentException("beautyParams is null");
        }
        this.beautyParams = beautyParams;
        if (renderingMode == RenderingMode.FaceUnity) {
//            if (beautyType == BEAUTY_FACE) {
//                faceUnityManager
//                .setFaceBeautyWhite(beautyParams.beautyWhite / 100)
//                .setFaceBeautyRuddy(beautyParams.beautyRuddy / 100)
//                .setFaceBeautyBuffing(beautyParams.beautyBuffing / 10 * 0.6f);
//            } else {
//                faceUnityManager
//                .setFaceBeautySlimFace(beautyParams.beautySlimFace / 100 * 1.5f)
//                .setFaceBeautyBigEye(beautyParams.beautyBigEye / 100 * 1.5f);
//            }
        } else if (renderingMode == RenderingMode.Race) {
            if (beautyType == BEAUTY_FACE) {
                raceManager
                .setFaceBeautyWhite(beautyParams.beautyWhite / 100)
                .setFaceBeautySharpLevel(beautyParams.beautyRuddy / 100)
                .setFaceBeautyBuffing(beautyParams.beautyBuffing / 100);
            } else {
                raceManager
                .setFaceBeautySlimFace(beautyParams.beautySlimFace / 50.F)
                .setFaceBeautyBigEye(beautyParams.beautyBigEye / 50.F);
            }
        }

    }

    public BeautyParams getBeautyParam() {
        return beautyParams;
    }

    public void changeBeautyMode(BeautyMode beautyMode) {

        if (beautyMode == BeautyMode.Normal) {

        } else {

        }
    }

    public void unbindFaceUnity() {
//        faceUnityManager = null;
        raceManager = null;
    }

    public void saveBeautyMode(Context context, BeautyMode beautyMode) {
        SharedPreferenceUtils.setBeautyMode(context, beautyMode);
    }


    public void saveSelectParam(Context context, int beautyNormalFacePosition, int beautyFacePosition,
                                int beautySkinPosition, int beautySharpPosition) {
        SharedPreferenceUtils.setBeautyNormalFaceLevel(context, beautyNormalFacePosition);
        SharedPreferenceUtils.setBeautyFaceLevel(context, beautyFacePosition);
        SharedPreferenceUtils.setBeautySkinLevel(context, beautySkinPosition);
        SharedPreferenceUtils.setBeautyShapeLevel(context, beautySharpPosition);
    }
}
