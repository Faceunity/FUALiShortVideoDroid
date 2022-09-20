package com.aliyun.svideo.editor.util;

import android.content.Context;
import android.util.Log;

import com.aliyun.svideo.editor.contant.CaptionConfig;
import com.aliyun.svideosdk.common.struct.effect.ActionBase;
import com.aliyun.svideosdk.common.struct.effect.ActionFade;
import com.aliyun.svideosdk.common.struct.effect.ActionFrameBase;
import com.aliyun.svideosdk.common.struct.effect.ActionRotateBy;
import com.aliyun.svideosdk.common.struct.effect.ActionRotateTo;
import com.aliyun.svideosdk.common.struct.effect.ActionScale;
import com.aliyun.svideosdk.common.struct.effect.ActionSet;
import com.aliyun.svideosdk.common.struct.effect.ActionShader;
import com.aliyun.svideosdk.common.struct.effect.ActionTranslate;
import com.aliyun.svideosdk.common.struct.effect.ActionWipe;
import com.aliyun.svideosdk.common.struct.effect.Frame;

import java.util.ArrayList;
import java.util.List;

public class CaptionFrameAnimationUtil {
    private static final String TAG = "CaptionFrameAnimationUt";

    public static ActionBase createAction(Context context, int animIndex, long duration, long startTimeUs, int displayWidth, int displayHeight,
                                          int pasterPostionX, int pasterPostionY) {
        Log.d(TAG, "createAction: pasterWidth:" + pasterPostionX + "  pasterHeight:" + pasterPostionY);
        ActionBase actionBase = null;
        String sourceId = String.valueOf(animIndex);
        switch (animIndex) {
        case CaptionConfig.EFFECT_NONE:
            actionBase = null;
            break;
        case CaptionConfig.EFFECT_UP: {
            //方式1: 整体动画
            ActionTranslate upActionTranslate = new ActionTranslate();
            setActionFromPoint(displayWidth, displayHeight, pasterPostionX, pasterPostionY, upActionTranslate);
            upActionTranslate.setToPointY(1f);
            upActionTranslate.setToPointX(upActionTranslate.getFromPointX());

            //方式2 : 文字动画
            /**
            ActionTranslate upActionTranslate = new ActionTranslate();
            setActionFromPoint(displayWidth, displayHeight, pasterPostionX, pasterPostionY, upActionTranslate);
            upActionTranslate.setScope(ActionBase.Scope.Part);
            ActionBase.PartParam lPartParam = new ActionBase.PartParam();
            lPartParam.setMode(ActionBase.PartParam.Mode.Random);
            lPartParam.setOverlayRadio(0.5f);
            upActionTranslate.setPartParam(lPartParam);
            upActionTranslate.setTranslateType(ActionTranslate.TranslateType.TranslateBy);
            upActionTranslate.setFillBefore(true);
            upActionTranslate.setFillAfter(true);
            upActionTranslate.setToPointY(5f);
            upActionTranslate.setToPointX(upActionTranslate.getFromPointX());
            **/
//            List<Frame<Frame.FramePoint>> frameList = new ArrayList<>();
//            frameList.add(new Frame<>(0.0f, new Frame.FramePoint(0.8f, -0.8f)));
//            frameList.add(new Frame<>(0.25f, new Frame.FramePoint(0.8f, 0.0f)));
//            frameList.add(new Frame<>(0.5f, new Frame.FramePoint(0.0f, 0.0f)));
//            frameList.add(new Frame<>(0.75f, new Frame.FramePoint(0.0f, 0.8f)));
//            frameList.add(new Frame<>(1.0f, new Frame.FramePoint(0.8f, 0.8f)));
//
//            upActionTranslate.setFrameConfig(frameList);

            actionBase = upActionTranslate;
        }
        break;
        case CaptionConfig.EFFECT_RIGHT:
            ActionTranslate rightActionBase = new ActionTranslate();
            setActionFromPoint(displayWidth, displayHeight, pasterPostionX, pasterPostionY, rightActionBase);
            rightActionBase.setToPointX(1f);
            rightActionBase.setToPointY(rightActionBase.getFromPointY());
            actionBase = rightActionBase;
            break;
        case CaptionConfig.EFFECT_LEFT:
            ActionTranslate leftActionTranslate = new ActionTranslate();
            setActionFromPoint(displayWidth, displayHeight, pasterPostionX, pasterPostionY, leftActionTranslate);
            leftActionTranslate.setToPointX(-1f);
            leftActionTranslate.setToPointY(leftActionTranslate.getFromPointY());
            actionBase = leftActionTranslate;
            break;
        case CaptionConfig.EFFECT_DOWN:
            ActionTranslate downActionTranslate = new ActionTranslate();
            setActionFromPoint(displayWidth, displayHeight, pasterPostionX, pasterPostionY, downActionTranslate);
            downActionTranslate.setToPointY(-1f);
            downActionTranslate.setToPointX(downActionTranslate.getFromPointX());
            actionBase = downActionTranslate;
            break;
        case CaptionConfig.EFFECT_SCALE:
            actionBase = new ActionScale();
            ((ActionScale) actionBase).setFromScale(1f);
            ((ActionScale) actionBase).setToScale(0.25f);
            break;
        case CaptionConfig.EFFECT_LINEARWIPE:
            actionBase = new ActionWipe();
            ((ActionWipe) actionBase).setWipeMode(ActionWipe.WIPE_MODE_DISAPPEAR);
            ((ActionWipe) actionBase).setDirection(ActionWipe.DIRECTION_RIGHT);
            break;
        case CaptionConfig.EFFECT_FADE:
            actionBase = new ActionFade();
            //方式1
            ((ActionFade) actionBase).setFromAlpha(1.0f);
            ((ActionFade) actionBase).setToAlpha(0.2f);
//            actionBase.setInterpolatorType(ActionBase.Interpolator.Bound);
            //方式2 帧动画
//            ((ActionFade) actionBase).setAnimationConfig("0.0:0.0;0.1:0.1;0.2:0.5;0.3:1.0;0.8:0.5;0.9:0.1;1.0:0.0;");
//            actionBase.setInterpolatorType(ActionBase.Interpolator.Accelerate);
            //方式3 与Repeat结合
//            ((ActionFade) actionBase).setAnimationConfig("0.0:0.0;0.1:0.1;0.2:0.5;0.5:1.0;");
//            actionBase.setRepeatMode(ActionBase.RepeatMode.Reverse);
//            actionBase.setDuration(duration/2);
//            actionBase.setStartTime(startTimeUs);
            break;
        case CaptionConfig.EFFECT_PRINT: {
            ActionFade lActionFade = new ActionFade();
//            lActionFade.setFromAlpha(0.1f);
//            lActionFade.setToAlpha(1.0f);
//            lActionFade.setAnimationConfig("0.0:0.0;0.7:1.0;");
            List<Frame<Float>> frameList = new ArrayList<>();
            frameList.add(new Frame<Float>(0.0f, 0.0f));
            frameList.add(new Frame<Float>(0.7f, 1.0f));
            lActionFade.setFrameConfig(frameList);
            lActionFade.setScope(ActionBase.Scope.Part);
            lActionFade.setFillBefore(true);
            lActionFade.setFillAfter(true);

            actionBase = lActionFade;
        }
        break;
        case CaptionConfig.EFFECT_ROTATE_BY: {
//            ActionRotateBy lActionRotateBy = new ActionRotateBy();
//            lActionRotateBy.setFromDegree(0);
//            lActionRotateBy.setRotateDegree(Math.PI);
//            actionBase = lActionRotateBy;

            //钟摆采用RotateBy的实现
            ActionSet lActionSet = new ActionSet();
            lActionSet.setMode(ActionSet.AnimationMode.Independent);
            //首选向左转 30度
            ActionRotateBy lActionRotateBy1 = new ActionRotateBy();
            lActionRotateBy1.setFromDegree(0);
            lActionRotateBy1.setRotateDegree((float) (-Math.PI / 6));
            lActionRotateBy1.setCenterX(0.0f);
            lActionRotateBy1.setCenterY(1.0f);
            lActionRotateBy1.setStartTime(startTimeUs);
            lActionRotateBy1.setDuration(duration / 6);

            //再向右转60，并来回转动
            ActionRotateBy lActionRotateBy2 = new ActionRotateBy();
            lActionRotateBy2.setFromDegree((float) (-Math.PI / 6));
            lActionRotateBy2.setRotateDegree((float) (Math.PI * 2 / 6));
            lActionRotateBy2.setCenterX(0.0f);
            lActionRotateBy2.setCenterY(1.0f);

            lActionRotateBy2.setRepeatMode(ActionBase.RepeatMode.Reverse);
            lActionRotateBy2.setStartTime(startTimeUs + duration / 6);
            lActionRotateBy2.setDuration(duration * 2 / 6);


            lActionSet.addAction(lActionRotateBy1);
            lActionSet.addAction(lActionRotateBy2);

            actionBase = lActionSet;
            actionBase.setResId(sourceId);
            return actionBase;
        }
//            break;
        case CaptionConfig.EFFECT_ROTATE_TO: {
//            ActionRotateTo lActionRotateTo = new ActionRotateTo();
//            lActionRotateTo.setFromDegree(0);
//            lActionRotateTo.setRotateToDegree(Math.PI * 2);
//            actionBase = lActionRotateTo;
            ActionSet lActionSet = new ActionSet();
            lActionSet.setMode(ActionSet.AnimationMode.Independent);

            //雨刷使用RotateTo的实现
            ActionRotateTo lActionRotateTo1 = new ActionRotateTo();
            //首选向右转 30度
            lActionRotateTo1.setFromDegree(0);
            lActionRotateTo1.setRotateToDegree((float) (Math.PI / 6.0f));
            lActionRotateTo1.setCenterX(0.0f);
            lActionRotateTo1.setCenterY(-1.0f);
            lActionRotateTo1.setStartTime(startTimeUs);
            lActionRotateTo1.setDuration(duration / 6);
            //再向左转60，并来回转动
            ActionRotateTo lActionRotateTo2 = new ActionRotateTo();
            lActionRotateTo2.setFromDegree((float) (Math.PI / 6.0f));
            lActionRotateTo2.setRotateToDegree((float) (-Math.PI / 6.0f));
            lActionRotateTo2.setCenterX(0.0f);
            lActionRotateTo2.setCenterY(-1.0f);
            lActionRotateTo2.setRepeatMode(ActionBase.RepeatMode.Reverse);
            lActionRotateTo2.setStartTime(startTimeUs + duration / 6);
            lActionRotateTo2.setDuration(duration / 3);


            lActionSet.addAction(lActionRotateTo1);
            lActionSet.addAction(lActionRotateTo2);


            actionBase = lActionSet;
            actionBase.setResId(sourceId);
            return actionBase;
        }

//            break;
        case CaptionConfig.EFFECT_WAVE: {
            ActionShader lActionShader = new ActionShader();
            String vertexFunc = AssetUtil.readAssertResource(context, "shader/wave.vert");
            String fragmentFunc = AssetUtil.readAssertResource(context, "shader/wave.frag");
            lActionShader.setShader(vertexFunc, fragmentFunc);
            actionBase = lActionShader;
        }
        break;
        case CaptionConfig.EFFECT_SET1: {
            //整体动画- Dependent 模式
            ActionSet lActionSet = new ActionSet();
            lActionSet.setMode(ActionSet.AnimationMode.Dependent);

            ActionFade lActionFade1 = new ActionFade();
            lActionFade1.setFromAlpha(0.1f);
            lActionFade1.setToAlpha(1.0f);
            lActionFade1.setDuration(duration / 4);
            lActionFade1.setFillBefore(true);
            lActionSet.addAction(lActionFade1);

            ActionFade lActionFade2 = new ActionFade();
            lActionFade2.setFromAlpha(1.f);
            lActionFade2.setToAlpha(0.1f);
            lActionFade2.setStartOffset(duration * 3 / 4);
            lActionFade2.setDuration(duration / 4);
            lActionSet.addAction(lActionFade2);

            ActionRotateBy lActionRotateBy1 = new ActionRotateBy();
            lActionRotateBy1.setFromDegree(0);
            lActionRotateBy1.setRotateDegree((float) (Math.PI * 2));
            lActionRotateBy1.setDuration(duration / 2);
            lActionRotateBy1.setFillBefore(true);
            lActionRotateBy1.setFillAfter(true);
            lActionSet.addAction(lActionRotateBy1);

            ActionScale lActionScale1 = new ActionScale();
            lActionScale1.setFromScale(0.25f);
            lActionScale1.setToScale(1f);
            lActionScale1.setDuration(duration / 2);
            lActionScale1.setFillBefore(true);
            lActionScale1.setFillAfter(true);

            lActionSet.addAction(lActionScale1);
            actionBase = lActionSet;
            actionBase.setResId(sourceId);

            break;
        }
        case CaptionConfig.EFFECT_SET2: {
            //整体动画 - Independent 模式
            ActionSet lActionSet = new ActionSet();
            lActionSet.setMode(ActionSet.AnimationMode.Independent);

            ActionFade lActionFade1 = new ActionFade();
            lActionFade1.setFromAlpha(0.1f);
            lActionFade1.setToAlpha(1.0f);
            lActionFade1.setStartTime(startTimeUs);
            lActionFade1.setDuration(duration / 3);
            lActionFade1.setFillAfter(true);
            lActionSet.addAction(lActionFade1);

            ActionRotateBy lActionRotateBy1 = new ActionRotateBy();
            lActionRotateBy1.setFromDegree(0);
            lActionRotateBy1.setRotateDegree((float) (Math.PI * 2));
            lActionRotateBy1.setStartTime(startTimeUs);
            lActionRotateBy1.setDuration(duration / 2);
            lActionRotateBy1.setFillAfter(true);
            lActionSet.addAction(lActionRotateBy1);

            ActionScale lActionScale1 = new ActionScale();
            lActionScale1.setFromScale(0.0f);
            lActionScale1.setToScale(1f);
            lActionScale1.setStartTime(startTimeUs);
            lActionScale1.setDuration(duration / 2);
            lActionScale1.setFillAfter(true);
            lActionSet.addAction(lActionScale1);

            actionBase = lActionSet;
            actionBase.setResId(sourceId);

            return actionBase;
        }
        case CaptionConfig.EFFECT_ROTATE_IN: {
            //螺旋上升
            ActionSet lActionSet = new ActionSet();
            lActionSet.setScope(ActionBase.Scope.Part);
            lActionSet.setMode(ActionSet.AnimationMode.Dependent);

            ActionBase.PartParam lPartParam = new ActionBase.PartParam();
            lPartParam.setMode(ActionBase.PartParam.Mode.Sequence);
            lPartParam.setOverlayRadio(0.7f);
            lActionSet.setPartParam(lPartParam);

            ActionFade lActionFade1 = new ActionFade();
            lActionFade1.setFromAlpha(0.1f);
            lActionFade1.setToAlpha(1.0f);
            lActionFade1.setDuration(duration / 4);
            lActionFade1.setFillBefore(true);
            lActionFade1.setFillAfter(true);
            lActionSet.addAction(lActionFade1);


            ActionRotateBy lActionRotateBy1 = new ActionRotateBy();
            lActionRotateBy1.setFromDegree(0);
            lActionRotateBy1.setRotateDegree((float) (Math.PI * 2));
            lActionRotateBy1.setDuration(duration / 2);
            lActionRotateBy1.setFillBefore(true);
            lActionRotateBy1.setRepeatMode(ActionBase.RepeatMode.Restart);
            lActionRotateBy1.setFillAfter(true);
            lActionSet.addAction(lActionRotateBy1);

            ActionTranslate lActionTranslate = new ActionTranslate();
            lActionTranslate.setTranslateType(ActionTranslate.TranslateType.TranslateBy);
            lActionTranslate.setFromPointX(0f);
            lActionTranslate.setFromPointY(-5.0f);

            lActionTranslate.setToPointX(0f);
            lActionTranslate.setToPointY(0.0f);
            lActionTranslate.setDuration(duration);

            lActionSet.addAction(lActionTranslate);
            lActionSet.setFillBefore(true);

            lActionSet.setDuration(duration * 3 / 4);
            lActionSet.setStartTime(startTimeUs);
            actionBase = lActionSet;
            actionBase.setResId(sourceId);

            return actionBase;
        }
        case CaptionConfig.EFFECT_HEAT: {
            //心跳动画
            ActionScale actionScale = new ActionScale();
//            actionBase.setAnimationConfig("0:1.0,1.0;0.06:0.92,0.92;0.12:1.0252,1.0252;0.18:1.1775,1.1775;0.24:1.3116,1.3116;0.3:1.4128,1.4128;0.36:1.4761,1.4761;0.42:1.5,1.5;0.48:1.5,1.5;0.54:1.4727,1.4727;0.6:1.4089,1.4089;0.66:1.3093,1.3093;0.72:1.1779,1.1779;0.78:1.0283,1.0283;0.9:0.92,0.92;1.0:1.0,1.0;");
            List<Frame<Float>> frameList = new ArrayList<>();
            frameList.add(new Frame<>(0.0f, 1.0f));
            frameList.add(new Frame<>(0.06f, 0.92f));
            frameList.add(new Frame<>(0.12f, 1.0252f));
            frameList.add(new Frame<>(0.18f, 1.1775f));
            frameList.add(new Frame<>(0.24f, 1.3116f));
            frameList.add(new Frame<>(0.3f, 1.4128f));
            frameList.add(new Frame<>(0.36f, 1.4761f));
            frameList.add(new Frame<>(0.42f, 1.5f));
            frameList.add(new Frame<>(0.48f, 1.5f));
            frameList.add(new Frame<>(0.54f, 1.4727f));
            frameList.add(new Frame<>(0.6f, 1.4089f));
            frameList.add(new Frame<>(0.66f, 1.3093f));
            frameList.add(new Frame<>(0.72f, 1.1779f));
            frameList.add(new Frame<>(0.78f, 1.0283f));
            frameList.add(new Frame<>(0.9f, 0.92f));
            frameList.add(new Frame<>(1.0f, 1.0f));
            actionScale.setFrameConfig(frameList);
            actionScale.setStartTime(startTimeUs);
            actionScale.setDuration(duration / 2);
            actionScale.setRepeatCount(1);
            actionScale.setRepeatMode(ActionBase.RepeatMode.Restart);

            actionBase = actionScale;
            actionBase.setResId(sourceId);
            return actionBase;
        }
        case CaptionConfig.EFFECT_ROUNDSCAN: {
            ActionShader lActionShader = new ActionShader();
            String vertexFunc = AssetUtil.readAssertResource(context, "shader/round_scan.vert");
            String fragmentFunc = AssetUtil.readAssertResource(context, "shader/round_scan.frag");
            lActionShader.setShader(vertexFunc, fragmentFunc);
            actionBase = lActionShader;
            actionBase.setResId(sourceId);
        }
        break;
        case CaptionConfig.EFFECT_WAVE_JUMP: {
            //波浪弹入
            ActionSet lActionSet = new ActionSet();
            lActionSet.setMode(ActionSet.AnimationMode.Dependent);
            lActionSet.setScope(ActionBase.Scope.Part);
            ActionBase.PartParam lPartParam = new ActionBase.PartParam();
            lPartParam.setMode(ActionBase.PartParam.Mode.Sequence);
            lPartParam.setOverlayRadio(0.6f);
            lActionSet.setPartParam(lPartParam);

            ActionTranslate lActionTranslate = new ActionTranslate();
            lActionTranslate.setTranslateType(ActionTranslate.TranslateType.TranslateBy);
            lActionTranslate.setFromPointX(0f);
            lActionTranslate.setFromPointY(0.0f);
            lActionTranslate.setToPointX(0f);
            lActionTranslate.setToPointY(1.0f);

            lActionTranslate.setDuration(duration / 2);
            lActionSet.addAction(lActionTranslate);

            ActionTranslate lActionTranslate2 = new ActionTranslate();
            lActionTranslate2.setTranslateType(ActionTranslate.TranslateType.TranslateBy);
            lActionTranslate2.setFromPointX(0.0f);
            lActionTranslate2.setFromPointY(1.0f);
            lActionTranslate2.setToPointX(0f);
            lActionTranslate2.setToPointY(0.0f);
            lActionTranslate2.setFillAfter(true);
            lActionTranslate2.setStartOffset(duration / 2);
            lActionTranslate2.setDuration(duration / 2);
            lActionSet.addAction(lActionTranslate2);

            ActionFade lActionFade1 = new ActionFade();
            lActionFade1.setFromAlpha(0.0f);
            lActionFade1.setToAlpha(1.0f);
            lActionFade1.setDuration(duration / 4);
            lActionFade1.setFillBefore(true);
            lActionSet.addAction(lActionFade1);

            lActionSet.setFillBefore(true);
            lActionSet.setFillAfter(true);

            actionBase = lActionSet;
            actionBase.setResId(sourceId);
        }
        default:
            break;
        }


        if (actionBase != null) {

            actionBase.setDuration(duration);
            actionBase.setStartTime(startTimeUs);
            actionBase.setResId(sourceId);
        }

        return actionBase;
    }

    private static void setActionFromPoint(int displayWidth, int displayHeight, int pasterPostionX, int pasterPostionY, ActionTranslate actionTranslate) {
        float xRadio = pasterPostionX * 1.0f / displayWidth;
        float yRadio = pasterPostionY * 1.0f / displayHeight;
        float fromPointX = xRadio * 2 - 1;
        float fromPotinY = 1 - yRadio * 2;
        actionTranslate.setFromPointX(fromPointX);
        actionTranslate.setFromPointY(fromPotinY);
    }

}
