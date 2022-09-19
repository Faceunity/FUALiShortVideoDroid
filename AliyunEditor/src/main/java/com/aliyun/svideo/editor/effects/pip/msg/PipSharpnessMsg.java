package com.aliyun.svideo.editor.effects.pip.msg;

import com.aliyun.Visible;

@Visible
public class PipSharpnessMsg {

    public float progress;

    public PipSharpnessMsg progress(float progress){
        this.progress = progress;
        return this;
    }
}
