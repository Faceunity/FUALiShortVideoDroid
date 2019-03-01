/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.media;


import com.aliyun.common.buffer.Allocator;
import com.aliyun.common.buffer.Recycler;

public class BitmapAllocator implements Allocator<ShareableBitmap> {

    private int mWidth;
    private int mHeight;

    public BitmapAllocator(int w, int h) {
        mWidth = w;
        mHeight = h;
    }

    @Override
    public ShareableBitmap allocate(Recycler<ShareableBitmap> recycler, ShareableBitmap reused) {
        if (reused != null) {
            reused.reset();
            return reused;
        }

        return new ShareableBitmap(recycler, mWidth, mHeight);
    }

    @Override
    public void recycle(ShareableBitmap object) {

    }

    @Override
    public void release(ShareableBitmap object) {
        object.getData().recycle();
    }

}
