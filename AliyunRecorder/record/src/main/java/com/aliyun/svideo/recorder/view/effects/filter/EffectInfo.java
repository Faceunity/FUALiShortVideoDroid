/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.recorder.view.effects.filter;

import com.aliyun.editor.TimeEffectType;
import com.aliyun.svideo.sdk.external.struct.form.AspectForm;

import java.util.List;

/**
 * 特效javaBean
 * @author xlx
 */
public class EffectInfo {
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

    public long startTime;

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
}
