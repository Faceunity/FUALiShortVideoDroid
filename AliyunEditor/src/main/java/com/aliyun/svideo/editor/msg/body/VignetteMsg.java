package com.aliyun.svideo.editor.msg.body;

public class VignetteMsg {
    private float progress;

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public VignetteMsg progress(float progress){
        this.progress = progress;
        return this;
    }
}
