package com.aliyun.svideo.editor.effects.pip.msg;

import com.aliyun.Visible;

@Visible
public class PipSaturationMsg {

    public float progress;

    public PipSaturationMsg progress(float progress){
        this.progress = progress;
        return this;
    }
}
