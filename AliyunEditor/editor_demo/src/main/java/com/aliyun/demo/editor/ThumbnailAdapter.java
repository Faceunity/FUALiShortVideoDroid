/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.editor;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.aliyun.common.media.ShareableBitmap;
import com.aliyun.qupai.editor.AliyunIThumbnailFetcher;
import com.aliyun.struct.common.ScaleMode;

public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ThumbnailViewHolder> {
    //    private SparseArray<Bitmap> mBitmapList = new SparseArray<>();
    private AliyunIThumbnailFetcher mFetcher;
    private int mCount;
    private long mInterval = 0;
    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_FOOTER = 2;
    private static final int VIEW_TYPE_THUMBNAIL = 3;

    private final int mScreenWidth;
    public class ThumbnailViewHolder extends RecyclerView.ViewHolder {
        ImageView mIvThumbnail;

        public ThumbnailViewHolder(View itemView) {
            super(itemView);
        }
    }

    public ThumbnailAdapter(int count, AliyunIThumbnailFetcher fetcher, int screenWidth) {
        this.mFetcher = fetcher;
        this.mCount = count;
        this.mScreenWidth = screenWidth;
        mFetcher.setParameters(60, 60, AliyunIThumbnailFetcher.CropMode.Mediate, ScaleMode.LB, 30);
    }

    @Override
    public ThumbnailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ThumbnailViewHolder holder;
        View itemView;
        switch (viewType) {
            case VIEW_TYPE_HEADER:
            case VIEW_TYPE_FOOTER:
                itemView = new View(parent.getContext());
                itemView.setLayoutParams(new ViewGroup.LayoutParams(mScreenWidth/2, ViewGroup.LayoutParams.MATCH_PARENT));
                itemView.setBackgroundColor(Color.TRANSPARENT);
                holder = new ThumbnailViewHolder(itemView);
                return holder;
            default:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.aliyun_svideo_layout_item_timeline_thumbnail, parent, false);
                holder = new ThumbnailViewHolder(itemView);
                holder.mIvThumbnail = (ImageView) itemView.findViewById(R.id.iv_thumbnail);
                return holder;
        }

    }

    @Override
    public void onBindViewHolder(ThumbnailViewHolder holder, int position) {
        if(position != 0 && position != mCount+1) {
            if (mInterval == 0) {
                mInterval = mFetcher.getTotalDuration() / mCount;
            }
            requestFetchThumbnail(holder, position);
        }
    }

    private void requestFetchThumbnail(final ThumbnailViewHolder holder, final int position){
        long[] times = {(position-1) * mInterval};
        mFetcher.requestThumbnailImage(times,
                new AliyunIThumbnailFetcher.OnThumbnailCompletion() {

                    private int vecIndex = 1;
                    @Override
                    public void onThumbnailReady(ShareableBitmap frameBitmap, long time) {
                        if (frameBitmap != null) {
                            holder.mIvThumbnail.setImageBitmap(frameBitmap.getData());
                            holder.mIvThumbnail.setTag(frameBitmap);
                        }else{
                            if(position == 0){
                                vecIndex = 1;
                            }else if(position == mCount + 1){
                                vecIndex = -1;
                            }
                            int np = position + vecIndex;
                            requestFetchThumbnail(holder, np);
                        }
                    }

                    @Override
                    public void onError(int errorCode) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return mCount == 0?0:mCount + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0) {
            return VIEW_TYPE_HEADER;
        }else if(position == mCount+1) {
            return VIEW_TYPE_FOOTER;
        }else {
            return VIEW_TYPE_THUMBNAIL;
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onViewRecycled(ThumbnailViewHolder holder) {
        super.onViewRecycled(holder);
        if(holder.mIvThumbnail != null) {
//            Object tag = holder.mIvThumbnail.getTag();
//            if (tag instanceof ShareableBitmap) {
//                ((ShareableBitmap) tag).release();
//                holder.mIvThumbnail.setTag(null);
//            }
            holder.mIvThumbnail.setImageBitmap(null);
        }
    }

    @Override
    public void onViewDetachedFromWindow(ThumbnailViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }
}
