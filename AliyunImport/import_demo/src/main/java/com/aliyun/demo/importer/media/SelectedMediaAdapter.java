/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.aliyun.demo.importer.media;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.aliyun.demo.importer.MediaImageLoader;
import com.aliyun.demo.importer.R;

public class SelectedMediaAdapter extends RecyclerView.Adapter<SelectedMediaViewHolder> {
    private List<MediaInfo> mDataList = new ArrayList<>();
    private MediaImageLoader mImageLoader;

    private OnItemViewCallback mItemViewCallback;
    private long mMaxDuration;
    private long mCurrDuration;
    public SelectedMediaAdapter(MediaImageLoader imageLoader, long maxDuration) {
        this.mImageLoader = imageLoader;
        this.mMaxDuration = maxDuration;
    }

    @Override
    public SelectedMediaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.aliyun_svideo_import_layout_selected_video_item
            ,parent , false);
        ImageView ivPhoto = (ImageView) itemView.findViewById(R.id.iv_photo);
        ImageView ivDelete = (ImageView) itemView.findViewById(R.id.iv_delete);
        TextView tvDuration = (TextView) itemView.findViewById(R.id.tv_duration);
        SelectedMediaViewHolder holder = new SelectedMediaViewHolder(itemView,
                ivPhoto,
                ivDelete,
                tvDuration,
                mImageLoader);
        holder.setCallback(new SelectedMediaViewHolder.OnItemCallback() {
            @Override
            public void onPhotoClick(SelectedMediaViewHolder holder, int position) {
                if(mItemViewCallback != null) {
                    mItemViewCallback.onItemPhotoClick(mDataList.get(position), position);
                }
            }

            @Override
            public void onItemDelete(SelectedMediaViewHolder holder, int position) {
                if(position >= mDataList.size() || position < 0){
                    return ;
                }
                if(mItemViewCallback != null) {
                    mItemViewCallback.onItemDeleteClick(mDataList.get(position));
                }
                removeIndex(position);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(SelectedMediaViewHolder holder, int position) {
        if(mDataList != null && position < mDataList.size()) {
            final MediaInfo info = mDataList.get(position);
            if(info != null) {
                holder.updateData(position, info);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    public void addMedia(MediaInfo info) {
//        mDataList.add(0, info);//保证最近添加的视频位于备选区的开始位置
        mDataList.add(info);//保证最近添加的视频位于备选区的最后
        notifyDataSetChanged();
        mCurrDuration += info.duration;
        if(mItemViewCallback != null) {
            if (mCurrDuration > mMaxDuration) {
                mItemViewCallback.onDurationChange(mCurrDuration, true);
            }else {
                mItemViewCallback.onDurationChange(mCurrDuration, false);
            }
        }    }

    private void removeIndex(int position) {
        MediaInfo info = mDataList.get(position);
        if(info != null) {
            mCurrDuration -= info.duration;
            mDataList.remove(position);
            notifyDataSetChanged();
            if(mItemViewCallback != null) {
                if (mCurrDuration > mMaxDuration) {
                    mItemViewCallback.onDurationChange(mCurrDuration, true);
                }else {
                    mItemViewCallback.onDurationChange(mCurrDuration, false);
                }
            }
        }
    }

    public void swap(int pos1, int pos2) {
        Collections.swap(mDataList, pos1, pos2);
        notifyItemMoved(pos1, pos2);
    }

    public void swap(SelectedMediaViewHolder viewHolder, SelectedMediaViewHolder target) {
        int pos1 = viewHolder.getAdapterPosition();
        int pos2 = target.getAdapterPosition();
        Collections.swap(mDataList, pos1, pos2);
        viewHolder.updatePosition(pos2);
        target.updatePosition(pos1);
        notifyItemMoved(pos1, pos2);
    }

    public void changeDurationPosition(int position, long duration) {
        MediaInfo mediaInfo = mDataList.get(position);
        if(mediaInfo != null) {
            mCurrDuration -= mediaInfo.duration;
            mCurrDuration += duration;
            if(mItemViewCallback != null) {
                if (mCurrDuration > mMaxDuration) {
                    mItemViewCallback.onDurationChange(mCurrDuration, true);
                }else {
                    mItemViewCallback.onDurationChange(mCurrDuration, false);
                }
            }
            notifyItemChanged(position);
        }
    }

    public boolean contains(MediaInfo info) {
        return mDataList.contains(info);
    }

    public void setItemViewCallback(OnItemViewCallback itemViewCallback) {
        mItemViewCallback = itemViewCallback;
    }

    public interface OnItemViewCallback {
        void onItemPhotoClick(MediaInfo info, int position);
        void onItemDeleteClick(MediaInfo info);
        void onDurationChange(long currDuration, boolean isReachedMaxDuration);
    }
}
