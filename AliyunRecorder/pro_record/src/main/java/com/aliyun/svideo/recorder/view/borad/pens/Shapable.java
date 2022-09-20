package com.aliyun.svideo.recorder.view.borad.pens;

import android.graphics.Path;

public interface Shapable {
    public Path getPath();

    public FirstCurrentPosition getFirstLastPoint();

    void setShap(ShapesInterface shape);
}
