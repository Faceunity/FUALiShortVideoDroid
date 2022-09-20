package com.aliyun.svideo.editor.effects.pip.msg;

import com.aliyun.Visible;

@Visible
public class PipVignetteMsg {

    public float progress;

    public PipVignetteMsg progress(float progress){
        this.progress = progress;
        return this;
    }
}
