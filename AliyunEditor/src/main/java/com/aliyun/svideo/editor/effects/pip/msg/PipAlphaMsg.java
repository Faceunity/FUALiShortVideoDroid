package com.aliyun.svideo.editor.effects.pip.msg;

import com.aliyun.Visible;

@Visible
public class PipAlphaMsg {

    public float progress;

    public PipAlphaMsg progress(float progress){
        this.progress = progress;
        return this;
    }
}
