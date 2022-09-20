package com.aliyun.svideo.editor.effects.pip.msg;

import com.aliyun.Visible;

@Visible
public class PipRadiusMsg {

    public float progress;

    public PipRadiusMsg progress(float progress){
        this.progress = progress;
        return this;
    }
}
