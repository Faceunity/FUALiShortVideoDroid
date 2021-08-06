package com.aliyun.svideo.editor.msg.body;

public class SharpProgressMsg {
    private float progress;

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public SharpProgressMsg progress(float progress){
        this.progress = progress;
        return this;
    }
}
