package com.aliyun.svideo.editor.effects.pip.msg;

import com.aliyun.Visible;
import com.aliyun.svideo.editor.msg.body.ContrastProgressMsg;

@Visible
public class PipScaleMsg {

    public float progress;

    public PipScaleMsg progress(float progress){
        this.progress = progress;
        return this;
    }
}
