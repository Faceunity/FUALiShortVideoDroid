package com.aliyun.svideo.editor.effects.pip.msg;

import com.aliyun.Visible;

@Visible
public class PipBrighnessMsg {

    public float progress;

    public PipBrighnessMsg progress(float progress){
        this.progress = progress;
        return this;
    }
}
