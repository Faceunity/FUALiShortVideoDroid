/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.control;


import androidx.annotation.Nullable;

import com.aliyun.svideosdk.common.struct.project.Source;
import com.aliyun.svideosdk.editor.AudioEffectType;
import com.aliyun.svideosdk.editor.TimeEffectType;
import com.aliyun.svideo.base.Form.AspectForm;
import com.aliyun.svideo.editor.effects.transition.TransitionChooserView;
import com.aliyun.svideosdk.common.struct.effect.TransitionBase;

import java.io.Serializable;
import java.util.List;


public class EffectInfo implements Serializable {

    /**
     * 用作取消的批处理
     * 1.转场的取消
     */
    public List<EffectInfo> mutiEffect;

    public UIEditorPage type;

    public TimeEffectType timeEffectType;
    /**
     * 音效类型
     */
    public AudioEffectType audioEffectType;

    /**
     * 音效权重
     */
    public int soundWeight;

    public float timeParam;

    public boolean isMoment;

    public boolean isCategory;

    public boolean isAudioMixBar;

    public boolean isLocalMusic;

    /**
     * 字体文件路径
     * @deprecated 使用 {@link #fontSource}替代
     */
    @Deprecated
    public String fontPath;

    /**
     * 字体文件资源
     */
    public Source fontSource;

    public int id;

    public int mixId;

    public List<AspectForm> list;

    public long startTime = -1;

    public long endTime;

    public long streamStartTime;

    public long streamEndTime;

    private Source mSource;

    public int musicWeight;


    /**
     * 获取资源文件路径
     * @deprecated 使用 {@link #getSource()}替代
     * @return 资源文件路径
     */
    /****
     * Gets the file of a resource.
     * @deprecated Replaced by {@link ##getSource()}.
     * @return path
     */
    @Deprecated
    public String getPath() {
        if (mSource != null) {
            return mSource.getPath();
        }
        return null;
    }

    /**
     * 设置资源文件路径
     * @param path
     * @deprecated 使用 {@link #setSource(Source)}替代
     */
    /****
     * Sets the file of a resource.
     * @param path
     * @deprecated Replaced by {@link ##setSource(Source)}.
     */
    @Deprecated
    public void setPath(String path) {
        mSource = new Source(path);
    }

    /**
     * 获取资源
     *
     * @return 资源
     */
    /****
     * Gets the file of a resource.
     * @return Source
     */
    public Source getSource() {
        return mSource;
    }

    /**
     * 设置资源
     * @param source 资源
     */
    /****
     * Sets the file of a resource.
     * @param source Source
     */
    public void setSource(final Source source) {
        mSource = source;
    }

    public int transitionType;

    public int clipIndex;

    public boolean isUpdateTransition = false;

    public boolean needReplay = false;

    public TransitionBase transitionBase;

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj){
            return true;
        }

        if (obj == null){
            return false;
        }

        if (obj instanceof EffectInfo){
            EffectInfo obj1 = (EffectInfo)obj;
            if (transitionType == obj1.transitionType){
                if (transitionType == TransitionChooserView.EFFECT_CUSTOM){
                    return mSource.equals(obj1.mSource);
                }else {
                    return true;
                }
            }
        }

        return false;

    }
}
