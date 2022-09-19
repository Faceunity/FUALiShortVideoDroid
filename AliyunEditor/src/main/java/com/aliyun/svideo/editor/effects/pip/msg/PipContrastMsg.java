package com.aliyun.svideo.editor.effects.pip.msg;

import com.aliyun.Visible;

@Visible
public class PipContrastMsg {

    public float progress;

    public PipContrastMsg progress(float progress){
        this.progress = progress;
        return this;
    }
}
