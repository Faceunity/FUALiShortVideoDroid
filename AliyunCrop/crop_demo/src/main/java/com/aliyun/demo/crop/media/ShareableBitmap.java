/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.crop.media;

import android.graphics.Bitmap;

import com.aliyun.common.buffer.AbstractAtomicShareable;
import com.aliyun.common.buffer.Recycler;

public class ShareableBitmap extends AbstractAtomicShareable<ShareableBitmap> {

    private final Bitmap mData;
    private boolean isDataUsed;

    public ShareableBitmap(Recycler<ShareableBitmap> recycler, int w, int h) {
        super(recycler);
        mData = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
    }

    public ShareableBitmap(Bitmap bitmap) {
        super(null);

        mData = bitmap;
    }

    public boolean isDataUsed() {
        return isDataUsed;
    }

    public void setDataUsed(boolean dataUsed) {
        isDataUsed = dataUsed;
    }

    @Override
    protected void onLastRef() {
        if (mRecycler != null) {
            mRecycler.recycle(this);
        } else {
            if(!mData.isRecycled()) {
                mData.recycle();
            }
        }
    }

    public Bitmap getData() {
        isDataUsed = true;
        return mData;
    }

}
