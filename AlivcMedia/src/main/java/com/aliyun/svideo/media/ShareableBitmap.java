/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.media;

import android.graphics.Bitmap;

import com.aliyun.common.buffer.AbstractAtomicShareable;
import com.aliyun.common.buffer.Recycler;

public class ShareableBitmap extends AbstractAtomicShareable<ShareableBitmap> {

    private final Bitmap mBitmap;

    public
    ShareableBitmap(Recycler<ShareableBitmap> recycler, int w, int h) {
        super(recycler);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    }

    public
    ShareableBitmap(Bitmap bitmap) {
        super(null);

        mBitmap = bitmap;
    }

    @Override
    protected void onLastRef() {
        if (mRecycler != null) {
            mRecycler.recycle(this);
        } else {
            mBitmap.recycle();
        }
    }

    public Bitmap getData() {
        return mBitmap;
    }

}
