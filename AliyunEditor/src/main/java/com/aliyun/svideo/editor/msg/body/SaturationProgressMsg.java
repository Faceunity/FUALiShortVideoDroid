package com.aliyun.svideo.editor.msg.body;

public class SaturationProgressMsg {
    private float progress;

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public SaturationProgressMsg progress(float progress){
        this.progress = progress;
        return this;
    }
}
