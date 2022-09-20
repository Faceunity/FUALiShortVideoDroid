package com.aliyun.svideo.editor.effects.pip.msg;

import com.aliyun.Visible;

@Visible
public class PipAngleMsg {

    public float progress;

    public PipAngleMsg progress(float progress){
        this.progress = progress;
        return this;
    }
}
