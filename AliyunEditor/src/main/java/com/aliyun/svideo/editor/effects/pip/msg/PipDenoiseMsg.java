package com.aliyun.svideo.editor.effects.pip.msg;

import com.aliyun.Visible;

@Visible
public class PipDenoiseMsg {

    public int progress;

    public PipDenoiseMsg progress(int progress){
        this.progress = progress;
        return this;
    }
}
