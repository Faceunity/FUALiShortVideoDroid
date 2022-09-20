package com.aliyun.svideo.editor.effects.pip.msg;

import com.aliyun.Visible;

@Visible
public class PipMoveMsg {

    public float progress;

    public PipMoveMsg progress(float progress){
        this.progress = progress;
        return this;
    }
}
