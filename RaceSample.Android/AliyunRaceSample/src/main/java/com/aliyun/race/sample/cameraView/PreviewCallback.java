package com.aliyun.race.sample.cameraView;

public interface PreviewCallback {
    void onPreviewFrame(byte[] data, int width, int height, int cameraFacing);
}
