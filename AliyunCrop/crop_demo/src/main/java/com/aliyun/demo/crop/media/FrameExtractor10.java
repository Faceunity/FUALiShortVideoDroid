/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.crop.media;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.SparseArray;

import com.aliyun.common.buffer.SynchronizedPool;
import com.aliyun.common.logger.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FrameExtractor10 {

    private static final String TAG = "FrameExtractor";

    public interface Callback {
        void onFrameExtracted(ShareableBitmap bitmap, long timestamp);
    }

    private final ExecutorService mExecutor;

    private final MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();

    private final SynchronizedPool<ShareableBitmap> mBitmapPool;

    private final Canvas mCanvas = new Canvas();
    private final Rect mRect = new Rect();
    private SparseArray mMetaDataCache = new SparseArray();
    private String mVideoPath;

    private class Task extends AsyncTask<Void, Void, ShareableBitmap> {

        private final long _TimestampNano;

        private Callback _Callback;

        public Task(Callback callback, long timestamp_nano) {
            _Callback = callback;
            _TimestampNano = timestamp_nano;
        }

        @Override
        protected ShareableBitmap doInBackground(Void... params) {
            if (isCancelled()) {
                return null;
            }
            long micro = TimeUnit.NANOSECONDS.toMicros(_TimestampNano);
            Bitmap bmp = mRetriever.getFrameAtTime(micro);

            if (bmp == null) {
                return null;
            }

            if (isCancelled()) {
                return null;
            }

            ShareableBitmap bitmap = mBitmapPool.allocate();

            mCanvas.setBitmap(bitmap.getData());
            Rect srcRect = new Rect();
            int bmpWidth = bmp.getWidth();
            int bmpHeight = bmp.getHeight();
            if (bmpWidth >= bmpHeight) {
                srcRect.left = (bmpWidth - bmpHeight) / 2;
                srcRect.right = (bmpWidth - bmpHeight) / 2 + bmpHeight;
                srcRect.top = 0;
                srcRect.bottom = bmpHeight;
            } else {
                srcRect.left = 0;
                srcRect.right = bmpWidth;
                srcRect.top = (bmpHeight - bmpWidth) / 2;
                srcRect.bottom = (bmpHeight - bmpWidth) / 2 + bmpWidth;
            }
            mCanvas.drawBitmap(bmp, srcRect, mRect, null);

            bmp.recycle();

            return bitmap;
        }

        @Override
        protected void onCancelled(ShareableBitmap bitmap) {
            if (bitmap != null) {
                bitmap.release();
            }
        }

        @Override
        protected void onPostExecute(ShareableBitmap bitmap) {
            _Callback.onFrameExtracted(bitmap, _TimestampNano);
        }

    }


    public FrameExtractor10() {
        mExecutor = Executors.newSingleThreadExecutor();

        int width = 128;
        int height = 128;
        mBitmapPool = new SynchronizedPool<>(new BitmapAllocator(width, height));
        mRect.set(0, 0, width, height);
    }

    public AsyncTask<Void, Void, ShareableBitmap> newTask(Callback callback, long timestamp_nano) {
        return new Task(callback, timestamp_nano).executeOnExecutor(mExecutor);
    }

    public boolean setDataSource(String source) {
        try {
            mVideoPath = source;
            mRetriever.setDataSource(source);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public long getVideoDuration() {
        Object result;
        if ((result = mMetaDataCache.get(MediaMetadataRetriever.METADATA_KEY_DURATION)) != null) {
            return (Long) result;
        } else if (mVideoPath != null) {
            String durationStr = mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if(durationStr != null && !"".equals(durationStr)) {
                Long duration = Long.parseLong(durationStr);
                mMetaDataCache.put(MediaMetadataRetriever.METADATA_KEY_DURATION, duration);
                return duration;
            }else {
                Logger.getDefaultLogger().e("Retrieve video duration failed");
                return 0;
            }
        } else {
            Logger.getDefaultLogger().e("Has no video source,so duration is 0");
            return 0;
        }
    }

    public void release() {
        mExecutor.shutdownNow();
        while (true) {
            try {
                if (mExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                    break;
                }
            } catch (InterruptedException e) {
            }
        }

        mRetriever.release();

        mBitmapPool.release();
    }

}
