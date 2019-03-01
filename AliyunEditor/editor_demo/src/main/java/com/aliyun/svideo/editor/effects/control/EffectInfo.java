/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.effects.control;

import android.os.Parcel;
import android.os.Parcelable;

import com.aliyun.editor.TimeEffectType;
import com.aliyun.svideo.sdk.external.struct.form.AspectForm;

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


}
