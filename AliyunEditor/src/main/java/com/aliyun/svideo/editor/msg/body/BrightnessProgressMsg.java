package com.aliyun.svideo.editor.msg.body;

public class BrightnessProgressMsg {
    private float progress;

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public BrightnessProgressMsg progress(float progress) {
        this.progress = progress;
        return this;
    }
}
