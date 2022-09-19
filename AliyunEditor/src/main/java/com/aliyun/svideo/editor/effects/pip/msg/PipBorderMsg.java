package com.aliyun.svideo.editor.effects.pip.msg;

import com.aliyun.Visible;

@Visible
public class PipBorderMsg {

    public float progress;

    public PipBorderMsg progress(float progress){
        this.progress = progress;
        return this;
    }
}
