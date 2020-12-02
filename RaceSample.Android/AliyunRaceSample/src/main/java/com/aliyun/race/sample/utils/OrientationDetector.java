/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.race.sample.utils;

import android.content.Context;
import android.view.OrientationEventListener;

public class OrientationDetector extends OrientationEventListener {
    int mOrientation;

    public interface OrientationChangedListener {
        void onOrientationChanged();
    }
    private OrientationChangedListener listener;
    public void setOrientationChangedListener(OrientationChangedListener l) {
        listener = l;
    }
    public OrientationDetector(Context context ) {
        super(context );
    }
    @Override
    public void onOrientationChanged(int orientation) {
        this.mOrientation = orientation;
        if (listener != null) {
            listener.onOrientationChanged();
        }
    }

    public int getOrientation() {
        return mOrientation;
    }
}
