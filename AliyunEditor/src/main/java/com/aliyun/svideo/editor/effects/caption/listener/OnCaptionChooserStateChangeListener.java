package com.aliyun.svideo.editor.effects.caption.listener;

import android.graphics.PointF;
import androidx.annotation.Nullable;

import com.aliyun.svideosdk.common.AliyunColor;
import com.aliyun.svideosdk.common.AliyunTypeface;
import com.aliyun.svideosdk.common.struct.project.Source;
import com.aliyun.svideosdk.editor.impl.AliyunPasterControllerCompoundCaption;

public interface OnCaptionChooserStateChangeListener {
    void onCaptionTextChanged(String text);

    void onCaptionTextColorChanged(AliyunColor aliyunColor);

    void onCaptionTextBackgroundColorChanged(AliyunColor aliyunColor);

    void onCaptionTextBackgroundCornerRadiusChanged(int radiusInPx);

    void onCaptionTextAlignmentChanged(int alignment);

    void onCaptionTextFontTypeFaceChanged(AliyunTypeface aliyunTypeface);

    void onCaptionTextFontTtfChanged(Source fontSource);

    void onCaptionTextStrokeColorChanged(AliyunColor aliyunColor);

    void onCaptionTextStrokeWidthChanged(int width);

    void onCaptionTextShandowColorChanged(AliyunColor aliyunColor);

    void onCaptionTextShandowOffsetChanged(PointF shadowOffset);

    void onBubbleEffectTemplateChanged(@Nullable Source bubbleSource, @Nullable Source fontSource);

    void onFontEffectTemplateChanged(@Nullable Source templateSource);

    void onCaptionFrameAnimation(int animationIndex);

    void onCaptionCancel();

    void onCaptionConfirm();

    AliyunPasterControllerCompoundCaption getAliyunPasterController();

    boolean isInvert();


}
