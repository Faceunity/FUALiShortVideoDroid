/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.svideo.editor.editor.thumblinebar;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.aliyun.svideo.editor.R;
import com.aliyun.svideo.sdk.external.struct.common.VideoDisplayMode;
import com.aliyun.svideo.sdk.external.thumbnail.AliyunIThumbnailFetcher;

public class ThumbRecyclerAdapter extends RecyclerView.Adapter<ThumbRecyclerAdapter.ThumbnailViewHolder> {
    private static final String TAG = "ThumbRecyclerAdapter";
    private AliyunIThumbnailFetcher mFetcher;
    private int mCount;
    private long mInterval = 0;
    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_FOOTER = 2;
    private static final int VIEW_TYPE_THUMBNAIL = 3;
    private final int mScreenWidth;

    public ThumbRecyclerAdapter(int count, int duration, AliyunIThumbnailFetcher fetcher, int screenWidth, int thumbnailWidth, int thumbnailHeight) {
        mInterval = duration / count;
        this.mFetcher = fetcher;
        this.mCount = count;
        this.mScreenWidth = screenWidth;
        mFetcher.setParameters(thumbnailWidth, thumbnailHeight, AliyunIThumbnailFetcher.CropMode.Mediate, VideoDisplayMode.SCALE, 1);
    }

    public void setData(int count, int duration, AliyunIThumbnailFetcher fetcher, int screenWidth, int thumbnailWidth, int thumbnailHeight) {

        if (mInterval * count != duration && mCacheBitmaps.size() != 0) {
            Log.i(TAG, "setData: clear cache");
            mCacheBitmaps.clear();
            cacheBitmaps();
        } else {
            mInterval = duration / count;
        }
        this.mFetcher = fetcher;
        this.mCount = count;
        mFetcher.setParameters(thumbnailWidth, thumbnailHeight, AliyunIThumbnailFetcher.CropMode.Mediate, VideoDisplayMode.SCALE, 1);

    }

    @Override
    public ThumbnailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ThumbnailViewHolder holder;
        View itemView;
        switch (viewType) {
        case VIEW_TYPE_HEADER:
        case VIEW_TYPE_FOOTER:
            itemView = new View(parent.getContext());
            itemView.setLayoutParams(new ViewGroup.LayoutParams(mScreenWidth / 2, ViewGroup.LayoutParams.MATCH_PARENT));
            itemView.setBackgroundColor(Color.TRANSPARENT);
            holder = new ThumbnailViewHolder(itemView);
            return holder;
        default:
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.aliyun_svideo_layout_item_timeline_thumbnail, parent, false);
            holder = new ThumbnailViewHolder(itemView);
            holder.mIvThumbnail = itemView.findViewById(R.id.iv_thumbnail);
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(ThumbnailViewHolder holder, int position) {
        if (position != 0 && position != mCount + 1) {
            if (mInterval == 0) {
                mInterval = mFetcher.getTotalDuration() / mCount;
            }
            requestFetchThumbnail(holder, position);
        }
    }

    private SparseArray<Bitmap> mCacheBitmaps = new SparseArray<>();

    private void requestFetchThumbnail(final ThumbnailViewHolder holder, final int position) {
        long[] times = {(position - 1) * mInterval + mInterval / 2};

        Bitmap bitmap = mCacheBitmaps.get(position);
        if (bitmap != null && !bitmap.isRecycled()) {
            holder.mIvThumbnail.setImageBitmap(bitmap);
            return;
        }
        Log.d(TAG, "requestThumbnailImage() times :" + times[0] + " ,position = " + position);
        mFetcher.requestThumbnailImage(times, new AliyunIThumbnailFetcher.OnThumbnailCompletion() {

            private int vecIndex = 1;

            @Override
            public void onThumbnailReady(Bitmap frameBitmap, long l) {
                if (frameBitmap != null && !frameBitmap.isRecycled()) {
                    Log.i(TAG, "onThumbnailReady  put: " + position + " ,l = " + l / 1000);
                    holder.mIvThumbnail.setImageBitmap(frameBitmap);
                    //缓存bitmap
                    mCacheBitmaps.put(position, frameBitmap);
                } else {
                    if (position == 0) {
                        vecIndex = 1;
                    } else if (position == mCount + 1) {
                        vecIndex = -1;
                    }
                    int np = position + vecIndex;
                    Log.i(TAG, "requestThumbnailImage  failure: thisPosition = " + position + "newPosition = " + np);
                    requestFetchThumbnail(holder, np);
                }
            }

            @Override
            public void onError(int errorCode) {
                Log.w(TAG, "requestThumbnailImage error msg: " + errorCode);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCount == 0 ? 0 : mCount + 2;//这里加上前后部分的view
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_HEADER;
        } else if (position == mCount + 1) {
            return VIEW_TYPE_FOOTER;
        } else {
            return VIEW_TYPE_THUMBNAIL;
        }
    }

    @Override
    public void onViewRecycled(ThumbnailViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.mIvThumbnail != null) {
            holder.mIvThumbnail.setImageBitmap(null);
        }
    }

    /**
     * 缓存缩略图
     */
    public void cacheBitmaps() {
        //与缩略图的长度一致
        for (int i = 1; i < mCount + 1; i++) {
            requestFetchThumbnail(i);
        }
    }

    /**
     * 提前获取缩略图的
     * @param index count角标
     */
    private void requestFetchThumbnail(final int index) {
        final long[] times = {(index - 1) * mInterval + mInterval / 2};
        Log.d(TAG, "requestFetchThumbnail请求缓存: ");
        mFetcher.requestThumbnailImage(times, new AliyunIThumbnailFetcher.OnThumbnailCompletion() {
            private int vecIndex = 1;//取不到缩略图时再次取值时100毫秒的差值
            @Override
            public void onThumbnailReady(Bitmap frameBitmap, long l) {
                if (frameBitmap != null && !frameBitmap.isRecycled()) {
                    //缓存bitmap
                    mCacheBitmaps.put(index, frameBitmap);
                    Log.d(TAG, "缓存ThumbnailReady put，time = " + l / 1000 + ", position = " + index);
                } else {
                    if (index == 0) {
                        vecIndex = 1;
                    } else if (index == mCount - 1) {
                        vecIndex = -100;
                    }
                    int np = index + vecIndex;
                    Log.i(TAG, "缓存ThumbnailReady put，failure  time = " + l / 1000);
                    requestFetchThumbnail(np);
                }
            }

            @Override
            public void onError(int errorCode) {
                Log.w(TAG, "requestThumbnailImage error msg: " + errorCode);
            }
        });
    }

    public class ThumbnailViewHolder extends RecyclerView.ViewHolder {
        ImageView mIvThumbnail;

        ThumbnailViewHolder(View itemView) {
            super(itemView);
        }
    }
}
