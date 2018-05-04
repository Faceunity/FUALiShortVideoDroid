/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.importer.media;

import android.graphics.Bitmap;

import com.aliyun.common.buffer.AtomicShareable;
import com.aliyun.common.buffer.Recycler;

public class ShareableBitmap extends AtomicShareable<ShareableBitmap> {

    private final Bitmap _Data;

    public
    ShareableBitmap(Recycler<ShareableBitmap> recycler, int w, int h) {
        super(recycler);
        _Data = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    }

    public
    ShareableBitmap(Bitmap bitmap) {
        super(null);

        _Data = bitmap;
    }

    @Override
    protected void onLastRef() {
        if (_Recycler != null) {
            _Recycler.recycle(this);
        } else {
            _Data.recycle();
        }
    }

    public Bitmap getData() {
        return _Data;
    }

}
