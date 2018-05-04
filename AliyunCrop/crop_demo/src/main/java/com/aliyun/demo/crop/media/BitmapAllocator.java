/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.crop.media;


import com.aliyun.common.buffer.Allocator;
import com.aliyun.common.buffer.Recycler;

public class BitmapAllocator implements Allocator<ShareableBitmap> {

    private final int _Width;
    private final int _Height;

    public BitmapAllocator(int w, int h) {
        _Width = w;
        _Height = h;
    }

    @Override
    public ShareableBitmap allocate(Recycler<ShareableBitmap> recycler, ShareableBitmap reused) {
        if (reused != null) {
            reused.reset();
            return reused;
        }

        return new ShareableBitmap(recycler, _Width, _Height);
    }

    @Override
    public void recycle(ShareableBitmap object) {

    }

    @Override
    public void release(ShareableBitmap object) {
        object.getData().recycle();
    }

}
