package com.aliyun.svideo.editor.effects.pip.msg;

import com.aliyun.Visible;

@Visible
public class PipVolumeMsg {

    public int progress;

    public PipVolumeMsg progress(int progress){
        this.progress = progress;
        return this;
    }
}
