package com.aliyun.svideo.editor.effects.caption.manager;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.svideo.downloader.DownloaderManager;
import com.aliyun.svideo.downloader.FileDownloaderCallback;
import com.aliyun.svideo.downloader.FileDownloaderModel;
import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.editor.bean.AlivcCaptionBorderBean;
import com.aliyun.svideo.editor.contant.CaptionConfig;
import com.aliyun.svideo.editor.view.IAlivcEditView;
import com.aliyun.svideo.editor.effects.caption.listener.OnCaptionChooserStateChangeListener;
import com.aliyun.svideosdk.common.AliyunColor;
import com.aliyun.svideosdk.common.AliyunFontStyle;
import com.aliyun.svideosdk.common.AliyunTypeface;
import com.aliyun.svideosdk.common.ISource;
import com.aliyun.svideosdk.common.struct.effect.ActionBase;
import com.aliyun.svideosdk.common.struct.project.Source;
import com.aliyun.svideosdk.editor.AliyunIEditor;
import com.aliyun.svideosdk.editor.AliyunIPasterController;
import com.aliyun.svideosdk.editor.AliyunPasterManager;
import com.aliyun.svideosdk.editor.impl.AliyunPasterControllerCompoundCaption;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CaptionManager {
    private static final String TAG = "CaptionManager";

    public static void applyDurationChanged(AliyunPasterControllerCompoundCaption controller, long startTime, long duration) {
        if (controller != null) {
            controller.setStartTime(startTime, TimeUnit.MILLISECONDS);
            controller.setDuration(duration, TimeUnit.MILLISECONDS);
            controller.apply();
        }
    }


    /**
     * 增加字幕
     *
     * @return
     */
    @Nullable
    public static AliyunPasterControllerCompoundCaption addCaptionWithStartTime(Context context, AliyunPasterManager manager, @Nullable String content, String fontPath, long currentPostion, long duration) {
        AliyunPasterControllerCompoundCaption controller = null;
        if (manager != null && duration > 0) {
            if (TextUtils.isEmpty(content)) {
                content = context.getString(R.string.alivc_editor_effect_text_default);
            }
            controller = manager.addCaptionWithStartTime(content, null, new Source(fontPath), currentPostion, duration, TimeUnit.MILLISECONDS);
            controller.setColor(new AliyunColor(Color.parseColor("#FFF9FAFB")));
            int ret = controller.apply();
            if (ret != 0) {
                removeCaption(manager, controller);
                return null;
            }
        }

        return controller;

    }


    /**
     * 移除字幕
     */
    public static void removeCaption(AliyunPasterManager aliyunPasterManager, AliyunPasterControllerCompoundCaption aliyunPasterControllerCompoundCaption) {
        if (aliyunPasterManager == null || aliyunPasterControllerCompoundCaption == null) {
            Log.d(TAG, "removeCaption: params is null ");
            return;
        }
        aliyunPasterManager.remove(aliyunPasterControllerCompoundCaption);

    }


    /**
     * 修改字幕文本
     *
     * @param controller
     * @param text
     */
    public static void applyCaptionTextChanged(final AliyunPasterControllerCompoundCaption controller, final String text) {
        if (controller != null && !TextUtils.isEmpty(text)) {
            controller.setText(text);
            controller.apply();
        }
    }

    public static void applyCaptionTextColorChanged(AliyunPasterControllerCompoundCaption controller, AliyunColor aliyunColor) {
        if (controller != null && aliyunColor != null) {
            controller.setColor(aliyunColor);
            controller.apply();
        }

    }

    public static void applyCaptionTextBackgroundColorChanged(AliyunPasterControllerCompoundCaption controller, AliyunColor aliyunColor) {
        if (controller != null && aliyunColor != null) {
            controller.setBackgroundColor(aliyunColor);
            controller.apply();
        }

    }

    public static void applyCaptionTextBackgroundCornerRadiusChanged(AliyunPasterControllerCompoundCaption controller, int cornerRadius) {
        if (controller != null ) {
            controller.setBackgroundCornerRadius(cornerRadius);
            controller.apply();
        }

    }

    public static void applyCaptionTextFontTypeFaceChanged(AliyunPasterControllerCompoundCaption controller, AliyunTypeface aliyunTypeface) {
        if (controller != null) {
            controller.setFontTypeface(aliyunTypeface);
            controller.apply();
        }
    }

    public static void applyCaptionTextAlignmentChanged(AliyunPasterControllerCompoundCaption controller, int alignment) {
        if (controller != null && alignment != 0) {
            controller.setTextAlignment(alignment);
            controller.apply();
        }
    }


    public static void applyCaptionTextFontTtfChanged(AliyunPasterControllerCompoundCaption controller, Source fontSource) {
        if (controller != null) {
            if (fontSource  != null) {
                if (!TextUtils.isEmpty(fontSource.getPath())) {
                    if (!fontSource.getPath().endsWith(CaptionConfig.FONT_NAME)) {
                        fontSource.setPath(fontSource.getPath() + CaptionConfig.FONT_NAME);
                    }
                } else {
                    fontSource.setPath(null);
                }
            }
            controller.setFontPath(fontSource);
            controller.apply();
        }
    }
    public static void applyCaptionTextStrokeColorChanged(AliyunPasterControllerCompoundCaption controller, AliyunColor aliyunColor) {
        if (controller != null && aliyunColor != null) {
            controller.setOutlineColor(aliyunColor);
            controller.apply();
        }
    }

    public static void applyCaptionTextStrokeWidthChanged(AliyunPasterControllerCompoundCaption controller, int width) {
        if (controller != null && width > 0) {
            controller.setOutlineWidth(width);
            controller.apply();
        }
    }

    public static void applyCaptionTextShandowColorChanged(AliyunPasterControllerCompoundCaption controller, AliyunColor aliyunColor) {
        if (controller != null && aliyunColor != null) {
            controller.setShadowColor(aliyunColor);
            controller.apply();
        }
    }

    public static void applyCaptionTextShandowOffsetChanged(AliyunPasterControllerCompoundCaption controller, PointF shadowOffset) {
        if (controller != null && shadowOffset != null) {
            controller.setShadowOffset(shadowOffset);
            controller.apply();
        }
    }

    /**
     * @deprecated 使用 {@link #applyBubbleEffectTemplateChanged(AliyunPasterControllerCompoundCaption,Source)} 替代
     */
    public static void applyBubbleEffectTemplateChanged(AliyunPasterControllerCompoundCaption controller, String template) {
        applyBubbleEffectTemplateChanged(controller, new Source(template));
    }


    public static void applyBubbleEffectTemplateChanged(AliyunPasterControllerCompoundCaption controller, Source template) {
        if (controller != null) {
            controller.setBubbleEffectTemplate(template);
            controller.apply();
            //添加气泡会更改duration时间，需要调整进度的大小
            Log.d(TAG, "applyBubbleEffectTemplateChanged: " + controller.getDuration());
        }
    }

    public static void applyFontEffectTemplateChanged(AliyunPasterControllerCompoundCaption controller, Source template) {
        if (controller != null) {
            controller.setFontEffectTemplate(template);
            controller.apply();
        }
    }

    public static void applyCaptionFrameAnimation(IAlivcEditView iAlivcEditView, AliyunPasterControllerCompoundCaption controller, ActionBase actionBase) {
        if (controller == null) {
            return;
        }

        controller.setFrameAnimation(actionBase);
        controller.apply();
        iAlivcEditView.getAliyunIEditor().seek(controller.getStartTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);

    }

    public static void applyCaptionPostion(AliyunPasterControllerCompoundCaption controller, PointF pointF) {
        if (controller != null && pointF != null) {
            controller.setPosition(pointF);
            controller.apply();
        }
    }

    public static void applyCaptionBorderChanged(AliyunPasterControllerCompoundCaption controller, float roation, float[] scale, PointF pointF) {
        if (controller == null) {
            return;
        }
        controller.setRotate(roation);
        if (scale != null && scale.length > 0) {
            controller.setScale(scale[0]);
        }
        if (pointF != null) {
            controller.setPosition(pointF);
        }
        controller.apply();

    }

    /**
     * 获取已下载字体
     */
    @NonNull
    public static List<FileDownloaderModel> getFontFromLocal() {

        List<FileDownloaderModel> fileDownloaderModels = DownloaderManager.getInstance().getDbController()
                .getResourceByType(CaptionConfig.FONT_TYPE);
        if (fileDownloaderModels == null) {
            fileDownloaderModels = new ArrayList<>();
        }
        FileDownloaderModel fileDownloaderModel = new FileDownloaderModel();
        fileDownloaderModel.setIcon(CaptionConfig.SYSTEM_FONT);
        fileDownloaderModels.add(0, fileDownloaderModel);
        return fileDownloaderModels;
    }

    public static void downloadFont(FileDownloaderModel model, FileDownloaderCallback fileDownloaderCallback) {
        if (model == null || fileDownloaderCallback == null) {
            return;
        }
        model.setEffectType(CaptionConfig.FONT_TYPE);
        model.setIsunzip(1);
        FileDownloaderModel fileMode = DownloaderManager.getInstance().addTask(model, model.getUrl());
        DownloaderManager.getInstance().startTask(fileMode.getTaskId(), fileDownloaderCallback);
    }

    public static List<FileDownloaderModel> getBubbleFromLocal() {
        return DownloaderManager.getInstance().getDbController().getResourceByType(CaptionConfig.CAPTION_TYPE);
    }


    public static void downloadPaster(FileDownloaderModel model, FileDownloaderCallback fileDownloaderCallback) {
        model.setEffectType(CaptionConfig.CAPTION_TYPE);
        model.setIsunzip(1);
        FileDownloaderModel fileMode = DownloaderManager.getInstance().addTask(model, model.getUrl());
        DownloaderManager.getInstance().startTask(fileMode.getTaskId(), fileDownloaderCallback);
    }


    public static RectF getCaptionRectF(ViewGroup.LayoutParams layoutParams, AliyunPasterControllerCompoundCaption controller) {
        if (layoutParams == null || controller == null) {
            return null;
        }
        RectF size = controller.getSize();
        return size;
    }

    public static AlivcCaptionBorderBean getCaptionSize(ViewGroup.LayoutParams layoutParams, AliyunPasterControllerCompoundCaption controller) {
        if (layoutParams == null || controller == null) {
            return null;
        }
        RectF size = controller.getSize();
        return new AlivcCaptionBorderBean(size, controller.getScale(), controller.getRotate());
    }


    /**
     * @param mPasterManager
     * @param e
     * @param tempPointF
     * @param currentPlayPosition
     * @return
     */
    @Nullable
    public static AliyunIPasterController findControllerAtPoint(AliyunPasterManager mPasterManager, MotionEvent e, PointF tempPointF, long currentPlayPosition) {
        if (mPasterManager == null || e == null || tempPointF == null || currentPlayPosition < 0) {
            return null;
        }
        tempPointF.x = e.getX();
        tempPointF.y = e.getY();
        AliyunIPasterController controllerAtPoint = mPasterManager.findControllerAtPoint(tempPointF, currentPlayPosition, TimeUnit.MILLISECONDS);
        //这里屏蔽添加动画，移动后禁止操作
        if (isCaptionAnimatorActive(currentPlayPosition, controllerAtPoint)) {
            return null;
        }
        return controllerAtPoint;
    }

    /**
     * 字幕添加动画后，不支持编辑，
     *
     * @param currentPlayPosition
     * @param controllerAtPoint
     * @return
     */
    private static boolean isCaptionAnimatorActive(long currentPlayPosition, AliyunIPasterController controllerAtPoint) {
        if (controllerAtPoint instanceof AliyunPasterControllerCompoundCaption) {
            List<ActionBase> frameAnimations = ((AliyunPasterControllerCompoundCaption) controllerAtPoint).getFrameAnimations();
            if (frameAnimations != null && frameAnimations.size() > 0) {
                long startTime = controllerAtPoint.getStartTime(TimeUnit.MILLISECONDS);
                if (currentPlayPosition - startTime > 200) {
                    return true;
                }
            }

        }
        return false;
    }

    public static boolean isTextOnly(AliyunPasterControllerCompoundCaption aliyunPasterController) {
        if (aliyunPasterController != null) {
            return TextUtils.isEmpty(aliyunPasterController.getBubbleEffectTemplate());
        }
        return true;
    }

    public static int getRootViewHeight(View view) {
        int contentHeight = 0;
        if (view != null) {
            View rootView = view.getRootView();
            if (rootView != null) {
                View contentView = rootView.findViewById(android.R.id.content);
                if (contentView != null) {
                    contentHeight = contentView.getHeight();
                }
            }
        }
        return contentHeight;
    }


    public static int getCaptionControllerId(OnCaptionChooserStateChangeListener captionChooserStateChangeListener) {
        int controllerId = 0;
        if (captionChooserStateChangeListener != null) {
            AliyunPasterControllerCompoundCaption aliyunPasterController = captionChooserStateChangeListener.getAliyunPasterController();
            return getCaptionControllerId(aliyunPasterController);
        }
        return controllerId;
    }

    public static int getCaptionControllerId(AliyunPasterControllerCompoundCaption aliyunPasterController) {
        if (aliyunPasterController != null) {
            return System.identityHashCode(aliyunPasterController);
        }
        return 0;
    }

    @Nullable
    public static AliyunPasterControllerCompoundCaption getCaptionController(OnCaptionChooserStateChangeListener captionChooserStateChangeListener) {
        if (captionChooserStateChangeListener != null) {
            return captionChooserStateChangeListener.getAliyunPasterController();
        }
        return null;
    }

    /**
     * 添加弹幕时边界判定
     *
     * @param mAliyunIEditor
     * @return 弹幕开始时间
     */
    public static long captionDurationBoundJudge(AliyunIEditor mAliyunIEditor, long duration) {
        long captionDuration = 0;
        if (mAliyunIEditor != null) {
            long totleDuration = mAliyunIEditor.getPlayerController().getDuration();
            long currentPlayPosition = mAliyunIEditor.getPlayerController().getCurrentPlayPosition();
            long rqDuration = currentPlayPosition + duration;
            if (rqDuration > totleDuration) {
                captionDuration = totleDuration - currentPlayPosition;
                if (captionDuration < CaptionConfig.CAPTION_MIN_DURATION) {
                    captionDuration = 0;
                    Log.w(TAG, "captionDurationBoundJudge: captionDuration less CAPTION_MIN_DURATION");
                }
            } else {
                captionDuration = duration;
            }

        }
        return captionDuration;
    }
}
