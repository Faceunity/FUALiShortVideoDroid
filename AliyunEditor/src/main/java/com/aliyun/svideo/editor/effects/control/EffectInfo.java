/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.control;


import android.support.annotation.Nullable;

import com.aliyun.editor.AudioEffectType;
import com.aliyun.editor.TimeEffectType;
import com.aliyun.svideo.base.Form.AspectForm;
import com.aliyun.svideo.editor.effects.transition.TransitionChooserView;
import com.aliyun.svideo.sdk.external.struct.effect.TransitionBase;

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

    public String fontPath;

    public int id;

    public int mixId;

    public List<AspectForm> list;

    public long startTime = -1;

    public long endTime;

    public long streamStartTime;

    public long streamEndTime;

    String path;

    public int musicWeight;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int transitionType;

    public int clipIndex;

    public boolean isUpdateTransition = false;

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
                    return path.equals(obj1.getPath());
                }else {
                    return true;
                }
            }
        }

        return false;

    }
}
